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
import org.wso2.carbon.streaming.integrator.core.internal.asyncapi.util.Md5HashGenerator;
import org.wso2.carbon.streaming.integrator.core.internal.asyncapi.util.Utils;
import org.wso2.carbon.streaming.integrator.core.internal.exception.ServiceCatalogueAPIServiceStubException;
import org.wso2.carbon.streaming.integrator.core.persistence.beans.AsyncAPIServiceCatalogueConfigs;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AsyncAPIDeployer implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(AsyncAPIDeployer.class);
    private final String hostAndPort;
    private final String username;
    private final String password;
    private final String asyncAPiKeyVersion;
    private final ServiceCatalogueApiHelper serviceCatalogueApiHelper;
    private final String asyncAPIContent;
    private final JSONObject asyncAPIJson;
    private final String directoryURI;
    private final String zipDirectoryURI;
    private String metadataContent;
    private String md5Calculated;

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
        asyncAPiKeyVersion = serviceKey + Constants.KEY_SEPARATOR + version;
        zipDirectoryURI = Constants.SERVICE_DEF_DIRECTORY + asyncAPiKeyVersion;
        directoryURI = zipDirectoryURI + File.separator + asyncAPiKeyVersion;
    }

    @Override
    public void run() {
        metadataContent = createAndGetMetadataContent(asyncAPIJson);
        createFiles();
        if (!isMD5Equal()) {
            zipFiles();
            try {
                JSONArray verifiers = new JSONArray();
                JSONObject verifier = new JSONObject();
                verifier.put("key", asyncAPiKeyVersion);
                verifier.put("md5", md5Calculated);
                verifiers.put(verifier);
                boolean isUploaded = serviceCatalogueApiHelper.uploadAsyncAPIDef(new File(zipDirectoryURI +
                                File.separator + asyncAPiKeyVersion + ".zip"), verifiers.toString(), hostAndPort,
                        username, password);
                if (isUploaded) {
                    log.info("Async API: " + asyncAPiKeyVersion + " uploaded to the service catalog");
                }
            } catch (ServiceCatalogueAPIServiceStubException e) {
                log.error("Exception occurred when deploying Async API: " + asyncAPiKeyVersion +
                        " to the service catalog");
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
            FileObject asyncAPIYamlFile = Utils.getFileObject(directoryURI + File.separator + "definition.yaml");
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
            // TODO: 2/16/21 Handle pagination
            JSONObject apiMd5s = serviceCatalogueApiHelper.getKeyMd5s(hostAndPort, username, password, asyncAPiKeyVersion);
            if (apiMd5s != null) {
                if (log.isDebugEnabled()) {
                    log.debug(" Retrieved Async API definition md5s: " + apiMd5s.toString());
                }
                JSONArray md5List = apiMd5s.getJSONArray("list");
                for (int i = 0; i < md5List.length(); i++) {
                    JSONObject apiKeyObject = md5List.getJSONObject(i);
                    if (apiKeyObject.getString("serviceKey").compareTo(asyncAPiKeyVersion) == 0) {
                        String md5 = apiKeyObject.getString("md5");
                        md5Calculated = Md5HashGenerator.generateHash(directoryURI);
                        if (md5Calculated != null && md5.compareTo(md5Calculated) == 0) {
                            if (log.isDebugEnabled()) {
                                log.debug("MD5 of " + asyncAPiKeyVersion + " is equal, hence not deploying Async API");
                            }
                            return true;
                        }
                    }
                }
            }
            metadataContent = createAndGetMetadataContent(asyncAPIJson);
            return false;
        } catch (ServiceCatalogueAPIServiceStubException e) {
            log.error("Exception occurred when getting md5 for async api: " +
                    asyncAPiKeyVersion + " when deploying Siddhi app", e);
            metadataContent = createAndGetMetadataContent(asyncAPIJson);
            return false;
        } catch (NoSuchAlgorithmException | IOException e) {
            log.error("Exception occurred when generating md5 for async api: " +
                    asyncAPiKeyVersion, e);
            return false;
        }
    }

    public String createAndGetMetadataContent(JSONObject asyncAPIJson) {
        Map<String, Object> map = new HashMap<>();
        map.put("key", asyncAPiKeyVersion);
        map.put("name", asyncAPIJson.getJSONObject("info").getString(Constants.ASYNC_API_TITLE));
        map.put("displayName", asyncAPIJson.getJSONObject("info").getString(Constants.ASYNC_API_TITLE).replaceAll(" ", ""));
        map.put("description", asyncAPIJson.getJSONObject("info").getString("description").replaceAll(" ", ""));
        map.put(Constants.ASYNC_API_VERSION, asyncAPIJson.getJSONObject("info").getString(Constants.ASYNC_API_VERSION)
                .replaceAll(" ", ""));
        String asyncApiServerName = asyncAPIJson.getJSONObject(Constants.SERVERS).names().get(0).toString();
        map.put("definitionType", "ASYNC_API");
        JSONObject securitySchemes = asyncAPIJson.getJSONObject("components").getJSONObject("securitySchemes");
        Iterator keys = securitySchemes.keys();
        StringBuilder strBuilder = new StringBuilder();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if (securitySchemes.get(key) instanceof JSONObject) {
                if (strBuilder.length() == 0) {
                    if (((JSONObject) securitySchemes.get(key)).getString("type")
                            .equals(Constants.PROTOCOL_HTTP)) {
                        strBuilder.append(((JSONObject) securitySchemes.get(key)).getString("scheme"));
                        map.put(Constants.PROPERTY_MUTUAL_SSL_ENABLED, false);
                    } else {
                        strBuilder.append(((JSONObject) securitySchemes.get(key)).getString("type"));
                        map.put(Constants.PROPERTY_MUTUAL_SSL_ENABLED, true);
                    }
                } else {
                    if (((JSONObject) securitySchemes.get(key)).getString("type")
                            .equals(Constants.PROTOCOL_HTTP)) {
                        strBuilder.append(",").append(((JSONObject) securitySchemes.get(key)).getString("scheme"));
                        map.put(Constants.PROPERTY_MUTUAL_SSL_ENABLED, false);
                    } else{
                        strBuilder.append(",").append(((JSONObject) securitySchemes.get(key)).getString("type"));
                        map.put(Constants.PROPERTY_MUTUAL_SSL_ENABLED, true);
                    }
                }
            }
        }
        map.putIfAbsent(Constants.PROPERTY_MUTUAL_SSL_ENABLED, false);
        String protocol = asyncAPIJson.getJSONObject(Constants.SERVERS).getJSONObject(asyncApiServerName)
                .getString("protocol");
        if (protocol.equalsIgnoreCase(Constants.ASYNC_API_TYPE_SSC) ||
                protocol.equalsIgnoreCase(Constants.ASYNC_API_TYPE_WEBSUB)) {
            if (Boolean.parseBoolean(map.get(Constants.PROPERTY_MUTUAL_SSL_ENABLED).toString())) {
                map.put("serviceUrl", "https://" + asyncAPIJson.getJSONObject(Constants.SERVERS)
                        .getJSONObject(asyncApiServerName).getString("url"));
            } else {
                map.put("serviceUrl", "http://" + asyncAPIJson.getJSONObject(Constants.SERVERS)
                        .getJSONObject(asyncApiServerName).getString("url"));
            }
        } else {
            map.put("serviceUrl", asyncAPIJson.getJSONObject(Constants.SERVERS).getJSONObject(asyncApiServerName)
                    .getString("protocol") + "://" + asyncAPIJson.getJSONObject(Constants.SERVERS)
                    .getJSONObject(asyncApiServerName).getString("url"));
        }
        if (strBuilder.toString().isEmpty()) {
            map.put("securityType", "NONE");
        } else {
            map.put("securityType", strBuilder.toString());
        }
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        options.setPrettyFlow(true);
        Yaml yaml = new Yaml(options);
        return yaml.dump(map);
    }
}

