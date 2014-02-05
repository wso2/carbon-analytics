/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.utils.config;

import java.util.List;

public class CFConfigBean {
    private String granularity;
    private List<KeyPart> rowKeyParts;
    private String cfName;
    private boolean defaultCF;
    private boolean primaryCF;
    private String indexRowKey;

    public String getIndexRowKey() {
        return indexRowKey;
    }

    public void setIndexRowKey(String indexRowKey) {
        this.indexRowKey = indexRowKey;
    }

    public boolean isDefaultCF() {
        return defaultCF;
    }

    public void setDefaultCF(boolean defaultCF) {
        this.defaultCF = defaultCF;
    }

    public boolean isPrimaryCF() {
        return primaryCF;
    }

    public void setPrimaryCF(boolean primaryCF) {
        this.primaryCF = primaryCF;
    }

    public String getCfName() {
        return cfName;
    }

    public void setCfName(String cfName) {
        this.cfName = cfName;
    }

    public String getGranularity() {
        return granularity;
    }

    public void setGranularity(String granularity) {
        this.granularity = granularity;
    }

    public List<KeyPart> getRowKeyParts() {
        return rowKeyParts;
    }

    public void setRowKeyParts(List<KeyPart> rowKeyParts) {
        this.rowKeyParts = rowKeyParts;
    }
}

