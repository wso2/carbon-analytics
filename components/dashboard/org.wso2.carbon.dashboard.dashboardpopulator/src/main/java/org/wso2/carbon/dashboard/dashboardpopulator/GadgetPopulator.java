package org.wso2.carbon.dashboard.dashboardpopulator;

/*
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.MediaTypesUtils;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.utils.CarbonUtils;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;


public class GadgetPopulator {

    private static final Log log = LogFactory.getLog(GadgetPopulator.class);

    public static final String SYSTEM_GADGETS_PATH = "/repository/dashboards/gadgets";

    private static final String REGISTRY_SYSTEM_DASHBOARDS_ROOT = "/repository/dashboards";

    static String gadgetsPath = SYSTEM_GADGETS_PATH;

    /**
     * This will not use because of on demand population method
     *
     * @param tenantId
     */
    public static void populateDashboard(int tenantId) {
        try {

            String dashboardDiskRoot = System.getProperty(ServerConstants.CARBON_HOME) + File
                    .separator + "repository" + File.separator + "resources" + File.separator + "dashboard";

            String dashboardConfigFile = dashboardDiskRoot + File.separator + "dashboard.xml";
            String gadgetsDiskLocation = dashboardDiskRoot + File.separator + "gadgets";

            // Check whether the system dashboard is already available if not populate
            UserRegistry registry = DashboardPopulatorContext.getRegistry(tenantId);

            // Set permission for annonymous read. We do it here because it should happen always in order
            // to support mounting a remote registry.
            AuthorizationManager accessControlAdmin =
                    registry.getUserRealm().getAuthorizationManager();

            if (!accessControlAdmin.isRoleAuthorized(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME, RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                    GadgetPopulator.SYSTEM_GADGETS_PATH, ActionConstants.GET)) {
                accessControlAdmin.authorizeRole(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME, RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                        GadgetPopulator.SYSTEM_GADGETS_PATH, ActionConstants.GET);
            }

            String serverName = CarbonUtils.getServerConfiguration().getFirstProperty("Name");

/*            String serverPrefix = serverName.indexOf("Business Activity Monitor") > -1 ? "bam" : "";

            if (serverName.length() > 0) {
                gadgetsPath = SYSTEM_GADGETS_PATH + "/" + serverPrefix;
            }*/

            if (!registry.resourceExists(REGISTRY_SYSTEM_DASHBOARDS_ROOT + "/" + serverName)) {
                // Creating an OMElement from file
                File dashboardConfigXml = new File(dashboardConfigFile);

                if (dashboardConfigXml.exists()) {
                    FileReader dashboardConfigXmlReader = new FileReader(dashboardConfigXml);

                    // Restoring from file
                    registry.restore(REGISTRY_SYSTEM_DASHBOARDS_ROOT, dashboardConfigXmlReader);

                    log.info("Successfully populated the default Dashboards.");

                } else {
                    log.info("Couldn't find a Dashboard at '" + dashboardConfigFile +
                            "'. Giving up.");
                }

                // Store gadgets
                File gadgetsDir = new File(gadgetsDiskLocation);
                if (gadgetsDir.exists()) {
                    GadgetPopulator.beginFileTansfer(gadgetsDir, tenantId);

                    log.info("Successfully populated the default Gadgets.");
                } else {
                    log.info("Couldn't find contents at '" + gadgetsDiskLocation +
                            "'. Giving up.");
                }
                registry.put(REGISTRY_SYSTEM_DASHBOARDS_ROOT + "/" + serverName, registry.newResource());
            }

        } catch (Exception e) {
            log.debug("Failed to activate Dashboard Populator for Governance bundle ");
        }
    }

    public static void beginFileTansfer(File rootDirectory, int tenantId) throws RegistryException {
        try {

            // Storing the root path for future reference
            String rootPath = rootDirectory.getAbsolutePath();

            UserRegistry registry = DashboardPopulatorContext.getRegistry(tenantId);

            // Creating the default gadget collection resource
            Collection defaultGadgetCollection = registry.newCollection();
            try {
                registry.beginTransaction();
                registry.put(gadgetsPath, defaultGadgetCollection);

                transferDirectoryContentToRegistry(rootDirectory, registry, rootPath, tenantId);
                registry.commitTransaction();
            } catch (Exception e) {
                registry.rollbackTransaction();
                log.error(e.getMessage(), e);
            }


        } catch (DashboardPopulatorException e) {
            log.error(e.getMessage(), e);
        }
    }

    private static void transferDirectoryContentToRegistry(File rootDirectory, Registry registry,
                                                           String rootPath, int tenantId)
            throws FileNotFoundException {

        try {
            File[] filesAndDirs = rootDirectory.listFiles();
            List<File> filesDirs = Arrays.asList(filesAndDirs);

            for (File file : filesDirs) {

                if (!file.isFile()) {
                    // This is a Directory add a new collection
                    // This path is used to store the file resource under registry
                    String directoryRegistryPath =
                            gadgetsPath + file.getAbsolutePath()
                                    .substring(rootPath.length()).replaceAll("[/\\\\]+", "/");
                    Collection newCollection = registry.newCollection();
                    registry.put(directoryRegistryPath, newCollection);

                    // recurse
                    transferDirectoryContentToRegistry(file, registry, rootPath, tenantId);
                } else {
                    // Add this to registry
                    addToRegistry(rootPath, file, tenantId);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

    }


    private static void addToRegistry(String rootPath, File file, int tenantId) {
        try {
            Registry registry = DashboardPopulatorContext.getRegistry(tenantId);

            // This path is used to store the file resource under registry
            String fileRegistryPath =
                    gadgetsPath + file.getAbsolutePath().substring(rootPath.length())
                            .replaceAll("[/\\\\]+", "/");

            // Adding the file to the Registry

            Resource fileResource = registry.newResource();
            String mediaType = MediaTypesUtils.getMediaType(file.getAbsolutePath());
            if (mediaType.equals("application/xml")) {
                fileResource.setMediaType("application/vnd.wso2-gadget+xml");
            } else {
                fileResource.setMediaType(mediaType);
            }
            fileResource.setContentStream(new FileInputStream(file));
            registry.put(fileRegistryPath, fileResource);

        } catch (DashboardPopulatorException e) {
            log.error(e.getMessage(), e);
        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        }
    }

    /**
     * This method populates dashboard layout using dashboard.xml
     * Tab Layout and default gadget lists are identified by this population
     *
     * @param tenantId
     * @return status of population
     */
    public static void PopulateDashboardLayout(int tenantId) {

        try {

            String dashboardDiskRoot = System.getProperty(ServerConstants.CARBON_HOME) + File
                    .separator + "repository" + File.separator + "resources" + File.separator + "dashboard";

            String dashboardConfigFile = dashboardDiskRoot + File.separator + "dashboard.xml";

            String serverName = CarbonUtils.getServerConfiguration().getFirstProperty("Name");

            UserRegistry registry = DashboardPopulatorContext.getRegistry(tenantId);

            // Set permission for anonymous read. We do it here because it should happen always in order
            // to support mounting a remote registry.
            AuthorizationManager accessControlAdmin =
                    registry.getUserRealm().getAuthorizationManager();

            if (!accessControlAdmin.isRoleAuthorized(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME, RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                    GadgetPopulator.SYSTEM_GADGETS_PATH, ActionConstants.GET)) {
                accessControlAdmin.authorizeRole(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME, RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                        GadgetPopulator.SYSTEM_GADGETS_PATH, ActionConstants.GET);
            }

            if (!registry.resourceExists(REGISTRY_SYSTEM_DASHBOARDS_ROOT + "/" + serverName)) {
                // Creating an OMElement from file
                File dashboardConfigXml = new File(dashboardConfigFile);

                if (dashboardConfigXml.exists()) {
                    FileReader dashboardConfigXmlReader = new FileReader(dashboardConfigXml);

                    // Restoring from file
                    registry.restore(REGISTRY_SYSTEM_DASHBOARDS_ROOT, dashboardConfigXmlReader);

                    log.info("Successfully populated the default Dashboard XML for tenant :" + tenantId);

                } else {
                    log.info("Couldn't find a Dashboard XML at '" + dashboardConfigFile +
                            "'. Giving up dashboard population for tenant : " + tenantId);
                }

                // This will prevent re-population of dashboard.xml and tabResources
                registry.put(REGISTRY_SYSTEM_DASHBOARDS_ROOT + "/" + serverName, registry.newResource());
            }

        } catch (Exception e) {
            String errorMsg = "Failed to populate Dashboard Layout (dashboard.xml) for tenant : " + tenantId;
            log.error(errorMsg, e);
        }

    }

}
