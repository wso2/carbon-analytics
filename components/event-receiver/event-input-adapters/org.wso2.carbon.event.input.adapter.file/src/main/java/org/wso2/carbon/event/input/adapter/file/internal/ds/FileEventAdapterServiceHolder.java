/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.input.adapter.file.internal.ds;

import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.event.input.adapter.file.internal.listener.LateStartAdapterListener;
import org.wso2.carbon.event.input.adapter.file.internal.util.FileEventAdapterConstants;
import org.wso2.carbon.utils.ConfigurationContextService;

import java.util.ArrayList;
import java.util.List;

public class FileEventAdapterServiceHolder {


    private static ConfigurationContextService configurationContextService;
    private static List<LateStartAdapterListener> lateStartAdapterListeners = new ArrayList<LateStartAdapterListener>();
    private static final org.apache.commons.logging.Log log = LogFactory.getLog(FileEventAdapterServiceHolder.class);

    public static void registerConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        FileEventAdapterServiceHolder.configurationContextService = configurationContextService;
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    log.info("File input event adapter waiting for dependent configurations to load");
                    Thread.sleep(FileEventAdapterConstants.AXIS_TIME_INTERVAL_IN_MILLISECONDS * 4);
                    loadLateStartEventAdapters();
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }).start();
    }

    public static void unregisterConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        FileEventAdapterServiceHolder.configurationContextService = configurationContextService;
    }

    public static ConfigurationContextService getConfigurationContextService() {
        return configurationContextService;
    }

    public static void addLateStartAdapterListener(LateStartAdapterListener lateStartAdapterListener) {
        lateStartAdapterListeners.add(lateStartAdapterListener);
    }

    public static void loadLateStartEventAdapters() {
        for (LateStartAdapterListener lateStartAdapterListener : lateStartAdapterListeners) {
            lateStartAdapterListener.tryStartAdapter();
        }
    }


}
