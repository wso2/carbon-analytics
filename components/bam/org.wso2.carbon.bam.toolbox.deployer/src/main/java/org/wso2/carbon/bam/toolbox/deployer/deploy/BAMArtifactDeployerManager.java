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

package org.wso2.carbon.bam.toolbox.deployer.deploy;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.CarbonConstants;
import org.wso2.carbon.bam.toolbox.deployer.BAMToolBoxDeployerConstants;
import org.wso2.carbon.bam.toolbox.deployer.ServiceHolder;
import org.wso2.carbon.bam.toolbox.deployer.client.DashboardClient;
import org.wso2.carbon.bam.toolbox.deployer.client.DataPublisher;
import org.wso2.carbon.bam.toolbox.deployer.client.HiveScriptStoreClient;
import org.wso2.carbon.bam.toolbox.deployer.exception.BAMComponentNotFoundException;
import org.wso2.carbon.bam.toolbox.deployer.exception.BAMToolboxDeploymentException;
import org.wso2.carbon.bam.toolbox.deployer.util.*;
import org.wso2.carbon.registry.core.*;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.session.UserRegistry;
import org.wso2.carbon.registry.core.utils.MediaTypesUtils;
import org.wso2.carbon.user.core.AuthorizationManager;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BAMArtifactDeployerManager {

    private static BAMArtifactDeployerManager instance;

    private static final Log log = LogFactory.getLog(BAMArtifactDeployerManager.class);

    private static final String gadgetsPath = "/repository/gadget-server/gadgets";

    private static final String jasperPath = "/repository/dashboards/jasper";

    private BAMArtifactDeployerManager() {

    }

    public static BAMArtifactDeployerManager getInstance() throws BAMToolboxDeploymentException {
        if (instance == null) {
            instance = new BAMArtifactDeployerManager();
        }
        return instance;
    }

    private void deployScripts(ToolBoxDTO toolBoxDTO) throws BAMToolboxDeploymentException {
        String scriptParent = toolBoxDTO.getScriptsParentDirectory();
        ArrayList<String> scriptNameWithId = new ArrayList<String>();
        for (AnalyzerScriptDTO scriptDTO : toolBoxDTO.getAnaltytics()) {
            String aScript = scriptDTO.getName();
            String path = scriptParent + File.separator + aScript;
            File scriptFile = new File(path);

            String scriptName = scriptFile.getName();
            scriptName = scriptName.split("\\.")[0];
            String content = getContent(scriptFile);
            scriptName = scriptName + "_" + getRandomArtifactId();
            scriptNameWithId.add(scriptName);
            try {
                HiveScriptStoreClient scriptStoreClient = HiveScriptStoreClient.getInstance();
                String cron = scriptDTO.getCron();
                if (null != cron && cron.equals("")) cron = null;
                scriptStoreClient.saveHiveScript(scriptName, content, cron);

            } catch (BAMComponentNotFoundException e) {
                log.error(e.getMessage() + "Skipping deploying Hive scripts..");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        toolBoxDTO.setScriptNames(scriptNameWithId);

    }

    private static int getRandomArtifactId() {
        Random randomGenerator = new Random();
        return randomGenerator.nextInt(1000);
    }

    private void deployGadget(ToolBoxDTO toolBoxDTO, String username) {
        try {
            DashboardClient dashboardClient = DashboardClient.getInstance();
            for (DashBoardTabDTO tabDTO : toolBoxDTO.getDashboardTabs()) {
                if (tabDTO.getGadgets().size() > 0) {
                    int tabID = dashboardClient.addTab(username, tabDTO.getTabName());
                    tabDTO.setTabId(tabID);
                    for (String aGadget : tabDTO.getGadgets()) {
                        ServiceHolder.getGadgetRepoService().addGadgetEntryToRepo(aGadget.replaceAll(".xml", ""), "conf:/repository/gadget-server/gadgets/" + aGadget, "", null, null, null);
                        dashboardClient.addNewGadget(username, String.valueOf(tabID),
                                "/registry/resource/_system/config/repository/gadget-server/gadgets/" + aGadget);
                    }
                }
            }
        } catch (BAMComponentNotFoundException e) {
            log.warn(e.getMessage());
        } catch (Exception e) {
            log.warn("Deploying gadget is not successful.. Skipping deploying script..");
        }
    }


    private void undeployScript(String scriptName) {
        HiveScriptStoreClient scriptStoreClient = null;
        try {
            scriptStoreClient = HiveScriptStoreClient.getInstance();
            scriptStoreClient.deleteScript(scriptName);
        } catch (BAMComponentNotFoundException e) {
            log.warn(e.getMessage() + " Skipping un deploying scripts.");
        }

    }

    private void undeployTab(int tabId, String username) {
        DashboardClient dashboardClient = null;
        try {
            dashboardClient = DashboardClient.getInstance();
            dashboardClient.removeTab(username, tabId);
        } catch (BAMComponentNotFoundException e) {
            log.warn(e.getMessage() + " Skipping undeploying tab :" + tabId);
        }

    }

    private void undeployJasperTab(JasperTabDTO tabDTO, int tenantId)
            throws BAMToolboxDeploymentException {
        try {
            Registry registry = ServiceHolder.getRegistry(tenantId);

            String jrxmlPath = jasperPath + RegistryConstants.PATH_SEPARATOR +
                    tabDTO.getJrxmlFileName();
            registry.delete(jrxmlPath);

        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new BAMToolboxDeploymentException(e.getMessage(), e);
        }

    }

    public void deploy(ToolBoxDTO toolBoxDTO, int tenantId, String username)
            throws BAMToolboxDeploymentException {
        if (canDeployDataStreamDefn()) {
            if (null != toolBoxDTO.getStreamDefnParentDirectory()) {
                deployStreamDefn(toolBoxDTO);
            }
        }

        if (canDeployScripts()) {
            if (null != toolBoxDTO.getScriptsParentDirectory()) {
                deployScripts(toolBoxDTO);
            }
        }

        if (canDeployGadgets()) {
            if (null != toolBoxDTO.getGagetsParentDirectory()) {
                transferGadgetsFilesToRegistry(new File(toolBoxDTO.getGagetsParentDirectory()), tenantId);
                deployGadget(toolBoxDTO, username);
            }

            if (toolBoxDTO.getJasperParentDirectory() != null) { // Jasper is optional for the moment
                transferJRXMLFilesToRegistry(new File(toolBoxDTO.getJasperParentDirectory()),
                        tenantId);
            }
        }
        //since the jaggery aps can be there without the  gadget.xml also
        if (null != toolBoxDTO.getJaggeryAppParentDirectory()) {
            deployJaggeryApps(toolBoxDTO);
        }
    }

    private boolean canDeployDataStreamDefn() {
        if (null == ServiceHolder.getDataBridgeReceiverService()) {
            if (log.isDebugEnabled())
                log.debug("No DataReceiverService Found! Skipping deploying DataStream Definitions..");
            return false;
        } else return true;
    }


    private void deployStreamDefn(ToolBoxDTO toolBoxDTO)
            throws BAMToolboxDeploymentException {
        DataPublisher client = DataPublisher.getInstance();
        for (StreamDefnDTO defn : toolBoxDTO.getDataStreamDefs()) {
            String defnPath = toolBoxDTO.getStreamDefnParentDirectory() + File.separator + defn.getFileName();
            String streamDefn = getStreamDefinition(defnPath);
            client.createEventDefn(streamDefn, defn.getUsername(), defn.getPassword(), defn.getIndexes());
        }
    }


    private void deployJaggeryApps(ToolBoxDTO toolBoxDTO) {
        String jaggeryDeployementDir = toolBoxDTO.getHotDeploymentRootDir()
                + File.separator + BAMToolBoxDeployerConstants.JAGGERY_DEPLOYMENT_DIR;
        File deployDir = new File(jaggeryDeployementDir);
        if (!deployDir.exists()) {
            deployDir.mkdirs();
        }
        try {
            copyFolder(new File(toolBoxDTO.getJaggeryAppParentDirectory()), new File(jaggeryDeployementDir));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private ArrayList<String> getFilesInDir(String dirPath) {
        File dir = new File(dirPath);
        ArrayList<String> files = new ArrayList<String>();

        String[] children = dir.list();
        if (null != children) {
            for (String aChildren : children) {
                if (!new File(aChildren).isDirectory()) {
                    files.add(aChildren);
                }
            }
        }
        return files;
    }

    private void copyFolder(File src, File dest)
            throws IOException {

        if (src.isDirectory()) {
            //if directory not exists, create it
            if (!dest.exists()) {
                dest.mkdir();
            }

            //list all the directory contents
            String files[] = src.list();

            for (String file : files) {
                //construct the src and dest file structure
                File srcFile = new File(src, file);
                File destFile = new File(dest, file);
                //recursive copy
                copyFolder(srcFile, destFile);
            }

        } else {
            //if file, then copy it
            //Use bytes stream to support all file types
            InputStream in = new FileInputStream(src);
            OutputStream out = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];

            int length;
            //copy the file content in bytes
            while ((length = in.read(buffer)) > 0) {
                out.write(buffer, 0, length);
            }

            in.close();
            out.close();
        }
    }


    private boolean canDeployScripts() {
        try {
            Class serviceClass = Class.forName("org.wso2.carbon.analytics.hive.web.HiveScriptStoreService");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    private boolean canDeployGadgets() {
        if (null != ServiceHolder.getDashboardService() && null != ServiceHolder.getGadgetRepoService()) {
            return true;
        } else {
            if (log.isDebugEnabled())
                log.debug("Dashboard services are not found. Skipping deploying dashboard artifacts");
            return false;
        }
    }

    private void transferGadgetsFilesToRegistry(File rootDirectory, int tenantId)
            throws BAMToolboxDeploymentException {
        try {
            // Storing the root path for future reference
            String rootPath = rootDirectory.getAbsolutePath();

            Registry registry = ServiceHolder.getRegistry(tenantId);

            // Creating the default gadget collection resource

            try {
                registry.beginTransaction();
                if (!registry.resourceExists(gadgetsPath)) {
                    registry.put(gadgetsPath, registry.newCollection());
                }
                transferDirectoryContentToRegistry(rootDirectory, registry, rootPath, gadgetsPath,
                        tenantId);
                registry.commitTransaction();
            } catch (Exception e) {
                registry.rollbackTransaction();
                log.error(e.getMessage(), e);
            }


        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new BAMToolboxDeploymentException(e.getMessage(), e);
        }
    }

    private void transferJRXMLFilesToRegistry(File rootDirectory, int tenantId)
            throws BAMToolboxDeploymentException {
        try {
            // Storing the root path for future reference
            String rootPath = rootDirectory.getAbsolutePath();

            Registry registry = ServiceHolder.getRegistry(tenantId);

            // Creating the default gadget collection resource

            try {
                registry.beginTransaction();
                if (!registry.resourceExists(jasperPath)) {
                    registry.put(jasperPath, registry.newCollection());
                }
                transferDirectoryContentToRegistry(rootDirectory, registry, rootPath, jasperPath,
                        tenantId);
                registry.commitTransaction();
            } catch (Exception e) {
                registry.rollbackTransaction();
                log.error(e.getMessage(), e);
            }


        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new BAMToolboxDeploymentException(e.getMessage(), e);
        }
    }

    private static void transferDirectoryContentToRegistry(File rootDirectory, Registry registry,
                                                           String rootPath, String registryPath,
                                                           int tenantId)
            throws FileNotFoundException, BAMToolboxDeploymentException {

        try {
            File[] filesAndDirs = rootDirectory.listFiles();
            if(null != filesAndDirs){
                List<File> filesDirs = Arrays.asList(filesAndDirs);

                for (File file : filesDirs) {

                    if (!file.isFile()) {
                        // This is a Directory add a new collection
                        // This path is used to store the file resource under registry
                        String directoryRegistryPath =
                                registryPath + file.getAbsolutePath()
                                        .substring(rootPath.length()).replaceAll("[/\\\\]+", "/");
                        Collection newCollection = registry.newCollection();
                        registry.put(directoryRegistryPath, newCollection);

                        // recurse
                        transferDirectoryContentToRegistry(file, registry, rootPath, registryPath,
                                tenantId);
                    } else {
                        // Add this to registry
                        addToRegistry(rootPath, file, registryPath, tenantId);
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BAMToolboxDeploymentException(e.getMessage(), e);
        }

    }

    private static void addToRegistry(String rootPath, File file, String registryPath, int tenantId)
            throws BAMToolboxDeploymentException {
        try {
            Registry registry = ServiceHolder.getRegistry(tenantId);

            // This path is used to store the file resource under registry
            String fileRegistryPath =
                    registryPath + file.getAbsolutePath().substring(rootPath.length())
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

            //adding anon role to the gadget
            AuthorizationManager authorizationManager = ((UserRegistry) ServiceHolder.getRegistry(tenantId)).getUserRealm().getAuthorizationManager();
            authorizationManager.authorizeRole(CarbonConstants.REGISTRY_ANONNYMOUS_ROLE_NAME,
                    RegistryConstants.CONFIG_REGISTRY_BASE_PATH + fileRegistryPath, ActionConstants.GET);
            Registry reg = ServiceHolder.getGovernanceSystemRegistry(tenantId);
            Resource resource = reg.newResource();
            resource.setProperty("timestamp", Long.toString(System.currentTimeMillis()));
            registry.put("/repository/components/org.wso2.carbon.user.mgt/updatedTime", resource);

        } catch (RegistryException e) {
            log.error(e.getMessage(), e);
            throw new BAMToolboxDeploymentException(e.getMessage(), e);
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new BAMToolboxDeploymentException(e.getMessage(), e);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new BAMToolboxDeploymentException(e.getMessage(), e);
        }
    }

    public void undeploy(ToolBoxDTO toolBoxDTO, String username, int tenantId)
            throws BAMToolboxDeploymentException {
        if (canDeployScripts()) {
            for (String aScript : toolBoxDTO.getScriptNames()) {
                undeployScript(aScript);
            }
        }

        if (canDeployGadgets()) {
            for (DashBoardTabDTO tabDTO : toolBoxDTO.getDashboardTabs()) {
                int tabId = tabDTO.getTabId();
                undeployTab(tabId, username);
            }

            if (toolBoxDTO.getJasperTabs() != null) {
                for (JasperTabDTO tabDTO : toolBoxDTO.getJasperTabs()) {
                    undeployJasperTab(tabDTO, tenantId);
                }
            }
        }
    }


    private String getContent(File file) throws BAMToolboxDeploymentException {
        if (!file.isDirectory()) {
            try {
                FileInputStream fstream = new FileInputStream(file);
                DataInputStream in = new DataInputStream(fstream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String content = "";
                StringBuilder sb = new StringBuilder();
                String strLine;
                while ((strLine = br.readLine()) != null) {
                    sb.append(strLine);
                    sb.append("\n");
                }
                content = sb.toString();
                in.close();
                return content;
            } catch (FileNotFoundException e) {
                log.error("File not found " + file.getAbsolutePath(), e);
                throw new BAMToolboxDeploymentException("File not found " + file.getAbsolutePath(), e);
            } catch (IOException e) {
                log.error("Exception while reading the file: " + file.getAbsolutePath(), e);
                throw new BAMToolboxDeploymentException("Exception while reading the file: " + file.getAbsolutePath(), e);
            }
        } else {
            return "";
        }
    }


    private String getStreamDefinition(String filePath) throws BAMToolboxDeploymentException {
        try {
            FileInputStream fstream = null;
            fstream = new FileInputStream(filePath);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String strLine;
            String streamDefn = "";
            while ((strLine = br.readLine()) != null) {
                streamDefn += strLine;
            }
            in.close();
            return streamDefn;
        } catch (FileNotFoundException e) {
            log.error(e.getMessage(), e);
            throw new BAMToolboxDeploymentException(e.getMessage(), e);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new BAMToolboxDeploymentException(e.getMessage(), e);
        }

    }
}