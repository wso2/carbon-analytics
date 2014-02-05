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
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NoSQLDataStoreFactory implements DataStoreFactory {

    private static volatile NoSQLDataStoreFactory instance;

    private Map<Integer, DataStore> dataStorePool;

    private NoSQLDataStoreFactory() {
        dataStorePool = new ConcurrentHashMap<Integer, DataStore>();
    }

    public static  DataStoreFactory getInstance() {
        if (instance == null) {
            synchronized(NoSQLDataStoreFactory.class){
               if(instance == null){
                    instance = new NoSQLDataStoreFactory();
               }
            }
        }
        return instance;
    }

    @Override
    public DataStore getDataStore(String userName) throws DataStoreException {

        if (userName == null) {
            return null;
        }

        String domain = MultitenantUtils.getTenantDomain(userName);

        int tenantId;
        try {
            tenantId = UtilsServiceComponent.getRealmService().getTenantManager().getTenantId(domain);
        } catch (UserStoreException e) {
            throw new DataStoreException("Unable to get tenant information..", e);
        }

        //int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        return dataStorePool.get(tenantId);
    }

    @Override
    public DataStore initializeDataStore(Map<String, String> credentials, boolean force)
            throws InitializationException {

        String userName = credentials.get(PersistencyConstants.USER_NAME);
        if (userName == null) {
            return null;
        }

        DataStore store = new NoSQLDataStore();
        store.initializeStore(credentials, force);

        String domain = MultitenantUtils.getTenantDomain(userName);

        int tenantId;
        try {
            tenantId = UtilsServiceComponent.getRealmService().getTenantManager().getTenantId(domain);
        } catch (UserStoreException e) {
            throw new InitializationException("Unable to get tenant information..", e);
        }

        //int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        dataStorePool.put(tenantId, store);

        return store;

    }

}
