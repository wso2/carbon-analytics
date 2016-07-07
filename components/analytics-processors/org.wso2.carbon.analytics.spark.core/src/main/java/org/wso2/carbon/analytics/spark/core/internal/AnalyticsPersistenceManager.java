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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.spark.core.AnalyticsTask;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsPersistenceException;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsConstants;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsScript;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.ntask.common.TaskException;
import org.wso2.carbon.ntask.core.TaskInfo;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.RegistryUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Persistence manager to do actual read/write from registry space for
 * the analytics scripts.
 */
public class AnalyticsPersistenceManager {
    private static final Log log = LogFactory.getLog(AnalyticsPersistenceManager.class);
    private static AnalyticsPersistenceManager instance = new AnalyticsPersistenceManager();

    private AnalyticsPersistenceManager() {
        /**
         * Avoid instantiation.
         */
    }

    /**
     * Singleton implementation, and hence always returns the same manager instance.
     *
     * @return Instance of the manager.
     */
    public static AnalyticsPersistenceManager getInstance() {
        return instance;
    }

    private void createScriptsCollectionIfNotExists(UserRegistry registry) throws RegistryException {
        if (!registry.resourceExists(AnalyticsConstants.ANALYTICS_SCRIPTS_LOCATION)) {
            Collection collection = registry.newCollection();
            registry.put(AnalyticsConstants.ANALYTICS_SCRIPTS_LOCATION, collection);
        }
    }

