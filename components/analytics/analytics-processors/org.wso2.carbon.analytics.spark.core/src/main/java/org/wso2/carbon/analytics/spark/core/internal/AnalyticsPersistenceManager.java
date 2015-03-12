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

import org.wso2.carbon.analytics.spark.core.AnalyticsTask;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsScript;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsPersistenceException;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsPersistenceManager {
    private static AnalyticsPersistenceManager instance = new AnalyticsPersistenceManager();

    private AnalyticsPersistenceManager() {
    }

    public static AnalyticsPersistenceManager getInstance() {
        return instance;
    }

    public void initialize(int tenantId) throws RegistryException {
        UserRegistry registry = ServiceHolder.getTenantConfigRegistry(tenantId);
        if (!registry.resourceExists(AnalyticsConstants.ANALYTICS_SCRIPTS_LOCATION)) {
            Collection collection = registry.newCollection();
            registry.put(AnalyticsConstants.ANALYTICS_SCRIPTS_LOCATION, collection);
        }
    }

    public void saveScript(int tenantId, String scriptName, String scriptContent, String cron)
            throws AnalyticsPersistenceException {
        try {
            UserRegistry userRegistry = ServiceHolder.getTenantConfigRegistry(tenantId);
            String scriptLocation = getScriptLocation(scriptName);
            if (!userRegistry.resourceExists(scriptLocation)) {
                AnalyticsScript script = processAndGetAnalyticsScript(scriptName, scriptContent, cron, null);
                Resource resource = userRegistry.newResource();
                resource.setContent(getConfiguration(script));
                resource.setMediaType(AnalyticsConstants.ANALYTICS_MEDIA_TYPE);
                userRegistry.put(scriptLocation, resource);
                scheduleTask(tenantId, script);
            } else {
                throw new AnalyticsPersistenceException("Already a script exists with same name : " + scriptName
                        + " for tenantId :" + tenantId);
            }
        } catch (RegistryException e) {
            throw new AnalyticsPersistenceException("Error while loading the registry for tenant : " + tenantId, e);
        } catch (JAXBException e) {
            throw new AnalyticsPersistenceException("Error while saving the script : " + scriptName
                    + "for tenantId : " + tenantId, e);
        }
    }

    private AnalyticsScript processAndGetAnalyticsScript(String scriptName, String scriptContent, String cron,
                                                         AnalyticsScript script) {
        if (script == null) {
            script = new AnalyticsScript(scriptName);
        }
        if (scriptContent != null && !scriptContent.trim().isEmpty()) script.setScriptContent(scriptContent);
        if (cron == null || cron.trim().isEmpty()) {
            script.setCronExpression(null);
        } else if (!cron.equals(AnalyticsConstants.DEFAULT_CRON)) {
            script.setCronExpression(cron);
        }
        return script;
    }

    private String getConfiguration(AnalyticsScript script) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(AnalyticsScript.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(script, stringWriter);
        return stringWriter.toString();
    }

    private void scheduleTask(int tenantId, AnalyticsScript script) throws AnalyticsPersistenceException {
        if (script.getCronExpression() != null) {
            Map<String, String> properties = new HashMap<>();
            properties.put(AnalyticsConstants.TASK_TENANT_ID_PROPERTY, String.valueOf(tenantId));
            properties.put(AnalyticsConstants.TASK_SCRIPT_NAME_PROPERTY, script.getName());
            TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo(script.getCronExpression());
            TaskInfo taskInfo = new TaskInfo(script.getName(), AnalyticsTask.class.getCanonicalName(),
                    properties, triggerInfo);
            try {
                ServiceHolder.getTaskManager().registerTask(taskInfo);
                ServiceHolder.getTaskManager().rescheduleTask(taskInfo.getName());
            } catch (TaskException e) {
                throw new AnalyticsPersistenceException("Error while trying to schedule task for script : "
                        + script.getName() + " for tenant: " + tenantId + " with cron expression :"
                        + script.getCronExpression() + " ." + e.getMessage(), e);
            }
        } else {
            try {
                ServiceHolder.getTaskManager().deleteTask(script.getName());
            } catch (TaskException e) {
                throw new AnalyticsPersistenceException("Error while trying un schedule the task :" +
                        script.getName() + " for tenant : " + tenantId, e);
            }
        }
    }

    private String getScriptLocation(String scriptName) throws AnalyticsPersistenceException {
        return AnalyticsConstants.ANALYTICS_SCRIPTS_LOCATION + RegistryConstants.PATH_SEPARATOR + scriptName +
                AnalyticsConstants.SCRIPT_EXTENSION;
    }

    public void deleteScript(int tenantId, String scriptName) throws AnalyticsPersistenceException {
        try {
            UserRegistry userRegistry = ServiceHolder.getTenantConfigRegistry(tenantId);
            String scriptLocation = getScriptLocation(scriptName);
            if (userRegistry.resourceExists(scriptLocation)) {
                userRegistry.delete(scriptLocation);
                scheduleTask(tenantId, new AnalyticsScript(scriptName));
            } else {
                throw new AnalyticsPersistenceException("Cannot delete non existing script : "
                        + scriptName + " for tenantId : " + tenantId);
            }
        } catch (RegistryException e) {
            throw new AnalyticsPersistenceException("Error while loading the registry for tenant : " + tenantId, e);
        }
    }

    private AnalyticsScript getAnalyticsScript(String config) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(AnalyticsScript.class);
        Unmarshaller un = context.createUnmarshaller();
        return (AnalyticsScript) un.unmarshal(new StringReader(config));
    }

    public void updateScript(int tenantId, String scriptName, String scriptContent, String cron)
            throws AnalyticsPersistenceException {
        try {
            UserRegistry userRegistry = ServiceHolder.getTenantConfigRegistry(tenantId);
            String scriptLocation = getScriptLocation(scriptName);
            if (userRegistry.resourceExists(scriptLocation)) {
                Resource resource = userRegistry.get(scriptLocation);
                AnalyticsScript script = processAndGetAnalyticsScript(scriptName, scriptContent,
                        cron, getAnalyticsScript(RegistryUtils.decodeBytes((byte[]) userRegistry.get(scriptLocation).getContent())));
                resource.setContent(getConfiguration(script));
                resource.setMediaType(AnalyticsConstants.ANALYTICS_MEDIA_TYPE);
                userRegistry.put(scriptLocation, resource);
                scheduleTask(tenantId, script);
            } else {
                throw new AnalyticsPersistenceException("Already a script exists with same name : " + scriptName
                        + " for tenantId :" + tenantId);
            }
        } catch (RegistryException e) {
            throw new AnalyticsPersistenceException("Error while loading the registry for tenant :" + tenantId, e);
        } catch (JAXBException e) {
            throw new AnalyticsPersistenceException("Error while converting the configuration for script : "
                    + scriptName, e);
        }
    }

    public AnalyticsScript getAnalyticsScript(int tenantId, String scriptName) throws AnalyticsPersistenceException {
        try {
            UserRegistry registry = ServiceHolder.getTenantConfigRegistry(tenantId);
            String scriptLocation = getScriptLocation(scriptName);
            if (registry.resourceExists(scriptLocation)) {
                return getAnalyticsScript(RegistryUtils.decodeBytes((byte[]) registry.get(scriptLocation).getContent()));
            } else {
                throw new AnalyticsPersistenceException("No script exists with name : "
                        + scriptName + " for tenantId : " + tenantId);
            }
        } catch (RegistryException e) {
            throw new AnalyticsPersistenceException("Error while loading the registry for tenant : " + tenantId, e);
        } catch (JAXBException e) {
            throw new AnalyticsPersistenceException("Error while loading the configuration for script : " + scriptName, e);
        }
    }

    public List<AnalyticsScript> getAllAnalyticsScripts(int tenantId) throws AnalyticsPersistenceException {
        try {
            UserRegistry registry = ServiceHolder.getTenantConfigRegistry(tenantId);
            Collection scriptsCollection = (Collection) registry.get(AnalyticsConstants.ANALYTICS_SCRIPTS_LOCATION);
            String[] scripts = scriptsCollection.getChildren();
            if (scripts != null) {
                List<AnalyticsScript> analyticsScripts = new ArrayList<>();
                for (String script : scripts) {
                    String configContent = RegistryUtils.decodeBytes((byte[]) registry.get(script).getContent());
                    analyticsScripts.add(getAnalyticsScript(configContent));
                }
                return analyticsScripts;
            }
            return null;
        } catch (RegistryException e) {
            throw new AnalyticsPersistenceException("Error while loading the registry for tenant : " + tenantId, e);
        } catch (JAXBException e) {
            throw new AnalyticsPersistenceException("Error while loading the configuration for scripts for tenant: "
                    + tenantId, e);
        }
    }

}
