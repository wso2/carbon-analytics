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
package org.wso2.carbon.analytics.spark.core.internal;

import org.wso2.carbon.analytics.spark.core.util.AnalyticsScript;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsPersistenceException;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;

public class AnalyticsPersistenceManager {
    private static AnalyticsPersistenceManager instance = new AnalyticsPersistenceManager();

    private AnalyticsPersistenceManager() {
    }

    public static AnalyticsPersistenceManager getInstance() {
        return instance;
    }

    public void saveScript(int tenantId, String scriptName, String scriptContent, String cron)
            throws AnalyticsPersistenceException {
        String scriptLocation = getScriptLocation(tenantId, scriptName);
        if (!new File(scriptLocation).exists()) {
            AnalyticsScript analyticsScript = new AnalyticsScript(scriptName);
            if (cron != null && !cron.trim().isEmpty()) analyticsScript.setCronExpression(cron);
            analyticsScript.setScriptContent(scriptContent);
            try {
                JAXBContext context = JAXBContext.newInstance(AnalyticsScript.class);
                Marshaller marshaller = context.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
                marshaller.marshal(analyticsScript, new File(scriptLocation));
            } catch (JAXBException e) {
                throw new AnalyticsPersistenceException("Error while saving the script : " + scriptName
                        + "for tenantId : " + tenantId, e);
            }
        } else {
            throw new AnalyticsPersistenceException("Already a script exists with same name : " + scriptName
                    + " for tenantId :" + tenantId);
        }
    }

    private String getScriptLocation(int tenantId, String scriptName) throws AnalyticsPersistenceException {
        createDirIfNotExistsLocation(tenantId);
        return MultitenantUtils.getAxis2RepositoryPath(tenantId) + File.separator +
                AnalyticsConstants.SCRIPT_STORE_DIR_NAME + File.separator + scriptName +
                AnalyticsConstants.SCRIPT_EXTENSION;
    }

    private String createDirIfNotExistsLocation(int tenantId) throws AnalyticsPersistenceException {
        String path = MultitenantUtils.getAxis2RepositoryPath(tenantId) + File.separator
                + AnalyticsConstants.SCRIPT_STORE_DIR_NAME;
        File file = new File(path);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                throw new AnalyticsPersistenceException("Error while creating the deployment directory : "
                        + file.getAbsolutePath() + " for tenantId :" + tenantId);
            }
        }
        return path;
    }

    public void deleteScript(int tenantId, String scriptName) throws AnalyticsPersistenceException {
        String scriptLocation = getScriptLocation(tenantId, scriptName);
        File script = new File(scriptLocation);
        if (script.exists()) {
            if (!script.delete()) {
                throw new AnalyticsPersistenceException("Unable to delete the script: " + scriptName
                        + " for tenantId: " + tenantId + ". ");
            }
        } else {
            throw new AnalyticsPersistenceException("Cannot delete non existing script : "
                    + scriptName + " for tenantId : " + tenantId);
        }
    }

    public void updateScript(int tenantId, String scriptName, String scriptContent, String cron)
            throws AnalyticsPersistenceException {
        if (cron != null && cron.trim().isEmpty()) cron = null;
        AnalyticsScript script = getScript(tenantId, scriptName);
        if (scriptContent != null && !scriptContent.trim().isEmpty()) script.setScriptContent(scriptContent);
        if ((cron == null || !cron.equals(AnalyticsConstants.DEFAULT_CRON)))
            script.setCronExpression(cron);
        try {
            JAXBContext context = JAXBContext.newInstance(AnalyticsScript.class);
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            marshaller.marshal(script, new File(getScriptLocation(tenantId, scriptName)));
        } catch (JAXBException e) {
            throw new AnalyticsPersistenceException("Error while saving the script : " + scriptName
                    + "for tenantId : " + tenantId, e);
        }
    }

    public AnalyticsScript getScript(int tenantId, String scriptName) throws AnalyticsPersistenceException {
        String scriptLocation = getScriptLocation(tenantId, scriptName);
        if (new File(scriptLocation).exists()) {
            try {
                JAXBContext context = JAXBContext.newInstance(AnalyticsScript.class);
                Unmarshaller un = context.createUnmarshaller();
                return (AnalyticsScript) un.unmarshal(new File(scriptLocation));
            } catch (JAXBException e) {
                throw new AnalyticsPersistenceException("Error while loading the script :" + scriptName
                        + " for tenantId: " + tenantId);
            }
        } else {
            throw new AnalyticsPersistenceException("No script exists with name : "
                    + scriptName + " for tenantId : " + tenantId);
        }
    }
}
