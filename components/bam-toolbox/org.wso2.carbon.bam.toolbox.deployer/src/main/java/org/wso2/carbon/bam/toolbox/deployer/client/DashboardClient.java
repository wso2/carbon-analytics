package org.wso2.carbon.bam.toolbox.deployer.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.toolbox.deployer.ServiceHolder;
import org.wso2.carbon.bam.toolbox.deployer.exception.BAMComponentNotFoundException;
import org.wso2.carbon.dashboard.DashboardDSService;

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
public class DashboardClient {

    private static Log log = LogFactory.getLog(DashboardClient.class);
    private DashboardDSService service;
    private static DashboardClient instance;

    private DashboardClient() throws BAMComponentNotFoundException {
        service = ServiceHolder.getDashboardService();
        if (service == null) {
            log.warn("No Dashboard service class found.");
            throw new BAMComponentNotFoundException("No Dashboard service class found");
        }
    }

    public static DashboardClient getInstance() throws BAMComponentNotFoundException {
        if (null == instance) {
            instance = new DashboardClient();
        }
        return instance;
    }

    public boolean addNewGadget(String userId, String tabId, String url) {
        try {
            return service.addGadgetToUser(userId, tabId, url, null, null);
        } catch (Exception e) {
            log.error("Exception while adding a new gadget", e);
            return false;
        }

    }

    public int addTab(String userId, String tabTitle) {
        try {
           //int tabId = service.addNewTab(userId, tabTitle, "bam");
            return service.addNewTab(userId, tabTitle, null);
        } catch (Exception e) {
            log.error("Exception while adding a new gadget", e);
            return -1;
        }
    }

    public void removeTab(String userId, int tabId) {
        try {
            service.removeTab(userId, String.valueOf(tabId), null);
        } catch (Exception e) {
            log.error("Exception while deleting tab: " + tabId, e);
        }
    }
}

