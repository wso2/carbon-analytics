/*
* Copyright 2004,2013 The Apache Software Foundation.
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
package org.wso2.carbon.bam.webapp.stat.publisher.data;

import org.apache.axis2.context.ConfigurationContext;
import org.wso2.carbon.user.core.service.RealmService;

/*
*   This class will get the carbon server data.
*/

public class CarbonDataHolder {

    private static ConfigurationContext serverConfigContext;
    private static RealmService realmService;

    public static ConfigurationContext getServerConfigContext() {
        return serverConfigContext;
    }

    public static void setServerConfigContext(ConfigurationContext serverConfigContext) {
        CarbonDataHolder.serverConfigContext = serverConfigContext;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

    public static void setRealmService(RealmService realmService) {
        CarbonDataHolder.realmService = realmService;
    }
}
