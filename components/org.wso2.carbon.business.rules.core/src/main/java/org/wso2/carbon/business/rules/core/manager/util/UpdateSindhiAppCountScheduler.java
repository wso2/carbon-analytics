/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.business.rules.core.manager.util;

import org.wso2.carbon.business.rules.core.bean.TemplateManagerInstance;
import org.wso2.carbon.business.rules.core.services.TemplateManagerService;

import java.util.Map;
import java.util.TimerTask;

/**
 * Scheduler task to update siddhi app count map
 * **/
public class UpdateSindhiAppCountScheduler extends TimerTask {

    @Override
    public void run() {
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        Map<String, Long> siddhiAppsCount = templateManagerService.getSiddhiAppCount();
        templateManagerService.setSiddhiAppsCountMap(siddhiAppsCount);
    }
}
