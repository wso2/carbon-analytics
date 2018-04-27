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

package org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.OLD_REMOVE.sequence.event;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.OLD_REMOVE.sequence.SequenceQueryEventConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.OLD_REMOVE.sequence.event.countingsequence.CountingSequenceConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.query.input.PatternSequenceEventType;

// TODO: 4/4/18 add class comment
public class CountingSequenceEventConfig extends SequenceQueryEventConfig {
    private String eventReference;
    private String streamName;
    private String filter;
    private CountingSequenceConfig countingSequence;

    public CountingSequenceEventConfig() {
        super(PatternSequenceEventType.COUNTING.toString());
    }

    public CountingSequenceEventConfig(String within,
                                       boolean forEvery,
                                       String eventReference,
                                       String streamName,
                                       String filter,
                                       CountingSequenceConfig countingSequence) {
        super(PatternSequenceEventType.COUNTING.toString(), within, forEvery);
        this.eventReference = eventReference;
        this.streamName = streamName;
        this.filter = filter;
        this.countingSequence = countingSequence;
    }

    public String getEventReference() {
        return eventReference;
    }

    public String getStreamName() {
        return streamName;
    }

    public String getFilter() {
        return filter;
    }

    public CountingSequenceConfig getCountingSequence() {
        return countingSequence;
    }

    public void setEventReference(String eventReference) {
        this.eventReference = eventReference;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public void setCountingSequence(CountingSequenceConfig countingSequence) {
        this.countingSequence = countingSequence;
    }
}
