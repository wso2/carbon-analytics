/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.stream.template.deployer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.event.execution.manager.core.DeployableTemplate;
import org.wso2.carbon.event.execution.manager.core.TemplateDeployer;
import org.wso2.carbon.event.execution.manager.core.TemplateDeploymentException;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.event.stream.core.exception.StreamDefinitionAlreadyDefinedException;
import org.wso2.carbon.event.stream.template.deployer.internal.EventStreamTemplateDeployerValueHolder;

public class EventStreamTemplateDeployer implements TemplateDeployer {

    private static final Log log = LogFactory.getLog(EventStreamTemplateDeployer.class);

    @Override
    public String getType() {
        return "eventstream";
    }


    @Override
    public void deployArtifact(DeployableTemplate template) throws TemplateDeploymentException {
        String stream = null;
        StreamDefinition streamDefinition = null;
        try {
            if (template == null) {
                throw new TemplateDeploymentException("No artifact received to be deployed.");
            }
            stream = template.getArtifact();
            streamDefinition = EventDefinitionConverterUtils.convertFromJson(stream);
            EventStreamTemplateDeployerValueHolder.getEventStreamService().addEventStreamDefinition(streamDefinition);
        } catch (MalformedStreamDefinitionException e) {
            throw new TemplateDeploymentException("Stream definition given in the template is not in valid format. Stream definition: " + stream, e);
        } catch (EventStreamConfigurationException e) {
            throw new TemplateDeploymentException("Exception occurred when configuring stream " + streamDefinition.getName(), e);
        } catch (StreamDefinitionAlreadyDefinedException e) {
            throw new TemplateDeploymentException("Same template stream name " + streamDefinition.getName()
                                                  + " has been defined for another definition ", e);
        }
    }


    @Override
    public void deployIfNotDoneAlready(DeployableTemplate template)
            throws TemplateDeploymentException {
        String stream = null;
        StreamDefinition streamDefinition = null;
        try {
            if (template == null) {
                throw new TemplateDeploymentException("No artifact received to be deployed.");
            }

            stream = template.getArtifact();
            streamDefinition = EventDefinitionConverterUtils.convertFromJson(stream);

            if (EventStreamTemplateDeployerValueHolder.getEventStreamService().
                    getStreamDefinition(streamDefinition.getStreamId()) == null) {
                deployArtifact(template);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Common Artifact: EventStream with ID " + streamDefinition.getStreamId() + " of Domain " + template.getConfiguration().getDomain()
                              + " was not deployed as it is already being deployed.");
                }
            }
        } catch (MalformedStreamDefinitionException e) {
            throw new TemplateDeploymentException("Stream definition given in the template is not in valid format. Stream definition: " + stream, e);
        } catch (EventStreamConfigurationException e) {
            throw new TemplateDeploymentException("Failed to get stream definition for StreamID: " + streamDefinition.getStreamId() + ", hence deployment failed.", e);
        }
    }


    @Override
    public void undeployArtifact(String artifactId) throws TemplateDeploymentException {
        //todo: Currently, this will do nothing because no mechanism has being implemented for keeping track of streams used by multiple templates.
    }

}
