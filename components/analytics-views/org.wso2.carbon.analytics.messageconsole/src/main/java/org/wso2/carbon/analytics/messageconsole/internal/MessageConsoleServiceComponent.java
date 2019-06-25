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
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.ntask.core.service.TaskService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * This class represents the analytics message console service declarative services component.
 */
@Component(
         name = "messageconsole.component", 
         immediate = true)
public class MessageConsoleServiceComponent {

    private static final Log log = LogFactory.getLog(MessageConsoleServiceComponent.class);

    @Activate
    protected void activate(ComponentContext ctx) {
    }

    @Reference(
             name = "analytics.component", 
             service = AnalyticsDataService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetAnalyticsDataService")
    protected void setAnalyticsDataService(AnalyticsDataService analyticsDataService) {
        ServiceHolder.setAnalyticsDataService(analyticsDataService);
    }

    protected void unsetAnalyticsDataService(AnalyticsDataService analyticsDataService) {
        ServiceHolder.setAnalyticsDataService(null);
    }

    @Reference(
             name = "ntask.component", 
             service = org.wso2.carbon.ntask.core.service.TaskService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetTaskService")
    protected void setTaskService(TaskService taskService) {
        ServiceHolder.setTaskService(taskService);
    }

    protected void unsetTaskService(TaskService taskService) {
        ServiceHolder.setTaskService(null);
    }
}

