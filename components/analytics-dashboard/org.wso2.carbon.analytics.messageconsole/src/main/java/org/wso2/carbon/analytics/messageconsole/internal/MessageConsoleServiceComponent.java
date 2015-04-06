package org.wso2.carbon.analytics.messageconsole.internal;

/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.SecureAnalyticsDataService;
import org.wso2.carbon.analytics.messageconsole.Constants;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.service.TaskService;

/**
 * This class represents the analytics message console service declarative services component.
 * @scr.component name="messageconsole.component" immediate="true"
 * @scr.reference name="secure.analytics.component" interface="org.wso2.carbon.analytics.dataservice.SecureAnalyticsDataService"
 * cardinality="1..1" policy="dynamic" bind="setSecureAnalyticsDataService" unbind="unsetSecureAnalyticsDataService"
 * @scr.reference name="analytics.component" interface="org.wso2.carbon.analytics.dataservice.AnalyticsDataService"
 * cardinality="1..1" policy="dynamic" bind="setAnalyticsDataService" unbind="unsetAnalyticsDataService"
 * @scr.reference name="ntask.component" interface="org.wso2.carbon.ntask.core.service.TaskService"
 * cardinality="1..1" policy="dynamic" bind="setTaskService" unbind="unsetTaskService"
 */
public class MessageConsoleServiceComponent {
    private static final Log logger = LogFactory.getLog(MessageConsoleServiceComponent.class);

    protected void activate(ComponentContext ctx) {
        if (logger.isDebugEnabled()) {
            logger.debug("Activating Analytics MessageConsoleServiceComponent module.");
        }
        try {
            ServiceHolder.getTaskService().registerTaskType(Constants.ANALYTICS_DATA_PURGING);
        } catch (TaskException e) {
            logger.error("Unable to register analytics data purging task type", e);
        }
    }

    protected void setSecureAnalyticsDataService(SecureAnalyticsDataService secureAnalyticsDataService) {
        if (logger.isDebugEnabled()) {
            logger.info("Setting the Secure Analytics Data Service");
        }
        ServiceHolder.setSecureAnalyticsDataService(secureAnalyticsDataService);
    }

    protected void unsetSecureAnalyticsDataService(SecureAnalyticsDataService secureAnalyticsDataService) {
        if (logger.isDebugEnabled()) {
            logger.info("Unsetting the Secure Analytics Data Service");
        }
        ServiceHolder.setSecureAnalyticsDataService(null);
    }

    protected void setAnalyticsDataService(AnalyticsDataService analyticsDataService) {
        if (logger.isDebugEnabled()) {
            logger.info("Setting the Analytics Data Service");
        }
        ServiceHolder.setAnalyticsDataService(analyticsDataService);
    }

    protected void unsetAnalyticsDataService(AnalyticsDataService analyticsDataService) {
        if (logger.isDebugEnabled()) {
            logger.info("Unsetting the Analytics Data Service");
        }
        ServiceHolder.setAnalyticsDataService(null);
    }

    protected void setTaskService(TaskService taskService) {
        if (logger.isDebugEnabled()) {
            logger.debug("Setting the Task Service");
        }
        ServiceHolder.setTaskService(taskService);
    }

    protected void unsetTaskService(TaskService taskService) {
        if (logger.isDebugEnabled()) {
            logger.debug("Unsetting the Task Service");
        }
        ServiceHolder.setTaskService(null);
    }
}
