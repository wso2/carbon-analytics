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

import io.siddhi.core.stream.input.source.Source;
import io.siddhi.distribution.editor.core.commons.configs.DockerConfigs;
import io.siddhi.distribution.editor.core.commons.kubernetes.Env;
import io.siddhi.distribution.editor.core.commons.kubernetes.KubernetesConfig;
import io.siddhi.distribution.editor.core.commons.kubernetes.SiddhiProcess;
import io.siddhi.distribution.editor.core.commons.kubernetes.SiddhiProcessApp;
import io.siddhi.distribution.editor.core.commons.kubernetes.SiddhiProcessContainer;
import io.siddhi.distribution.editor.core.commons.kubernetes.SiddhiProcessSpec;
import io.siddhi.distribution.editor.core.commons.request.ExportAppsRequest;
import io.siddhi.distribution.editor.core.exception.DockerGenerationException;
import io.siddhi.distribution.editor.core.exception.KubernetesGenerationException;
import io.siddhi.distribution.editor.core.util.Constants;
import io.siddhi.distribution.editor.core.util.SourceEditorUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
import org.yaml.snakeyaml.introspector.Property;
import org.yaml.snakeyaml.nodes.NodeTuple;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This class creates Docker artifacts with given Siddhi files.
 */
public class ExportUtils {

    private static final Logger log = LoggerFactory.getLogger(ExportUtils.class);
    private static final String CONFIG_BLOCK_TEMPLATE = "\\{\\{CONFIGURATION_BLOCK}}";
    private static final String CONFIG_PARAMETER_TEMPLATE = "\\{\\{CONFIGURATION_PARAMETER_BLOCK}}";
    private static final String JARS_BLOCK_TEMPLATE = "\\{\\{JARS_BLOCK}}";
    private static final String BUNDLES_BLOCK_TEMPLATE = "\\{\\{BUNDLES_BLOCK}}";
    private static final String ENV_BLOCK_TEMPLATE = "\\{\\{ENV_BLOCK}}";
    private static final String APPS_BLOCK_TEMPLATE = "\\{\\{APPS_BLOCK}}";
    private static final String EXPOSE_PORTS_BLOCK_TEMPLATE = "\\{\\{EXPORT_PORTS_BLOCK}}";
    private static final String DOCKER_BASE_IMAGE_TEMPLATE = "\\{\\{SIDDHI_RUNNER_BASE_IMAGE}}";
    private static final String DOCKER_IMAGE_NAME_TEMPLATE = "\\{\\{DOCKER_IMAGE_NAME}}";
    private static final String PORT_BIND_TEMPLATE = "\\{\\{BIND_PORTS}}";
    private static final String CONFIG_BLOCK_VALUE =
            "COPY --chown=siddhi_user:siddhi_io \\$\\{CONFIG_FILE}/ \\$\\{USER_HOME}";
    private static final String CONFIG_PARAMETER_VALUE =
            ", \"-Dconfig=/home/siddhi_user/configurations.yaml\"";
    private static final String JARS_BLOCK_VALUE =
            "COPY --chown=siddhi_user:siddhi_io \\$\\{HOST_JARS_DIR}/ \\$\\{JARS}";
    private static final String BUNDLES_BLOCK_VALUE =
            "COPY --chown=siddhi_user:siddhi_io \\$\\{HOST_BUNDLES_DIR}/ \\$\\{BUNDLES}";
    private static final String APPS_BLOCK_VALUE =
            "COPY --chown=siddhi_user:siddhi_io \\$\\{HOST_APPS_DIR}/ \\$\\{APPS}";
    private static final String SIDDHI_PROCESS_SPEC_TEMPLATE = "\\{\\{SIDDHI_PROCESS_SPEC}}";
    private static final String SIDDHI_PROCESS_NAME_TEMPLATE = "\\{\\{SIDDHI_PROCESS_NAME}}";
    private static final String SIDDHI_PROCESS_DEFAULT_NAME = "sample-siddhi-process";
    private static final String SIDDHI_APP_NAME_ENTRY = "appName";
    private static final String SIDDHI_APP_CONTENT_ENTRY = "appContent";
    private static final String SIDDHI_TEMPLATED_VAR_KEY_ENTRY = "key";
    private static final String SIDDHI_TEMPLATED_VAR_VALUE_ENTRY = "value";
    private static final String RESOURCES_DIR = "resources/docker-export";
    private static final String DOCKER_FILE_NAME = "Dockerfile";
    private static final String DOCKER_README_FILE_NAME = "DOCKER-README.md";
    private static final String KUBERNETES_README_FILE_NAME = "K8S-README.md";
    private static final String GENERIC_README_FILE_NAME = "README.md";
    private static final String KUBERNETES_FILE_NAME = "siddhi-process.yaml";
    private static final String JARS_DIR = "jars" + File.separator;
    private static final String BUNDLE_DIR = "bundles" + File.separator;
    private static final String APPS_DIR = "siddhi-files" + File.separator;
    private static final String CONFIG_FILE = "configurations.yaml";
    private static final String EXPORT_TYPE_KUBERNETES = "kubernetes";
    private static final String EXPORT_TYPE_DOCKER = "docker";
    private static final String RUNNER_DEPLOYMENT_YAML_FILE = "runner-deployment.yaml";
    private static final String TOOLING_DEPLOYMENT_YAML_FILE = "deployment.yaml";
    private static final String DIRECTORY_CONF = "conf";
    private static final String DIRECTORY_PROFILE = "tooling";
    private static final String SIDDHI_NAMESPACE = "siddhi";
    private static final String DATA_SOURCES_NAMESPACE = "dataSources";
    private final ConfigProvider configProvider;
    private DockerConfigs dockerConfigs;
    private ExportAppsRequest exportAppsRequest;
    private String exportType;
    Path tempDockerDirectoryPath;
    private Set<Integer> exposePorts = new HashSet<>();
    private String zipFileName = "siddhi-docker.zip";
    private String zipFileRoot = "siddhi-docker" + File.separator;

