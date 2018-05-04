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
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import org.wso2.siddhi.query.api.execution.query.input.handler.Filter;
import org.wso2.siddhi.query.api.execution.query.input.state.*;
import org.wso2.siddhi.query.api.execution.query.input.stream.BasicSingleInputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.input.stream.StateInputStream;
import org.wso2.siddhi.query.api.expression.constant.TimeConstant;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates PatternQueryConfig with given Siddhi elements
 */
public class PatternConfigGenerator {
    private String siddhiAppString;

    private List<PatternSequenceConditionConfig> conditions = new ArrayList<>();
    private List<String> existingEventReferences = new ArrayList<>();
    // Indexes of the 'conditions' list members, whose event references have not been given
    private List<Integer> eventReferenceAbsentConditionIndexes = new ArrayList<>();

    private List<String> logicComponents = new ArrayList<>();

    public PatternConfigGenerator(String siddhiAppString) {
        this.siddhiAppString = siddhiAppString;
    }

    /**
     * Generates a PatternQueryConfig object, from the QueryInputStream object
     * @param queryInputStream      Siddhi Query InputStream
     * @return                      PatternSequenceQueryConfig object
     */
    public PatternSequenceConfig getPatternQueryConfig(InputStream queryInputStream) {
        traversePatternElements(((StateInputStream) queryInputStream).getStateElement());
        String logic = replaceLogicEventReferences(String.join(" -> ", logicComponents));
        return new PatternSequenceConfig(
                QueryInputType.PATTERN.toString(),
                conditions,
                logic);
    }

    /**
     * Traverses through the given StateElement object. Adds relevant Configs, when Pattern Elements are met
     * @param stateElement      Siddhi StateElement object, consisting elements of the Pattern
     */
    private void traversePatternElements(StateElement stateElement) {
        if (stateElement instanceof NextStateElement) {
            traversePatternElements(((NextStateElement) stateElement).getStateElement());
            traversePatternElements(((NextStateElement) stateElement).getNextStateElement());
        } else if (stateElement instanceof EveryStateElement){
            addPatternElementConfig(generateEveryPatternElementConfig((EveryStateElement) stateElement));
        } else {
            addPatternElementConfig(generatePatternEventConfig(stateElement));
        }
    }

    /**
     * Adds all the ConditionConfigs, and the Logic Component of the given PatternElementConfig object,
     * to the relevant lists
     * @param patternElementConfig        PatternElementConfig object, representing an element in the Pattern
     */
    private void addPatternElementConfig(PatternElementConfig patternElementConfig) {
        conditions.addAll(patternElementConfig.conditionConfigs);
        logicComponents.add(patternElementConfig.logicComponent);
    }

    /**
     * Gets type of the Pattern Element, with the given StateElement object
     * @param stateElement      Siddhi StateElement object, which contains data related to an element in the Pattern
     * @return                  Type of the Pattern Element
     */
    private ElementType getElementType(StateElement stateElement) {
        if (stateElement instanceof CountStateElement) {
            return ElementType.COUNTING;
        } else if (stateElement instanceof LogicalStateElement) {
            LogicalStateElement logicalStateElement = (LogicalStateElement) stateElement;
            String logicalStateElementType = logicalStateElement.getType().name();

            if ((logicalStateElement.getStreamStateElement1() instanceof AbsentStreamStateElement) &&
                    (logicalStateElementType.equalsIgnoreCase(AndOrElementEventType.AND.toString()))) {
                return ElementType.NOT_AND;
            } else if (logicalStateElementType.equalsIgnoreCase(AndOrElementEventType.AND.toString()) ||
                    logicalStateElementType.equalsIgnoreCase(AndOrElementEventType.OR.toString())) {
                return ElementType.AND_OR;
            }
            throw new IllegalArgumentException("Event config type for StateElement is unknown");
        } else if (stateElement instanceof AbsentStreamStateElement) {
            return ElementType.NOT_FOR;
        }
        throw new IllegalArgumentException("Event config type for StateElement is unknown");
    }

    /**
     * Generates Config object for a Pattern's element, from the given Siddhi StateElement object
     * @param stateElement          Siddhi StateElement object, that holds data about a Pattern query's element
     * @return                      Config object of a Pattern Query's element
     */
    private PatternElementConfig generatePatternEventConfig(StateElement stateElement) {
        switch (getElementType(stateElement)) {
            case COUNTING:
                return generateCountPatternElementConfig((CountStateElement) stateElement);
            case NOT_AND:
                return generateNotAndPatternElementConfig((LogicalStateElement) stateElement);
            case AND_OR:
                return generateAndOrPatternElementConfig((LogicalStateElement) stateElement);
            case NOT_FOR:
                return generateNotForPatternElementConfig((AbsentStreamStateElement) stateElement);
            default:
                throw new IllegalArgumentException("Unknown type: " + getElementType(stateElement) +
                        " for generating Event Config");
        }
    }

