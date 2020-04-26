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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Scanner;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for executing the Extension Installer CLI. This is used to handle the user given commands appropriately.
 */
public class ExtensionsInstallerCli {

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionsInstallerCli.class);

    private static final String STATUS_COMMAND = "status";
    private static final String INSTALL_COMMAND = "install";
    private static final String UNINSTALL_COMMAND = "uninstall";
    private static final String EXTENSION_INSTALLER_ENDPOINT_URL_FORMAT = "%s:%s/siddhi-extensions";
    private static final String CONFIG_FILE_NAME = "config.properties";
    private static final String SERVER_HOST_CONFIG_PROPERTY = "server.host";
    private static final String SERVER_PORT_CONFIG_PROPERTY = "server.port";
    private static final String COMMAND_USAGE_FORMAT = "%-50s%-50s";

    private static String serverHost;
    private static String serverPort;

    public static void main(String[] args) {
        try {
            resolveConfig();
            executeCommand(args);
        } catch (ExtensionsInstallerCliException e) {
            LOGGER.error("Unable to execute Extension Installer.", e);
        }
    }

    /**
     * Resolves values for server host and server port, from the config file.
     *
     * @throws ExtensionsInstallerCliException Error occurred while resolving values from the config file.
     */
    private static void resolveConfig() throws ExtensionsInstallerCliException {
        Properties properties = new Properties();
        InputStream inputStream = ExtensionsInstallerCli.class.getClassLoader().getResourceAsStream(CONFIG_FILE_NAME);
        if (inputStream != null) {
            try {
                properties.load(inputStream);
                if (properties.getProperty(SERVER_HOST_CONFIG_PROPERTY) != null &&
                    properties.getProperty(SERVER_PORT_CONFIG_PROPERTY) != null) {
                    serverHost = properties.getProperty(SERVER_HOST_CONFIG_PROPERTY);
                    serverPort = properties.getProperty(SERVER_PORT_CONFIG_PROPERTY);
                } else {
                    throw new ExtensionsInstallerCliException(
                        String.format("Both '%s' and '%s' properties are required in the config file.",
                            SERVER_HOST_CONFIG_PROPERTY, SERVER_PORT_CONFIG_PROPERTY));
                }
            } catch (IOException e) {
                throw new ExtensionsInstallerCliException("Unable to load the config file.", e);
            }
        } else {
            throw new ExtensionsInstallerCliException("Unable to find the config file.");
        }
    }

    /**
     * Executes the appropriate command, based on the given args.
     *
     * @param args Command line arguments given by the user.
     * @throws ExtensionsInstallerCliException Failed to execute the command.
     */
    private static void executeCommand(String[] args) throws ExtensionsInstallerCliException {
        if (args.length > 0) {
            switch (args[0]) {
                case STATUS_COMMAND:
                    executeStatusCommand(args);
                    break;
                case INSTALL_COMMAND:
                    executeInstallCommand(args);
                    break;
                case UNINSTALL_COMMAND:
                    executeUnInstallCommand(args);
                    break;
                default:
                    LOGGER.error(String.format("Unknown command: '%s'.", args[0]));
                    showAvailableCommands();
            }
        } else {
            LOGGER.error("No command specified.");
            showAvailableCommands();
        }
    }

    /**
     * Executes {@value STATUS_COMMAND} command, with the given args.
     *
     * @param args Command line arguments given by the user.
     * @throws ExtensionsInstallerCliException Failed to execute {@value STATUS_COMMAND} command.
     */
    private static void executeStatusCommand(String[] args) throws ExtensionsInstallerCliException {
        switch (args.length) {
            case 1:
                listStatuses();
                break;
            case 2:
                listStatus(args[1]);
                break;
            default:
                showCorrectUsages(STATUS_COMMAND, getStatusCommandUsage());
        }
    }

    /**
     * Executes {@value INSTALL_COMMAND} command, with the given args.
     *
     * @param args Command line arguments given by the user.
     * @throws ExtensionsInstallerCliException Failed to execute {@value INSTALL_COMMAND} command.
     */
    private static void executeInstallCommand(String[] args) throws ExtensionsInstallerCliException {
        if (args.length == 2) {
            install(args[1]);
        } else {
            showCorrectUsages(INSTALL_COMMAND, getInstallCommandUsage());
        }
    }

    /**
     * Executes {@value UNINSTALL_COMMAND} command, with the given args.
     *
     * @param args Command line arguments given by the user.
     * @throws ExtensionsInstallerCliException Failed to execute {@value UNINSTALL_COMMAND} command.
     */
    private static void executeUnInstallCommand(String[] args) throws ExtensionsInstallerCliException {
        if (args.length == 2) {
            String extensionName = args[1];
            if (doesShareDependencies(extensionName)) {
                confirmAndUnInstall(extensionName);
            } else {
                unInstall(extensionName);
            }
        } else {
            showCorrectUsages(UNINSTALL_COMMAND, getUnInstallCommandUsage());
        }
    }

    /**
     * Constructs the base URL of the Extensions Installer REST API.
     *
     * @return Base URL of the Extension Installer REST API.
     */
    private static String constructExtensionInstallerBaseUrl() {
        return String.format(EXTENSION_INSTALLER_ENDPOINT_URL_FORMAT, serverHost, serverPort);
    }

    private static void listStatuses() throws ExtensionsInstallerCliException {
        RequestHandler.doGetAllExtensionStatuses(constructExtensionInstallerBaseUrl());
    }

    private static void listStatus(String extensionName) throws ExtensionsInstallerCliException {
        RequestHandler.doGetExtensionStatus(extensionName, constructExtensionInstallerBaseUrl());
    }

    private static void install(String extensionName) throws ExtensionsInstallerCliException {
        RequestHandler.doInstall(extensionName, constructExtensionInstallerBaseUrl());
    }

    private static boolean doesShareDependencies(String extensionName) throws ExtensionsInstallerCliException {
        return RequestHandler
            .isDependencySharingExtensionsAvailable(extensionName, constructExtensionInstallerBaseUrl());
    }

    private static void confirmAndUnInstall(String extensionName) throws ExtensionsInstallerCliException {
        Scanner input = new Scanner(System.in, StandardCharsets.UTF_8.name());
        LOGGER.warn("Are you sure you want to un-install? [y/n] ");
        String choice = input.next().trim();
        if ("y".equalsIgnoreCase(choice) || "yes".equalsIgnoreCase(choice)) {
            unInstall(extensionName);
        }
    }

    private static void unInstall(String extensionName) throws ExtensionsInstallerCliException {
        RequestHandler.doUnInstall(extensionName, constructExtensionInstallerBaseUrl());
    }

    private static String getStatusCommandUsage() {
        return String.format(COMMAND_USAGE_FORMAT, STATUS_COMMAND, "List installation statuses of all extensions") +
            System.lineSeparator() +
            String.format(COMMAND_USAGE_FORMAT, STATUS_COMMAND + " [extension name]",
                "List installation status and manually installable dependencies (if any) of an extension");
    }

    private static String getInstallCommandUsage() {
        return String.format(COMMAND_USAGE_FORMAT, INSTALL_COMMAND + " [extension name]", "Install an extension");
    }

    private static String getUnInstallCommandUsage() {
        return String.format(COMMAND_USAGE_FORMAT, UNINSTALL_COMMAND + " [extension name]", "Un-install an extension");
    }

    private static void showAvailableCommands() {
        String message = "Available commands:" + System.lineSeparator() + getStatusCommandUsage() +
            System.lineSeparator() + getInstallCommandUsage() + System.lineSeparator() + getUnInstallCommandUsage();
        LOGGER.info(message);
    }

    private static void showCorrectUsages(String command, String expectedUsages) {
        String message = String.format("Invalid usage of command: '%s'.", command) +
            System.lineSeparator() + "Available usage(s): " + System.lineSeparator() + expectedUsages;
        LOGGER.error(message);
    }
}
