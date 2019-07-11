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

package org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.input.types;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.JoinConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.JoinElementConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.query.input.JoinWithType;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.streamhandler.StreamHandlerConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import io.siddhi.query.api.SiddhiApp;
import io.siddhi.query.api.execution.query.input.stream.InputStream;
import io.siddhi.query.api.execution.query.input.stream.JoinInputStream;
import io.siddhi.query.api.execution.query.input.stream.SingleInputStream;

import java.util.ArrayList;
import java.util.List;

/**
 * Generator to create Join Query Input Config with Siddhi elements
 */
public class JoinConfigGenerator {
    // Elements of the Join Input Stream
    private List<String> streamIDs = new ArrayList<>();
    private List<String> tableIDs = new ArrayList<>();
    private List<String> aggregationIDs = new ArrayList<>();
    private List<String> windowIDs = new ArrayList<>();

    /**
     * Gets a JoinQueryConfig object, from the given Siddhi Query InputStream object
     * @param queryInputStream      Siddhi Query InputStream object, which contains data regarding Siddhi Query input
     * @param siddhiApp             Compiled Siddhi app
     * @param siddhiAppString       Complete Siddhi app string
     * @return                      JoinQueryConfig object
     */
    public JoinConfig getJoinQueryConfig(InputStream queryInputStream, SiddhiApp siddhiApp, String siddhiAppString)
            throws DesignGenerationException {
        distinguishElements(queryInputStream.getUniqueStreamIds(), siddhiApp);
        return generateJoinConfig(queryInputStream, siddhiAppString);
    }

    /**
     * Gets the JoinWithType for the JoinQueryConfig
     * @return                                  JoinWithType object
     * @throws DesignGenerationException        Error while generating config
     */
    private JoinWithType getJoinWithType() throws DesignGenerationException {
        if (tableIDs.size() == 1) {
            return JoinWithType.TABLE;
        } else if (aggregationIDs.size() == 1) {
            return JoinWithType.AGGREGATION;
        } else if (windowIDs.size() == 1) {
            return JoinWithType.WINDOW;
        } else if (!streamIDs.isEmpty()) {
            return JoinWithType.STREAM;
        } else {
            throw new DesignGenerationException("Unknown element present in Join Query");
        }
    }

    /**
     * Gets the JoinElementType with the given streamId, which is an element of the join
     * @param streamId      ID of the stream, defined for a window|table|aggregation|stream
     * @return              JoinElementType
     */
    private JoinElementType getJoinElementType(String streamId) {
        if (tableIDs.contains(streamId)) {
            return JoinElementType.TABLE;
        } else if (aggregationIDs.contains(streamId)) {
            return JoinElementType.AGGREGATION;
        } else if (windowIDs.contains(streamId)) {
            return JoinElementType.WINDOW;
        } else {
            return JoinElementType.STREAM;
        }
    }

    /**
     * Distinguishes elements that are represented with each Stream ID in the given list of streamIds,
     * and adds the streamId to the relevant list,
     * since Streams are manually defined in the Siddhi run time for Tables, Aggregations and Windows
     * @param streamIds     IDs of Streams, inclusive of the Streams defined for Tables, Aggregations and Windows,
     *                      by the Siddhi run time
     * @param siddhiApp     Compiled SiddhiApp
     */
    private void distinguishElements(List<String> streamIds, SiddhiApp siddhiApp) {
        for (String streamId : streamIds) {
            if (siddhiApp.getTableDefinitionMap().containsKey(streamId)) {
                tableIDs.add(streamId);
            } else if (siddhiApp.getAggregationDefinitionMap().containsKey(streamId)) {
                aggregationIDs.add(streamId);
            } else if (siddhiApp.getWindowDefinitionMap().containsKey(streamId)) {
                windowIDs.add(streamId);
            } else {
                streamIDs.add(streamId);
            }
        }
        if (streamIDs.isEmpty()) {
            throw new IllegalArgumentException("Unable to convert a Join Query Input with no streams");
        }
    }

    /**
     * Generates Config for a Join Element of a Join QueryInput, with the given Siddhi SingleInputStream
     * @param singleInputStream     Siddhi SingleInputStream object
     * @param siddhiAppString       Complete Siddhi app string
     * @return                      JoinElementConfig object, representing a Left|Right element of a join
     */
    private JoinElementConfig generateJoinElementConfig(SingleInputStream singleInputStream, String siddhiAppString)
            throws DesignGenerationException {
        JoinElementType joinElementType = getJoinElementType(singleInputStream.getStreamId());
        return new JoinElementConfig(
                joinElementType.toString(),
                singleInputStream.getStreamId(),
                new StreamHandlerConfigGenerator(siddhiAppString)
                        .generateStreamHandlerConfigList(singleInputStream.getStreamHandlers()),
                singleInputStream.getStreamReferenceId(),
                false);
    }

    /**
     * Generates a JoinConfig, which represents a Join Input of a Siddhi Query
     * @param queryInputStream                  Siddhi Query InputStream object
     * @param siddhiAppString                   Complete Siddhi app string
     * @return                                  JoinConfig object
     * @throws DesignGenerationException        Error while generating config
     */
    private JoinConfig generateJoinConfig(InputStream queryInputStream, String siddhiAppString)
            throws DesignGenerationException {
        JoinInputStream joinInputStream = (JoinInputStream) queryInputStream;
        // Left element of the join
        JoinElementConfig leftElement =
                generateJoinElementConfig((SingleInputStream) (joinInputStream.getLeftInputStream()), siddhiAppString);
        // Right element of the join
        JoinElementConfig rightElement =
                generateJoinElementConfig((SingleInputStream) (joinInputStream.getRightInputStream()), siddhiAppString);
        // Set 'isUnidirectional'
        if (joinInputStream.getTrigger().name().equalsIgnoreCase(JoinDirection.LEFT.toString())) {
            leftElement.setUnidirectional(true);
        } else if (joinInputStream.getTrigger().name().equalsIgnoreCase(JoinDirection.RIGHT.toString())) {
            rightElement.setUnidirectional(true);
        }

        JoinConfig joinConfig =
                new JoinConfig(
                        getJoinWithType().toString(),
                        leftElement,
                        joinInputStream.getType().name(),
                        rightElement,
                        null,
                        null,
                        null);
        // Set 'on'
        if (joinInputStream.getOnCompare() != null) {
            joinConfig.setOn(ConfigBuildingUtilities.getDefinition(joinInputStream.getOnCompare(), siddhiAppString));
        }
        // 'within' and 'per' can be not null only for Aggregations
        if (joinInputStream.getWithin() != null) {
            joinConfig.setWithin(
                    ConfigBuildingUtilities.getDefinition(
                            joinInputStream.getWithin(), siddhiAppString).split("within ")[1]);
        }
        if (joinInputStream.getPer() != null) {
            joinConfig.setPer(ConfigBuildingUtilities.getDefinition(joinInputStream.getPer(), siddhiAppString));
        }

        return joinConfig;
    }

    /**
     * Join Element Type
     */
    private enum JoinElementType {
        STREAM,
        TABLE,
        AGGREGATION,
        WINDOW
    }

    /**
     * Directions of a Join
     */
    private enum JoinDirection {
        LEFT,
        RIGHT
    }
}