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

package org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators.query.input.types.patternsequencesupporters;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.streamhandler.StreamHandlerConfig;

import java.util.List;

public class StreamStateElementConfig implements StateElementConfig {
    private String streamReference;
    private String streamName;
    private List<StreamHandlerConfig> streamHandlerList;
    private String within;

    public String getStreamReference() {
        return streamReference;
    }

    public void setStreamReference(String streamReference) {
        this.streamReference = streamReference;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public List<StreamHandlerConfig> getStreamHandlerList() {
        return streamHandlerList;
    }

    public void setStreamHandlerList(List<StreamHandlerConfig> streamHandlerList) {
        this.streamHandlerList = streamHandlerList;
    }

    public String getWithin() {
        return within;
    }

    public void setWithin(String within) {
        this.within = within;
    }
}
