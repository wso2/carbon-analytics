/*
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

package org.wso2.carbon.dashboard.bean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.carbon.dashboard.common.bean.Tab;


import java.util.Iterator;

/**
 * The bean represent all the dashboard props which need to be rendered in the FE
 */
public class DashboardContentBean {
    private String portalCss;
    private Tab[] tabs;
    private String[] defaultGadgetUrlSet;
    private boolean readOnlyMode;
    private String tabLayout;
    private String backendHttpPort;

    public String getBackendHttpPort() {
        return backendHttpPort;
    }

    public void setBackendHttpPort(String backendHttpPort) {
        this.backendHttpPort = backendHttpPort;
    }

    public String[] getDefaultGadgetUrlSet() {
        return defaultGadgetUrlSet;
    }

    public void setDefaultGadgetUrlSet(String[] defaultGadgetUrlSet) {
        this.defaultGadgetUrlSet = defaultGadgetUrlSet;
    }

    public String getPortalCss() {
        return portalCss;
    }

    public void setPortalCss(String portalCss) {
        this.portalCss = portalCss;
    }

    public boolean isReadOnlyMode() {
        return readOnlyMode;
    }

    public void setReadOnlyMode(boolean readOnlyMode) {
        this.readOnlyMode = readOnlyMode;
    }

    public Tab[] getTabs() {
        return tabs;
    }

    public void setTabs(Tab[] tabs) {
        this.tabs = tabs;
    }

    public String getTabLayout() {
        return tabLayout;
    }

    public void setTabLayout(String tabLayout) {
        this.tabLayout = tabLayout;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("portalCss", portalCss);
        json.put("readOnlyMode", readOnlyMode);
        json.put("tabLayout", tabLayout);
        json.put("backendHttpPort", backendHttpPort);

        JSONArray jsonItems = new JSONArray();
        if (defaultGadgetUrlSet != null) {
            for (String defaultGadgetUrl : defaultGadgetUrlSet) {
                jsonItems.put(defaultGadgetUrl);
            }
        }
        json.put("defaultGadgetUrlSet", jsonItems);
        JSONArray tabsJArray = new JSONArray();
        if (tabs != null) {
            for (Tab tab : tabs) {
                tabsJArray.put(tab.toJSONObject());
            }
        }
        json.put("tabs", tabsJArray);

        return json;
    }
}
