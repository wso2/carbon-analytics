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

/**
 * Represents a Join element of Join QueryInputConfig
 */
public class JoinElementConfig {
    private String type;
    private String from;
    private String filter;
    private JoinElementWindowConfig window;
    private String as;
    private boolean isUnidirectional;

    public JoinElementConfig() {
    }

    public JoinElementConfig(String type,
                             String from,
                             String filter,
                             JoinElementWindowConfig window,
                             String as,
                             boolean isUnidirectional) {
        this.type = type;
        this.from = from;
        this.filter = filter;
        this.window = window;
        this.as = as;
        this.isUnidirectional = isUnidirectional;
    }

    public String getType() {
        return type;
    }

    public String getFrom() {
        return from;
    }

    public String getFilter() {
        return filter;
    }

    public JoinElementWindowConfig getWindow() {
        return window;
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

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void setWindow(JoinElementWindowConfig window) {
        this.window = window;
    }

    public void setAs(String as) {
        this.as = as;
    }

    public void setUnidirectional(boolean unidirectional) {
        isUnidirectional = unidirectional;
    }
}
