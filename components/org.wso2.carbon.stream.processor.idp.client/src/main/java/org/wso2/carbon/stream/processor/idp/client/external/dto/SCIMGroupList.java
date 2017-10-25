/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.stream.processor.idp.client.external.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Model of SCIM group
 */
public class SCIMGroupList {

    @SerializedName("schemas")
    private List<String> schemas;
    @SerializedName("totalResults")
    private int totalResults;
    @SerializedName("startIndex")
    private int startIndex;
    @SerializedName("itemsPerPage")
    private int itemsPerPage;
    @SerializedName("Resources")
    private List<SCIMGroupResources> resources;
    
    public List<SCIMGroupResources> getResources() {
        return resources;
    }

    public void setResources(List<SCIMGroupResources> resources) {
        this.resources = resources;
    }

    public List<String> getSchemas() {
        return schemas;
    }

    public void setSchemas(List<String> schemas) {
        this.schemas = schemas;
    }

    public int getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getItemsPerPage() {
        return itemsPerPage;
    }

    public void setItemsPerPage(int itemsPerPage) {
        this.itemsPerPage = itemsPerPage;
    }

    /**
     * This inner class contains the model of resources of SCIM group
     */
    public static class SCIMGroupResources {

        private String id;
        private String displayName;

        public String getId() {
            return this.id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getDisplayName() {
            return this.displayName;
        }

        public void setDisplayName(String display) {
            this.displayName = display;
        }

    }

}
