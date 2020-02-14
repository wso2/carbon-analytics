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

package org.wso2.carbon.siddhi.extensions.installer.core.models.enums;

import org.wso2.carbon.siddhi.extensions.installer.core.config.mapping.models.UsageConfig;

/**
 * Denotes the status of an extension's installation.
 * This is used when retrieving an extension's status, as well as after installing an extension.
 * The status is based on {@link UsageConfig}.
 */
public enum ExtensionInstallationStatus {
    /**
     * Jars for all the extension usages are present.
     **/
    INSTALLED,

    /**
     * Jars for none of the extension usages are present.
     **/
    NOT_INSTALLED,

    /**
     * Jars for some of the extension usages are missing.
     **/
    PARTIALLY_INSTALLED,
}
