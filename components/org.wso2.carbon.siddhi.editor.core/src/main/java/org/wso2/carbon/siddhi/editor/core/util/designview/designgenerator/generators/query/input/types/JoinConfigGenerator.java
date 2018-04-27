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
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.JoinElementWindowConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.query.input.JoinWithType;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import org.wso2.siddhi.query.api.SiddhiApp;
import org.wso2.siddhi.query.api.execution.query.input.handler.Filter;
import org.wso2.siddhi.query.api.execution.query.input.handler.StreamHandler;
import org.wso2.siddhi.query.api.execution.query.input.handler.Window;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.JoinInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.SingleInputStream;
import org.wso2.siddhi.query.api.expression.Expression;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates JoinQueryConfig with given Siddhi elements TODO method comments
 */
public class JoinConfigGenerator {
    // Counts of elements in the Query
    private int streamCount = 0;
    private int tableCount = 0;
    private int aggregationCount = 0;
    private int windowCount = 0;

    /**
     * Gets a JoinQueryConfig object, from the given Siddhi Query InputStream object
     * @param queryInputStream      Siddhi Query InputStream object, which contains data regarding Siddhi Query input
     * @param siddhiApp             Compiled Siddhi app
     * @param siddhiAppString       Complete Siddhi app string
     * @return                      JoinQueryConfig object
     */
    public JoinConfig getJoinQueryConfig(InputStream queryInputStream,
                                              SiddhiApp siddhiApp,
                                              String siddhiAppString) {
        return generateJoinConfig(queryInputStream, siddhiAppString, siddhiApp);
//        switch (getType(queryInputStream, siddhiApp)) {
//            case JOIN_STREAM:
//                return generateJoinStreamQueryConfig(queryInputStream, siddhiAppString);
//            case JOIN_TABLE:
//                return generateJoinTableQueryConfig(queryInputStream, siddhiAppString);
//            case JOIN_AGGREGATION:
//                return generateJoinAggregationQueryConfig(queryInputStream, siddhiAppString);
//            case JOIN_WINDOW:
//                return generateJoinWindowQueryConfig(queryInputStream, siddhiAppString);
//            default:
//                throw new IllegalArgumentException("Unknown type: " + getType(queryInputStream, siddhiApp) +
//                        " for generating Join Query Config");
//        }
    }

//    /**
//     * Gets the type of JoinQueryConfig, with the given Siddhi Query InputStream
//     * @param queryInputStream      Siddhi Query InputStream object, which contains data regarding Siddhi Query input
//     * @param siddhiApp             Compiled Siddhi app
//     * @return                      Type of the Join Query Config
//     */
//    private JoinType getType(InputStream queryInputStream, SiddhiApp siddhiApp) {
//        countElements(queryInputStream.getUniqueStreamIds(), siddhiApp);
//        if (tableCount == 1) {
//            return JoinType.JOIN_TABLE;
//        } else if (aggregationCount == 1) {
//            return JoinType.JOIN_AGGREGATION;
//        } else if (windowCount == 1) {
//            return JoinType.JOIN_WINDOW;
//        } else if (streamCount > 0) {
//            return JoinType.JOIN_STREAM;
//        } else {
//            throw new IllegalArgumentException("Unknown element present in Join Query");
//        }
//    }

    private JoinWithType getJoinWithType(InputStream queryInputStream, SiddhiApp siddhiApp) {
        // TODO this method is under testing
        countElements(queryInputStream.getUniqueStreamIds(), siddhiApp);
        if (tableCount == 1) {
            return JoinWithType.TABLE;
        } else if (aggregationCount == 1) {
            return JoinWithType.AGGREGATION;
        } else if (windowCount == 1) {
            return JoinWithType.WINDOW;
        } else if (streamCount > 0) {
            return JoinWithType.STREAM;
        } else {
            throw new IllegalArgumentException("Unknown element present in Join Query");
        }
    }

    private JoinElementType getJoinElementType(String streamId, SiddhiApp siddhiApp) {
        // TODO this method is under testing.
        // TODO maybe we can optimize by adding to a list of TABLES & all when counting
        if (siddhiApp.getTableDefinitionMap().containsKey(streamId)) {
            return JoinElementType.TABLE;
        } else if (siddhiApp.getAggregationDefinitionMap().containsKey(streamId)) {
            return JoinElementType.AGGREGATION;
        } else if (siddhiApp.getWindowDefinitionMap().containsKey(streamId)) {
            return JoinElementType.WINDOW;
        } else {
            return JoinElementType.STREAM;
        }
    }

