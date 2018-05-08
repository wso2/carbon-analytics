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

package org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.input.types.OLD_REMOVE;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.OLD_REMOVE.sequence.SequenceQueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.OLD_REMOVE.sequence.event.AndOrSequenceEventConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.OLD_REMOVE.sequence.event.CountingSequenceEventConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.OLD_REMOVE.sequence.SequenceQueryEventConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.OLD_REMOVE.sequence.event.NotAndSequenceEventConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.OLD_REMOVE.sequence.event.NotForSequenceEventConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.OLD_REMOVE.sequence.event.countingsequence.CountingPatternCountingSequence;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.OLD_REMOVE.sequence.event.countingsequence.CountingSequenceConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.OLD_REMOVE.sequence.event.countingsequence.MinMaxCountingSequenceValue;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import org.wso2.siddhi.query.api.execution.query.input.handler.Filter;
import org.wso2.siddhi.query.api.execution.query.input.state.*;
import org.wso2.siddhi.query.api.execution.query.input.stream.BasicSingleInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.StateInputStream;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates SequenceQueryConfig with given Siddhi elements
 */
public class SequenceConfigGenerator {
    private String siddhiAppString;

    private boolean isEvery = false;
    private boolean isFirstStateElementTraversed = false;
    private String firstWithinStatement = "";

    private boolean isEveryStateToggled = false;
    private boolean isNotForExist = false;
    private int notForIndex = -1;
    private int sequenceElementsCount = 0;

    private List<String> withins = new ArrayList<>();


    private List<SequenceQueryEventConfig> events = new ArrayList<>();

    public SequenceConfigGenerator(String siddhiAppString) {
        this.siddhiAppString = siddhiAppString;
    }

    /**
     * Gets a SequenceQueryConfig object, from the given Siddhi Query InputStream object
     * @param queryInputStream      Siddhi Query InputStream object
     * @return                      SequenceQueryConfig object
     */
    public SequenceQueryConfig getSequenceQueryConfig(InputStream queryInputStream) {
        addEvent(((StateInputStream) queryInputStream).getStateElement());
        // Penetrate NotFor within
        if (isNotForExist) {
            withins.add(notForIndex, firstWithinStatement);
            withins.remove(0);
        }

        // Add Within
        for (int i = 0; i < events.size(); i++) {
            events.get(i).setWithin(withins.get(i));
        }

        // Add Every
        if (isEvery) {
            events.get(0).setForEvery(true);
        }

        return new SequenceQueryConfig(events);
    }

    /**
     * Adds EventConfig objects generated from the given StateElement object, in a recursive manner
     * @param stateElement      Siddhi StateElement object, that holds data about a Sequence query's element
     */
    private void addEvent(StateElement stateElement) {
        if (!isFirstStateElementTraversed) {
            if (stateElement.getWithin() != null) {
                // This is the first ever element that has 'within'
                firstWithinStatement = ConfigBuildingUtilities.getDefinition(stateElement.getWithin(), siddhiAppString);
                isFirstStateElementTraversed = true;
            }
        }
        if (stateElement.getWithin() != null) {
            withins.add(ConfigBuildingUtilities.getDefinition(stateElement.getWithin(), siddhiAppString));
        }
        if (stateElement instanceof NextStateElement) {
            addEvent(((NextStateElement) stateElement).getStateElement());
            addEvent(((NextStateElement) stateElement).getNextStateElement());
        } else if (stateElement instanceof EveryStateElement){
            // Only the first Element of Sequence can fall into this. Otherwise a compilation error would have occurred
            isEvery = true;
            isEveryStateToggled = true; // TODO: 4/20/18 confirm
            addEvent(((EveryStateElement) stateElement).getStateElement());
            isEveryStateToggled = false; // TODO: 4/20/18 confirm
        } else {
            // This is a single element
            sequenceElementsCount++;
            events.add(generateSequenceEventConfig(stateElement));
        }
    }

