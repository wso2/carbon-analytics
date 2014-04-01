/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.event.input.adaptor.http.internal.ds;

import org.osgi.service.http.HttpService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * common place to hold some OSGI service references.
 */
public final class HTTPEventAdaptorServiceValueHolder {

    private static RealmService realmService;
    private static HttpService httpService;

    private HTTPEventAdaptorServiceValueHolder() {
    }

    public static void registerRealmService(
            RealmService realmService) {
        HTTPEventAdaptorServiceValueHolder.realmService = realmService;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    public static void registerHTTPService(
            HttpService httpService) {
        HTTPEventAdaptorServiceValueHolder.httpService = httpService;
    }

    public static HttpService getHTTPService() {
        return httpService;
    }


}
