/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.siddhi.editor.core.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.siddhi.editor.core.commons.configs.DockerConfigs;
import org.wso2.carbon.siddhi.editor.core.exception.DockerGenerationException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.wso2.carbon.siddhi.editor.core.util.Constants.CARBON_HOME;
import static org.wso2.carbon.siddhi.editor.core.util.Constants.DIRECTORY_DEPLOYMENT;
import static org.wso2.carbon.siddhi.editor.core.util.Constants.DIRECTORY_WORKSPACE;
import static org.wso2.carbon.siddhi.editor.core.util.Constants.RUNTIME_PATH;

/**
 * This class creates Docker artifacts with given Siddhi files.
 */
public class DockerUtils {
    private static final Logger log = LoggerFactory.getLogger(DockerUtils.class);
    private static final String RESOURCES_DIR = "resources/docker-export";
    private static final String SIDDHI_FILES_DIR = "siddhi-files";
    private static final String README_FILE = "README.md";
    private static final String DOCKER_COMPOSE_FILE = "docker-compose.yml";
    private static final String DOCKER_COMPOSE_EDITOR_FILE = "docker-compose.server.yml";
    private static final String DOCKER_COMPOSE_WORKER_FILE = "docker-compose.worker.yml";
    private static final String PRODUCT_VERSION_TOKEN = "\\{\\{PRODUCT_VERSION}}";
    private final ConfigProvider configProvider;
    private DockerConfigs dockerConfigs;

    public DockerUtils(ConfigProvider configProvider) {
        this.configProvider = configProvider;
    }

    /**
     * Create a zip archive.
     *
     * @param siddhiFiles List of Siddhi siddhiFiles
     * @return Zip archive file
     */
    public File createArchive(String profile, List<String> siddhiFiles) throws DockerGenerationException {
        // Create <CARBON_HOME>/tmp/docker-export directory.
        String tmpDirPath = Paths.get(CARBON_HOME, "tmp", "docker-export").toString();
        File tmpDir = new File(tmpDirPath);
        if (!tmpDir.exists() && !tmpDir.mkdirs()) {
            throw new DockerGenerationException("Cannot create temporary directory at " + tmpDirPath);
        }

        // Create the destination zip file in the <CARBON_HOME>/tmp directory.
        String destFilename = UUID.randomUUID().toString();
        String destPath = Paths.get(tmpDirPath, destFilename).toString();
        File zipFile = new File(destPath);
        log.debug("Created temporary zip archive at " + destPath);

        // Create zip archive.
        ZipOutputStream outputStream = null;
        try {
            outputStream = new ZipOutputStream(new FileOutputStream(zipFile));
            for (Map.Entry<String, Path> entry : getFileMap(profile, siddhiFiles).entrySet()) {
                // Create zip entry for each file in the map.
                ZipEntry zipEntry = new ZipEntry(entry.getKey());
                outputStream.putNextEntry(zipEntry);
                byte[] data = DOCKER_COMPOSE_FILE.equals(entry.getKey()) ?
                        this.readDockerComposeFile(entry.getValue()) : Files.readAllBytes(entry.getValue());
                outputStream.write(data, 0, data.length);
                outputStream.closeEntry();
            }
        } catch (IOException e) {
            throw new DockerGenerationException("Cannot write to the zip file.", e);
        } catch (ConfigurationException e) {
            throw new DockerGenerationException("Cannot read configurations from the deployment.yaml", e);
        } finally {
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    log.error("Cannot close the zip file.", e);
                }
            }
        }
        return zipFile;
    }

    /**
     * Create file map for the zip archive.
     *
     * @param siddhiFiles Siddhi files to be included
     * @return File map
     */
    private Map<String, Path> getFileMap(String profile, List<String> siddhiFiles) {
        Map<String, Path> files = new HashMap<>();

        // Add readme.md and the respective docker-compose.yml files.
        files.put(README_FILE, Paths.get(RUNTIME_PATH, RESOURCES_DIR, README_FILE));
        files.put(DOCKER_COMPOSE_FILE, Paths.get(RUNTIME_PATH, RESOURCES_DIR,
                "server".equals(profile) ? DOCKER_COMPOSE_EDITOR_FILE : DOCKER_COMPOSE_WORKER_FILE));

        // Add selected Siddhi files.
        for (String siddhiFile : siddhiFiles) {
            String key = Paths.get(SIDDHI_FILES_DIR, siddhiFile).toString();
            Path path = Paths.get(RUNTIME_PATH, DIRECTORY_DEPLOYMENT, DIRECTORY_WORKSPACE, siddhiFile);
            files.put(key, path);
        }
        return files;
    }

    /**
     * Read docker-compose.yml file and replace the string tokens with valid values read from configurations.
     *
     * @param path Path to the docker-compose.yml file
     * @return Content
     * @throws IOException
     * @throws ConfigurationException
     */
    private byte[] readDockerComposeFile(Path path) throws IOException, ConfigurationException {
        String productVersion = this.getConfigrations().getProductVersion();

        byte[] data = Files.readAllBytes(path);
        String content = new String(data, StandardCharsets.UTF_8);
        String replacedContent = content
                .replaceAll(PRODUCT_VERSION_TOKEN, productVersion);
        return replacedContent.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * Read configurations from the deployment.yaml.
     *
     * @return Configuration object
     * @throws ConfigurationException
     */
    private DockerConfigs getConfigrations() throws ConfigurationException {
        if (this.dockerConfigs == null) {
            this.dockerConfigs = configProvider.getConfigurationObject(DockerConfigs.class);
        }
        return this.dockerConfigs;
    }
}
