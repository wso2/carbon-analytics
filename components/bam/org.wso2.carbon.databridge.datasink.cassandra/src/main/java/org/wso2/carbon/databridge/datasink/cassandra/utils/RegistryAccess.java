/**
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.databridge.datasink.cassandra.utils;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.IndexDefinition;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.utils.IndexDefinitionConverterUtils;
import org.wso2.carbon.databridge.streamdefn.registry.util.RegistryStreamDefinitionStoreUtil;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

public class RegistryAccess {

    private static Log log = LogFactory.getLog(RegistryAccess.class);

    // TODO: We need to move this method to some common utils class
    // At the moment BAM toolbox deployer also use this method and due to dep sync changes
    // this method is call before registry object get initialized. Because of this this method change
    // to send registry object as a parameter.
    public static void saveIndexDefinition(StreamDefinition streamDefinition,
                                           IndexDefinition indexDefinition, UserRegistry registry) {
        if (indexDefinition != null && registry != null) {
            String indexDefnStr = IndexDefinitionConverterUtils.
                    getIndexDefinitionString(indexDefinition);
            if (indexDefnStr != null && !indexDefnStr.trim().isEmpty()) {
                try {
                    Resource resource = registry.newResource();

                    resource.setMediaType("application/vnd.wso2-bam-index-defn+xml");
                    resource.setContent(indexDefnStr);
                    registry.put(RegistryStreamDefinitionStoreUtil.getStreamIndexDefinitionPath(
                            streamDefinition.getName(), streamDefinition.getVersion()), resource);
                } catch (RegistryException e) {
                    log.error("Error while adding index properties to Stream Definition properties " +
                              streamDefinition.getName() + ":" + streamDefinition.getVersion(), e);
                }
            }
        }
    }
}
