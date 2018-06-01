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

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.QueryInputConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.query.QueryInputType;

/**
 * Represents a Join QueryInputConfig, for Siddhi Query
 */
public class JoinConfig extends QueryInputConfig {
    private String joinWith;
    private JoinElementConfig left;
    private String joinType;
    private JoinElementConfig right;
    private String on;
    private String within;
    private String per;

    public JoinConfig(String joinWith,
                      JoinElementConfig left,
                      String joinType,
                      JoinElementConfig right,
                      String on,
                      String within,
                      String per) {
        super(QueryInputType.JOIN.toString());
        this.joinWith = joinWith;
        this.left = left;
        this.joinType = joinType;
        this.right = right;
        this.on = on;
        this.within = within;
        this.per = per;
    }

    public String getJoinWith() {
        return joinWith;
    }

    public JoinElementConfig getLeft() {
        return left;
    }

    public String getJoinType() {
        return joinType;
    }

    public JoinElementConfig getRight() {
        return right;
    }

    public String getOn() {
        return on;
    }

    public String getWithin() {
        return within;
    }

    public String getPer() {
        return per;
    }

    public void setJoinWith(String joinWith) {
        this.joinWith = joinWith;
    }

    public void setLeft(JoinElementConfig left) {
        this.left = left;
    }

    public void setJoinType(String joinType) {
        this.joinType = joinType;
    }

    public void setRight(JoinElementConfig right) {
        this.right = right;
    }

    public void setOn(String on) {
        this.on = on;
    }

    public void setWithin(String within) {
        this.within = within;
    }

    public void setPer(String per) {
        this.per = per;
    }
}
