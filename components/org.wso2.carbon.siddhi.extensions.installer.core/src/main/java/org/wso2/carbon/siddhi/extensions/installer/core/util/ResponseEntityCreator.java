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

import org.wso2.carbon.siddhi.extensions.installer.core.config.mapping.models.DependencyConfig;
import org.wso2.carbon.siddhi.extensions.installer.core.config.mapping.models.ExtensionConfig;
import org.wso2.carbon.siddhi.extensions.installer.core.execution.DependencyRetriever;
import org.wso2.carbon.siddhi.extensions.installer.core.models.enums.ExtensionInstallationStatus;
import org.wso2.carbon.siddhi.extensions.installer.core.models.enums.ExtensionUnInstallationStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains methods that organize data to create responses, for requests sent to
 * {@link org.wso2.carbon.siddhi.extensions.installer.core.internal.SiddhiExtensionsInstallerMicroservice}.
 */
public class ResponseEntityCreator {

    public static final String ACTION_TYPE_KEY = "actionType";
    public static final String ACTION_STATUS_KEY = "status";
    private static final String ACTION_COMPLETED_DEPENDENCIES_KEY = "completed";
    private static final String ACTION_FAILED_DEPENDENCIES_KEY = "failed";
    private static final String MANUALLY_INSTALLABLE_DEPENDENCIES_KEY = "manuallyInstall";
    private static final String AUTO_DOWNLOADABLE_DEPENDENCIES_KEY = "autoDownloadable";
    private static final String EXTENSION_INFO_KEY = "extensionInfo";
    private static final String EXTENSION_STATUS_KEY = "extensionStatus";
    private static final String DEPENDENCY_INFO_KEY = "dependencyInfo";
    private static final String DEPENDENCY_IS_INSTALLED_KEY = "isInstalled";

    private ResponseEntityCreator() {
        // Prevents instantiation.
    }

    /**
     * Creates response after installing dependencies for an extension, through:
     * {@link
     * org.wso2.carbon.siddhi.extensions.installer.core.execution.DependencyInstaller#installDependenciesFor(String)}.
     *
     * @param status                Status of the installation.
     * @param installedDependencies Dependencies that were successfully installed.
     * @param failedDependencies    Dependencies that were failed to install.
     * @return Information for the response.
     */
    public static Map<String, Object> createExtensionInstallationResponse(
        ExtensionConfig extension,
        ExtensionInstallationStatus status,
        List<DependencyConfig> installedDependencies,
        List<DependencyConfig> failedDependencies) {

        Map<String, Object> details = new HashMap<>();
        details.put(ACTION_TYPE_KEY, "INSTALL");
        details.put(ACTION_STATUS_KEY, status);
        putDependencyDetails(
            installedDependencies,
            failedDependencies,
            extension.getManuallyInstallableDependencies(),
            extension.getAutoDownloadableDependencies(),
            details);
        return details;
    }

    /**
     * Creates response after un-installing dependencies for an extension, through:
     * {@link
     * org.wso2.carbon.siddhi.extensions.installer.core.execution.DependencyInstaller#unInstallDependenciesFor(String)}.
     *
     * @param status                  Status of the un-installation.
     * @param uninstalledDependencies Dependencies that were successfully un-installed.
     * @param failedDependencies      Dependencies that were failed to un-install.
     * @return Information for the response.
     */
    public static Map<String, Object> createExtensionUnInstallationResponse(
        ExtensionUnInstallationStatus status,
        List<DependencyConfig> uninstalledDependencies,
        List<DependencyConfig> failedDependencies) {
        Map<String, Object> details = new HashMap<>();
        details.put(ACTION_TYPE_KEY, "UNINSTALL");
        details.put(ACTION_STATUS_KEY, status);
        putDependencyDetails(uninstalledDependencies, failedDependencies, null, null, details);
        return details;
    }

    /**
     * Creates response after retrieving extension statuses, through:
     * {@link DependencyRetriever#getAllExtensionStatuses()} or
     * {@link DependencyRetriever#getExtensionStatusFor(String)}.
     *
     * @param extension Configuration of the extension.
     * @param status    Installation status of the extension.
     * @return Information for the response.
     */
    public static Map<String, Object> createExtensionStatusResponse(ExtensionConfig extension,
                                                                    ExtensionInstallationStatus status) {
        Map<String, Object> details = new HashMap<>();
        details.put(EXTENSION_INFO_KEY, extension.getExtensionInfo());
        details.put(EXTENSION_STATUS_KEY, status);
        if (status != ExtensionInstallationStatus.INSTALLED) {
            putDependencyDetails(
                null,
                null,
                extension.getManuallyInstallableDependencies(),
                extension.getAutoDownloadableDependencies(),
                details);
        }
        return details;
    }

    private static void putDependencyDetails(List<DependencyConfig> completedDependencies,
                                             List<DependencyConfig> failedDependencies,
                                             List<DependencyConfig> manuallyInstallableDependencies,
                                             List<DependencyConfig> autoDownloadableDependencies,
                                             Map<String, Object> details) {
        if (completedDependencies != null && !completedDependencies.isEmpty()) {
            details.put(ACTION_COMPLETED_DEPENDENCIES_KEY, completedDependencies);
        }
        if (failedDependencies != null && !failedDependencies.isEmpty()) {
            details.put(ACTION_FAILED_DEPENDENCIES_KEY, failedDependencies);
        }
        if (manuallyInstallableDependencies != null && !manuallyInstallableDependencies.isEmpty()) {
            details.put(MANUALLY_INSTALLABLE_DEPENDENCIES_KEY, manuallyInstallableDependencies);
        }
        if (autoDownloadableDependencies != null && !autoDownloadableDependencies.isEmpty()) {
            details.put(AUTO_DOWNLOADABLE_DEPENDENCIES_KEY, autoDownloadableDependencies);
        }
    }

    /**
     * Creates response after retrieving dependency statuses, through:
     * {@link DependencyRetriever#getDependencyStatusesFor(String)}.
     *
     * @param dependency  Configuration of the dependency.
     * @param isInstalled Whether the dependency is installed or not.
     * @return Information for the response.
     */
    public static Map<String, Object> createDependencyStatusResponse(DependencyConfig dependency, boolean isInstalled) {
        Map<String, Object> details = new HashMap<>();
        details.put(DEPENDENCY_INFO_KEY, dependency);
        details.put(DEPENDENCY_IS_INSTALLED_KEY, isInstalled);
        return details;
    }

}
