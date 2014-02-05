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
package org.wso2.carbon.analytics.hive.persistence;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.hive.HiveConstants;
import org.wso2.carbon.analytics.hive.ServiceHolder;
import org.wso2.carbon.analytics.hive.exception.HiveScriptStoreException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.ResourceImpl;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HiveScriptPersistenceManager {

    private static final Log log = LogFactory.getLog(HiveScriptPersistenceManager.class);

    private static HiveScriptPersistenceManager instance = new HiveScriptPersistenceManager();

    private HiveScriptPersistenceManager() {

    }

    public static HiveScriptPersistenceManager getInstance() {
        return instance;
    }

    public String retrieveScript(String scriptName) throws HiveScriptStoreException {
        UserRegistry registry;
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        Resource resource;
        InputStream scriptStream = null;
        String script;
        try {
            registry = ServiceHolder.getRegistryService().
                    getConfigSystemRegistry(tenantId);
        } catch (RegistryException e) {
            throw new HiveScriptStoreException("Failed to get registry", e);
        }
        String scriptPath = HiveConstants.HIVE_SCRIPT_BASE_PATH + scriptName + HiveConstants.HIVE_SCRIPT_EXT;

        try {
            resource = registry.get(scriptPath);
            scriptStream = resource.getContentStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(scriptStream));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }

            br.close();
            script = sb.toString();
            return script;
        } catch (RegistryException e) {
            log.error("Error while retrieving the script - " + scriptName + " from registry", e);
            throw new HiveScriptStoreException("Error while retrieving the script - " + scriptName + " from registry", e);
        } catch (IOException e) {
            log.error("Error while retrieving the script - " + scriptName + " from registry", e);
            throw new HiveScriptStoreException("Error while retrieving the script - " + scriptName + " from registry", e);
        }
    }

    public void saveScript(String scriptName, String scriptContent)
            throws HiveScriptStoreException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        UserRegistry configSystemRegistry;
        try {
            configSystemRegistry = ServiceHolder.getRegistryService().
                    getConfigSystemRegistry(tenantId);
            Resource resource = configSystemRegistry.newResource();
            resource.setContent(scriptContent);
            resource.setProperty(HiveConstants.HIVE_SCRIPT_NAME, scriptName);
            resource.setProperty(HiveConstants.SCRIPT_TRIGGER_CRON,
                                 HiveConstants.DEFAULT_TRIGGER_CRON);
            resource.setMediaType("text/plain");
            configSystemRegistry.put(HiveConstants.HIVE_SCRIPT_BASE_PATH + scriptName +
                                     HiveConstants.HIVE_SCRIPT_EXT, resource);

        } catch (RegistryException e) {
            throw new HiveScriptStoreException("Failed to get registry", e);
        }

        try {
            updateTenantTracker(tenantId);
        } catch (RegistryException e) {
            log.error("Unable to store tenant information for the script : " + scriptName +
                      ". May not be rescheduled at startup due to this..", e);
            throw new HiveScriptStoreException("Unable to store tenant information for the script" +
                                               " : " + scriptName + ". May not be rescheduled at" +
                                               " startup due to this..", e);
        }
    }


    public String[] getAllHiveScriptNames() throws HiveScriptStoreException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        Resource resource;
        ArrayList<String> scriptNames = null;
        try {
            UserRegistry registry = ServiceHolder.getRegistryService().getConfigSystemRegistry(tenantId);
            if (registry.resourceExists(HiveConstants.HIVE_SCRIPT_BASE_PATH)) {
                resource = registry.get(HiveConstants.HIVE_SCRIPT_BASE_PATH);
                if (resource instanceof Collection) {
                    scriptNames = new ArrayList<String>();
                    String[] paths = ((Collection) resource).getChildren();
                    for (String resourcePath : paths) {
                        Resource childResource = registry.get(resourcePath);

                        if (!(childResource instanceof Collection)) {
                            String name = ((ResourceImpl) childResource).getName();
                            scriptNames.add(name.split(HiveConstants.HIVE_SCRIPT_EXT)[0]);
                        }

                    }
                }
            } else {
                if (log.isDebugEnabled()) {
                    log.info("No scripts avaliable");
                }
            }
        } catch (RegistryException e) {
            throw new HiveScriptStoreException("Error occurred getting all the script names", e);
        }
        String[] nameArray = new String[0];
        if (scriptNames != null && scriptNames.size() > 0) {
            nameArray = new String[scriptNames.size()];
            nameArray = scriptNames.toArray(nameArray);
        }
        return nameArray;
    }


    public void deleteScript(String scriptName) throws HiveScriptStoreException {
        try {
            int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
            UserRegistry registry = ServiceHolder.getRegistryService().getConfigSystemRegistry(tenantId);
            if (registry.resourceExists(HiveConstants.HIVE_SCRIPT_BASE_PATH)) {
                registry.delete(HiveConstants.HIVE_SCRIPT_BASE_PATH + scriptName + HiveConstants.HIVE_SCRIPT_EXT);
            } else {
                if (log.isDebugEnabled()) {
                    log.info("no any script called " + scriptName + " , to delete");
                }
            }
        } catch (RegistryException e) {
            throw new HiveScriptStoreException("Error occurred deleting the script : " + scriptName, e);
        }


    }

    private void updateTenantTracker(int tenantId) throws RegistryException {
        ConfigurationContext superTenantContext = ServiceHolder.getConfigurationContextService().
                getServerConfigContext();
        UserRegistry superTenantRegistry = ServiceHolder.getRegistryService().
                getConfigSystemRegistry(PrivilegedCarbonContext.
                        //getCurrentContext(superTenantContext).getTenantId());
                         getThreadLocalCarbonContext().getTenantId());

        checkAndSetTenantId(superTenantRegistry, tenantId);

    }

    private synchronized void checkAndSetTenantId(UserRegistry superTenantRegistry, int tenantId)
            throws RegistryException {

        String trackerPath = HiveConstants.TENANT_TRACKER_PATH;

        Resource tenantTrackerResource;
        if (superTenantRegistry.resourceExists(trackerPath)) {
            tenantTrackerResource = superTenantRegistry.get(trackerPath);
            List<String> propertyValues = tenantTrackerResource.getPropertyValues(
                    HiveConstants.TENANTS_PROPERTY);
            if (propertyValues == null) {
                propertyValues = new ArrayList<String>();
            }
            propertyValues.add(String.valueOf(tenantId));
            //propertyValues.add("1");

            // Make sure that there is no duplication of tenant id's
            Set<String> uniqueProperties = new HashSet<String>();
            uniqueProperties.addAll(propertyValues);

            propertyValues.clear();
            for (String id : uniqueProperties) {
                propertyValues.add(id);
            }

            tenantTrackerResource.setProperty(HiveConstants.TENANTS_PROPERTY,
                                              propertyValues);
        } else {
            tenantTrackerResource = superTenantRegistry.newResource();
            List<String> propertyValues = new ArrayList<String>();
            propertyValues.add(String.valueOf(tenantId));

            // Make sure that there is no duplication of tenant id's
            Set<String> uniqueProperties = new HashSet<String>();
            uniqueProperties.addAll(propertyValues);

            propertyValues.clear();
            for (String id : uniqueProperties) {
                propertyValues.add(id);
            }

            tenantTrackerResource.setProperty(HiveConstants.TENANTS_PROPERTY,
                                              propertyValues);
        }
        superTenantRegistry.put(trackerPath, tenantTrackerResource);

    }
}
