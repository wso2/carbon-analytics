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

package org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.windowfilterprojection;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.streamhandler.StreamHandlerConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.QueryInputConfig;

import java.util.List;

/**
 * Represents a Window | Filter | Projection QueryInputConfig, for Siddhi Query
 */
public class WindowFilterProjectionConfig extends QueryInputConfig {
    private String from;
    private List<StreamHandlerConfig> streamHandlerList;

    public WindowFilterProjectionConfig(String type,
                                        String from,
                                        List<StreamHandlerConfig> streamHandlerList) {
        super(type);
        this.from = from;
        this.streamHandlerList = streamHandlerList;
    }

    public String getFrom() {
        return from;
    }

    public List<StreamHandlerConfig> getStreamHandlerList() {
        return streamHandlerList;
    }
}
