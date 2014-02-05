package org.wso2.carbon.bam.toolbox.deployer.util;

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
public class ToolBoxStatusDTO {
    private String[] deployedTools;
    private String[] toBeDeployedTools;
    private String[] toBeUndeployedTools;

    public ToolBoxStatusDTO(){
        deployedTools = new String[0];
        toBeDeployedTools = new String[0];
        toBeUndeployedTools = new String[0];
    }
    public String[] getDeployedTools() {
        return deployedTools;
    }

    public void setDeployedTools(String[] deployedTools) {
        this.deployedTools = deployedTools;
    }

    public String[] getToBeDeployedTools() {
        return toBeDeployedTools;
    }

    public void setToBeDeployedTools(String[] toBeDeployedTools) {
        this.toBeDeployedTools = toBeDeployedTools;
    }

    public String[] getToBeUndeployedTools() {
        return toBeUndeployedTools;
    }

    public void setToBeUndeployedTools(String[] toBeUndeployedTools) {
        this.toBeUndeployedTools = toBeUndeployedTools;
    }
}

