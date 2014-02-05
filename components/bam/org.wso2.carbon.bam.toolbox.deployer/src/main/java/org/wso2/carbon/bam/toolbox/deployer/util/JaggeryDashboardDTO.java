package org.wso2.carbon.bam.toolbox.deployer.util;

import java.util.ArrayList;

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
public class JaggeryDashboardDTO {
    private String dashboardName;
    private ArrayList<JaggeryTabDTO> jaggeryTabs;


    public JaggeryDashboardDTO(){
        dashboardName = "";
        jaggeryTabs = new ArrayList<JaggeryTabDTO>();
    }


    public String getDashboardName() {
        return dashboardName;
    }

    public void setDashboardName(String dashboardName) {
        this.dashboardName = dashboardName;
    }


    public void addJaggeryTab(JaggeryTabDTO tabDTO){
       this.jaggeryTabs.add(tabDTO);
    }

    public boolean isValidDashboardConfig(){
        return this.jaggeryTabs.size() > 0;
    }

    public ArrayList<JaggeryTabDTO> getJaggeryTabs(){
        return jaggeryTabs;
    }
}
