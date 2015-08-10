/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.receiver.core.internal.tenantmgt;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.catalina.Context;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.tomcat.util.http.mapper.MappingData;
import org.wso2.carbon.core.multitenancy.utils.TenantAxisUtils;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;
import org.wso2.carbon.event.receiver.core.internal.ds.EventReceiverServiceValueHolder;
import org.wso2.carbon.tomcat.ext.valves.CarbonTomcatValve;
import org.wso2.carbon.tomcat.ext.valves.CompositeValve;
import org.wso2.carbon.user.core.tenant.TenantManager;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.servlet.http.HttpServletRequest;

public class TenantLazyLoaderValve extends CarbonTomcatValve {

    private static final Log log = LogFactory.getLog(TenantLazyLoaderValve.class);

    @Override
    public void invoke(Request request, Response response, CompositeValve compositeValve) {
        String requestURI = request.getRequestURI();

        String domain = MultitenantUtils.getTenantDomainFromRequestURL(requestURI);
        if (domain == null || domain.trim().length() == 0) {
            getNext().invoke(request, response, compositeValve);
            return;
        }
        if (!(requestURI.contains("/" + EventReceiverConstants.HTTP_RECEIVER_ENDPOINT_PREFIX + "/"))) {
            getNext().invoke(request, response, compositeValve);
            return;
        }

        try {
            TenantManager tenantManager = EventReceiverServiceValueHolder.getRealmService().getTenantManager();
            int tenantId = tenantManager.getTenantId(domain);
            if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
                if (log.isDebugEnabled()) {
                    log.debug("Tenant does not exist: " + domain);
                }
                getNext().invoke(request, response, compositeValve);
                return;
            }
        } catch (Exception e) {
            log.error("Error occurred while checking tenant existence", e);
            getNext().invoke(request, response, compositeValve);
            return;
        }
        ConfigurationContext serverConfigCtx = EventReceiverServiceValueHolder.getConfigurationContextService().getServerConfigContext();
        // Fixing NPE when server shutting down while requests keep coming in
        if (serverConfigCtx != null) {
            if (TenantAxisUtils.getLastAccessed(domain, serverConfigCtx) == -1) { // First time access
                try {
                    if (requestURI.contains("/" + EventReceiverConstants.HTTP_RECEIVER_ENDPOINT_PREFIX + "/")) {
                        remapRequest(request);
                    } else {
                        request.getRequestDispatcher(requestURI).forward(request, response);
                    }
                } catch (Exception e) {
                    String msg = "Cannot redirect tenant request to " + requestURI + " for tenant " + domain;
                    log.error(msg, e);
                    throw new RuntimeException(msg, e);
                }
            }
            setTenantAccessed(domain, serverConfigCtx);
            getNext().invoke(request, response, compositeValve);
        }
    }

    private void setTenantAccessed(String domain, ConfigurationContext serverConfigCtx) {
        ClassLoader tccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(TenantLazyLoaderValve.class.getClassLoader());
            TenantAxisUtils.setTenantAccessed(domain, serverConfigCtx);
        } finally {
            Thread.currentThread().setContextClassLoader(tccl);
        }
    }

    /**
     * This method is used in remapping a request with context at tomcat level. This is mainly used
     * with Lazy loading of tenants and Lazy loading of http receivers, where we can remap a request for a
     * lazy loaded http receiver so that any request (GET, POST) parameters will not get lost with the
     * first request.
     *
     * @param request - servlet request to be remapped for contexts
     * @throws Exception - on error
     */
    public static void remapRequest(HttpServletRequest request) throws Exception {
        Request connectorReq = (Request) request;

        MappingData mappingData = connectorReq.getMappingData();
        mappingData.recycle();

        connectorReq.getConnector().
                getMapper().map(connectorReq.getCoyoteRequest().serverName(),
                connectorReq.getCoyoteRequest().decodedURI(), null,
                mappingData);

        connectorReq.setContext((Context) connectorReq.getMappingData().context);
        connectorReq.setWrapper((Wrapper) connectorReq.getMappingData().wrapper);
    }
}