    ExportUtils(
            ConfigProvider configProvider,
            ExportAppsRequest exportAppsRequest,
            String exportType
    ) {

        this.configProvider = configProvider;
        this.exportAppsRequest = exportAppsRequest;
        this.exportType = exportType;
    }

    ExportUtils(ConfigProvider configProvider) {

        this.configProvider = configProvider;
    }

    /**
     * Create a zip archive.
     *
     * @return Zip archive file
     * @throws DockerGenerationException if docker generation fails
     */
    public File createZipFile()
            throws DockerGenerationException, KubernetesGenerationException {

        boolean jarsAdded = false;
        boolean bundlesAdded = false;
        boolean configChanged = false;
        boolean envChanged = false;
        boolean buildDocker = false;
        if (exportType != null && exportType.equals(EXPORT_TYPE_DOCKER)) {
            if (exportAppsRequest.getDockerConfiguration() != null) {
                if (exportAppsRequest.getDockerConfiguration().getImageName() != null &&
                        !exportAppsRequest.getDockerConfiguration().getImageName().equals("")) {
                    String dockerImageName = exportAppsRequest
                            .getDockerConfiguration()
                            .getImageName()
                            .replaceAll("/", "-")
                            .replaceAll(":", "-").trim();
                    zipFileName = dockerImageName + ".zip";
                    zipFileRoot = dockerImageName + File.separator;
                }
            }
        }
        if (exportType != null && exportType.equals(EXPORT_TYPE_KUBERNETES)) {
            zipFileName = "siddhi-kubernetes.zip";
            zipFileRoot = "siddhi-kubernetes" +  File.separator;
            if (exportAppsRequest.getKubernetesConfiguration() != null) {
                KubernetesConfig kubernetesConfig = getKubernetesConfigs(
                        exportAppsRequest.getKubernetesConfiguration()
                );

                if (kubernetesConfig != null && kubernetesConfig.getSiddhiProcessName() != null) {
                    zipFileName = kubernetesConfig.getSiddhiProcessName().toLowerCase().trim()
                            + ".zip";
                    zipFileRoot = kubernetesConfig.getSiddhiProcessName().toLowerCase().trim()
                            + File.separator;
                }
            }
        }
        Path dockerFilePath = Paths.get(Constants.RUNTIME_PATH, RESOURCES_DIR, DOCKER_FILE_NAME);
        Path dockerReadmeFilePath = Paths.get(Constants.RUNTIME_PATH, RESOURCES_DIR, DOCKER_README_FILE_NAME);
        Path kubernetesReadmeFilePath = Paths.get(Constants.RUNTIME_PATH, RESOURCES_DIR, KUBERNETES_README_FILE_NAME);
        File zipFile = new File(zipFileName);
        ZipOutputStream zipOutputStream = null;
        ZipEntry dockerFileEntry = new ZipEntry(Paths.get(zipFileRoot, DOCKER_FILE_NAME).toString());

        if (exportAppsRequest.getDockerConfiguration() != null) {
            UUID uuid = UUID.randomUUID();
            tempDockerDirectoryPath = Paths.get(RESOURCES_DIR, uuid.toString());
            if (!Files.exists(tempDockerDirectoryPath)) {
                if (!new File(tempDockerDirectoryPath.toString()).mkdir()) {
                    throw new DockerGenerationException(
                            "Failed to create the sample directory " +
                                    tempDockerDirectoryPath.toString()
                    );
                }
            }
            if (Files.isWritable(tempDockerDirectoryPath)) {
                buildDocker = true;
            }
        }

        try {
            zipOutputStream = new ZipOutputStream(new FileOutputStream(zipFile));

            // Write JARs to the zip file
            if (exportAppsRequest.getJars() != null && exportAppsRequest.getJars().size() > 0) {
                jarsAdded = true;
                String jarRootDir = Paths.get(Constants.CARBON_HOME, JARS_DIR).toString();
                String jarEntryRootDir = Paths.get(zipFileRoot, JARS_DIR).toString();
                Path tempDockerJarDirPath = Paths.get(tempDockerDirectoryPath.toString(), JARS_DIR);
                if (buildDocker && !Files.exists(tempDockerJarDirPath)) {
                    if (!new File(tempDockerJarDirPath.toString()).mkdir()) {
                        throw new DockerGenerationException(
                                "Failed to create the sample jars directory " +
                                        tempDockerJarDirPath.toString()
                        );
                    }
                }

                for (String jar : exportAppsRequest.getJars()) {
                    Path jarPath = Paths.get(jarRootDir, jar);
                    ZipEntry jarEntry = new ZipEntry(Paths.get(jarEntryRootDir, jar).toString());
                    if (Files.isReadable(jarPath)) {
                        zipOutputStream.putNextEntry(jarEntry);
                        byte[] jarData = Files.readAllBytes(jarPath);
                        zipOutputStream.write(jarData, 0, jarData.length);
                        zipOutputStream.closeEntry();
                        if (buildDocker) {
                            Files.write(Paths.get(tempDockerJarDirPath.toString(), jar), jarData);
                        }
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
                String bundleEntryRootDir = Paths.get(zipFileRoot, BUNDLE_DIR).toString();
                Path tempDockerBundleDirPath = Paths.get(tempDockerDirectoryPath.toString(), BUNDLE_DIR);
                if (buildDocker && !Files.exists(tempDockerBundleDirPath)) {
                    if (!new File(tempDockerBundleDirPath.toString()).mkdir()) {
                        throw new DockerGenerationException(
                                "Failed to create the sample bundles directory " +
                                        tempDockerBundleDirPath.toString()
                        );
                    }
                }

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
                        if (buildDocker) {
                            Files.write(Paths.get(tempDockerBundleDirPath.toString(), bundle), bundleData);
                        }
                    } else {
                        log.error("Bundle file" + bundlePath.toString() + " is not readable.");
                    }
                }
            }

            // Write Siddhi apps to the zip file
            List<String> userGivenSiddhiApps = new ArrayList<>();
            String appsEntryRootDir = Paths.get(zipFileRoot, APPS_DIR).toString();
            Path tempDockerAppDirPath = Paths.get(tempDockerDirectoryPath.toString(), APPS_DIR);
            if (buildDocker && !Files.exists(tempDockerAppDirPath)) {
                if (!new File(tempDockerAppDirPath.toString()).mkdir()) {
                    throw new DockerGenerationException(
                            "Failed to create the sample apps directory " +
                                    tempDockerAppDirPath.toString()
                    );
                }
            }
            if (exportAppsRequest.getTemplatedSiddhiApps() != null) {
                for (Map<String, String> app : exportAppsRequest.getTemplatedSiddhiApps()) {
                    String appName = app.get(SIDDHI_APP_NAME_ENTRY);
                    ZipEntry appEntry = new ZipEntry(
                            Paths.get(appsEntryRootDir, appName).toString()
                    );
                    zipOutputStream.putNextEntry(appEntry);
                    String siddhiAppContent = app.get(SIDDHI_APP_CONTENT_ENTRY);
                    userGivenSiddhiApps.add(siddhiAppContent);
                    byte[] appData = siddhiAppContent.getBytes(StandardCharsets.UTF_8);
                    zipOutputStream.write(appData, 0, appData.length);
                    zipOutputStream.closeEntry();
                    if (buildDocker) {
                        Files.write(Paths.get(tempDockerAppDirPath.toString(), appName), appData);
                    }
                }
            }

            // Write config file to the zip file
            if (exportAppsRequest.getConfiguration() != null &&
                    !exportAppsRequest.getConfiguration().isEmpty()) {
                configChanged = true;
                ZipEntry configFileEntry = new ZipEntry(
                        Paths.get(zipFileRoot, CONFIG_FILE).toString()
                );
                zipOutputStream.putNextEntry(configFileEntry);
                byte[] configData = exportAppsRequest
                        .getConfiguration()
                        .getBytes(StandardCharsets.UTF_8);
                zipOutputStream.write(configData, 0, configData.length);
                zipOutputStream.closeEntry();
                if (buildDocker) {
                    Path tempDockerConfigPath = Paths.get(tempDockerDirectoryPath.toString(), CONFIG_FILE);
                    Files.write(tempDockerConfigPath, configData);
                }
            }

            // Write ENVs to the docker file
            StringBuilder envStringBuilder = new StringBuilder();
            Map<String, String> envMap = new HashMap<>();
            if (exportAppsRequest.getTemplatedVariables() != null &&
                    !exportAppsRequest.getTemplatedVariables().isEmpty()) {
                envChanged = true;
                for (Map<String, String> env :
                        exportAppsRequest.getTemplatedVariables()) {
                    String envKey = env.get(SIDDHI_TEMPLATED_VAR_KEY_ENTRY);
                    String envVal = env.get(SIDDHI_TEMPLATED_VAR_VALUE_ENTRY);
                    envMap.put(envKey, envVal);
                    envStringBuilder
                            .append("ENV ")
                            .append(envKey)
                            .append(" ")
                            .append(envVal)
                            .append("\n");
                }
            }

            //add default expose ports
            exposePorts.addAll(Arrays.asList(9090, 9443));
            List<String> siddhiApps = userGivenSiddhiApps;
            if (envMap.size() != 0 && !userGivenSiddhiApps.isEmpty()) {
                List<String> populatedSiddhiApps = new ArrayList<>();
                for (String siddhiApp : siddhiApps) {
                    String populatedSiddhiApp = SourceEditorUtils.populateSiddhiAppWithVars(envMap, siddhiApp);
                    populatedSiddhiApps.add(populatedSiddhiApp);
                }
                siddhiApps = populatedSiddhiApps;
            }
            for (String app : siddhiApps) {
                try {
                    Collection<List<Source>> sources = EditorDataHolder.getSiddhiManager()
                            .createSiddhiAppRuntime(app).getSources();
                    for (List<Source> sourceList : sources) {
                        for (Source source : sourceList) {
                            if (source.getServiceDeploymentInfo() != null) {
                                exposePorts.add(source.getServiceDeploymentInfo().getPort());
                            }
                        }
                    }
                } catch (Exception ignored) {
                    //ignoring exception since Siddhi app parsing might fail due to unset variables
                    if (log.isDebugEnabled()) {
                        log.error("Exception caught while parsing the exported Siddhi applications.", ignored);
                    }
                }
            }

            // Write Expose Ports to the docker file
            StringBuilder exposePortsStr = new StringBuilder();
            if (!exposePorts.isEmpty()) {
                exposePortsStr.append("EXPOSE ");
                for (int port : exposePorts) {
                    exposePortsStr
                            .append(port)
                            .append(" ");
                }
                exposePortsStr.append("\n");
            }

            // Write the docker file to the zip file
            zipOutputStream.putNextEntry(dockerFileEntry);
            byte[] dockerContent = this.getDockerFile(
                    dockerFilePath,
                    jarsAdded,
                    bundlesAdded,
                    configChanged,
                    envChanged,
                    envStringBuilder.toString(),
                    exposePortsStr.toString()
            );
            zipOutputStream.write(dockerContent, 0, dockerContent.length);
            zipOutputStream.closeEntry();
            if (buildDocker) {
                Path tempDockerFilePath = Paths.get(tempDockerDirectoryPath.toString(), DOCKER_FILE_NAME);
                Files.write(tempDockerFilePath, dockerContent);
            }

            // Write the kubernetes file to the zip file and add README.md
            ZipEntry readmeEntry = new ZipEntry(Paths.get(zipFileRoot, GENERIC_README_FILE_NAME).toString());
            zipOutputStream.putNextEntry(readmeEntry);
            if (exportType != null && exportType.equals(EXPORT_TYPE_KUBERNETES)) {
                // Add K8s README.md
                if (!Files.isReadable(kubernetesReadmeFilePath)) {
                    throw new KubernetesGenerationException(
                            "Readme file " + kubernetesReadmeFilePath.toString() + " is not readable."
                    );
                }
                byte[] readmeContent = Files.readAllBytes(kubernetesReadmeFilePath);
                zipOutputStream.write(readmeContent, 0, readmeContent.length);
                zipOutputStream.closeEntry();

                // Add K8s YAML
                ZipEntry kubernetesFileEntry = new ZipEntry(
                        Paths.get(zipFileRoot, KUBERNETES_FILE_NAME).toString()
                );
                zipOutputStream.putNextEntry(kubernetesFileEntry);
                byte[] kubernetesFileData = this.getKubernetesFile(
                        Paths.get(Constants.RUNTIME_PATH, RESOURCES_DIR, KUBERNETES_FILE_NAME)
                );
                zipOutputStream.write(kubernetesFileData, 0, kubernetesFileData.length);
                zipOutputStream.closeEntry();
            } else {
                // Add Docker README.md
                StringBuilder portBindingStr = new StringBuilder();
                for (int port: exposePorts) {
                    portBindingStr.append("-p ");
                    portBindingStr.append(port);
                    portBindingStr.append(":");
                    portBindingStr.append(port);
                    portBindingStr.append(" ");
                }
                if (!Files.isReadable(dockerReadmeFilePath)) {
                    throw new DockerGenerationException(
                            "Readme file " + dockerReadmeFilePath.toString() + " is not readable."
                    );
                }
                byte[] data = Files.readAllBytes(dockerReadmeFilePath);
                String content = new String(data, StandardCharsets.UTF_8);
                content = content.replaceAll(PORT_BIND_TEMPLATE, portBindingStr.toString());
                if (exportAppsRequest.getDockerConfiguration() != null &&
                        exportAppsRequest.getDockerConfiguration().getImageName() != null) {
                    content = content.replaceAll(
                            DOCKER_IMAGE_NAME_TEMPLATE,
                            exportAppsRequest.getDockerConfiguration().getImageName()
                    );
                } else {
                    content = content.replaceAll(
                            DOCKER_IMAGE_NAME_TEMPLATE, Constants.DEFAULT_SIDDHI_DOCKER_IMAGE_NAME);
                }

                byte[] readmeContent = content.getBytes(StandardCharsets.UTF_8);
                zipOutputStream.write(readmeContent, 0, readmeContent.length);
                zipOutputStream.closeEntry();
            }
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
                    log.error("Cannot close the zip file " + zipFileName, e);
                }
            }
        }
        return zipFile;
    }

    /**
     * Return the path of the temporary directory path that created for docker build.
     *
     * @return Path
     */
    public Path getTempDockerPath()  {
        if (tempDockerDirectoryPath != null) {
            return tempDockerDirectoryPath;
        } else {
            return Paths.get("");
        }

    }

    /**
     * Read Dockerfile and replace the string tokens with valid values read from configurations.
     *
     * @param dockerFilePath Path to the Dockerfile
     * @param jarsAdded      True if user specified custom JARs in the request
     * @param bundlesAdded   True if user specified custom JARs in the request
     * @param configChanged  True if user changed the existing deployment.yaml
     * @param envList        String that contained environment variable list
     * @return Content
     * @throws IOException
     */
    private byte[] getDockerFile(
            Path dockerFilePath,
            boolean jarsAdded,
            boolean bundlesAdded,
            boolean configChanged,
            boolean envChanged,
            String envList,
            String exportPortList
    ) throws IOException, DockerGenerationException, ConfigurationException {

        byte[] data;
        if (!Files.isReadable(dockerFilePath)) {
            throw new DockerGenerationException(
                    "Docker file " + dockerFilePath.toString() + " is not readable."
            );
        }
        data = Files.readAllBytes(dockerFilePath);
        String content = new String(data, StandardCharsets.UTF_8);
        String dockerBaseImgName = Constants.DEFAULT_SIDDHI_DOCKER_BASE_IMAGE_NAME;
        String version = EditorDataHolder.getBundleContext().getBundle().getVersion().toString();
        if (version != null && !version.isEmpty()) {
            dockerBaseImgName = dockerBaseImgName.concat(":").concat(version.toLowerCase());
        }
        if (configProvider.getConfigurationObject(Constants.EXPORT_PROPERTIES_NAMESPACE) != null) {
            dockerBaseImgName = (String) ((Map) configProvider
                    .getConfigurationObject(Constants.EXPORT_PROPERTIES_NAMESPACE))
                    .get(Constants.DOCKER_BASE_IMAGE_PROPERTY);
        }
        content = content.replaceAll(DOCKER_BASE_IMAGE_TEMPLATE, dockerBaseImgName);

        if (exportType != null && exportType.equals(EXPORT_TYPE_KUBERNETES)) {
            content = content.replaceAll(APPS_BLOCK_TEMPLATE, "");
        } else {
            content = content.replaceAll(APPS_BLOCK_TEMPLATE, APPS_BLOCK_VALUE);
        }

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

        content = content.replaceAll(EXPOSE_PORTS_BLOCK_TEMPLATE, exportPortList);

        return content.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Generate SiddhiProcess Kubernetes YAML file.
     *
     * @param kubernetesFilePath Path to the Kubernetes YAML file
     * @return YAML content
     * @throws IOException
     * @throws KubernetesGenerationException
     */
    private byte[] getKubernetesFile(Path kubernetesFilePath)
            throws KubernetesGenerationException, IOException {

        if (!Files.isReadable(kubernetesFilePath)) {
            throw new KubernetesGenerationException(
                    "Kubernetes file " + kubernetesFilePath.toString() + " is not readable."
            );
        }
        byte[] data = Files.readAllBytes(kubernetesFilePath);
        String content = new String(data, StandardCharsets.UTF_8);
        KubernetesConfig kubernetesConfig;
        if (exportAppsRequest.getKubernetesConfiguration() != null) {
            SiddhiProcessSpec siddhiProcessSpec = new SiddhiProcessSpec();
            kubernetesConfig = getKubernetesConfigs(
                    exportAppsRequest.getKubernetesConfiguration()
            );
            if (kubernetesConfig != null) {
                if (kubernetesConfig.getMessagingSystem() != null) {
                    siddhiProcessSpec.setMessagingSystem(kubernetesConfig.getMessagingSystem());
                }

                if (kubernetesConfig.getPersistentVolumeClaim() != null) {
                    siddhiProcessSpec.setPersistentVolumeClaim(
                            kubernetesConfig.getPersistentVolumeClaim()
                    );
                }
            }

            // Set container spec
            SiddhiProcessContainer siddhiProcessContainer = new SiddhiProcessContainer();
            boolean changedContainerSpec = false;
            if (this.exportAppsRequest.getTemplatedVariables() != null &&
                    this.exportAppsRequest.getTemplatedVariables().size() > 0) {
                ArrayList<Env> envs = new ArrayList<Env>();
                for (Map<String, String> templatedVariable :
                        exportAppsRequest.getTemplatedVariables()) {
                    Env env = new Env(
                            templatedVariable.get(SIDDHI_TEMPLATED_VAR_KEY_ENTRY),
                            templatedVariable.get(SIDDHI_TEMPLATED_VAR_VALUE_ENTRY)
                    );
                    envs.add(env);
                }
                siddhiProcessContainer.setEnv(envs);
                siddhiProcessSpec.setContainer(siddhiProcessContainer);
                changedContainerSpec = true;
            }

            if (exportAppsRequest.getDockerConfiguration() != null &&
                    exportAppsRequest.getDockerConfiguration().getImageName() != null) {
                siddhiProcessContainer.setImage(
                        exportAppsRequest.getDockerConfiguration().getImageName()
                );
                changedContainerSpec = true;
            }

            if (changedContainerSpec) {
                siddhiProcessSpec.setContainer(siddhiProcessContainer);
            }

            if (this.exportAppsRequest.getTemplatedSiddhiApps() != null &&
                    this.exportAppsRequest.getTemplatedSiddhiApps().size() > 0) {
                ArrayList<SiddhiProcessApp> siddhiProcessApps = new ArrayList<SiddhiProcessApp>();
                for (Map<String, String> app : exportAppsRequest.getTemplatedSiddhiApps()) {
                    String escapedApp = app.get(SIDDHI_APP_CONTENT_ENTRY)
                            .replaceAll("( |\\t)*\\n", "\n")
                            .replaceAll("(\t)+", "");
                    SiddhiProcessApp siddhiProcessApp = new SiddhiProcessApp(escapedApp);
                    siddhiProcessApps.add(siddhiProcessApp);
                }
                siddhiProcessSpec.setApps(siddhiProcessApps);
            }

            if (this.exportAppsRequest.getConfiguration() != null &&
                    !this.exportAppsRequest.getConfiguration().isEmpty()) {
                String escapedConfig = this.exportAppsRequest
                        .getConfiguration()
                        .replaceAll("( |\\t)*\\n", "\n");
                siddhiProcessSpec.setRunner(escapedConfig);
            }

            SiddhiProcess siddhiProcess = new SiddhiProcess(siddhiProcessSpec);

            Representer representer = new Representer() {
                @Override
                protected NodeTuple representJavaBeanProperty(
                        Object javaBean, Property property, Object propertyValue, Tag customTag) {
                    // if value of property is null, ignore it.
                    if (propertyValue == null) {
                        return null;
                    } else {
                        return super.representJavaBeanProperty(
                                javaBean,
                                property,
                                propertyValue,
                                customTag
                        );
                    }
                }
            };
            representer.addClassTag(SiddhiProcess.class, Tag.MAP);
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml yaml = new Yaml(representer, options);
            String spec = yaml.dump(siddhiProcess);
            spec = spec.replaceAll("\\$", "\\\\\\$");
            spec = spec.replaceAll("\\{", "\\\\\\{");
            content = content.replaceAll(SIDDHI_PROCESS_SPEC_TEMPLATE, spec);
            if (kubernetesConfig != null) {
                if (kubernetesConfig.getSiddhiProcessName() != null) {
                    content = content.replaceAll(
                            SIDDHI_PROCESS_NAME_TEMPLATE,
                            kubernetesConfig.getSiddhiProcessName()
                    );
                } else {
                    content = content.replaceAll(
                            SIDDHI_PROCESS_NAME_TEMPLATE,
                            SIDDHI_PROCESS_DEFAULT_NAME
                    );
                }
            } else {
                content = content.replaceAll(
                        SIDDHI_PROCESS_NAME_TEMPLATE,
                        SIDDHI_PROCESS_DEFAULT_NAME
                );
            }
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

    /**
     * Read configurations from the tooling configs and merge with default configs.
     *
     * @return YAML string of combined configurations
     * @throws IOException
     */
    public String exportConfigs() throws IOException {

        Path toolingConfigFile = Paths.get(
                Constants.CARBON_HOME,
                DIRECTORY_CONF,
                DIRECTORY_PROFILE,
                TOOLING_DEPLOYMENT_YAML_FILE
        );
        Path runnerConfigFile = Paths.get(
                Constants.RUNTIME_PATH,
                RESOURCES_DIR,
                RUNNER_DEPLOYMENT_YAML_FILE
        );
        if (!Files.isReadable(toolingConfigFile)) {
            throw new IOException(
                    "Config file " + toolingConfigFile.toString() + " is not readable."
            );
        }

        if (!Files.isReadable(runnerConfigFile)) {
            throw new IOException(
                    "Config file " + runnerConfigFile.toString() + " is not readable."
            );
        }
        String toolingDeploymentYamlContent = new String(
                Files.readAllBytes(
                        toolingConfigFile
                ),
                StandardCharsets.UTF_8
        );
        String runnerDeploymentYamlContent = new String(
                Files.readAllBytes(
                        runnerConfigFile
                ),
                StandardCharsets.UTF_8
        );
        Yaml loadYaml = new Yaml();
        Map<String, Object> runnerConfigMap = loadYaml.load(runnerDeploymentYamlContent);
        Map<String, Object> toolingConfigMap = loadYaml.load(toolingDeploymentYamlContent);
        if (runnerConfigMap != null) {
            if (toolingConfigMap.get(DATA_SOURCES_NAMESPACE) != null) {
                runnerConfigMap.put(DATA_SOURCES_NAMESPACE, toolingConfigMap.get(DATA_SOURCES_NAMESPACE));
            }
            if (toolingConfigMap.get(SIDDHI_NAMESPACE) != null) {
                runnerConfigMap.put(SIDDHI_NAMESPACE, toolingConfigMap.get(SIDDHI_NAMESPACE));
            }
            Representer representer = new Representer();
            representer.addClassTag(SiddhiProcess.class, Tag.MAP);
            DumperOptions options = new DumperOptions();
            options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            Yaml dumpYaml = new Yaml(representer, options);
            return dumpYaml.dump(runnerConfigMap);
        }
        return "";
    }

    /**
     * Read configurations from the deployment.yaml.
     *
     * @return Configuration object
     * @throws ConfigurationException
     */
    private KubernetesConfig getKubernetesConfigs(String kubernetesConfigString) {
        CustomClassLoaderConstructor customClassLoaderConstructor = new
                CustomClassLoaderConstructor(this.getClass().getClassLoader());
        Yaml kubernetesConfigYaml = new Yaml(customClassLoaderConstructor);
        KubernetesConfig kubernetesConfig = kubernetesConfigYaml.loadAs(
                kubernetesConfigString,
                KubernetesConfig.class
        );
        return kubernetesConfig;
    }

    /**
     * Name of the created ZIP file.
     *
     * @return Name of the created ZIP file
     */
    public String getZipFileName() {
        return zipFileName;
    }
}
