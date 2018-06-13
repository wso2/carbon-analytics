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
import java.util.Arrays;
import java.util.List;

/**
 * Generates Pattern/Sequence Query Input Config with given Siddhi elements
 */
public class PatternSequenceConfigGenerator {
    private static final String PATTERN_DELIMITER = " -> ";
    private static final String SEQUENCE_DELIMITER = " , ";

    private static final String WHITE_SPACE = " ";
    private static final String COUNT_MIN = "<";
    private static final String COUNT_MAX = ">";
    private static final String COUNT_MIN_MAX_SEPARATOR = ":";
    private static final String WITHIN = "within";
    private static final String EVERY = "every";
    private static final String NOT = "not";
    private static final String FOR = "for";
    private static final String ONE_OR_MORE_POSTFIX_SYMBOL = "+";
    private static final String ZERO_OR_MORE_POSTFIX_SYMBOL = "*";
    private static final String ZERO_OR_ONE_POSTFIX_SYMBOL = "?";

    private String siddhiAppString;
    private QueryInputType mode;
    private List<PatternSequenceConditionConfig> conditionList = new ArrayList<>();
    private List<String> logicComponentList = new ArrayList<>();

    private List<String> availableEventReferences = new ArrayList<>();
    private int eventReferenceCounter = 0;

    public PatternSequenceConfigGenerator(String siddhiAppString, QueryInputType mode) {
        this.siddhiAppString = siddhiAppString;
        this.mode = mode;
    }

