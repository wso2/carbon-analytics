/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;


@ServerEndpoint(value = "/t/{tdomain}/{adaptername}")
public class TenantDataReceiverEndpoint extends DataReceiverEndpoint {

    private int tenantId;

    private static final Log log = LogFactory.getLog(TenantDataReceiverEndpoint.class);

    @OnOpen
    public void onOpen (Session session, @PathParam("tdomain") String tdomain, @PathParam("adaptername") String adapterName) {
        if (log.isDebugEnabled()) {
            log.debug("WebSocket opened with session id: "+session.getId()+", for the tenant domain:"+tdomain+", for the adapter"+adapterName);
        }
        PrivilegedCarbonContext carbonContext = PrivilegedCarbonContext.getThreadLocalCarbonContext();
        carbonContext.setTenantDomain(tdomain,true);
        tenantId = carbonContext.getTenantId();
    }

    @OnMessage
    public void onMessage (Session session, String message, @PathParam("tdomain") String tdomain, @PathParam("adaptername") String adapterName){
        if (log.isDebugEnabled()) {
                log.debug("Received message: " + message+", for session id: "+session.getId()+", for tenant domain"+tdomain+", for the adapter:"+adapterName);
        }
        PrivilegedCarbonContext.startTenantFlow();
        PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
        InputEventAdapterListener adapterListener = websocketLocalInputCallbackRegisterService.getAdapterListener(adapterName);
        if(adapterListener != null){
            adapterListener.onEvent(message);
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Dropping the message:"+message+" from session id: "+session.getId()+", because no input websocket-local adapter exists" +
                        "with name '"+adapterName+"', for tenant id: " + tenantId + ", and tenant domain: " + tdomain);
            }
        }
        PrivilegedCarbonContext.endTenantFlow();
    }

    @OnClose
    public void onClose (Session session, CloseReason reason) {
        super.onClose(session,reason);
    }

    @OnError
    public void onError (Session session, Throwable throwable) {
        super.onError(session,throwable);
    }
}
