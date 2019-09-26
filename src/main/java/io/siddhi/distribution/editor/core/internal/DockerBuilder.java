/*
 * Copyright (c)  2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.RegistryAuth;
import io.siddhi.distribution.editor.core.exception.DockerGenerationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class build and push Docker images using a given Dockerfile.
 */
public class DockerBuilder extends Thread {
    private Thread t;
    private static final Logger log = LoggerFactory.getLogger(DockerBuilder.class);
    private static final String UNIX_DEFAULT_DOCKER_HOST = "unix:///var/run/docker.sock";
    private static final String WINDOWS_DEFAULT_DOCKER_HOST = "tcp://localhost:2375";
    private static final String STEP_BUILDING = "Building";
    private static final String STEP_PUSHING = "Pushing";
    private static final String STEP_COMPLETED = "Completed";
    private static final String STEP_ERROR = "Error";
    private DockerClient dockerClient;
    private DockerBuilderStatus dockerBuilderStatus;
    private String imageId;
    private String dockerImageName;
    private String dockerUserName;
    private String dockerEmail;
    private String dockerPassword;
    private Path dockerFilePath;

    public DockerBuilder(
            String dockerImageName,
            String dockerUserName,
            String dockerEmail,
            String dockerPassword,
            Path dockerFilePath,
            DockerBuilderStatus dockerBuilderStatus
    ) {
        this.dockerImageName = dockerImageName;
        this.dockerUserName = dockerUserName;
        this.dockerEmail = dockerEmail;
        this.dockerPassword = dockerPassword;
        this.dockerFilePath = dockerFilePath;
        this.dockerBuilderStatus = dockerBuilderStatus;
    }

    public void run() {
        boolean isDockerBuilt;
        boolean isDockerPushed;
        try {
            isDockerBuilt = buildDockerImage();
            if (isDockerBuilt) {
                isDockerPushed = pushDockerImage();
                if (isDockerPushed) {
                    dockerBuilderStatus.setStep(STEP_COMPLETED);
                } else {
                    dockerBuilderStatus.setStep(STEP_ERROR);
                }
            }
            removeSampleDockerDir();
        } catch (DockerGenerationException e) {
            log.error("Failed to build and push Docker artifacts automatically ", e);
        }
    }

    public void start () {
        if (t == null) {
            t = new Thread (this);
            t.start ();
        }
    }

    private void createDockerClient() throws DockerGenerationException {
        String dockerHost;
        if (SystemUtils.IS_OS_WINDOWS) {
            dockerHost = WINDOWS_DEFAULT_DOCKER_HOST;
        } else {
            dockerHost = UNIX_DEFAULT_DOCKER_HOST;
        }
        dockerClient = DefaultDockerClient.builder().uri(dockerHost).build();
        try {
            dockerClient.ping();
        } catch (DockerException | InterruptedException e) {
            dockerBuilderStatus.setStatus(e.getMessage());
            throw new DockerGenerationException(
                    "Failed to connect to the Docker host using provided host URI " + dockerHost, e
            );
        }
    }

    private boolean buildDockerImage() throws DockerGenerationException {
        boolean isDockerBuilt = false;
        dockerBuilderStatus.setStep(STEP_BUILDING);
        if (dockerClient == null) {
            createDockerClient();
        }

        final AtomicReference<String> imageIdFromMessage = new AtomicReference<>();
        try {
            // build the image
            imageId = dockerClient.build(dockerFilePath, dockerImageName, message -> {
                final String imageId = message.buildImageId();
                if (message.stream() != null && !message.stream().equals("\n") && !message.stream().equals("")) {
                    dockerBuilderStatus.setStatus(message.stream().replace("\n", ""));
                    log.info(message.stream().replace("\n", ""));
                }
                // Image creation successful
                if (imageId != null) {
                    imageIdFromMessage.set(imageId);
                }
            }, DockerClient.BuildParam.noCache(), DockerClient.BuildParam.forceRm());
            if (imageIdFromMessage.get() != null) {
                isDockerBuilt = true;
            }
        } catch (DockerException | InterruptedException | IOException e) {
            dockerBuilderStatus.setStatus(e.getMessage());
            throw new DockerGenerationException(
                    "Failed to build the Docker image in directory " + dockerFilePath.toString() +
                            " and Docker image name " + dockerImageName, e
            );
        }

        return isDockerBuilt;
    }

    private boolean pushDockerImage() throws DockerGenerationException {
        boolean isPushed = false;
        dockerBuilderStatus.setStep(STEP_PUSHING);
        if (dockerClient == null) {
            createDockerClient();
        }

        //push built docker image to the docker hub registry
        final RegistryAuth registryAuth = RegistryAuth.builder()
                .email(dockerEmail)
                .username(dockerUserName)
                .password(dockerPassword)
                .build();
        try {
            final int statusCode = dockerClient.auth(registryAuth);
            if (statusCode == 200) {
                dockerClient.push(dockerImageName, message -> {
                    if (message.status() != null) {
                        dockerBuilderStatus.setStatus(message.status());
                        log.info(message.status());
                    }
                }, registryAuth);
                isPushed = true;
            } else {
                dockerBuilderStatus.setStatus("Authentication failed to connect to the Docker registry using UserName: "
                        + dockerUserName + " and Email: " + dockerEmail);
                throw new DockerGenerationException(
                        "Authentication failed to connect to the Docker registry using UserName: "
                                + dockerUserName + " and Email: " + dockerEmail
                );
            }
        } catch (DockerException | InterruptedException e) {
            dockerBuilderStatus.setStatus(e.getMessage());
            throw new DockerGenerationException(
                    "Failed to push the Docker image " + dockerImageName +
                            " with UserName: " + dockerUserName + " and Email: " + dockerEmail, e
            );
        }
        return isPushed;
    }

    private void removeSampleDockerDir() {
        if (dockerFilePath != null && Files.exists(dockerFilePath)) {
            try {
                FileUtils.deleteDirectory(new File(dockerFilePath.toString()));
            } catch (IOException e) {
                log.error("Failed automatic deletion of the temporary Docker directory " +
                        dockerFilePath.toString(), e);
            }
        }
    }
}

