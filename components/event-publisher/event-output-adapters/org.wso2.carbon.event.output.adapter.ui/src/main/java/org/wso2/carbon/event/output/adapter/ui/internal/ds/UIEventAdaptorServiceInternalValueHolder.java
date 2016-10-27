/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.event.output.adapter.ui.internal.ds;

import org.wso2.carbon.event.output.adapter.ui.UIAdaptorException;
import org.wso2.carbon.event.output.adapter.ui.UIOutputAuthorizationService;
import org.wso2.carbon.event.output.adapter.ui.internal.DefaultUIOutputAuthorizationServiceImpl;
import org.wso2.carbon.event.output.adapter.ui.internal.UIOutputCallbackControllerServiceImpl;
import org.wso2.carbon.event.output.adapter.ui.internal.util.UIEventAdapterConstants;
import org.wso2.carbon.event.stream.core.EventStreamService;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * Creates a holder of type UIOutputCallbackRegisterServiceImpl.
 */
public final class UIEventAdaptorServiceInternalValueHolder {

    private static UIOutputCallbackControllerServiceImpl UIOutputCallbackRegisterServiceImpl;
    private static ConcurrentHashMap<Integer, ConcurrentHashMap<String, String>> tenantSpecificOutputEventStreamAdapterMap = new
            ConcurrentHashMap<Integer, ConcurrentHashMap<String, String>>();
    private static ConcurrentHashMap<Integer, ConcurrentHashMap<String, LinkedBlockingDeque<Object>>> tenantSpecificStreamEventMap
            = new ConcurrentHashMap<Integer, ConcurrentHashMap<String, LinkedBlockingDeque<Object>>>();
    private static Map<String, UIOutputAuthorizationService> authorizationServiceRegistry = new HashMap<>();
    private static EventStreamService eventStreamService;

    public static void registerUIOutputCallbackRegisterServiceInternal(
            UIOutputCallbackControllerServiceImpl UIOutputCallbackRegisterServiceImpl) {
        UIEventAdaptorServiceInternalValueHolder.UIOutputCallbackRegisterServiceImpl =
                UIOutputCallbackRegisterServiceImpl;
    }

    public static UIOutputCallbackControllerServiceImpl getUIOutputCallbackRegisterServiceImpl() {
        return UIEventAdaptorServiceInternalValueHolder.UIOutputCallbackRegisterServiceImpl;
    }

    public static ConcurrentHashMap<Integer, ConcurrentHashMap<String, String>> getTenantSpecificOutputEventStreamAdapterMap() {
        return tenantSpecificOutputEventStreamAdapterMap;
    }

    public static ConcurrentHashMap<Integer, ConcurrentHashMap<String, LinkedBlockingDeque<Object>>>
    getTenantSpecificStreamEventMap() {
        return tenantSpecificStreamEventMap;
    }

    public static void registerAuthorizationService(UIOutputAuthorizationService authorizationService)
            throws UIAdaptorException {
        if (authorizationServiceRegistry.get(authorizationService.getAuthorizationServiceName()) == null) {
            authorizationServiceRegistry.put(authorizationService.getAuthorizationServiceName(), authorizationService);
        } else {
            throw new UIAdaptorException("Already UI Authorization Service Exists in name - "
                    + authorizationService.getAuthorizationServiceName()
                    + ", therefore cannot proceed with a new registration with same name");
        }
    }

    public static void unresgiterAuthorizationService(String serviceName){
        authorizationServiceRegistry.remove(serviceName);
    }

    public static UIOutputAuthorizationService getAuthorizationService(String serviceName){
        if (serviceName == null) {
            serviceName = UIEventAdapterConstants.DEFAULT_AUTHORIZATION_SERVICE_NAME;
        }
        return authorizationServiceRegistry.get(serviceName);
    }


    public static EventStreamService getEventStreamService() {
        return eventStreamService;
    }

    public static void setEventStreamService(EventStreamService eventStreamService) {
        UIEventAdaptorServiceInternalValueHolder.eventStreamService = eventStreamService;
    }
}