    /**
     * Returns the type of Sequence Event Config to generate, from the given Siddhi StateElement object
     * @param stateElement      Siddhi StateElement object, that holds data about a Sequence query's element
     * @return                  Type of Sequence Event Config to generate
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
            notForIndex = sequenceElementsCount;
            isNotForExist = true;
            return EventConfigType.NOT_FOR;
        }
        throw new IllegalArgumentException("Event config type for StateElement is unknown");
    }

    /**
     * Generates an Event Config object for a Sequence Query's element, from the given Siddhi State Element object
     * @param stateElement      Siddhi StateElement object, that holds data about a Sequence query's element
     * @return                  Event Config object of a Sequence Query's element
     */
    private SequenceQueryEventConfig generateSequenceEventConfig(StateElement stateElement) {
        switch (getEventConfigType(stateElement)) {
            case COUNTING:
                // Single element without within statement todo in a nice way
                if (!isEveryStateToggled && (stateElement.getWithin() == null)) {
                    withins.add("");
                }
                return generateCountSequenceEventConfig((CountStateElement) stateElement);
            case NOT_AND:
                // Single element without within statement todo in a nice way
                if (!isEveryStateToggled && (stateElement.getWithin() == null)) {
                    withins.add("");
                }
                return generateNotAndEventConfig((LogicalStateElement) stateElement);
            case AND_OR:
                // Single element without within statement todo in a nice way
                if (!isEveryStateToggled && (stateElement.getWithin() == null)) {
                    withins.add("");
                }
                return generateAndOrEventConfig((LogicalStateElement) stateElement);
            case NOT_FOR:
                return generateNotForEventConfig((AbsentStreamStateElement) stateElement);
            default:
                throw new IllegalArgumentException("Unknown type: " + getEventConfigType(stateElement) +
                        " for generating Event Config");
        }
    }

//    /**
//     * Generates an Event Config object for a Sequence Query's element which has 'every' keyword,
//     * from the given Siddhi EveryStateElement object
//     * @param everyStateElement     Siddhi EveryStateElement object, which represents a Sequence Query's element
//     *                              with 'every' keyword
//     * @return                      Event Config object of a Sequence Query's 'every' element
//     */
//    private SequenceQueryEventConfig generateEverySequenceEventConfig(EveryStateElement everyStateElement) {
//        SequenceQueryEventConfig sequenceQueryEventConfig =
//                generateSequenceEventConfig(everyStateElement.getStateElement());
//         // Set 'within'
//        if (everyStateElement.getWithin() != null) {
//            sequenceQueryEventConfig.setWithin(
//                    ConfigBuildingUtilities.getDefinition(everyStateElement.getWithin(), siddhiAppString));
//        }
//
//        sequenceQueryEventConfig.setForEvery(true);
//        return sequenceQueryEventConfig;
//    }

    /**
     * Generates an Event Config object for a Sequence Query's element with a single 'and' or 'or' keyword,
     * from the given Siddhi LogicalStateElement object
     * @param logicalStateElement       Siddhi LogicStateElement object, which represents a Sequence Query's element
     *                                  with a single 'and' or 'or' keyword
     * @return                          Event Config object of an 'and' or 'or' Element
     */
    private AndOrSequenceEventConfig generateAndOrEventConfig(LogicalStateElement logicalStateElement) {
        AndOrSequenceEventConfig andOrSequenceEventConfig = new AndOrSequenceEventConfig();

        // Set Left Stream elements
        StreamStateElementConfig leftStreamStateElementConfig =
                generateStreamStateElementConfig(logicalStateElement.getStreamStateElement1());
        andOrSequenceEventConfig.setLeftStreamEventReference(leftStreamStateElementConfig.streamReference);
        andOrSequenceEventConfig.setLeftStreamName(leftStreamStateElementConfig.streamName);
        andOrSequenceEventConfig.setLeftStreamFilter(leftStreamStateElementConfig.streamFilter);

        // 'and' or 'or'
        andOrSequenceEventConfig.setConnectedWith(logicalStateElement.getType().name());

        // Set Right Stream elements
        StreamStateElementConfig rightStreamStateElementConfig =
                generateStreamStateElementConfig(logicalStateElement.getStreamStateElement2());
        andOrSequenceEventConfig.setRightStreamEventReference(rightStreamStateElementConfig.streamReference);
        andOrSequenceEventConfig.setRightStreamName(rightStreamStateElementConfig.streamName);
        andOrSequenceEventConfig.setRightStreamFilter(rightStreamStateElementConfig.streamFilter);

        // Set Within
        if (logicalStateElement.getWithin() != null) {
            andOrSequenceEventConfig.setWithin(
                    ConfigBuildingUtilities.getDefinition(logicalStateElement.getWithin(), siddhiAppString));
        }

        return andOrSequenceEventConfig;
    }

    /**
     * Generates an Event Config object for a Sequence Query's element with 'not' and 'for' keywords,
     * from the given Siddhi AbsentStreamStateElement object
     * @param absentStreamStateElement      Siddhi LogicStateElement object, which represents a Sequence Query's element
     *                                      with 'not' and 'for' keywords
     * @return                              Event Config object of a 'not for' Element
     */
    private NotForSequenceEventConfig generateNotForEventConfig(AbsentStreamStateElement absentStreamStateElement) {
        StreamStateElementConfig streamStateElementConfig = generateStreamStateElementConfig(absentStreamStateElement);

        NotForSequenceEventConfig notForSequenceEventConfig = new NotForSequenceEventConfig();
        notForSequenceEventConfig.setStreamName(streamStateElementConfig.streamName);
        notForSequenceEventConfig.setFilter(streamStateElementConfig.streamFilter);
        notForSequenceEventConfig.setForDuration(
                ConfigBuildingUtilities.getDefinition(
                        absentStreamStateElement.getWaitingTime(), siddhiAppString));
        notForSequenceEventConfig.setWithin(streamStateElementConfig.within);

        // Set Within
        if (absentStreamStateElement.getWithin() != null) {
            notForSequenceEventConfig.setWithin(
                    ConfigBuildingUtilities.getDefinition(absentStreamStateElement.getWithin(), siddhiAppString));
        }

        return notForSequenceEventConfig;
    }

