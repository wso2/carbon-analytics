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

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.patternsequence.PatternSequenceConditionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.patternsequence.PatternSequenceConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.query.QueryInputType;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.input.types.patternsequencesupporters.*;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.streamhandler.StreamHandlerConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import org.wso2.siddhi.query.api.SiddhiElement;
import org.wso2.siddhi.query.api.execution.query.input.state.*;
import org.wso2.siddhi.query.api.execution.query.input.stream.BasicSingleInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.StateInputStream;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates PatternQueryConfig with given Siddhi elements
 */
public class PatternSequenceConfigGenerator {
    private static final String PATTERN_DELIMITER = " -> ";
    private static final String SEQUENCE_DELIMITER = " , ";

    // Keywords used in logic
    private static final String WHITE_SPACE = " ";
    private static final String COUNT_MIN = "<";
    private static final String COUNT_MAX = ">";
    private static final String COUNT_MIN_MAX_SEPARATOR = ":";
    private static final String WITHIN = "within";
    private static final String EVERY = "every";
    private static final String NOT = "not";
    private static final String FOR = "for";

    private String siddhiAppString;
    private List<PatternSequenceConditionConfig> conditionList = new ArrayList<>();
    private List<String> logicComponentList = new ArrayList<>();

    private List<String> availableEventReferences = new ArrayList<>();
    private int eventReferenceCounter = 0;

    public PatternSequenceConfigGenerator(String siddhiAppString) {
        this.siddhiAppString = siddhiAppString;
    }

    /**
     * Generates config for a Siddhi Pattern Query Input, from the given Siddhi InputStream object
     * @param inputStream                       Siddhi InputStream object
     * @return                                  PatternSequenceConfig object
     * @throws DesignGenerationException        Error while generating Pattern Query Input
     */
    public PatternSequenceConfig generatePatternConfig(InputStream inputStream)
            throws DesignGenerationException {
        StateElementConfig stateElementConfig =
                generateStateElementConfig(((StateInputStream) inputStream).getStateElement());
        traverseInOrder(stateElementConfig);
        return new PatternSequenceConfig(
                QueryInputType.PATTERN.toString(),
                conditionList,
                String.join(PATTERN_DELIMITER, logicComponentList));
    }

    /**
     * Generates config for a Siddhi Sequence Query Input, from the given Siddhi InputStream object
     * @param inputStream                       Siddhi InputStream object
     * @return                                  PatternSequenceConfig object
     * @throws DesignGenerationException        Error while generating Sequence Query Input
     */
    public PatternSequenceConfig generateSequenceConfig(InputStream inputStream)
            throws DesignGenerationException {
        StateElementConfig stateElementConfig =
                generateStateElementConfig(((StateInputStream) inputStream).getStateElement());
        traverseInOrder(stateElementConfig);
        return new PatternSequenceConfig(
                QueryInputType.SEQUENCE.toString(),
                conditionList,
                String.join(SEQUENCE_DELIMITER, logicComponentList));
    }

    /**
     * Performs an In-order Traversal through the tree of StateElementConfig objects recursively
     * starting from the given StateElementConfig,
     * while acquires necessary details of the StateElements
     * @param currentElement                    Root of the tree during the first call of the method,
     *                                          and the current traversal element during next calls of the method
     * @throws DesignGenerationException        Error occurred while traversing through the tree elements,
     *                                          and acquiring necessary elements
     */
    private void traverseInOrder(StateElementConfig currentElement) throws DesignGenerationException {
        if (currentElement instanceof NextStateElementConfig) {
            traverseInOrder(((NextStateElementConfig) currentElement).getStateElement());
            traverseInOrder(((NextStateElementConfig) currentElement).getNextStateElement());
        } else {
            addElementComponent(currentElement);
        }
    }

