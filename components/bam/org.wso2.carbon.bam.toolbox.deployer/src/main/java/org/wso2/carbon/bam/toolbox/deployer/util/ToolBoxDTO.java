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


package org.wso2.carbon.bam.toolbox.deployer.util;

import org.wso2.carbon.bam.toolbox.deployer.exception.BAMComponentNotFoundException;
import org.wso2.carbon.bam.toolbox.deployer.exception.BAMToolboxDeploymentException;

import java.util.ArrayList;


public class ToolBoxDTO {
    private String name;
    private ArrayList<AnalyzerScriptDTO> analtytics;
    private ArrayList<DashBoardTabDTO> dashboardTabs;
    private ArrayList<JasperTabDTO> jasperTabs;
    private ArrayList<StreamDefnDTO> dataStreamDefs;
    private String scriptsParentDirectory;
    private String gagetsParentDirectory;
    private String jasperParentDirectory;
    private String jaggeryAppParentDirectory;
    private ArrayList<JaggeryDashboardDTO> jaggeryDashboards;
    private String streamDefnParentDirectory;
    private String hotDeploymentRootDir;
    private String datasource;
    private String dsConfiguration;

    public ToolBoxDTO(String name) {
        this.name = name;
        analtytics = new ArrayList<AnalyzerScriptDTO>();
        dashboardTabs = new ArrayList<DashBoardTabDTO>();
        jasperTabs = new ArrayList<JasperTabDTO>();
        dataStreamDefs = new ArrayList<StreamDefnDTO>();
        jaggeryDashboards = new ArrayList<JaggeryDashboardDTO>();
        datasource = "";
    }

    public String getName() {
        return name;
    }

    public ArrayList<DashBoardTabDTO> getDashboardTabs() {
        return dashboardTabs;
    }

    public void setDashboardTabs(ArrayList<DashBoardTabDTO> dashboardTabs) {
        this.dashboardTabs = dashboardTabs;
    }

    public ArrayList<JasperTabDTO> getJasperTabs() {
        return jasperTabs;
    }

    public void setJasperTabs(ArrayList<JasperTabDTO> jasperTabs) {
        this.jasperTabs = jasperTabs;
    }

    public void setScriptNames(ArrayList<String> scriptNames) {
        this.analtytics = new ArrayList<AnalyzerScriptDTO>();
        for (String aScript : scriptNames) {
            this.analtytics.add(new AnalyzerScriptDTO(aScript));
        }
    }

    public ArrayList<JaggeryDashboardDTO> getJaggeryDashboards() {
        return jaggeryDashboards;
    }

    public void setJaggeryDashboards(ArrayList<JaggeryDashboardDTO> jaggeryDashboards) {
        this.jaggeryDashboards = jaggeryDashboards;
    }

    public ArrayList<AnalyzerScriptDTO> getAnaltytics() {
        return analtytics;
    }

    public String getScriptsParentDirectory() {
        return scriptsParentDirectory;
    }

    public void setScriptsParentDirectory(String scriptsParentDirectory) {
        this.scriptsParentDirectory = scriptsParentDirectory;
    }

    public String getGagetsParentDirectory() {
        return gagetsParentDirectory;
    }

    public void setGagetsParentDirectory(String gagetsParentDirectory) {
        this.gagetsParentDirectory = gagetsParentDirectory;
    }

    public void addDashboradTab(DashBoardTabDTO dashBoardTabDTO) {
        this.dashboardTabs.add(dashBoardTabDTO);
    }

    public void addJasperTab(JasperTabDTO jasperTabDTO) {
        this.jasperTabs.add(jasperTabDTO);
    }

    public String getJaggeryAppParentDirectory() {
        return jaggeryAppParentDirectory;
    }

    public void setJaggeryAppParentDirectory(String jaggeryAppParentDirectory) {
        this.jaggeryAppParentDirectory = jaggeryAppParentDirectory;
    }

    public String getHotDeploymentRootDir() {
        return hotDeploymentRootDir;
    }

    public void setHotDeploymentRootDir(String hotDeploymentRootDir) {
        this.hotDeploymentRootDir = hotDeploymentRootDir;
    }

