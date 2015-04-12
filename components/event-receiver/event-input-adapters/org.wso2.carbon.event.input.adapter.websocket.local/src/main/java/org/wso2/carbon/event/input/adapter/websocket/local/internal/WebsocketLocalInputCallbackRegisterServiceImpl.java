/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.input.adapter.websocket.local.internal;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;
import org.wso2.carbon.event.input.adapter.websocket.local.WebsocketLocalInputCallbackRegisterService;

import java.util.concurrent.ConcurrentHashMap;

public class WebsocketLocalInputCallbackRegisterServiceImpl implements WebsocketLocalInputCallbackRegisterService {

    private ConcurrentHashMap<Integer, ConcurrentHashMap<String, InputEventAdapterListener>> inputEventAdaptorListenerMap;

    public WebsocketLocalInputCallbackRegisterServiceImpl(){
        inputEventAdaptorListenerMap = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, InputEventAdapterListener>>();
    }

    @Override
    public void updateAdapterListenerMap(String adaptorName, InputEventAdapterListener eventAdapterListener) {
        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ConcurrentHashMap<String,InputEventAdapterListener> tenantSpecificAdaptorMap = inputEventAdaptorListenerMap
                .get(tenantID);
        if(tenantSpecificAdaptorMap == null){
            tenantSpecificAdaptorMap = new ConcurrentHashMap<String, InputEventAdapterListener>();
            if(null != inputEventAdaptorListenerMap.putIfAbsent(tenantID, tenantSpecificAdaptorMap)){
                tenantSpecificAdaptorMap = inputEventAdaptorListenerMap.get(tenantID);
            }
        }
        tenantSpecificAdaptorMap.putIfAbsent(adaptorName, eventAdapterListener);
    }

    @Override
    public InputEventAdapterListener getAdapterListener(String adaptorName) {
        int tenantID = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ConcurrentHashMap<String,InputEventAdapterListener> tenantSpecificAdaptorMap = inputEventAdaptorListenerMap
                .get(tenantID);
        if(tenantSpecificAdaptorMap != null) {
            return tenantSpecificAdaptorMap.get(adaptorName);
        } else {
            return null;
        }
    }
}
