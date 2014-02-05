package org.wso2.carbon.bam.toolbox.deployer.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.toolbox.deployer.BAMToolBoxDeployerConstants;
import org.wso2.carbon.bam.toolbox.deployer.exception.BAMToolboxDeploymentException;
import org.wso2.carbon.bam.toolbox.deployer.util.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

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
public class BAMArtifactProcessor {
    private static BAMArtifactProcessor instance;
    private static final Log log = LogFactory.getLog(BAMArtifactProcessor.class);

    private BAMArtifactProcessor() {
        //to avoid instantiation
    }

    public static BAMArtifactProcessor getInstance() {
        if (null == instance) {
            instance = new BAMArtifactProcessor();
        }
        return instance;
    }

    public String extractBAMArtifact(String bamArtifact, String destFolder)
            throws BAMToolboxDeploymentException {
        return unzipFolder(bamArtifact, destFolder);
    }

    private String unzipFolder(String zipFile, String destFolder)
            throws BAMToolboxDeploymentException {
        try {
            ZipFile bamArtifact = new ZipFile(zipFile);
            Enumeration<? extends ZipEntry> zipEnum = bamArtifact.entries();

            while (zipEnum.hasMoreElements()) {
                ZipEntry item = zipEnum.nextElement();

                if (item.isDirectory()) {
                    File newdir = new File(destFolder + File.separator + item.getName());
                    newdir.mkdir();
                } else {
                    String newfilePath = destFolder + File.separator + item.getName();
                    File newFile = new File(newfilePath);
                    if (!newFile.getParentFile().exists()) {
                        newFile.getParentFile().mkdirs();
                    }

                    InputStream is = bamArtifact.getInputStream(item);
                    FileOutputStream fos = new FileOutputStream(newfilePath);
                    int ch;
                    while ((ch = is.read()) != -1) {
                        fos.write(ch);
                    }
                    is.close();
                    fos.close();
                }
            }
            bamArtifact.close();
            File file = new File(bamArtifact.getName());
            return destFolder + File.separator + file.getName().replace("." + BAMToolBoxDeployerConstants.BAM_ARTIFACT_EXT, "");
        } catch (Exception e) {
            log.error("Exception while extracting the BAM artifact:" + zipFile, e);
            throw new BAMToolboxDeploymentException("Exception while extracting the BAM artifact:" + zipFile, e);
        }
    }


    public ToolBoxDTO getToolBoxDTO(String barDir) throws BAMToolboxDeploymentException {
        return createDTO(barDir);
    }


    private ToolBoxDTO createDTO(String barDir) throws BAMToolboxDeploymentException {
        File file = new File(barDir);
        ToolBoxDTO toolBoxDTO;
        String name = file.getName();

        toolBoxDTO = new ToolBoxDTO(name);
        setScriptsNames(toolBoxDTO, barDir);
        setGadgetNames(toolBoxDTO, barDir);
        setJaggeryAppNames(toolBoxDTO, barDir);
        setStreamDefnNames(toolBoxDTO, barDir);
        setJasperResourceNames(toolBoxDTO, barDir);
        return toolBoxDTO;
    }

    private void setScriptsNames(ToolBoxDTO toolBoxDTO, String barDir)
            throws BAMToolboxDeploymentException {
        String analyticsDir = barDir + File.separator + BAMToolBoxDeployerConstants.SCRIPTS_DIR;
        if (new File(analyticsDir).exists()) {
            ArrayList<String> scriptNames = getFilesInDirectory(analyticsDir);
            int i = 0;
            for (String aFile : scriptNames) {
                if (aFile.equalsIgnoreCase(BAMToolBoxDeployerConstants.ANALYZERS_PROPERTIES_FILE)) {
                    scriptNames.remove(i);
                    break;
                }
                i++;
            }
            if (scriptNames.size() == 0) {
                toolBoxDTO.setScriptsParentDirectory(null);
                log.warn("No scripts available in the specified directory");
            } else {
                toolBoxDTO.setScriptsParentDirectory(analyticsDir);
                toolBoxDTO.setScriptNames(scriptNames);
                setCronForAnalyticScripts(toolBoxDTO, analyticsDir);
            }
        } else {
            log.info("No Analytics found for toolbox :" + toolBoxDTO.getName());
        }
    }

