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

import io.siddhi.core.exception.SiddhiAppRuntimeException;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.Selectors;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.streaming.integrator.core.internal.asyncapi.util.Constants;
import org.wso2.carbon.streaming.integrator.core.internal.asyncapi.util.Utils;
import org.wso2.carbon.streaming.integrator.core.internal.exception.ServiceCatalogueAPIServiceStubException;
import org.wso2.carbon.streaming.integrator.core.internal.exception.SiddhiAppDeploymentException;
import org.wso2.carbon.streaming.integrator.core.persistence.beans.AsyncAPIServiceCatalogueConfigs;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import sun.nio.cs.UTF_32;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AsyncAPIDeployer implements Runnable {

    private final String hostAndPort;
    private final String username;
    private final String password;
    private final String asyncAPiKeyVersion;
    private final ServiceCatalogueApiHelper serviceCatalogueApiHelper;
    private final String asyncAPIContent;
    private String metadataContent;
    private final JSONObject asyncAPIJson;
    private final String directoryURI;
    private final String zipDirectoryURI;

    private static final Logger log = LoggerFactory.getLogger(AsyncAPIDeployer.class);

    public AsyncAPIDeployer(AsyncAPIServiceCatalogueConfigs asyncAPIServiceCatalogueConfigs, String asyncAPIContent) {
        hostAndPort = asyncAPIServiceCatalogueConfigs.getHostname() + ":" + asyncAPIServiceCatalogueConfigs.getPort();
        username = asyncAPIServiceCatalogueConfigs.getUsername();
        password = asyncAPIServiceCatalogueConfigs.getPassword();
        serviceCatalogueApiHelper = new ServiceCatalogueApiHelper();
        this.asyncAPIContent = asyncAPIContent;
        Yaml yaml = new Yaml();
        Map<String, Object> map = yaml.load(asyncAPIContent);
        this.asyncAPIJson = new JSONObject(map);
        metadataContent = createAndGetMetadataContent(asyncAPIJson);
        String serviceKey = asyncAPIJson.getJSONObject(Constants.ASYNC_API_INFO).
                getString(Constants.ASYNC_API_TITLE).replaceAll(" ", "");
        String version = asyncAPIJson.getJSONObject(Constants.ASYNC_API_INFO).getString(Constants.ASYNC_API_VERSION);
        asyncAPiKeyVersion = serviceKey + "-" + version;
        zipDirectoryURI = Constants.SERVICE_DEF_DIRECTORY + asyncAPiKeyVersion;
        directoryURI = zipDirectoryURI + File.separator + asyncAPiKeyVersion;
    }

    @Override
    public void run() {
        if (!isMD5Equal()) {
            createFiles();
            zipFiles();
            try {
                boolean isUploaded = serviceCatalogueApiHelper.uploadAsyncAPIDef(new File(zipDirectoryURI +
                        File.separator + asyncAPiKeyVersion + ".zip"), hostAndPort, username, password);
                if (isUploaded) {
                    log.info("Async API: " + asyncAPiKeyVersion + " uploaded to the service catalogue");
                }
            } catch (ServiceCatalogueAPIServiceStubException e) {
                log.error("Exception occurred when deploying Async API: " + asyncAPiKeyVersion +
                        " to the service catalogue");
            }
        }
    }

    public void zipFiles() {
        File sourceFile = new File(zipDirectoryURI);
        List<String> fileList = new ArrayList<>();
        Utils.generateFileList(zipDirectoryURI, sourceFile, fileList);
        try {
            Utils.zip(zipDirectoryURI, zipDirectoryURI + File.separator + asyncAPiKeyVersion, fileList);
        } catch (IOException e) {
            throw new SiddhiAppRuntimeException("IOException occurred when archiving  " + zipDirectoryURI, e);
        }
    }

    public void createFiles() {
        FileObject fileObject = Utils.getFileObject(directoryURI);
        try {
            if (!fileObject.exists()) {
                fileObject.createFolder();
            }
            FileObject metadataYamlFile = Utils.getFileObject(directoryURI + File.separator + "metadata.yaml");
            FileObject asyncAPIYamlFile = Utils.getFileObject(directoryURI + File.separator + "asyncAPI.yaml");
            if (metadataYamlFile.exists()) {
                metadataYamlFile.delete(Selectors.SELECT_ALL);
            }
            if (asyncAPIYamlFile.exists()) {
                asyncAPIYamlFile.delete(Selectors.SELECT_ALL);
            }
            if (!metadataYamlFile.exists()) {
                metadataYamlFile.createFile();
                metadataYamlFile.refresh();
                OutputStream outputStream = metadataYamlFile.getContent().getOutputStream();
                outputStream.write(metadataContent.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }
            if (!asyncAPIYamlFile.exists()) {
                asyncAPIYamlFile.createFile();
                asyncAPIYamlFile.refresh();
                OutputStream outputStream = asyncAPIYamlFile.getContent().getOutputStream();
                outputStream.write(asyncAPIContent.getBytes(StandardCharsets.UTF_8));
                outputStream.flush();
            }
        } catch (IOException e) {
            log.error("Exception occurred when creating service definition files for " + asyncAPiKeyVersion, e);
        }
    }



    public boolean isMD5Equal() {
        try {
            JSONObject apiMd5s = serviceCatalogueApiHelper.getKeyMd5s(hostAndPort, username, password, asyncAPiKeyVersion);
            if (log.isDebugEnabled() && apiMd5s != null) {
                log.debug(" Retrieved Async API definition md5s: " + apiMd5s.toString());
            }
            if (apiMd5s != null && apiMd5s.getInt("count") > 0) {
                JSONArray md5List = apiMd5s.getJSONArray("list");
                for (int i = 0; i < md5List.length(); i++) {
                    JSONObject apiKeyObject = md5List.getJSONObject(i);
                    if (apiKeyObject.getString("key").compareTo(asyncAPiKeyVersion) == 0) {
                        String md5 = apiKeyObject.getString("md5");
                        metadataContent = createAndGetMetadataContent(asyncAPIJson);
                        String md5Calculated =
                                getMD5(asyncAPIContent, asyncAPiKeyVersion) + getMD5(metadataContent, asyncAPiKeyVersion);
                        if (md5.compareTo(md5Calculated) == 0) {
                            if (log.isDebugEnabled()) {
                                log.debug("MD5 of " + asyncAPiKeyVersion + " is equal, hence not deployong Async API");
                            }
                            log.info("MD5 of " + asyncAPiKeyVersion +
                                    " is equal, hence not deploying Async API to service catalogue");
                            return true;
                        }
                    }
                }
            }
            metadataContent = createAndGetMetadataContent(asyncAPIJson);
            return false;
        } catch (ServiceCatalogueAPIServiceStubException | SiddhiAppDeploymentException e) {
            log.error("Exception occurred when getting md5 for async api: " +
                    asyncAPiKeyVersion + " when deploying Siddhi app", e);
            metadataContent = createAndGetMetadataContent(asyncAPIJson);
            return false;
        }
    }

    public String getMD5(String stringValue, String asyncAPiKeyVersion) throws SiddhiAppDeploymentException {
        MessageDigest md5Digest;
        try {
            md5Digest = MessageDigest.getInstance("MD5");
            md5Digest.update(stringValue.getBytes(StandardCharsets.UTF_8));
            byte[] bytes = md5Digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(Integer.toString((aByte & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new SiddhiAppDeploymentException("Exception occurred when getting md5 for async api: " +
                    asyncAPiKeyVersion + " when deploying Siddhi app");
        }
    }

    public String createAndGetMetadataContent(JSONObject asyncAPIJson) {
        Map<String, Object> map = new HashMap<>();
        map.put("key", asyncAPiKeyVersion);
        map.put("name", asyncAPIJson.getJSONObject("info").getString("title"));
        map.put("displayName", asyncAPIJson.getJSONObject("info").getString("title").replaceAll(" ", ""));
        map.put("description", asyncAPIJson.getJSONObject("info").getString("description").replaceAll(" ", ""));
        map.put("version", asyncAPIJson.getJSONObject("info").getString("version").replaceAll(" ", ""));
        map.put("serviceUrl", asyncAPIJson.getJSONObject("servers").getJSONObject("production").getString("url"));
        map.put("definitionType", "ASYNC_API");
        JSONObject securitySchemes = asyncAPIJson.getJSONObject("components").getJSONObject("securitySchemes");
        Iterator keys = securitySchemes.keys();
        StringBuilder strBuilder = new StringBuilder();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if (securitySchemes.get(key) instanceof JSONObject) {
                if (strBuilder.length() == 0) {
                    strBuilder.append(((JSONObject) securitySchemes.get(key)).getString("type"));
                } else {
                    strBuilder.append(",").append(((JSONObject) securitySchemes.get(key)).getString("type"));
                }
            }
        }
        map.put("mutualSSLEnabled", false);
        map.put("securityType", strBuilder.toString());
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);
        return yaml.dump(map);
    }
}

