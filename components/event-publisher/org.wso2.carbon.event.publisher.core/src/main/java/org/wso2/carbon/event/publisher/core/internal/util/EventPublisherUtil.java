/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.event.publisher.core.internal.util;

import org.apache.axis2.engine.AxisConfiguration;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConfiguration;
import org.wso2.carbon.event.publisher.core.config.EventPublisherConstants;
import org.wso2.carbon.event.publisher.core.exception.EventPublisherConfigurationException;
import org.wso2.carbon.event.publisher.core.internal.ds.EventPublisherServiceValueHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.io.File;

public class EventPublisherUtil {

    public static String generateFilePath(String eventPublisherName, String repositoryPath) throws EventPublisherConfigurationException {
        File repoDir = new File(repositoryPath);
        if (!repoDir.exists()) {
            synchronized (repositoryPath.intern()) {
                if (!repoDir.exists()) {
                    if (!repoDir.mkdir()) {
                        throw new EventPublisherConfigurationException("Cannot create directory to add tenant specific event publisher :" + eventPublisherName);
                    }
                }
            }
        }
        String path = repoDir.getAbsolutePath() + File.separator + EventPublisherConstants.EF_CONFIG_DIRECTORY;
        File subDir = new File(path);
        if (!subDir.exists()) {
            synchronized (path.intern()) {
                if (!subDir.exists()) {
                    if (!subDir.mkdir()) {
                        throw new EventPublisherConfigurationException("Cannot create directory " + EventPublisherConstants.EF_CONFIG_DIRECTORY + " to add tenant specific event publisher :" + eventPublisherName);
                    }
                }
            }
        }
        return subDir.getAbsolutePath() + File.separator + eventPublisherName + EventPublisherConstants.EF_CONFIG_FILE_EXTENSION_WITH_DOT;
    }

    public static String getImportedStreamIdFrom(
            EventPublisherConfiguration eventPublisherConfiguration) {
        String streamId = null;
        if (eventPublisherConfiguration != null && eventPublisherConfiguration.getFromStreamName() != null && !eventPublisherConfiguration.getFromStreamName().isEmpty()) {
            streamId = eventPublisherConfiguration.getFromStreamName() + EventPublisherConstants.STREAM_ID_SEPERATOR +
                    ((eventPublisherConfiguration.getFromStreamVersion() != null && !eventPublisherConfiguration.getFromStreamVersion().isEmpty()) ?
                            eventPublisherConfiguration.getFromStreamVersion() : EventPublisherConstants.DEFAULT_STREAM_VERSION);
        }

        return streamId;
    }

    public static AxisConfiguration getAxisConfiguration() {
        AxisConfiguration axisConfiguration = null;
        if (CarbonContext.getThreadLocalCarbonContext().getTenantId() == MultitenantConstants.SUPER_TENANT_ID) {
            axisConfiguration = EventPublisherServiceValueHolder.getConfigurationContextService().
                    getServerConfigContext().getAxisConfiguration();
        } else {
            axisConfiguration = TenantAxisUtils.getTenantAxisConfiguration(CarbonContext.
                            getThreadLocalCarbonContext().getTenantDomain(),
                    EventPublisherServiceValueHolder.getConfigurationContextService().
                            getServerConfigContext());
        }
        return axisConfiguration;
    }
}
