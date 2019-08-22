/*
 * Copyright (c)  2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.siddhi.distribution.editor.core.internal;

import io.siddhi.distribution.editor.core.commons.configs.DockerConfigs;
import io.siddhi.distribution.editor.core.commons.request.ExportAppsRequest;
import io.siddhi.distribution.editor.core.exception.DockerGenerationException;
import io.siddhi.distribution.editor.core.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This class creates Docker artifacts with given Siddhi files.
 */
public class DockerUtils {

    private static final Logger log = LoggerFactory.getLogger(DockerUtils.class);
    private static final String CONFIG_BLOCK_TEMPLATE = "\\{\\{CONFIGURATION_BLOCK}}";
    private static final String CONFIG_PARAMETER_TEMPLATE = "\\{\\{CONFIGURATION_PARAMETER_BLOCK}}";
    private static final String JARS_BLOCK_TEMPLATE = "\\{\\{JARS_BLOCK}}";
    private static final String BUNDLES_BLOCK_TEMPLATE = "\\{\\{BUNDLES_BLOCK}}";
    private static final String ENV_BLOCK_TEMPLATE = "\\{\\{ENV_BLOCK}}";
    private static final String PRODUCT_VERSION_TEMPLATE = "\\{\\{PRODUCT_VERSION}}";
    private static final String CONFIG_BLOCK_VALUE =
            "COPY --chown=siddhi_user:siddhi_io \\$\\{CONFIG_FILE}/ \\$\\{USER_HOME}";
    private static final String CONFIG_PARAMETER_VALUE =
            ", \"-Dconfig=\\$CONFIG_FILE_PATH\"";
    private static final String JARS_BLOCK_VALUE =
            "COPY --chown=siddhi_user:siddhi_io \\$\\{HOST_JARS_DIR}/ \\$\\{JARS}";
    private static final String BUNDLES_BLOCK_VALUE =
            "COPY --chown=siddhi_user:siddhi_io \\$\\{HOST_BUNDLES_DIR}/ \\$\\{BUNDLES}";
    private static final String RESOURCES_DIR = "resources/docker-export";
    private static final String ZIP_FILE_NAME = "siddhi-docker.zip";
    private static final String ZIP_FILE_ROOT = "siddhi-docker/";
    private static final String DOCKER_FILE_NAME = "Dockerfile";
    private static final String JARS_DIR = "jars/";
    private static final String BUNDLE_DIR = "bundles/";
    private static final String APPS_DIR = "siddhi-files/";
    private static final String CONFIG_FILE = "configurations.yaml";
    private final ConfigProvider configProvider;
    private DockerConfigs dockerConfigs;

    private ExportAppsRequest exportAppsRequest;

    DockerUtils(ConfigProvider configProvider, ExportAppsRequest exportAppsRequest) {
        this.configProvider = configProvider;
        this.exportAppsRequest = exportAppsRequest;
    }

    /**
     * Create a zip archive.
     *
     * @return Zip archive file
     * @throws DockerGenerationException if docker generation fails
     */
    public File createZipFile() throws DockerGenerationException {
        boolean jarsAdded = false;
        boolean bundlesAdded = false;
        boolean configChanged = false;
        boolean envChanged = false;

        Path dockerFilePath = Paths.get(Constants.RUNTIME_PATH, RESOURCES_DIR, DOCKER_FILE_NAME);
        File zipFile = new File(ZIP_FILE_NAME);
        StringBuilder stringBuilder = new StringBuilder();
        ZipOutputStream zipOutputStream = null;
        ZipEntry dockerFileEntry = new ZipEntry(
                Paths.get(ZIP_FILE_ROOT, DOCKER_FILE_NAME).toString()
        );
        try {
            zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));

            // Write JARs to the zip file
            if (exportAppsRequest.getJars() != null && exportAppsRequest.getJars().size() > 0) {
                jarsAdded = true;
                String jarRootDir = Paths.get(Constants.CARBON_HOME, JARS_DIR).toString();
                String jarEntryRootDir = Paths.get(ZIP_FILE_ROOT, JARS_DIR).toString();
                for (String jar : exportAppsRequest.getJars()) {
                    Path jarPath = Paths.get(jarRootDir, jar);
                    ZipEntry jarEntry = new ZipEntry(Paths.get(jarEntryRootDir, jar).toString());
                    if (Files.isReadable(jarPath)) {
                        zipOutputStream.putNextEntry(jarEntry);
                        byte[] jarData = Files.readAllBytes(jarPath);
                        zipOutputStream.write(jarData, 0, jarData.length);
                        zipOutputStream.closeEntry();
                    } else {
                        log.error("JAR file" + jarPath.toString() + " is not readable.");
                    }
                }
            }

            // Write bundles to the zip file
            if (exportAppsRequest.getBundles() != null &&
                    exportAppsRequest.getBundles().size() > 0) {
                bundlesAdded = true;
                String bundleRootDir = Paths.get(Constants.CARBON_HOME, BUNDLE_DIR).toString();
                String bundleEntryRootDir = Paths.get(ZIP_FILE_ROOT, BUNDLE_DIR).toString();
                for (String bundle : exportAppsRequest.getBundles()) {
                    Path bundlePath = Paths.get(bundleRootDir, bundle);
                    ZipEntry bundleEntry = new ZipEntry(
                            Paths.get(bundleEntryRootDir,
                                    bundle).toString()
                    );
                    if (Files.isReadable(bundlePath)) {
                        zipOutputStream.putNextEntry(bundleEntry);
                        byte[] bundleData = Files.readAllBytes(bundlePath);
                        zipOutputStream.write(bundleData, 0, bundleData.length);
                        zipOutputStream.closeEntry();
                    } else {
                        log.error("Bundle file" + bundlePath.toString() + " is not readable.");
                    }
                }
            }

