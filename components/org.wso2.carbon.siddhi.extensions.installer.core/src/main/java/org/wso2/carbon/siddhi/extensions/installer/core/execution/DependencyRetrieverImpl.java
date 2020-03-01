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
import org.wso2.carbon.siddhi.extensions.installer.core.config.mapping.models.DependencyConfig;
import org.wso2.carbon.siddhi.extensions.installer.core.config.mapping.models.ExtensionConfig;
import org.wso2.carbon.siddhi.extensions.installer.core.config.mapping.models.UsageConfig;
import org.wso2.carbon.siddhi.extensions.installer.core.models.enums.ExtensionInstallationStatus;
import org.wso2.carbon.siddhi.extensions.installer.core.util.ResponseEntityCreator;
import org.wso2.carbon.siddhi.extensions.installer.core.util.ExtensionsInstallerUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads information related to the installation of extension dependencies. Used for retrieving extension statuses.
 */
public class DependencyRetrieverImpl implements DependencyRetriever {

    private final Map<String, ExtensionConfig> extensionConfigs;

    public DependencyRetrieverImpl(Map<String, ExtensionConfig> extensionConfigs) {
        this.extensionConfigs = extensionConfigs;
    }

    @Override
    public Map<String, Map<String, Object>> getAllExtensionStatuses() throws ExtensionsInstallerException {
        Map<String, Map<String, Object>> extensionStatuses = new HashMap<>();
        for (Map.Entry<String, ExtensionConfig> extension : extensionConfigs.entrySet()) {
            extensionStatuses.put(extension.getKey(), getExtensionStatus(extension.getValue()));
        }
        return extensionStatuses;
    }

    @Override
    public Map<String, Object> getExtensionStatusFor(String extensionId) throws ExtensionsInstallerException {
        ExtensionConfig extension = ExtensionsInstallerUtils.findExtension(extensionId, extensionConfigs);
        return getExtensionStatus(extension);
    }

    private Map<String, Object> getExtensionStatus(ExtensionConfig extension) throws ExtensionsInstallerException {
        return ResponseEntityCreator.createExtensionStatusResponse(
            extension, getExtensionInstallationStatus(extension));
    }

    private ExtensionInstallationStatus getExtensionInstallationStatus(ExtensionConfig extension)
        throws ExtensionsInstallerException {
        List<DependencyConfig> dependencies = extension.getDependencies();
        if (dependencies != null) {
            int installedDependenciesCount = 0;
            for (DependencyConfig dependency : dependencies) {
                if (isDependencyInstalled(dependency)) {
                    installedDependenciesCount++;
                }
            }
            return ExtensionsInstallerUtils
                .getInstallationStatus(installedDependenciesCount, dependencies.size());
        }
        throw new ExtensionsInstallerException("No dependencies were specified.");
    }

    private boolean isDependencyInstalled(DependencyConfig dependency) throws ExtensionsInstallerException {
        String lookupRegex = dependency.getLookupRegex();
        if (lookupRegex != null) {
            for (UsageConfig usage : dependency.getUsages()) {
                String usageDirectoryPath = ExtensionsInstallerUtils.getBundleLocation(usage);
                if (!doesUsageFileExist(lookupRegex, usageDirectoryPath)) {
                    return false;
                }
            }
            return true;
        } else {
            throw new ExtensionsInstallerException("Unable to find property: 'lookupRegex'.");
        }
    }

    private boolean doesUsageFileExist(String regexPattern, String directoryPath)
        throws ExtensionsInstallerException {
        try {
            List<Path> filteredFiles = ExtensionsInstallerUtils.listMatchingFiles(regexPattern, directoryPath);
            return !filteredFiles.isEmpty();
        } catch (IOException e) {
            throw new ExtensionsInstallerException(
                String.format("Failed when matching files for regex pattern: %s in directory: %s.",
                    regexPattern, directoryPath), e);
        }
    }

    @Override
    public List<Map<String, Object>> getDependencyStatusesFor(String extensionId) throws ExtensionsInstallerException {
        ExtensionConfig extension = ExtensionsInstallerUtils.findExtension(extensionId, extensionConfigs);

        List<Map<String, Object>> dependencyStatuses = new ArrayList<>();
        List<DependencyConfig> dependencies = extension.getDependencies();
        if (dependencies != null) {
            for (DependencyConfig dependency : dependencies) {
                dependencyStatuses.add(
                    ResponseEntityCreator.createDependencyStatusResponse(
                        dependency, isDependencyInstalled(dependency)));
            }
            return dependencyStatuses;
        }
        throw new ExtensionsInstallerException(
            String.format("No dependencies were specified for extension: %s.", extensionId));
    }

}
