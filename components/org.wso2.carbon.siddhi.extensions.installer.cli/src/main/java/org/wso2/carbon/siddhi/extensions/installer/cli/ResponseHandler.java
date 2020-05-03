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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Handles responses of requests, made by the {@link RequestHandler}.
 */
public class ResponseHandler {

    private static final Logger logger = LoggerFactory.getLogger(ResponseHandler.class);

    private static final String EXTENSION_INFO = "extensionInfo";
    private static final String NAME = "name";
    private static final String DISPLAY_NAME = "displayName";
    private static final String VERSION = "version";
    private static final String DOWNLOAD = "download";
    private static final String INSTRUCTIONS = "instructions";
    private static final String EXTENSION_STATUS = "extensionStatus";
    private static final String INSTALLED = "INSTALLED";
    private static final String MANUALLY_INSTALL = "manuallyInstall";
    private static final String STATUS = "status";
    private static final String FAILED = "failed";
    private static final String DOES_SHARE_DEPENDENCIES = "doesShareDependencies";
    private static final String SHARES_WITH = "sharesWith";
    private static final String EXTENSION_ROW_FORMAT = "%-50s%-30s%-20s";

    private ResponseHandler() {
        // Prevents Instantiation.
    }

    /**
     * Parses the given response to JSON, and returns it as a {@link JsonElement}.
     *
     * @param response Response string, that was obtained from a request.
     * @return Parsed response.
     * @throws ExtensionsInstallerCliException The given response can not be parsed to JSON.
     */
    private static JsonElement parseResponse(String response) throws ExtensionsInstallerCliException {
        try {
            return new JsonParser().parse(response);
        } catch (JsonSyntaxException e) {
            /*
            During erroneous cases such as requesting status of an extension whose configuration does not exist,
            the response will be a string (error message), not a JSON object.
            We identify such a response by attempting to parse that as a JSON, and failing.
            */
            throw new ExtensionsInstallerCliException(response, e);
        }
    }

    /**
     * Notifies installed extensions, from the given response.
     *
     * @param response The response received by the corresponding request.
     * @throws ExtensionsInstallerCliException Failed to parse the given response.
     */
    public static void handleInstalledExtensionsResponse(String response) throws ExtensionsInstallerCliException {
        JsonElement parsedResponse = parseResponse(response);
        if (parsedResponse instanceof JsonObject) {
            StringBuilder message = new StringBuilder();
            message.append(System.lineSeparator());
            message.append(" (*) - Contains manually installable dependencies");
            message.append(System.lineSeparator());
            message.append(getExtensionRowHeader());
            message.append(System.lineSeparator());
            ((JsonObject) parsedResponse).entrySet().stream().filter(extension -> {
                String status = ((JsonObject) extension.getValue()).get(EXTENSION_STATUS).getAsString();
                return INSTALLED.equals(status);
            }).forEach(installedExtension -> {
                message.append(getExtensionEntryAsRow(installedExtension));
                message.append(System.lineSeparator());
            });
            logger.info(message.toString());
        } else {
            // Ideally we won't reach here. This would mean that the parsed response is not a JsonObject.
            throw new ExtensionsInstallerCliException("Invalid response for listing installed extensions.");
        }
    }

    /**
     * Notifies installation statuses of all extensions, from the given response.
     *
     * @param response The response received by the corresponding request.
     * @throws ExtensionsInstallerCliException Failed to parse the given response.
     */
    public static void handleAllExtensionsResponse(String response) throws ExtensionsInstallerCliException {
        JsonElement parsedResponse = parseResponse(response);
        if (parsedResponse instanceof JsonObject) {
            StringBuilder message = new StringBuilder();
            message.append(System.lineSeparator());
            message.append(" (*) - Contains manually installable dependencies");
            message.append(System.lineSeparator());
            message.append(getExtensionRowHeader());
            message.append(System.lineSeparator());
            // Gather each extension's details.
            for (Map.Entry<String, JsonElement> extension : ((JsonObject) parsedResponse).entrySet()) {
                // Append the row which represents an extension's status.
                message.append(getExtensionEntryAsRow(extension));
                message.append(System.lineSeparator());
            }
            logger.info(message.toString());
        } else {
            // Ideally we won't reach here. This would mean that the parsed response is not a JsonObject.
            throw new ExtensionsInstallerCliException("Invalid response for listing all extensions.");
        }
    }

    private static String getExtensionEntryAsRow(Map.Entry<String, JsonElement> extensionEntry) {
        String name = extensionEntry.getKey();
        String displayName = ((JsonObject) extensionEntry.getValue()).get(EXTENSION_INFO).getAsJsonObject()
            .get(DISPLAY_NAME).getAsString();
        String version = ((JsonObject) extensionEntry.getValue()).get(EXTENSION_INFO).getAsJsonObject()
            .get(VERSION).getAsString();
        String status = ((JsonObject) extensionEntry.getValue()).get(EXTENSION_STATUS).getAsString();
        return getExtensionRow(name, displayName, version, status,
            ((JsonObject) extensionEntry.getValue()).keySet().contains(MANUALLY_INSTALL));
    }

