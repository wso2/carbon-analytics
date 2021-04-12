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

import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.Selectors;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.streaming.integrator.core.internal.asyncapi.util.Constants;
import org.wso2.carbon.streaming.integrator.core.internal.asyncapi.util.Utils;
import org.wso2.carbon.streaming.integrator.core.internal.exception.ServiceCatalogueAPIServiceStubException;
import org.wso2.carbon.streaming.integrator.core.persistence.beans.AsyncAPIServiceCatalogueConfigs;
import org.yaml.snakeyaml.Yaml;

import java.util.Map;

public class AsyncAPIUndeployer implements Runnable {

    private final String hostAndPort;
    private final String username;
    private final String password;
    private final String asyncAPiKeyVersion;
    private final ServiceCatalogueApiHelper serviceCatalogueApiHelper;
    private final String zipDirectoryURI;

    private static final Logger log = LoggerFactory.getLogger(AsyncAPIUndeployer.class);

    public AsyncAPIUndeployer(AsyncAPIServiceCatalogueConfigs asyncAPIServiceCatalogueConfigs, String asyncAPIContent) {
        hostAndPort = asyncAPIServiceCatalogueConfigs.getHostname() + ":" + asyncAPIServiceCatalogueConfigs.getPort();
        username = asyncAPIServiceCatalogueConfigs.getUsername();
        password = asyncAPIServiceCatalogueConfigs.getPassword();
        serviceCatalogueApiHelper = new ServiceCatalogueApiHelper();
        Yaml yaml = new Yaml();
        Map<String, Object> map = yaml.load(asyncAPIContent);
        JSONObject asyncAPIJson = new JSONObject(map);
        String serviceKey = asyncAPIJson.getJSONObject(Constants.ASYNC_API_INFO).
                getString(Constants.ASYNC_API_TITLE).replaceAll(" ", "");
        String version = asyncAPIJson.getJSONObject(Constants.ASYNC_API_INFO).getString(Constants.ASYNC_API_VERSION);
        asyncAPiKeyVersion = serviceKey + "-" + version;
        zipDirectoryURI = Constants.SERVICE_DEF_DIRECTORY + asyncAPiKeyVersion;
    }

    @Override
    public void run() {
        String serviceUUID = getAsyncAPIUUIDinServiceCatalogue();
        if (serviceUUID != null) {
            try {
                serviceCatalogueApiHelper.deleteAsyncAPIDef(serviceUUID, hostAndPort, username, password);
                FileObject fileObject = Utils.getFileObject(zipDirectoryURI);
                fileObject.delete(Selectors.SELECT_ALL);
                log.info("Async api: " + asyncAPiKeyVersion +
                        " deleted from service catalog when undeploying Siddhi app");
            } catch (ServiceCatalogueAPIServiceStubException e) {
                log.error("Exception occurred when deleting async api: " +
                        asyncAPiKeyVersion + " when undeploying Siddhi app", e);
            } catch (FileSystemException e) {
                log.error("Exception occurred when deleting async api in the file system: " +
                        asyncAPiKeyVersion + " when undeploying Siddhi app", e);
            }
        }
    }

    public String getAsyncAPIUUIDinServiceCatalogue() {
        try {
            JSONObject apiMd5s = serviceCatalogueApiHelper.getKeyMd5s(hostAndPort, username, password, asyncAPiKeyVersion);
            if (log.isDebugEnabled() && apiMd5s != null) {
                log.info(" Retrieved Async API definition md5s: " + apiMd5s.toString());
            }
            if (apiMd5s != null) {
                JSONArray md5List = apiMd5s.getJSONArray("list");
                for (int i = 0; i < md5List.length(); i++) {
                    JSONObject apiKeyObject = md5List.getJSONObject(i);
                    if (apiKeyObject.getString("serviceKey").compareTo(asyncAPiKeyVersion) == 0) {
                        return apiKeyObject.getString("id");
                    }
                }
            }
            return null;
        } catch (ServiceCatalogueAPIServiceStubException e) {
            log.error("Exception occurred when getting checking for async api: " +
                    asyncAPiKeyVersion + " when undeploying Siddhi app", e);
            return null;
        }
    }
}

