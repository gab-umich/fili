// Copyright 2019 Oath Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.config.luthier.factories;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.yahoo.bard.webservice.config.luthier.Factory;
import com.yahoo.bard.webservice.config.luthier.LuthierIndustrialPark;
import com.yahoo.bard.webservice.data.config.metric.makers.LongSumMaker;
import com.yahoo.bard.webservice.data.config.metric.makers.MetricMaker;

public class LongSumMakerFactory implements Factory<MetricMaker> {
    @Override
    public MetricMaker build(String name, ObjectNode configTable, LuthierIndustrialPark resourceFactories) {
        return new LongSumMaker(resourceFactories.getMetricDictionary());
    }
}