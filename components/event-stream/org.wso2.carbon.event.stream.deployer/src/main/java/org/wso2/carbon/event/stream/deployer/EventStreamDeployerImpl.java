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
package org.wso2.carbon.event.stream.deployer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.event.stream.deployer.internal.EventStreamDeployerValueHolder;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.event.stream.core.exception.StreamDefinitionAlreadyDefinedException;

public class EventStreamDeployerImpl implements EventStreamDeployer {

    private static final Log log = LogFactory.getLog(EventStreamDeployerImpl.class);

    @Override
    public boolean deployEventStream(String eventStream) {
        StreamDefinition streamDefinition = null;
        if (eventStream != null) {
                try {
                    streamDefinition = EventDefinitionConverterUtils.convertFromJson(eventStream);
                    EventStreamDeployerValueHolder.getEventStreamService().addEventStreamDefinition(streamDefinition);
                    return true;
                } catch (MalformedStreamDefinitionException e) {
                    log.error("Stream definition is incorrect " + eventStream, e);
                } catch (EventStreamConfigurationException e) {
                    log.error("Exception occurred when configuring stream " + streamDefinition.getName(), e);
                } catch (StreamDefinitionAlreadyDefinedException e) {
                    log.error("Same stream name " + streamDefinition.getName()
                            + " has been defined for another definition ", e);
                    throw e;
                }
        }else{
            log.error("Stream definition cant be null");
        }
        return  false;
    }

    @Override
    public boolean undeployEventStream(String eventStream) {
        StreamDefinition streamDefinition = null;
        if (eventStream != null) {
            try {
                streamDefinition = EventDefinitionConverterUtils.convertFromJson(eventStream);
                EventStreamDeployerValueHolder.getEventStreamService()
                        .removeEventStreamDefinition(streamDefinition.getName(),streamDefinition.getVersion());
                return true;
            } catch (MalformedStreamDefinitionException e) {
                log.error("Stream definition is incorrect " + eventStream, e);
            } catch (EventStreamConfigurationException e) {
                log.error("Exception occurred when configuring stream " + streamDefinition.getName(), e);
            }
        }else{
            log.error("Stream definition cant be null");
        }
        return  true;
    }

    @Override
    public String getType() {
        return "eventStream";
    }
}
