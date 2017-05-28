/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.databridge.core.conf;

/**
 * Resolves the databridge configuration file.
 */

import org.wso2.carbon.databridge.commons.utils.DataBridgeCommonsUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class DatabridgeConfigurationFileResolver {

    public static DataBridgeConfiguration resolveAndSetDatabridgeConfiguration(LinkedHashMap databridgeConfigHashMap) {
        DataBridgeConfiguration dataBridgeConfiguration = new DataBridgeConfiguration();

        Object workerThreadsObject = databridgeConfigHashMap.get("workerThreads");
        if (workerThreadsObject != null && !workerThreadsObject.toString().trim().isEmpty()) {
            dataBridgeConfiguration.setWorkerThreads(Integer.parseInt(workerThreadsObject.toString().trim()));
        }

        Object maxBufferCapacityObject = databridgeConfigHashMap.get("maxEventBufferCapacity");
        if (maxBufferCapacityObject != null && !maxBufferCapacityObject.toString().trim().isEmpty()) {
            dataBridgeConfiguration.setMaxEventBufferCapacity(Integer.parseInt(maxBufferCapacityObject.
                    toString().trim()));
        }

        Object eventBufferSizeObject = databridgeConfigHashMap.get("eventBufferSize");
        if (eventBufferSizeObject != null && !eventBufferSizeObject.toString().trim().isEmpty()) {
            dataBridgeConfiguration.setEventBufferSize(Integer.parseInt(eventBufferSizeObject.toString().trim()));
        }

        Object clientTimeoutMinObject = databridgeConfigHashMap.get("clientTimeoutMin");
        if (clientTimeoutMinObject != null && !clientTimeoutMinObject.toString().trim().isEmpty()) {
            dataBridgeConfiguration.setClientTimeoutMin(Integer.parseInt(clientTimeoutMinObject.toString().trim()));
        }

        Object keyStoreLocationObject = databridgeConfigHashMap.get("keyStoreLocation");
        if (keyStoreLocationObject != null) {
            String keyStoreLocation = keyStoreLocationObject.toString().trim();
            if (!keyStoreLocation.isEmpty()) {
                dataBridgeConfiguration.setKeyStoreLocation((DataBridgeCommonsUtils.
                        replaceSystemProperty(keyStoreLocation)));
            }
        }

        Object keyStorePassword = databridgeConfigHashMap.get("keyStorePassword");
        if (keyStorePassword != null && !keyStorePassword.toString().trim().isEmpty()) {
            dataBridgeConfiguration.setKeyStorePassword((keyStorePassword.toString().trim()));
        }

        List<DataReceiver> dataReceiverList = new ArrayList<>();
        for (Object dataReceiverObject : ((ArrayList) databridgeConfigHashMap.get("dataReceivers"))) {
            String type = ((LinkedHashMap) ((LinkedHashMap) dataReceiverObject).get("dataReceiver")).
                    get("type").toString();
            LinkedHashMap propertiesMap = ((LinkedHashMap) ((LinkedHashMap)
                                                                    ((LinkedHashMap) dataReceiverObject).
                                                                            get("dataReceiver")).get("properties"));
            DataReceiver dataReceiver = new DataReceiver(type, propertiesMap);
            dataReceiverList.add(dataReceiver);
        }
        dataBridgeConfiguration.setDataReceivers(dataReceiverList);

        return dataBridgeConfiguration;
    }

}
