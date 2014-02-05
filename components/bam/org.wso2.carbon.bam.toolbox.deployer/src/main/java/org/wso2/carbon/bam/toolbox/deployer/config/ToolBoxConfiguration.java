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

package org.wso2.carbon.bam.toolbox.deployer.config;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.bam.toolbox.deployer.exception.BAMToolboxDeploymentException;
import org.wso2.carbon.bam.toolbox.deployer.util.DashBoardTabDTO;
import org.wso2.carbon.bam.toolbox.deployer.util.JaggeryDashboardDTO;
import org.wso2.carbon.bam.toolbox.deployer.util.JaggeryTabDTO;
import org.wso2.carbon.bam.toolbox.deployer.util.JasperTabDTO;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;

public class ToolBoxConfiguration {

    private OMElement config;
    private String toolboxName;

    private static String SCRIPTS = "scripts";
    private static String SCRIPT = "script";
    private static String DASHBOARD = "dashboard";
    private static String JAGGERYDASHBOARDS = "jaggeryDashboards";
    private static String DISPLAYNAME = "displayName";
    private static String URL = "URL";
    private static String GADGETS = "gadgets";
    private static String GADGET = "gadget";
    private static String TAB = "tab";
    private static String JRXML_TAB = "jrxmlTab";
    private static String JRXML = "jrxml";
    private static String TAB_ID = "id";
    private static String TAB_NAME = "name";
    private static String DATASOURCE = "datasource";
    private static String CONFIGURATION = "configuration";


    private static final Log log = LogFactory.getLog(ToolBoxConfiguration.class);

    public ToolBoxConfiguration(String name) {
        this.toolboxName = name;
        createRootElement();
    }

    public ToolBoxConfiguration(String name, OMElement config) {
        this.toolboxName = name;
        this.config = config;
    }

    public void addDataSource(String datasource) {
        config.addAttribute(DATASOURCE, datasource, null);
    }

    public void addDataSourceConfiguration(String dsConfiguration) {
        config.addAttribute(CONFIGURATION, dsConfiguration, null);
    }

    public void addScript(String scriptName) throws BAMToolboxDeploymentException {
        OMElement scripts;
        Iterator iterator = config.getChildrenWithName(new QName(SCRIPTS));
        if (iterator != null && iterator.hasNext()) {
            scripts = (OMElement) iterator.next();
            OMFactory fac = OMAbstractFactory.getOMFactory();
            OMElement script = fac.createOMElement(new QName(SCRIPT));
            script.setText(scriptName);
            scripts.addChild(script);
        } else {
            throw new BAMToolboxDeploymentException("Cannot find " + SCRIPTS + "node in the " +
                    "configuration file when adding script name: " + scriptName + " for tool box :" + toolboxName);
        }
    }

    private void createRootElement() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        String ROOT = "toolbox";
        config = fac.createOMElement(new QName(ROOT));
        String NAME = "name";
        config.addAttribute(NAME, this.toolboxName, null);

        OMElement scripts = fac.createOMElement(new QName(SCRIPTS));
        config.addChild(scripts);

        OMElement dashboard = fac.createOMElement(new QName(DASHBOARD));
        config.addChild(dashboard);

