/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.status.dashboard.core.api;

import feign.Client;
import feign.Feign;
import feign.auth.BasicAuthRequestInterceptor;
import feign.codec.Encoder;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.wso2.carbon.status.dashboard.core.api.utils.AMSSLSocketFactory;

/**
 * Rest API service which is used to access service stub for calling another worker.
 */
public class WorkerServiceFactory {
    // TODO: 11/15/17 Remove after fix UI server
    public static WorkerServiceStub getWorkerHttpClient(String url, String username, String password) {
        return Feign.builder()
                .requestInterceptor(new BasicAuthRequestInterceptor(username,
                        password))
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .target(WorkerServiceStub.class, url);
    }

    public static WorkerServiceStub getWorkerHttpsClient(String url, String username, String password,String
            kmCertAlias) {
        return Feign.builder()
                .requestInterceptor(new BasicAuthRequestInterceptor(username,
                        password))
                .encoder(new GsonEncoder())
                .decoder(new GsonDecoder())
                .client(new Client.Default(AMSSLSocketFactory.getSSLSocketFactory(kmCertAlias),
                        (hostname, sslSession) -> true))
                .target(WorkerServiceStub.class, url);
    }

}