    /**
     * Generates PatternSequenceConditionConfig for the given StateElementConfig object
     * @param element                           StateElementConfig object
     * @return                                  PatternSequenceConditionConfig object
     * @throws DesignGenerationException        Type of StateElementConfig is unknown for generating config
     */
    private PatternSequenceConditionConfig generateConditionConfig(StateElementConfig element)
            throws DesignGenerationException {
        PatternSequenceConditionConfig conditionConfig = new PatternSequenceConditionConfig();
        if (element instanceof AbsentStreamStateElementConfig) {
            conditionConfig
                    .setConditionId(
                            acceptOrGenerateNextStreamReference(
                                    ((AbsentStreamStateElementConfig) element).getStreamReference()));
            conditionConfig
                    .setStreamName(((AbsentStreamStateElementConfig) element).getStreamName());
            conditionConfig
                    .setStreamHandlerList(((AbsentStreamStateElementConfig) element).getStreamHandlerList());
            return conditionConfig;
        } else if (element instanceof CountStateElementConfig) {
            conditionConfig
                    .setConditionId(
                            acceptOrGenerateNextStreamReference(
                                    ((CountStateElementConfig) element).getStreamStateElement().getStreamReference()));
            conditionConfig
                    .setStreamName(((CountStateElementConfig) element).getStreamStateElement().getStreamName());
            conditionConfig
                    .setStreamHandlerList(
                            ((CountStateElementConfig) element).getStreamStateElement().getStreamHandlerList());
            return conditionConfig;
        } else if (element instanceof EveryStateElementConfig) {
            conditionConfig
                    .setConditionId(
                            acceptOrGenerateNextStreamReference(
                                    generateConditionConfig(
                                            ((EveryStateElementConfig) element).getStateElement()).getConditionId()));
            conditionConfig
                    .setStreamName(
                            generateConditionConfig(
                                    ((EveryStateElementConfig) element).getStateElement()).getStreamName());
            conditionConfig
                    .setStreamHandlerList(
                            generateConditionConfig(
                                    ((EveryStateElementConfig) element).getStateElement()).getStreamHandlerList());
            return conditionConfig;
        } else if (element instanceof StreamStateElementConfig) {
            conditionConfig
                    .setConditionId(
                            acceptOrGenerateNextStreamReference(
                                    ((StreamStateElementConfig) element).getStreamReference()));
            conditionConfig
                    .setStreamName(((StreamStateElementConfig) element).getStreamName());
            conditionConfig
                    .setStreamHandlerList(((StreamStateElementConfig) element).getStreamHandlerList());
            return conditionConfig;
        }
        throw new DesignGenerationException("Unable to generate condition config for element of type unknown");
    }

    /**
     * Accepts the given stream reference when not null, or otherwise generates the next un-available stream reference
     * @param streamReference       Stream Reference String when available, or null
     * @return                      Accepted/Generated stream reference
     */
    private String acceptOrGenerateNextStreamReference(String streamReference) {
        if (streamReference != null) {
            return streamReference;
        }
        return generateNextUnavailableStreamReference();
    }

    /**
     * Generates and returns the next stream reference - that is not already taken by any streams in this query
     * @return      Next unavailable stream reference
     */
    private String generateNextUnavailableStreamReference() {
        String eventReference;
        do {
            eventReference = generateNextStreamReference();
        } while (availableEventReferences.contains(eventReference));
        return eventReference;
    }

    /**
     * Generates the next stream reference in a pre-defined format
     * @return      Next stream reference
     */
    private String generateNextStreamReference() {
        return "e" + (++eventReferenceCounter);
    }

    /**
     * Adds the given stream reference to the list of available stream references
     * @param streamReference       Stream reference of a Siddhi InputStream when available, or null
     */
    private void addToAvailableStreamReferences(String streamReference) {
        if (streamReference != null) {
            availableEventReferences.add(streamReference);
        }
    }

    /**
     * Add components of the given StateElementConfig to the relevant lists from which,
     * the Pattern/Sequence query's events are represented
     * @param element                       Element of the Pattern/Sequence query
     * @throws DesignGenerationException    Error while generating ElementComponent object
     */
    private void addElementComponent(StateElementConfig element) throws DesignGenerationException {
        ElementComponent elementComponent = generateElementComponent(element);
        conditionList.addAll(elementComponent.conditions);
        logicComponentList.add(elementComponent.logicComponent);
    }

    /**
     * Generates ElementComponent object - from which, an event of a Pattern/Sequence query is represented,
     * for the given StateElementConfig object
     * @param element                           StateElementConfig object
     * @return                                  ElementComponent object
     * @throws DesignGenerationException        Error while generating ElementComponent
     */
    private ElementComponent generateElementComponent(StateElementConfig element) throws DesignGenerationException {
        if (element instanceof CountStateElementConfig) {
            return generateCountStateElementComponents((CountStateElementConfig) element);
        } else if (element instanceof EveryStateElementConfig) {
            return generateEveryStateElementComponents((EveryStateElementConfig) element);
        } else if (element instanceof LogicalStateElementConfig) {
            return generateLogicalStateElementComponents((LogicalStateElementConfig) element);
        } else if (element instanceof StreamStateElementConfig) {
            return generateStreamStateElementComponents((StreamStateElementConfig) element);
        }
        throw new IllegalArgumentException(
                "Failed to generate config for Pattern/Sequence element, since type is unknown");
    }

