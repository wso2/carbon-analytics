/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.siddhi.extensions.installer.core.config.mapping.models;

import java.util.Set;

/**
 * Contains configuration of a dependency - which will be ultimately a jar file.
 */
public class DependencyConfig {

    private String name;
    private String version;
    private DownloadConfig download;
    private String lookupRegex;
    private Set<UsageConfig> usages;

    public boolean isAutoDownloadable() {
        if (download != null) {
            return download.isAutoDownloadable();
        }
        return false;
    }

    public DownloadConfig getDownload() {
        return download;
    }

    public String getLookupRegex() {
        return lookupRegex;
    }

    public Set<UsageConfig> getUsages() {
        return usages;
    }

    public String getRepresentableName() {
        return name + "-" + version;
    }

}
