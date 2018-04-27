/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.output.types.setattribute.SetAttributeConfig;

import java.util.List;

/**
 * todo class comment
 */
public class UpdateInsertIntoOutputConfig extends OutputConfig {
    private List<SetAttributeConfig> set;
    private String on;

    public UpdateInsertIntoOutputConfig() {
    }

    public UpdateInsertIntoOutputConfig(String eventType) {
        super(eventType);
    }

    public UpdateInsertIntoOutputConfig(String eventType, List<SetAttributeConfig> set, String on) {
        super(eventType);
        this.set = set;
        this.on = on;
    }

    public List<SetAttributeConfig> getSet() {
        return set;
    }

    public String getOn() {
        return on;
    }

    public void setSet(List<SetAttributeConfig> set) {
        this.set = set;
    }

    public void setOn(String on) {
        this.on = on;
    }
}
