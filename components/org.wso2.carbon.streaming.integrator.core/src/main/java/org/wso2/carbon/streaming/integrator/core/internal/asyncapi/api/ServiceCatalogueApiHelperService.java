/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.streaming.integrator.core.internal.asyncapi.api;

import org.json.JSONObject;
import org.wso2.carbon.streaming.integrator.core.internal.exception.ServiceCatalogueAPIServiceStubException;

import java.io.File;

/**
 * Describes methods that communicate with the Error Handler API, exposed from the SI server.
 */
public interface ServiceCatalogueApiHelperService {

    JSONObject getKeyMd5s(String hostAndPort, String username, String password, String key)
        throws ServiceCatalogueAPIServiceStubException;

    boolean uploadAsyncAPIDef(File file, String hostAndPort, String username, String password)
        throws ServiceCatalogueAPIServiceStubException;

    boolean deleteAsyncAPIDef(String serviceKey, String hostAndPort, String username, String password)
        throws ServiceCatalogueAPIServiceStubException;

}
