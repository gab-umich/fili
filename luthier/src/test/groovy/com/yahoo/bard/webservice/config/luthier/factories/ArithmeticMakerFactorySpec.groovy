// Copyright 2019 Oath Inc.
// Licensed under the terms of the Apache license. Please see LICENSE.md file distributed with this work for terms.
package com.yahoo.bard.webservice.config.luthier.factories

import com.yahoo.bard.webservice.data.config.metric.makers.LongSumMaker

import static com.yahoo.bard.webservice.druid.model.postaggregation.ArithmeticPostAggregation.ArithmeticPostAggregationFunction.MINUS
import static com.yahoo.bard.webservice.druid.model.postaggregation.ArithmeticPostAggregation.ArithmeticPostAggregationFunction.PLUS;
import static com.yahoo.bard.webservice.druid.model.postaggregation.ArithmeticPostAggregation.ArithmeticPostAggregationFunction.MULTIPLY;
import static com.yahoo.bard.webservice.druid.model.postaggregation.ArithmeticPostAggregation.ArithmeticPostAggregationFunction.DIVIDE;

import com.yahoo.bard.webservice.application.ObjectMappersSuite
import com.yahoo.bard.webservice.config.luthier.Factory
import com.yahoo.bard.webservice.config.luthier.LuthierIndustrialPark
import com.yahoo.bard.webservice.data.config.metric.makers.ArithmeticMaker
import com.yahoo.bard.webservice.data.config.metric.makers.MetricMaker
import com.yahoo.bard.webservice.druid.model.postaggregation.ArithmeticPostAggregation

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode

import spock.lang.Shared
import spock.lang.Specification

class ArithmeticMakerFactorySpec extends Specification {

    LuthierIndustrialPark luthierIndustrialPark = new LuthierIndustrialPark.Builder().build()

    @Shared String plusConfig = """
{
  "arithmeticPLUS": {
    "type": "ArithmeticMaker",
    "function": "PLUS"
  }
}
"""

    @Shared String minusConfig = """
{
  "arithmeticPLUS": {
    "type": "ArithmeticMaker",
    "function": "MINUS"
  }
}
"""

    @Shared String multiplyConfig = """
{
  "arithmeticPLUS": {
    "type": "ArithmeticMaker",
    "function": "MULTIPLY"
  }
}
"""

    @Shared String divideConfig = """
{
  "arithmeticPLUS": {
    "type": "ArithmeticMaker",
    "function": "DIVIDE"
  }
}
"""

    ObjectMapper objectReader = new ObjectMappersSuite().mapper

    def "Test creating factory from JSON string with function #operation"() {
        setup: "Build a factory"
            JsonNode plusNode = objectReader.readTree(config);
            Factory<MetricMaker> factory = new ArithmeticMakerFactory()

        when:
            MetricMaker actual = factory.build(
                    "arithmeticPLUS",
                    (ObjectNode) plusNode.get("arithmeticPLUS"),
                    luthierIndustrialPark
            )

        then: "parse plusConfig"
            actual instanceof ArithmeticMaker
            ((ArithmeticMaker)actual).function == operation

        where:
        config         | operation
        plusConfig     | PLUS
        minusConfig    | MINUS
        multiplyConfig | MULTIPLY
        divideConfig   | DIVIDE
    }

    def "building longSum metricMaker correctly through a LIP"() {
        setup: "Build LIP, and then extract the metricMaker"
            LuthierIndustrialPark park = new LuthierIndustrialPark.Builder().build()
            LongSumMaker longSumMaker = park.getMetricMaker("longSum")
        expect:
            // a pretty "dumb" check that guarantees that there is no exception in build
            // also guarantees that the factoryMap aliases contain "longSum"
            longSumMaker instanceof LongSumMaker
    }
}
