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

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.JoinDirectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.JoinDirectionWindowConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.JoinQueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.types.JoinAggregationQueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.types.JoinStreamQueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.types.JoinTableQueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.types.JoinWindowQueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.handler.Filter;
import org.wso2.siddhi.query.api.execution.query.input.handler.StreamHandler;
import org.wso2.siddhi.query.api.execution.query.input.handler.Window;
import org.wso2.siddhi.query.api.execution.query.input.stream.JoinInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.SingleInputStream;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.ArrayList;
import java.util.List;

/**
 * Generator to create a QueryInputConfig of type Join
 */
public class JoinConfigGenerator {
    private Query query;
    private String siddhiAppString;
    private SiddhiApp siddhiApp;

    // Counts of elements in the Query
    private int streamCount = 0;
    private int tableCount = 0;
    private int aggregationCount = 0;
    private int windowCount = 0;

    public JoinConfigGenerator(Query query, String siddhiAppString, SiddhiApp siddhiApp) {
        this.query = query;
        this.siddhiAppString = siddhiAppString;
        this.siddhiApp = siddhiApp;
    }

    /**
     * Gets a JoinQueryConfig object, from the Siddhi Query object
     * @return          JoinQueryConfig object
     */
    public JoinQueryConfig getJoinQueryConfig() {
        switch (getType(query)) {
            case JOIN_STREAM:
                return generateJoinStreamQueryConfig(query);
            case JOIN_TABLE:
                return generateJoinTableQueryConfig(query);
            case JOIN_AGGREGATION:
                return generateJoinAggregationQueryConfig(query);
            case JOIN_WINDOW:
                return generateJoinWindowQueryConfig(query);
            default:
                throw new IllegalArgumentException("Unknown type: " + getType(query) +
                        " for generating Join Query Config");
        }
    }

    /**
     * Gets the type of JoinQueryConfig
     * @param query     Siddhi Query object
     * @return          Type of the query
     */
    private JoinType getType(Query query) {
        countElements(query.getInputStream().getUniqueStreamIds(), siddhiApp);
        if (tableCount > 0) {
            return JoinType.JOIN_TABLE;
        } else if (aggregationCount > 0) {
            return JoinType.JOIN_AGGREGATION;
        } else if (windowCount > 0) {
            return JoinType.JOIN_WINDOW;
        } else if (streamCount > 0) {
            return JoinType.JOIN_STREAM;
        } else {
            throw new IllegalArgumentException("Unknown element present in Join Query");
        }
    }

    /**
     * Counts tables, aggregations, windows and defined streams in the given SiddhiApp
     * with the given list of Stream Ids,
     * since Streams are automatically defined inside for these elements, in addition to defined streams
     * @param streamIds     List of Stream IDs, which consists of defined streams, and auto defined streams for each
     *                      table, aggregation, window
     * @param siddhiApp     SiddhiApp object, which is a compiled Siddhi application
     */
    private void countElements(List<String> streamIds, SiddhiApp siddhiApp) {
        for (String streamId : streamIds) {
            if (siddhiApp.getTableDefinitionMap().containsKey(streamId)) {
                tableCount++;
            } else if (siddhiApp.getAggregationDefinitionMap().containsKey(streamId)) {
                aggregationCount++;
            } else if (siddhiApp.getWindowDefinitionMap().containsKey(streamId)) {
                windowCount++;
            } else {
                streamCount++;
            }
        }
    }

    /**
     * Generates Config for a Join Stream Query Input, from the given Siddhi Query
     * @param query     Siddhi Query object
     * @return          JoinStreamQueryConfig object
     */
    private JoinStreamQueryConfig generateJoinStreamQueryConfig(Query query) {
        JoinInputStream joinInputStream = (JoinInputStream)(query.getInputStream());

        // Left side Stream Information of the join
        JoinedStreamConfig leftStreamConfig =
                generateJoinedStreamConfig((SingleInputStream)(joinInputStream.getLeftInputStream()));
        // Right side Stream Information of the join
        JoinedStreamConfig rightStreamConfig =
                generateJoinedStreamConfig((SingleInputStream)(joinInputStream.getRightInputStream()));

        // Set 'isUnidirectional'
        if (joinInputStream.getTrigger().name().equalsIgnoreCase(JoinDirection.LEFT.toString())) {
            leftStreamConfig.isUnidirectional = true;
        } else if (joinInputStream.getTrigger().name().equalsIgnoreCase(JoinDirection.RIGHT.toString())) {
            rightStreamConfig.isUnidirectional = true;
        }

        return new JoinStreamQueryConfig(
                generateJoinDirectionConfig(leftStreamConfig),
                joinInputStream.getType().name(),
                generateJoinDirectionConfig(rightStreamConfig),
                ConfigBuildingUtilities.getDefinition(joinInputStream.getOnCompare(), siddhiAppString));
    }

    /**
     * Generates Config for a Join Table Query Input, from the given Siddhi Query
     * @param query     Siddhi Query object
     * @return          JoinTableQueryConfig object
     */
    private JoinTableQueryConfig generateJoinTableQueryConfig(Query query) {
        JoinInputStream joinInputStream = (JoinInputStream)(query.getInputStream());

        // Left side Stream Information of the join
        JoinedStreamConfig leftStreamConfig =
                generateJoinedStreamConfig((SingleInputStream)(joinInputStream.getLeftInputStream()));

        // Right side Stream Information of the join
        JoinedStreamConfig rightStreamConfig =
                generateJoinedStreamConfig((SingleInputStream)(joinInputStream.getRightInputStream()));

        return new JoinTableQueryConfig(
                generateJoinDirectionConfig(leftStreamConfig),
                joinInputStream.getType().name(),
                generateJoinDirectionConfig(rightStreamConfig),
                ConfigBuildingUtilities.getDefinition(joinInputStream.getOnCompare(), siddhiAppString));
    }

