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

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.pattern.PatternQueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.pattern.PatternQueryEventConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.pattern.event.AndOrPatternEventConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.pattern.event.CountingPatternEventConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.pattern.event.NotAndPatternEventConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.pattern.event.NotForPatternEventConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.handler.Filter;
import org.wso2.siddhi.query.api.execution.query.input.state.*;
import org.wso2.siddhi.query.api.execution.query.input.stream.BasicSingleInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.StateInputStream;

import java.util.ArrayList;
import java.util.List;

/**
 * Generator for Pattern Query Config
 */
public class PatternConfigGenerator {
    private String siddhiAppString;

    private List<PatternQueryEventConfig> events = new ArrayList<>();

    public PatternConfigGenerator(String siddhiAppString) {
        this.siddhiAppString = siddhiAppString;
    }

    /**
     * Gets a PatternQueryConfig object, from the Query object
     * @param queryInputStream      Siddhi Query InputStream
     * @return                      PatternQueryConfig object
     */
    public PatternQueryConfig getPatternQueryConfig(InputStream queryInputStream) {
        addEvent(((StateInputStream) queryInputStream).getStateElement());
        return new PatternQueryConfig(events);
    }

    /**
     * Adds EventConfig objects generated from the given StateElement object, in a recursive manner
     * @param stateElement      Siddhi StateElement object, that holds data about a Pattern query's element
     */
    private void addEvent(StateElement stateElement) {
        if (stateElement instanceof NextStateElement) {
            addEvent(((NextStateElement) stateElement).getStateElement());
            addEvent(((NextStateElement) stateElement).getNextStateElement());
        } else if (stateElement instanceof EveryStateElement){
            events.add(generateEveryPatternEventConfig((EveryStateElement) stateElement));
        } else {
            events.add(generatePatternEventConfig(stateElement));
        }
    }

    /**
     * Returns the type of Pattern Event Config to generate, from the given Siddhi StateElement object
     * @param stateElement      Siddhi StateElement object, that holds data about a Pattern query's element
     * @return                  Type of Pattern Event Config to generate
     */
    private EventConfigType getEventConfigType(StateElement stateElement) {
        if (stateElement instanceof CountStateElement) {
            return EventConfigType.COUNTING;
        } else if (stateElement instanceof LogicalStateElement) {
            LogicalStateElement logicalStateElement = (LogicalStateElement) stateElement;
            String logicalStateElementType = logicalStateElement.getType().name();

            if ((logicalStateElement.getStreamStateElement1() instanceof AbsentStreamStateElement) &&
                    (logicalStateElementType.equalsIgnoreCase(AndOrEventConfigType.AND.toString()))) {
                return EventConfigType.NOT_AND;
            } else if (logicalStateElementType.equalsIgnoreCase(AndOrEventConfigType.AND.toString()) ||
                    logicalStateElementType.equalsIgnoreCase(AndOrEventConfigType.OR.toString())) {
                return EventConfigType.AND_OR;
            }
            throw new IllegalArgumentException("Event config type for StateElement is unknown");
        } else if (stateElement instanceof AbsentStreamStateElement) {
            return EventConfigType.NOT_FOR;
        }
        throw new IllegalArgumentException("Event config type for StateElement is unknown");
    }

    /**
     * Generates an Event Config object for a Pattern Query's element, from the given Siddhi State Element object
     * @param stateElement          Siddhi StateElement object, that holds data about a Pattern query's element
     * @return                      Event Config object of a Pattern Query's element
     */
    private PatternQueryEventConfig generatePatternEventConfig(StateElement stateElement) {
        switch (getEventConfigType(stateElement)) {
            case COUNTING:
                return generateCountPatternEventConfig((CountStateElement) stateElement);
            case NOT_AND:
                return generateNotAndEventConfig((LogicalStateElement) stateElement);
            case AND_OR:
                return generateAndOrEventConfig((LogicalStateElement) stateElement);
            case NOT_FOR:
                return generateNotForEventConfig((AbsentStreamStateElement) stateElement);
            default:
                throw new IllegalArgumentException("Unknown type: " + getEventConfigType(stateElement) +
                        " for generating Event Config");
        }
    }

    /**
     * Generates an Event Config object for a Pattern Query's element which has 'every' keyword,
     * from the given Siddhi EveryStateElement object
     * @param everyStateElement     Siddhi EveryStateElement object, which represents a Pattern Query's element
     *                              with 'every' keyword
     * @return                      Event Config object of a Pattern Query's 'every' element
     */
    private PatternQueryEventConfig generateEveryPatternEventConfig(EveryStateElement everyStateElement) {
        PatternQueryEventConfig patternQueryEventConfig =
                generatePatternEventConfig(everyStateElement.getStateElement());
        // Set 'within'
        if (everyStateElement.getWithin() != null) {
            patternQueryEventConfig.setWithin(
                    ConfigBuildingUtilities.getDefinition(everyStateElement.getWithin(), siddhiAppString));
        }
        patternQueryEventConfig.setForEvery(true);
        return patternQueryEventConfig;
    }

