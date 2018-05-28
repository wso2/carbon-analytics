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

package org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.patternsequence;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.streamhandler.StreamHandlerConfig;

import java.util.List;

/**
 * Represents a Condition element of Pattern | Sequence QueryInputConfig
 */
public class PatternSequenceConditionConfig {
    private String conditionId;
    private String streamName;
    private List<StreamHandlerConfig> streamHandlerList;

    public PatternSequenceConditionConfig(String conditionId, String streamName,
                                          List<StreamHandlerConfig> streamHandlerList) {
        this.conditionId = conditionId;
        this.streamName = streamName;
        this.streamHandlerList = streamHandlerList;
    }

    public String getConditionId() {
        return conditionId;
    }

    public String getStreamName() {
        return streamName;
    }

    public List<StreamHandlerConfig> getStreamHandlerList() {
        return streamHandlerList;
    }

    public void setConditionId(String conditionId) {
        this.conditionId = conditionId;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public void setStreamHandlerList(List<StreamHandlerConfig> streamHandlerList) {
        this.streamHandlerList = streamHandlerList;
    }
}