    private void setCronForAnalyticScripts(ToolBoxDTO toolBoxDTO, String analyticDir) {
        String analyticsPropPath = analyticDir + File.separator + BAMToolBoxDeployerConstants.ANALYZERS_PROPERTIES_FILE;
        File analyticsProps = new File(analyticsPropPath);
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(analyticsProps));
            String scripts = props.getProperty(BAMToolBoxDeployerConstants.ANALYZER_SCRIPTS_VAR_NAME);
            if (null != scripts && !scripts.trim().equals("")) {
                scripts = scripts.trim();
                String[] scriptNames = scripts.split(",");
                if (scriptNames == null || scriptNames.length == 0) {
                    throw new BAMToolboxDeploymentException("Invalid toolbox artifact. No scripts found in analyzers.properties");
                } else {
                    boolean valid = false;
                    for (String aScriptVarName : scriptNames) {
                        if (!aScriptVarName.trim().equals("")) {
                            valid = true;
                            String scriptFileName = props.getProperty(BAMToolBoxDeployerConstants.ANALYZER_SCRIPT_PREFIX + "."
                                    + aScriptVarName.trim() + "." + BAMToolBoxDeployerConstants.ANALYZER_SCRIPT_FILE_NAME_SUFFIX);
                            if (null == scriptFileName || scriptFileName.trim().equals("")) {
                                log.error("No script file name specified for script reference name: " + aScriptVarName);
                            }
                            String cron = props.getProperty(BAMToolBoxDeployerConstants.ANALYZER_SCRIPT_PREFIX + "."
                                    + aScriptVarName.trim() + "." + BAMToolBoxDeployerConstants.ANALYZER_SCRIPT_CRON_SUFFIX);
                            if (null != cron && !cron.trim().equals("")) {
                                toolBoxDTO.setCronForScript(scriptFileName, cron.trim());
                            } else {
                                log.warn("No cron expression(s) are specified for script: " + scriptFileName);
                            }
                        }
                    }
                    if (!valid) {
                        toolBoxDTO.setGagetsParentDirectory(null);
                        log.error("Invalid toolbox artifact. No tab names " +
                                "found in dashboard.properties");
                        throw new BAMToolboxDeploymentException("Invalid toolbox artifact. No tab names " +
                                "found in dashboard.properties");

                    }
                }
            } else {
                toolBoxDTO.setGagetsParentDirectory(null);
                log.error("Invalid toolbox artifact. No property " + BAMToolBoxDeployerConstants.DASHBOARD_TABS_VAR_NAME +
                        "found in dashboard.properties");
                throw new BAMToolboxDeploymentException("Invalid toolbox artifact. No property " + BAMToolBoxDeployerConstants.DASHBOARD_TABS_VAR_NAME +
                        "found in dashboard.properties");
            }
        } catch (IOException e) {
            if (log.isDebugEnabled()) {
                log.debug("No " + BAMToolBoxDeployerConstants.ANALYZERS_PROPERTIES_FILE + " file found, scripts won't be scheduled");
            }
        }


    }


    private void setStreamDefnNames(ToolBoxDTO toolBoxDTO, String barDir)
            throws BAMToolboxDeploymentException {
        ArrayList<String> streamDefNames = getFilesInDirectory(barDir + File.separator + BAMToolBoxDeployerConstants.STREAM_DEFN_DIR);
        int i = 0;
        for (String aFile : streamDefNames) {
            if (aFile.equalsIgnoreCase(BAMToolBoxDeployerConstants.STREAM_DEFN_PROP_FILE)) {
                streamDefNames.remove(i);
                break;
            }
            i++;
        }
        if (streamDefNames.size() == 0) {
            toolBoxDTO.setStreamDefnParentDirectory(null);
            log.warn("No event stream(s) found in the specified directory");
        } else {
            String streamDefnDir = barDir + File.separator + BAMToolBoxDeployerConstants.STREAM_DEFN_DIR;
            toolBoxDTO.setStreamDefnParentDirectory(streamDefnDir);
            toolBoxDTO.setDataStreamDefs(streamDefNames);
            setPropertiesForDataStreams(toolBoxDTO, streamDefnDir);
        }
    }

    private void setPropertiesForDataStreams(ToolBoxDTO toolBoxDTO, String streamDefnDir) {
        String streamDefnPropPath = streamDefnDir + File.separator + BAMToolBoxDeployerConstants.STREAM_DEFN_PROP_FILE;
        File streamsProps = new File(streamDefnPropPath);
        Properties props = new Properties();
        try {
            props.load(new FileInputStream(streamsProps));

            String streamDefnVarString = props.getProperty(BAMToolBoxDeployerConstants.STREAMS_DEFN_VAR_NAME);
            if (null != streamDefnVarString && !streamDefnVarString.trim().equals("")) {
                streamDefnVarString = streamDefnVarString.trim();
                String[] streamDefnVars = streamDefnVarString.split(",");
                if (streamDefnVars == null || streamDefnVars.length == 0) {
                    throw new BAMToolboxDeploymentException("Invalid toolbox artifact. No scripts found in analyzers.properties");
                } else {
                    for (String aStreamVarName : streamDefnVars) {
                        if (!aStreamVarName.trim().equals("")) {
                            String streamFileName = props.getProperty(BAMToolBoxDeployerConstants.STREAM_DEFN_PREFIX + "."
                                    + aStreamVarName.trim() + "." + BAMToolBoxDeployerConstants.STREAM_DEFN_FILE_NAME_SUFFIX);
                            if (null == streamFileName || streamFileName.trim().equals("")) {
                                log.error("No stream definition file name specified for stream reference name: " + aStreamVarName);
                                toolBoxDTO.removeStreamDefn(streamFileName);
                            } else {
                                String streamUsername = props.getProperty(BAMToolBoxDeployerConstants.STREAM_DEFN_PREFIX + "."
                                        + aStreamVarName.trim() + "." + BAMToolBoxDeployerConstants.STREAM_DEFN_USERNAME_SUFFIX);
                                String streamSecIndexes = props.getProperty(BAMToolBoxDeployerConstants.STREAM_DEFN_PREFIX + "."
                                        + aStreamVarName.trim() + "." + BAMToolBoxDeployerConstants.STREAM_DEFN_SECONDARY_INDEXES);
                                String streamCustIndexes = props.getProperty(BAMToolBoxDeployerConstants.STREAM_DEFN_PREFIX + "."
                                        + aStreamVarName.trim() + "." + BAMToolBoxDeployerConstants.STREAM_DEFN_CUSTOM_INDEXES);
                                String fixedSearchProperties = props.getProperty(BAMToolBoxDeployerConstants.STREAM_DEFN_PREFIX + "."
                                        + aStreamVarName.trim() + "." + BAMToolBoxDeployerConstants.STREAM_DEFN_FIXED_SEARCH_PROPERTIES);
                                String incremntalIndexStr = props.getProperty(BAMToolBoxDeployerConstants.STREAM_DEFN_PREFIX + "."
                                        + aStreamVarName.trim() + "." +BAMToolBoxDeployerConstants.STREAM_DEFN_INCREMETAL_INDEX);
                                String arbitraryIndexes= props.getProperty(BAMToolBoxDeployerConstants.STREAM_DEFN_PREFIX + "."
                                        + aStreamVarName.trim() + "." + BAMToolBoxDeployerConstants.STREAM_DEFN_ARBITRARY_INDEXES);
                                if(null != incremntalIndexStr) incremntalIndexStr = incremntalIndexStr.toLowerCase();
                                boolean isEnableIncremental = Boolean.parseBoolean(incremntalIndexStr);
                                if (null != streamUsername && !streamUsername.trim().equals("")) {
                                    String streamPassword = props.getProperty(BAMToolBoxDeployerConstants.STREAM_DEFN_PREFIX + "."
                                            + aStreamVarName.trim() + "." + BAMToolBoxDeployerConstants.STREAM_DEFN_PASSWORD_SUFFIX);
                                    if (null != streamPassword && !streamPassword.isEmpty()) {
                                        toolBoxDTO.setPropertiesForStreamDefn(streamFileName, streamUsername, streamPassword,
                                                streamSecIndexes, streamCustIndexes, fixedSearchProperties, isEnableIncremental,
                                                arbitraryIndexes);
                                    } else {
                                        log.warn("No password specified for stream definition: "
                                                + streamFileName + ".Stream defn " + streamFileName + " won't be deployed");
                                        toolBoxDTO.removeStreamDefn(streamFileName);
                                    }
                                } else {
                                    log.warn("No username specified for stream definition: "
                                            + streamFileName + ".Stream defn " + streamFileName + " won't be deployed");
                                    toolBoxDTO.removeStreamDefn(streamFileName);
                                }
                            }
                        }
                    }
                }
            } else {
                log.warn("No stream defn variables defined in " + BAMToolBoxDeployerConstants.STREAM_DEFN_PROP_FILE + " file." +
                        " No stream definitions will be deployed");
                toolBoxDTO.setStreamDefnParentDirectory(null);
                toolBoxDTO.setDataStreamDefs(new ArrayList<String>());
            }
        } catch (IOException e) {
            log.warn(e.getMessage());
            toolBoxDTO.setStreamDefnParentDirectory(null);
            toolBoxDTO.setDataStreamDefs(new ArrayList<String>());
            log.warn("No stream definitions will be deployed");
        }


    }

    private void setJaggeryAppNames(ToolBoxDTO toolBoxDTO, String barDir)
            throws BAMToolboxDeploymentException {
        String dirName = barDir + File.separator + BAMToolBoxDeployerConstants.DASHBOARD_DIR
                + File.separator + BAMToolBoxDeployerConstants.JAGGERY_DIR;
        File jaggeryDir = new File(dirName);
        if (jaggeryDir.exists()) {
            toolBoxDTO.setJaggeryAppParentDirectory(dirName);
            File jaggeryMetaFile = new File(barDir + File.separator + BAMToolBoxDeployerConstants.DASHBOARD_DIR +
                    File.separator + BAMToolBoxDeployerConstants.GADGET_META_FILE);
            if (jaggeryMetaFile.exists()) {
                Properties properties = new Properties();
                try {
                    properties.load(new FileInputStream(jaggeryMetaFile));
                    setJaggeryAppsDashboardProperties(toolBoxDTO, properties);

                } catch (FileNotFoundException e) {
                    log.warn("No " + BAMToolBoxDeployerConstants.GADGET_META_FILE +
                            " found in dir:" + barDir + File.separator + BAMToolBoxDeployerConstants.DASHBOARD_DIR);
                    log.error("Jaggery Applications won't be added to the BAM dashboard");
                    toolBoxDTO.setJaggeryDashboards(new ArrayList<JaggeryDashboardDTO>());
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                    throw new BAMToolboxDeploymentException(e.getMessage(), e);
                }
            } else {
                toolBoxDTO.setJaggeryDashboards(new ArrayList<JaggeryDashboardDTO>());
            }
        } else {
            log.info("No jaggery artifacts found");
            toolBoxDTO.setJaggeryAppParentDirectory(null);
        }
    }

    private void setJaggeryAppsDashboardProperties(ToolBoxDTO toolBoxDTO, Properties properties) {
        String appsString = properties.getProperty(BAMToolBoxDeployerConstants.JAGGERY_APPS);
        if (appsString != null) {
            ArrayList<JaggeryDashboardDTO> dashboardDTOs = new ArrayList<JaggeryDashboardDTO>();
            String[] apps = appsString.split(",");
            for (String app : apps) {
                JaggeryDashboardDTO dashboardDTO = new JaggeryDashboardDTO();
                app = app.trim();
                String perfixProperty = BAMToolBoxDeployerConstants.JAGGERY_APPS + "." + app + ".";
                String mainDisplay = properties.getProperty(perfixProperty +
                        BAMToolBoxDeployerConstants.JAGGERY_APP_MAIN_DISPLAY_NANE);

                if (null != mainDisplay) {
                    dashboardDTO.setDashboardName(mainDisplay);
                } else {
                    dashboardDTO.setDashboardName(app);
                }

                String jaggeryTabsString = properties.getProperty(
                        perfixProperty + BAMToolBoxDeployerConstants.JAGGERY_APP_SUBS);

                if (null != jaggeryTabsString) {
                    String[] jaggeryTabs = jaggeryTabsString.split(",");
                    for (String aTab : jaggeryTabs) {
                        aTab = aTab.trim();
                        String tabPrefix = perfixProperty+aTab;
                        String displayName = properties.getProperty(tabPrefix+"."+
                                BAMToolBoxDeployerConstants.JAGGERY_APP_SUB_DISPLAY_NAME);
                        String url = properties.getProperty(tabPrefix+"."+BAMToolBoxDeployerConstants.JAGGERY_APP_SUB_URL);
                        if(null != displayName && null!= url){
                            JaggeryTabDTO tabDTO = new JaggeryTabDTO(displayName, url);
                            dashboardDTO.addJaggeryTab(tabDTO);
                        }
                    }
                }

                if(dashboardDTO.isValidDashboardConfig()){
                    dashboardDTOs.add(dashboardDTO);
                }
            }
            toolBoxDTO.setJaggeryDashboards(dashboardDTOs);
        }
    }


    private void setGadgetNames(ToolBoxDTO toolBoxDTO, String barDir)
            throws BAMToolboxDeploymentException {
        if (new File(barDir + File.separator + BAMToolBoxDeployerConstants.DASHBOARD_DIR
                + File.separator + BAMToolBoxDeployerConstants.GADGETS_DIR).exists()) {

            toolBoxDTO.setGagetsParentDirectory(barDir + File.separator + BAMToolBoxDeployerConstants.DASHBOARD_DIR
                    + File.separator + BAMToolBoxDeployerConstants.GADGETS_DIR);
            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream(barDir + File.separator + BAMToolBoxDeployerConstants.DASHBOARD_DIR +
                        File.separator + BAMToolBoxDeployerConstants.GADGET_META_FILE));
                setTabAndGadgetNames(toolBoxDTO, properties);

            } catch (FileNotFoundException e) {
                log.warn("No " + BAMToolBoxDeployerConstants.GADGET_META_FILE +
                        " found in dir:" + barDir + File.separator + BAMToolBoxDeployerConstants.DASHBOARD_DIR);
                log.error("Skipping installing dashboard artifacts..");
                toolBoxDTO.setGagetsParentDirectory(null);
                toolBoxDTO.setDashboardTabs(new ArrayList<DashBoardTabDTO>());
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new BAMToolboxDeploymentException(e.getMessage(), e);
            }
        } else {
            toolBoxDTO.setGagetsParentDirectory(null);
            toolBoxDTO.setDashboardTabs(new ArrayList<DashBoardTabDTO>());
        }
    }

    private void setJasperResourceNames(ToolBoxDTO toolBoxDTO, String barDir)
            throws BAMToolboxDeploymentException {
        String jasperDirectory = barDir + File.separator +
                BAMToolBoxDeployerConstants.DASHBOARD_DIR +
                File.separator + BAMToolBoxDeployerConstants.JASPER_DIR;
        if (new File(jasperDirectory).exists()) {
            toolBoxDTO.setJasperParentDirectory(barDir + File.separator +
                    BAMToolBoxDeployerConstants.DASHBOARD_DIR +
                    File.separator + BAMToolBoxDeployerConstants.JASPER_DIR);
            Properties properties = new Properties();
            try {
                properties.load(new FileInputStream(barDir + File.separator +
                        BAMToolBoxDeployerConstants.DASHBOARD_DIR +
                        File.separator +
                        BAMToolBoxDeployerConstants.JASPER_META_FILE));

                setJasperTabAndJrxmlNames(toolBoxDTO, properties);

                toolBoxDTO.setDataSource(properties.getProperty(BAMToolBoxDeployerConstants.DATASOURCE));
                toolBoxDTO.setDataSourceConfiguration(barDir + File.separator +
                        BAMToolBoxDeployerConstants.DASHBOARD_DIR +
                        File.separator +
                        properties.getProperty(BAMToolBoxDeployerConstants.
                                DATASOURCE_CONFIGURATION));
            } catch (FileNotFoundException e) {
                log.error("No " + BAMToolBoxDeployerConstants.JASPER_META_FILE +
                        " found in dir:" + barDir + File.separator + BAMToolBoxDeployerConstants.DASHBOARD_DIR, e);
                throw new BAMToolboxDeploymentException("No " + BAMToolBoxDeployerConstants.JASPER_META_FILE +
                        " found in dir:" + barDir + File.separator
                        + BAMToolBoxDeployerConstants.DASHBOARD_DIR, e);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
                throw new BAMToolboxDeploymentException(e.getMessage(), e);
            }
        } else {
            toolBoxDTO.setJasperParentDirectory(null);
            toolBoxDTO.setJasperTabs(new ArrayList<JasperTabDTO>());
        }
    }

    private void setJasperTabAndJrxmlNames(ToolBoxDTO toolBoxDTO, Properties props)
            throws BAMToolboxDeploymentException {
        String tabs = props.getProperty(BAMToolBoxDeployerConstants.JASPER_TABS_VAR_NAME);
        if (null != tabs && !tabs.trim().equals("")) {
            tabs = tabs.trim();
            String[] tabVarNames = tabs.split(",");
            if (tabVarNames == null || tabVarNames.length == 0) {
                throw new BAMToolboxDeploymentException("Invalid toolbox artifact. No tabs found in jasper.properties");
            } else {
                boolean valid = false;
                int tabId = 1;
                for (String aTabVarName : tabVarNames) {
                    if (!aTabVarName.trim().equals("")) {
                        valid = true;
                        JasperTabDTO tabDTO = new JasperTabDTO();
                        String tabName = props.getProperty(BAMToolBoxDeployerConstants.JASPER_TAB_PREFIX + "."
                                + aTabVarName.trim() + "." + BAMToolBoxDeployerConstants.TAB_NAME_SUFFIX);
                        if (null == tabName || tabName.trim().equals("")) {
                            tabName = aTabVarName;
                        }
                        tabDTO.setTabName(tabName.trim());
                        toolBoxDTO.addJasperTab(tabDTO);

                        String jrxml = props.getProperty(BAMToolBoxDeployerConstants.JASPER_TAB_PREFIX + "."
                                + aTabVarName.trim() + "." + BAMToolBoxDeployerConstants.JRXML_NAME_SUFFIX);
                        if (null != jrxml && !jrxml.trim().equals("")) {
                            tabDTO.setTabId(tabId);
                            tabDTO.setJrxmlFileName(jrxml.trim());
                        } else {
                            log.warn("No gadgets are specified for tab: " + tabName);
                        }
                        tabId++;
                    }
                }
                if (!valid) {
                    toolBoxDTO.setJasperParentDirectory(null);
                    log.error("Invalid toolbox artifact. No tab names " +
                            "found in jasper.properties");
                    throw new BAMToolboxDeploymentException("Invalid toolbox artifact. No tab names " +
                            "found in jasper.properties");

                }
            }
        } else {
            toolBoxDTO.setJasperParentDirectory(null);
            log.error("Invalid toolbox artifact. No property " + BAMToolBoxDeployerConstants.DASHBOARD_TABS_VAR_NAME +
                    "found in jasper.properties");
            throw new BAMToolboxDeploymentException("Invalid toolbox artifact. No property " + BAMToolBoxDeployerConstants.DASHBOARD_TABS_VAR_NAME +
                    "found in jasper.properties");
        }
    }

    private void setTabAndGadgetNames(ToolBoxDTO toolBoxDTO, Properties props)
            throws BAMToolboxDeploymentException {
        String tabs = props.getProperty(BAMToolBoxDeployerConstants.DASHBOARD_TABS_VAR_NAME);
        if (null != tabs && !tabs.trim().equals("")) {
            tabs = tabs.trim();
            String[] tabVarNames = tabs.split(",");
            if (tabVarNames == null || tabVarNames.length == 0) {
                throw new BAMToolboxDeploymentException("Invalid toolbox artifact. No tabs found in dashboard.properties");
            } else {
                boolean valid = false;
                for (String aTabVarName : tabVarNames) {
                    if (!aTabVarName.trim().equals("")) {
                        valid = true;
                        DashBoardTabDTO tabDTO = new DashBoardTabDTO();
                        String tabName = props.getProperty(BAMToolBoxDeployerConstants.DASHBOARD_TAB_PREFIX + "."
                                + aTabVarName.trim() + "." + BAMToolBoxDeployerConstants.TAB_NAME_SUFFIX);
                        if (null == tabName || tabName.trim().equals("")) {
                            tabName = aTabVarName;
                        }
                        tabDTO.setTabName(tabName);
                        toolBoxDTO.addDashboradTab(tabDTO);

                        String gadgets = props.getProperty(BAMToolBoxDeployerConstants.DASHBOARD_TAB_PREFIX + "."
                                + aTabVarName.trim() + "." + BAMToolBoxDeployerConstants.GADGET_NAME_SUFFIX);
                        if (null != gadgets && !gadgets.trim().equals("")) {
                            String[] gadgetNames = gadgets.trim().split(",");
                            for (String aGadget : gadgetNames) {
                                if (null != aGadget && !aGadget.trim().equals("")) {
                                    tabDTO.addGadget(aGadget.trim());
                                } else {
                                    log.warn("Empty gadget name found for tab: " + tabName);
                                }
                            }
                        } else {
                            log.warn("No gadgets are specified for tab: " + tabName);
                        }
                    }
                }
                if (!valid) {
                    toolBoxDTO.setGagetsParentDirectory(null);
                    log.error("Invalid toolbox artifact. No tab names " +
                            "found in dashboard.properties");
                    throw new BAMToolboxDeploymentException("Invalid toolbox artifact. No tab names " +
                            "found in dashboard.properties");

                }
            }
        } else {
            toolBoxDTO.setGagetsParentDirectory(null);
            log.error("Invalid toolbox artifact. No property " + BAMToolBoxDeployerConstants.DASHBOARD_TABS_VAR_NAME +
                    "found in dashboard.properties");
            throw new BAMToolboxDeploymentException("Invalid toolbox artifact. No property " + BAMToolBoxDeployerConstants.DASHBOARD_TABS_VAR_NAME +
                    "found in dashboard.properties");
        }
    }


    private ArrayList<String> getFilesInDirectory(String dirPath)
            throws BAMToolboxDeploymentException {
        File dir = new File(dirPath);
        ArrayList<String> files = new ArrayList<String>();

        if (dir.exists()) {
            String[] children = dir.list();
            if (null != children) {
                for (String aChildren : children) {
                    if (!new File(aChildren).isDirectory()) {
                        files.add(aChildren);
                    }
                }
            }
        }
        return files;
    }
}

