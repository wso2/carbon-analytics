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

package org.wso2.carbon.streaming.integrator.core.internal.asyncapi;

import feign.Response;
import feign.RetryableException;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.streaming.integrator.core.internal.asyncapi.api.ServiceCatalogueApiHelperService;
import org.wso2.carbon.streaming.integrator.core.internal.asyncapi.util.HTTPSClientUtil;
import org.wso2.carbon.streaming.integrator.core.internal.exception.ServiceCatalogueAPIServiceStubException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * Consists of methods that communicate with the Error Handler API, exposed from the SI server.
 */
public class ServiceCatalogueApiHelper implements ServiceCatalogueApiHelperService {

    private static final String CANNOT_CONNECT_TO_SERVER_ERROR_FORMAT = "Cannot connect to the server node %s.";
    private static final String FAILED_TO_READ_THE_RESPONSE_ERROR = "Failed to read the response.";
    private static final Logger log = LoggerFactory.getLogger(ServiceCatalogueApiHelper.class);

    private String getAsString(InputStream inputStream) throws IOException {
        return IOUtils.toString(inputStream);
    }

    @Override
    public JSONObject getKeyMd5s(String hostAndPort, String username, String password, String key)
        throws ServiceCatalogueAPIServiceStubException {
        try (Response response = HTTPSClientUtil.doGetKeyMd5s(hostAndPort, username, password, key)) {
            if (response.status() == 200 && response.body() != null) {
                return new JSONObject(getAsString(response.body().asInputStream()));
            }
            throw new ServiceCatalogueAPIServiceStubException("Failed to get md5s for the keys. " + response.reason());
        } catch (RetryableException e) {
            throw new ServiceCatalogueAPIServiceStubException(
                String.format(CANNOT_CONNECT_TO_SERVER_ERROR_FORMAT, hostAndPort), e);
        } catch (IOException e) {
            throw new ServiceCatalogueAPIServiceStubException(FAILED_TO_READ_THE_RESPONSE_ERROR, e);
        }
    }

    @Override
    public boolean uploadAsyncAPIDef(File file, String hostAndPort, String username, String password)
        throws ServiceCatalogueAPIServiceStubException {
        try (Response response = HTTPSClientUtil.uploadZip(file, hostAndPort, username, password)) {
            if (response.status() == 200) {
                return true;
            }
            throw new ServiceCatalogueAPIServiceStubException("There were failures during the replay." + response.reason());
        } catch (RetryableException e) {
            throw new ServiceCatalogueAPIServiceStubException(
                String.format(CANNOT_CONNECT_TO_SERVER_ERROR_FORMAT, hostAndPort), e);
        }
    }

    @Override
    public boolean deleteAsyncAPIDef(String serviceUUID, String hostAndPort, String username, String password)
        throws ServiceCatalogueAPIServiceStubException {
        try (Response response = HTTPSClientUtil.deleteAsyncAPI(serviceUUID, hostAndPort, username, password)) {
            if (response.status() == 204) {
                return true;
            }
            throw new ServiceCatalogueAPIServiceStubException(
                String.format("Failed to delete the error entry with id: %s. %s", serviceUUID, response.reason()));
        } catch (RetryableException e) {
            throw new ServiceCatalogueAPIServiceStubException(
                String.format(CANNOT_CONNECT_TO_SERVER_ERROR_FORMAT, hostAndPort), e);
        }
    }
}