    /**
     * Generates ElementComponent object for the given CountStateElementConfig object
     * @param element                           CountStateElement object
     * @return                                  ElementComponent object
     * @throws DesignGenerationException        Error while generating ElementComponent
     */
    private ElementComponent generateCountStateElementComponents(CountStateElementConfig element)
            throws DesignGenerationException {
        PatternSequenceConditionConfig conditionConfig = generateConditionConfig(element);
        // Add condition
        List<PatternSequenceConditionConfig> conditions = new ArrayList<>();
        conditions.add(conditionConfig);
        String logicComponentBuilder =
                conditionConfig.getConditionId() +
                WHITE_SPACE +
                COUNT_MIN +
                element.getMin() +
                COUNT_MIN_MAX_SEPARATOR +
                element.getMax() +
                COUNT_MAX +
                WHITE_SPACE +
                buildWithin(element.getWithin());
        return new ElementComponent(conditions, logicComponentBuilder);
    }

    /**
     * Generates ElementComponent object for the given EveryStateElementConfig object
     * @param element                           EveryStateElementConfig object
     * @return                                  ElementComponent object
     * @throws DesignGenerationException        Error while generating ElementComponent object
     */
    private ElementComponent generateEveryStateElementComponents(EveryStateElementConfig element)
            throws DesignGenerationException {
        ElementComponent containedElement = generateElementComponent(element.getStateElement());
        // Add conditions
        List<PatternSequenceConditionConfig> conditions = new ArrayList<>(containedElement.conditions);
        String logicComponentBuilder =
                EVERY +
                WHITE_SPACE +
                containedElement.logicComponent +
                buildWithin(element.getWithin());
        return new ElementComponent(conditions, logicComponentBuilder);
    }

    /**
     * Generates ElementComponent object for the given LogicalStateElementConfig object
     * @param element                           LogicalStateElementConfig object
     * @return                                  ElementComponent object
     * @throws DesignGenerationException        Error while generating ElementComponent object
     */
    private ElementComponent generateLogicalStateElementComponents(LogicalStateElementConfig element)
            throws DesignGenerationException {
        PatternSequenceConditionConfig conditionConfig1 =
                generateConditionConfig(element.getStreamStateElement1());
        PatternSequenceConditionConfig conditionConfig2 =
                generateConditionConfig(element.getStreamStateElement2());
        // Add conditions
        List<PatternSequenceConditionConfig> conditions = new ArrayList<>();
        conditions.add(conditionConfig1);
        conditions.add(conditionConfig2);
        String logicComponentBuilder =
                generateStreamStateElementComponents(element.getStreamStateElement1()).logicComponent +
                WHITE_SPACE +
                element.getType().toLowerCase() +
                WHITE_SPACE +
                generateStreamStateElementComponents(element.getStreamStateElement2()).logicComponent +
                buildWithin(element.getWithin());
        return new ElementComponent(conditions, logicComponentBuilder);
    }

    /**
     * Generates ElementComponent object for the given StreamStateElementConfig object
     * @param element                           StreamStateElementConfig object
     * @return                                  ElementComponent object
     * @throws DesignGenerationException        Error while generating ElementComponent object
     */
    private ElementComponent generateStreamStateElementComponents(StreamStateElementConfig element)
            throws DesignGenerationException {
        PatternSequenceConditionConfig conditionConfig = generateConditionConfig(element);
        boolean isAbsentStreamStateElementConfig = element instanceof AbsentStreamStateElementConfig;
        StringBuilder logicComponentBuilder = new StringBuilder();
        if (isAbsentStreamStateElementConfig) {
            logicComponentBuilder.append(NOT);
            logicComponentBuilder.append(WHITE_SPACE);
        }
        logicComponentBuilder.append(conditionConfig.getConditionId());
        if (isAbsentStreamStateElementConfig &&
                ((AbsentStreamStateElementConfig) element).getWaitingTime() != null) {
            logicComponentBuilder.append(WHITE_SPACE);
            logicComponentBuilder.append(FOR);
            logicComponentBuilder.append(WHITE_SPACE);
            logicComponentBuilder.append(((AbsentStreamStateElementConfig) element).getWaitingTime());
        }

        logicComponentBuilder.append(buildWithin(element.getWithin()));
        // Add condition
        List<PatternSequenceConditionConfig> conditions = new ArrayList<>();
        conditions.add(conditionConfig);
        return new ElementComponent(conditions, logicComponentBuilder.toString());
    }

