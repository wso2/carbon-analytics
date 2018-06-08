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

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.StreamConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.partition.PartitionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.partition.PartitionWithElement;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.QueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.query.QueryListType;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.QueryConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.AbstractDefinition;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.execution.partition.Partition;
import org.wso2.siddhi.query.api.execution.partition.PartitionType;
import org.wso2.siddhi.query.api.execution.partition.RangePartitionType;
import org.wso2.siddhi.query.api.execution.partition.ValuePartitionType;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generator to create PartitionConfig
 */
public class PartitionConfigGenerator {
    private String siddhiAppString;
    private SiddhiApp siddhiApp;
    private Map<String, Map<String, AbstractDefinition>> partitionedInnerStreamDefinitions;
    private Map<String, String> connectorsAndStreams = new HashMap<>();

    public PartitionConfigGenerator(String siddhiAppString,
                                    SiddhiApp siddhiApp,
                                    Map<String, Map<String, AbstractDefinition>> partitionedInnerStreamDefinitions) {
        this.siddhiAppString = siddhiAppString;
        this.siddhiApp = siddhiApp;
        this.partitionedInnerStreamDefinitions = partitionedInnerStreamDefinitions;
    }

    /**
     * Generates PartitionConfig object from the given Siddhi Partition
     * @param partition                         Siddhi Partition
     * @param partitionId                       Id of the Partition against which,
     *                                          Inner Stream definitions are contained in the respective map
     * @return                                  PartitionConfig object
     * @throws DesignGenerationException        Error when generating PartitionConfig object
     */
    public PartitionConfig generatePartitionConfig(Partition partition, String partitionId)
            throws DesignGenerationException {
        PartitionConfig partitionConfig = new PartitionConfig();
        partitionConfig.setQueryLists(generateQueryList(partition.getQueryList()));
        partitionConfig.setStreamList(
                generateInnerStreams(
                        partitionedInnerStreamDefinitions.get(partitionId).values()));
        partitionConfig.setPartitionWith(generatePartitionWith(partition.getPartitionTypeMap()));
        partitionConfig.setAnnotationList(generateAnnotations(partition.getAnnotations()));
        partitionConfig.setConnectorsAndStreams(connectorsAndStreams);
        return partitionConfig;
    }

    /**
     * Generates QueryList from the given list of Siddhi Queries
     * @param queryList                         List of Siddhi Queries
     * @return                                  QueryConfig lists
     * @throws DesignGenerationException        Error when generating a QueryConfig
     */
    private Map<QueryListType, List<QueryConfig>> generateQueryList(List<Query> queryList)
            throws DesignGenerationException {
        Map<QueryListType, List<QueryConfig>> queryLists = populateEmptyQueryLists();
        QueryConfigGenerator queryConfigGenerator = new QueryConfigGenerator(siddhiAppString, siddhiApp);
        for (Query query : queryList) {
            QueryConfig queryConfig = queryConfigGenerator.generateQueryConfig(query);
            QueryListType queryListType = QueryConfigGenerator.getQueryListType(queryConfig);
            queryLists.get(queryListType).add(queryConfig);
        }
        return queryLists;
    }

    /**
     * Populates an empty map of QueryConfig lists
     * @return      Map of QueryConfig lists
     */
    private Map<QueryListType, List<QueryConfig>> populateEmptyQueryLists() {
        Map<QueryListType, List<QueryConfig>> queryLists = new EnumMap<>(QueryListType.class);
        queryLists.put(QueryListType.WINDOW_FILTER_PROJECTION, new ArrayList<>());
        queryLists.put(QueryListType.JOIN, new ArrayList<>());
        queryLists.put(QueryListType.PATTERN, new ArrayList<>());
        queryLists.put(QueryListType.SEQUENCE, new ArrayList<>());
        return queryLists;
    }

    /**
     * Generates a list of StreamConfigs from the given map of Inner Stream definition
     * @param innerStreamDefinitions        Map of Inner Stream definitions
     * @return                              List of StreamConfigs
     */
    private List<StreamConfig> generateInnerStreams(Collection<AbstractDefinition> innerStreamDefinitions) {
        StreamDefinitionConfigGenerator streamDefinitionConfigGenerator = new StreamDefinitionConfigGenerator();
        List<StreamConfig> innerStreams = new ArrayList<>();
        for (AbstractDefinition innerStreamDefinition : innerStreamDefinitions) {
            if (innerStreamDefinition instanceof StreamDefinition) {
                innerStreams.add(
                        streamDefinitionConfigGenerator.generateStreamConfig((StreamDefinition) innerStreamDefinition));
            }
        }
        return innerStreams;
    }