    /**
     * Validates whether there is already existing a script with same in the registry,
     * if such resource is existing in the registry then it'll throw and exception.
     * It also converts the all the given information about the script
     * to an xml content and store it in the registry.
     *
     * @param tenantId      Id of the tenant the operation belongs to.
     * @param scriptName    Name of the script which needs to be stored.
     * @param scriptContent Queries content provided to be saved on registry.
     * @param cron          Cron expression for the rate of the script execution task.
     * @throws AnalyticsPersistenceException
     */
    public void saveScript(int tenantId, String scriptName, String scriptContent, String cron, String carbonAppName, boolean editable)
            throws AnalyticsPersistenceException {
        try {
            UserRegistry userRegistry = ServiceHolder.getTenantConfigRegistry(tenantId);
            createScriptsCollectionIfNotExists(userRegistry);
            String scriptLocation = getScriptLocation(scriptName);
            if (!userRegistry.resourceExists(scriptLocation)) {
                AnalyticsScript script = processAndGetAnalyticsScript(scriptName, scriptContent, cron, carbonAppName, editable, null);
                Resource resource = userRegistry.newResource();
                resource.setContent(getConfiguration(script));
                resource.setMediaType(AnalyticsConstants.ANALYTICS_MEDIA_TYPE);
                userRegistry.put(scriptLocation, resource);
                try {
                    scheduleTask(tenantId, script);
                } catch (AnalyticsPersistenceException ex) {
                    deleteScript(tenantId, scriptName);
                    throw ex;
                }
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

    /**
     * Building the object of the analytics script based on the information being passed.
     *
     * @param scriptName    Name of the script.
     * @param scriptContent Script content of the analytics script object,
     * @param cron          Cron expression of the analytics script.If this is with 'DEFAULT',
     *                      then it won't modify the cron information of the script passed in.
     * @param script        Analytics Script object which needs to be populated with passed content.
     *                      If this is null, and new object will be created with passed information.
     * @return New populated analytics script with provided information.
     */
    private AnalyticsScript processAndGetAnalyticsScript(String scriptName, String scriptContent, String cron,
                                                         String carbonAppName, boolean editable, AnalyticsScript script) {
        if (script == null) {
            script = new AnalyticsScript(scriptName);
        }
        if (scriptContent != null && !scriptContent.trim().isEmpty()) script.setScriptContent(scriptContent);
        if (cron == null || cron.trim().isEmpty()) {
            script.setCronExpression(null);
        } else if (!cron.equals(AnalyticsConstants.DEFAULT_CRON)) {
            script.setCronExpression(cron);
        }
        script.setEditable(editable);
        script.setCarbonApplicationFileName(carbonAppName);
        return script;
    }

    /**
     * Get the XML configuration of the analytics script object passed in.
     *
     * @param script Analytics script object which needs to be converted as XML.
     * @return XML configuration representation for the analytics script.
     * @throws JAXBException
     */
    private String getConfiguration(AnalyticsScript script) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(AnalyticsScript.class);
        Marshaller marshaller = context.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(script, stringWriter);
        return stringWriter.toString();
    }

    /**
     * Based on the information provided in the analytics script object schedule/reschedule/delete t
     * he task associated with the script.
     *
     * @param tenantId Id of the tenant for which this operation belongs to.
     * @param script   Analytics script object which needs to be evaluated.
     * @throws AnalyticsPersistenceException
     */
    private void scheduleTask(int tenantId, AnalyticsScript script) throws AnalyticsPersistenceException {
        if (script.getCronExpression() != null) {
            Map<String, String> properties = new HashMap<>();
            properties.put(AnalyticsConstants.TASK_TENANT_ID_PROPERTY, String.valueOf(tenantId));
            properties.put(AnalyticsConstants.TASK_SCRIPT_NAME_PROPERTY, script.getName());
            TaskInfo.TriggerInfo triggerInfo = new TaskInfo.TriggerInfo(script.getCronExpression());
            triggerInfo.setDisallowConcurrentExecution(true);
            TaskInfo taskInfo = new TaskInfo(script.getName(), AnalyticsTask.class.getCanonicalName(),
                    properties, triggerInfo);
            try {
                ServiceHolder.getTaskManager().registerTask(taskInfo);
                ServiceHolder.getTaskManager().rescheduleTask(taskInfo.getName());
            } catch (Exception e) {
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

    /**
     * An utility method used to find the scripts location in the registry space.
     *
     * @param scriptName Name of the script.
     * @return Actual resource location path in the registry.
     * @throws AnalyticsPersistenceException
     */
    private String getScriptLocation(String scriptName) throws AnalyticsPersistenceException {
        scriptName = GenericUtils.checkAndReturnPath(scriptName);
        return AnalyticsConstants.ANALYTICS_SCRIPTS_LOCATION + RegistryConstants.PATH_SEPARATOR + scriptName +
                AnalyticsConstants.SCRIPT_EXTENSION_SEPARATOR + AnalyticsConstants.SCRIPT_EXTENSION;
    }

    /**
     * Delete the script information stored in registry.
     *
     * @param tenantId   Id of the tenant.
     * @param scriptName Name of the script.
     * @throws AnalyticsPersistenceException
     */
    public void deleteScript(int tenantId, String scriptName) throws AnalyticsPersistenceException {
        try {
            UserRegistry userRegistry = ServiceHolder.getTenantConfigRegistry(tenantId);
            String scriptLocation = getScriptLocation(scriptName);
            if (userRegistry.resourceExists(scriptLocation)) {
                userRegistry.delete(scriptLocation);
                scheduleTask(tenantId, new AnalyticsScript(scriptName));
            } else {
                log.info("Cannot delete non existing script : " + scriptName + " for tenantId : " + tenantId + ". " +
                        "It might have been deleted already." );
            }
        } catch (RegistryException e) {
            throw new AnalyticsPersistenceException("Error while loading the registry for tenant : " + tenantId, e);
        }
    }

    /**
     * Builds the analytics script object based on the XML configuration passed.
     *
     * @param config XML configuration representation for the script.
     * @return Analytics Script object for the configuration.
     * @throws JAXBException
     */
    private AnalyticsScript getAnalyticsScript(String config) throws JAXBException {
        JAXBContext context = JAXBContext.newInstance(AnalyticsScript.class);
        Unmarshaller un = context.createUnmarshaller();
        AnalyticsScript analyticsScript = (AnalyticsScript) un.unmarshal(new StringReader(config));
        if (!analyticsScript.isEditable()) {
            if (analyticsScript.getCarbonApplicationFileName() != null &&
                    !analyticsScript.getCarbonApplicationFileName().trim().isEmpty()) {
                String carbonAppLocation = MultitenantUtils.getAxis2RepositoryPath(PrivilegedCarbonContext.
                        getThreadLocalCarbonContext().getTenantId()) + File.separator + AnalyticsConstants.CARBON_APPLICATION_DEPLOYMENT_DIR
                        + File.separator + analyticsScript.getCarbonApplicationFileName() + AnalyticsConstants.CARBON_APPLICATION_EXT;
                if (!new File(carbonAppLocation).exists()) {
                    analyticsScript.setEditable(true);
                }
            } else {
                analyticsScript.setEditable(true);
            }
        }
        return analyticsScript;
    }

    /**
     * Update the script with given details at the tenant registry space.
     *
     * @param tenantId      Id of the tenant.
     * @param scriptName    Name of the script.
     * @param scriptContent Queries content of the script.
     * @param cron          New cron expression of the script.
     * @throws AnalyticsPersistenceException
     */
    public void putScript(int tenantId, String scriptName, String scriptContent, String cron, String carbonAppName, boolean editable)
            throws AnalyticsPersistenceException {
        try {
            UserRegistry userRegistry = ServiceHolder.getTenantConfigRegistry(tenantId);
            String scriptLocation = getScriptLocation(scriptName);
            if (userRegistry.resourceExists(scriptLocation)) {
                Resource resource = userRegistry.get(scriptLocation);
                AnalyticsScript script = processAndGetAnalyticsScript(scriptName, scriptContent,
                        cron, carbonAppName, editable, getAnalyticsScript(RegistryUtils.decodeBytes((byte[]) userRegistry.get(scriptLocation).getContent())));
                resource.setContent(getConfiguration(script));
                resource.setMediaType(AnalyticsConstants.ANALYTICS_MEDIA_TYPE);
                userRegistry.put(scriptLocation, resource);
                scheduleTask(tenantId, script);
            } else {
                this.saveScript(tenantId, scriptName, scriptContent, cron, carbonAppName, editable);
            }
        } catch (RegistryException e) {
            throw new AnalyticsPersistenceException("Error while loading the registry for tenant :" + tenantId, e);
        } catch (JAXBException e) {
            throw new AnalyticsPersistenceException("Error while converting the configuration for script : "
                    + scriptName, e);
        }
    }

    /**
     * Get the acutal analytics script object for the given script name in the tenant registry space.
     *
     * @param tenantId   Id of the tenant.
     * @param scriptName Name of the script.
     * @return Analytics Script object.
     * @throws AnalyticsPersistenceException
     */
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

    /**
     * Return all the scripts in the tenant registry space.
     *
     * @param tenantId Id of the tenant.
     * @return List of analytics scripts.
     * @throws AnalyticsPersistenceException
     */
    public List<AnalyticsScript> getAllAnalyticsScripts(int tenantId) throws AnalyticsPersistenceException {
        try {
            UserRegistry registry = ServiceHolder.getTenantConfigRegistry(tenantId);
            createScriptsCollectionIfNotExists(registry);
            Collection scriptsCollection = (Collection) registry.get(AnalyticsConstants.ANALYTICS_SCRIPTS_LOCATION);
            String[] scripts = scriptsCollection.getChildren();
            if (scripts != null) {
                List<AnalyticsScript> analyticsScripts = new ArrayList<>();
                for (String script : scripts) {
                    Object content = registry.get(script).getContent();
                    if (content instanceof byte[]) {
                        String configContent = RegistryUtils.decodeBytes((byte[])content);
                        analyticsScripts.add(getAnalyticsScript(configContent));
                    } else {
                        log.error("Failed to load the configuration at: " + script + " for tenant: " + tenantId
                                  + ". Resource not in valid format. Required byte[] but found " + script.getClass().getCanonicalName());
                    }
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
