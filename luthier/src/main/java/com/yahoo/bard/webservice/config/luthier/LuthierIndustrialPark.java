// Copyright 2019 Oath Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.config.luthier;

import com.fasterxml.jackson.databind.JsonNode;
import com.yahoo.bard.webservice.data.config.ConfigurationLoader;
import com.yahoo.bard.webservice.data.config.ResourceDictionaries;
import com.yahoo.bard.webservice.data.dimension.Dimension;
import com.yahoo.bard.webservice.data.dimension.DimensionDictionary;
import com.yahoo.bard.webservice.data.dimension.KeyValueStore;
import com.yahoo.bard.webservice.data.dimension.MapStore;
import com.yahoo.bard.webservice.data.dimension.SearchProvider;
import com.yahoo.bard.webservice.data.dimension.impl.LuceneSearchProvider;
import com.yahoo.bard.webservice.data.dimension.impl.NoOpSearchProvider;
import com.yahoo.bard.webservice.data.dimension.impl.ScanSearchProvider;
import com.yahoo.bard.webservice.data.metric.MetricDictionary;
import com.yahoo.bard.webservice.table.LogicalTableDictionary;
import com.yahoo.bard.webservice.table.PhysicalTableDictionary;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Dependency Injection container for Config Objects configured via Luthier.
 */
public class LuthierIndustrialPark implements ConfigurationLoader {

    private static final String DOMAIN_NOT_FOUND = "'%s' is not found in SearchProviderConfig.json";
    private static final String FORMAT_MISMATCH = "Unexpected format encountered when parsing domain '%s'";
    private static final String UNKNOWN_SEARCH_PROVIDER = "Unknown search provider '%s' when processing domain '%s'";
    private final ResourceDictionaries resourceDictionaries;
    private final Map<String, Factory<Dimension>> dimensionFactories;
    private final FactoryPark<Dimension> dimensionFactoryPark;

    /**
     * Constructor.
     *
     * @param resourceDictionaries  The dictionaries to initialize the industrial park with.
     * @param dimensionFactories The map of factories for creating dimensions from external config
     */
    protected LuthierIndustrialPark(
            ResourceDictionaries resourceDictionaries,
            Map<String, Factory<Dimension>> dimensionFactories
    ) {
        this.resourceDictionaries = resourceDictionaries;
        this.dimensionFactories = dimensionFactories;
        Supplier<ObjectNode> dimensionConfig = new ResourceNodeSupplier("DimensionConfig.json");
        dimensionFactoryPark = new FactoryPark<>(dimensionConfig, dimensionFactories);
    }

/*
    LogicalTable getLogicalTable(String tableName);
    PhysicalTable getPhysicalTable(String tableName);
    LogicalMetric getLogicalMetric(String metricName);
    MetricMaker getMetricMaker(String makerName);
*/

    /**
     * Retrieve or build a dimension.
     *
     * @param dimensionName the name for the dimension to be provided.
     *
     * @return the dimension instance corresponding to this name.
     */
    public Dimension getDimension(String dimensionName) {
        DimensionDictionary dimensionDictionary = resourceDictionaries.getDimensionDictionary();
        if (dimensionDictionary.findByApiName(dimensionName) == null) {
            Dimension dimension = dimensionFactoryPark.buildEntity(dimensionName, this);
            dimensionDictionary.add(dimension);
        }
        return dimensionDictionary.findByApiName(dimensionName);
    }

