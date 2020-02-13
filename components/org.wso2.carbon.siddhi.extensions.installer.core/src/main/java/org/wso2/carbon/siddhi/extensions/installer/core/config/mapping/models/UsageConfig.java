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

import java.util.Objects;

/**
 * Denotes a usage of an extension's dependency (jar file).
 * A 'usage' refers to an instance of the jar file, in a particular directory.
 * The directory path of the usage is decided based on the {@link UsedByType} and the {@link UsageType}, by the method:
 * {@link
 * org.wso2.carbon.siddhi.extensions.installer.core.util.ExtensionsInstallerUtils#getInstallationLocation(UsageConfig)}.
 * An extension's dependency can have one or many usages.
 */
public class UsageConfig {

    private UsageType type;
    private UsedByType usedBy;

    public UsageType getType() {
        return type;
    }

    public UsedByType getUsedBy() {
        return usedBy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UsageConfig that = (UsageConfig) o;
        return type == that.type && usedBy == that.usedBy;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, usedBy);
    }

}
