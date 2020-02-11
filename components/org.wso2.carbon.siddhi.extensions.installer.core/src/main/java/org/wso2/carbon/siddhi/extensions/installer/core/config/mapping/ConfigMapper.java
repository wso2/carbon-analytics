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

package org.wso2.carbon.siddhi.extensions.installer.core.config.mapping;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.siddhi.extensions.installer.core.exceptions.ExtensionsInstallerException;
import org.wso2.carbon.siddhi.extensions.installer.core.config.mapping.models.ExtensionConfig;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Does mapping between the configuration file and Siddhi Extensions Installer.
 */
public class ConfigMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigMapper.class);

    private ConfigMapper() {
        // Prevents instantiation.
    }

    /**
     * Maps the given JSON configuration of extensions and their dependencies, to Java Object.
     *
     * @param configFileLocation Location of the JSON configuration file.
     * @return A map that contains extension configurations,
     * denoted by extension ids.
     * @throws ExtensionsInstallerException Failed to read and map configurations.
     */
    public static Map<String, ExtensionConfig> loadAllExtensionConfigs(String configFileLocation)
        throws ExtensionsInstallerException {
        Gson gson = new Gson();
        Type mapType = new TypeToken<Map<String, ExtensionConfig>>() {
        }.getType();
        try (Reader reader = new InputStreamReader(new FileInputStream(configFileLocation), StandardCharsets.UTF_8)) {
            return gson.fromJson(reader, mapType);
        } catch (FileNotFoundException e) {
            String errorMessage =
                String.format("Configuration file not found in: %s.", configFileLocation);
            LOGGER.error(errorMessage, e);
            throw new ExtensionsInstallerException(errorMessage, e);
        } catch (IOException e) {
            String errorMessage =
                String.format("Unable to read configuration file: %s.", configFileLocation);
            LOGGER.error(errorMessage, e);
            throw new ExtensionsInstallerException(errorMessage, e);
        }
    }

}
