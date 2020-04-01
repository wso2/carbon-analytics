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
import org.wso2.carbon.siddhi.extensions.installer.core.models.SiddhiAppExtensionUsage;
import org.wso2.carbon.siddhi.extensions.installer.core.models.enums.ExtensionInstallationStatus;
import org.wso2.carbon.siddhi.extensions.installer.core.models.enums.ExtensionUnInstallationStatus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Contains methods that organize data to create responses for requests sent to
 * {@link org.wso2.carbon.siddhi.extensions.installer.core.internal.SiddhiExtensionsInstallerMicroservice},
 * and to extract data from such responses.
 */
public class ResponseEntityCreator {

    public static final String ACTION_TYPE_KEY = "actionType";
    public static final String ACTION_STATUS_KEY = "status";
    private static final String DOES_SHARE_DEPENDENCIES_KEY = "doesShareDependencies";
    private static final String SHARES_WITH_KEY = "sharesWith";
    private static final String ACTION_COMPLETED_DEPENDENCIES_KEY = "completed";
    private static final String ACTION_FAILED_DEPENDENCIES_KEY = "failed";
    private static final String MANUALLY_INSTALLABLE_DEPENDENCIES_KEY = "manuallyInstall";
    private static final String AUTO_DOWNLOADABLE_DEPENDENCIES_KEY = "autoDownloadable";
    private static final String EXTENSION_INFO_KEY = "extensionInfo";
    private static final String EXTENSION_STATUS_KEY = "extensionStatus";
    private static final String DEPENDENCY_INFO_KEY = "dependencyInfo";
    private static final String DEPENDENCY_IS_INSTALLED_KEY = "isInstalled";
    private static final String EXTENSION_KEY = "extension";
    private static final String USAGES_KEY = "usages";

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
     * Creates response with information about dependency sharing extensions, through:
     * {@link DependencyRetriever#getDependencySharingExtensionsFor(String)}.
     *
     * @param dependencySharingExtensions Extensions that share same dependencies.
     * @return Information for the response.
     */
    public static Map<String, Object> createDependencySharingExtensionsResponse(
        Map<String, List<DependencyConfig>> dependencySharingExtensions) {
        Map<String, Object> details = new HashMap<>();
        details.put(DOES_SHARE_DEPENDENCIES_KEY, !dependencySharingExtensions.isEmpty());
        details.put(SHARES_WITH_KEY, dependencySharingExtensions);
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

    /**
     * Adds the given siddhi app usage and the corresponding extension status,
     * to the given map - which contains all the used extensions and their statuses.
     *
     * @param siddhiAppExtensionUsage Element of the Siddhi app, which uses the extension.
     * @param extensionId             Unique id of the extension.
     * @param extensionStatusResponse Installation status response of the extension.
     * @param usedExtensionStatuses   The map, which maintains all used extensions and their statuses.
     */
    public static void addUsedExtensionStatusResponse(SiddhiAppExtensionUsage siddhiAppExtensionUsage,
                                                      String extensionId,
                                                      Map<String, Object> extensionStatusResponse,
                                                      Map<String, Map<String, Object>> usedExtensionStatuses) {
        if (!usedExtensionStatuses.containsKey(extensionId)) {
            // This is the first usage of the extension.
            Map<String, Object> extensionAndUsages = new HashMap<>();
            List<SiddhiAppExtensionUsage> usages = new ArrayList<>();
            usages.add(siddhiAppExtensionUsage);
            extensionAndUsages.put(EXTENSION_KEY, extensionStatusResponse);
            extensionAndUsages.put(USAGES_KEY, usages);
            usedExtensionStatuses.put(extensionId, extensionAndUsages);
        } else {
            // This extension has been already used. Add this as the next usage.
            List<SiddhiAppExtensionUsage> usages =
                (List<SiddhiAppExtensionUsage>) (usedExtensionStatuses.get(extensionId).get(USAGES_KEY));
            if (usages != null) {
                usages.add(siddhiAppExtensionUsage);
            }
        }
    }

    /**
     * Extracts keys of extensions - whose installation status is anything other than installed,
     * from the given map of extensions used in a Siddhi app.
     *
     * @param usedExtensionStatuses A map that contains installation statuses of multiple extensions.
     * @return Keys of extensions - whose installation status is anything other than installed.
     */
    public static Set<String> extractNonInstalledExtensionKeys(Map<String, Map<String, Object>> usedExtensionStatuses) {
        return usedExtensionStatuses.entrySet().stream().filter(
            usedExtensionStatusEntry -> {
                Object extensionDetails = usedExtensionStatusEntry.getValue().get(EXTENSION_KEY);
                if (extensionDetails != null) {
                    Object extensionStatus = ((Map<String, Object>) extensionDetails).get(EXTENSION_STATUS_KEY);
                    if (extensionStatus != null) {
                        return !Objects.equals(extensionStatus, ExtensionInstallationStatus.INSTALLED);
                    }
                }
                /*
                Ideally we will not reach here.
                This would mean that, enough information to install a particular extension is not present.
                Therefore, the extension will not be considered for installation.
                 */
                return false;
            }).map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    /**
     * Extracts keys of extensions - whose installations were not successful, from the given map of
     * installation responses.
     *
     * @param installationResponses A map that contains installation responses of multiple extensions.
     * @return Keys of extensions - whose installations were not successful.
     */
    public static Set<String> extractIncompleteInstallations(
        Map<String, Map<String, Object>> installationResponses) {
        return installationResponses.entrySet().stream().filter(
            installationResponseEntry -> {
                Map<String, Object> installationResponse = installationResponseEntry.getValue();
                if (installationResponse.containsKey(ACTION_STATUS_KEY)) {
                    return !Objects.equals(
                        installationResponse.get(ACTION_STATUS_KEY), ExtensionInstallationStatus.INSTALLED);
                }
                /*
                Ideally we will not reach here.
                This would mean that, an extension's installation haven't returned a status.
                Therefore, the extension will be considered as not installed.
                 */
                return true;
            }).map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    /**
     * Extracts manually installable dependencies (if any) from each entry of the given map of installation responses.
     *
     * @param installationResponses A map that contains installation responses of multiple extensions.
     * @return A map that contains manually installable dependencies of extensions.
     */
    public static Map<String, List<DependencyConfig>> extractManuallyInstallableDependencies(
        Map<String, Map<String, Object>> installationResponses) {
        Map<String, List<DependencyConfig>> manuallyInstallableDependencies = new HashMap<>();
        for (Map.Entry<String, Map<String, Object>> installationResponseEntry : installationResponses.entrySet()) {
            if (installationResponseEntry.getValue().containsKey(MANUALLY_INSTALLABLE_DEPENDENCIES_KEY)) {
                manuallyInstallableDependencies.put(installationResponseEntry.getKey(),
                    (List<DependencyConfig>) (installationResponseEntry.getValue()
                        .get(MANUALLY_INSTALLABLE_DEPENDENCIES_KEY)));
            }
        }
        return manuallyInstallableDependencies;
    }

}