    /**
     * Generates PatternElementConfig object, for a Pattern element that has the 'every' keyword
     * @param everyStateElement     Siddhi EveryStateElement object,
     *                              which contains a Pattern element with the 'every' keyword
     * @return                      PatternElementConfig object
     */
    private PatternElementConfig generateEveryPatternElementConfig(EveryStateElement everyStateElement) {
        PatternElementConfig patternElementConfig = generatePatternEventConfig(everyStateElement.getStateElement());
        patternElementConfig.logicComponent =
                String.join(" ", "every", patternElementConfig.logicComponent, generateWithinString(everyStateElement.getWithin()));
        return patternElementConfig;
    }

    /**
     * Generates PatternElementConfig object, for a Pattern element, which is a Count element
     * @param countStateElement     Siddhi CountStateElement object
     * @return                      PatternElementConfig object
     */
    private PatternElementConfig generateCountPatternElementConfig(CountStateElement countStateElement) {
        ElementEventConfig elementEventConfig =
                generateElementEventConfig(countStateElement.getStreamStateElement());

        PatternElementConfig patternElementConfig = new PatternElementConfig();
        patternElementConfig.conditionConfigs.add(
                new PatternSequenceConditionConfig(
                        elementEventConfig.reference,
                        elementEventConfig.streamName,
                        elementEventConfig.filter));

        patternElementConfig.logicComponent =
                String.join(
                        " ",
                        elementEventConfig.reference,
                        "<" + countStateElement.getMinCount() + ":" + countStateElement.getMaxCount() + ">",
                        generateWithinString(countStateElement.getWithin()));

        return patternElementConfig;
    }

    /**
     * Generates PatternElementConfig object, for a Pattern element, which is an 'and or' element
     * @param logicalStateElement       Siddhi LogicalStateElement object
     * @return                          PatternElementConfig object
     */
    private PatternElementConfig generateAndOrPatternElementConfig(LogicalStateElement logicalStateElement) {
        PatternElementConfig patternElementConfig = new PatternElementConfig();

        ElementEventConfig firstElementEventConfig =
                generateElementEventConfig(logicalStateElement.getStreamStateElement1());
        patternElementConfig.conditionConfigs.add(
                new PatternSequenceConditionConfig(
                        firstElementEventConfig.reference,
                        firstElementEventConfig.streamName,
                        firstElementEventConfig.filter));

        ElementEventConfig secondElementEventConfig =
                generateElementEventConfig(logicalStateElement.getStreamStateElement2());
        patternElementConfig.conditionConfigs.add(
                new PatternSequenceConditionConfig(
                        secondElementEventConfig.reference,
                        secondElementEventConfig.streamName,
                        secondElementEventConfig.filter));

        patternElementConfig.logicComponent =
                String.join(
                        " ",
                        firstElementEventConfig.reference,
                        logicalStateElement.getType().name().toLowerCase(),
                        secondElementEventConfig.reference,
                        generateWithinString(logicalStateElement.getWithin()));

        return patternElementConfig;
    }

    /**
     * Generates PatternElementConfig object, for a Pattern element, which is an 'not for' element
     * @param absentStreamStateElement      Siddhi AbsentStreamStateElement object
     * @return                              PatternElementConfig object
     */
    private PatternElementConfig generateNotForPatternElementConfig(AbsentStreamStateElement absentStreamStateElement) {
        ElementEventConfig elementEventConfig = generateElementEventConfig(absentStreamStateElement);

        PatternElementConfig patternElementConfig = new PatternElementConfig();
        patternElementConfig.conditionConfigs.add(
                new PatternSequenceConditionConfig(
                        elementEventConfig.reference,
                        elementEventConfig.streamName,
                        elementEventConfig.filter));

        patternElementConfig.logicComponent =
                String.join(
                        " ",
                        "not",
                        elementEventConfig.reference,
                        "for",
                        ConfigBuildingUtilities.getDefinition(
                                absentStreamStateElement.getWaitingTime(), siddhiAppString));
        return patternElementConfig;
    }

    /**
     * Generates PatternElementConfig object, for a Pattern element, which is an 'not and' element
     * @param logicalStateElement       Siddhi LogicalStateElement object
     * @return                          PatternElementConfig object
     */
    private PatternElementConfig generateNotAndPatternElementConfig(LogicalStateElement logicalStateElement) {
        PatternElementConfig patternElementConfig = new PatternElementConfig();

        ElementEventConfig firstElementEventConfig =
                generateElementEventConfig(logicalStateElement.getStreamStateElement1());
        patternElementConfig.conditionConfigs.add(
                new PatternSequenceConditionConfig(
                        firstElementEventConfig.reference,
                        firstElementEventConfig.streamName,
                        firstElementEventConfig.filter));

        ElementEventConfig secondElementEventConfig =
                generateElementEventConfig(logicalStateElement.getStreamStateElement2());
        patternElementConfig.conditionConfigs.add(
                new PatternSequenceConditionConfig(
                        secondElementEventConfig.reference,
                        secondElementEventConfig.streamName,
                        secondElementEventConfig.filter));

        patternElementConfig.logicComponent =
                String.join(
                        " ",
                        "not",
                        firstElementEventConfig.reference,
                        "and",
                        secondElementEventConfig.reference,
                        generateWithinString(logicalStateElement.getWithin()));

        return patternElementConfig;
    }

