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
package org.wso2.carbon.databridge.receiver.thrift.internal;

import org.osgi.service.http.HttpService;
import org.wso2.carbon.base.api.ServerConfigurationService;
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.databridge.receiver.thrift.ThriftDataReceiver;
import org.wso2.carbon.utils.ConfigurationContextService;

public class ServiceHolder {
    private static DataBridgeReceiverService dataBridgeReceiverService;
    private static ServerConfigurationService serverConfiguration;
    private static ConfigurationContextService configurationContext;
    private static ThriftDataReceiver dataReceiver;
    private static HttpService httpServiceInstance;

    public static DataBridgeReceiverService getDataBridgeReceiverService() {
        return dataBridgeReceiverService;
    }

    public static void setDataBridgeReceiverService(DataBridgeReceiverService dataBridgeReceiverService) {
        ServiceHolder.dataBridgeReceiverService = dataBridgeReceiverService;
    }

    public static ServerConfigurationService getServerConfiguration() {
        return serverConfiguration;
    }

    public static void setServerConfiguration(ServerConfigurationService serverConfiguration) {
        ServiceHolder.serverConfiguration = serverConfiguration;
    }

    public static ConfigurationContextService getConfigurationContext() {
        return configurationContext;
    }

    public static void setConfigurationContext(ConfigurationContextService configurationContext) {
        ServiceHolder.configurationContext = configurationContext;
    }

    public static HttpService getHttpServiceInstance() {
        return httpServiceInstance;
    }

    public static void setHttpServiceInstance(HttpService httpServiceInstance) {
        ServiceHolder.httpServiceInstance = httpServiceInstance;
    }

    public static ThriftDataReceiver getDataReceiver() {
        return dataReceiver;
    }

    public static void setDataReceiver(ThriftDataReceiver dataReceiver) {
        ServiceHolder.dataReceiver = dataReceiver;
    }
}