    /**
     * Counts number of tables, aggregations, windows and defined streams in the given SiddhiApp,
     * with the given list of Stream Ids,
     * since Streams are automatically defined inside for these elements, in addition to defined streams
     * @param streamIds     List of Stream IDs, which consists of defined streams, and auto defined streams for each
     *                      table, aggregation, window
     * @param siddhiApp     Compiled SiddhiApp
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
        if (streamCount == 0) {
            throw new IllegalArgumentException("Unable to convert a Join Query Input with no streams");
        }
    }

    /**
     * Generates Config for a Join Element of a Join QueryInput, with the given Siddhi SingleInputStream
     * @param singleInputStream     Siddhi SingleInputStream object
     * @param siddhiAppString       Complete Siddhi app string
     * @return                      JoinElementConfig object,
     *                              that has specific extracted information from the Siddhi SingleInputStream
     */
    private JoinElementConfig generateJoinElementConfig(SingleInputStream singleInputStream,
                                                        String siddhiAppString,
                                                        SiddhiApp siddhiApp) {
        // TODO: THIS METHOD IS IN TESTING

        JoinElementConfig joinElementConfig =
                new JoinElementConfig(
                        "foo", // TODO: 4/27/18 Have a method to get the type
                        singleInputStream.getStreamId(),
                        "", // TODO Confirm whether this should be "" or null
                        new JoinElementWindowConfig(), // TODO Confirm whether this should be {} or null
                        singleInputStream.getStreamReferenceId(),
                        false);

        for (StreamHandler streamHandler : singleInputStream.getStreamHandlers()) {
            if (streamHandler instanceof Filter) {
                String definition = ConfigBuildingUtilities.getDefinition(streamHandler, siddhiAppString);
                joinElementConfig.setFilter(definition.substring(1, definition.length() - 1).trim());
            } else if (streamHandler instanceof Window) {
                List<String> windowParameters = new ArrayList<>();
                for (Expression expression : streamHandler.getParameters()) {
                    windowParameters.add(ConfigBuildingUtilities.getDefinition(expression, siddhiAppString));
                }
                joinElementConfig.setWindow(
                        new JoinElementWindowConfig(((Window)streamHandler).getName(), windowParameters));
            }
        }

        // Special Conditions
        JoinElementType joinElementType = getJoinElementType(singleInputStream.getStreamId(), siddhiApp);
        if (joinElementType.equals(JoinElementType.WINDOW)) {
            // Cannot have Inner Window. Force a null todo
            joinElementConfig.setWindow(null); // TODO confirm whether {} or null
        }
        // TODO I STOPPED HERE ***********************

        return joinElementConfig;
    }

//    /**
//     * Generates Config for a Join Element of a Join QueryInput, with the given Siddhi SingleInputStream
//     * @param singleInputStream     Siddhi SingleInputStream object
//     * @param siddhiAppString       Complete Siddhi app string
//     * @return                      JoinElementConfig object,
//     *                              that has specific extracted information from the Siddhi SingleInputStream
//     */
//    private JoinElementConfig generateJoinElementConfig(SingleInputStream singleInputStream, String siddhiAppString) {
//        // TODO: Check which type is 'singleInputStream'.
//        // TODO: If defined window, there cannot be a query window.
//        // TODO: Seems that's handled below in noArg instantiation. Confirm with test queries
//
//        JoinElementConfig joinElementConfig =
//                new JoinElementConfig(
//                        singleInputStream.getStreamId(),
//                        "", // TODO Confirm whether this should be "" or null
//                        new JoinElementWindowConfig(), // TODO Confirm whether this should be {} or null
//                        singleInputStream.getStreamReferenceId(),
//                        false);
//
//        for (StreamHandler streamHandler : singleInputStream.getStreamHandlers()) {
//            if (streamHandler instanceof Filter) {
//                String definition = ConfigBuildingUtilities.getDefinition(streamHandler, siddhiAppString);
//                joinElementConfig.setFilter(definition.substring(1, definition.length() - 1).trim());
//            } else if (streamHandler instanceof Window) {
//                List<String> windowParameters = new ArrayList<>();
//                for (Expression expression : streamHandler.getParameters()) {
//                    windowParameters.add(ConfigBuildingUtilities.getDefinition(expression, siddhiAppString));
//                }
//                joinElementConfig.setWindow(
//                        new JoinElementWindowConfig(((Window)streamHandler).getName(), windowParameters));
//            }
//        }
//
//        return joinElementConfig;
//    }

