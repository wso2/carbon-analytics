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

package org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.pattern.event;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.pattern.PatternQueryEventConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.QueryInputTypes;

// TODO: 4/4/18 add class comment
public class NotForPatternEventConfig extends PatternQueryEventConfig {
    private String streamName;
    private String filter;
    private String forDuration;

    /**
     * Constructs with default values
     */
    public NotForPatternEventConfig() {
        super(QueryInputTypes.NOT_FOR_EVENT);
        streamName = "";
        filter = "";
        forDuration = "";
    }

    public NotForPatternEventConfig(boolean forEvery,
                                    String streamName,
                                    String filter,
                                    String forDuration) {
        super(QueryInputTypes.NOT_FOR_EVENT, forEvery);
        this.streamName = streamName;
        this.filter = filter;
        this.forDuration = forDuration;
    }

    public String getStreamName() {
        return streamName;
    }

    public String getFilter() {
        return filter;
    }

    public String getForDuration() {
        return forDuration;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void setForDuration(String forDuration) {
        this.forDuration = forDuration;
    }
}
