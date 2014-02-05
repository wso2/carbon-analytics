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
package org.wso2.carbon.bam.utils.persistence;

import org.wso2.carbon.bam.utils.internal.UtilsServiceComponent;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class QueryManagerFactory {

    private static QueryManagerFactory instance;
    private Map<Integer, QueryManager> managerPool = new ConcurrentHashMap<Integer, QueryManager>();

    public synchronized static QueryManagerFactory getInstance() {
        if (instance == null) {
            instance = new QueryManagerFactory();
        }

        return instance;
    }

    public QueryManager getQueryManager(String userName) throws InitializationException {

        if (userName == null) {
            return null;
        }

        String domain = MultitenantUtils.getTenantDomain(userName);

        int tenantId;
        try {
            tenantId = UtilsServiceComponent.getRealmService().getTenantManager().getTenantId(domain);
        } catch (UserStoreException e) {
            throw new InitializationException("Unable to get tenant information..", e);
        }

        //int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        return managerPool.get(tenantId);
    }

    public QueryManager initializeQueryManager(Map<String, String> credentials)
            throws InitializationException {

        String userName = credentials.get(PersistencyConstants.USER_NAME);

        if (userName == null) {
            return null;
        }
        
        String domain = MultitenantUtils.getTenantDomain(userName);

        int tenantId;
        try {
            tenantId = UtilsServiceComponent.getRealmService().getTenantManager().getTenantId(domain);
        } catch (UserStoreException e) {
            throw new InitializationException("Unable to get tenant information..", e);
        }

        QueryManager manager = new QueryManager();
        manager.initializeManager(credentials, tenantId);

/*        // ToDO: Get tenant ID from credentials
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();*/
        managerPool.put(tenantId, manager);

        return manager;
    }
}