    public JoinConfig generateJoinConfig(InputStream queryInputStream, String siddhiAppString, SiddhiApp siddhiApp) {
        // TODO THIS METHOD IS IN TESTING

        // TODO if this comes
        // TODO *** No need for 'getType' method. After counting validation, this method occurs
        // TODO *** 'Window' validations are handled inside leftElement, while generating JoinElementConfig
        // TODO *** 'Aggregation' validation will happen in this method (within and per)

        JoinInputStream joinInputStream = (JoinInputStream) queryInputStream;
        // TODO 'type' of leftElement should be calculated with another method
        // Left element of the join
        JoinElementConfig leftElement =
                generateJoinElementConfig(
                        (SingleInputStream)(joinInputStream.getLeftInputStream()),
                        siddhiAppString,
                        siddhiApp);

        // Right element of the join
        JoinElementConfig rightElement =
                generateJoinElementConfig(
                        (SingleInputStream)(joinInputStream.getRightInputStream()),
                        siddhiAppString,
                        siddhiApp);

        // Set 'isUnidirectional'
        if (joinInputStream.getTrigger().name().equalsIgnoreCase(JoinDirection.LEFT.toString())) {
            leftElement.setUnidirectional(true);
        } else if (joinInputStream.getTrigger().name().equalsIgnoreCase(JoinDirection.RIGHT.toString())) {
            rightElement.setUnidirectional(true);
        }

        JoinConfig joinConfig =
                new JoinConfig(
                        getJoinWithType(queryInputStream, siddhiApp).toString(),
                        leftElement,
                        joinInputStream.getType().name(),
                        rightElement,
                        ConfigBuildingUtilities.getDefinition(joinInputStream.getOnCompare(), siddhiAppString),
                        null, // TODO: 4/25/18 confirm whether null or ''
                        null); // TODO: 4/25/18 confirm whether null or ''

        // TODO this will only happen for Aggregations
        if (joinInputStream.getWithin() != null) {
            joinConfig.setWithin(ConfigBuildingUtilities.getDefinition(joinInputStream.getWithin(), siddhiAppString));
        }
        if (joinInputStream.getWithin() != null) {
            joinConfig.setWithin(ConfigBuildingUtilities.getDefinition(joinInputStream.getWithin(), siddhiAppString));
        }

        return joinConfig;
    }

