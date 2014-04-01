/*
 *  Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.event.input.adaptor.http;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adaptor.core.InputEventAdaptorListener;
import org.wso2.carbon.event.input.adaptor.http.internal.ds.HTTPEventAdaptorServiceValueHolder;
import org.wso2.carbon.user.api.UserStoreManager;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class HTTPMessageServlet extends HttpServlet {

    private static final String AUTHORIZATION_HEADER = "Authorization";

    private static final String AUTH_MESSAGE_STORE_TENANT_ID = "AUTH_MESSAGE_STORE_TENANT_ID";

    private static final String AUTH_FAILURE_RESPONSE = "_AUTH_FAILURE_";

    private static Log log = LogFactory.getLog(HTTPMessageServlet.class);

    private String topic = "";

    public HTTPMessageServlet(String topic) {
        this.topic = topic;
    }

    private String[] getUserPassword(HttpServletRequest req) {
        String authHeader = req.getHeader(AUTHORIZATION_HEADER);
        if (authHeader == null) {
            return null;
        }
        if (!authHeader.startsWith("Basic ")) {
            return null;
        }
        String[] userPassword = new String(Base64.decode(authHeader.substring(6))).split(":");
        if (userPassword == null || userPassword.length != 2) {
            return null;
        }
        return userPassword;
    }

    private int checkAuthentication(HttpServletRequest req) {
        Object tidObj = req.getSession().getAttribute(AUTH_MESSAGE_STORE_TENANT_ID);
        if (tidObj != null) {
            return (Integer) tidObj;
        }
        String[] userPassword = this.getUserPassword(req);
        if (userPassword == null) {
            return -1;
        }
        String username = userPassword[0];
        String password = userPassword[1];

        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        String tenantAwareUserName = MultitenantUtils.getTenantAwareUsername(username);
        username = (tenantAwareUserName + "@" + tenantDomain).toLowerCase();
        RealmService realmService = HTTPEventAdaptorServiceValueHolder.getRealmService();
        int tenantId;
        try {
            tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
            if (tenantId == -1) {
                return -1;
            }
            UserStoreManager usm = realmService.getTenantUserRealm(tenantId).getUserStoreManager();
            boolean success = usm.authenticate(tenantAwareUserName, password);
            if (success) {
                req.getSession().setAttribute(AUTH_MESSAGE_STORE_TENANT_ID, tenantId);
                return tenantId;
            } else {
                return -1;
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("checkAuthentication() fail: " + e.getMessage(), e);
            }
            return -1;
        }
    }


    private String inputStreamToString(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buff = new byte[1024];
        int i;
        while ((i = in.read(buff)) > 0) {
            out.write(buff, 0, i);
        }
        out.close();
        return out.toString();
    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse res) throws IOException {

        String data = this.inputStreamToString(req.getInputStream());
        if (data == null) {
            return;
        }

        int tenantId = this.checkAuthentication(req);
        if (tenantId == -1) {
            res.getOutputStream().write(AUTH_FAILURE_RESPONSE.getBytes());
            return;
        } else {
            Map<String, InputEventAdaptorListener> adaptorListenerMap = HTTPEventEventAdaptorType.inputEventAdaptorListenerMap.get(topic);
            if (adaptorListenerMap != null) {
                for (InputEventAdaptorListener inputEventAdaptorListener : adaptorListenerMap.values()) {
                    log.info("Message : " + data);
                    inputEventAdaptorListener.onEvent(data);
                }
            }
        }

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse res) throws IOException {

        String data = this.inputStreamToString(req.getInputStream());
        if (data == null) {
            return;
        }

        int tenantId = this.checkAuthentication(req);
        if (tenantId == -1) {
            res.getOutputStream().write(AUTH_FAILURE_RESPONSE.getBytes());
            return;
        } else {
            Map<String, InputEventAdaptorListener> adaptorListenerMap = HTTPEventEventAdaptorType.inputEventAdaptorListenerMap.get(topic);
            if (adaptorListenerMap != null) {
                for (InputEventAdaptorListener inputEventAdaptorListener : adaptorListenerMap.values()) {
                    log.info("Message : " + data);
                    inputEventAdaptorListener.onEvent(data);
                }
            }
        }

        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId);
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }

    }


}
