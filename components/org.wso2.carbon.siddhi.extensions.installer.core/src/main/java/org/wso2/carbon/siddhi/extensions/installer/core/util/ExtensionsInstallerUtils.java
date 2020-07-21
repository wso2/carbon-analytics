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
import org.wso2.carbon.siddhi.extensions.installer.core.config.mapping.models.UsageConfig;
import org.wso2.carbon.siddhi.extensions.installer.core.config.mapping.models.UsageType;
import org.wso2.carbon.siddhi.extensions.installer.core.config.mapping.models.UsedByType;
import org.wso2.carbon.siddhi.extensions.installer.core.constants.ExtensionsInstallerConstants;
import org.wso2.carbon.siddhi.extensions.installer.core.exceptions.ExtensionsInstallerException;
import org.wso2.carbon.siddhi.extensions.installer.core.models.enums.ExtensionInstallationStatus;
import org.wso2.carbon.siddhi.extensions.installer.core.models.enums.ExtensionUnInstallationStatus;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.PatternSyntaxException;

/**
 * Contains utility methods that are used in Siddhi Extensions Installer.
 */
public class ExtensionsInstallerUtils {

    private ExtensionsInstallerUtils() {
        // Prevents instantiation.
    }

    /**
     * Returns the extension which has the given id, from the given map of extension configurations.
     *
     * @param extensionId      Id of the extension.
     * @param extensionConfigs Configurations of all available extensions.
     * @return Configuration of the found extension.
     * @throws ExtensionsInstallerException No such extension was found.
     */
    public static ExtensionConfig findExtension(String extensionId, Map<String, ExtensionConfig> extensionConfigs)
        throws ExtensionsInstallerException {
        ExtensionConfig extensionConfig = extensionConfigs.get(extensionId);
        if (extensionConfig != null) {
            return extensionConfig;
        }
        throw new ExtensionsInstallerException(
            String.format("Configuration for extension: %s was not found.", extensionId));
    }

    /**
     * Returns the existing installation status of an extension,
     * based on the given counts of its installed dependencies, and whether the self jar of the extension is available.
     *
     * @param isSelfDependencyInstalled  Whether the self jar of the extension is available.
     * @param installedDependenciesCount Actual count of successfully installed dependencies.
     * @param expectedDependenciesCount  Expected count of successfully installed dependencies.
     * @return Installation status of the extension.
     */
    public static ExtensionInstallationStatus getExistingInstallationStatus(boolean isSelfDependencyInstalled,
                                                                            int installedDependenciesCount,
                                                                            int expectedDependenciesCount) {
        if (installedDependenciesCount == 0 || !isSelfDependencyInstalled) {
            return ExtensionInstallationStatus.NOT_INSTALLED;
        }
        if (installedDependenciesCount < expectedDependenciesCount) {
            return ExtensionInstallationStatus.PARTIALLY_INSTALLED;
        }
        return ExtensionInstallationStatus.INSTALLED;
    }

    /**
     * Returns the installation status of an extension after an installation,
     * based on the given counts of its installed dependencies.
     *
     * @param installedDependenciesCount Actual count of successfully installed dependencies.
     * @param expectedDependenciesCount  Expected count of successfully installed dependencies.
     * @return Installation status of the extension.
     */
    public static ExtensionInstallationStatus getInstallationStatus(int installedDependenciesCount,
                                                                    int expectedDependenciesCount) {
        if (installedDependenciesCount == 0) {
            return ExtensionInstallationStatus.NOT_INSTALLED;
        }
        if (installedDependenciesCount < expectedDependenciesCount) {
            return ExtensionInstallationStatus.PARTIALLY_INSTALLED;
        }
        return ExtensionInstallationStatus.INSTALLED;
    }

    /**
     * Returns the un-installation status of an extension (after an un-installation),
     * based on the given counts of its un-installed dependencies.
     *
     * @param uninstalledDependenciesCount Actual count of successfully un-installed dependencies.
     * @param expectedDependenciesCount    Expected count of successfully un-installed dependencies.
     * @return Un-installation status of the extension.
     */
    public static ExtensionUnInstallationStatus getUnInstallationStatus(int uninstalledDependenciesCount,
                                                                        int expectedDependenciesCount) {
        if (uninstalledDependenciesCount == 0) {
            return ExtensionUnInstallationStatus.NOT_UNINSTALLED;
        }
        if (uninstalledDependenciesCount < expectedDependenciesCount) {
            return ExtensionUnInstallationStatus.PARTIALLY_UNINSTALLED;
        }
        return ExtensionUnInstallationStatus.UNINSTALLED;
    }

