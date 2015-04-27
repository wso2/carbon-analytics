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

package org.wso2.carbon.event.input.adapter.http.internal;

import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.input.adapter.core.exception.InputEventAdapterRuntimeException;
import org.wso2.carbon.event.input.adapter.http.HTTPEventAdapter;
import org.wso2.carbon.event.input.adapter.http.HTTPMessageServlet;
import org.wso2.carbon.event.input.adapter.http.internal.ds.HTTPEventAdapterServiceValueHolder;
import org.wso2.carbon.event.input.adapter.http.internal.util.HTTPEventAdapterConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.ServletException;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public final class HTTPEventAdapterManager {
    public static Map<String, List<HTTPEventAdapter>> ADAPTER_MAP = new ConcurrentHashMap<String, List<HTTPEventAdapter>>();

    private HTTPEventAdapterManager() {

    }

    public static synchronized void registerDynamicEndpoint(String adapterName, HTTPEventAdapter httpEventAdapter) {

        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

        String endpoint;
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            endpoint = HTTPEventAdapterConstants.ENDPOINT_PREFIX + adapterName;
        } else {
            endpoint = HTTPEventAdapterConstants.ENDPOINT_PREFIX + HTTPEventAdapterConstants.ENDPOINT_TENANT_KEY
                    + HTTPEventAdapterConstants.ENDPOINT_URL_SEPARATOR + tenantDomain
                    + HTTPEventAdapterConstants.ENDPOINT_URL_SEPARATOR + adapterName;
        }
        List<HTTPEventAdapter> adapterList = ADAPTER_MAP.get(endpoint);
        if (adapterList == null) {
            adapterList = new CopyOnWriteArrayList<HTTPEventAdapter>();
            try {
                adapterList.add(httpEventAdapter);
                HttpService httpService = HTTPEventAdapterServiceValueHolder.getHTTPService();
                httpService.registerServlet(endpoint,
                        new HTTPMessageServlet(endpoint, tenantId),
                        new Hashtable(),
                        httpService.createDefaultHttpContext());
                ADAPTER_MAP.put(endpoint, adapterList);
            } catch (ServletException e) {
                throw new InputEventAdapterRuntimeException("Error in registering endpoint " + endpoint, e);
            } catch (NamespaceException e) {
                throw new InputEventAdapterRuntimeException("Error in registering endpoint " + endpoint, e);
            }
            ADAPTER_MAP.put(endpoint, adapterList);
        } else {
            adapterList.add(httpEventAdapter);
        }
    }

    public static void unregisterDynamicEndpoint(String adapterName, HTTPEventAdapter httpEventAdapter) {
        HttpService httpService = HTTPEventAdapterServiceValueHolder.getHTTPService();
        String tenantDomain = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain();
        String endpoint;
        if (MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equals(tenantDomain)) {
            endpoint = HTTPEventAdapterConstants.ENDPOINT_PREFIX + adapterName;
        } else {
            endpoint = HTTPEventAdapterConstants.ENDPOINT_PREFIX + HTTPEventAdapterConstants.ENDPOINT_TENANT_KEY
                    + HTTPEventAdapterConstants.ENDPOINT_URL_SEPARATOR
                    + tenantDomain + HTTPEventAdapterConstants.ENDPOINT_URL_SEPARATOR + adapterName;
        }
        List<HTTPEventAdapter> adapterList = ADAPTER_MAP.get(endpoint);
        if (adapterList != null) {
            adapterList.remove(httpEventAdapter);
            httpEventAdapter.destroy();
            if (adapterList.size() == 0) {
                httpService.unregister(endpoint);
                ADAPTER_MAP.remove(endpoint);
            }

        }
    }
}
