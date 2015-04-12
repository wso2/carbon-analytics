/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.event.input.adapter.wso2event.internal.ds;

import org.wso2.carbon.databridge.core.DataBridgeSubscriberService;
import org.wso2.carbon.event.input.adapter.wso2event.WSO2EventAdapter;

import java.util.concurrent.ConcurrentHashMap;

/**
 * common place to hold some OSGI bundle references.
 */
public final class WSO2EventAdapterServiceValueHolder {

    private static DataBridgeSubscriberService dataBridgeSubscriberService;

    private static ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, WSO2EventAdapter>>> inputEventAdapterListenerMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, ConcurrentHashMap<String, WSO2EventAdapter>>>();

    private WSO2EventAdapterServiceValueHolder() {
    }

    public static void registerDataBridgeSubscriberService(
            DataBridgeSubscriberService dataBridgeSubscriberService) {
        WSO2EventAdapterServiceValueHolder.dataBridgeSubscriberService = dataBridgeSubscriberService;
    }

    public static DataBridgeSubscriberService getDataBridgeSubscriberService() {
        return dataBridgeSubscriberService;
    }

    public static synchronized void registerAdapterService(String tenantDomain, String streamId, WSO2EventAdapter wso2EventAdapter) {
        ConcurrentHashMap<String, ConcurrentHashMap<String, WSO2EventAdapter>> tenantSpecificInputEventAdapterListenerMap = inputEventAdapterListenerMap.get(tenantDomain);
        if (tenantSpecificInputEventAdapterListenerMap == null) {
            tenantSpecificInputEventAdapterListenerMap = new ConcurrentHashMap<String, ConcurrentHashMap<String, WSO2EventAdapter>>();
            inputEventAdapterListenerMap.put(tenantDomain, tenantSpecificInputEventAdapterListenerMap);
        }
        ConcurrentHashMap<String, WSO2EventAdapter> streamSpecificInputEventAdapterListenerMap = tenantSpecificInputEventAdapterListenerMap.get(streamId);
        if (streamSpecificInputEventAdapterListenerMap == null) {
            streamSpecificInputEventAdapterListenerMap = new ConcurrentHashMap<String, WSO2EventAdapter>();
            tenantSpecificInputEventAdapterListenerMap.put(streamId, streamSpecificInputEventAdapterListenerMap);
        }
        streamSpecificInputEventAdapterListenerMap.put(wso2EventAdapter.getEventAdapterName(), wso2EventAdapter);
    }

    public static void unregisterAdapterService(String tenantDomain, String streamId, WSO2EventAdapter wso2EventAdapter) {
        ConcurrentHashMap<String, ConcurrentHashMap<String, WSO2EventAdapter>> tenantSpecificInputEventAdapterListenerMap = inputEventAdapterListenerMap.get(tenantDomain);
        if (tenantSpecificInputEventAdapterListenerMap != null) {
            ConcurrentHashMap<String, WSO2EventAdapter> streamSpecificInputEventAdapterListenerMap = tenantSpecificInputEventAdapterListenerMap.get(streamId);
            if (streamSpecificInputEventAdapterListenerMap != null) {
                streamSpecificInputEventAdapterListenerMap.remove(wso2EventAdapter.getEventAdapterName());
            }
        }

    }

    public static ConcurrentHashMap<String, WSO2EventAdapter> getAdapterService(String tenantDomain, String streamId) {
        ConcurrentHashMap<String, ConcurrentHashMap<String, WSO2EventAdapter>> tenantSpecificInputEventAdapterListenerMap = inputEventAdapterListenerMap.get(tenantDomain);
        if (tenantSpecificInputEventAdapterListenerMap != null) {
           return tenantSpecificInputEventAdapterListenerMap.get(streamId);
        }

        return null;
    }
}
