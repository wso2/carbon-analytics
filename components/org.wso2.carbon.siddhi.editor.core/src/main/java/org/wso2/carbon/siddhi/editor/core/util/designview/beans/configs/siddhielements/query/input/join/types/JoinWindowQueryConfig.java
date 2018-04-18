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

package org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.types;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.JoinDirectionConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.join.JoinQueryConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.QueryInputTypes;

/**
 * Represents a Join Window Query, for Siddhi Query's input
 */
public class JoinWindowQueryConfig extends JoinQueryConfig {
    private JoinDirectionConfig left;
    private JoinDirectionConfig right;
    private String on;

    public JoinWindowQueryConfig(JoinDirectionConfig left,
                                 JoinDirectionConfig right,
                                 String on) {
        super(QueryInputTypes.JOIN_WINDOW);
        this.left = left;
        this.right = right;
        this.on = on;
    }

    public JoinDirectionConfig getLeft() {
        return left;
    }

    public JoinDirectionConfig getRight() {
        return right;
    }

    public String getOn() {
        return on;
    }
}