    /**
     * Returns the 'within' expression when the given parameter is not null.
     * Otherwise, returns an empty string
     * @param nullableWithin        Value contained in 'within', which might be null
     * @return                      Within expression
     */
    private static String buildWithin(String nullableWithin) {
        if (nullableWithin == null) {
            return "";
        }
        return WHITE_SPACE + WITHIN + WHITE_SPACE + nullableWithin;
    }

    /**
     * Creates a StateElementConfig object from the given Siddhi StateElement object
     * @param stateElement                      Siddhi StateElement object
     * @return                                  StateElementConfig object
     * @throws DesignGenerationException        Error while generating StateElementConfig object
     */
    private StateElementConfig generateStateElementConfig(StateElement stateElement) throws DesignGenerationException {
        if (stateElement instanceof StreamStateElement) {
            return generateStreamStateElementConfig((StreamStateElement) stateElement);
        } else if (stateElement instanceof CountStateElement) {
            return generateCountStateElementConfig((CountStateElement) stateElement);
        } else if (stateElement instanceof LogicalStateElement) {
            return generateLogicalStateElementConfig((LogicalStateElement) stateElement);
        } else if (stateElement instanceof EveryStateElement) {
            return generateEveryStateElementConfig((EveryStateElement) stateElement);
        } else if (stateElement instanceof NextStateElement) {
            return generateNextStateElementConfig((NextStateElement) stateElement);
        } else {
            throw new DesignGenerationException("Unknown type of StateElement");
        }
    }

    /**
     * Generates the definition of the given SiddhiElement object, in Siddhi app String.
     * This method wraps the getDefinition method of the ConfigBuildingUtilities class,
     * as the returned value should be null - when null is given for the parameter SiddhiElement
     * @param siddhiElement                     SiddhiElement object
     * @return                                  Definition of the given SiddhiElement object when not null,
     *                                          otherwise returns null
     * @throws DesignGenerationException        Error while getting definition for the non-null SiddhiElement object
     */
    private String generateNullableElementDefinition(SiddhiElement siddhiElement) throws DesignGenerationException {
        if (siddhiElement == null) {
            return null;
        }
        return ConfigBuildingUtilities.getDefinition(siddhiElement, siddhiAppString);
    }

    /**
     * Generates config for the given Siddhi StreamStateElement object
     * @param streamStateElement                Siddhi StreamStateElement object
     * @return                                  StreamStateElement config
     * @throws DesignGenerationException        Error while generating StreamStateElement config
     */
    private StreamStateElementConfig generateStreamStateElementConfig(StreamStateElement streamStateElement)
            throws DesignGenerationException {
        if (streamStateElement instanceof AbsentStreamStateElement) {
            return generateAbsentStreamStateElementConfig((AbsentStreamStateElement) streamStateElement);
        }
        BasicSingleInputStream basicSingleInputStream = streamStateElement.getBasicSingleInputStream();

        StreamStateElementConfig streamStateElementConfig = new StreamStateElementConfig();
        streamStateElementConfig
                .setStreamReference(streamStateElement.getBasicSingleInputStream().getStreamReferenceId());
        streamStateElementConfig.setStreamName(basicSingleInputStream.getStreamId());
        streamStateElementConfig
                .setStreamHandlerList(
                        new StreamHandlerConfigGenerator(siddhiAppString)
                                .generateStreamHandlerConfigList(basicSingleInputStream.getStreamHandlers()));
        streamStateElementConfig.setWithin(generateNullableElementDefinition(streamStateElement.getWithin()));

        addToAvailableStreamReferences(streamStateElement.getBasicSingleInputStream().getStreamReferenceId());

        return streamStateElementConfig;
    }

    /**
     * Generates config for the given Siddhi CountStateElement object
     * @param countStateElement                 Siddhi CountStateElement object
     * @return                                  CountStateElementConfig object
     * @throws DesignGenerationException        Error while generating CountStateElementConfig object
     */
    private CountStateElementConfig generateCountStateElementConfig(CountStateElement countStateElement)
            throws DesignGenerationException {
        CountStateElementConfig countStateElementConfig = new CountStateElementConfig();
        countStateElementConfig
                .setStreamStateElement(generateStreamStateElementConfig(countStateElement.getStreamStateElement()));
        countStateElementConfig.setWithin(generateNullableElementDefinition(countStateElement.getWithin()));
        countStateElementConfig.setMin(countStateElement.getMinCount());
        countStateElementConfig.setMax(countStateElement.getMaxCount());

        return countStateElementConfig;
    }

