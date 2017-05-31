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
import org.wso2.carbon.databridge.core.DataBridgeReceiverService;
import org.wso2.carbon.databridge.receiver.thrift.ThriftDataReceiver;
import org.wso2.carbon.kernel.CarbonRuntime;

public class ServiceHolder {
    private static DataBridgeReceiverService dataBridgeReceiverService;
    private static ThriftDataReceiver dataReceiver;
    private static CarbonRuntime carbonRuntime;

    public static DataBridgeReceiverService getDataBridgeReceiverService() {
        return dataBridgeReceiverService;
    }

    public static void setDataBridgeReceiverService(DataBridgeReceiverService dataBridgeReceiverService) {
        ServiceHolder.dataBridgeReceiverService = dataBridgeReceiverService;
    }

    public static CarbonRuntime getCarbonRuntime() {
        return carbonRuntime;
    }

    public static void setCarbonRuntime(CarbonRuntime carbonRuntime) {
        ServiceHolder.carbonRuntime = carbonRuntime;
    }

    public static ThriftDataReceiver getDataReceiver() {
        return dataReceiver;
    }

    public static void setDataReceiver(ThriftDataReceiver dataReceiver) {
        ServiceHolder.dataReceiver = dataReceiver;
    }
}