    /**
     * Generates Config for a Join Aggregation Query Input, from the given Siddhi Query
     * @param query     Siddhi Query object
     * @return          JoinAggregationQueryConfig object
     */
    private JoinAggregationQueryConfig generateJoinAggregationQueryConfig(Query query) {
        JoinInputStream joinInputStream = (JoinInputStream)(query.getInputStream());

        // Left side Stream Information of the join
        JoinedStreamConfig leftStreamConfig =
                generateJoinedStreamConfig((SingleInputStream)(joinInputStream.getLeftInputStream()));

        // Right side Stream Information of the join
        JoinedStreamConfig rightStreamConfig =
                generateJoinedStreamConfig((SingleInputStream)(joinInputStream.getRightInputStream()));

        return new JoinAggregationQueryConfig(
                generateJoinDirectionConfig(leftStreamConfig),
                joinInputStream.getType().name(),
                generateJoinDirectionConfig(rightStreamConfig),
                ConfigBuildingUtilities.getDefinition(joinInputStream.getOnCompare(), siddhiAppString),
                ConfigBuildingUtilities.getDefinition(joinInputStream.getWithin(), siddhiAppString),
                ConfigBuildingUtilities.getDefinition(joinInputStream.getPer(), siddhiAppString));
    }

    /**
     * Generates Config for a Join Window Query Input, from the given Siddhi Query
     * @param query     Siddhi Query object
     * @return          JoinWindowQueryConfig object
     */
    private JoinWindowQueryConfig generateJoinWindowQueryConfig(Query query) {
        JoinInputStream joinInputStream = (JoinInputStream)(query.getInputStream());

        // Left side Stream Information of the join
        JoinedStreamConfig leftStreamConfig =
                generateJoinedStreamConfig((SingleInputStream)(joinInputStream.getLeftInputStream()));

        // Right side Stream Information of the join
        JoinedStreamConfig rightStreamConfig =
                generateJoinedStreamConfig((SingleInputStream)(joinInputStream.getRightInputStream()));

        return new JoinWindowQueryConfig(
                generateJoinDirectionConfig(leftStreamConfig),
                generateJoinDirectionConfig(rightStreamConfig),
                ConfigBuildingUtilities.getDefinition(joinInputStream.getOnCompare(), siddhiAppString));
    }

    /**
     * Generates Config for a Joined Stream of a Join Query, with the given Siddhi SingleInputStream
     * @param singleInputStream     Siddhi SingleInputStream object,
     *                              that has information about a stream of a Join Query
     * @return                      JoinedStreamConfig object, that has specific extracted information
     *                              from the Siddhi SingleInputStream
     */
    private JoinedStreamConfig generateJoinedStreamConfig(SingleInputStream singleInputStream) {
        JoinedStreamConfig joinedStreamConfig = new JoinedStreamConfig();
        joinedStreamConfig.name = singleInputStream.getStreamId();
        joinedStreamConfig.as = singleInputStream.getStreamReferenceId();

        for (StreamHandler streamHandler : singleInputStream.getStreamHandlers()) {
            if (streamHandler instanceof Filter) {
                String definition = ConfigBuildingUtilities.getDefinition(streamHandler, siddhiAppString);
                joinedStreamConfig.filter = definition.substring(1, definition.length() - 1).trim();
            } else if (streamHandler instanceof Window) {
                for (Expression expression : streamHandler.getParameters()) {
                    joinedStreamConfig.windowParameters.add(
                            ConfigBuildingUtilities.getDefinition(expression, siddhiAppString));
                }
                joinedStreamConfig.windowFunction = ((Window)streamHandler).getName();
            }
        }

        return joinedStreamConfig;
    }

    /**
     * Generates a JoinDirectionConfig object from the given JoinedStreamConfig object
     * @param joinedStreamConfig        JoinedStreamConfig object, that has specific extracted information
     *                                  of a stream that is present in a Join Query
     * @return                          JoinDirectionConfig object, that will be used in a Join Query Config
     */
    private JoinDirectionConfig generateJoinDirectionConfig(JoinedStreamConfig joinedStreamConfig) {
        return new JoinDirectionConfig(
                joinedStreamConfig.name,
                joinedStreamConfig.filter,
                new JoinDirectionWindowConfig(
                        joinedStreamConfig.windowFunction,
                        joinedStreamConfig.windowParameters),
                joinedStreamConfig.isUnidirectional,
                joinedStreamConfig.as);
    }

    /**
     * Config for a stream present in a side of the join
     */
    private class JoinedStreamConfig {
        private String name;
        private String filter;
        private String windowFunction;
        private List<String> windowParameters;
        private String as;
        private boolean isUnidirectional;

        private JoinedStreamConfig() {
            this.name = "";
            this.filter = "";
            this.windowFunction = "";
            this.windowParameters = new ArrayList<>();
            this.as = "";
            this.isUnidirectional = false;
        }
    }

    /**
     * Join Type
     */
    private enum JoinType {
        JOIN_STREAM,
        JOIN_TABLE,
        JOIN_AGGREGATION,
        JOIN_WINDOW
    }

    /**
     * Directions of a Join
     */
    private enum JoinDirection {
        LEFT,
        RIGHT
    }
}
