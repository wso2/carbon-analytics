/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.databridge.agent.test;

import java.io.File;

public class DataPublisherTestUtil {
    public static final String LOCAL_HOST = "localhost";

    public static void setTrustStoreParams() {
        File filePath = new File("src" + File.separator + "test" + File.separator + "resources");
        if (!filePath.exists()) {
            filePath = new File("components" + File.separator + "data-bridge" + File.separator + "org.wso2.carbon.databridge.agent" + File.separator + "src" + File.separator + "test" + File.separator + "resources");
        }
        if (!filePath.exists()) {
            filePath = new File("resources");
        }
        if (!filePath.exists()) {
            filePath = new File("test" + File.separator + "resources");
        }
        String trustStore = filePath.getAbsolutePath();
        System.setProperty("javax.net.ssl.trustStore", trustStore + File.separator + "client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

    }

    public static void setKeyStoreParams() {
        File filePath = new File("src" + File.separator + "test" + File.separator + "resources");
        if (!filePath.exists()) {
            filePath = new File("components" + File.separator + "data-bridge" + File.separator + "org.wso2.carbon.databridge.agent" + File.separator + "src" + File.separator + "test" + File.separator + "resources");
        }
        if (!filePath.exists()) {
            filePath = new File("resources");
        }
        if (!filePath.exists()) {
            filePath = new File("test" + File.separator + "resources");
        }
        String keyStore = filePath.getAbsolutePath();
        System.setProperty("Security.KeyStore.Location", keyStore + File.separator + "wso2carbon.jks");
        System.setProperty("Security.KeyStore.Password", "wso2carbon");
    }

    public static String getDataAgentConfigPath(String fileName) {
        File filePath = new File("src" + File.separator + "test" + File.separator + "resources");
        if (!filePath.exists()) {
            filePath = new File("components" + File.separator + "data-bridge" + File.separator + "org.wso2.carbon.databridge.agent" + File.separator + "src" + File.separator + "test" + File.separator + "resources");
        }
        if (!filePath.exists()) {
            filePath = new File("resources");
        }
        if (!filePath.exists()) {
            filePath = new File("test" + File.separator + "resources");
        }
        return filePath.getAbsolutePath() + File.separator + fileName;
    }

    public static String getDataBridgeConfigPath() {
        File filePath = new File("src" + File.separator + "test" + File.separator + "resources");
        if (!filePath.exists()) {
            filePath = new File("components" + File.separator + "data-bridge" + File.separator + "org.wso2.carbon.databridge.agent" + File.separator + "src" + File.separator + "test" + File.separator + "resources");
        }
        if (!filePath.exists()) {
            filePath = new File("resources");
        }
        if (!filePath.exists()) {
            filePath = new File("test" + File.separator + "resources");
        }
        return filePath.getAbsolutePath() + File.separator + "databridge.config.yaml";
    }

}