    /**
     * Generates config for the given Siddhi LogicalStateElement object
     * @param logicalStateElement               Siddhi LogicalStateElement object
     * @return                                  LogicalStateElementConfig object
     * @throws DesignGenerationException        Error while generating LogicalStateElementConfig object
     */
    private LogicalStateElementConfig generateLogicalStateElementConfig(LogicalStateElement logicalStateElement)
            throws DesignGenerationException {
        LogicalStateElementConfig logicalStateElementConfig = new LogicalStateElementConfig();
        logicalStateElementConfig
                .setStreamStateElement1(generateStreamStateElementConfig(logicalStateElement.getStreamStateElement1()));
        logicalStateElementConfig.setType(logicalStateElement.getType().toString().toLowerCase());
        logicalStateElementConfig
                .setStreamStateElement2(generateStreamStateElementConfig(logicalStateElement.getStreamStateElement2()));
        logicalStateElementConfig.setWithin(generateNullableElementDefinition(logicalStateElement.getWithin()));

        return logicalStateElementConfig;
    }

    /**
     * Generates config for the given Siddhi EveryStateElement object
     * @param everyStateElement                 Siddhi EveryStateElement object
     * @return                                  EveryStateElementConfig object
     * @throws DesignGenerationException        Error while generating EveryStateElementConfig object
     */
    private EveryStateElementConfig generateEveryStateElementConfig(EveryStateElement everyStateElement)
            throws DesignGenerationException {
        EveryStateElementConfig everyStateElementConfig = new EveryStateElementConfig();
        everyStateElementConfig.setStateElement(generateStateElementConfig(everyStateElement.getStateElement()));
        everyStateElementConfig.setWithin(generateNullableElementDefinition(everyStateElement.getWithin()));

        return everyStateElementConfig;
    }

    /**
     * Generates config for the given Siddhi NextStateElement object
     * @param nextStateElement                  Siddhi NextStateElement object
     * @return                                  NextStateElementConfig object
     * @throws DesignGenerationException        Error while generating NextStateElementConfig object
     */
    private NextStateElementConfig generateNextStateElementConfig(NextStateElement nextStateElement)
            throws DesignGenerationException {
        NextStateElementConfig nextStateElementConfig = new NextStateElementConfig();
        nextStateElementConfig.setStateElement(generateStateElementConfig(nextStateElement.getStateElement()));
        nextStateElementConfig.setNextStateElement(generateStateElementConfig(nextStateElement.getNextStateElement()));
        nextStateElementConfig.setWithin(generateNullableElementDefinition(nextStateElement.getWithin()));

        return nextStateElementConfig;
    }

    /**
     * Generates config for the given Siddhi AbsentStreamStateElement object
     * @param absentStreamStateElement          Siddhi AbsentStreamStateElement object
     * @return                                  AbsentStreamStateElementConfig object
     * @throws DesignGenerationException        Error while generating AbsentStreamStateElementConfig object
     */
    private AbsentStreamStateElementConfig generateAbsentStreamStateElementConfig(
            AbsentStreamStateElement absentStreamStateElement) throws DesignGenerationException {
        BasicSingleInputStream basicSingleInputStream = absentStreamStateElement.getBasicSingleInputStream();
        AbsentStreamStateElementConfig absentStreamStateElementConfig = new AbsentStreamStateElementConfig();
        absentStreamStateElementConfig
                .setStreamReference(absentStreamStateElement.getBasicSingleInputStream().getStreamReferenceId());
        absentStreamStateElementConfig.setStreamName(basicSingleInputStream.getStreamId());
        absentStreamStateElementConfig
                .setStreamHandlerList(
                        new StreamHandlerConfigGenerator(siddhiAppString)
                                .generateStreamHandlerConfigList(basicSingleInputStream.getStreamHandlers()));
        absentStreamStateElementConfig.setWithin(
                generateNullableElementDefinition(absentStreamStateElement.getWithin()));
        absentStreamStateElementConfig
                .setWaitingTime(generateNullableElementDefinition(absentStreamStateElement.getWaitingTime()));

        addToAvailableStreamReferences(absentStreamStateElement.getBasicSingleInputStream().getStreamReferenceId());

        return absentStreamStateElementConfig;
    }

    /**
     * Represents a State Element component
     */
    private class ElementComponent {
        List<PatternSequenceConditionConfig> conditions;
        String logicComponent;

        private ElementComponent(List<PatternSequenceConditionConfig> conditions, String logicComponent) {
            this.conditions = conditions;
            this.logicComponent = logicComponent;
        }
    }
}
