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

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.patternsequence.PatternSequenceConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.streamhandler.StreamHandlerConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.input.types.patternsequencesupporters.*;
import org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.streamhandler.StreamHandlerConfigGenerator;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import org.wso2.siddhi.query.api.SiddhiElement;
import org.wso2.siddhi.query.api.execution.query.input.handler.StreamHandler;
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
    private String siddhiAppString;

    public PatternSequenceConfigGenerator(String siddhiAppString) {
        this.siddhiAppString = siddhiAppString;
    }

    public PatternSequenceConfig generatePatternSequenceConfig(InputStream inputStream)
            throws DesignGenerationException {
        StateElementConfig stateElementConfig =
                generateStateElementConfig(((StateInputStream) inputStream).getStateElement());
        return null;
    }

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

    private String generateElementDefinition(SiddhiElement siddhiElement) throws DesignGenerationException {
        if (siddhiElement == null) {
            return "";
        }
        return ConfigBuildingUtilities.getDefinition(siddhiElement, siddhiAppString);
    }

    private StreamStateElementConfig generateStreamStateElementConfig(StreamStateElement streamStateElement)
            throws DesignGenerationException {
        if (streamStateElement instanceof AbsentStreamStateElement) {
            return generateAbsentStreamStateElementConfig((AbsentStreamStateElement) streamStateElement);
        }
        BasicSingleInputStream basicSingleInputStream = streamStateElement.getBasicSingleInputStream();
        StreamHandlerConfigGenerator streamHandlerConfigGenerator = new StreamHandlerConfigGenerator(siddhiAppString);
        List<StreamHandlerConfig> streamHandlerConfigList = new ArrayList<>();
        for (StreamHandler streamHandler : basicSingleInputStream.getStreamHandlers()) {
            streamHandlerConfigList.add(streamHandlerConfigGenerator.generateStreamHandlerConfig(streamHandler));
        }

        StreamStateElementConfig streamStateElementConfig = new StreamStateElementConfig();
        streamStateElementConfig
                .setStreamReference(streamStateElement.getBasicSingleInputStream().getStreamReferenceId());
        streamStateElementConfig.setStreamName(basicSingleInputStream.getStreamId());
        streamStateElementConfig.setStreamHandlerList(streamHandlerConfigList);
        streamStateElementConfig.setWithin(generateElementDefinition(streamStateElement.getWithin()));

        return streamStateElementConfig;
    }

    private CountStateElementConfig generateCountStateElementConfig(CountStateElement countStateElement)
            throws DesignGenerationException {
        CountStateElementConfig countStateElementConfig = new CountStateElementConfig();
        countStateElementConfig
                .setStreamStateElement(generateStreamStateElementConfig(countStateElement.getStreamStateElement()));
        countStateElementConfig.setWithin(generateElementDefinition(countStateElement.getWithin()));
        countStateElementConfig.setMin(countStateElement.getMinCount());
        countStateElementConfig.setMax(countStateElement.getMaxCount());

        return countStateElementConfig;
    }

    private LogicalStateElementConfig generateLogicalStateElementConfig(LogicalStateElement logicalStateElement)
            throws DesignGenerationException {
        LogicalStateElementConfig logicalStateElementConfig = new LogicalStateElementConfig();
        logicalStateElementConfig
                .setStreamStateElement1(generateStreamStateElementConfig(logicalStateElement.getStreamStateElement1()));
        logicalStateElementConfig.setType(logicalStateElement.getType().toString());
        logicalStateElementConfig
                .setStreamStateElement2(generateStreamStateElementConfig(logicalStateElement.getStreamStateElement2()));
        logicalStateElementConfig.setWithin(generateElementDefinition(logicalStateElement.getWithin()));

        return logicalStateElementConfig;
    }

    private EveryStateElementConfig generateEveryStateElementConfig(EveryStateElement everyStateElement)
            throws DesignGenerationException {
        EveryStateElementConfig everyStateElementConfig = new EveryStateElementConfig();
        everyStateElementConfig.setStateElement(generateStateElementConfig(everyStateElement.getStateElement()));
        everyStateElementConfig.setWithin(generateElementDefinition(everyStateElement.getWithin()));

        return everyStateElementConfig;
    }

    private NextStateElementConfig generateNextStateElementConfig(NextStateElement nextStateElement)
            throws DesignGenerationException {
        NextStateElementConfig nextStateElementConfig = new NextStateElementConfig();
        nextStateElementConfig.setStateElement(generateStateElementConfig(nextStateElement.getStateElement()));
        nextStateElementConfig.setNextStateElement(generateStateElementConfig(nextStateElement.getNextStateElement()));
        nextStateElementConfig.setWithin(generateElementDefinition(nextStateElement.getWithin()));

        return nextStateElementConfig;
    }

    private AbsentStreamStateElementConfig generateAbsentStreamStateElementConfig(
            AbsentStreamStateElement absentStreamStateElement) throws DesignGenerationException {
        BasicSingleInputStream basicSingleInputStream = absentStreamStateElement.getBasicSingleInputStream();
        StreamHandlerConfigGenerator streamHandlerConfigGenerator = new StreamHandlerConfigGenerator(siddhiAppString);
        List<StreamHandlerConfig> streamHandlerConfigList = new ArrayList<>();
        for (StreamHandler streamHandler : basicSingleInputStream.getStreamHandlers()) {
            streamHandlerConfigList.add(streamHandlerConfigGenerator.generateStreamHandlerConfig(streamHandler));
        }

        AbsentStreamStateElementConfig absentStreamStateElementConfig = new AbsentStreamStateElementConfig();
        absentStreamStateElementConfig
                .setStreamReference(absentStreamStateElement.getBasicSingleInputStream().getStreamReferenceId());
        absentStreamStateElementConfig.setStreamName(basicSingleInputStream.getStreamId());
        absentStreamStateElementConfig.setStreamHandlerList(streamHandlerConfigList);
        absentStreamStateElementConfig.setWithin(generateElementDefinition(absentStreamStateElement.getWithin()));
        absentStreamStateElementConfig
                .setWaitingTime(generateElementDefinition(absentStreamStateElement.getWaitingTime()));

        return absentStreamStateElementConfig;
    }
}
