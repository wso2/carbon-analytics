/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.StoreConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.aggregationbytimeperiod.AggregateByTimePeriod;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.AggregationConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.aggregationbytimeperiod.aggregationbytimerange.AggregateByTimeInterval;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.aggregationbytimeperiod.aggregationbytimerange.AggregateByTimeRange;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.aggregationbytimeperiod.aggregationbytimerange.AggregationByTimeRangeValue;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import io.siddhi.query.api.aggregation.TimePeriod;
import io.siddhi.query.api.annotation.Annotation;
import io.siddhi.query.api.definition.AggregationDefinition;
import io.siddhi.query.api.execution.query.selection.BasicSelector;
import io.siddhi.query.api.expression.Variable;

import java.util.ArrayList;
import java.util.List;

/**
 * Generator to create AggregationConfig
 */
public class AggregationConfigGenerator extends CodeSegmentsPreserver {
    private String siddhiAppString;

    public AggregationConfigGenerator(String siddhiAppString) {
        this.siddhiAppString = siddhiAppString;
    }

    /**
     * Generates AggregationConfig from the given Siddhi AggregationDefinition
     * @param aggregationDefinition             Siddhi AggregationDefinition object
     * @return                                  AggregationConfig object
     * @throws DesignGenerationException        Error while generating AggregationConfig
     */
    public AggregationConfig generateAggregationConfig(AggregationDefinition aggregationDefinition)
            throws DesignGenerationException {
        AggregationConfig aggregationConfig = new AggregationConfig();

        aggregationConfig.setName(aggregationDefinition.getId());
        aggregationConfig.setFrom(aggregationDefinition.getBasicSingleInputStream().getStreamId());

        // 'select' and 'groupBy'
        AttributesSelectionConfigGenerator attributesSelectionConfigGenerator =
                new AttributesSelectionConfigGenerator(siddhiAppString);
        if (aggregationDefinition.getSelector() instanceof BasicSelector) {
            preserveCodeSegment(aggregationDefinition.getSelector());
            BasicSelector selector = (BasicSelector) aggregationDefinition.getSelector();
            aggregationConfig.setSelect(attributesSelectionConfigGenerator.generateAttributesSelectionConfig(selector));
            aggregationConfig.setGroupBy(generateGroupBy(selector.getGroupByList()));
        } else {
            throw new DesignGenerationException("Selector of AggregationDefinition is not of class BasicSelector");
        }

        // 'aggregateByAttribute'
        aggregationConfig.setAggregateByAttribute(
                generateAggregateByAttribute(aggregationDefinition.getAggregateAttribute()));

        // 'aggregateByTime'
        aggregationConfig.setAggregateByTime(generateAggregateByTime(aggregationDefinition.getTimePeriod()));

        // 'store' and annotations
        StoreConfigGenerator storeConfigGenerator = new StoreConfigGenerator();
        AnnotationConfigGenerator annotationConfigGenerator = new AnnotationConfigGenerator();
        StoreConfig storeConfig = null;
        List<String> annotationList = new ArrayList<>();
        List<Annotation> annotationListObjects = new ArrayList<>();
        for (Annotation annotation : aggregationDefinition.getAnnotations()) {
            if (annotation.getName().equalsIgnoreCase("STORE")) {
                storeConfig = storeConfigGenerator.generateStoreConfig(annotation);
            } else {
                annotationListObjects.add(annotation);
                annotationList.add(annotationConfigGenerator.generateAnnotationConfig(annotation));
            }
        }
        aggregationConfig.setStore(storeConfig);
        aggregationConfig.setAnnotationList(annotationList);
        aggregationConfig.setAnnotationListObjects(annotationListObjects);

        preserveCodeSegmentsOf(annotationConfigGenerator, storeConfigGenerator, attributesSelectionConfigGenerator);
        preserveAndBindCodeSegment(aggregationDefinition, aggregationConfig);
        return aggregationConfig;
    }

    /**
     * Generates list of groupBy variables, from the given list of Siddhi Variables
     * @param groupByVariables                  Siddhi Variables list
     * @return                                  String list of variables
     * @throws DesignGenerationException        Error while generating groupBy variables
     */
    private List<String> generateGroupBy(List<Variable> groupByVariables) throws DesignGenerationException {
        List<String> groupByList = new ArrayList<>();
        for (Variable variable : groupByVariables) {
            preserveCodeSegment(variable);
            groupByList.add(ConfigBuildingUtilities.getDefinition(variable, siddhiAppString));
        }
        return groupByList;
    }

    /**
     * Generates AggregateByTimePeriod object with the given Siddhi TimePeriod
     * @param timePeriod                        Siddhi TimePeriod object
     * @return                                  AggregateByTimePeriod object
     * @throws DesignGenerationException        Unknown type of TimePeriod operator
     */
    private AggregateByTimePeriod generateAggregateByTime(TimePeriod timePeriod) throws DesignGenerationException {
        preserveCodeSegment(timePeriod);
        if (("INTERVAL").equalsIgnoreCase(timePeriod.getOperator().toString())) {
            return generateAggregateByTimeInterval(timePeriod.getDurations());
        } else if (("RANGE").equalsIgnoreCase(timePeriod.getOperator().toString())) {
            return generateAggregateByTimeRange(timePeriod.getDurations());
        }
        throw new DesignGenerationException("Unable to generate AggregateByTime for TimePeriod of type unknown");
    }

    /**
     * Generates AggregateByTimeInterval object with the given list of Siddhi TimePeriod.Durations
     * @param durations         List of Siddhi TimePeriod.Durations
     * @return                  AggregateByTimeInterval object
     */
    private AggregateByTimeInterval generateAggregateByTimeInterval(List<TimePeriod.Duration> durations) {
        List<String> intervals = new ArrayList<>();
        for (TimePeriod.Duration duration : durations) {
            intervals.add(duration.name());
        }
        return new AggregateByTimeInterval(intervals);
    }

    /**
     * Generates AggregateByTimeRange object with the given list of Siddhi TimePeriod.Durations
     * @param durations         List of Siddhi TimePeriod.Durations
     * @return                  AggregateByTimeRange object
     */
    private AggregateByTimeRange generateAggregateByTimeRange(List<TimePeriod.Duration> durations) {
        return new AggregateByTimeRange(
                new AggregationByTimeRangeValue(
                        (durations.get(0)).name(),
                        (durations.get(durations.size() - 1)).name()));
    }

    /**
     * Generates string for aggregateBy attribute, with the given Siddhi Variable
     * @param aggregateAttribute        Siddhi Variable
     * @return                          String representing the aggregateAttribute
     */
    private String generateAggregateByAttribute(Variable aggregateAttribute) {
        if (aggregateAttribute != null) {
            preserveCodeSegment(aggregateAttribute);
            return aggregateAttribute.getAttributeName();
        }
        return "";
    }
}
