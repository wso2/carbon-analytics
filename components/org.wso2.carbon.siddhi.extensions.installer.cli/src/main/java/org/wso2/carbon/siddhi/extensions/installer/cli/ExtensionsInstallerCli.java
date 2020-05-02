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
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for executing the Extension Installer CLI. This is used to handle the user given commands appropriately.
 */
public class ExtensionsInstallerCli {

    private static final Logger logger = LoggerFactory.getLogger(ExtensionsInstallerCli.class);

    private static final String LIST_COMMAND = "list";
    private static final String ALL_FLAG = "--all";
    private static final String INSTALL_COMMAND = "install";
    private static final String UNINSTALL_COMMAND = "uninstall";
    private static final String EXTENSION_INSTALLER_ENDPOINT_URL_FORMAT = "http://%s:%s/siddhi-extensions";
    private static final String CONFIG_FILE_NAME = "config.properties";
    private static final String SERVER_HOST_CONFIG_PROPERTY = "server.host";
    private static final String SERVER_PORT_CONFIG_PROPERTY = "server.port";
    private static final String COMMAND_USAGE_FORMAT = "  %-50s%-50s";

    private static String serverHost;
    private static String serverPort;

    public static void main(String[] args) {
        try {
            resolveConfig();
            executeCommand(args);
        } catch (ExtensionsInstallerCliException e) {
            logger.error("Unable to execute Extension Installer.", e);
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
                case LIST_COMMAND:
                    executeListCommand(args);
                    break;
                case INSTALL_COMMAND:
                    executeInstallCommand(args);
                    break;
                case UNINSTALL_COMMAND:
                    executeUnInstallCommand(args);
                    break;
                default:
                    logger.error(String.format("Unknown command: '%s'.", args[0]));
                    showAvailableCommands();
            }
        } else {
            showAvailableCommands();
        }
    }

    /**
     * Executes {@value LIST_COMMAND} command, with the given args.
     *
     * @param args Command line arguments given by the user.
     * @throws ExtensionsInstallerCliException Failed to execute {@value LIST_COMMAND} command.
     */
    private static void executeListCommand(String[] args) throws ExtensionsInstallerCliException {
        switch (args.length) {
            case 1:
                listInstalledExtensions();
                break;
            case 2:
                if (ALL_FLAG.equals(args[1])) {
                    listAllExtensions();
                } else {
                    listExtension(args[1]);
                }
                break;
            default:
                showCorrectUsages(LIST_COMMAND, getStatusCommandUsage());
        }
    }

    /**
     * Executes {@value INSTALL_COMMAND} command, with the given args.
     *
     * @param args Command line arguments given by the user.
     * @throws ExtensionsInstallerCliException Failed to execute {@value INSTALL_COMMAND} command.
     */
    private static void executeInstallCommand(String[] args) throws ExtensionsInstallerCliException {
        switch (args.length) {
            case 1:
                confirmAndInstallMissingExtensions();
                break;
            case 2:
                installExtension(args[1]);
                break;
            default:
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
            confirmAndUnInstall(args[1]);
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

    private static void listInstalledExtensions() throws ExtensionsInstallerCliException {
        RequestHandler.doGetInstalledExtensions(constructExtensionInstallerBaseUrl());
    }

    private static void listAllExtensions() throws ExtensionsInstallerCliException {
        RequestHandler.doGetAllExtensions(constructExtensionInstallerBaseUrl());
    }

    private static void listExtension(String extensionName) throws ExtensionsInstallerCliException {
        RequestHandler.doGetExtension(extensionName, constructExtensionInstallerBaseUrl());
    }

    private static void confirmAndInstallMissingExtensions() throws ExtensionsInstallerCliException {
        Set<String> missingExtensionNames =
            RequestHandler.getMissingExtensionNames(constructExtensionInstallerBaseUrl());
        if (!missingExtensionNames.isEmpty()) {
            Scanner input = new Scanner(System.in, StandardCharsets.UTF_8.name());
            logger.info("Are you sure you want to install missing extensions? [y/n] ");
            String choice = input.next().trim();
            if ("y".equalsIgnoreCase(choice) || "yes".equalsIgnoreCase(choice)) {
                RequestHandler.doInstallMissingExtensions(missingExtensionNames,
                    constructExtensionInstallerBaseUrl());
                logger.info("Please restart the server.");
            }
        } else {
            logger.info("All the required extensions have been already installed.");
        }
    }

    private static void installExtension(String extensionName) throws ExtensionsInstallerCliException {
        RequestHandler.doInstallExtension(extensionName, constructExtensionInstallerBaseUrl());
        logger.info("Please restart the server.");
    }

    private static void confirmAndUnInstall(String extensionName) throws ExtensionsInstallerCliException {
        Set<String> dependencySharingExtensionNames =
            RequestHandler.getDependencySharingExtensionNames(extensionName, constructExtensionInstallerBaseUrl());
        if (!dependencySharingExtensionNames.isEmpty()) {
            Scanner input = new Scanner(System.in, StandardCharsets.UTF_8.name());
            logger.warn("Are you sure you want to un-install? [y/n] ");
            String choice = input.next().trim();
            if ("y".equalsIgnoreCase(choice) || "yes".equalsIgnoreCase(choice)) {
                unInstallExtension(extensionName);
            }
        } else {
            unInstallExtension(extensionName);
        }
    }

    private static void unInstallExtension(String extensionName) throws ExtensionsInstallerCliException {
        RequestHandler.doUnInstallExtension(extensionName, constructExtensionInstallerBaseUrl());
        logger.info("Please restart the server.");
    }

    private static String getStatusCommandUsage() {
        return String.format(COMMAND_USAGE_FORMAT, LIST_COMMAND, "List installed extensions") +
            System.lineSeparator() +
            String.format(COMMAND_USAGE_FORMAT, LIST_COMMAND + " " + ALL_FLAG,
                "List installation statuses of all the extensions") +
            System.lineSeparator() +
            String.format(COMMAND_USAGE_FORMAT, LIST_COMMAND + " [extension name]",
                "List the installation status, and instructions of manually installable dependencies (if any) " +
                    "of an extension");
    }

    private static String getInstallCommandUsage() {
        return String.format(COMMAND_USAGE_FORMAT, INSTALL_COMMAND,
            "Install extensions that are used in Siddhi apps, but are not installed") +
            System.lineSeparator() +
            String.format(COMMAND_USAGE_FORMAT, INSTALL_COMMAND + " [extension name]", "Install an extension");
    }

    private static String getUnInstallCommandUsage() {
        return String.format(COMMAND_USAGE_FORMAT, UNINSTALL_COMMAND + " [extension name]", "Un-install an extension");
    }

    private static void showAvailableCommands() {
        String message = "Extension Installer manages Siddhi Extensions." + System.lineSeparator() +
            System.lineSeparator() + "Available Commands:" + System.lineSeparator() + getStatusCommandUsage() +
            System.lineSeparator() + getInstallCommandUsage() + System.lineSeparator() + getUnInstallCommandUsage();
        logger.info(message);
    }

    private static void showCorrectUsages(String command, String expectedUsages) {
        String message = String.format("Invalid usage of command: '%s'.", command) +
            System.lineSeparator() + "Available usage(s): " + System.lineSeparator() + expectedUsages;
        logger.error(message);
    }
}