    /**
     * Generates a list of PartitionWithElements, with the given map of Siddhi PartitionType
     * @param partitionTypeMap                  Map of Siddhi PartitionType
     * @return                                  List of PartitionWithElements
     * @throws DesignGenerationException        Error when generating a PartitionWithElement
     */
    private List<PartitionWithElement> generatePartitionWith(Map<String, PartitionType> partitionTypeMap)
            throws DesignGenerationException {
        List<PartitionWithElement> partitionWithElements = new ArrayList<>();
        int partitionConnectorIdCounter = 0;
        for (Map.Entry<String, PartitionType> partitionWithEntry : partitionTypeMap.entrySet()) {
            PartitionWithElement partitionWithElement = generatePartitionWithElement(partitionWithEntry);
            connectorsAndStreams.put(
                    String.valueOf(++partitionConnectorIdCounter),
                    partitionWithElement.getStreamName());
            partitionWithElements.add(partitionWithElement);
        }
        return partitionWithElements;
    }

    /**
     * Generates PartitionWithElement, with the given map entry of PartitionType
     * @param partitionWithEntry                PartitionType map entry
     * @return                                  PartitionWithElement
     * @throws DesignGenerationException        Error when generating PartitionElementExpression
     */
    private PartitionWithElement generatePartitionWithElement(Map.Entry<String, PartitionType> partitionWithEntry)
            throws DesignGenerationException {
        return new PartitionWithElement(
                generatePartitionElementExpression(partitionWithEntry.getValue()),
                partitionWithEntry.getKey());
    }

    /**
     * Generates a string denoting PartitionElementExpression, with the given Siddhi PartitionType
     * @param partitionType                     Siddhi PartitionType
     * @return                                  String denoting PartitionElementExpression
     * @throws DesignGenerationException        Error when generating PartitionExpression
     */
    private String generatePartitionElementExpression(PartitionType partitionType) throws DesignGenerationException {
        if (partitionType instanceof RangePartitionType) {
            return generateRangePartitionExpression(((RangePartitionType) partitionType).getRangePartitionProperties());
        } else if (partitionType instanceof ValuePartitionType) {
            return generateValuePartitionExpression(((ValuePartitionType) partitionType).getExpression());
        }
        throw new DesignGenerationException("Unable to generate expression for Partition element of type unknown");
    }

    /**
     * Generates a string denoting RangePartitionExpression, with the given Siddhi RangePartitionProperty array
     * @param rangePartitionProperties          Array of Siddhi RangePartitionProperty
     * @return                                  String denoting RangePartitionExpression
     * @throws DesignGenerationException        Error when generating RangePartitionExpression string
     */
    private String generateRangePartitionExpression(
            RangePartitionType.RangePartitionProperty[] rangePartitionProperties) throws DesignGenerationException {
        List<String> expression = new ArrayList<>();
        for (RangePartitionType.RangePartitionProperty rangePartitionProperty : rangePartitionProperties) {
            expression.add(ConfigBuildingUtilities.getDefinition(rangePartitionProperty, siddhiAppString));
        }
        return String.join(" or ", expression);
    }

    /**
     * Generates a string denoting ValuePartitionExpression, with the given Siddhi Expression
     * @param valuePartitionExpression          Siddhi Expression
     * @return                                  String denoting ValuePartitionExpression
     * @throws DesignGenerationException        Error when generating ValuePartitionExpression string
     */
    private String generateValuePartitionExpression(Expression valuePartitionExpression)
            throws DesignGenerationException {
        return ConfigBuildingUtilities.getDefinition(valuePartitionExpression, siddhiAppString);
    }

    /**
     * Generates list of strings, denoting annotations
     * @param annotations       List of Siddhi Annotations
     * @return                  List of strings, denoting annotations
     */
    private List<String> generateAnnotations(List<Annotation> annotations) {
        AnnotationConfigGenerator annotationConfigGenerator = new AnnotationConfigGenerator();
        return annotationConfigGenerator.generateAnnotationConfigList(annotations);
    }
}
