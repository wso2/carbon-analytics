package org.wso2.carbon.dashboard.gadgetrepopopulator;

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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.utils.Transaction;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.user.core.AuthorizationManager;
import org.wso2.carbon.user.core.UserStoreException;
import org.wso2.carbon.utils.ServerConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Arrays;
import java.util.List;


public class GadgetRepoPopulator {

    private static final Log log = LogFactory.getLog(GadgetRepoPopulator.class);

    public static final String SYSTEM_GS_GADGETS_PATH = "/repository/gadget-server/gadgets";

    private static final String REGISTRY_SYSTEM_GADGET_REPO_ROOT = "/repository/gadget-server/gadget-repo";

    public static void populateRepo(int tenantId) {
        try {

            String resourcesDiskRoot = System.getProperty(ServerConstants.CARBON_HOME) + File
                    .separator + "repository" + File.separator + "resources" + File.separator + "gadget-repo";

            String gadgetRepoConfigFile = resourcesDiskRoot + File.separator + "gadget-repo.xml";
            String gadgetsDiskLocation = resourcesDiskRoot + File.separator + "gadgets";

            // Check whether the system dashboard is already available if not populate
            Registry registry = null;
            try {
                registry = GadgetRepoPopulatorContext.getRegistry(tenantId);
            } catch (Throwable e) {
                throw new GadgetRepoPopulatorException("Exception occoured : " + e);
            }
            if (!registry.resourceExists(REGISTRY_SYSTEM_GADGET_REPO_ROOT)) {

                // Creating an OMElement from file
                File dashboardConfigXml = new File(gadgetRepoConfigFile);

                if (dashboardConfigXml.exists()) {
                    StAXOMBuilder sab = new StAXOMBuilder(new FileInputStream(gadgetRepoConfigFile));
                    OMElement dashboardsRootEl = sab.getDocument().getOMDocumentElement();

                    FileReader dashboardConfigXmlReader = new FileReader(dashboardConfigXml);

                    // Restoring from file
                    registry.restore(REGISTRY_SYSTEM_GADGET_REPO_ROOT, dashboardConfigXmlReader);

                    log.info("Successfully populated the default Gadget Repository.");

                } else {
                    log.info("Couldn't find a Dashboard at '" + gadgetRepoConfigFile +
                            "'. Giving up.");
                }
            }

            // Check whether Gadgets are stored. If not store
            if (!registry.resourceExists(GadgetRepoPopulator.SYSTEM_GS_GADGETS_PATH)) {

                File gadgetsDir = new File(gadgetsDiskLocation);
                if (gadgetsDir.exists()) {
                    GadgetRepoPopulator.beginFileTansfer(gadgetsDir, tenantId);

                    log.info("Successfully added default Gadgets to Registry.");
                } else {
                    log.info("Couldn't find contents at '" + gadgetsDiskLocation +
                            "'. Giving up.");
                }
            }

        } catch (Exception e) {
            log.debug("Failed to activate Gadget Repository Populator for Gadget Server bundle ", e);
        }
    }


    public static void beginFileTansfer(File rootDirectory, int tenantId) throws RegistryException {
        try {

            // Storing the root path for future reference
            String rootPath = rootDirectory.getAbsolutePath();

            Registry registry = GadgetRepoPopulatorContext.getRegistry(tenantId);

            UserRegistry uReg = (UserRegistry) registry;

            // Creating the default gadget collection resource
            Collection defaultGadgetCollection = registry.newCollection();
            // Set permission for annonymous read
            AuthorizationManager authorizationManager = uReg.getUserRealm().getAuthorizationManager();
            authorizationManager.authorizeRole(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME, RegistryConstants.CONFIG_REGISTRY_BASE_PATH +
                    SYSTEM_GS_GADGETS_PATH, ActionConstants.GET);

            /*authorizationManager.authorizeRole(RegistryConstants.GUESTS_ROLE,
                                             SYSTEM_GS_GADGETS_PATH, ActionConstants.GET);*/

            boolean transactionStarted = Transaction.isStarted();
            try {
                if (!transactionStarted) {
                    registry.beginTransaction();
                }
                registry.put(SYSTEM_GS_GADGETS_PATH, defaultGadgetCollection);

                transferDirectoryContentToRegistry(rootDirectory, registry, rootPath, tenantId);
                if (!transactionStarted) {
                    registry.commitTransaction();
                }
            } catch (Exception e) {
                if (!transactionStarted) {
                    registry.rollbackTransaction();
                }
                log.error(e.getMessage(), e);
            }


        } catch (GadgetRepoPopulatorException e) {
            log.error(e.getMessage(), e);
        } catch (UserStoreException e) {
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
                            SYSTEM_GS_GADGETS_PATH + file.getAbsolutePath()
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
            Registry registry = GadgetRepoPopulatorContext.getRegistry(tenantId);

            // This path is used to store the file resource under registry
            String fileRegistryPath =
                    SYSTEM_GS_GADGETS_PATH + file.getAbsolutePath().substring(rootPath.length())
                            .replaceAll("[/\\\\]+", "/");

            // Adding the file to the Registry
            Resource fileResource = registry.newResource();
            //fileResource.setMediaType(new MimetypesFileTypeMap().getContentType(file));
            fileResource.setMediaType("application/vnd.wso2-gadget+xml");

            fileResource.setContentStream(new FileInputStream(file));
            registry.put(fileRegistryPath, fileResource);

        } catch (GadgetRepoPopulatorException e) {
            log.error(e.getMessage(), e);
        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
        }
    }
}
