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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.siddhi.extensions.installer.core.config.mapping.models.DependencyConfig;
import org.wso2.carbon.siddhi.extensions.installer.core.exceptions.ExtensionsInstallerException;
import org.wso2.carbon.siddhi.extensions.installer.core.execution.DependencyInstaller;
import org.wso2.carbon.siddhi.extensions.installer.core.execution.SiddhiAppExtensionUsageDetector;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Installs extensions that are used in Siddhi apps, but have not been installed.
 */
public class MissingExtensionsInstaller {

    private static final Logger LOGGER = LoggerFactory.getLogger(MissingExtensionsInstaller.class);

    private MissingExtensionsInstaller() {
        // Prevents Instantiation.
    }

    /**
     * Installs extensions that are used in the given Siddhi app, but have not been installed.
     *
     * @param siddhiAppBody       Body of a Siddhi app.
     * @param siddhiAppName       Name of the Siddhi app.
     * @param usageDetector       Siddhi app usage detector, which detects extension usages from the
     *                            Siddhi app.
     * @param dependencyInstaller Dependency installer, which installs dependencies related to extensions.
     * @throws ExtensionsInstallerException Failure occurred when installing missing extensions.
     */
    public static void installMissingExtensions(String siddhiAppBody,
                                                String siddhiAppName,
                                                SiddhiAppExtensionUsageDetector usageDetector,
                                                DependencyInstaller dependencyInstaller)
        throws ExtensionsInstallerException {
        Map<String, Map<String, Object>> usedExtensions = usageDetector.getUsedExtensionStatuses(siddhiAppBody);
        Set<String> nonInstalledExtensionKeys = ResponseEntityCreator.extractNonInstalledExtensionKeys(usedExtensions);
        if (!nonInstalledExtensionKeys.isEmpty()) {
            LOGGER.info(String.format(
                "Installing extensions: %s for Siddhi app: '%s'.", nonInstalledExtensionKeys, siddhiAppName));
            Map<String, Map<String, Object>> installationResponse =
                dependencyInstaller.installDependenciesFor(nonInstalledExtensionKeys);
            notifyIncompleteInstallations(installationResponse);
            notifyManuallyRequiredInstallations(installationResponse);
            LOGGER.warn(String.format(
                "Extensions installation finished for Siddhi app: '%s'. Please restart the server.", siddhiAppName));
        }
    }

    private static void notifyIncompleteInstallations(Map<String, Map<String, Object>> installationResponse) {
        Set<String> nonSuccessfulInstallations =
            ResponseEntityCreator.extractIncompleteInstallations(installationResponse);
        if (!nonSuccessfulInstallations.isEmpty()) {
            // Notify about installations, whose installation statuses are anything other than successful.
            LOGGER.error(String.format("Installations were not complete for extensions: %s.",
                nonSuccessfulInstallations));
        }
    }

    private static void notifyManuallyRequiredInstallations(Map<String, Map<String, Object>> installationResponse) {
        Map<String, List<DependencyConfig>> manuallyRequiredInstallations =
            ResponseEntityCreator.extractManuallyInstallableDependencies(installationResponse);
        if (!manuallyRequiredInstallations.isEmpty()) {
            // Notify about each extension's manually installable dependency.
            int extensionCounter = 1;
            StringBuilder manuallyInstallMessage = new StringBuilder();
            for (Map.Entry<String, List<DependencyConfig>> extensionEntry : manuallyRequiredInstallations.entrySet()) {
                // Gather extension's details.
                manuallyInstallMessage.append(extensionCounter);
                manuallyInstallMessage.append(". Extension: ");
                manuallyInstallMessage.append(extensionEntry.getKey());
                manuallyInstallMessage.append(System.lineSeparator());
                // Gather dependencies' details.
                manuallyInstallMessage.append("Dependencies:");
                manuallyInstallMessage.append(System.lineSeparator());
                for (DependencyConfig manuallyInstallableDependency : extensionEntry.getValue()) {
                    manuallyInstallMessage.append("- ");
                    manuallyInstallMessage.append(manuallyInstallableDependency.getRepresentableName());
                    manuallyInstallMessage.append(":");
                    manuallyInstallMessage.append(System.lineSeparator());
                    if (manuallyInstallableDependency.getDownload() != null) {
                        String instructions = manuallyInstallableDependency.getDownload().getInstructions();
                        if (instructions != null) {
                            instructions = instructions.replaceAll("<br/>", System.lineSeparator());
                            manuallyInstallMessage.append(instructions);
                            manuallyInstallMessage.append(System.lineSeparator());
                        }
                    }
                }
                manuallyInstallMessage.append(System.lineSeparator());
                extensionCounter++;
            }
            LOGGER.info(String.format(
                "Please follow the instructions and install the following dependencies manually:%n%n%s",
                manuallyInstallMessage.toString()));
        }
    }
}
