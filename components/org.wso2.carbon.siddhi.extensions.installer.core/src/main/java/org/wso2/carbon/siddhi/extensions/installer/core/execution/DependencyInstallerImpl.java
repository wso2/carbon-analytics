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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.siddhi.extensions.installer.core.config.mapping.models.DependencyConfig;
import org.wso2.carbon.siddhi.extensions.installer.core.config.mapping.models.DownloadConfig;
import org.wso2.carbon.siddhi.extensions.installer.core.config.mapping.models.ExtensionConfig;
import org.wso2.carbon.siddhi.extensions.installer.core.config.mapping.models.UsageConfig;
import org.wso2.carbon.siddhi.extensions.installer.core.exceptions.ExtensionsInstallerException;
import org.wso2.carbon.siddhi.extensions.installer.core.models.enums.ExtensionInstallationStatus;
import org.wso2.carbon.siddhi.extensions.installer.core.models.enums.ExtensionUnInstallationStatus;
import org.wso2.carbon.siddhi.extensions.installer.core.util.ResponseEntityCreator;
import org.wso2.carbon.siddhi.extensions.installer.core.util.ExtensionsInstallerUtils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

/**
 * Handles installation and un-installation of extensions.
 */
public class DependencyInstallerImpl implements DependencyInstaller {

    private static final Logger LOGGER = LoggerFactory.getLogger(DependencyInstallerImpl.class);

    /**
     * Extension configurations, denoted by extension Id.
     */
    private final Map<String, ExtensionConfig> extensionConfigs;

    public DependencyInstallerImpl(Map<String, ExtensionConfig> extensionConfigs) {
        this.extensionConfigs = extensionConfigs;
    }

    @Override
    public Map<String, Map<String, Object>> installDependenciesFor(Set<String> extensionIds)
        throws ExtensionsInstallerException {
        Map<String, Map<String, Object>> installationResponses = new HashMap<>();
        for (String extensionId : extensionIds) {
            installationResponses.put(extensionId, installDependenciesFor(extensionId));
        }
        return installationResponses;
    }

    @Override
    public Map<String, Object> installDependenciesFor(String extensionId) throws ExtensionsInstallerException {
        List<DependencyConfig> completedDependencies = new ArrayList<>();
        List<DependencyConfig> failedDependencies = new ArrayList<>();

        ExtensionConfig extension = ExtensionsInstallerUtils.findExtension(extensionId, extensionConfigs);
        List<DependencyConfig> dependencies = extension.getDependencies();
        if (dependencies != null) {
            for (DependencyConfig dependency : dependencies) {
                if (dependency.isAutoDownloadable()) {
                    try {
                        installUsagesFor(dependency);
                        completedDependencies.add(dependency);
                    } catch (ExtensionsInstallerException e) {
                        failedDependencies.add(dependency);
                        LOGGER.error(String.format("Failed to install dependency: %s for extension: %s.",
                            dependency.getRepresentableName(), extensionId), e);
                    }
                }
            }
            ExtensionInstallationStatus status = ExtensionsInstallerUtils.getInstallationStatus(
                completedDependencies.size(), extension.getDependencies().size());
            return ResponseEntityCreator.createExtensionInstallationResponse(
                extension, status, completedDependencies, failedDependencies);
        }
        throw new ExtensionsInstallerException(String.format(
            "No dependencies were specified in extension: %s", extensionId));
    }

    private void installUsagesFor(DependencyConfig dependency) throws ExtensionsInstallerException {
        DownloadConfig download = dependency.getDownload();
        if (download != null) {
            try {
                URL downloadUrl = new URL(download.getUrl());
                String fileName = FilenameUtils.getName(downloadUrl.getPath());
                List<File> usageDestinations = generateUsageDestinations(dependency, fileName);
                if (ExtensionsInstallerUtils.isSelfDependency(dependency)) {
                    // Delete the jar of the self dependency if exists, to allow downloading latest version.
                    unInstallUsagesFor(dependency);
                }
                downloadOrCopyFilesTo(usageDestinations, downloadUrl);
            } catch (MalformedURLException e) {
                throw new ExtensionsInstallerException(
                    String.format("Invalid download URL: %s.", download.getUrl()), e);
            }
        } else {
            throw new ExtensionsInstallerException("Unable to find property: 'download'.");
        }
    }

    private List<File> generateUsageDestinations(DependencyConfig dependency, String fileName)
        throws ExtensionsInstallerException {
        Set<UsageConfig> usages = dependency.getUsages();
        if (usages != null) {
            List<File> fileDestinations = new ArrayList<>(usages.size());
            for (UsageConfig usage : usages) {
                fileDestinations.add(new File(ExtensionsInstallerUtils.getInstallationLocation(usage), fileName));
            }
            return fileDestinations;
        }
        throw new ExtensionsInstallerException("Unable to find property: 'usages'.");
    }

    private void downloadOrCopyFilesTo(List<File> fileDestinations, URL downloadUrl)
        throws ExtensionsInstallerException {
        Optional<File> existingFile = fileDestinations.stream().filter(File::exists).findAny();
        List<File> nonExistingFiles =
            fileDestinations.stream().filter(file -> !file.exists()).collect(Collectors.toList());
        if (!existingFile.isPresent()) {
            downloadAndCopyFile(downloadUrl, nonExistingFiles);
        } else {
            for (File nonExistingFile : nonExistingFiles) {
                copyExistingFile(existingFile.get(), nonExistingFile);
            }
        }
    }

