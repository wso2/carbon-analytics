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
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.AggregateByTimePeriod;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.aggregation.AggregationConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.attributesselection.AttributesSelectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import org.wso2.siddhi.query.api.aggregation.TimePeriod;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.AggregationDefinition;
import org.wso2.siddhi.query.api.execution.query.selection.BasicSelector;
import org.wso2.siddhi.query.api.expression.Variable;

import java.util.ArrayList;
import java.util.List;

/**
 * Generator to create AggregationConfig
 */
public class AggregationConfigGenerator {
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
        AttributesSelectionConfig selectedAttributesConfig;
        List<String> groupBy = new ArrayList<>();

        if (aggregationDefinition.getSelector() instanceof BasicSelector) {
            BasicSelector selector = (BasicSelector) aggregationDefinition.getSelector();
            selectedAttributesConfig =
                    new AttributesSelectionConfigGenerator(siddhiAppString).generateAttributesSelectionConfig(selector);

            // Populate 'groupBy' list
            for (Variable variable : selector.getGroupByList()) {
                groupBy.add(ConfigBuildingUtilities.getDefinition(variable, siddhiAppString));
            }
        } else {
            throw new DesignGenerationException("Selector of AggregationDefinition is not of class BasicSelector");
        }

        // For creating 'aggregate by time' object
        List<TimePeriod.Duration> aggregationTimePeriodDurations = aggregationDefinition.getTimePeriod().getDurations();

        // 'aggregateByAttribute'
        String aggregateByAttribute = "";
        if (aggregationDefinition.getAggregateAttribute() != null) {
            aggregateByAttribute = aggregationDefinition.getAggregateAttribute().getAttributeName();
        }

        // 'store' and annotations
        StoreConfigGenerator storeConfigGenerator = new StoreConfigGenerator();
        StoreConfig storeConfig = null;
        AnnotationConfigGenerator annotationConfigGenerator = new AnnotationConfigGenerator();
        List<String> annotationList = new ArrayList<>();
        for (Annotation annotation : aggregationDefinition.getAnnotations()) {
            if (annotation.getName().equalsIgnoreCase("STORE")) {
                storeConfig = storeConfigGenerator.generateStoreConfig(annotation);
            } else {
                annotationList.add(annotationConfigGenerator.generateAnnotationConfig(annotation));
            }
        }

        return new AggregationConfig(
                aggregationDefinition.getId(),
                aggregationDefinition.getId(),
                aggregationDefinition.getBasicSingleInputStream().getStreamId(),
                selectedAttributesConfig,
                groupBy,
                aggregateByAttribute,
                new AggregateByTimePeriod(
                        (aggregationTimePeriodDurations.get(0)).name().toLowerCase(),
                        (aggregationTimePeriodDurations.get(aggregationTimePeriodDurations.size() - 1))
                                .name().toLowerCase()),
                storeConfig,
                annotationList);
    }
}
