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

package org.wso2.carbon.siddhi.editor.core.util.designview.utilities;

/**
 * Has helper methods for Code to Design generation
 */
public class DesignGenerationHelper {

    /**
     * Avoids instantiation
     */
    private DesignGenerationHelper() {
    }

    /**
     * Generates Edge ID using the parent ID and the child ID, that are connected to this edge
     * @param parentID  ID of the parent node
     * @param childID   ID of the child node
     * @return          ID of the edge
     */
    public static String generateEdgeID(String parentID, String childID) {
        return String.format("%s_%s", parentID, childID);
    }
}
