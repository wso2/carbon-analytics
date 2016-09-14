/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.analytics.dataservice.core.config;

import org.wso2.carbon.analytics.dataservice.core.Constants;

import javax.xml.bind.annotation.XmlElement;

/**
 * This class represents the configuration for the TaxonomyWriters cache implementation
 */
public class TaxonomyWriterCacheConfiguration {
    private String cacheType;
    private String LRUType;
    private int cacheSize;

    @XmlElement(name = "cacheType", defaultValue = "" + Constants.DEFAULT_TAXONOMY_WRITER_CACHE)
    public String getCacheType() {
        return cacheType;
    }

    public void setCacheType(String cacheType) {
        this.cacheType = cacheType;
    }

    @XmlElement(name = "LRUType", defaultValue = "" + Constants.DEFAULT_LRU_CACHE_TYPE)
    public String getLRUType() {
        return LRUType;
    }

    public void setLRUType(String LRUType) {
        this.LRUType = LRUType;
    }

    @XmlElement(name = "cacheSize", defaultValue = "" + Constants.DEFAULT_LRU_CACHE_SIZE)
    public int getCacheSize() {
        return cacheSize;
    }

    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }
}
