package org.wso2.carbon.bam.toolbox.deployer.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.hive.exception.HiveScriptStoreException;
import org.wso2.carbon.analytics.hive.web.HiveScriptStoreService;
import org.wso2.carbon.bam.toolbox.deployer.exception.BAMComponentNotFoundException;

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
 public class HiveScriptStoreClient{

    private static Log log = LogFactory.getLog(HiveScriptStoreClient.class);
    private static HiveScriptStoreClient instance;
    private static HiveScriptStoreService service;

    private HiveScriptStoreClient() throws BAMComponentNotFoundException{
        try{
            service = new HiveScriptStoreService();
        }catch (Exception ex){
            log.info("No Hive Script store service available..");
            throw new BAMComponentNotFoundException("No Hive Script store service available..");
        }
    }

    public static HiveScriptStoreClient getInstance() throws BAMComponentNotFoundException {
        if(null == instance){
            instance = new HiveScriptStoreClient();
        }
        return instance;
    }


    public void saveHiveScript(String scriptName, String scriptContent, String cron) {
        try {
            service.saveHiveScript(scriptName, scriptContent, cron);
        } catch (HiveScriptStoreException e) {
             log.error("Error while saving the hive script", e);
        }
    }

    public void deleteScript(String scriptName){
        try {
            service.deleteScript(scriptName);
        } catch (HiveScriptStoreException e) {
            log.error("Error while deleting the hive script", e);
        }
    }
}