    /**
     * Returns files of which, name matches the given regex pattern, from the given directory path.
     *
     * @param regexPattern  Regex pattern for file name.
     * @param directoryPath Path of the directory.
     * @return List of matching files.
     * @throws IOException            Failure occurred while walking the file tree.
     * @throws PatternSyntaxException The provided regex pattern is invalid.
     */
    public static List<Path> listMatchingFiles(String regexPattern, String directoryPath)
        throws IOException, PatternSyntaxException {
        final String PATH_MATCHER_SYNTAX = "regex:";
        Path path = Paths.get(directoryPath);
        final PathMatcher matcher = FileSystems.getDefault().getPathMatcher(PATH_MATCHER_SYNTAX + regexPattern);

		/*
		Ideally a single file will be returned,
		unless a jar with a different version (but with the same name) is manually placed in the directory.
		 */
        List<Path> matchingFiles = new ArrayList<>(1);
        FileVisitor<Path> matcherVisitor = new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attribs) {
                Path name = file.getFileName();
                if (matcher.matches(name)) {
                    matchingFiles.add(file);
                }
                return FileVisitResult.CONTINUE;
            }
        };
        Files.walkFileTree(path, matcherVisitor);
        return matchingFiles;
    }

    /**
     * Returns the directory path, where the jar for the given usage is initially put during an installation.
     *
     * @param usage Configuration of a dependency's usage.
     * @return Directory path for installation.
     * @throws ExtensionsInstallerException Invalid usage type and/or used by type.
     */
    public static String getInstallationLocation(UsageConfig usage) throws ExtensionsInstallerException {
        UsageType usageType = usage.getType();
        UsedByType usedBy = usage.getUsedBy();
        if (usageType != null && usedBy != null) {
            if (usedBy == UsedByType.RUNTIME) {
                if (usageType == UsageType.JAR) {
                    return ExtensionsInstallerConstants.RUNTIME_JARS_LOCATION;
                } else if (usageType == UsageType.BUNDLE) {
                    return ExtensionsInstallerConstants.RUNTIME_BUNDLES_LOCATION;
                }
                throw new ExtensionsInstallerException(
                    String.format("Invalid value: %s for usage type.", usageType));
            } else if (usedBy == UsedByType.SAMPLES) {
                if (usageType == UsageType.JAR) {
                    return ExtensionsInstallerConstants.SAMPLES_LIB_LOCATION;
                } else if (usageType == UsageType.BUNDLE) {
                    throw new ExtensionsInstallerException(
                        String.format("Usage type: %s is not applicable when used by: %s.",
                            UsageType.BUNDLE, UsedByType.SAMPLES));
                }
                throw new ExtensionsInstallerException(
                    String.format("Invalid value: %s for usage type.", usageType));
            }
        }
        throw new ExtensionsInstallerException(String.format("Invalid value: %s for 'usedBy' property.", usage));
    }

    /**
     * Returns the directory path, where the bundled jar for the given usage is finally available.
     *
     * @param usage Configuration of a dependency's usage.
     * @return Directory path of the bundled jar.
     * @throws ExtensionsInstallerException Invalid used by type.
     */
    public static String getBundleLocation(UsageConfig usage) throws ExtensionsInstallerException {
        UsedByType usedBy = usage.getUsedBy();
        if (usedBy != null) {
            if (usedBy == UsedByType.RUNTIME) {
                return ExtensionsInstallerConstants.RUNTIME_LIB_LOCATION;
            } else if (usedBy == UsedByType.SAMPLES) {
                return ExtensionsInstallerConstants.SAMPLES_LIB_LOCATION;
            }
        }
        throw new ExtensionsInstallerException(String.format("Invalid value: %s for 'usedBy' property.", usage));
    }

    public static boolean isSelfDependency(DependencyConfig dependency) {
        if (dependency.getName() != null) {
            return dependency.getName().toLowerCase().startsWith("siddhi-");
        }
        return false;
    }

}