    public String getJasperParentDirectory() {
        return jasperParentDirectory;
    }

    public void setJasperParentDirectory(String jasperParentDirectory) {
        this.jasperParentDirectory = jasperParentDirectory;
    }

    public void setDataSource(String dataSourceName) {
        this.datasource = dataSourceName;
    }

    public String getDataSource() {
        if (null == datasource) return "";
        return datasource;
    }

    public void setDataSourceConfiguration(String dsConfiguration) {
        this.dsConfiguration = dsConfiguration;
    }

    public String getDataSourceConfiguration() {
        if (null == dsConfiguration) return "";
        return dsConfiguration;
    }

    public ArrayList<StreamDefnDTO> getDataStreamDefs() {
        return dataStreamDefs;
    }

    public void setDataStreamDefs(ArrayList<String> dataStreamDefNames) {
        this.dataStreamDefs = new ArrayList<StreamDefnDTO>();
        for (String defn : dataStreamDefNames) {
            this.dataStreamDefs.add(new StreamDefnDTO(defn));
        }
    }

    public String getStreamDefnParentDirectory() {
        return streamDefnParentDirectory;
    }

    public void setStreamDefnParentDirectory(String streamDefnParentDirectory) {
        this.streamDefnParentDirectory = streamDefnParentDirectory;
    }

    public ArrayList<String> getScriptNames() {
        ArrayList<String> scripts = new ArrayList<String>();
        for (AnalyzerScriptDTO scriptDTO : analtytics) {
            scripts.add(scriptDTO.getName());
        }
        return scripts;
    }

    public void setCronForScript(String scriptName, String cron) throws
            BAMComponentNotFoundException, BAMToolboxDeploymentException {
        boolean found = false;
        if (null != scriptName && !scriptName.trim().equals("")) {
            for (AnalyzerScriptDTO scriptDTO : analtytics) {
                String aScript = scriptDTO.getName();
                if (null != aScript && !aScript.equals("")) {
                    if (aScript.equalsIgnoreCase(scriptName)) {
                        scriptDTO.setCron(cron);
                        found = true;
                        break;
                    }
                }
            }
        } else {
            throw new BAMToolboxDeploymentException("Analytic script file name is not specified!!");
        }
        if (!found) {
            throw new BAMComponentNotFoundException("Specified analytics script: " + scriptName + " is not found!!");
        }
    }

    public ArrayList<String> getStreamDefnNames() {
        ArrayList<String> names = new ArrayList<String>();
        for (StreamDefnDTO aStreamDefn : dataStreamDefs) {
            names.add(aStreamDefn.getFileName());
        }
        return names;
    }

    public void setPropertiesForStreamDefn(String streamDefnName, String username, String password,
                                           String secIndexes, String custIndexes, String fixedSearchProperties,
                                           boolean isIncrementalIndex, String arbitraryIndexes)
            throws BAMToolboxDeploymentException, BAMComponentNotFoundException {
        boolean found = false;
        if (null != streamDefnName && !streamDefnName.isEmpty()) {
            for (StreamDefnDTO aStreamDefn : dataStreamDefs) {
                if (aStreamDefn.getFileName().equalsIgnoreCase(streamDefnName)) {
                    aStreamDefn.setUsername(username);
                    aStreamDefn.setPassword(password);
                    aStreamDefn.setIndexes(secIndexes == null ? "" : secIndexes,
                            custIndexes == null ? "" : custIndexes,
                            fixedSearchProperties == null ? "" : fixedSearchProperties,
                            isIncrementalIndex,
                            arbitraryIndexes == null ? "" : arbitraryIndexes);
                    found = true;
                    break;
                }
            }
        } else {
            throw new BAMToolboxDeploymentException("Stream definition name is not specified...!!");
        }
        if (!found) {
            throw new BAMComponentNotFoundException("Specified stream definition: " + streamDefnName + " is not found!!");
        }
    }

    public void removeStreamDefn(String streamDefnFileName){
        for (int i=0; i<this.dataStreamDefs.size(); i++){
            if(this.dataStreamDefs.get(i).getFileName().equalsIgnoreCase(streamDefnFileName)){
                this.dataStreamDefs.remove(i);
                break;
            }
        }
    }
}