    /**
     * Notifies the following details of the extension with the given name, from the given response.
     * 1. Status of the extension.
     * 2. Installation instructions for manually installable dependencies (if any).
     *
     * @param response      The response received by the corresponding request.
     * @param extensionName Name of the extension of which, installation status is requested.
     * @throws ExtensionsInstallerCliException Failed to parse the given response.
     */
    public static void handleExtensionResponse(String response, String extensionName)
        throws ExtensionsInstallerCliException {
        JsonElement parsedResponse = parseResponse(response);
        if (parsedResponse instanceof JsonObject) {
            String displayName = ((JsonObject) parsedResponse).get(EXTENSION_INFO).getAsJsonObject()
                .get(DISPLAY_NAME).getAsString();
            String version = ((JsonObject) parsedResponse).get(EXTENSION_INFO).getAsJsonObject()
                .get(VERSION).getAsString();
            String status = ((JsonObject) parsedResponse).get(EXTENSION_STATUS).getAsString();
            StringBuilder message = new StringBuilder();
            message.append(System.lineSeparator());
            message.append(getExtensionRowHeader());
            message.append(System.lineSeparator());
            // Get information of manually installable dependencies (if any).
            if (((JsonObject) parsedResponse).get(MANUALLY_INSTALL) != null) {
                message.append(getExtensionRow(extensionName, displayName, version, status, true));
                message.append(System.lineSeparator());
                message.append(System.lineSeparator());
                message.append(getManuallyInstallMessage((JsonObject) parsedResponse));
            } else {
                message.append(getExtensionRow(extensionName, displayName, version, status, false));
            }
            logger.info(message.toString());
        } else {
            // Ideally we won't reach here. This would mean that the parsed response is not a JsonObject.
            throw new ExtensionsInstallerCliException("Invalid response for listing an extension.");
        }
    }

    private static String getExtensionRowHeader() {
        return String.format(EXTENSION_ROW_FORMAT, "EXTENSION", "NAME", "STATUS");
    }

    private static String getExtensionRow(String extensionName, String displayName, String version, String status,
                                          boolean hasManuallyInstall) {
        String formattedDisplayName = String.format("%s %s", displayName, version);
        return String.format(EXTENSION_ROW_FORMAT,
            formattedDisplayName, extensionName, hasManuallyInstall ? status + " (*)" : status);
    }