    private void downloadAndCopyFile(URL downloadUrl, List<File> destinations) throws ExtensionsInstallerException {
        for (int i = 0; i < destinations.size(); i++) {
            File destination = destinations.get(i);
            if (i == 0) {
                // Download from the URL for the first destination.
                downloadFile(downloadUrl, destination);
            } else {
                // Copy from the first file (which has been downloaded) for rest of the destinations.
                copyExistingFile(destinations.get(0), destination);
            }
        }
    }

    private void downloadFile(URL downloadUrl, File destination) throws ExtensionsInstallerException {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Downloading file: %s from: %s to: %s.",
                    destination.getName(), downloadUrl.toString(), destination.getPath()));
            }
            FileUtils.copyURLToFile(downloadUrl, destination);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Downloaded file: %s.", destination.getName()));
            }
        } catch (IOException e) {
            throw new ExtensionsInstallerException(
                String.format("Failed to download file: %s from: %s to: %s.",
                    destination.getName(), downloadUrl.toString(), destination.getPath()), e);
        }
    }

    private void copyExistingFile(File existingFile, File destination) throws ExtensionsInstallerException {
        try {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Copying file: %s from: %s to: %s.",
                    destination.getName(), existingFile.getPath(), destination.getPath()));
            }
            FileUtils.copyFile(existingFile, destination);
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("Copied file: %s.", destination.getName()));
            }
        } catch (IOException e) {
            throw new ExtensionsInstallerException(
                String.format("Failed to copy file: %s from: %s to: %s.",
                    destination.getName(), existingFile.getPath(), destination.getPath()), e);
        }
    }

    @Override
    public Map<String, Object> unInstallDependenciesFor(String extensionId) throws ExtensionsInstallerException {
        List<DependencyConfig> completedDependencies = new ArrayList<>();
        List<DependencyConfig> failedDependencies = new ArrayList<>();

        ExtensionConfig extension = ExtensionsInstallerUtils.findExtension(extensionId, extensionConfigs);
        List<DependencyConfig> dependencies = extension.getDependencies();
        if (dependencies != null) {
            for (DependencyConfig dependency : dependencies) {
                boolean isUninstalled = unInstallUsagesFor(dependency);
                if (isUninstalled) {
                    completedDependencies.add(dependency);
                } else {
                    failedDependencies.add(dependency);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug(String.format("Failed to uninstall dependency: %s.",
                            dependency.getRepresentableName()));
                    }
                }
            }
            ExtensionUnInstallationStatus status =
                ExtensionsInstallerUtils.getUnInstallationStatus(
                    completedDependencies.size(), extension.getDependencies().size());
            return ResponseEntityCreator.createExtensionUnInstallationResponse(
                status, completedDependencies, failedDependencies);
        }
        throw new ExtensionsInstallerException(String.format(
            "No dependencies were specified in extension: %s", extensionId));
    }

    private boolean unInstallUsagesFor(DependencyConfig dependency) throws ExtensionsInstallerException {
        String lookupRegex = dependency.getLookupRegex();
        if (lookupRegex != null) {
            boolean hasFailures = false;
            for (UsageConfig usage : dependency.getUsages()) {
                // Delete jar(s) for the usage, from the directory where they are finally put to after conversion.
                boolean deletedInFinalDirectory =
                    deleteMatchingJars(lookupRegex, ExtensionsInstallerUtils.getBundleLocation(usage));
                // Delete jar(s) for the usage, from the initial download directory (before conversion).
                boolean deletedInInitialDirectory =
                    deleteMatchingJars(lookupRegex, ExtensionsInstallerUtils.getInstallationLocation(usage));

                hasFailures = !deletedInFinalDirectory || !deletedInInitialDirectory;
            }
            return !hasFailures;
        } else {
            throw new ExtensionsInstallerException("Unable to find property: 'lookupRegex'.");
        }
    }

    private boolean deleteMatchingJars(String lookupRegex, String directoryPath) {
        boolean allJarsDeleted = false;
        try {
            /*
            Ideally there will be just one matching jar,
            unless a jar with a different version (but with the same name) is manually placed in the directory.
             */
            List<Path> versionedJarFiles = ExtensionsInstallerUtils.listMatchingFiles(lookupRegex, directoryPath);
            if (!versionedJarFiles.isEmpty()) {
                allJarsDeleted = deleteJarFiles(versionedJarFiles);
            } else {
                // No matching jar found. Hence no need to delete.
                allJarsDeleted = true;
            }
        } catch (IOException e) {
            LOGGER.error(String.format("Failed to list matching files for lookup regex: %s.", lookupRegex));
        } catch (PatternSyntaxException e) {
            LOGGER.error(String.format("Lookup regex: %s is invalid.", lookupRegex));
        }
        return allJarsDeleted;
    }

    private boolean deleteJarFiles(List<Path> jarFiles) {
        boolean hasFailures = false;
        for (Path jarFile : jarFiles) {
            try {
                Files.deleteIfExists(jarFile);
            } catch (IOException e) {
                /*
                Where multiple files are present, deletion status will be failure even if there is a single failure.
                Remaining files are attempted to be deleted, without stopping after the very first failure.
                 */
                hasFailures = true;
                LOGGER.error(String.format("Failed to delete the file: %s.", jarFile.getFileName()), e);
            }
        }
        return !hasFailures;
    }

}
