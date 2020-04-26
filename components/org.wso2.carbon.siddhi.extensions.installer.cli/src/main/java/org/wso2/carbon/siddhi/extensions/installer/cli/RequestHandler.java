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

package org.wso2.carbon.siddhi.extensions.installer.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

/**
 * Communicates with the Extensions Installer REST API, when requested by the {@link ExtensionsInstallerCli}.
 */
public class RequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private static final String GET_STATUS_CURL_FORMAT = "curl -s --location --request GET %s/status";
    private static final String GET_STATUS_EXTENSION_CURL_FORMAT = "curl -s --location --request GET %s/status/%s";
    private static final String INSTALL_CURL_FORMAT = "curl -s --location --request POST %s/%s/install";
    private static final String DEPENDENCY_SHARING_EXTENSIONS_CURL_FORMAT =
        "curl -s --location --request GET %s/%s/dependency-sharing-extensions";
    private static final String UN_INSTALL_CURL_FORMAT = "curl -s --location --request POST %s/%s/uninstall";

    private RequestHandler() {
        // Prevents Instantiation.
    }

    /**
     * Executes the given cURL command.
     *
     * @param command cURL command.
     * @return Response of the cURL command.
     * @throws IOException Error occurred when executing the cURL command.
     */
    private static String executeCurl(String command) throws IOException {
        Reader reader = new InputStreamReader(
            Runtime.getRuntime().exec(command).getInputStream(),
            StandardCharsets.UTF_8
        );
        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(reader)) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }
        return response.toString();
    }

    /**
     * Performs request to get installation statuses of all the extensions.
     *
     * @param baseUrl Base URL of Extensions Installer REST API.
     * @throws ExtensionsInstallerCliException Failed to get installation statuses of extensions.
     */
    public static void doGetAllExtensionStatuses(String baseUrl) throws ExtensionsInstallerCliException {
        try {
            String command = String.format(GET_STATUS_CURL_FORMAT, baseUrl);
            String response = executeCurl(command);
            ResponseHandler.handleAllExtensionStatusesResponse(response);
        } catch (IOException e) {
            throw new ExtensionsInstallerCliException("Failed to get statuses of extensions.", e);
        }
    }

    /**
     * Performs request to get the installation status of the extension, which has the given name.
     *
     * @param extensionName Name of the extension of which, installation status is requested.
     * @param baseUrl       Base URL of Extensions Installer REST API.
     * @throws ExtensionsInstallerCliException Failed to get installation status of the extension.
     */
    public static void doGetExtensionStatus(String extensionName, String baseUrl)
        throws ExtensionsInstallerCliException {
        try {
            String command = String.format(GET_STATUS_EXTENSION_CURL_FORMAT, baseUrl, extensionName);
            String response = executeCurl(command);
            ResponseHandler.handleExtensionStatusResponse(response, extensionName);
        } catch (IOException e) {
            throw new ExtensionsInstallerCliException(
                String.format("Failed to get status of extension: %s .", extensionName), e);
        }
    }

    /**
     * Performs request to install the extension, which has the given name.
     *
     * @param extensionName Name of the extension of which, installation is requested.
     * @param baseUrl       Base URL of Extensions Installer REST API.
     * @throws ExtensionsInstallerCliException Failed to install the extension.
     */
    public static void doInstall(String extensionName, String baseUrl) throws ExtensionsInstallerCliException {
        try {
            String command = String.format(INSTALL_CURL_FORMAT, baseUrl, extensionName);
            logger.info(String.format("Installing %s ...", extensionName));
            String response = executeCurl(command);
            ResponseHandler.handleInstallationResponse(response);
        } catch (IOException e) {
            throw new ExtensionsInstallerCliException(
                String.format("Failed to install extension: %s .", extensionName), e);
        }
    }

    /**
     * Performs request to check whether the extension which has the given name,
     * shares any dependencies with other extensions.
     *
     * @param extensionName Name of the extension, which is checked for dependency sharing.
     * @param baseUrl       Base URL of Extensions Installer REST API.
     * @return Whether the extension shares dependencies with other extensions.
     * @throws ExtensionsInstallerCliException Failed to get dependency sharing extensions.
     */
    public static boolean isDependencySharingExtensionsAvailable(String extensionName, String baseUrl)
        throws ExtensionsInstallerCliException {
        try {
            String command = String.format(DEPENDENCY_SHARING_EXTENSIONS_CURL_FORMAT, baseUrl, extensionName);
            String response = executeCurl(command);
            return ResponseHandler.isDependencySharingExtensionsAvailable(response);
        } catch (IOException e) {
            throw new ExtensionsInstallerCliException(
                String.format("Failed to get dependency sharing extensions for extension: %s .", extensionName), e);
        }
    }

    /**
     * Performs request to un-install the extension, which has the given name.
     *
     * @param extensionName Name of the extension of which, un-installation is requested.
     * @param baseUrl       Base URL of Extensions Installer REST API.
     * @throws ExtensionsInstallerCliException Failed to un-install the extension.
     */
    public static void doUnInstall(String extensionName, String baseUrl) throws ExtensionsInstallerCliException {
        try {
            String command = String.format(UN_INSTALL_CURL_FORMAT, baseUrl, extensionName);
            logger.info(String.format("Un-installing %s ...", extensionName));
            String response = executeCurl(command);
            ResponseHandler.handleUnInstallationResponse(response);
        } catch (IOException e) {
            throw new ExtensionsInstallerCliException(
                String.format("Failed to un-install extension: %s .", extensionName), e);
        }
    }
}
