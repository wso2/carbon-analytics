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

package org.wso2.carbon.event.input.adapter.http.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.http.HttpService;
import org.wso2.carbon.event.input.adapter.core.InputEventAdapterFactory;
import org.wso2.carbon.event.input.adapter.http.HTTPEventAdapterFactory;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="input.httpEventAdapterService.component" immediate="true"
 * @scr.reference name="user.realmservice.default"
 * interface="org.wso2.carbon.user.core.service.RealmService" cardinality="1..1"
 * policy="dynamic" bind="setRealmService" unbind="unsetRealmService"
 * @scr.reference name="http.service" interface="org.osgi.service.http.HttpService"
 * cardinality="1..1" policy="dynamic" bind="setHttpService" unbind="unsetHttpService"
 */


public class HTTPEventAdapterServiceDS {

    private static final Log log = LogFactory.getLog(HTTPEventAdapterServiceDS.class);

    /**
     * initialize the agent service here service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            InputEventAdapterFactory httpEventEventAdapterFactory = new HTTPEventAdapterFactory();
            context.getBundleContext().registerService(InputEventAdapterFactory.class.getName(), httpEventEventAdapterFactory, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed the input HTTP adapter service");
            }

        } catch (RuntimeException e) {
            log.error("Can not create the input HTTP adapter service ", e);
        }
    }

    protected void setRealmService(RealmService realmService) {
        HTTPEventAdapterServiceValueHolder.registerRealmService(realmService);
    }

    protected void unsetRealmService(RealmService realmService) {
        HTTPEventAdapterServiceValueHolder.registerRealmService(null);
    }

    protected void setHttpService(HttpService httpService) {
        HTTPEventAdapterServiceValueHolder.registerHTTPService(httpService);
    }

    protected void unsetHttpService(HttpService httpService) {
        HTTPEventAdapterServiceValueHolder.registerHTTPService(null);
    }

}
