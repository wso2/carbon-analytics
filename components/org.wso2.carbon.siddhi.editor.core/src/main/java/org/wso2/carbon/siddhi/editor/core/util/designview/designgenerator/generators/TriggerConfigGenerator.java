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

package org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.TriggerConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.ConfigBuildingUtilities;
import org.wso2.siddhi.query.api.annotation.Annotation;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.definition.TriggerDefinition;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Generator to create config for a Siddhi Trigger
 */
public class TriggerConfigGenerator {
    private static final String EVERY_SPLIT_KEYWORD = " every ";

    private String siddhiAppString;
    private Map<String, StreamDefinition> streamDefinitions;

    public TriggerConfigGenerator(String siddhiAppString, Map<String, StreamDefinition> streamDefinitions) {
        this.siddhiAppString = siddhiAppString;
        this.streamDefinitions = streamDefinitions;
    }

    /**
     * Generates TriggerConfig for the given Siddhi TriggerDefinition
     * @param triggerDefinition                 Siddhi TriggerDefinition object
     * @return                                  TriggerConfig object
     * @throws DesignGenerationException        Error when getting the definition of the trigger definition
     */
    public TriggerConfig generateTriggerConfig(TriggerDefinition triggerDefinition) throws DesignGenerationException {
        String at = "";
        if (triggerDefinition.getAtEvery() != null) {
            at = "every " + ConfigBuildingUtilities.getDefinition(triggerDefinition, siddhiAppString)
                    .split(EVERY_SPLIT_KEYWORD)[1];
        } else if (triggerDefinition.getAt() != null) {
            at = triggerDefinition.getAt();
        }

        return new TriggerConfig(
                triggerDefinition.getId(),
                triggerDefinition.getId(),
                at,
                new AnnotationConfigGenerator()
                        .generateAnnotationConfigList(
                                getTriggerStream(triggerDefinition.getId()).getAnnotations()));
    }

    /**
     * Gets the stream, which is defined for the Trigger with the given name
     * @param triggerName                       Name of the Trigger
     * @return                                  Stream Definition object
     * @throws DesignGenerationException        No stream is found with the Trigger's name
     */
    private StreamDefinition getTriggerStream(String triggerName) throws DesignGenerationException {
        for (Map.Entry<String, StreamDefinition> streamDefinitionEntry : streamDefinitions.entrySet()) {
            if (streamDefinitionEntry.getKey().equals(triggerName)) {
                return streamDefinitionEntry.getValue();
            }
        }
        throw new DesignGenerationException("Unable to find stream for trigger '" + triggerName + "'");
    }
}
