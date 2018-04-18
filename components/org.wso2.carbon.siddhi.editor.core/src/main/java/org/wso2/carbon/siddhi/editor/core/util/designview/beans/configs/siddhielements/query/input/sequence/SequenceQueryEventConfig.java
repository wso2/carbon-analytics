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

package org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.sequence;

// TODO: 4/4/18 add class comment
public abstract class SequenceQueryEventConfig {
    private String type;
    private String within;
    private boolean forEvery;

    public SequenceQueryEventConfig(String type) {
        this.type = type;
        this.within = "";
        this.forEvery = false;
    }

    public SequenceQueryEventConfig(String type, String within, boolean forEvery) {
        this.type = type;
        this.within = within;
        this.forEvery = forEvery;
    }

    public String getType() {
        return type;
    }

    public String getWithin() {
        return within;
    }

    public boolean isForEvery() {
        return forEvery;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setWithin(String within) {
        this.within = within;
    }

    public void setForEvery(boolean forEvery) {
        this.forEvery = forEvery;
    }
}