    /**
     * Generates config for a Siddhi Pattern/Sequence Query Input, from the given Siddhi InputStream object
     * @param inputStream                       Siddhi InputStream object
     * @return                                  PatternSequenceConfig object
     * @throws DesignGenerationException        Error while generating Pattern/Sequence Query Input
     */
    public PatternSequenceConfig generatePatternSequenceConfig(InputStream inputStream)
            throws DesignGenerationException {
        String delimiter = getDelimiter(mode);
        PatternSequenceConfigTreeInfo patternSequenceConfigTreeInfo =
                new PatternSequenceConfigTreeInfoGenerator(siddhiAppString)
                        .generatePatternSequenceConfigTreeInfo(((StateInputStream) inputStream).getStateElement());
        StateElementConfig patternSequenceConfigTree = patternSequenceConfigTreeInfo.getPatternSequenceConfigTree();
        availableEventReferences = patternSequenceConfigTreeInfo.getAvailableEventReferences();
        traverseInOrder(patternSequenceConfigTree);
        return new PatternSequenceConfig(
                mode.toString(),
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
     * Accepts the given stream reference when not null,
     * otherwise generates the next un-available stream reference
     * @param nullableStreamReference       Stream Reference String when available, or null
     * @return                              Accepted/Generated stream reference
     */
    private String acceptOrGenerateNextStreamReference(String nullableStreamReference) {
        if (nullableStreamReference != null) {
            return nullableStreamReference;
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
        logicComponentList.addAll(elementComponent.logicComponents);
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
        } else if (element instanceof NextStateElementConfig) {
            return generateNextStateElementComponent((NextStateElementConfig) element);
        }
        throw new IllegalArgumentException(
                "Failed to generate config for Pattern/Sequence element, since type is unknown");
    }

    /**
     * Returns the delimiter for the given mode
     * @param mode                          QueryInputType
     * @return                              Delimiter for the Pattern/Sequence query
     * @throws DesignGenerationException    QueryInputType if not one of Pattern and Sequence
     */
    private static String getDelimiter(QueryInputType mode) throws DesignGenerationException {
        if (mode == QueryInputType.PATTERN) {
            return PATTERN_DELIMITER;
        } else if (mode == QueryInputType.SEQUENCE) {
            return SEQUENCE_DELIMITER;
        }
        throw new DesignGenerationException("Invalid QueryInputType for generating PatternSequenceConfig");
    }

    /**
     * Returns the 'min:max' expression with the given parameters
     * @param min       Min value of a CountStateElement
     * @param max       Max value of a CountStateElement
     * @param mode      Whether the stateful query input is Pattern/Sequence
     * @return          MinMax expression
     */
    private static String buildMinMax(int min, int max, QueryInputType mode) {
        String acceptedMin = String.valueOf(min);
        String acceptedMax = String.valueOf(max);
        if (mode == QueryInputType.PATTERN) {
            if (min == -1) {
                acceptedMin = "";
            }
            if (max == -1) {
                acceptedMax = "";
            }
        } else if (mode == QueryInputType.SEQUENCE) {
            if (min == 1 && max == -1) {
                return ONE_OR_MORE_POSTFIX_SYMBOL;
            } else if (min == 0 && max == -1) {
                return ZERO_OR_MORE_POSTFIX_SYMBOL;
            } else if (min == 0 && max == 1) {
                return ZERO_OR_ONE_POSTFIX_SYMBOL;
            }
        }
        if (acceptedMin.equals(acceptedMax)) {
            return COUNT_MIN + acceptedMax + COUNT_MAX;
        }
        return COUNT_MIN + acceptedMin + COUNT_MIN_MAX_SEPARATOR + acceptedMax + COUNT_MAX;
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
        String logicComponent =
                conditionConfig.getConditionId() +
                buildMinMax(element.getMin(), element.getMax(), mode) +
                buildWithin(element.getWithin());
        return new ElementComponent(conditions, new ArrayList<>(Arrays.asList(logicComponent)));
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
        String logicComponent =
                EVERY +
                WHITE_SPACE +
                containedElement.getLogicComponentsString(mode) +
                buildWithin(element.getWithin());
        return new ElementComponent(conditions, new ArrayList<>(Arrays.asList(logicComponent)));
    }

    /**
     * Generates ElementComponent object for the given NextStateElementConfig object
     * @param element                           NextStateElementConfig object
     * @return                                  ElementComponent object
     * @throws DesignGenerationException        Error wihle generating ElementComponent object
     */
    private ElementComponent generateNextStateElementComponent(NextStateElementConfig element)
            throws DesignGenerationException {
        ElementComponent stateElement = generateElementComponent(element.getStateElement());
        ElementComponent nextStateElement = generateElementComponent(element.getNextStateElement());
        List<PatternSequenceConditionConfig> conditions = new ArrayList<>(stateElement.conditions);
        conditions.addAll(nextStateElement.conditions);
        List<String> logicComponents = new ArrayList<>(stateElement.logicComponents);
        logicComponents.addAll(nextStateElement.logicComponents);

        return new ElementComponent(conditions, logicComponents);
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
        String logicComponent =
                condition1Component.getLogicComponentsString(mode) +
                WHITE_SPACE +
                element.getType().toLowerCase() +
                WHITE_SPACE +
                condition2Component.getLogicComponentsString(mode) +
                buildWithin(element.getWithin());
        // Add conditions
        List<PatternSequenceConditionConfig> conditions = new ArrayList<>();
        conditions.addAll(condition1Component.conditions);
        conditions.addAll(condition2Component.conditions);
        return new ElementComponent(conditions, new ArrayList<>(Arrays.asList(logicComponent)));
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
        StringBuilder logicComponent = new StringBuilder();
        if (element instanceof AbsentStreamStateElementConfig) {
            logicComponent.append(NOT);
            logicComponent.append(WHITE_SPACE);
        }
        logicComponent.append(conditionConfig.getConditionId());
        if (element instanceof AbsentStreamStateElementConfig &&
                ((AbsentStreamStateElementConfig) element).getWaitingTime() != null) {
            logicComponent.append(WHITE_SPACE);
            logicComponent.append(FOR);
            logicComponent.append(WHITE_SPACE);
            logicComponent.append(((AbsentStreamStateElementConfig) element).getWaitingTime());
        }
        logicComponent.append(buildWithin(element.getWithin()));
        // Add condition
        List<PatternSequenceConditionConfig> conditions = new ArrayList<>();
        conditions.add(conditionConfig);
        return new ElementComponent(conditions, new ArrayList<>(Arrays.asList(logicComponent.toString())));
    }

    /**
     * Represents a State Element component,
     * which consists of conditions and logic components belonging to a State Element
     */
    private static class ElementComponent {
        List<PatternSequenceConditionConfig> conditions;
        List<String> logicComponents;

        private ElementComponent(List<PatternSequenceConditionConfig> conditions, List<String> logicComponents) {
            this.conditions = conditions;
            this.logicComponents = logicComponents;
        }

        private String getLogicComponentsString(QueryInputType mode) throws DesignGenerationException {
            return String.join(getDelimiter(mode), logicComponents);
        }
    }
}