            // Write Siddhi apps to the zip file
            String appsEntryRootDir = Paths.get(ZIP_FILE_ROOT, APPS_DIR).toString();
            if (exportAppsRequest.getSiddhiApps() != null) {
                for (Map.Entry<String, String> app : exportAppsRequest.getSiddhiApps().entrySet()) {
                    String appName = app.getKey() + Constants.SIDDHI_APP_FILE_EXTENSION;
                    ZipEntry appEntry = new ZipEntry(
                            Paths.get(appsEntryRootDir, appName).toString()
                    );
                    zipOutputStream.putNextEntry(appEntry);
                    byte[] appData = app.getValue().getBytes(StandardCharsets.UTF_8);
                    zipOutputStream.write(appData, 0, appData.length);
                    zipOutputStream.closeEntry();
                }
            }

            // Write config file to the zip file
            if (exportAppsRequest.getConfiguration() != null &&
                    !exportAppsRequest.getConfiguration().isEmpty()) {
                configChanged = true;
                ZipEntry configFileEntry = new ZipEntry(
                        Paths.get(ZIP_FILE_ROOT, CONFIG_FILE).toString()
                );
                zipOutputStream.putNextEntry(configFileEntry);
                byte[] configData = exportAppsRequest
                        .getConfiguration()
                        .getBytes(StandardCharsets.UTF_8);
                zipOutputStream.write(configData, 0, configData.length);
                zipOutputStream.closeEntry();
            }

            // Write ENVs to the docker file
            if (exportAppsRequest.getTemplatedVariables() != null &&
                    !exportAppsRequest.getTemplatedVariables().isEmpty()) {
                envChanged = true;
                for (Map.Entry<String, String> env :
                        exportAppsRequest.getTemplatedVariables().entrySet()) {
                    stringBuilder.append("ENV " + env.getKey() + " " + env.getValue() + "\n");
                }
            }

            // Write the docker file to the zip file
            zipOutputStream.putNextEntry(dockerFileEntry);
            byte[] data = this.getDockerFile(
                    dockerFilePath,
                    jarsAdded,
                    bundlesAdded,
                    configChanged,
                    envChanged,
                    stringBuilder.toString()
            );
            zipOutputStream.write(data, 0, data.length);
            zipOutputStream.closeEntry();
        } catch (IOException e) {
            throw new DockerGenerationException(
                    "Cannot write to the zip file " + dockerFilePath.toString(), e
            );
        } catch (ConfigurationException e) {
            throw new DockerGenerationException(
                    "Cannot read configurations from the deployment.yaml", e
            );
        } finally {
            if (zipOutputStream != null) {
                try {
                    zipOutputStream.close();
                } catch (IOException e) {
                    log.error("Cannot close the zip file " + ZIP_FILE_NAME, e);
                }
            }
        }
        return zipFile;
    }

    /**
     * Read Dockerfile and replace the string tokens with valid values read from configurations.
     *
     * @param dockerFilePath Path to the Dockerfile
     * @param jarsAdded True if user specified custom JARs in the request
     * @param bundlesAdded True if user specified custom JARs in the request
     * @param configChanged True if user changed the existing deployment.yaml
     * @param envList String that contained environment variable list
     * @return Content
     * @throws IOException
     */
    private byte[] getDockerFile(
            Path dockerFilePath,
            boolean jarsAdded,
            boolean bundlesAdded,
            boolean configChanged,
            boolean envChanged,
            String envList
    ) throws IOException, DockerGenerationException, ConfigurationException {
        byte[] data;
        if (!Files.isReadable(dockerFilePath)) {
            throw new DockerGenerationException(
                    "Docker file " + dockerFilePath.toString() + " is not readable."
            );
        }
        data = Files.readAllBytes(dockerFilePath);
        String content = new String(data, StandardCharsets.UTF_8);
        String productVersion = this.getConfigurations().getProductVersion();
        content = content.replaceAll(PRODUCT_VERSION_TEMPLATE, productVersion);
        if (jarsAdded) {
            content = content.replaceAll(JARS_BLOCK_TEMPLATE, JARS_BLOCK_VALUE);
        } else {
            content = content.replaceAll(JARS_BLOCK_TEMPLATE, "");
        }

        if (bundlesAdded) {
            content = content.replaceAll(BUNDLES_BLOCK_TEMPLATE, BUNDLES_BLOCK_VALUE);
        } else {
            content = content.replaceAll(BUNDLES_BLOCK_TEMPLATE, "");
        }

        if (configChanged) {
            content = content.replaceAll(CONFIG_BLOCK_TEMPLATE, CONFIG_BLOCK_VALUE);
            content = content.replaceAll(CONFIG_PARAMETER_TEMPLATE, CONFIG_PARAMETER_VALUE);
        } else {
            content = content.replaceAll(CONFIG_BLOCK_TEMPLATE, "");
            content = content.replaceAll(CONFIG_PARAMETER_TEMPLATE, "");
        }

        if (envChanged) {
            content = content.replaceAll(ENV_BLOCK_TEMPLATE, envList);
        } else {
            content = content.replaceAll(ENV_BLOCK_TEMPLATE, "");
        }
        return content.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Read configurations from the deployment.yaml.
     *
     * @return Configuration object
     * @throws ConfigurationException
     */
    private DockerConfigs getConfigurations() throws ConfigurationException {

        if (this.dockerConfigs == null) {
            this.dockerConfigs = configProvider.getConfigurationObject(DockerConfigs.class);
        }
        return this.dockerConfigs;
    }
}
