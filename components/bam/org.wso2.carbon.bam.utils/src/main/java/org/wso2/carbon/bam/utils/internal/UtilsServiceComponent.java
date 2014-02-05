/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.utils.internal;

import org.wso2.carbon.bam.utils.persistence.QueryUtils;
import org.wso2.carbon.cassandra.dataaccess.DataAccessService;
import org.wso2.carbon.user.core.service.RealmService;

/**
 * @scr.component name="bam.utils.component" immediate="true"
 * @scr.reference name="cassandra.service" interface="org.wso2.carbon.cassandra.dataaccess.DataAccessService"
 * cardinality="1..1" policy="dynamic" bind="setDataAccessService" unbind="unsetDataAccessService"
 * @scr.reference name="user.realmservice.default" interface="org.wso2.carbon.user.core.service.RealmService"
 * cardinality="1..1" policy="dynamic" bind="setRealmService"  unbind="unsetRealmService"
 */
public class UtilsServiceComponent {

    private static RealmService realmService = null;

    protected void setDataAccessService(DataAccessService dataAccessService) {
        QueryUtils.setDataAccessService(dataAccessService);
    }

    protected void unsetDataAccessService(DataAccessService dataAccessService) {
        QueryUtils.setDataAccessService(null);
    }

    protected static void setRealmService(RealmService realm) {
        realmService = realm;
    }

    protected static void unsetRealmService(RealmService realm) {
        realmService = null;
    }

    public static RealmService getRealmService() {
        return realmService;
    }

}
