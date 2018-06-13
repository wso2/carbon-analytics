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

package org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.input.types.patternsequencesupporters;

import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.streamhandler.StreamHandlerConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import org.wso2.siddhi.query.api.SiddhiElement;
import org.wso2.siddhi.query.api.execution.query.input.state.*;
import org.wso2.siddhi.query.api.execution.query.input.stream.BasicSingleInputStream;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates PatternSequenceConfig tree, along with event references that are already present
 */
public class PatternSequenceConfigTreeInfoGenerator {
    private String siddhiAppString;
    private List<String> availableEventReferences = new ArrayList<>();

    public PatternSequenceConfigTreeInfoGenerator(String siddhiAppString) {
        this.siddhiAppString = siddhiAppString;
    }

    /**
     * Generates PatternSequenceConfigTreeInfo object, with the given Siddhi StateElement
     * @param stateElement                      Siddhi StateElement
     * @return                                  PatternSequenceConfigTreeInfo object
     * @throws DesignGenerationException        Error while generating StateElementConfig tree
     */
    public PatternSequenceConfigTreeInfo generatePatternSequenceConfigTreeInfo(StateElement stateElement)
            throws DesignGenerationException {
        StateElementConfig patternSequenceConfigTree = generateStateElementConfigTree(stateElement);
        return new PatternSequenceConfigTreeInfo(patternSequenceConfigTree, availableEventReferences);
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
     * Creates a StateElementConfig object from the given Siddhi StateElement object
     * @param stateElement                      Siddhi StateElement object
     * @return                                  StateElementConfig object
     * @throws DesignGenerationException        Error while generating StateElementConfig object
     */
    private StateElementConfig generateStateElementConfigTree(StateElement stateElement)
            throws DesignGenerationException {
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
        everyStateElementConfig.setStateElement(generateStateElementConfigTree(everyStateElement.getStateElement()));
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
        nextStateElementConfig.setStateElement(generateStateElementConfigTree(nextStateElement.getStateElement()));
        nextStateElementConfig.setNextStateElement(generateStateElementConfigTree(nextStateElement.getNextStateElement()));
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
}
