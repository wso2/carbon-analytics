/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.stream.core.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.stream.core.EventStreamListener;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.wso2.carbon.event.stream.core.internal.CarbonEventStreamService;
import org.wso2.carbon.event.stream.core.internal.EventStreamRuntime;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component name="eventStreamService.component" immediate="true"
 * @scr.reference name="config.context.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="0..1" policy="dynamic"
 * bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 * @scr.reference name="eventStreamListener.service"
 * interface="org.wso2.carbon.event.stream.core.EventStreamListener" cardinality="0..n" policy="dynamic"
 * bind="setEventStreamListener" unbind="unsetEventStreamListener"
 */
public class EventStreamServiceDS {
    private static final Log log = LogFactory.getLog(EventStreamServiceDS.class);

    protected void activate(ComponentContext context) {
        try {
            EventStreamServiceValueHolder.registerEventStreamRuntime(new EventStreamRuntime());
            CarbonEventStreamService carbonEventStreamService = new CarbonEventStreamService();
            EventStreamServiceValueHolder.setCarbonEventStreamService(carbonEventStreamService);
            context.getBundleContext().registerService(EventStreamService.class.getName(), carbonEventStreamService, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed EventStreamService");
            }
        } catch (Throwable e) {
            log.error("Could not create EventStreamService : " + e.getMessage(), e);
        }
    }

    protected void setConfigurationContextService(ConfigurationContextService configurationContextService) {
        EventStreamServiceValueHolder.registerConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(ConfigurationContextService configurationContextService) {
        EventStreamServiceValueHolder.registerConfigurationContextService(null);
    }

    protected void setEventStreamListener(EventStreamListener eventStreamListener) {
        EventStreamServiceValueHolder.registerEventStreamListener(eventStreamListener);
    }

    protected void unsetEventStreamListener(EventStreamListener eventStreamListener) {
        EventStreamServiceValueHolder.unregisterEventStreamListener(eventStreamListener);
    }
}