        OMElement jaggeryDashboard = fac.createOMElement(new QName(JAGGERYDASHBOARDS));
        config.addChild(jaggeryDashboard);
    }

    private void addGadget(String gadgetName, OMElement element)
            throws BAMToolboxDeploymentException {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement gadgetElement = fac.createOMElement(new QName(GADGET));
        gadgetElement.setText(gadgetName);
        element.addChild(gadgetElement);
    }

    public void addGadgetToTab(int tabId, String gadgetName) throws BAMToolboxDeploymentException {
        OMElement dashboard;
        Iterator iterator = config.getChildrenWithName(new QName(DASHBOARD));
        OMElement tabElement = null;
        if (iterator != null && iterator.hasNext()) {
            dashboard = (OMElement) iterator.next();

            Iterator tabIterator = dashboard.getChildrenWithName(new QName(TAB));
            while (tabIterator.hasNext()) {
                OMElement aTab = (OMElement) tabIterator.next();
                String strTabId = aTab.getAttribute(new QName(TAB_ID)).getAttributeValue();
                if (strTabId != null) {
                    int aTabId = Integer.parseInt(strTabId);
                    if (aTabId == tabId) {
                        tabElement = aTab;
                        break;
                    }
                }
            }
            if (null == tabElement) {
                OMFactory fac = OMAbstractFactory.getOMFactory();
                tabElement = fac.createOMElement(new QName(TAB));
                tabElement.addAttribute(TAB_ID, String.valueOf(tabId), null);
                dashboard.addChild(tabElement);
            }

            addGadget(gadgetName, tabElement);
        } else {
            throw new BAMToolboxDeploymentException("Cannot find " + GADGETS + "node in the " +
                    "configuration file when adding gadget name: " + gadgetName + " for tool box :" + toolboxName);
        }
    }

    public void addJRXMLToTab(int tabId, String jrxmlName, String tabName)
            throws BAMToolboxDeploymentException {
        OMElement dashboard;
        Iterator iterator = config.getChildrenWithName(new QName(DASHBOARD));
        OMElement tabElement = null;
        if (iterator != null && iterator.hasNext()) {
            dashboard = (OMElement) iterator.next();

            Iterator tabIterator = dashboard.getChildrenWithName(new QName(JRXML_TAB));
            while (tabIterator.hasNext()) {
                OMElement aTab = (OMElement) tabIterator.next();
                String strTabId = aTab.getAttribute(new QName(TAB_ID)).getAttributeValue();
                if (strTabId != null) {
                    int aTabId = Integer.parseInt(strTabId);
                    if (aTabId == tabId) {
                        tabElement = aTab;
                        break;
                    }
                }
            }
            if (null == tabElement) {
                OMFactory fac = OMAbstractFactory.getOMFactory();
                tabElement = fac.createOMElement(new QName(JRXML_TAB));
                tabElement.addAttribute(TAB_ID, String.valueOf(tabId), null);
                tabElement.addAttribute(TAB_NAME, tabName, null);
                dashboard.addChild(tabElement);
            }

            OMFactory fac = OMAbstractFactory.getOMFactory();
            OMElement jrxmlElement = fac.createOMElement(new QName(JRXML));
            jrxmlElement.setText(jrxmlName);
            tabElement.addChild(jrxmlElement);

        } else {
            throw new BAMToolboxDeploymentException("Cannot find " + JRXML + "node in the " +
                    "configuration file when adding jrxml name: " +
                    jrxmlName + " for tool box :" + toolboxName);
        }
    }

    public void addJaggeryDashboards(ArrayList<JaggeryDashboardDTO> dashboardDTOs){
        Iterator iterator = config.getChildrenWithName(new QName(JAGGERYDASHBOARDS));
        if (iterator != null && iterator.hasNext()) {
            OMElement jaggeryDashboards = (OMElement) iterator.next();

            for (JaggeryDashboardDTO dashboardDTO : dashboardDTOs) {
                OMFactory fac = OMAbstractFactory.getOMFactory();
                OMElement dashboard = fac.createOMElement(new QName(DASHBOARD));
                dashboard.addAttribute(DISPLAYNAME, dashboardDTO.getDashboardName(), null);

                ArrayList<JaggeryTabDTO> tabs = dashboardDTO.getJaggeryTabs();

                for (JaggeryTabDTO aTab : tabs) {
                    OMElement tab = fac.createOMElement(new QName(TAB));
                    tab.addAttribute(DISPLAYNAME, aTab.getDisplayName(), null);
                    tab.addAttribute(URL, aTab.getUrl(), null);

                    dashboard.addChild(tab);
                }
                jaggeryDashboards.addChild(dashboard);
            }
        }
    }

    public String toString() {
        return this.config.toString();
    }

    public ArrayList<String> getScriptNames() throws BAMToolboxDeploymentException {
        if (null != config) {
            ArrayList<String> scripts = new ArrayList<String>();
            Iterator iterator = config.getChildrenWithName(new QName(SCRIPTS));
            if (iterator != null && iterator.hasNext()) {
                Iterator scriptIterator = ((OMElement) iterator.next()).getChildrenWithName(new QName(SCRIPT));
                if (scriptIterator != null && scriptIterator.hasNext()) {
                    while (scriptIterator.hasNext()) {
                        OMElement script = (OMElement) scriptIterator.next();
                        String scriptName = script.getText();
                        scripts.add(scriptName);
                    }
                    return scripts;
                } else {
                    return scripts;
                }
            } else {
                log.warn("No scripts are found for tool box:" + this.toolboxName);
                return scripts;
            }
        } else {
            log.error("Configuration has not been set for the tool box:" + this.toolboxName);
            throw new BAMToolboxDeploymentException("Configuration has not been set for the tool box:" + this.toolboxName);
        }
    }

    public String getDataSource() {
        return config.getAttribute(new QName(DATASOURCE)).getAttributeValue();
    }

    public String getDataSourceConfiguration() {
        return config.getAttribute(new QName(CONFIGURATION)).getAttributeValue();
    }

    public ArrayList<DashBoardTabDTO> getDashboardTabs() throws BAMToolboxDeploymentException {
        if (null != config) {
            ArrayList<DashBoardTabDTO> tabs = new ArrayList<DashBoardTabDTO>();
            Iterator dashboardIterator = config.getChildrenWithName(new QName(DASHBOARD));
            if (null != dashboardIterator && dashboardIterator.hasNext()) {
                OMElement dashBoadElement = (OMElement) dashboardIterator.next();
                Iterator tabIterator = dashBoadElement.getChildrenWithName(new QName(TAB));
                if (null != tabIterator && tabIterator.hasNext()) {
                    while (tabIterator.hasNext()) {
                        OMElement tabElement = (OMElement) tabIterator.next();
                        String tabId = tabElement.getAttributeValue(new QName(TAB_ID));

                        DashBoardTabDTO tabDTO = new DashBoardTabDTO();
                        tabDTO.setTabId(Integer.parseInt(tabId));

                        Iterator gadgetIterator = tabElement.getChildrenWithName(new QName(GADGET));
                        while (gadgetIterator.hasNext()) {
                            OMElement gadget = (OMElement) gadgetIterator.next();
                            tabDTO.addGadget(gadget.getText());
                        }
                        tabs.add(tabDTO);
                    }
                }
                return tabs;
            } else {
                log.warn("No dashboard configuration is found for" +
                        " tool box:" + this.toolboxName);
                return tabs;
            }
        } else {
            log.error("Configuration has not been set for the tool " +
                    "box:" + this.toolboxName);
            throw new BAMToolboxDeploymentException("Configuration has not been set " +
                    "for the tool box:" + this.toolboxName);
        }
    }

    public ArrayList<JasperTabDTO> getJasperTabs() throws BAMToolboxDeploymentException {
        if (null != config) {
            ArrayList<JasperTabDTO> tabs = new ArrayList<JasperTabDTO>();
            Iterator dashboardIterator = config.getChildrenWithName(new QName(DASHBOARD));
            if (null != dashboardIterator && dashboardIterator.hasNext()) {
                OMElement dashBoadElement = (OMElement) dashboardIterator.next();
                Iterator tabIterator = dashBoadElement.getChildrenWithName(new QName(JRXML_TAB));
                if (null != tabIterator && tabIterator.hasNext()) {
                    while (tabIterator.hasNext()) {
                        OMElement tabElement = (OMElement) tabIterator.next();
                        String tabId = tabElement.getAttributeValue(new QName(TAB_ID));
                        String tabName = tabElement.getAttributeValue(new QName(TAB_NAME));

                        JasperTabDTO tabDTO = new JasperTabDTO();
                        tabDTO.setTabId(Integer.parseInt(tabId));
                        tabDTO.setTabName(tabName);

                        Iterator jrxmlIterator = tabElement.getChildrenWithName(new QName(JRXML));
                        while (jrxmlIterator.hasNext()) {
                            OMElement jrxml = (OMElement) jrxmlIterator.next();
                            tabDTO.setJrxmlFileName(jrxml.getText());
                        }
                        tabs.add(tabDTO);
                    }
                }
                return tabs;
            } else {
                log.warn("No dashboard configuration is found for" +
                        " tool box:" + this.toolboxName);
                return tabs;
            }
        } else {
            log.error("Configuration has not been set for the tool " +
                    "box:" + this.toolboxName);
            throw new BAMToolboxDeploymentException("Configuration has not been set " +
                    "for the tool box:" + this.toolboxName);
        }
    }

    public ArrayList<JaggeryDashboardDTO> getJaggeryDashboards() throws BAMToolboxDeploymentException {
       if (null != config) {
            ArrayList<JaggeryDashboardDTO> jaggerydashboards = new ArrayList<JaggeryDashboardDTO>();
            Iterator dashboardsIterator = config.getChildrenWithName(new QName(JAGGERYDASHBOARDS));
            if (null != dashboardsIterator && dashboardsIterator.hasNext()) {
                OMElement jaggeryDashboards = (OMElement) dashboardsIterator.next();
                Iterator dashboardIterator = jaggeryDashboards.getChildrenWithName(new QName(DASHBOARD));
                if (null != dashboardIterator && dashboardIterator.hasNext()) {
                    while (dashboardIterator.hasNext()) {
                        OMElement dashboard = (OMElement) dashboardIterator.next();
                        String displayname = dashboard.getAttributeValue(new QName(DISPLAYNAME));

                        JaggeryDashboardDTO jaggeryDashboardDTO = new JaggeryDashboardDTO();
                        jaggeryDashboardDTO.setDashboardName(displayname);

                        Iterator tabIterator = dashboard.getChildrenWithName(new QName(TAB));
                        while (tabIterator.hasNext()) {
                            JaggeryTabDTO tabDTO = new JaggeryTabDTO();
                            OMElement tab = (OMElement) tabIterator.next();

                            tabDTO.setDisplayName(tab.getAttribute(new QName(DISPLAYNAME)).getAttributeValue());
                            tabDTO.setUrl(tab.getAttribute(new QName(URL)).getAttributeValue());
                            jaggeryDashboardDTO.addJaggeryTab(tabDTO);
                        }
                       jaggerydashboards.add(jaggeryDashboardDTO);
                    }
                }
                return jaggerydashboards;
            } else {
                log.debug("No jaggery dashboard configuration is found for" +
                        " tool box:" + this.toolboxName);
                return jaggerydashboards;
            }
        } else {
            log.error("Configuration has not been set for the tool " +
                    "box:" + this.toolboxName);
            throw new BAMToolboxDeploymentException("Configuration has not been set " +
                    "for the tool box:" + this.toolboxName);
        }
    }

}
