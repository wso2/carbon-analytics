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
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Set;

/**
 * Communicates with the Extensions Installer REST API, when requested by the {@link ExtensionsInstallerCli}.
 */
public class RequestHandler {

    private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

    private static final String HTTP_GET = "GET";
    private static final String HTTP_POST = "POST";
    private static final String HTTP_DELETE = "DELETE";
    private static final String GET_ALL_EXTENSION_STATUS_URL_FORMAT = "%s/status";
    private static final String GET_EXTENSION_STATUS_URL_FORMAT = "%s/%s/status";
    private static final String GET_USED_EXTENSION_STATUS_URL_FORMAT = "%s/status?isUsed=true";
    private static final String INSTALL_EXTENSION_URL_FORMAT = "%s/%s";
    private static final String GET_DEPENDENCY_SHARING_EXTENSIONS_URL_FORMAT = "%s/%s/dependency-sharing-extensions";
    private static final String UNINSTALL_EXTENSION_URL_FORMAT = "%s/%s";

    private RequestHandler() {
        // Prevents Instantiation.
    }

    private static String performRequest(String requestUrl, String requestMethod) throws IOException {
        URL url = new URL(requestUrl);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod(requestMethod);
        int status = con.getResponseCode();
        Reader streamReader = (status > 299) ? new InputStreamReader(con.getErrorStream(), StandardCharsets.UTF_8) :
            new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8);
        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(streamReader)) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }
        return response.toString();
    }

    /**
     * Performs request to get installed extensions.
     *
     * @param baseUrl Base URL of Extensions Installer REST API.
     * @throws ExtensionsInstallerCliException Failed to get installation statuses of extensions.
     */
    public static void doGetInstalledExtensions(String baseUrl) throws ExtensionsInstallerCliException {
        try {
            String response = performRequest(String.format(GET_ALL_EXTENSION_STATUS_URL_FORMAT, baseUrl), HTTP_GET);
            ResponseHandler.handleInstalledExtensionsResponse(response);
        } catch (IOException e) {
            throw new ExtensionsInstallerCliException("Failed to get installed extensions.", e);
        }
    }

    /**
     * Performs request to get installation statuses of all the extensions.
     *
     * @param baseUrl Base URL of Extensions Installer REST API.
     * @throws ExtensionsInstallerCliException Failed to get installation statuses of extensions.
     */
    public static void doGetAllExtensions(String baseUrl) throws ExtensionsInstallerCliException {
        try {
            String response = performRequest(String.format(GET_ALL_EXTENSION_STATUS_URL_FORMAT, baseUrl), HTTP_GET);
            ResponseHandler.handleAllExtensionsResponse(response);
        } catch (IOException e) {
            throw new ExtensionsInstallerCliException("Failed to get statuses of extensions.", e);
        }
    }

    /**
     * Performs request to get the details of the extension, which has the given name.
     *
     * @param extensionName Name of the extension of which, installation status is requested.
     * @param baseUrl       Base URL of Extensions Installer REST API.
     * @throws ExtensionsInstallerCliException Failed to get installation status of the extension.
     */
    public static void doGetExtension(String extensionName, String baseUrl)
        throws ExtensionsInstallerCliException {
        try {
            String response =
                performRequest(String.format(GET_EXTENSION_STATUS_URL_FORMAT, baseUrl, extensionName), HTTP_GET);
            ResponseHandler.handleExtensionResponse(response, extensionName);
        } catch (IOException e) {
            throw new ExtensionsInstallerCliException(
                String.format("Failed to get status of extension: %s .", extensionName), e);
        }
    }

    /**
     * Performs request to get names of extensions of which, usages are present in Siddhi apps, but are not installed.
     *
     * @param baseUrl Base URL of Extensions Installer REST API.
     * @return Names of missing extensions.
     * @throws ExtensionsInstallerCliException Failed to get missing extension names.
     */
    public static Set<String> getMissingExtensionNames(String baseUrl) throws ExtensionsInstallerCliException {
        try {
            String response = performRequest(String.format(GET_USED_EXTENSION_STATUS_URL_FORMAT, baseUrl), HTTP_GET);
            return ResponseHandler.getMissingExtensionNames(response);
        } catch (IOException e) {
            throw new ExtensionsInstallerCliException("Failed to get missing extension names.", e);
        }
    }

    /**
     * Performs request to install missing extensions, that are denoted by the given set of names.
     *
     * @param missingExtensionNames Names of missing extensions.
     * @param baseUrl               Base URL of Extensions Installer REST API.
     */
    public static void doInstallMissingExtensions(Set<String> missingExtensionNames, String baseUrl) {
        int installingExtensionCount = 1;
        for (String extensionName : missingExtensionNames) {
            try {
                doInstallExtension(extensionName, baseUrl);
                logger.info(String.format("Installed missing extension %s of %s.", installingExtensionCount,
                    missingExtensionNames.size()));
            } catch (ExtensionsInstallerCliException e) {
                logger.error(String.format(
                    "Failed to install extension: %s . Attempting to install the next extension.", extensionName), e);
            } finally {
                installingExtensionCount++;
            }
        }
    }

    /**
     * Performs request to install the extension, which has the given name.
     *
     * @param extensionName Name of the extension of which, installation is requested.
     * @param baseUrl       Base URL of Extensions Installer REST API.
     * @throws ExtensionsInstallerCliException Failed to install the extension.
     */
    public static void doInstallExtension(String extensionName, String baseUrl) throws ExtensionsInstallerCliException {
        try {
            logger.info(String.format("Installing %s ...", extensionName));
            String response =
                performRequest(String.format(INSTALL_EXTENSION_URL_FORMAT, baseUrl, extensionName), HTTP_POST);
            ResponseHandler.handleExtensionInstallationResponse(response);
        } catch (IOException e) {
            throw new ExtensionsInstallerCliException(
                String.format("Failed to install extension: %s .", extensionName), e);
        }
    }

    /**
     * Performs request to get names of extensions - that share dependencies with the extension that has the given name.
     *
     * @param extensionName Name of the extension of which, dependency sharing extension names are requested.
     * @param baseUrl       Base URL of Extensions Installer REST API.
     * @return Names of extensions that share dependencies with the extension that has the given name.
     * @throws ExtensionsInstallerCliException Failed to get dependency sharing extensions.
     */
    public static Set<String> getDependencySharingExtensionNames(String extensionName, String baseUrl)
        throws ExtensionsInstallerCliException {
        try {
            String response = performRequest(
                String.format(GET_DEPENDENCY_SHARING_EXTENSIONS_URL_FORMAT, baseUrl, extensionName), HTTP_GET);
            return ResponseHandler.getDependencySharingExtensionNames(response);
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
    public static void doUnInstallExtension(String extensionName, String baseUrl)
        throws ExtensionsInstallerCliException {
        try {
            logger.info(String.format("Un-installing %s ...", extensionName));
            String response = performRequest(
                String.format(UNINSTALL_EXTENSION_URL_FORMAT, baseUrl, extensionName), HTTP_DELETE);
            ResponseHandler.handleExtensionUnInstallationResponse(response);
        } catch (IOException e) {
            throw new ExtensionsInstallerCliException(
                String.format("Failed to un-install extension: %s .", extensionName), e);
        }
    }
}
