/*
 * Copyright 2017 WSO2 Inc. (http://wso2.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.siddhi.metrics.core;


import org.wso2.carbon.config.ConfigurationException;
import org.wso2.carbon.config.provider.ConfigProvider;
import org.wso2.carbon.config.provider.ConfigProviderImpl;
import org.wso2.carbon.config.reader.ConfigFileReader;
import org.wso2.carbon.config.reader.YAMLBasedConfigFileReader;
import org.wso2.carbon.secvault.SecureVault;
import org.wso2.carbon.secvault.exception.SecureVaultException;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


/**
 * Test Utilities.
 */
public class TestUtils {

    public static ConfigProvider getConfigProvider(String file) throws ConfigurationException {
        SecureVault secureVault = mock(SecureVault.class);
        try {
            when(secureVault.resolve(anyString())).thenReturn("n3wP4s5w0r4".toCharArray());
        } catch (SecureVaultException e) {
            throw new ConfigurationException("Error resolving secure vault", e);
        }
        Path carbonHome = Paths.get("");
        carbonHome = Paths.get(carbonHome.toString(), "src", "test");
        System.setProperty("carbon.home", carbonHome.toString());
        String filePath = carbonHome.toAbsolutePath() + File.separator + "resources" + File.separator + "conf" +
                File.separator + file;
        Path configurationFilePath = Paths.get(URI.create("file:" + filePath));
        ConfigFileReader configFileReader = new YAMLBasedConfigFileReader(configurationFilePath);
        return new ConfigProviderImpl(configFileReader, secureVault);
    }

}
