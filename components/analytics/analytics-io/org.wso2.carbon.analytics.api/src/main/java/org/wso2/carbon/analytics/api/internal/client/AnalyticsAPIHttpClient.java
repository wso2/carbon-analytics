/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.carbon.analytics.api.internal.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.analytics.api.RemoteRecordIterator;
import org.wso2.carbon.analytics.api.internal.AnalyticsDataConfiguration;
import org.wso2.carbon.analytics.dataservice.io.commons.RemoteRecordGroup;
import org.wso2.carbon.analytics.api.exception.AnalyticsServiceAuthenticationException;
import org.wso2.carbon.analytics.api.exception.AnalyticsServiceException;
import org.wso2.carbon.analytics.dataservice.io.commons.AnalyticsAPIConstants;
import org.wso2.carbon.analytics.dataservice.commons.IndexType;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AnalyticsAPIHttpClient {
    private static final Log log = LogFactory.getLog(AnalyticsAPIHttpClient.class);
    private static AnalyticsAPIHttpClient instance;
    private String hostname;
    private int port;
    private String protocol;
    private String sessionId;
    private DefaultHttpClient httpClient;

    private AnalyticsAPIHttpClient(String protocol, String hostname, int port,
                                   int maxPerRoute, int maxConnection,
                                   int socketTimeout, int connectionTimeout) {
        this.hostname = hostname;
        this.port = port;
        this.protocol = protocol;
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(
                new Scheme(this.protocol, port, PlainSocketFactory.getSocketFactory()));
        PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager(schemeRegistry);
        connectionManager.setDefaultMaxPerRoute(maxPerRoute);
        connectionManager.setMaxTotal(maxConnection);
        BasicHttpParams params = new BasicHttpParams();
        params.setParameter(CoreConnectionPNames.SO_TIMEOUT, socketTimeout);
        params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, connectionTimeout);
        this.httpClient = new DefaultHttpClient(connectionManager, params);
    }

    public static void init(AnalyticsDataConfiguration dataConfiguration) throws AnalyticsServiceException {
        try {
            URL url = new URL(dataConfiguration.getEndpoint());
            instance = new AnalyticsAPIHttpClient(url.getProtocol(), url.getHost(), url.getPort(),
                    dataConfiguration.getMaxConnectionsPerRoute(), dataConfiguration.getMaxConnections(),
                    dataConfiguration.getSocketConnectionTimeoutMS(), dataConfiguration.getConnectionTimeoutMS());
        } catch (MalformedURLException e) {
            throw new AnalyticsServiceException("Error while initializing the analytics http client. " + e.getMessage(), e);
        }
    }

    public static AnalyticsAPIHttpClient getInstance() {
        return instance;
    }

    public void authenticate(String username, String password) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(this.protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.AUTHENTICATION_SERVICE_URI)
                .setParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.LOGIN_OPERATION);
        try {
            HttpGet getMethod = new HttpGet(builder.build().toString());
            getMethod.addHeader(AnalyticsAPIConstants.AUTHORIZATION_HEADER, "Basic " + Base64.encode(
                    (username + AnalyticsAPIConstants.SEPARATOR
                            + password).getBytes()));
            HttpResponse httpResponse = httpClient.execute(getMethod);
            if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                throw new AnalyticsServiceAuthenticationException("Authentication failed for user : " + username);
            }
            String response = getResponse(httpResponse);
            if (response.startsWith(AnalyticsAPIConstants.SESSION_ID)) {
                String[] reponseElements = response.split(AnalyticsAPIConstants.SEPARATOR);
                if (reponseElements.length == 2) {
                    this.sessionId = reponseElements[1];
                } else {
                    throw new AnalyticsServiceAuthenticationException("Invalid response returned, cannot find " +
                            "sessionId. Response:" + response);
                }
            } else {
                throw new AnalyticsServiceAuthenticationException("Invalid response returned, no session id found!"
                        + response);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided for authentication. "
                    + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. "
                    + e.getMessage(), e);
        }
    }

    public void validateAndAuthenticate(String username, String password) throws AnalyticsServiceException {
        if (sessionId == null) {
            authenticate(username, password);
        }
    }

    private String getResponse(HttpResponse httpResponse) throws AnalyticsServiceException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()));
            String readLine;
            String response = "";
            while (((readLine = br.readLine()) != null)) {
                response += readLine;
            }
            return response;
        } catch (IOException e) {
            throw new AnalyticsServiceException("Error while reading the response from the remote service. "
                    + e.getMessage(), e);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.warn("Error while closing the connection! " + e.getMessage());
                }
            }
        }
    }

    public void createTable(int tenantId, String tableName) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants
                .TABLE_PROCESSOR_SERVICE_URI);
        try {
            HttpPost postMethod = new HttpPost(builder.build().toString());
            postMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants
                    .CREATE_TABLE_OPERATION));
            params.add(new BasicNameValuePair(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId)));
            params.add(new BasicNameValuePair(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName));
            postMethod.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse httpResponse = httpClient.execute(postMethod);
            if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponse(httpResponse);
                throw new AnalyticsServiceException("Unable to create the table - " + tableName + " for tenant id : "
                        + tenantId + ". " + response);
            } else {
                EntityUtils.consume(httpResponse.getEntity());
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. "
                    + e.getMessage(), e);
        }
    }

    public void setTableSchema(int tenantId, String tableName,
                               AnalyticsSchema schema) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants
                .SCHEMA_PROCESSOR_SERVICE_URI);
        String jsonSchema = new GsonBuilder().create().toJson(schema);
        try {
            HttpPost postMethod = new HttpPost(builder.build().toString());
            postMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.
                    SET_SCHEMA_OPERATION));
            params.add(new BasicNameValuePair(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId)));
            params.add(new BasicNameValuePair(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName));
            params.add(new BasicNameValuePair(AnalyticsAPIConstants.SCHEMA_PARAM, jsonSchema));
            postMethod.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse httpResponse = httpClient.execute(postMethod);
            if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponse(httpResponse);
                throw new AnalyticsServiceException("Unable to set the schema for the table - " + tableName
                        + ", schema - " + jsonSchema + " for tenant id : " + tenantId + ". " + response);
            } else {
                EntityUtils.consume(httpResponse.getEntity());
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. "
                    + e.getMessage(), e);
        }
    }

    public AnalyticsSchema getTableSchema(int tenantId, String tableName) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.SCHEMA_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.GET_SCHEMA_OPERATION)
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName);
        try {
            HttpGet getMethod = new HttpGet(builder.build().toString());
            getMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(getMethod);
            String response = getResponse(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                throw new AnalyticsServiceException("Unable to get the schema for the table - " + tableName
                        + " for tenant id : " + tenantId + ". " + response);
            } else {
                return new GsonBuilder().create().fromJson(response, AnalyticsSchema.class);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. "
                    + e.getMessage(), e);
        }
    }

    public boolean isTableExists(int tenantId, String tableName) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants
                .TABLE_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.TABLE_EXISTS_OPERATION)
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName);
        try {
            HttpGet getMethod = new HttpGet(builder.build().toString());
            getMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(getMethod);
            String response = getResponse(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                throw new AnalyticsServiceException("Unable to check the existence for the table - " + tableName
                        + " for tenant id : " + tenantId + ". " + response);
            } else {
                if (response.startsWith(AnalyticsAPIConstants.TABLE_EXISTS)) {
                    String[] reponseElements = response.split(AnalyticsAPIConstants.SEPARATOR);
                    if (reponseElements.length == 2) {
                        return Boolean.parseBoolean(reponseElements[1]);
                    } else {
                        throw new AnalyticsServiceException("Invalid response returned, cannot find table existence" +
                                " message. Response:" + response);
                    }
                } else {
                    throw new AnalyticsServiceException("Invalid response returned, table existence message not found!"
                            + response);
                }
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. "
                    + e.getMessage(), e);
        }
    }

    public List<String> listTables(int tenantId) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants
                .TABLE_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.LIST_TABLES_OPERATION)
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
        try {
            HttpGet getMethod = new HttpGet(builder.build().toString());
            getMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(getMethod);
            String response = getResponse(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                throw new AnalyticsServiceException("Unable to get the list of tables for tenant id : "
                        + tenantId + ". " + response);
            } else {
                Type tableNamesListType = new TypeToken<List<String>>() {
                }.getType();
                return new Gson().fromJson(response, tableNamesListType);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. "
                    + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. "
                    + e.getMessage(), e);
        }
    }

    public void deleteTable(int tenantId, String tableName) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.TABLE_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.DELETE_TABLE_OPERATION)
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName);
        try {
            HttpDelete deleteMethod = new HttpDelete(builder.build().toString());
            deleteMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(deleteMethod);
            if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponse(httpResponse);
                throw new AnalyticsServiceException("Unable to create the table - " + tableName + " for tenant id : " + tenantId + ". " + response);
            } else {
                EntityUtils.consume(httpResponse.getEntity());
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        }
    }

    public long getRecordCount(int tenantId, String tableName, long timeFrom, long timeTo) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.RECORD_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.GET_RECORD_COUNT_OPERATION)
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.TIME_FROM_PARAM, String.valueOf(timeFrom))
                .addParameter(AnalyticsAPIConstants.TIME_TO_PARAM, String.valueOf(timeTo));
        try {
            HttpGet getMethod = new HttpGet(builder.build().toString());
            getMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(getMethod);
            String response = getResponse(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                throw new AnalyticsServiceException("Unable to get the record count for the table - " + tableName
                        + ", time from : " + timeFrom + " , timeTo : " + timeTo
                        + " for tenant id : " + tenantId + ". " + response);
            } else {
                if (response.startsWith(AnalyticsAPIConstants.RECORD_COUNT)) {
                    String[] reponseElements = response.split(AnalyticsAPIConstants.SEPARATOR);
                    if (reponseElements.length == 2) {
                        return Long.parseLong(reponseElements[1]);
                    } else {
                        throw new AnalyticsServiceException("Invalid response returned, cannot find record count" +
                                " message. Response:" + response);
                    }
                } else {
                    throw new AnalyticsServiceException("Invalid response returned, record count message not found!"
                            + response);
                }
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        }
    }

    public void putRecords(List<Record> records) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.RECORD_PROCESSOR_SERVICE_URI);
        try {
            HttpPost postMethod = new HttpPost(builder.build().toString());
            postMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.PUT_RECORD_OPERATION));
            params.add(new BasicNameValuePair(AnalyticsAPIConstants.RECORDS_PARAM, new Gson().toJson(records)));
            postMethod.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse httpResponse = httpClient.execute(postMethod);
            String response = getResponse(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                throw new AnalyticsServiceException("Unable to put the records. " + response);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        }
    }

    public void deleteRecords(int tenantId, String tableName, long timeFrom, long timeTo) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.RECORD_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.DELETE_RECORDS_RANGE_OPERATION)
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.TIME_FROM_PARAM, String.valueOf(timeFrom))
                .addParameter(AnalyticsAPIConstants.TIME_TO_PARAM, String.valueOf(timeTo));
        try {
            HttpDelete deleteMethod = new HttpDelete(builder.build().toString());
            deleteMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(deleteMethod);
            String response = getResponse(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                throw new AnalyticsServiceException("Unable to delete the record count for the table - " + tableName
                        + ", time from : " + timeFrom + " , timeTo : " + timeTo
                        + " for tenant id : " + tenantId + ". " + response);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        }
    }

    public void deleteRecords(int tenantId, String tableName, List<String> recordIds) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.RECORD_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.DELETE_RECORDS_IDS_OPERATION)
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.RECORD_IDS_PARAM, new Gson().toJson(recordIds));
        try {
            HttpDelete deleteMethod = new HttpDelete(builder.build().toString());
            deleteMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(deleteMethod);
            String response = getResponse(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                throw new AnalyticsServiceException("Unable to delete the record count for the table - " + tableName
                        + ", records - " + recordIds
                        + " for tenant id : " + tenantId + ". " + response);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        }
    }

    public void setIndices(int tenantId, String tableName, Map<String, IndexType> columns) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.INDEX_PROCESSOR_SERVICE_URI);
        try {
            HttpPost postMethod = new HttpPost(builder.build().toString());
            postMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.SET_INDICES_OPERATION));
            params.add(new BasicNameValuePair(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId)));
            params.add(new BasicNameValuePair(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName));
            String indexJson = new Gson().toJson(columns);
            params.add(new BasicNameValuePair(AnalyticsAPIConstants.INDEX_PARAM, indexJson));
            postMethod.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse httpResponse = httpClient.execute(postMethod);
            String response = getResponse(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                throw new AnalyticsServiceException("Unable to set the index for table - " + tableName
                        + " with index  - " + indexJson
                        + " for tenant id : " + tenantId + ". " + response);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        }
    }

    public Map<String, IndexType> getIndices(int tenantId, String tableName) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.INDEX_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.GET_INDICES_OPERATION)
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName);
        try {
            HttpGet getMethod = new HttpGet(builder.build().toString());
            getMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(getMethod);
            String response = getResponse(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                throw new AnalyticsServiceException("Unable to get the index for table - " + tableName
                        + " for tenant id : " + tenantId + ". " + response);
            } else {
                Type indexMap = new TypeToken<Map<String, IndexType>>() {
                }.getType();
                return new Gson().fromJson(response, indexMap);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        }
    }


    public void clearIndices(int tenantId, String tableName) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.INDEX_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.DELETE_INDICES_OPERATION)
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName);
        try {
            HttpDelete deleteMethod = new HttpDelete(builder.build().toString());
            deleteMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(deleteMethod);
            if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponse(httpResponse);
                throw new AnalyticsServiceException("Unable to get the index for table - " + tableName
                        + " for tenant id : " + tenantId + ". " + response);
            } else {
                EntityUtils.consume(httpResponse.getEntity());
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        }
    }

    public List<SearchResultEntry> search(int tenantId, String tableName, String language, String query,
                                          int start, int count) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.INDEX_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.SEARCH_OPERATION)
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.LANGUAGE_PARAM, language)
                .addParameter(AnalyticsAPIConstants.QUERY, query)
                .addParameter(AnalyticsAPIConstants.START_PARAM, String.valueOf(start))
                .addParameter(AnalyticsAPIConstants.COUNT_PARAM, String.valueOf(count));
        try {
            HttpGet getMethod = new HttpGet(builder.build().toString());
            getMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(getMethod);
            String response = getResponse(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                throw new AnalyticsServiceException("Unable to search the table - " + tableName
                        + " for tenant id : " + tenantId + " with language : " + language + ", query : " + query + ". "
                        + response);
            } else {
                Type searchResultType = new TypeToken<List<SearchResultEntry>>() {
                }.getType();
                return new Gson().fromJson(response, searchResultType);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        }
    }

    public int searchCount(int tenantId, String tableName, String language, String query) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.SEARCH_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.SEARCH_COUNT_OPERATION)
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.LANGUAGE_PARAM, language)
                .addParameter(AnalyticsAPIConstants.QUERY, query);
        try {
            HttpGet getMethod = new HttpGet(builder.build().toString());
            getMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(getMethod);
            String response = getResponse(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                throw new AnalyticsServiceException("Unable to search the table - " + tableName
                        + " for tenant id : " + tenantId + " with language : " + language + ", query : " + query + ". "
                        + response);
            } else {
                if (response.startsWith(AnalyticsAPIConstants.SEARCH_COUNT)) {
                    String[] responseElements = response.split(AnalyticsAPIConstants.SEPARATOR);
                    if (responseElements.length == 2) {
                        return Integer.parseInt(responseElements[1]);
                    } else {
                        throw new AnalyticsServiceException("Invalid response returned, cannot find search count" +
                                " message. Response:" + response);
                    }
                } else {
                    throw new AnalyticsServiceException("Invalid response returned, search count message not found!"
                            + response);
                }
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        }
    }

    public void waitForIndexing(long maxWait) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.INDEX_PROCESSOR_SERVICE_URI);
        try {
            HttpPost postMethod = new HttpPost(builder.build().toString());
            postMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.WAIT_FOR_INDEXING_OPERATION));
            params.add(new BasicNameValuePair(AnalyticsAPIConstants.MAX_WAIT_PARAM, String.valueOf(maxWait)));
            postMethod.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse httpResponse = httpClient.execute(postMethod);
            String response = getResponse(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                throw new AnalyticsServiceException("Unable to configure max wait: " + maxWait + " for indexing. "
                        + response);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        }
    }

    public void destroy() throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.ANALYTICS_SERVICE_PROCESSOR_URI);
        try {
            HttpPost postMethod = new HttpPost(builder.build().toString());
            postMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.DESTROY_OPERATION));
            postMethod.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse httpResponse = httpClient.execute(postMethod);
            String response = getResponse(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                throw new AnalyticsServiceException("Unable to destroy the process . "
                        + response);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        }
    }

    public RecordGroup[] getRecordGroup(int tenantId, String tableName, int numPartitionsHint, List<String> columns, long timeFrom,
                                        long timeTo, int recordsFrom, int recordsCount) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.ANALYTIC_RECORD_READ_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.GET_RANGE_RECORD_GROUP_OPERATION)
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.PARTITIONER_NO_PARAM, String.valueOf(numPartitionsHint))
                .addParameter(AnalyticsAPIConstants.COLUMNS_PARAM, new Gson().toJson(columns))
                .addParameter(AnalyticsAPIConstants.TIME_FROM_PARAM, String.valueOf(timeFrom))
                .addParameter(AnalyticsAPIConstants.TIME_TO_PARAM, String.valueOf(timeTo))
                .addParameter(AnalyticsAPIConstants.RECORD_FROM_PARAM, String.valueOf(recordsFrom))
                .addParameter(AnalyticsAPIConstants.COUNT_PARAM, String.valueOf(recordsCount));
        ObjectInputStream objIn = null;
        try {
            HttpGet getMethod = new HttpGet(builder.build().toString());
            getMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(getMethod);
            if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponse(httpResponse);
                throw new AnalyticsServiceException("Unable to destroy the process . "
                        + response);
            } else {
                objIn = new ObjectInputStream(httpResponse.getEntity().getContent());
                return (RemoteRecordGroup[]) objIn.readObject();
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. "
                    + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new AnalyticsServiceException("Unable to load the class when trying to deserialize the object. "
                    + e.getMessage(), e);
        } finally {
            if (objIn != null) {
                try {
                    objIn.close();
                } catch (IOException e) {
                    log.warn("Error while closing the stream! " + e.getMessage());
                }
            }
        }
    }

    public RecordGroup[] getRecordGroup(int tenantId, String tableName, int numPartitionsHint, List<String> columns,
                                        List<String> ids) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        Gson gson = new Gson();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.ANALYTIC_RECORD_READ_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.GET_IDS_RECORD_GROUP_OPERATION)
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.PARTITIONER_NO_PARAM, String.valueOf(numPartitionsHint))
                .addParameter(AnalyticsAPIConstants.COLUMNS_PARAM, gson.toJson(columns))
                .addParameter(AnalyticsAPIConstants.RECORD_IDS_PARAM, gson.toJson(ids));
        ObjectInputStream objIn = null;
        try {
            HttpGet getMethod = new HttpGet(builder.build().toString());
            getMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(getMethod);
            if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponse(httpResponse);
                throw new AnalyticsServiceException("Unable to destroy the process . "
                        + response);
            } else {
                objIn = new ObjectInputStream(httpResponse.getEntity().getContent());
                return (RemoteRecordGroup[]) objIn.readObject();
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new AnalyticsServiceException("Unable to load the class when trying to deserialize the object. " + e.getMessage(), e);
        } finally {
            if (objIn != null) {
                try {
                    objIn.close();
                } catch (IOException e) {
                    log.warn("Error while closing the stream! " + e.getMessage());
                }
            }
        }
    }

    public Iterator<Record> readRecords(RecordGroup recordGroup) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.ANALYTIC_RECORD_READ_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.READ_RECORD_OPERATION);
        ByteArrayOutputStream out = null;
        ObjectOutputStream os = null;
        try {
            HttpPost postMethod = new HttpPost(builder.build().toString());
            postMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            out = new ByteArrayOutputStream();
            os = new ObjectOutputStream(out);
            os.writeObject(recordGroup);
            postMethod.setEntity(new ByteArrayEntity(out.toByteArray()));
            HttpResponse httpResponse = httpClient.execute(postMethod);
            if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponse(httpResponse);
                throw new AnalyticsServiceException("Unable to read the record group. "
                        + response);
            }
            return new RemoteRecordIterator<Record>(httpResponse.getEntity().getContent());
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    log.warn("Error while closing object stream! " + e.getMessage());
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.warn("Error while closing output stream! " + e.getMessage());
                }
            }
        }
    }

}
