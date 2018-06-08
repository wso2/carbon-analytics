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

import java.util.Objects;

/**
 * Represents a connection between two Siddhi app design view elements
 */
public class Edge {
    private String id;
    private String parentId;
    private NodeType parentType;
    private String childId;
    private NodeType childType;

    public Edge(String id, String parentId, NodeType parentType, String childId, NodeType childType) {
        this.id = id;
        this.parentId = parentId;
        this.parentType = parentType;
        this.childId = childId;
        this.childType = childType;
    }

    public String getId() {
        return id;
    }

    public String getParentId() {
        return parentId;
    }

    public NodeType getParentType() {
        return parentType;
    }

    public String getChildId() {
        return childId;
    }

    public NodeType getChildType() {
        return childType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Edge edge = (Edge) o;
        return Objects.equals(id, edge.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
