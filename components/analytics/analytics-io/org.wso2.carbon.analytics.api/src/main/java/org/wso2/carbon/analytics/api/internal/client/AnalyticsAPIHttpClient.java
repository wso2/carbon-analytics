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
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.*;
import org.apache.http.HttpHost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.wso2.carbon.analytics.api.RemoteRecordIterator;
import org.wso2.carbon.analytics.dataservice.api.commons.RemoteRecordGroup;
import org.wso2.carbon.analytics.api.exception.AnalyticsServiceAuthenticationException;
import org.wso2.carbon.analytics.api.exception.AnalyticsServiceException;
import org.wso2.carbon.analytics.dataservice.api.commons.AnalyticsAPIConstants;
import org.wso2.carbon.analytics.dataservice.commons.IndexType;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class AnalyticsAPIHttpClient {
    private static AnalyticsAPIHttpClient instance;
    private String hostname;
    private int port;
    private String sessionId;


    private AnalyticsAPIHttpClient(String hostname, int port){
        this.hostname = hostname;
        this.port = port;
    }

    public static void init(String hostname, int port){
        instance = new AnalyticsAPIHttpClient(hostname, port);
    }

    public static AnalyticsAPIHttpClient getInstance(){
        return instance;
    }

    public void authenticate(String username, String password) throws AnalyticsServiceException {
        HttpClient httpclient = new HttpClient();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.AUTHENTICATION_SERVICE_URI)
                .setParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.LOGIN_OPERATION)
                .setParameter(AnalyticsAPIConstants.USERNAME_PARAM, username)
                .setParameter(AnalyticsAPIConstants.PASSWORD_PARAM, password);
        GetMethod getMethod = null;
        try {
            getMethod = new GetMethod(builder.build().toString());
            int status = httpclient.executeMethod(getMethod);
            if (status != HttpServletResponse.SC_ACCEPTED) {
                throw new AnalyticsServiceAuthenticationException("Authentication failed for user : " + username);
            }
            String response = getResponse(getMethod);
            if (response.startsWith(AnalyticsAPIConstants.SESSION_ID)) {
                String[] reponseElements = response.split(AnalyticsAPIConstants.SEPARATOR);
                if (reponseElements.length == 2) {
                    this.sessionId = reponseElements[1];
                } else {
                    throw new AnalyticsServiceAuthenticationException("Invalid response returned, cannot find sessionId. Response:" + response);
                }
            } else {
                throw new AnalyticsServiceAuthenticationException("Invalid response returned, no session id found!" + response);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided for authentication. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
    }

    public void validateAndAuthenticate(String username, String password) throws AnalyticsServiceException {
        if (sessionId == null) {
            authenticate(username, password);
        }
    }

    private String getResponse(HttpMethod httpMethod) throws AnalyticsServiceException {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(httpMethod.getResponseBodyAsStream()));
            String readLine;
            String response = "";
            while (((readLine = br.readLine()) != null)) {
                response += readLine;
            }
            return response;
        } catch (IOException e) {
            throw new AnalyticsServiceException("Error while reading the response from the remote service. "
                    + e.getMessage(), e);
        }
    }

    public void createTable(int tenantId, String tableName) throws AnalyticsServiceException {
        HttpClient httpclient = new HttpClient();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.TABLE_PROCESSOR_SERVICE_URI);
        PostMethod postMethod = null;
        try {
            postMethod = new PostMethod(builder.build().toString());
            postMethod.addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.CREATE_TABLE_OPERATION);
            postMethod.addParameter(AnalyticsAPIConstants.SESSION_ID, sessionId);
            postMethod.addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
            postMethod.addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName);
            int status = httpclient.executeMethod(postMethod);
            if (status != HttpServletResponse.SC_ACCEPTED) {
                String response = getResponse(postMethod);
                throw new AnalyticsServiceException("Unable to create the table - " + tableName + " for tenant id : " + tenantId + ". " + response);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }
    }

    public void setTableSchema(int tenantId, String tableName,
                               AnalyticsSchema schema) throws AnalyticsServiceException {
        HttpClient httpclient = new HttpClient();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.SCHEMA_PROCESSOR_SERVICE_URI);
        String jsonSchema = new GsonBuilder().create().toJson(schema);
        PostMethod postMethod = null;
        try {
            postMethod = new PostMethod(builder.build().toString());
            postMethod.addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.SET_SCHEMA_OPERATION);
            postMethod.addParameter(AnalyticsAPIConstants.SESSION_ID, sessionId);
            postMethod.addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
            postMethod.addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName);
            postMethod.addParameter(AnalyticsAPIConstants.SCHEMA_PARAM, jsonSchema);
            int status = httpclient.executeMethod(postMethod);
            if (status != HttpServletResponse.SC_ACCEPTED) {
                String response = getResponse(postMethod);
                throw new AnalyticsServiceException("Unable to set the schema for the table - " + tableName
                        + ", schema - " + jsonSchema + " for tenant id : " + tenantId + ". " + response);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }
    }

    public AnalyticsSchema getTableSchema(int tenantId, String tableName) throws AnalyticsServiceException {
        HttpClient httpclient = new HttpClient();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.SCHEMA_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.GET_SCHEMA_OPERATION)
                .addParameter(AnalyticsAPIConstants.SESSION_ID, sessionId)
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName);
        GetMethod getMethod = null;
        try {
            getMethod = new GetMethod(builder.build().toString());
            int status = httpclient.executeMethod(getMethod);
            String response = getResponse(getMethod);
            if (status != HttpServletResponse.SC_ACCEPTED) {
                throw new AnalyticsServiceException("Unable to get the schema for the table - " + tableName
                        + " for tenant id : " + tenantId + ". " + response);
            } else {
                return new GsonBuilder().create().fromJson(response, AnalyticsSchema.class);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
    }

    public boolean isTableExists(int tenantId, String tableName) throws AnalyticsServiceException {
        HttpClient httpclient = new HttpClient();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.TABLE_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.TABLE_EXISTS_OPERATION)
                .addParameter(AnalyticsAPIConstants.SESSION_ID, sessionId)
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName);
        GetMethod getMethod = null;
        try {
            getMethod = new GetMethod(builder.build().toString());
            int status = httpclient.executeMethod(getMethod);
            String response = getResponse(getMethod);
            if (status != HttpServletResponse.SC_ACCEPTED) {
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
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
    }

    public List<String> listTables(int tenantId) throws AnalyticsServiceException {
        HttpClient httpclient = new HttpClient();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.TABLE_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.LIST_TABLES_OPERATION)
                .addParameter(AnalyticsAPIConstants.SESSION_ID, sessionId)
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
        GetMethod getMethod = null;
        try {
            getMethod = new GetMethod(builder.build().toString());
            int status = httpclient.executeMethod(getMethod);
            String response = getResponse(getMethod);
            if (status != HttpServletResponse.SC_ACCEPTED) {
                throw new AnalyticsServiceException("Unable to get the list of tables for tenant id : "
                        + tenantId + ". " + response);
            } else {
                Type tableNamesListType = new TypeToken<List<String>>() {
                }.getType();
                return new Gson().fromJson(response, tableNamesListType);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. "
                    + e.getMessage(), e);
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
    }

    public void deleteTable(int tenantId, String tableName) throws AnalyticsServiceException {
        HttpClient httpclient = new HttpClient();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.TABLE_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.DELETE_TABLE_OPERATION)
                .addParameter(AnalyticsAPIConstants.SESSION_ID, sessionId)
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName);
        DeleteMethod deleteMethod = null;
        try {
            deleteMethod = new DeleteMethod(builder.build().toString());
            int status = httpclient.executeMethod(deleteMethod);
            if (status != HttpServletResponse.SC_ACCEPTED) {
                String response = getResponse(deleteMethod);
                throw new AnalyticsServiceException("Unable to create the table - " + tableName + " for tenant id : " + tenantId + ". " + response);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        } finally {
            if (deleteMethod != null) {
                deleteMethod.releaseConnection();
            }
        }
    }

    public long getRecordCount(int tenantId, String tableName, long timeFrom, long timeTo) throws AnalyticsServiceException {
        HttpClient httpclient = new HttpClient();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.RECORD_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.GET_RECORD_COUNT_OPERATION)
                .addParameter(AnalyticsAPIConstants.SESSION_ID, sessionId)
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.TIME_FROM_PARAM, String.valueOf(timeFrom))
                .addParameter(AnalyticsAPIConstants.TIME_TO_PARAM, String.valueOf(timeTo));
        GetMethod getMethod = null;
        try {
            getMethod = new GetMethod(builder.build().toString());
            int status = httpclient.executeMethod(getMethod);
            String response = getResponse(getMethod);
            if (status != HttpServletResponse.SC_ACCEPTED) {
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
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
    }

    public void putRecords(List<Record> records) throws AnalyticsServiceException {
        HttpClient httpclient = new HttpClient();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.RECORD_PROCESSOR_SERVICE_URI);
        PostMethod postMethod = null;
        try {
            postMethod = new PostMethod(builder.build().toString());
            postMethod.addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.PUT_RECORD_OPERATION);
            postMethod.addParameter(AnalyticsAPIConstants.SESSION_ID, sessionId);
            postMethod.addParameter(AnalyticsAPIConstants.RECORDS_PARAM, new Gson().toJson(records));
            int status = httpclient.executeMethod(postMethod);
            String response = getResponse(postMethod);
            if (status != HttpServletResponse.SC_ACCEPTED) {
                throw new AnalyticsServiceException("Unable to put the records. " + response);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }
    }

    public void deleteRecords(int tenantId, String tableName, long timeFrom, long timeTo) throws AnalyticsServiceException {
        HttpClient httpclient = new HttpClient();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.RECORD_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.DELETE_RECORDS_RANGE_OPERATION)
                .addParameter(AnalyticsAPIConstants.SESSION_ID, sessionId)
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.TIME_FROM_PARAM, String.valueOf(timeFrom))
                .addParameter(AnalyticsAPIConstants.TIME_TO_PARAM, String.valueOf(timeTo));
        DeleteMethod deleteMethod = null;
        try {
            deleteMethod = new DeleteMethod(builder.build().toString());
            int status = httpclient.executeMethod(deleteMethod);
            String response = getResponse(deleteMethod);
            if (status != HttpServletResponse.SC_ACCEPTED) {
                throw new AnalyticsServiceException("Unable to delete the record count for the table - " + tableName
                        + ", time from : " + timeFrom + " , timeTo : " + timeTo
                        + " for tenant id : " + tenantId + ". " + response);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        } finally {
            if (deleteMethod != null) {
                deleteMethod.releaseConnection();
            }
        }
    }

    public void deleteRecords(int tenantId, String tableName, List<String> recordIds) throws AnalyticsServiceException {
        HttpClient httpclient = new HttpClient();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.RECORD_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.DELETE_RECORDS_IDS_OPERATION)
                .addParameter(AnalyticsAPIConstants.SESSION_ID, sessionId)
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.RECORD_IDS_PARAM, new Gson().toJson(recordIds));
        DeleteMethod deleteMethod = null;
        try {
            deleteMethod = new DeleteMethod(builder.build().toString());
            int status = httpclient.executeMethod(deleteMethod);
            String response = getResponse(deleteMethod);
            if (status != HttpServletResponse.SC_ACCEPTED) {
                throw new AnalyticsServiceException("Unable to delete the record count for the table - " + tableName
                        + ", records - " + recordIds
                        + " for tenant id : " + tenantId + ". " + response);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        } finally {
            if (deleteMethod != null) {
                deleteMethod.releaseConnection();
            }
        }
    }

    public void setIndices(int tenantId, String tableName, Map<String, IndexType> columns) throws AnalyticsServiceException {
        HttpClient httpclient = new HttpClient();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.INDEX_PROCESSOR_SERVICE_URI);
        PostMethod postMethod = null;
        try {
            postMethod = new PostMethod(builder.build().toString());
            postMethod.addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.SET_INDICES_OPERATION);
            postMethod.addParameter(AnalyticsAPIConstants.SESSION_ID, sessionId);
            postMethod.addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
            postMethod.addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName);
            String indexJson = new Gson().toJson(columns);
            postMethod.addParameter(AnalyticsAPIConstants.INDEX_PARAM, indexJson);
            int status = httpclient.executeMethod(postMethod);
            String response = getResponse(postMethod);
            if (status != HttpServletResponse.SC_ACCEPTED) {
                throw new AnalyticsServiceException("Unable to set the index for table - " + tableName
                        + " with index  - " + indexJson
                        + " for tenant id : " + tenantId + ". " + response);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }
    }

    public Map<String, IndexType> getIndices(int tenantId, String tableName) throws AnalyticsServiceException {
        HttpClient httpclient = new HttpClient();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.INDEX_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.GET_INDICES_OPERATION)
                .addParameter(AnalyticsAPIConstants.SESSION_ID, sessionId)
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName);
        GetMethod getMethod = null;
        try {
            getMethod = new GetMethod(builder.build().toString());
            int status = httpclient.executeMethod(getMethod);
            String response = getResponse(getMethod);
            if (status != HttpServletResponse.SC_ACCEPTED) {
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
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
    }


    public void clearIndices(int tenantId, String tableName) throws AnalyticsServiceException {
        HttpClient httpclient = new HttpClient();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.INDEX_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.DELETE_INDICES_OPERATION)
                .addParameter(AnalyticsAPIConstants.SESSION_ID, sessionId)
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName);
        DeleteMethod deleteMethod = null;
        try {
            deleteMethod = new DeleteMethod(builder.build().toString());
            int status = httpclient.executeMethod(deleteMethod);
            if (status != HttpServletResponse.SC_ACCEPTED) {
                String response = getResponse(deleteMethod);
                throw new AnalyticsServiceException("Unable to get the index for table - " + tableName
                        + " for tenant id : " + tenantId + ". " + response);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        } finally {
            if (deleteMethod != null) {
                deleteMethod.releaseConnection();
            }
        }
    }

    public List<SearchResultEntry> search(int tenantId, String tableName, String language, String query,
                                          int start, int count) throws AnalyticsServiceException {
        HttpClient httpclient = new HttpClient();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.INDEX_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.SEARCH_OPERATION)
                .addParameter(AnalyticsAPIConstants.SESSION_ID, sessionId)
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.LANGUAGE_PARAM, language)
                .addParameter(AnalyticsAPIConstants.QUERY, query)
                .addParameter(AnalyticsAPIConstants.START_PARAM, String.valueOf(start))
                .addParameter(AnalyticsAPIConstants.COUNT_PARAM, String.valueOf(count));
        GetMethod getMethod = null;
        try {
            getMethod = new GetMethod(builder.build().toString());
            int status = httpclient.executeMethod(getMethod);
            String response = getResponse(getMethod);
            if (status != HttpServletResponse.SC_ACCEPTED) {
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
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
    }

    public int searchCount(int tenantId, String tableName, String language, String query) throws AnalyticsServiceException {
        HttpClient httpclient = new HttpClient();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.SEARCH_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.SEARCH_COUNT_OPERATION)
                .addParameter(AnalyticsAPIConstants.SESSION_ID, sessionId)
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.LANGUAGE_PARAM, language)
                .addParameter(AnalyticsAPIConstants.QUERY, query);
        GetMethod getMethod = null;
        try {
            getMethod = new GetMethod(builder.build().toString());
            int status = httpclient.executeMethod(getMethod);
            String response = getResponse(getMethod);
            if (status != HttpServletResponse.SC_ACCEPTED) {
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
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
    }

    public void waitForIndexing(long maxWait) throws AnalyticsServiceException {
        HttpClient httpclient = new HttpClient();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.INDEX_PROCESSOR_SERVICE_URI);
        PostMethod postMethod = null;
        try {
            postMethod = new PostMethod(builder.build().toString());
            postMethod.addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.WAIT_FOR_INDEXING_OPERATION);
            postMethod.addParameter(AnalyticsAPIConstants.SESSION_ID, sessionId);
            postMethod.addParameter(AnalyticsAPIConstants.MAX_WAIT_PARAM, String.valueOf(maxWait));
            int status = httpclient.executeMethod(postMethod);
            String response = getResponse(postMethod);
            if (status != HttpServletResponse.SC_ACCEPTED) {
                throw new AnalyticsServiceException("Unable to configure max wait: " + maxWait + " for indexing. "
                        + response);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }
    }

    public void destroy() throws AnalyticsServiceException {
        HttpClient httpclient = new HttpClient();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.ANALYTICS_SERVICE_PROCESSOR_URI)
                .addParameter(AnalyticsAPIConstants.SESSION_ID, sessionId);
        PostMethod postMethod = null;
        try {
            postMethod = new PostMethod(builder.build().toString());
            postMethod.addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.DESTROY_OPERATION);
            int status = httpclient.executeMethod(postMethod);
            String response = getResponse(postMethod);
            if (status != HttpServletResponse.SC_ACCEPTED) {
                throw new AnalyticsServiceException("Unable to destroy the process . "
                        + response);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        } finally {
            if (postMethod != null) {
                postMethod.releaseConnection();
            }
        }
    }

    public RecordGroup[] getRecordGroup(int tenantId, String tableName, int numPartitionsHint, List<String> columns, long timeFrom,
                                        long timeTo, int recordsFrom, int recordsCount) throws AnalyticsServiceException {
        HttpClient httpclient = new HttpClient();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.ANALYTIC_RECORD_READ_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.GET_RANGE_RECORD_GROUP_OPERATION)
                .addParameter(AnalyticsAPIConstants.SESSION_ID, sessionId)
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.PARTITIONER_NO_PARAM, String.valueOf(numPartitionsHint))
                .addParameter(AnalyticsAPIConstants.COLUMNS_PARAM, new Gson().toJson(columns))
                .addParameter(AnalyticsAPIConstants.TIME_FROM_PARAM, String.valueOf(timeFrom))
                .addParameter(AnalyticsAPIConstants.TIME_TO_PARAM, String.valueOf(timeTo))
                .addParameter(AnalyticsAPIConstants.RECORD_FROM_PARAM, String.valueOf(recordsFrom))
                .addParameter(AnalyticsAPIConstants.COUNT_PARAM, String.valueOf(recordsCount));
        GetMethod getMethod = null;
        try {
            getMethod = new GetMethod(builder.build().toString());
            int status = httpclient.executeMethod(getMethod);
            if (status != HttpServletResponse.SC_ACCEPTED) {
                String response = getResponse(getMethod);
                throw new AnalyticsServiceException("Unable to destroy the process . "
                        + response);
            } else {
                ObjectInputStream objIn = new ObjectInputStream(getMethod.getResponseBodyAsStream());
                return (RemoteRecordGroup[]) objIn.readObject();
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new AnalyticsServiceException("Unable to load the class when trying to deserialize the object. " + e.getMessage(), e);
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
    }

    public RecordGroup[] getRecordGroup(int tenantId, String tableName, int numPartitionsHint, List<String> columns, List<String> ids) throws AnalyticsServiceException {
        HttpClient httpclient = new HttpClient();
        URIBuilder builder = new URIBuilder();
        Gson gson = new Gson();
        builder.setScheme("http").setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.ANALYTIC_RECORD_READ_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.GET_IDS_RECORD_GROUP_OPERATION)
                .addParameter(AnalyticsAPIConstants.SESSION_ID, sessionId)
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.PARTITIONER_NO_PARAM, String.valueOf(numPartitionsHint))
                .addParameter(AnalyticsAPIConstants.COLUMNS_PARAM, gson.toJson(columns))
                .addParameter(AnalyticsAPIConstants.RECORD_IDS_PARAM, gson.toJson(ids));
        GetMethod getMethod = null;
        try {
            getMethod = new GetMethod(builder.build().toString());
            int status = httpclient.executeMethod(getMethod);
            if (status != HttpServletResponse.SC_ACCEPTED) {
                String response = getResponse(getMethod);
                throw new AnalyticsServiceException("Unable to destroy the process . "
                        + response);
            } else {
                ObjectInputStream objIn = new ObjectInputStream(getMethod.getResponseBodyAsStream());
                return (RemoteRecordGroup[]) objIn.readObject();
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new AnalyticsServiceException("Unable to load the class when trying to deserialize the object. " + e.getMessage(), e);
        } finally {
            if (getMethod != null) {
                getMethod.releaseConnection();
            }
        }
    }

    public Iterator<Record> readRecords(RecordGroup recordGroup) throws AnalyticsServiceException {
        HttpClient httpclient = new HttpClient();
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http").setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.ANALYTIC_RECORD_READ_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.SESSION_ID, sessionId)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.READ_RECORD_OPERATION);
        PostMethod postMethod = null;
        try {
            postMethod = new PostMethod(builder.build().toString());
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(out);
            os.writeObject(recordGroup);
            postMethod.setRequestEntity(new ByteArrayRequestEntity(out.toByteArray()));
            os.close();
            out.close();
            int status = httpclient.executeMethod(postMethod);
            if (status != HttpServletResponse.SC_ACCEPTED) {
                String response = getResponse(postMethod);
                throw new AnalyticsServiceException("Unable to read the record group. "
                        + response);
            }
            return new RemoteRecordIterator<Record>(postMethod, postMethod.getResponseBodyAsStream());
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        } finally {
        }
    }

}