    /**
     * Bare minimum that can work.
     *
     * @param domain name of the domain
     * @return a searchProvider that is built from the domain name, according to the SearchProviderConfig.json
     */
    public SearchProvider getSearchProvider(String domain) {
        Supplier<ObjectNode> searchProviderConfig = new ResourceNodeSupplier("SearchProviderConfig.json");
        JsonNode config = searchProviderConfig.get().get(domain);
        if (config == null) {
            String message = String.format(DOMAIN_NOT_FOUND, domain);
            throw new LuthierFactoryException(message);
        }
        try {
            String type = config.get("type").textValue();
            Class searchProviderClass = Class.forName(type);
            switch (type) {
                case "com.yahoo.bard.webservice.data.dimension.impl.NoOpSearchProvider":
                    int queryWeightLimit = config.get("queryWeightLimit").intValue();
                    return new NoOpSearchProvider(queryWeightLimit);

                case "com.yahoo.bard.webservice.data.dimension.impl.LuceneSearchProvider":
                    String indexPath = config.get("indexPath").textValue();
                    int maxResults = config.get("maxResults").intValue();
                    int searchTimeout = config.get("searchTimeout").intValue();
                    return new LuceneSearchProvider(indexPath, maxResults, searchTimeout);

                case "com.yahoo.bard.webservice.data.dimension.impl.ScanSearchProvider":
                    return new ScanSearchProvider();

                default:
                    String message = String.format(UNKNOWN_SEARCH_PROVIDER, type, domain);
                    throw new LuthierFactoryException(message);
            }
        } catch (ClassNotFoundException | NullPointerException e) {
            String message = String.format(FORMAT_MISMATCH, domain);
            throw new LuthierFactoryException(message, e);
        }
    }

    /**
     * Bare minimum.
     *
     * @param keyValueStoreName identifier of the keyValueStore
     * @return the keyValueStore built according to the keyValueStore identifier
     * @throws UnsupportedOperationException when passed in redisStore.
     */
    public KeyValueStore getKeyValueStore(String keyValueStoreName) throws UnsupportedOperationException {
        switch (keyValueStoreName) {
            // TODO: Magic values!
            case "com.yahoo.bard.webservice.data.dimension.RedisStore":
                throw new UnsupportedOperationException(keyValueStoreName);
            default:
                return new MapStore();
        }
    }


    @Override
    public void load() {
        dimensionFactoryPark.fetchConfig().fieldNames().forEachRemaining(this::getDimension);
    }

    @Override
    public DimensionDictionary getDimensionDictionary() {
        return resourceDictionaries.getDimensionDictionary();
    }

    @Override
    public MetricDictionary getMetricDictionary() {
        return resourceDictionaries.getMetricDictionary();
    }

    @Override
    public LogicalTableDictionary getLogicalTableDictionary() {
        return resourceDictionaries.getLogicalDictionary();
    }

    @Override
    public PhysicalTableDictionary getPhysicalTableDictionary() {
        return resourceDictionaries.getPhysicalDictionary();
    }

    @Override
    public ResourceDictionaries getDictionaries() {
        return resourceDictionaries;
    }

    /**
     * Builder object to construct a new LuthierIndustrialPark instance with.
     */
    public static class Builder {

        private Map<String, Factory<Dimension>> dimensionFactories;

        private final ResourceDictionaries resourceDictionaries;

        /**
         * Constructor.
         *
         * @param resourceDictionaries a class that contains resource dictionaries including
         *                             PhysicalTableDictionary, DimensionDictionary, etc.
         */
        public Builder(ResourceDictionaries resourceDictionaries) {
            this.resourceDictionaries = resourceDictionaries;
            dimensionFactories = getDefaultDimensionFactories();
        }

        /**
         * Constructor.
         * <p>
         * Default to use an empty resource dictionary.
         */
        public Builder() {
            this(new ResourceDictionaries());
        }

        public Map<String, Factory<Dimension>> getDefaultDimensionFactories() {
            return new LinkedHashMap<>();
        }

        /**
         * specifies dimension factories when initializing a builder.
         *
         * @param factories a factory of a specific dimension
         * @return the builder object
         */
        public Builder withDimensionFactories(Map<String, Factory<Dimension>> factories) {
            this.dimensionFactories = factories;
            return this;
        }

        /**
         * specifies a dimension when initializing a builder.
         *
         * @param name the name of the factory
         * @param factory factory to supply
         * @return the builder object
         */
        public Builder withDimensionFactory(String name, Factory<Dimension> factory) {
            dimensionFactories.put(name, factory);
            return this;
        }

        /**
         * build function to construct an instance of LuthierIndustrialPark.
         *
         * @return the LuthierIndustrialPark with the specified resourceDictionaries and dimensionFactories
         */
        public LuthierIndustrialPark build() {
            return new LuthierIndustrialPark(resourceDictionaries, new LinkedHashMap<>(dimensionFactories));
        }
    }
}
