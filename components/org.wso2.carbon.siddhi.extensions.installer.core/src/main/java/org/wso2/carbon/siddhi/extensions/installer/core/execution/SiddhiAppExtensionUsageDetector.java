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

package org.wso2.carbon.siddhi.extensions.installer.core.execution;

import org.wso2.carbon.siddhi.extensions.installer.core.exceptions.ExtensionsInstallerException;
import org.wso2.carbon.siddhi.extensions.installer.core.models.SiddhiAppStore;

import java.util.Map;
import java.util.Set;

/**
 * Interface which describes methods, that are used to detect extension usages in a Siddhi app.
 */
public interface SiddhiAppExtensionUsageDetector {

    /**
     * Returns the installation statuses of all extensions,
     * that have been used in the Siddhi app - that has the given body.
     *
     * @param siddhiAppString Body of the Siddhi app, which will be used to detect extension usages.
     * @return Status of all the extensions used in the Siddhi app that has the given body.
     * @throws ExtensionsInstallerException Failed to retrieve installation status of an extension.
     */
    Map<String, Map<String, Object>> getUsedExtensionStatuses(String siddhiAppString)
        throws ExtensionsInstallerException;

    /**
     * Returns keys of extensions, that are used in Siddhi apps present in the given Siddhi app store.
     *
     * @param siddhiAppStore Siddhi app store which contains Siddhi apps.
     * @return Keys of used extensions.
     */
    Set<String> getUsedExtensionKeys(SiddhiAppStore siddhiAppStore);

}
