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

package org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.sequence.event;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.query.input.sequence.SequenceQueryEventConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.constants.QueryInputTypes;

// TODO: 4/4/18 add class comment
public class AndOrSequenceEventConfig extends SequenceQueryEventConfig {
    private String leftStreamEventReference;
    private String leftStreamName;
    private String leftStreamFilter;
    private String connectedWith;
    private String rightStreamEventReference;
    private String rightStreamName;
    private String rightStreamFilter;

    public AndOrSequenceEventConfig() {
        super(QueryInputTypes.AND_OR_EVENT);
        leftStreamEventReference = "";
        leftStreamName = "";
        leftStreamFilter = "";
        connectedWith = "";
        rightStreamEventReference = "";
        rightStreamName = "";
        rightStreamFilter = "";
    }

    public AndOrSequenceEventConfig(String within,
                                    boolean forEvery,
                                    String leftStreamEventReference,
                                    String leftStreamName,
                                    String leftStreamFilter,
                                    String connectedWith,
                                    String rightStreamEventReference,
                                    String rightStreamName,
                                    String rightStreamFilter) {
        super(QueryInputTypes.AND_OR_EVENT, within, forEvery);
        this.leftStreamEventReference = leftStreamEventReference;
        this.leftStreamName = leftStreamName;
        this.leftStreamFilter = leftStreamFilter;
        this.connectedWith = connectedWith;
        this.rightStreamEventReference = rightStreamEventReference;
        this.rightStreamName = rightStreamName;
        this.rightStreamFilter = rightStreamFilter;
    }

    public String getLeftStreamEventReference() {
        return leftStreamEventReference;
    }

    public String getLeftStreamName() {
        return leftStreamName;
    }

    public String getLeftStreamFilter() {
        return leftStreamFilter;
    }

    public String getConnectedWith() {
        return connectedWith;
    }

    public String getRightStreamEventReference() {
        return rightStreamEventReference;
    }

    public String getRightStreamName() {
        return rightStreamName;
    }

    public String getRightStreamFilter() {
        return rightStreamFilter;
    }

    public void setLeftStreamEventReference(String leftStreamEventReference) {
        this.leftStreamEventReference = leftStreamEventReference;
    }

    public void setLeftStreamName(String leftStreamName) {
        this.leftStreamName = leftStreamName;
    }

    public void setLeftStreamFilter(String leftStreamFilter) {
        this.leftStreamFilter = leftStreamFilter;
    }

    public void setConnectedWith(String connectedWith) {
        this.connectedWith = connectedWith;
    }

    public void setRightStreamEventReference(String rightStreamEventReference) {
        this.rightStreamEventReference = rightStreamEventReference;
    }

    public void setRightStreamName(String rightStreamName) {
        this.rightStreamName = rightStreamName;
    }

    public void setRightStreamFilter(String rightStreamFilter) {
        this.rightStreamFilter = rightStreamFilter;
    }
}
