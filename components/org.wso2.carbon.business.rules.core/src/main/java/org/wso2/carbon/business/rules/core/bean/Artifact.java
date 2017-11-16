/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.business.rules.core.bean;

/**
 * Represents an Artifact, which is a derived Template
 * i.e : SiddhiApp (Not considering Gadget & Dashboard for now)
 */
public class Artifact extends Template {
    public Artifact(String type, String content, String exposedStreamDefinition) {
        super(type, content, exposedStreamDefinition);
    }

    @Override
    public String toString() {
        return "Artifact{" +
                "\n" + super.toString() +
                "}";
    }
}
