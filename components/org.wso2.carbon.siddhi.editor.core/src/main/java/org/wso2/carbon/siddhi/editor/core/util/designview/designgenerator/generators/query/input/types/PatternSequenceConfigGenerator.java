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
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;
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
        return generatePatternSequenceConfig(inputStream, QueryInputType.PATTERN);
    }

    /**
     * Generates config for a Siddhi Pattern/Sequence Query Input,
     * from the given Siddhi InputStream object and QueryInputType
     * @param inputStream                       Siddhi InputStream object
     * @param queryInputType                    QueryInputType
     * @return                                  PatternSequenceConfig object
     * @throws DesignGenerationException        Error while generating Pattern/Sequence Query Input
     */
    private PatternSequenceConfig generatePatternSequenceConfig(InputStream inputStream, QueryInputType queryInputType)
            throws DesignGenerationException {
        String delimiter;
        if (queryInputType == QueryInputType.PATTERN) {
            delimiter = PATTERN_DELIMITER;
        } else if (queryInputType == QueryInputType.SEQUENCE) {
            delimiter = SEQUENCE_DELIMITER;
        } else {
            throw new DesignGenerationException("Invalid QueryInputType for generating PatternSequenceConfig");
        }
        PatternSequenceConfigTreeInfo patternSequenceConfigTreeInfo =
                new PatternSequenceConfigTreeInfoGenerator(siddhiAppString)
                        .generatePatternSequenceConfigTreeInfo(((StateInputStream) inputStream).getStateElement());
        StateElementConfig patternSequenceConfigTree = patternSequenceConfigTreeInfo.getPatternSequenceConfigTree();
        availableEventReferences = patternSequenceConfigTreeInfo.getAvailableEventReferences();
        traverseInOrder(patternSequenceConfigTree);
        return new PatternSequenceConfig(
                queryInputType.toString(),
                conditionList,
                String.join(delimiter, logicComponentList));
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
        availableEventReferences.add(eventReference);
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
            return generateCountStateElementComponent((CountStateElementConfig) element);
        } else if (element instanceof EveryStateElementConfig) {
            return generateEveryStateElementComponent((EveryStateElementConfig) element);
        } else if (element instanceof LogicalStateElementConfig) {
            return generateLogicalStateElementComponent((LogicalStateElementConfig) element);
        } else if (element instanceof StreamStateElementConfig) {
            return generateStreamStateElementComponent((StreamStateElementConfig) element);
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
    private ElementComponent generateCountStateElementComponent(CountStateElementConfig element)
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
    private ElementComponent generateEveryStateElementComponent(EveryStateElementConfig element)
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
    private ElementComponent generateLogicalStateElementComponent(LogicalStateElementConfig element)
            throws DesignGenerationException {
        ElementComponent condition1Component = generateStreamStateElementComponent(element.getStreamStateElement1());
        ElementComponent condition2Component = generateStreamStateElementComponent(element.getStreamStateElement2());
        String logicComponentBuilder =
                condition1Component.logicComponent +
                WHITE_SPACE +
                element.getType().toLowerCase() +
                WHITE_SPACE +
                condition2Component.logicComponent +
                buildWithin(element.getWithin());
        // Add conditions
        List<PatternSequenceConditionConfig> conditions = new ArrayList<>();
        conditions.addAll(condition1Component.conditions);
        conditions.addAll(condition2Component.conditions);
        return new ElementComponent(conditions, logicComponentBuilder);
    }

    /**
     * Generates ElementComponent object for the given StreamStateElementConfig object
     * @param element                           StreamStateElementConfig object
     * @return                                  ElementComponent object
     * @throws DesignGenerationException        Error while generating ElementComponent object
     */
    private ElementComponent generateStreamStateElementComponent(StreamStateElementConfig element)
            throws DesignGenerationException {
        PatternSequenceConditionConfig conditionConfig = generateConditionConfig(element);
        StringBuilder logicComponentBuilder = new StringBuilder();
        if (element instanceof AbsentStreamStateElementConfig) {
            logicComponentBuilder.append(NOT);
            logicComponentBuilder.append(WHITE_SPACE);
        }
        logicComponentBuilder.append(conditionConfig.getConditionId());
        if (element instanceof AbsentStreamStateElementConfig &&
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
     * Represents a State Element component,
     * which consists of conditions and the logic component belonging to a State Element
     */
    private static class ElementComponent {
        List<PatternSequenceConditionConfig> conditions;
        String logicComponent;

        private ElementComponent(List<PatternSequenceConditionConfig> conditions, String logicComponent) {
            this.conditions = conditions;
            this.logicComponent = logicComponent;
        }
    }
}
