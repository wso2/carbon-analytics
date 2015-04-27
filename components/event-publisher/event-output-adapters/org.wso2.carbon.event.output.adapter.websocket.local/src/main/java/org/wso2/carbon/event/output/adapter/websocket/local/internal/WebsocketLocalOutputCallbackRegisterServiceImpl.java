/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.event.output.adapter.websocket.local.internal;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.output.adapter.websocket.local.WebsocketLocalOutputCallbackRegisterService;

import javax.websocket.Session;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class WebsocketLocalOutputCallbackRegisterServiceImpl implements WebsocketLocalOutputCallbackRegisterService {

    private ConcurrentHashMap<Integer, ConcurrentHashMap<String, CopyOnWriteArrayList<Session>>> outputEventAdaptorSessionMap;

    public WebsocketLocalOutputCallbackRegisterServiceImpl(){
        outputEventAdaptorSessionMap =
                new ConcurrentHashMap<Integer, ConcurrentHashMap<String, CopyOnWriteArrayList<Session>>>();
    }


    public void subscribe(String adaptorName, Session session) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ConcurrentHashMap<String, CopyOnWriteArrayList<Session>> tenantSpecificAdaptorMap = outputEventAdaptorSessionMap.get(tenantId);
        if (tenantSpecificAdaptorMap == null) {
            tenantSpecificAdaptorMap = new ConcurrentHashMap<String, CopyOnWriteArrayList<Session>>();
            if (null != outputEventAdaptorSessionMap.putIfAbsent(tenantId, tenantSpecificAdaptorMap)){
                tenantSpecificAdaptorMap = outputEventAdaptorSessionMap.get(tenantId);
            }
        }
        CopyOnWriteArrayList<Session> adapterSpecificSessions = tenantSpecificAdaptorMap.get(adaptorName);
        if (adapterSpecificSessions == null){
            adapterSpecificSessions = new CopyOnWriteArrayList<Session>();
            if (null != tenantSpecificAdaptorMap.putIfAbsent(adaptorName,adapterSpecificSessions)){
                adapterSpecificSessions = tenantSpecificAdaptorMap.get(adaptorName);
            }
        }
        adapterSpecificSessions.add(session);
    }


    public CopyOnWriteArrayList<Session> getSessions(int tenantId, String adaptorName){
        ConcurrentHashMap<String, CopyOnWriteArrayList<Session>> tenantSpecificAdaptorMap = outputEventAdaptorSessionMap.get(tenantId);
        if (tenantSpecificAdaptorMap != null) {
            CopyOnWriteArrayList<Session> adapterSpecificSessions = tenantSpecificAdaptorMap.get(adaptorName);
            return adapterSpecificSessions;
        }
        return null;
    }

    public void unsubscribe(String adaptorName, Session session) {
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        ConcurrentHashMap<String, CopyOnWriteArrayList<Session>> tenantSpecificAdaptorMap = outputEventAdaptorSessionMap.get(tenantId);
        if (tenantSpecificAdaptorMap != null) {
            CopyOnWriteArrayList<Session> adapterSpecificSessions = tenantSpecificAdaptorMap.get(adaptorName);
            if (adapterSpecificSessions != null) {
                Session sessionToRemove = null;
                for (Iterator<Session> iterator = adapterSpecificSessions.iterator(); iterator.hasNext(); ) {
                    Session thisSession = iterator.next();
                    if (session.getId().equals(thisSession.getId())) {
                        sessionToRemove = session;
                        break;
                    }
                }
                if (sessionToRemove != null) {
                    adapterSpecificSessions.remove(sessionToRemove);
                }
            }
        }
    }
}