    /**
     * Gets missing extension names from the given response.
     *
     * @param response The response received by the corresponding request.
     * @return Names of missing extensions.
     * @throws ExtensionsInstallerCliException Failed to get missing extension names.
     */
    public static Set<String> getMissingExtensionNames(String response) throws ExtensionsInstallerCliException {
        JsonElement parsedResponse = parseResponse(response);
        if (parsedResponse instanceof JsonObject) {
            Map<String, JsonElement> missingExtensions =
                ((JsonObject) parsedResponse).entrySet().stream().filter(extension -> {
                    String status = ((JsonObject) extension.getValue()).get(EXTENSION_STATUS).getAsString();
                    return !INSTALLED.equals(status);
                }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            if (!missingExtensions.isEmpty()) {
                StringBuilder message = new StringBuilder();
                message.append(System.lineSeparator());
                message.append("The following extensions have been used in Siddhi apps, but have not been installed.");
                message.append(System.lineSeparator());
                message.append(System.lineSeparator());
                message.append(getExtensionRowHeader());
                message.append(System.lineSeparator());
                for (Map.Entry<String, JsonElement> extensionEntry : missingExtensions.entrySet()) {
                    message.append(getExtensionEntryAsRow(extensionEntry));
                    message.append(System.lineSeparator());
                }
                message.append(System.lineSeparator());
                logger.info(message.toString());
                return missingExtensions.keySet();
            }
            return Collections.emptySet();
        }
        // Ideally we won't reach here. This would mean that the parsed response is not a JsonObject.
        throw new ExtensionsInstallerCliException("Invalid response for getting missing extension names.");
    }

    /**
     * Notifies information about an installation, from the given response.
     *
     * @param response The response received by the corresponding request.
     * @throws ExtensionsInstallerCliException Failed to parse the given response.
     */
    public static void handleExtensionInstallationResponse(String response) throws ExtensionsInstallerCliException {
        JsonElement parsedResponse = parseResponse(response);
        if (parsedResponse instanceof JsonObject) {
            StringBuilder message = new StringBuilder();
            message.append("Installation completed with status: ");
            message.append(((JsonObject) parsedResponse).get(STATUS).getAsString());
            message.append(".");
            message.append(System.lineSeparator());
            // Get information of dependencies of which, installation has been failed (if any).
            String failureOccurredMessage = getFailureOccurredMessage((JsonObject) parsedResponse);
            if (failureOccurredMessage != null) {
                message.append(System.lineSeparator());
                message.append(failureOccurredMessage);
            }
            // Get information of manually installable dependencies (if any).
            if (((JsonObject) parsedResponse).get(MANUALLY_INSTALL) != null) {
                message.append(System.lineSeparator());
                message.append(getManuallyInstallMessage((JsonObject) parsedResponse));
            }
            message.append(System.lineSeparator());
            logger.info(message.toString());
        } else {
            // Ideally we won't reach here. This would mean that the parsed response is not a JsonObject.
            throw new ExtensionsInstallerCliException("Invalid extension installation response.");
        }
    }

    private static String getManuallyInstallMessage(JsonObject parsedResponse) {
        StringBuilder message = new StringBuilder("The following dependencies should be manually installed: ");
        message.append(System.lineSeparator());
        message.append(System.lineSeparator());
        // Gather each dependency's details.
        for (JsonElement dependency : parsedResponse.getAsJsonArray(MANUALLY_INSTALL)) {
            message.append(printManuallyInstallableDependency(dependency));
        }
        return message.toString();
    }

    private static String printManuallyInstallableDependency(JsonElement dependency) {
        StringBuilder message = new StringBuilder();
        // Dependency information.
        message.append("  - ");
        message.append(dependency.getAsJsonObject().get(NAME).getAsString());
        message.append(" ");
        message.append(dependency.getAsJsonObject().get(VERSION).getAsString());
        message.append(":");
        message.append(System.lineSeparator());
        // Instructions.
        if (dependency.getAsJsonObject().getAsJsonObject(DOWNLOAD) != null) {
            String instructions =
                dependency.getAsJsonObject().getAsJsonObject(DOWNLOAD).get(INSTRUCTIONS).getAsString();
            instructions = instructions.replace("<br/>", System.lineSeparator());
            message.append(instructions);
            message.append(System.lineSeparator());
        }
        return message.toString();
    }

    private static String getFailureOccurredMessage(JsonObject parsedResponse) {
        if (parsedResponse.get(FAILED) != null) {
            StringBuilder message = new StringBuilder("Failure occurred with the following dependencies:");
            message.append(System.lineSeparator());

            // Gather each failed dependency's information.
            for (JsonElement failedDependency : parsedResponse.getAsJsonArray(FAILED)) {
                message.append(" ");
                message.append(failedDependency.getAsJsonObject().get(NAME).getAsString());
                message.append(" ");
                message.append(failedDependency.getAsJsonObject().get(VERSION).getAsString());
                message.append(System.lineSeparator());
            }
            return message.toString();
        }
        return null;
    }

    /**
     * Gets dependency sharing extension names from the given response.
     *
     * @param response The response received by the corresponding request.
     * @return Names of dependency sharing extensions.
     * @throws ExtensionsInstallerCliException Failed to get dependency sharing extensions.
     */
    public static Set<String> getDependencySharingExtensionNames(String response)
        throws ExtensionsInstallerCliException {
        JsonElement parsedResponse = parseResponse(response);
        if (parsedResponse instanceof JsonObject) {
            if (((JsonObject) parsedResponse).get(DOES_SHARE_DEPENDENCIES) != null &&
                ((JsonObject) parsedResponse).get(DOES_SHARE_DEPENDENCIES).getAsBoolean()) {
                Set<String> dependencySharingExtensionKeys =
                    ((JsonObject) parsedResponse).get(SHARES_WITH).getAsJsonObject().keySet();
                StringBuilder message = new StringBuilder();
                message.append(System.lineSeparator());
                message.append("The extension shares its dependencies with the following extensions: ");
                message.append(String.join(", ", dependencySharingExtensionKeys));
                message.append(System.lineSeparator());
                logger.warn(message.toString());
                return dependencySharingExtensionKeys;
            }
            return Collections.emptySet();
        }
        // Ideally we won't reach here. This would mean that the parsed response is not a JsonObject.
        throw new ExtensionsInstallerCliException("Invalid dependency sharing extensions response.");
    }

    /**
     * Notifies information about an un-installation, from the given response.
     *
     * @param response The response received by the corresponding request.
     * @throws ExtensionsInstallerCliException Failed to parse the given response.
     */
    public static void handleExtensionUnInstallationResponse(String response) throws ExtensionsInstallerCliException {
        JsonElement parsedResponse = parseResponse(response);
        if (parsedResponse instanceof JsonObject) {
            StringBuilder message = new StringBuilder();
            message.append("Un-installation completed with status: ");
            message.append(((JsonObject) parsedResponse).get(STATUS).getAsString());
            message.append(".");
            message.append(System.lineSeparator());
            // Get information of dependencies of which, un-installation has been failed (if any).
            String failureOccurredMessage = getFailureOccurredMessage((JsonObject) parsedResponse);
            if (failureOccurredMessage != null) {
                message.append(System.lineSeparator());
                message.append(failureOccurredMessage);
            }
            message.append(System.lineSeparator());
            logger.info(message.toString());
        } else {
            // Ideally we won't reach here. This would mean that the parsed response is not a JsonObject.
            throw new ExtensionsInstallerCliException("Invalid response for extension un-installation.");
        }
    }
}
