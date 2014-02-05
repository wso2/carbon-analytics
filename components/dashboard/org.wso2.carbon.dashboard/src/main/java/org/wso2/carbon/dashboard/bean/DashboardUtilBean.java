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

/**
 * The bean represent all the util props for the dashboard
 */
public class DashboardUtilBean {
    private boolean sessionValid;
    private boolean anonModeActive;
    private String[] gadgetUrlSetForUnSignedUser;
    private String backendHttpPort;

    public String getBackendHttpPort() {
        return backendHttpPort;
    }

    public void setBackendHttpPort(String backendHttpPort) {
        this.backendHttpPort = backendHttpPort;
    }

    public boolean isAnonModeActive() {
        return anonModeActive;
    }

    public void setAnonModeActive(boolean anonModeActive) {
        this.anonModeActive = anonModeActive;
    }

    public String[] getGadgetUrlSetForUnSignedUser() {
        return gadgetUrlSetForUnSignedUser;
    }

    public void setGadgetUrlSetForUnSignedUser(String[] gadgetUrlSetForUnSignedUser) {
        this.gadgetUrlSetForUnSignedUser = gadgetUrlSetForUnSignedUser;
    }

    public boolean isSessionValid() {
        return sessionValid;
    }

    public void setSessionValid(boolean sessionValid) {
        this.sessionValid = sessionValid;
    }
}