    /**
     * Generates an Event Config object for a Logical Pattern Query's element with a single 'and' or 'or' keyword,
     * from the given Siddhi LogicalStateElement object
     * @param logicalStateElement       Siddhi LogicStateElement object, which represents a Pattern Query's element
     *                                  with a single 'and' or 'or' keyword
     * @return                          Event Config object of an 'and' or 'or' Element
     */
    private AndOrPatternEventConfig generateAndOrEventConfig(LogicalStateElement logicalStateElement) {
        AndOrPatternEventConfig andOrPatternEventConfig = new AndOrPatternEventConfig();

        // Set Left Stream elements
        StreamStateElementConfig leftStreamStateElementConfig =
                generateStreamStateElementConfig(logicalStateElement.getStreamStateElement1());
        andOrPatternEventConfig.setLeftStreamEventReference(leftStreamStateElementConfig.streamReference);
        andOrPatternEventConfig.setLeftStreamName(leftStreamStateElementConfig.streamName);
        andOrPatternEventConfig.setLeftStreamFilter(leftStreamStateElementConfig.streamFilter);

        // 'and' or 'or'
        andOrPatternEventConfig.setConnectedWith(logicalStateElement.getType().name());

        // Set Right Stream elements
        StreamStateElementConfig rightStreamStateElementConfig =
                generateStreamStateElementConfig(logicalStateElement.getStreamStateElement2());
        andOrPatternEventConfig.setRightStreamEventReference(rightStreamStateElementConfig.streamReference);
        andOrPatternEventConfig.setRightStreamName(rightStreamStateElementConfig.streamName);
        andOrPatternEventConfig.setRightStreamFilter(rightStreamStateElementConfig.streamFilter);

        // Set Within
        if (logicalStateElement.getWithin() != null) {
            andOrPatternEventConfig.setWithin(
                    ConfigBuildingUtilities.getDefinition(logicalStateElement.getWithin(), siddhiAppString));
        }

        return andOrPatternEventConfig;
    }

    /**
     * Generates an Event Config object for a Logical Pattern Query's element with 'not' and 'for' keywords,
     * from the given Siddhi AbsentStreamStateElement object
     * @param absentStreamStateElement      Siddhi LogicStateElement object, which represents a Pattern Query's element
     *                                      with 'not' and 'for' keywords
     * @return                              Event Config object of a 'not for' Element
     */
    private NotForPatternEventConfig generateNotForEventConfig(AbsentStreamStateElement absentStreamStateElement) {
        StreamStateElementConfig streamStateElementConfig = generateStreamStateElementConfig(absentStreamStateElement);

        NotForPatternEventConfig notForPatternEventConfig = new NotForPatternEventConfig();
        notForPatternEventConfig.setStreamName(streamStateElementConfig.streamName);
        notForPatternEventConfig.setFilter(streamStateElementConfig.streamFilter);
        notForPatternEventConfig.setForDuration(
                ConfigBuildingUtilities.getDefinition(
                        absentStreamStateElement.getWaitingTime(), siddhiAppString));

        return notForPatternEventConfig;
    }

    /**
     * Generates an Event Config object for a Logical Pattern Query's element with 'not' and 'and' keywords,
     * from the given Siddhi LogicalStateElement object
     * @param logicalStateElement           Siddhi LogicStateElement object, which represents a Pattern Query's element
     *                                      with 'not' and 'and' keywords
     * @return                              Event Config object of a 'not and' Element
     */
    private NotAndPatternEventConfig generateNotAndEventConfig(LogicalStateElement logicalStateElement) {
        NotAndPatternEventConfig notAndPatternEventConfig = new NotAndPatternEventConfig();

        StreamStateElementConfig leftStreamStateElementConfig =
                generateStreamStateElementConfig(logicalStateElement.getStreamStateElement1());
        notAndPatternEventConfig.setLeftStreamName(leftStreamStateElementConfig.streamName);
        notAndPatternEventConfig.setLeftStreamFilter(leftStreamStateElementConfig.streamFilter);

        StreamStateElementConfig rightStreamStateElementConfig =
                generateStreamStateElementConfig(logicalStateElement.getStreamStateElement2());
        notAndPatternEventConfig.setRightStreamEventReference(rightStreamStateElementConfig.streamReference);
        notAndPatternEventConfig.setRightStreamName(rightStreamStateElementConfig.streamName);
        notAndPatternEventConfig.setRightStreamFilter(rightStreamStateElementConfig.streamFilter);

        // Set Within
        if (logicalStateElement.getWithin() != null) {
            notAndPatternEventConfig.setWithin(
                    ConfigBuildingUtilities.getDefinition(logicalStateElement.getWithin(), siddhiAppString));
        }

        return notAndPatternEventConfig;
    }