    //////// TODO [OLD METHODS] BEGIN

//    /**
//     * Generates Config for a Join Stream Query Input, from the given Siddhi Query
//     * @param queryInputStream      Siddhi Query InputStream object
//     * @param siddhiAppString       Complete Siddhi app string
//     * @return                      JoinStreamQueryConfig object
//     */
//    private JoinConfig generateJoinStreamQueryConfig(InputStream queryInputStream, String siddhiAppString) {
//        JoinInputStream joinInputStream = (JoinInputStream) queryInputStream;
//
//        // Left element of the join
//        JoinElementConfig leftElement =
//                generateJoinElementConfig((SingleInputStream)(joinInputStream.getLeftInputStream()), siddhiAppString);
//
//        // Right element of the join
//        JoinElementConfig rightElement =
//                generateJoinElementConfig((SingleInputStream)(joinInputStream.getRightInputStream()), siddhiAppString);
//
//        // Set 'isUnidirectional'
//        if (joinInputStream.getTrigger().name().equalsIgnoreCase(JoinDirection.LEFT.toString())) {
//            leftElement.setUnidirectional(true);
//        } else if (joinInputStream.getTrigger().name().equalsIgnoreCase(JoinDirection.RIGHT.toString())) {
//            rightElement.setUnidirectional(true);
//        }
//
//        return new JoinConfig(
//                JoinWithType.STREAM.toString(),
//                leftElement,
//                joinInputStream.getType().name(),
//                rightElement,
//                ConfigBuildingUtilities.getDefinition(joinInputStream.getOnCompare(), siddhiAppString),
//                null, // TODO: 4/25/18 confirm whether null or ''
//                null); // TODO: 4/25/18 confirm whether null or ''
//    }
//
//    /**
//     * Generates Config for a Join Table Query Input, from the given Siddhi Query
//     * @param queryInputStream      Siddhi Query InputStream object
//     * @param siddhiAppString       Complete Siddhi app string
//     * @return                      JoinTableQueryConfig object
//     */
//    private JoinConfig generateJoinTableQueryConfig(InputStream queryInputStream, String siddhiAppString) {
//        JoinInputStream joinInputStream = (JoinInputStream) queryInputStream;
//
//        // Left element of the join
//        JoinElementConfig leftElement =
//                generateJoinElementConfig((SingleInputStream)(joinInputStream.getLeftInputStream()), siddhiAppString);
//
//        // Right element of the join
//        JoinElementConfig rightElement =
//                generateJoinElementConfig((SingleInputStream)(joinInputStream.getRightInputStream()), siddhiAppString);
//
//        return new JoinConfig(
//                JoinWithType.TABLE.toString(),
//                leftElement,
//                joinInputStream.getType().name(),
//                rightElement,
//                ConfigBuildingUtilities.getDefinition(joinInputStream.getOnCompare(), siddhiAppString),
//                null, // TODO: 4/25/18 confirm whether null or ''
//                null); // TODO: 4/25/18 confirm whether null or ''
//    }
//
//    /**
//     * Generates Config for a Join Aggregation Query Input, from the given Siddhi Query
//     * @param queryInputStream      Siddhi Query InputStream object
//     * @param siddhiAppString       Complete Siddhi app string
//     * @return                      JoinAggregationQueryConfig object
//     */
//    private JoinConfig generateJoinAggregationQueryConfig(InputStream queryInputStream, String siddhiAppString) {
//        JoinInputStream joinInputStream = (JoinInputStream) queryInputStream;
//
//        // Left element of the join
//        JoinElementConfig leftElement =
//                generateJoinElementConfig((SingleInputStream)(joinInputStream.getLeftInputStream()), siddhiAppString);
//
//        // Right element of the join
//        JoinElementConfig rightElement =
//                generateJoinElementConfig((SingleInputStream)(joinInputStream.getRightInputStream()), siddhiAppString);
//
//        return new JoinConfig(
//                JoinWithType.AGGREGATION.toString(),
//                leftElement,
//                joinInputStream.getType().name(),
//                rightElement,
//                ConfigBuildingUtilities.getDefinition(joinInputStream.getOnCompare(), siddhiAppString),
//                ConfigBuildingUtilities.getDefinition(joinInputStream.getWithin(), siddhiAppString),
//                ConfigBuildingUtilities.getDefinition(joinInputStream.getPer(), siddhiAppString));
//    }
//
//    /**
//     * Generates Config for a Join Window Query Input, from the given Siddhi Query
//     * @param queryInputStream      Siddhi Query InputStream object
//     * @param siddhiAppString       Complete Siddhi app string
//     * @return                      JoinWindowQueryConfig object
//     */
//    private JoinConfig generateJoinWindowQueryConfig(InputStream queryInputStream, String siddhiAppString) {
//        JoinInputStream joinInputStream = (JoinInputStream) queryInputStream;
//
//        // Left element of the join
//        JoinElementConfig leftElement =
//                generateJoinElementConfig((SingleInputStream)(joinInputStream.getLeftInputStream()), siddhiAppString);
//
//        // Right element of the join
//        JoinElementConfig rightElement =
//                generateJoinElementConfig((SingleInputStream)(joinInputStream.getRightInputStream()), siddhiAppString);
//
//        // TODO: 4/25/18 set 'window' to null
//
//        return new JoinConfig(
//                JoinWithType.WINDOW.toString(),
//                leftElement,
//                joinInputStream.getType().name(),
//                rightElement,
//                ConfigBuildingUtilities.getDefinition(joinInputStream.getOnCompare(), siddhiAppString),
//                null, // TODO: 4/25/18 confirm whether null or ''
//                null); // TODO: 4/25/18 confirm whether null or ''
//    }

    //////// TODO [OLD METHODS] END













