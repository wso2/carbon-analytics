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

package org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.streamhandler.StreamHandlerConfig;

import java.util.List;

/**
 * Represents a Join element of Join QueryInputConfig
 */
public class JoinElementConfig {
    private String type;
    private String from;
    private List<StreamHandlerConfig> streamHandlerList;
    private String as;
    private boolean isUnidirectional;

    public JoinElementConfig() {
    }

    public JoinElementConfig(String type,
                             String from,
                             List<StreamHandlerConfig> streamHandlerList,
                             String as,
                             boolean isUnidirectional) {
        this.type = type;
        this.from = from;
        this.streamHandlerList = streamHandlerList;
        this.as = as;
        this.isUnidirectional = isUnidirectional;
    }

    public String getType() {
        return type;
    }

    public String getFrom() {
        return from;
    }

    public List<StreamHandlerConfig> getStreamHandlerList() {
        return streamHandlerList;
    }

    public String getAs() {
        return as;
    }

    public boolean isUnidirectional() {
        return isUnidirectional;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setStreamHandlerList(List<StreamHandlerConfig> streamHandlerList) {
        this.streamHandlerList = streamHandlerList;
    }

    public void setAs(String as) {
        this.as = as;
    }

    public void setUnidirectional(boolean unidirectional) {
        isUnidirectional = unidirectional;
    }
}