    /**
     * Generates an Event Config object for a Counting Pattern Query's element,
     * from the given Siddhi CountStateElement object
     * @param countStateElement             Siddhi CountStateElement object, which represents a Pattern Query's element
     * @return                              Event Config object of a Counting Pattern Element
     */
    private CountingPatternEventConfig generateCountPatternEventConfig(CountStateElement countStateElement) {
        CountingPatternEventConfig countingPatternEventConfig =
                generateCountPatternEventConfig(countStateElement.getStreamStateElement());

        // Set minCount & maxCount
        countingPatternEventConfig.setMinCount(String.valueOf(countStateElement.getMinCount()));
        countingPatternEventConfig.setMaxCount(String.valueOf(countStateElement.getMaxCount()));

        // Set Within
        if (countStateElement.getWithin() != null) {
            countingPatternEventConfig.setWithin(
                    ConfigBuildingUtilities.getDefinition(countStateElement.getWithin(), siddhiAppString));
        }

        return countingPatternEventConfig;
    }

    /**
     * Generates an Event Config object, from the given Siddhi StreamStateElement object
     * @param streamStateElement        Siddhi StreamStateElement object, which represents State of a Stream
     * @return                          Incomplete Event Config object of a StreamStateElement
     */
    private CountingPatternEventConfig generateCountPatternEventConfig(StreamStateElement streamStateElement) {
        StreamStateElementConfig streamStateElementConfig = generateStreamStateElementConfig(streamStateElement);

        CountingPatternEventConfig countingPatternEventConfig = new CountingPatternEventConfig();
        countingPatternEventConfig.setEventReference(streamStateElementConfig.streamReference);
        countingPatternEventConfig.setStreamName(streamStateElementConfig.streamName);
        countingPatternEventConfig.setFilter(streamStateElementConfig.streamFilter);
        countingPatternEventConfig.setWithin(streamStateElementConfig.within);

        return countingPatternEventConfig;
    }

    /**
     * Generates a Stream State Element Config object, from the given Siddhi StreamStateElement object
     * @param streamStateElement        Siddhi StreamStateElement object, which represents a State element of a Stream
     * @return                          Stream State Config object
     */
    private StreamStateElementConfig generateStreamStateElementConfig(StreamStateElement streamStateElement) {
        BasicSingleInputStream basicSingleInputStream = streamStateElement.getBasicSingleInputStream();

        StreamStateElementConfig streamStateElementConfig = new StreamStateElementConfig();
        streamStateElementConfig.streamReference = basicSingleInputStream.getStreamReferenceId();
        streamStateElementConfig.streamName = basicSingleInputStream.getStreamId();

        // Set Filter
        if (basicSingleInputStream.getStreamHandlers().size() == 1) {
            if (basicSingleInputStream.getStreamHandlers().get(0) instanceof Filter) {
                String filterExpression = ConfigBuildingUtilities
                        .getDefinition(basicSingleInputStream.getStreamHandlers().get(0), siddhiAppString);
                streamStateElementConfig.streamFilter =
                        filterExpression.substring(1, filterExpression.length() - 1).trim();
            } else {
                throw new IllegalArgumentException("Unknown type of stream handler in BasicSingleInputStream");
            }
        } else if (!basicSingleInputStream.getStreamHandlers().isEmpty()) {
            throw new IllegalArgumentException(
                    "Failed to convert more than one stream handlers within a BasicSingleInputStream");
        }

        // Set Within
        if (streamStateElement.getWithin() != null) {
            streamStateElementConfig.within =
                    ConfigBuildingUtilities.getDefinition(streamStateElement.getWithin(), siddhiAppString);
        }

        return streamStateElementConfig;
    }

    /**
     * Config for a Siddhi Stream State Element
     */
    private class StreamStateElementConfig {
        private String streamName;
        private String streamReference;
        private String streamFilter;
        private String within;

        /**
         * Constructs with default values
         */
        private StreamStateElementConfig() {
            streamName = "";
            streamReference = "";
            streamFilter = "";
            within = "";
        }
    }

    /**
     * Event Config Type
     */
    private enum EventConfigType {
        COUNTING,
        NOT_AND,
        AND_OR,
        NOT_FOR
    }

    /**
     * And Or Event Config Type
     */
    private enum AndOrEventConfigType {
        AND,
        OR
    }
}
