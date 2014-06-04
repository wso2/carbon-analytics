package org.wso2.carbon.bam.toolbox.deployer.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.toolbox.deployer.ServiceHolder;
import org.wso2.carbon.bam.toolbox.deployer.exception.BAMToolboxDeploymentException;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.IndexDefinition;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.utils.EventDefinitionConverterUtils;
import org.wso2.carbon.databridge.datasink.cassandra.utils.RegistryAccess;
import org.wso2.carbon.event.stream.manager.core.EventStreamService;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class DataPublisher {
    private static DataPublisher instance;
    private static EventStreamService eventStreamService;

    private static Log log = LogFactory.getLog(DataPublisher.class);

    private DataPublisher() {
        eventStreamService = ServiceHolder.getEventStreamService();
        if (null == eventStreamService) {
            log.warn("Event Stream service is not available...");
        }
    }

    public static DataPublisher getInstance() {
        if (null == instance) {
            instance = new DataPublisher();
        }
        return instance;
    }

    public void createEventDefn(String defn, String indexDefn, int tenantId)
            throws BAMToolboxDeploymentException {
        try {

            StreamDefinition streamDefinition = EventDefinitionConverterUtils.convertFromJson(defn);
            eventStreamService.addEventStreamDefinition(streamDefinition, tenantId);

            if ((indexDefn != null) && (!indexDefn.isEmpty())) {
                IndexDefinition indexDefinition = new IndexDefinition();
                indexDefinition.setIndexData(indexDefn, streamDefinition);
                // Adding Timestamp as default index.
                if (indexDefinition.getCustomIndexData() != null) {
                    indexDefinition.getCustomIndexData().add(new Attribute("Timestamp", AttributeType.LONG));
                }

                RegistryAccess.saveIndexDefinition(streamDefinition, indexDefinition, ServiceHolder.getRegistryService().
                        getGovernanceSystemRegistry(tenantId));
            }

        } catch (Exception e) {
            log.error("Error while creating event stream :" + e.getMessage());
            throw new BAMToolboxDeploymentException(e.getMessage(), e);
        }
    }
}
