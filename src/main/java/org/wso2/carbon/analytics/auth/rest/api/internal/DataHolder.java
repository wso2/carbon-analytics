/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.analytics.auth.rest.api.internal;

import org.wso2.carbon.analytics.idp.client.core.api.IdPClient;

/**
 * Data holder to hold reference to IdPClient OSGi service.
 */
public class DataHolder {
    private static DataHolder instance = new DataHolder();
    private IdPClient idPClient;

    private DataHolder() {
    }

    /**
     * Provide instance of DataHolder class.
     *
     * @return Instance of DataHolder
     */
    public static DataHolder getInstance() {
        return instance;
    }

    public IdPClient getIdPClient() {
        return idPClient;
    }

    public void setIdPClient(IdPClient idPClient) {
        this.idPClient = idPClient;
    }
}
