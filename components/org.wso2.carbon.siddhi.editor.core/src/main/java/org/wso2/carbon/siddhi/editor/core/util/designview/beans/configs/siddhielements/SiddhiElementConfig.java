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

package org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.CommentCodeSegment;

/**
 * Represents a Siddhi Element
 */
public abstract class SiddhiElementConfig {
    private String id;
    private int[] queryContextStartIndex;
    private int[] queryContextEndIndex;
    private CommentCodeSegment previousCommentSegment;

    public SiddhiElementConfig() {
    }

    public SiddhiElementConfig(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int[] getQueryContextStartIndex() {
        return new int[]{queryContextStartIndex[0], queryContextStartIndex[1]};
    }

    public void setQueryContextStartIndex(int[] queryContextStartIndex) {
        this.queryContextStartIndex = new int[]{queryContextStartIndex[0], queryContextStartIndex[1]};
    }

    public int[] getQueryContextEndIndex() {
        return new int[]{queryContextEndIndex[0], queryContextEndIndex[1]};
    }

    public void setQueryContextEndIndex(int[] queryContextEndIndex) {
        this.queryContextEndIndex = new int[]{queryContextEndIndex[0], queryContextEndIndex[1]};
    }

    public CommentCodeSegment getPreviousCommentSegment() {
        return previousCommentSegment;
    }

    public void setPreviousCommentSegment(CommentCodeSegment previousCommentSegment) {
        this.previousCommentSegment = previousCommentSegment;
    }
}
