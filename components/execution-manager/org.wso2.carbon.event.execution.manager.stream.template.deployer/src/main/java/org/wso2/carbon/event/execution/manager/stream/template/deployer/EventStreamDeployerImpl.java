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
package org.wso2.carbon.event.execution.manager.stream.template.deployer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.event.execution.manager.core.DeployableTemplate;
import org.wso2.carbon.event.execution.manager.core.TemplateDeployer;
import org.wso2.carbon.event.execution.manager.core.TemplateDeploymentException;
import org.wso2.carbon.event.execution.manager.stream.template.deployer.internal.EventStreamDeployerValueHolder;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.event.stream.core.exception.StreamDefinitionAlreadyDefinedException;

public class EventStreamDeployerImpl implements TemplateDeployer {

    private static final Log log = LogFactory.getLog(EventStreamDeployerImpl.class);

    @Override
    public String getType() {
        return "eventStream";
    }

    @Override
    public void deployArtifact(DeployableTemplate template) throws TemplateDeploymentException {
        if (template.getStreams() != null) {
            for (String stream : template.getStreams()) {
               /* stream = "{\n" +
                        "  \"name\": \"inStreamTest\",\n" +
                        "  \"version\": \"1.0.0\",\n" +
                        "  \"nickName\": \"\",\n" +
                        "  \"description\": \"\",\n" +
                        "  \"metaData\": [\n" +
                        "    {\n" +
                        "      \"name\": \"temperature\",\n" +
                        "      \"type\": \"DOUBLE\"\n" +
                        "    },\n" +
                        "    {\n" +
                        "      \"name\": \"roomNumber\",\n" +
                        "      \"type\": \"INT\"\n" +
                        "    }\n" +
                        "  ]\n" +
                        "}";*/
                StreamDefinition streamDefinition = null;
                try {
                    streamDefinition = EventDefinitionConverterUtils.convertFromJson(stream);
                    EventStreamDeployerValueHolder.getEventStreamService().addEventStreamDefinition(streamDefinition);
                } catch (MalformedStreamDefinitionException e) {
                    log.error("Stream definition is incorrect in domain template " + stream, e);
                } catch (EventStreamConfigurationException e) {
                    log.error("Exception occurred when configuring stream " + streamDefinition.getName(), e);
                } catch (StreamDefinitionAlreadyDefinedException e) {
                    log.error("Same template stream name " + streamDefinition.getName()
                            + " has been defined for another definition ", e);
                    throw e;
                }
            }
        }
    }

    @Override
    public void undeployArtifact(String artifactId) throws TemplateDeploymentException {

    }
}
