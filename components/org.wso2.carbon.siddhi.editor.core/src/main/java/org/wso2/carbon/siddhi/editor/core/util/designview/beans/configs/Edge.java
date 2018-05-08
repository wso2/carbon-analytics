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

package org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs;

import org.wso2.carbon.siddhi.editor.core.util.designview.constants.NodeType;
import org.wso2.carbon.siddhi.editor.core.util.designview.utilities.DesignGeneratorHelper;

/**
 * Represents a connection between two Siddhi app design view elements
 */
public class Edge {
    private String id;
    private String parentID;
    private NodeType parentType;
    private String childID;
    private NodeType childType;

    public Edge(String id, String parentID, NodeType parentType, String childID, NodeType childType) {
        this.id = id;
        this.parentID = parentID;
        this.parentType = parentType;
        this.childID = childID;
        this.childType = childType;
    }

    public String getId() {
        return id;
    }

    public String getParentID() {
        return parentID;
    }

    public NodeType getParentType() {
        return parentType;
    }

    public String getChildID() {
        return childID;
    }

    public NodeType getChildType() {
        return childType;
    }
}