    /**
     * Generates an Event Config object for a Sequence Query's element with 'not' and 'and' keywords,
     * from the given Siddhi LogicalStateElement object
     * @param logicalStateElement           Siddhi LogicStateElement object, which represents a Sequence Query's element
     *                                      with 'not' and 'and' keywords
     * @return                              Event Config object of a 'not and' Element
     */
    private NotAndSequenceEventConfig generateNotAndEventConfig(LogicalStateElement logicalStateElement) {
        NotAndSequenceEventConfig notAndSequenceEventConfig = new NotAndSequenceEventConfig();

        // Set Left Stream elements
        StreamStateElementConfig leftStreamStateElementConfig =
                generateStreamStateElementConfig(logicalStateElement.getStreamStateElement1());
        notAndSequenceEventConfig.setLeftStreamName(leftStreamStateElementConfig.streamName);
        notAndSequenceEventConfig.setLeftStreamFilter(leftStreamStateElementConfig.streamFilter);

        // Set Right Stream elements
        StreamStateElementConfig rightStreamStateElementConfig =
                generateStreamStateElementConfig(logicalStateElement.getStreamStateElement2());
        notAndSequenceEventConfig.setRightStreamEventReference(rightStreamStateElementConfig.streamReference);
        notAndSequenceEventConfig.setRightStreamName(rightStreamStateElementConfig.streamName);
        notAndSequenceEventConfig.setRightStreamFilter(rightStreamStateElementConfig.streamFilter);

        // Set Within
        if (logicalStateElement.getWithin() != null) {
            notAndSequenceEventConfig.setWithin(
                    ConfigBuildingUtilities.getDefinition(logicalStateElement.getWithin(), siddhiAppString));
        }

        return notAndSequenceEventConfig;
    }

    /**
     * Generates an Event Config object for a Counting Sequence Query's element,
     * from the given Siddhi CountStateElement object
     * @param countStateElement             Siddhi CountStateElement object, which represents a Sequence Query's element
     * @return                              Event Config object of a Counting Sequence Element
     */
    private CountingSequenceEventConfig generateCountSequenceEventConfig(CountStateElement countStateElement) {
        CountingSequenceEventConfig countingSequenceEventConfig =
                generateCountSequenceEventConfig(countStateElement.getStreamStateElement());

        // Set CountingSequence
        CountingSequenceConfig countingSequence;
        if (countStateElement.getMinCount() == 0 && countStateElement.getMaxCount() == 1) {
            countingSequence =
                    new CountingPatternCountingSequence(CountingPatternCountingSequenceType.ZERO_OR_ONE.toString());
        } else if (countStateElement.getMinCount() == 0 && countStateElement.getMaxCount() == -1) {
            countingSequence =
                    new CountingPatternCountingSequence(CountingPatternCountingSequenceType.ZERO_OR_MORE.toString());
        } else if (countStateElement.getMinCount() == 1 && countStateElement.getMaxCount() == -1) {
            countingSequence =
                    new CountingPatternCountingSequence(CountingPatternCountingSequenceType.ONE_OR_MORE.toString());
        } else {
            countingSequence = new MinMaxCountingSequenceValue(
                    String.valueOf(countStateElement.getMinCount()),
                    String.valueOf(countStateElement.getMaxCount()));
        }
        countingSequenceEventConfig.setCountingSequence(countingSequence);

        // Set Within
        if (countStateElement.getWithin() != null) {
            countingSequenceEventConfig.setWithin(
                    ConfigBuildingUtilities.getDefinition(countStateElement.getWithin(), siddhiAppString));
        }

        return countingSequenceEventConfig;
    }

    /**
     * Generates an Event Config object, from the given Siddhi StreamStateElement object
     * @param streamStateElement        Siddhi StreamStateElement object, which represents State of a Stream
     * @return                          Incomplete Event Config object of a StreamStateElement
     */
    private CountingSequenceEventConfig generateCountSequenceEventConfig(StreamStateElement streamStateElement) {
        StreamStateElementConfig streamStateElementConfig = generateStreamStateElementConfig(streamStateElement);

        CountingSequenceEventConfig countingSequenceEventConfig = new CountingSequenceEventConfig();
        countingSequenceEventConfig.setEventReference(streamStateElementConfig.streamReference);
        countingSequenceEventConfig.setStreamName(streamStateElementConfig.streamName);
        countingSequenceEventConfig.setFilter(streamStateElementConfig.streamFilter);
        countingSequenceEventConfig.setWithin(streamStateElementConfig.within);

        return countingSequenceEventConfig;
    }

    /**
     * Generates a Stream State Element Config object, from the given Siddhi StreamStateElement object
     * @param streamStateElement        Siddhi StreamStateElement object, which represents State of a Stream
     * @return                          Stream State Config object, which contains Stream State data
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
     * Counting Sequence's CountingPattern type
     */
    private enum CountingPatternCountingSequenceType {
        ONE_OR_MORE, // +
        ZERO_OR_MORE, // *
        ZERO_OR_ONE // ?
    }

    /**
     * And Or Event Config Type
     */
    private enum AndOrEventConfigType {
        AND,
        OR
    }
}