    /////////////////////////////////////////////////

//    /**
//     * Generates Config for a Join Stream Query Input, from the given Siddhi Query
//     * @param queryInputStream      Siddhi Query InputStream object
//     * @param siddhiAppString       Complete Siddhi app string
//     * @return                      JoinStreamQueryConfig object
//     */
//    private JoinStreamQueryConfig generateJoinStreamQueryConfig(InputStream queryInputStream, String siddhiAppString) {
//        JoinInputStream joinInputStream = (JoinInputStream) queryInputStream;
//
//        // Left side Stream Information of the join
//        JoinedStreamConfig leftStreamConfig =
//                generateJoinedStreamConfig((SingleInputStream)(joinInputStream.getLeftInputStream()), siddhiAppString);
//        // Right side Stream Information of the join
//        JoinedStreamConfig rightStreamConfig =
//                generateJoinedStreamConfig((SingleInputStream)(joinInputStream.getRightInputStream()), siddhiAppString);
//
//        // Set 'isUnidirectional'
//        if (joinInputStream.getTrigger().name().equalsIgnoreCase(JoinDirection.LEFT.toString())) {
//            leftStreamConfig.isUnidirectional = true;
//        } else if (joinInputStream.getTrigger().name().equalsIgnoreCase(JoinDirection.RIGHT.toString())) {
//            rightStreamConfig.isUnidirectional = true;
//        }
//
//        return new JoinStreamQueryConfig(
//                generateJoinDirectionConfig(leftStreamConfig),
//                joinInputStream.getType().name(),
//                generateJoinDirectionConfig(rightStreamConfig),
//                ConfigBuildingUtilities.getDefinition(joinInputStream.getOnCompare(), siddhiAppString));
//    }

//    /**
//     * Generates Config for a Join Table Query Input, from the given Siddhi Query
//     * @param queryInputStream      Siddhi Query InputStream object
//     * @param siddhiAppString       Complete Siddhi app string
//     * @return                      JoinTableQueryConfig object
//     */
//    private JoinTableQueryConfig generateJoinTableQueryConfig(InputStream queryInputStream, String siddhiAppString) {
//        JoinInputStream joinInputStream = (JoinInputStream) queryInputStream;
//
//        // Left side Stream Information of the join
//        JoinedStreamConfig leftStreamConfig =
//                generateJoinedStreamConfig((SingleInputStream)(joinInputStream.getLeftInputStream()), siddhiAppString);
//
//        // Right side Stream Information of the join
//        JoinedStreamConfig rightStreamConfig =
//                generateJoinedStreamConfig((SingleInputStream)(joinInputStream.getRightInputStream()), siddhiAppString);
//
//        return new JoinTableQueryConfig(
//                generateJoinDirectionConfig(leftStreamConfig),
//                joinInputStream.getType().name(),
//                generateJoinDirectionConfig(rightStreamConfig),
//                ConfigBuildingUtilities.getDefinition(joinInputStream.getOnCompare(), siddhiAppString));
//    }

//    /**
//     * Generates Config for a Join Aggregation Query Input, from the given Siddhi Query
//     * @param queryInputStream      Siddhi Query InputStream object
//     * @param siddhiAppString       Complete Siddhi app string
//     * @return                      JoinAggregationQueryConfig object
//     */
//    private JoinAggregationQueryConfig generateJoinAggregationQueryConfig(InputStream queryInputStream,
//                                                                          String siddhiAppString) {
//        JoinInputStream joinInputStream = (JoinInputStream) queryInputStream;
//
//        // Left side Stream Information of the join
//        JoinedStreamConfig leftStreamConfig =
//                generateJoinedStreamConfig((SingleInputStream)(joinInputStream.getLeftInputStream()), siddhiAppString);
//
//        // Right side Stream Information of the join
//        JoinedStreamConfig rightStreamConfig =
//                generateJoinedStreamConfig((SingleInputStream)(joinInputStream.getRightInputStream()), siddhiAppString);
//
//        return new JoinAggregationQueryConfig(
//                generateJoinDirectionConfig(leftStreamConfig),
//                joinInputStream.getType().name(),
//                generateJoinDirectionConfig(rightStreamConfig),
//                ConfigBuildingUtilities.getDefinition(joinInputStream.getOnCompare(), siddhiAppString),
//                ConfigBuildingUtilities.getDefinition(joinInputStream.getWithin(), siddhiAppString),
//                ConfigBuildingUtilities.getDefinition(joinInputStream.getPer(), siddhiAppString));
//    }

//    /**
//     * Generates Config for a Join Window Query Input, from the given Siddhi Query
//     * @param queryInputStream      Siddhi Query InputStream object
//     * @param siddhiAppString       Complete Siddhi app string
//     * @return                      JoinWindowQueryConfig object
//     */
//    private JoinWindowQueryConfig generateJoinWindowQueryConfig(InputStream queryInputStream,
//                                                                String siddhiAppString) {
//        JoinInputStream joinInputStream = (JoinInputStream) queryInputStream;
//
//        // Left side Stream Information of the join
//        JoinedStreamConfig leftStreamConfig =
//                generateJoinedStreamConfig((SingleInputStream)(joinInputStream.getLeftInputStream()), siddhiAppString);
//
//        // Right side Stream Information of the join
//        JoinedStreamConfig rightStreamConfig =
//                generateJoinedStreamConfig((SingleInputStream)(joinInputStream.getRightInputStream()), siddhiAppString);
//
//        return new JoinWindowQueryConfig(
//                generateJoinDirectionConfig(leftStreamConfig),
//                generateJoinDirectionConfig(rightStreamConfig),
//                ConfigBuildingUtilities.getDefinition(joinInputStream.getOnCompare(), siddhiAppString));
//    }
//
//    /**
//     * Generates Config for a Joined Stream of a Join Query, with the given Siddhi SingleInputStream
//     * @param singleInputStream     Siddhi SingleInputStream object,
//     *                              that has information about a stream of a Join Query
//     * @param siddhiAppString       Complete Siddhi app string
//     * @return                      JoinedStreamConfig object, that has specific extracted information
//     *                              from the Siddhi SingleInputStream
//     */
//    private JoinedStreamConfig generateJoinedStreamConfig(SingleInputStream singleInputStream,
//                                                          String siddhiAppString) {
//        JoinedStreamConfig joinedStreamConfig = new JoinedStreamConfig();
//        joinedStreamConfig.name = singleInputStream.getStreamId();
//        joinedStreamConfig.as = singleInputStream.getStreamReferenceId();
//
//        for (StreamHandler streamHandler : singleInputStream.getStreamHandlers()) {
//            if (streamHandler instanceof Filter) {
//                String definition = ConfigBuildingUtilities.getDefinition(streamHandler, siddhiAppString);
//                joinedStreamConfig.filter = definition.substring(1, definition.length() - 1).trim();
//            } else if (streamHandler instanceof Window) {
//                for (Expression expression : streamHandler.getParameters()) {
//                    joinedStreamConfig.windowParameters.add(
//                            ConfigBuildingUtilities.getDefinition(expression, siddhiAppString));
//                }
//                joinedStreamConfig.windowFunction = ((Window)streamHandler).getName();
//            }
//        }
//
//        return joinedStreamConfig;
//    }
//
//    /**
//     * Generates a JoinDirectionConfig object from the given JoinedStreamConfig object
//     * @param joinedStreamConfig        JoinedStreamConfig object, that has specific extracted information
//     *                                  of a stream that is present in a Join Query
//     * @return                          JoinDirectionConfig object, that will be used in a Join Query Config
//     */
//    private JoinDirectionConfig generateJoinDirectionConfig(JoinedStreamConfig joinedStreamConfig) {
//        return new JoinDirectionConfig(
//                joinedStreamConfig.name,
//                joinedStreamConfig.filter,
//                new JoinDirectionWindowConfig(
//                        joinedStreamConfig.windowFunction,
//                        joinedStreamConfig.windowParameters),
//                joinedStreamConfig.isUnidirectional,
//                joinedStreamConfig.as);
//    }
//
//    /**
//     * Config for a stream present in a side of the join
//     */
//    private class JoinedStreamConfig {
//        private String name;
//        private String filter;
//        private String windowFunction;
//        private List<String> windowParameters;
//        private String as;
//        private boolean isUnidirectional;
//
//        private JoinedStreamConfig() {
//            this.name = "";
//            this.filter = "";
//            this.windowFunction = "";
//            this.windowParameters = new ArrayList<>();
//            this.as = "";
//            this.isUnidirectional = false;
//        }
//    }

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
