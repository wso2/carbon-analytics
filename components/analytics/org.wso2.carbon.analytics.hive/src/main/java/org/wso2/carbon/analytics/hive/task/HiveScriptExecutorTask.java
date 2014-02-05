/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.hive.task;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.hive.HiveConstants;
import org.wso2.carbon.analytics.hive.ServiceHolder;
import org.wso2.carbon.analytics.hive.exception.HiveExecutionException;
import org.wso2.carbon.analytics.hive.exception.HiveScriptStoreException;
import org.wso2.carbon.analytics.hive.persistence.HiveScriptPersistenceManager;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ntask.core.Task;

import java.util.Date;
import java.util.Map;

public class HiveScriptExecutorTask implements Task {

    private static Log log = LogFactory.getLog(HiveScriptExecutorTask.class);

    private Map<String, String> properties;

    public void setProperties(Map<String, String> properties) {
        this.properties = properties;
    }

    public void init() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void execute() {
        String scriptName = properties.get(HiveConstants.HIVE_SCRIPT_NAME);
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(Integer.parseInt(properties.get(HiveConstants.TASK_TENANT_ID_KEY)));
        log.info("Running script executor task for script " + scriptName + ". [" + new Date() + "]");

        String script;
        try {
            HiveScriptPersistenceManager manager = HiveScriptPersistenceManager.getInstance();
            script = manager.retrieveScript(scriptName);
        } catch (HiveScriptStoreException e) {
            log.error("Error while retrieving the script : " + scriptName, e);
            return;
        }

        try {
            ServiceHolder.getHiveExecutorService().execute(scriptName, script);
        } catch (HiveExecutionException e) {
            log.error("Error while executing script : " + scriptName , e);
        }
    }

}
