// Copyright 2019 Oath Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.config.luthier.factories;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yahoo.bard.webservice.config.luthier.ConceptType;
import com.yahoo.bard.webservice.config.luthier.Factory;
import com.yahoo.bard.webservice.config.luthier.LuthierFactoryException;
import com.yahoo.bard.webservice.config.luthier.LuthierIndustrialPark;
import com.yahoo.bard.webservice.config.luthier.LuthierValidationUtils;
import com.yahoo.bard.webservice.data.config.metric.makers.AggregationAverageMaker;
import com.yahoo.bard.webservice.data.config.metric.makers.MetricMaker;
import com.yahoo.bard.webservice.data.time.ZonelessTimeGrain;
import com.yahoo.bard.webservice.util.GranularityParseException;

public class AggregationAverageMakerFactory implements Factory<MetricMaker> {
    private static final String AGGREGATION_AVG_MAKER = "AggregationAverageMaker";
    private static final String INNER_GRAIN = "innerGrain";
    private static final String GRAIN_NAME_NOT_EXPECTED = "granularity name '%s' is not recognized by the Luthier " +
            "module when building " + AGGREGATION_AVG_MAKER + " '%s'";

    @Override
    public MetricMaker build(String name, ObjectNode configTable, LuthierIndustrialPark resourceFactories) {
        LuthierValidationUtils.validateField(configTable.get(INNER_GRAIN), ConceptType.METRIC_MAKER, name, INNER_GRAIN);
        String grainName = configTable.get(INNER_GRAIN).textValue();
        try {
            ZonelessTimeGrain grain = (ZonelessTimeGrain) resourceFactories.getGranularityParser().parseGranularity(grainName);
            return new AggregationAverageMaker(resourceFactories.getMetricDictionary(), grain);
        } catch (GranularityParseException | ClassCastException e) {
            throw new LuthierFactoryException(String.format(GRAIN_NAME_NOT_EXPECTED, grainName, name), e);
        }
    }
}