    /**
     * Generates Config for an event of a Pattern element, from the given Siddhi StreamStateElement object
     * @param streamStateElement        Siddhi StreamStateElement object,
     *                                  which has information about an event of a Pattern element
     * @return                          ElementEventConfig object
     */
    private ElementEventConfig generateElementEventConfig(StreamStateElement streamStateElement) {
        BasicSingleInputStream basicSingleInputStream = streamStateElement.getBasicSingleInputStream();

        ElementEventConfig elementEventConfig = new ElementEventConfig();
        elementEventConfig.reference =
                acceptOrPlaceHoldEventReference(basicSingleInputStream.getStreamReferenceId());
        elementEventConfig.streamName = basicSingleInputStream.getStreamId();

        // Set Filter
        if (basicSingleInputStream.getStreamHandlers().size() == 1) {
            if (basicSingleInputStream.getStreamHandlers().get(0) instanceof Filter) {
                String filterExpression = ConfigBuildingUtilities
                        .getDefinition(basicSingleInputStream.getStreamHandlers().get(0), siddhiAppString);
                elementEventConfig.filter =
                        filterExpression.substring(1, filterExpression.length() - 1).trim();
            } else {
                throw new IllegalArgumentException("Unknown type of stream handler in BasicSingleInputStream");
            }
        } else if (!basicSingleInputStream.getStreamHandlers().isEmpty()) {
            throw new IllegalArgumentException(
                    "Failed to convert more than one stream handlers within a BasicSingleInputStream");
        }

        return elementEventConfig;
    }

    /**
     * Accepts and returns the given event reference when not null,
     * otherwise returns a placeholder, which will be later replaced by a generated event reference
     * @param eventReference        Reference provided for the event, or null when not provided
     * @return                      Provided reference, or a placeholder when not provided
     */
    private String acceptOrPlaceHoldEventReference(String eventReference) {
        if (eventReference != null) {
            // Event reference is present
            existingEventReferences.add(eventReference);
            return eventReference;
        }
        // Event reference is not present. Add and keep the index, for generating Event reference later
        eventReferenceAbsentConditionIndexes.add(conditions.size());
        // Return placeholder with next placeholder number, for replacing later
        return "${" + eventReferenceAbsentConditionIndexes.size() + "}";
    }

    /**
     * Replaces placeholders in the given logic, with generated event references
     * @param logicWithPlaceHolders     Logic of the Pattern, that has placeholders for event references
     * @return                          Logic, replaced with generated event references
     */
    private String replaceLogicEventReferences(String logicWithPlaceHolders) {
        Pattern pattern = Pattern.compile("\\$\\{([^$\\s]+)}");
        Matcher matcher = pattern.matcher(logicWithPlaceHolders);

        StringBuffer replacedLogic = new StringBuffer();
        int placeholderCounter = 0;
        while (matcher.find()) {
            String eventReference = generateEventReference(Integer.parseInt(matcher.group(1)));
            // Update relevant condition, for each index in eventReferenceAbsentConditionIndexes
            conditions.get(eventReferenceAbsentConditionIndexes.get(placeholderCounter++)).setId(eventReference);
            matcher.appendReplacement(replacedLogic, eventReference);
        }
        matcher.appendTail(replacedLogic);
        return replacedLogic.toString();
    }

    /**
     * Generates event reference with the given id of the placeholder
     * @param conditionIndex        Index of the condition, whose event reference is absent
     * @return                      Generated event reference
     */
    private String generateEventReference(int conditionIndex) {
        String eventReference;
        do {
            eventReference = "e" + conditionIndex;
            conditionIndex++;
        } while (existingEventReferences.contains(eventReference));
        existingEventReferences.add(eventReference);
        return eventReference;
    }

    /**
     * Generates the 'within' definition for the given within TimeConstant
     * @param within        Siddhi TimeConstant object
     * @return              Within definition
     */
    private String generateWithinString(TimeConstant within) {
        if (within != null) {
            return "within " + ConfigBuildingUtilities.getDefinition(within, siddhiAppString);
        }
        return "";
    }

    /**
     * Contains ConditionConfig(s), and the relevant Logic Component, for a Pattern Element
     * There will be two ConditionConfigs, if the element is 'NOT_AND', 'AND_OR' and 'NOT_FOR'
     */
    private class PatternElementConfig {
        private List<PatternSequenceConditionConfig> conditionConfigs;
        private String logicComponent;

        private PatternElementConfig() {
            conditionConfigs = new ArrayList<>();
        }
    }

    /**
     * Represents an Event, in a Pattern element
     */
    private class ElementEventConfig {
        private String reference;
        private String streamName;
        private String filter;

        /**
         * Constructs with default values
         */
        private ElementEventConfig() {
            streamName = "";
            reference = "";
            filter = "";
        }
    }

    /**
     * Type of a Pattern Element
     */
    private enum ElementType {
        COUNTING,
        NOT_AND,
        AND_OR,
        NOT_FOR
    }

    /**
     * Type of an event in an 'and or' Pattern element
     */
    private enum AndOrElementEventType {
        AND,
        OR
    }
}
