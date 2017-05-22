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


import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class DatabridgeConfigurationFileResolver {

    public static DataBridgeConfiguration resolveAndSetDatabridgeConfiguration(LinkedHashMap databridgeConfigHashMap) {
        DataBridgeConfiguration dataBridgeConfiguration = new DataBridgeConfiguration();

        dataBridgeConfiguration.setWorkerThreads(Integer.parseInt(databridgeConfigHashMap.get("workerThreads").toString()));
        dataBridgeConfiguration.setMaxEventBufferCapacity(Integer.parseInt(databridgeConfigHashMap.get("maxEventBufferCapacity").toString()));
        dataBridgeConfiguration.setEventBufferSize(Integer.parseInt(databridgeConfigHashMap.get("eventBufferSize").toString()));
        dataBridgeConfiguration.setClientTimeoutMin(Integer.parseInt(databridgeConfigHashMap.get("clientTimeoutMin").toString()));

        if (databridgeConfigHashMap.get("keyStoreLocation") != null) {
            dataBridgeConfiguration.setKeyStoreLocation((databridgeConfigHashMap.get("keyStoreLocation").toString()));
        }

        if (databridgeConfigHashMap.get("keyStorePassword") != null) {
            dataBridgeConfiguration.setKeyStorePassword((databridgeConfigHashMap.get("keyStorePassword").toString()));
        }

        List<DataReceiver> dataReceiverList = new ArrayList<>();
        for (Object dataReceiverObject : ((ArrayList) databridgeConfigHashMap.get("dataReceivers"))) {

            String type = ((LinkedHashMap) ((LinkedHashMap) dataReceiverObject).get("dataReceiver")).get("type").toString();
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
