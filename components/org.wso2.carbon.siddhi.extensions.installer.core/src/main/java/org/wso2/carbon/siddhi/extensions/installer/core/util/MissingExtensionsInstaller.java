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

package org.wso2.carbon.siddhi.extensions.installer.core.util;

import org.wso2.carbon.siddhi.extensions.installer.core.exceptions.ExtensionsInstallerException;
import org.wso2.carbon.siddhi.extensions.installer.core.execution.DependencyInstaller;
import org.wso2.carbon.siddhi.extensions.installer.core.execution.SiddhiAppExtensionUsageDetector;
import org.wso2.carbon.siddhi.extensions.installer.core.models.SiddhiAppStore;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Installs extensions that are used in Siddhi apps, but have not been installed.
 */
public class MissingExtensionsInstaller {

    private MissingExtensionsInstaller() {
        // Prevents Instantiation.
    }

    /**
     * Installs extensions that are used in Siddhi apps of the Siddhi app store,
     * but are not installed.
     *
     * @param siddhiAppStore      Siddhi app store.
     * @param usageDetector       Siddhi app extension usage detector.
     * @param dependencyInstaller Dependency installer.
     * @return Response of the installation.
     * @throws ExtensionsInstallerException Failed to install missing extensions.
     */
    public static Map<String, Map<String, Object>> installMissingExtensions(
        SiddhiAppStore siddhiAppStore, SiddhiAppExtensionUsageDetector usageDetector,
        DependencyInstaller dependencyInstaller) throws ExtensionsInstallerException {
        Map<String, Map<String, Object>> responses = new HashMap<>();
        for (Map.Entry<String, String> siddhiAppEntry : siddhiAppStore.getSiddhiApps().entrySet()) {
            String siddhiAppName = siddhiAppEntry.getKey();
            String siddhiAppBody = siddhiAppEntry.getValue();
            Set<String> nonInstalledExtensionKeys = getNotInstalledExtensionKeys(usageDetector, siddhiAppBody);
            if (!nonInstalledExtensionKeys.isEmpty()) {
                Map<String, Map<String, Object>> installationResponses =
                    dependencyInstaller.installDependenciesFor(nonInstalledExtensionKeys);
                Map<String, Object> response =
                    ResponseEntityCreator.createMissingExtensionsInstallationResponse(installationResponses);
                responses.put(siddhiAppName, response);
            }
        }
        return responses;
    }

    /**
     * Returns a set of names, of extensions - that are used in the given Siddhi app, but are not installed.
     *
     * @param usageDetector Siddhi app extension usage detector.
     * @param siddhiAppBody Body of the Siddhi app.
     * @return A set containing names of extensions - that are used in the given Siddhi app,
     * but are not installed.
     * @throws ExtensionsInstallerException Failed to get used extension statuses.
     */
    public static Set<String> getNotInstalledExtensionKeys(SiddhiAppExtensionUsageDetector usageDetector,
                                                           String siddhiAppBody) throws ExtensionsInstallerException {
        Map<String, Map<String, Object>> usedExtensions = usageDetector.getUsedExtensionStatuses(siddhiAppBody);
        return ResponseEntityCreator.extractNonInstalledExtensionKeys(usedExtensions);
    }
}
