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
import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.AllClientPNames;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.analytics.api.AnalyticsDataConstants;
import org.wso2.carbon.analytics.api.RemoteRecordIterator;
import org.wso2.carbon.analytics.api.exception.AnalyticsServiceAuthenticationException;
import org.wso2.carbon.analytics.api.exception.AnalyticsServiceException;
import org.wso2.carbon.analytics.api.exception.AnalyticsServiceRemoteException;
import org.wso2.carbon.analytics.api.exception.AnalyticsServiceUnauthorizedException;
import org.wso2.carbon.analytics.api.internal.AnalyticsDataConfiguration;
import org.wso2.carbon.analytics.dataservice.commons.AggregateRequest;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRange;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.CategoryDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
import org.wso2.carbon.analytics.dataservice.commons.SubCategories;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.io.commons.GenericUtils;
import org.wso2.carbon.analytics.io.commons.AnalyticsAPIConstants;
import org.wso2.carbon.base.ServerConfiguration;

import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class represents the client implementation which uses the pool of http connection,
 * and communicate between the remote instance.
 */

@SuppressWarnings("deprecation")
public class AnalyticsAPIHttpClient {
    private static final Log log = LogFactory.getLog(AnalyticsAPIHttpClient.class);
    private static AnalyticsAPIHttpClient instance;
    private String hostname;
    private int port;
    private String protocol;
    private String sessionId;
    private DefaultHttpClient httpClient;
    private Gson gson;

    private AnalyticsAPIHttpClient(String protocol, String hostname, int port,
                                   int maxPerRoute, int maxConnection,
                                   int socketTimeout, int connectionTimeout,
                                   String trustStoreLocation, String trustStorePassword) {
        this.hostname = hostname;
        this.port = port;
        this.protocol = protocol;
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        if (this.protocol.equalsIgnoreCase(AnalyticsDataConstants.HTTP_PROTOCOL)) {
            schemeRegistry.register(
                    new Scheme(this.protocol, port, PlainSocketFactory.getSocketFactory()));
        } else {
            System.setProperty(AnalyticsDataConstants.SSL_TRUST_STORE_SYS_PROP, trustStoreLocation);
            System.setProperty(AnalyticsDataConstants.SSL_TRUST_STORE_PASSWORD_SYS_PROP, trustStorePassword);
            schemeRegistry.register(
                    new Scheme(this.protocol, port, SSLSocketFactory.getSocketFactory()));
        }
        PoolingClientConnectionManager connectionManager = new PoolingClientConnectionManager(schemeRegistry);
        connectionManager.setDefaultMaxPerRoute(maxPerRoute);
        connectionManager.setMaxTotal(maxConnection);
        BasicHttpParams params = new BasicHttpParams();
        params.setParameter(AllClientPNames.SO_TIMEOUT, socketTimeout);
        params.setParameter(AllClientPNames.CONNECTION_TIMEOUT, connectionTimeout);
        this.httpClient = new DefaultHttpClient(connectionManager, params);
        gson = new GsonBuilder().create();
    }

    public static void init(AnalyticsDataConfiguration dataConfiguration) throws AnalyticsServiceException {
        try {
            URL url = new URL(dataConfiguration.getEndpoint());
            instance = new AnalyticsAPIHttpClient(url.getProtocol(), url.getHost(), url.getPort(),
                    dataConfiguration.getMaxConnectionsPerRoute(), dataConfiguration.getMaxConnections(),
                    dataConfiguration.getSocketConnectionTimeoutMS(), dataConfiguration.getConnectionTimeoutMS(),
                    getTrustStoreLocation(dataConfiguration.getTrustStoreLocation()),
                    getTrustStorePassword(dataConfiguration.getTrustStorePassword()));
        } catch (MalformedURLException e) {
            throw new AnalyticsServiceException("Error while initializing the analytics http client. " + e.getMessage(), e);
        }
    }

    private static String getTrustStoreLocation(String trustStoreLocation) {
        if (trustStoreLocation == null || trustStoreLocation.trim().isEmpty()) {
            ServerConfiguration serverConfig = ServerConfiguration.getInstance();
            String trustStore = serverConfig.getFirstProperty(AnalyticsDataConstants.TRUST_STORE_CARBON_CONFIG);
            if (trustStore == null) {
                trustStore = System.getProperty(AnalyticsDataConstants.TRUST_STORE_CARBON_CONFIG);
            }
            return trustStore;
        }
        return new File(trustStoreLocation).getAbsolutePath();
    }

    private static String getTrustStorePassword(String trustStorePassword) {
        if (trustStorePassword == null || trustStorePassword.trim().isEmpty()) {
            ServerConfiguration serverConfig = ServerConfiguration.getInstance();
            String trustStorePw = serverConfig.getFirstProperty(AnalyticsDataConstants.TRUST_STORE_PASSWORD_CARBON_CONFIG);
            if (trustStorePw == null) {
                trustStorePw = System.getProperty(AnalyticsDataConstants.TRUST_STORE_PASSWORD_CARBON_CONFIG);
            }
            return trustStorePw;
        }
        return trustStorePassword;
    }

    public static AnalyticsAPIHttpClient getInstance() {
        return instance;
    }

    public synchronized void authenticate(String username, String password) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(this.protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.MANAGEMENT_SERVICE_URI)
                .setParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.LOGIN_OPERATION);
        try {
            HttpGet getMethod = new HttpGet(builder.build().toString());
            getMethod.addHeader(AnalyticsAPIConstants.AUTHORIZATION_HEADER, AnalyticsAPIConstants.BASIC_AUTH_HEADER + Base64.encode(
                    (username + AnalyticsAPIConstants.SEPARATOR
                            + password).getBytes(StandardCharsets.UTF_8)));
            HttpResponse httpResponse = httpClient.execute(getMethod);
            if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = httpResponse.getStatusLine().toString();
                EntityUtils.consume(httpResponse.getEntity());
                if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_NOT_FOUND) {
                    throw new AnalyticsServiceAuthenticationException("Authentication failed for user : " + username + " ."
                            + "Response received from remote instance : " + response);
                } else {
                    throw new AnalyticsServiceRemoteException("Unable to reach the endpoint : " +
                            builder.build().toString() + ". " + response);
                }
            }
            String response = getResponseString(httpResponse);
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
            throw new AnalyticsServiceRemoteException("Error while connecting to the remote service. "
                    + e.getMessage(), e);
        }
    }

    public void validateAndAuthenticate(String username, String password) throws AnalyticsServiceException {
        if (sessionId == null) {
            int numberOfRetry = 0;
            while (numberOfRetry < AnalyticsDataConstants.MAXIMUM_NUMBER_OF_RETRY) {
                try {
                    numberOfRetry++;
                    authenticate(username, password);
                    return;
                } catch (AnalyticsServiceRemoteException ex) {
                    if (numberOfRetry == AnalyticsDataConstants.MAXIMUM_NUMBER_OF_RETRY - 1) {
                        log.error("Unable to connect to remote service, have retried "
                                + AnalyticsDataConstants.MAXIMUM_NUMBER_OF_RETRY + " times, but unable to reach. ", ex);
                    } else {
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException ignored) {
                        }
                    }
                }
            }
        }
    }

    private String getResponseString(HttpResponse httpResponse) throws AnalyticsServiceException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), StandardCharsets.UTF_8));
            String readLine;
            StringBuffer response = new StringBuffer();
            while (((readLine = br.readLine()) != null)) {
                response.append(readLine);
            }
            return response.toString();
        } catch (IOException e) {
            throw new AnalyticsServiceException("Error while reading the response from the remote service. "
                    + e.getMessage(), e);
        } finally {
            EntityUtils.consumeQuietly(httpResponse.getEntity());
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    log.warn("Error while closing the connection! " + e.getMessage());
                }
            }
        }
    }

    public synchronized void invalidateSessionAndAuthenticate(String userName, String password) {
        sessionId = null;
        validateAndAuthenticate(userName, password);
    }

    public void createTable(int tenantId, String username, String recordStoreName, String tableName, 
            boolean securityEnabled, boolean ifNotExists) throws AnalyticsServiceException, AnalyticsServiceUnauthorizedException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants
                .TABLE_PROCESSOR_SERVICE_URI);
        try {
            HttpPost postMethod = new HttpPost(builder.build().toString());
            postMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            List<NameValuePair> params = new ArrayList<>();
            if (ifNotExists) {
                params.add(new BasicNameValuePair(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.CREATE_IF_NOT_EXISTS_TABLE_OPERATION));
            } else {
                params.add(new BasicNameValuePair(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.CREATE_TABLE_OPERATION));
            }
            if (!securityEnabled) {
                params.add(new BasicNameValuePair(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId)));
            } else {
                params.add(new BasicNameValuePair(AnalyticsAPIConstants.USERNAME_PARAM, username));
            }
            if (recordStoreName != null) {
                params.add(new BasicNameValuePair(AnalyticsAPIConstants.RECORD_STORE_NAME_PARAM, recordStoreName));
            }
            params.add(new BasicNameValuePair(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM, String.valueOf(securityEnabled)));
            params.add(new BasicNameValuePair(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName));
            postMethod.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse httpResponse = httpClient.execute(postMethod);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceUnauthorizedException("Unable to create the table - " + tableName + " for tenant id : "
                        + tenantId + ". " + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponseString(httpResponse);
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

    public void setTableSchema(int tenantId, String username, String tableName,
                               AnalyticsSchema schema, boolean securityEnabled) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.SCHEMA_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.SET_SCHEMA_OPERATION)
                .addParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM, String.valueOf(securityEnabled))
                .addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName);
        if (!securityEnabled) {
            builder.addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
        } else {
            builder.addParameter(AnalyticsAPIConstants.USERNAME_PARAM, username);
        }
        try {
            HttpPost postMethod = new HttpPost(builder.build().toString());
            postMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            postMethod.setEntity(new ByteArrayEntity(GenericUtils.serializeObject(schema)));
            HttpResponse httpResponse = httpClient.execute(postMethod);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceUnauthorizedException("Unable to set the schema for the table - " + tableName
                        + ", schema - " + gson.toJson(schema) + " for tenant id : " + tenantId + ". " + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceException("Unable to set the schema for the table - " + tableName
                        + ", schema - " + gson.toJson(schema) + " for tenant id : " + tenantId + ". " + response);
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

    public AnalyticsSchema getTableSchema(int tenantId, String username, String tableName, boolean securityEnabled) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.SCHEMA_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.GET_SCHEMA_OPERATION)
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM, String.valueOf(securityEnabled));
        if (!securityEnabled) {
            builder.addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
        } else {
            builder.addParameter(AnalyticsAPIConstants.USERNAME_PARAM, username);
        }
        try {
            HttpGet getMethod = new HttpGet(builder.build().toString());
            getMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(getMethod);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceUnauthorizedException("Unable to get the schema for the table - " + tableName
                        + " for tenant id : " + tenantId + ". " + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceException("Unable to get the schema for the table - " + tableName
                        + " for tenant id : " + tenantId + ". " + response);
            } else {
                Object analyticsSchemaObject = GenericUtils.deserializeObject(httpResponse.getEntity().getContent());
                EntityUtils.consumeQuietly(httpResponse.getEntity());
                if (analyticsSchemaObject != null && analyticsSchemaObject instanceof AnalyticsSchema) {
                    return (AnalyticsSchema) analyticsSchemaObject;
                } else {
                    throw new AnalyticsServiceException(getUnexpectedResponseReturnedErrorMsg("getting the table schema",
                            tableName, "analytics schema object ", analyticsSchemaObject));
                }
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. "
                    + e.getMessage(), e);
        }
    }

    public boolean isTableExists(int tenantId, String username, String tableName, boolean securityEnabled) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants
                .TABLE_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.TABLE_EXISTS_OPERATION)
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM, String.valueOf(securityEnabled));
        if (!securityEnabled) {
            builder.addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
        } else {
            builder.addParameter(AnalyticsAPIConstants.USERNAME_PARAM, username);
        }
        try {
            HttpGet getMethod = new HttpGet(builder.build().toString());
            getMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(getMethod);
            String response = getResponseString(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                throw new AnalyticsServiceUnauthorizedException("Unable to check the existence for the table - " + tableName
                        + " for tenant id : " + tenantId + ". " + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
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

    @SuppressWarnings("unchecked")
    public List<String> listTables(int tenantId, String username, boolean securityEnabled) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants
                .TABLE_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.LIST_TABLES_OPERATION)
                .addParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM, String.valueOf(securityEnabled));
        if (!securityEnabled) {
            builder.addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
        } else {
            builder.addParameter(AnalyticsAPIConstants.USERNAME_PARAM, username);
        }
        try {
            HttpGet getMethod = new HttpGet(builder.build().toString());
            getMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(getMethod);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceUnauthorizedException("Unable to get the list of tables for tenant id : "
                        + tenantId + ". " + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceException("Unable to get the list of tables for tenant id : "
                        + tenantId + ". " + response);
            } else {
                Object listOfTablesObj = GenericUtils.deserializeObject(httpResponse.getEntity().getContent());
                EntityUtils.consumeQuietly(httpResponse.getEntity());
                if (listOfTablesObj != null && listOfTablesObj instanceof List) {
                    return (List<String>) listOfTablesObj;
                } else {
                    throw new AnalyticsServiceException(getUnexpectedResponseReturnedErrorMsg("getting list of tables",
                            null, "list of tables", listOfTablesObj));
                }
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. "
                    + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. "
                    + e.getMessage(), e);
        }
    }

    public void deleteTable(int tenantId, String username, String tableName, boolean securityEnabled) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.TABLE_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.DELETE_TABLE_OPERATION)
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM, String.valueOf(securityEnabled));
        if (!securityEnabled) {
            builder.addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
        } else {
            builder.addParameter(AnalyticsAPIConstants.USERNAME_PARAM, username);
        }
        try {
            HttpDelete deleteMethod = new HttpDelete(builder.build().toString());
            deleteMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(deleteMethod);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceUnauthorizedException("Unable to delete the table - " + tableName +
                        " for tenant id : " + tenantId + ". " + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceException("Unable to delete the table - " + tableName + " for tenant id : " + tenantId + ". " + response);
            } else {
                EntityUtils.consume(httpResponse.getEntity());
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        }
    }

    public long getRecordCount(int tenantId, String username, String tableName, long timeFrom, long timeTo, boolean securityEnabled) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.RECORD_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.GET_RECORD_COUNT_OPERATION)
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.TIME_FROM_PARAM, String.valueOf(timeFrom))
                .addParameter(AnalyticsAPIConstants.TIME_TO_PARAM, String.valueOf(timeTo))
                .addParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM, String.valueOf(securityEnabled));
        if (!securityEnabled) {
            builder.addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
        } else {
            builder.addParameter(AnalyticsAPIConstants.USERNAME_PARAM, username);
        }
        try {
            HttpGet getMethod = new HttpGet(builder.build().toString());
            getMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(getMethod);
            String response = getResponseString(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                throw new AnalyticsServiceUnauthorizedException("Unable to get the record count for the table - " + tableName
                        + ", time from : " + timeFrom + " , timeTo : " + timeTo
                        + " for tenant id : " + tenantId + ". " + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
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

    @SuppressWarnings("unchecked")
    public void putRecords(String username, List<Record> records, boolean securityEnabled) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.RECORD_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.PUT_RECORD_OPERATION)
                .addParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM, String.valueOf(securityEnabled));
        if (securityEnabled) {
            builder.addParameter(AnalyticsAPIConstants.USERNAME_PARAM, username);
        }
        try {
            HttpPost postMethod = new HttpPost(builder.build().toString());
            postMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            postMethod.setEntity(new ByteArrayEntity(GenericUtils.serializeObject(records)));
            HttpResponse httpResponse = httpClient.execute(postMethod);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceUnauthorizedException("Unable to put the records. " + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceException("Unable to put the records. " + response);
            } else {
                Object recordIdsObj = GenericUtils.deserializeObject(httpResponse.getEntity().getContent());
                EntityUtils.consumeQuietly(httpResponse.getEntity());
                if (recordIdsObj != null && recordIdsObj instanceof List) {
                    List<String> recordIds = (List<String>) recordIdsObj;
                    int index = 0;
                    for (Record record : records) {
                        record.setId(recordIds.get(index));
                        index++;
                    }
                } else {
                    throw new AnalyticsServiceException(getUnexpectedResponseReturnedErrorMsg("putting the records",
                            null, "list of strings", recordIdsObj));
                }
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        }
    }

    public void deleteRecords(int tenantId, String username, String tableName, long timeFrom, long timeTo, boolean securityEnabled) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.RECORD_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.DELETE_RECORDS_RANGE_OPERATION)
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.TIME_FROM_PARAM, String.valueOf(timeFrom))
                .addParameter(AnalyticsAPIConstants.TIME_TO_PARAM, String.valueOf(timeTo))
                .addParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM, String.valueOf(securityEnabled));
        if (!securityEnabled) {
            builder.addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
        } else {
            builder.addParameter(AnalyticsAPIConstants.USERNAME_PARAM, username);
        }
        try {
            HttpDelete deleteMethod = new HttpDelete(builder.build().toString());
            deleteMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(deleteMethod);
            String response = getResponseString(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                throw new AnalyticsServiceUnauthorizedException("Unable to delete the record count for the table - " + tableName
                        + ", time from : " + timeFrom + " , timeTo : " + timeTo
                        + " for tenant id : " + tenantId + ". " + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
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

    public void deleteRecords(int tenantId, String username, String tableName, List<String> recordIds, boolean securityEnabled) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.RECORD_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.DELETE_RECORDS_IDS_OPERATION)
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.RECORD_IDS_PARAM, gson.toJson(recordIds))
                .addParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM, String.valueOf(securityEnabled));
        if (!securityEnabled) {
            builder.addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
        } else {
            builder.addParameter(AnalyticsAPIConstants.USERNAME_PARAM, username);
        }
        try {
            HttpDelete deleteMethod = new HttpDelete(builder.build().toString());
            deleteMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(deleteMethod);
            String response = getResponseString(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                throw new AnalyticsServiceUnauthorizedException("Unable to delete the record count for the table - " + tableName
                        + ", records - " + recordIds
                        + " for tenant id : " + tenantId + ". " + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
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

    private String getUnexpectedResponseReturnedErrorMsg(String operationName, String tableName, String expectedResult,
                                                         Object foundObj) {
        StringBuilder errorMsgBuilder = new StringBuilder();
        errorMsgBuilder.append("Unexpected response returned from remote analytics service when trying the get the ");
        errorMsgBuilder.append(operationName);
        if (tableName != null) {
            errorMsgBuilder.append(" for table : ");
            errorMsgBuilder.append(tableName);
        } else {
            errorMsgBuilder.append(". ");
        }
        errorMsgBuilder.append("Expected type : ");
        errorMsgBuilder.append(expectedResult);
        errorMsgBuilder.append("but found : ");
        if (foundObj == null) errorMsgBuilder.append("NULL.");
        else errorMsgBuilder.append(foundObj.getClass().getCanonicalName());
        return errorMsgBuilder.toString();
    }

    public void clearIndices(int tenantId, String username, String tableName, boolean securityEnabled) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.INDEX_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.DELETE_INDICES_OPERATION)
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM, String.valueOf(securityEnabled));
        if (!securityEnabled) {
            builder.addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
        } else {
            builder.addParameter(AnalyticsAPIConstants.USERNAME_PARAM, username);
        }
        try {
            HttpDelete deleteMethod = new HttpDelete(builder.build().toString());
            deleteMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(deleteMethod);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceUnauthorizedException("Unable to get the index for table - " + tableName
                        + " for tenant id : " + tenantId + ". " + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponseString(httpResponse);
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

    @SuppressWarnings("unchecked")
    public List<SearchResultEntry> search(int tenantId, String username, String tableName, String query,
                                          int start, int count, List<SortByField> sortByFields, boolean securityEnabled)
            throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.SEARCH_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.SEARCH_OPERATION)
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.QUERY, query)
                .addParameter(AnalyticsAPIConstants.START_PARAM, String.valueOf(start))
                .addParameter(AnalyticsAPIConstants.COUNT_PARAM, String.valueOf(count))
                .addParameter(AnalyticsAPIConstants.SORT_BY_FIELDS_PARAM, gson.toJson(sortByFields))
                .addParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM, String.valueOf(securityEnabled));
        if (!securityEnabled) {
            builder.addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
        } else {
            builder.addParameter(AnalyticsAPIConstants.USERNAME_PARAM, username);
        }
        try {
            HttpGet getMethod = new HttpGet(builder.build().toString());
            getMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(getMethod);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceUnauthorizedException("Unable to search the table - " + tableName
                        + " for tenant id : " + tenantId + " with query : " + query + ". "
                        + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceException("Unable to search the table - " + tableName
                        + " for tenant id : " + tenantId + " with query : " + query + ". "
                        + response);
            } else {
                Object searchResultObj = GenericUtils.deserializeObject(httpResponse.getEntity().getContent());
                EntityUtils.consumeQuietly(httpResponse.getEntity());
                if (searchResultObj != null && searchResultObj instanceof List) {
                    return (List<SearchResultEntry>) searchResultObj;
                } else {
                    throw new AnalyticsServiceException(getUnexpectedResponseReturnedErrorMsg("searching the table",
                            tableName, "List of Search Result Entry objects", searchResultObj));
                }
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        }
    }

    public int searchCount(int tenantId, String username, String tableName, String query,
                           boolean securityEnabled) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.SEARCH_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.SEARCH_COUNT_OPERATION)
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.QUERY, query)
                .addParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM, String.valueOf(securityEnabled));
        if (!securityEnabled) {
            builder.addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
        } else {
            builder.addParameter(AnalyticsAPIConstants.USERNAME_PARAM, username);
        }
        try {
            HttpGet getMethod = new HttpGet(builder.build().toString());
            getMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(getMethod);
            String response = getResponseString(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                throw new AnalyticsServiceUnauthorizedException("Unable to search the table - " + tableName
                        + " for tenant id : " + tenantId + " with query : " + query + ". "
                        + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                throw new AnalyticsServiceException("Unable to search the table - " + tableName
                        + " for tenant id : " + tenantId + " with query : " + query + ". "
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

    public void waitForIndexing(int tenantId, String username, String tableName, long maxWait, boolean securityEnabled)
            throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.INDEX_PROCESSOR_SERVICE_URI);
        try {
            HttpPost postMethod = new HttpPost(builder.build().toString());
            postMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.WAIT_FOR_INDEXING_OPERATION));
            params.add(new BasicNameValuePair(AnalyticsAPIConstants.MAX_WAIT_PARAM, String.valueOf(maxWait)));
            params.add(new BasicNameValuePair(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName));
            params.add(new BasicNameValuePair(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId)));
            params.add(new BasicNameValuePair(AnalyticsAPIConstants.USERNAME_PARAM, username));
            params.add(new BasicNameValuePair(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM, String.valueOf(securityEnabled)));
            postMethod.setEntity(new UrlEncodedFormEntity(params));
            HttpResponse httpResponse = httpClient.execute(postMethod);
            String response = getResponseString(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                throw new AnalyticsServiceUnauthorizedException("Unable to configure max wait: " + maxWait + " for indexing. "
                        + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
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
            String response = getResponseString(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                throw new AnalyticsServiceUnauthorizedException("Unable to destroy the process . "
                        + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                throw new AnalyticsServiceException("Unable to destroy the process . "
                        + response);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        }
    }

    public AnalyticsDataResponse getRecordGroup(int tenantId, String username, String tableName, int numPartitionsHint,
                                                List<String> columns, long timeFrom, long timeTo, int recordsFrom,
                                                int recordsCount, boolean securityEnabled) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.ANALYTIC_RECORD_READ_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.GET_RANGE_RECORD_GROUP_OPERATION)
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.PARTITIONER_NO_PARAM, String.valueOf(numPartitionsHint))
                .addParameter(AnalyticsAPIConstants.COLUMNS_PARAM, new Gson().toJson(columns))
                .addParameter(AnalyticsAPIConstants.TIME_FROM_PARAM, String.valueOf(timeFrom))
                .addParameter(AnalyticsAPIConstants.TIME_TO_PARAM, String.valueOf(timeTo))
                .addParameter(AnalyticsAPIConstants.RECORD_FROM_PARAM, String.valueOf(recordsFrom))
                .addParameter(AnalyticsAPIConstants.COUNT_PARAM, String.valueOf(recordsCount))
                .addParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM, String.valueOf(securityEnabled));
        if (!securityEnabled) {
            builder.addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
        } else {
            builder.addParameter(AnalyticsAPIConstants.USERNAME_PARAM, username);
        }
        try {
            HttpGet getMethod = new HttpGet(builder.build().toString());
            getMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(getMethod);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceUnauthorizedException("Unable to destroy the process . "
                        + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceException("Unable to destroy the process . "
                        + response);
            } else {
                Object analyticsDataResponseObj = GenericUtils.deserializeObject(httpResponse.getEntity().getContent());
                EntityUtils.consumeQuietly(httpResponse.getEntity());
                if (analyticsDataResponseObj != null && analyticsDataResponseObj instanceof AnalyticsDataResponse) {
                    return (AnalyticsDataResponse) analyticsDataResponseObj;                    
                } else {
                    throw new AnalyticsServiceAuthenticationException(getUnexpectedResponseReturnedErrorMsg("getting " +
                            "the record group", tableName, "Analytics Data Response object", analyticsDataResponseObj));
                }
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. "
                    + e.getMessage(), e);
        }
    }

    public AnalyticsDataResponse getWithKeyValues(int tenantId, String username, String tableName, int numPartitionsHint,
                                                  List<String> columns, List<Map<String, Object>> valuesBatch, boolean securityEnabled) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.ANALYTIC_RECORD_READ_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.GET_RECORDS_WITH_KEY_VALUES_OPERATION)
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.PARTITIONER_NO_PARAM, String.valueOf(numPartitionsHint))
                .addParameter(AnalyticsAPIConstants.COLUMNS_PARAM, new Gson().toJson(columns))
                .addParameter(AnalyticsAPIConstants.KEY_VALUE_PARAM, new Gson().toJson(valuesBatch))
                .addParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM, String.valueOf(securityEnabled));
        if (!securityEnabled) {
            builder.addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
        } else {
            builder.addParameter(AnalyticsAPIConstants.USERNAME_PARAM, username);
        }
        try {
            HttpGet getMethod = new HttpGet(builder.build().toString());
            getMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(getMethod);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceUnauthorizedException("Unable to get with key values . "
                        + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceException("Unable to get with key values . "
                        + response);
            } else {
                Object analyticsDataResponseObj = GenericUtils.deserializeObject(httpResponse.getEntity().getContent());
                EntityUtils.consumeQuietly(httpResponse.getEntity());
                if (analyticsDataResponseObj != null && analyticsDataResponseObj instanceof AnalyticsDataResponse) {
                    return (AnalyticsDataResponse) analyticsDataResponseObj;                    
                } else {
                    throw new AnalyticsServiceAuthenticationException(getUnexpectedResponseReturnedErrorMsg("getting " +
                            "with key value", tableName, "Analytics Data Response object", analyticsDataResponseObj));
                }
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. "
                    + e.getMessage(), e);
        }
    }

    public String getRecordStoreNameByTable(int tenantId, String username, String tableName, boolean securityEnabled) {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.ANALYTIC_RECORD_STORE_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.GET_RECORD_STORE_OF_TABLE_OPERATION)
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM, String.valueOf(securityEnabled));
        if (!securityEnabled) {
            builder.addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
        } else {
            builder.addParameter(AnalyticsAPIConstants.USERNAME_PARAM, username);
        }
        try {
            HttpGet getMethod = new HttpGet(builder.build().toString());
            getMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(getMethod);
            String response = getResponseString(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                throw new AnalyticsServiceUnauthorizedException("Unable to get the record store for the table : " + tableName + " ." +
                        response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                throw new AnalyticsServiceException("Unable to get the record store for the table : " + tableName + " ." +
                        response);
            } else {
                if (response.startsWith(AnalyticsAPIConstants.RECORD_STORE_NAME)) {
                    String[] reponseElements = response.split(AnalyticsAPIConstants.SEPARATOR);
                    if (reponseElements.length == 2) {
                        return reponseElements[1].trim();
                    } else {
                        throw new AnalyticsServiceException("Invalid response returned, cannot find record store name" +
                                " message. Response:" + response);
                    }
                } else {
                    throw new AnalyticsServiceException("Invalid response returned, record store name message not found!"
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

    @SuppressWarnings("unchecked")
    public List<String> listRecordStoreNames() {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.ANALYTIC_RECORD_STORE_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.LIST_RECORD_STORES_OPERATION);
        try {
            HttpGet getMethod = new HttpGet(builder.build().toString());
            getMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(getMethod);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceUnauthorizedException("Unable to list the record stores." +
                        response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceException("Unable to list the record stores." +
                        response);
            } else {
                Object listOfRecordStores = GenericUtils.deserializeObject(httpResponse.getEntity().getContent());
                EntityUtils.consumeQuietly(httpResponse.getEntity());
                if (listOfRecordStores != null && listOfRecordStores instanceof List) {
                    return (List<String>) listOfRecordStores;
                } else {
                    throw new AnalyticsServiceException(getUnexpectedResponseReturnedErrorMsg("getting list of record stores",
                            null, "list of record store", listOfRecordStores));
                }
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. "
                    + e.getMessage(), e);
        }
    }

    public AnalyticsDataResponse getRecordGroup(int tenantId, String username, String tableName, int numPartitionsHint, List<String> columns,
                                                List<String> ids, boolean securityEnabled) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        Gson gson = new Gson();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.ANALYTIC_RECORD_READ_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.GET_IDS_RECORD_GROUP_OPERATION)
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.PARTITIONER_NO_PARAM, String.valueOf(numPartitionsHint))
                .addParameter(AnalyticsAPIConstants.COLUMNS_PARAM, gson.toJson(columns))
                .addParameter(AnalyticsAPIConstants.RECORD_IDS_PARAM, gson.toJson(ids))
                .addParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM, String.valueOf(securityEnabled));
        if (!securityEnabled) {
            builder.addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
        } else {
            builder.addParameter(AnalyticsAPIConstants.USERNAME_PARAM, username);
        }
        try {
            HttpGet getMethod = new HttpGet(builder.build().toString());
            getMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(getMethod);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceException("Unable to destroy the process . "
                        + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceException("Unable to destroy the process . "
                        + response);
            } else {
                Object analyticsDataResponseObj = GenericUtils.deserializeObject(httpResponse.getEntity().getContent());
                EntityUtils.consumeQuietly(httpResponse.getEntity());
                if (analyticsDataResponseObj != null && analyticsDataResponseObj instanceof AnalyticsDataResponse) {
                    return (AnalyticsDataResponse) analyticsDataResponseObj;                    
                } else {
                    throw new AnalyticsServiceAuthenticationException(getUnexpectedResponseReturnedErrorMsg("getting " +
                            "the record group", tableName, "Analytics Data Response object", analyticsDataResponseObj));
                }
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. "
                    + e.getMessage(), e);
        }
    }

    public AnalyticsIterator<Record> readRecords(String recordStoreName, RecordGroup recordGroup) throws AnalyticsServiceException {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.ANALYTIC_RECORD_READ_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.READ_RECORD_OPERATION)
                .addParameter(AnalyticsAPIConstants.RECORD_STORE_NAME_PARAM, recordStoreName);
        try {
            HttpPost postMethod = new HttpPost(builder.build().toString());
            postMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            postMethod.setEntity(new ByteArrayEntity(GenericUtils.serializeObject(recordGroup)));
            HttpResponse httpResponse = httpClient.execute(postMethod);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceUnauthorizedException("Unable to read the record group. "
                        + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceException("Unable to read the record group. "
                        + response);
            }
            return new RemoteRecordIterator(httpResponse.getEntity().getContent());
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceAuthenticationException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceAuthenticationException("Error while connecting to the remote service. " + e.getMessage(), e);
        }
    }

    public boolean isPaginationSupported(String recordStoreName) {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(this.protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.MANAGEMENT_SERVICE_URI)
                .setParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.IS_PAGINATION_SUPPORTED_OPERATION)
                .addParameter(AnalyticsAPIConstants.RECORD_STORE_NAME_PARAM, recordStoreName);
        try {
            HttpGet getMethod = new HttpGet(builder.build().toString());
            getMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(getMethod);
            String response = getResponseString(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                throw new AnalyticsServiceUnauthorizedException("Error while checking the pagination support. " + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                throw new AnalyticsServiceException("Error while checking the pagination support. " + response);
            }
            if (response.startsWith(AnalyticsAPIConstants.PAGINATION_SUPPORT)) {
                String[] reponseElements = response.split(AnalyticsAPIConstants.SEPARATOR);
                if (reponseElements.length == 2) {
                    return Boolean.parseBoolean(reponseElements[1]);
                } else {
                    throw new AnalyticsServiceAuthenticationException("Invalid response returned, cannot find " +
                            "pagination support element. Response:" + response);
                }
            } else {
                throw new AnalyticsServiceAuthenticationException("Invalid response returned, no pagination support found!"
                        + response);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceException("Malformed URL provided for pagination support checking. "
                    + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceException("Error while connecting to the remote service. "
                    + e.getMessage(), e);
        }
    }

    public boolean isRecordCountSupported(String recordStoreName) {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(this.protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.MANAGEMENT_SERVICE_URI)
                .setParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.IS_RECORD_COUNT_SUPPORTED_OPERATION)
                .addParameter(AnalyticsAPIConstants.RECORD_STORE_NAME_PARAM, recordStoreName);
        try {
            HttpGet getMethod = new HttpGet(builder.build().toString());
            getMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(getMethod);
            String response = getResponseString(httpResponse);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                throw new AnalyticsServiceUnauthorizedException("Error while checking the record count support. " + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                throw new AnalyticsServiceException("Error while checking the record count support. " + response);
            }
            if (response.startsWith(AnalyticsAPIConstants.RECORD_COUNT_SUPPORT)) {
                String[] reponseElements = response.split(AnalyticsAPIConstants.SEPARATOR);
                if (reponseElements.length == 2) {
                    return Boolean.parseBoolean(reponseElements[1]);
                } else {
                    throw new AnalyticsServiceAuthenticationException("Invalid response returned, cannot find " +
                            "record count support element. Response:" + response);
                }
            } else {
                throw new AnalyticsServiceAuthenticationException("Invalid response returned, no record count support found!"
                        + response);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceException("Malformed URL provided for record count support checking. "
                    + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceException("Error while connecting to the remote service. "
                    + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<SearchResultEntry> drillDownSearch(int tenantId, String username,
                                                   AnalyticsDrillDownRequest drillDownRequest,
                                                   boolean securityEnabled) {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.SEARCH_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.DRILL_DOWN_SEARCH_OPERATION)
                .addParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM, String.valueOf(securityEnabled));
        if (!securityEnabled) {
            builder.addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
        } else {
            builder.addParameter(AnalyticsAPIConstants.USERNAME_PARAM, username);
        }
        try {
            HttpPost postMethod = new HttpPost(builder.build().toString());
            postMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            postMethod.setEntity(new ByteArrayEntity(GenericUtils.serializeObject(drillDownRequest)));
            HttpResponse httpResponse = httpClient.execute(postMethod);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceUnauthorizedException("Unable to process the DrillDown Request. " + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceException("Unable to process the DrillDown Request. " + response);
            } else {
                Object searchResultListObj = GenericUtils.deserializeObject(httpResponse.getEntity().getContent());
                EntityUtils.consumeQuietly(httpResponse.getEntity());
                if (searchResultListObj != null && searchResultListObj instanceof List) {
                    return (List<SearchResultEntry>) searchResultListObj;
                } else {
                    throw new AnalyticsServiceException(getUnexpectedResponseReturnedErrorMsg("preforming drill down search",
                            drillDownRequest.getTableName(), "list of search result entry", searchResultListObj));
                }
            }

        } catch (URISyntaxException e) {
            throw new AnalyticsServiceException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceException("Error while connecting to the remote service. " + e.getMessage(), e);
        }
    }

    public double drillDownSearchCount(int tenantId, String username,
                                    AnalyticsDrillDownRequest drillDownRequest,
                                    boolean securityEnabled) {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.SEARCH_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.DRILL_DOWN_SEARCH_COUNT_OPERATION)
                .addParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM, String.valueOf(securityEnabled));
        if (!securityEnabled) {
            builder.addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
        } else {
            builder.addParameter(AnalyticsAPIConstants.USERNAME_PARAM, username);
        }
        try {
            HttpPost postMethod = new HttpPost(builder.build().toString());
            postMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            postMethod.setEntity(new ByteArrayEntity(GenericUtils.serializeObject(drillDownRequest)));
            HttpResponse httpResponse = httpClient.execute(postMethod);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceUnauthorizedException("Unable to process the drillDown request. "
                        + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceException("Unable to process the drillDown request. "
                        + response);
            } else {
                Object searchCountObj = GenericUtils.deserializeObject(httpResponse.getEntity().getContent());
                EntityUtils.consumeQuietly(httpResponse.getEntity());
                if (searchCountObj != null && searchCountObj instanceof Double) {
                    return (Double) searchCountObj;
                } else {
                    throw new AnalyticsServiceException(getUnexpectedResponseReturnedErrorMsg("preforming drill down search count",
                            drillDownRequest.getTableName(), "number of search result", searchCountObj));
                }
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceException("Error while connecting to the remote service. " + e.getMessage(), e);
        }
    }

    public SubCategories drillDownCategories(int tenantId, String username,
                                             CategoryDrillDownRequest drillDownRequest,
                                             boolean securityEnabled) {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.SEARCH_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.DRILL_DOWN_SEARCH_CATEGORY_OPERATION)
                .addParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM, String.valueOf(securityEnabled));
        if (!securityEnabled) {
            builder.addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
        } else {
            builder.addParameter(AnalyticsAPIConstants.USERNAME_PARAM, username);
        }
        try {
            HttpPost postMethod = new HttpPost(builder.build().toString());
            postMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            postMethod.setEntity(new ByteArrayEntity(GenericUtils.serializeObject(drillDownRequest)));
            HttpResponse httpResponse = httpClient.execute(postMethod);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceUnauthorizedException("Unable to read the Category drilldown object. "
                        + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceException("Unable to read the Category drilldown object. "
                        + response);
            } else {
                Object drillDownCategoriesObj = GenericUtils.deserializeObject(httpResponse.getEntity().getContent());
                EntityUtils.consumeQuietly(httpResponse.getEntity());
                if (drillDownCategoriesObj != null && drillDownCategoriesObj instanceof SubCategories) {
                    return (SubCategories) drillDownCategoriesObj;
                } else {
                    throw new AnalyticsServiceException(getUnexpectedResponseReturnedErrorMsg("preforming drill down" +
                                    " search for categories", drillDownRequest.getTableName(),
                            "object of sub categories", drillDownCategoriesObj));
                }
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceException("Error while connecting to the remote service. " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<AnalyticsDrillDownRange> drillDownRangeCount(int tenantId, String username,
                                                             AnalyticsDrillDownRequest drillDownRequest,
                                                             boolean securityEnabled) {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.SEARCH_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.DRILL_DOWN_SEARCH_RANGE_COUNT_OPERATION)
                .addParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM, String.valueOf(securityEnabled));
        if (!securityEnabled) {
            builder.addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
        } else {
            builder.addParameter(AnalyticsAPIConstants.USERNAME_PARAM, username);
        }
        try {
            HttpPost postMethod = new HttpPost(builder.build().toString());
            postMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            postMethod.setEntity(new ByteArrayEntity(GenericUtils.serializeObject(drillDownRequest)));
            HttpResponse httpResponse = httpClient.execute(postMethod);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceUnauthorizedException("Unable to read the Analytics drilldown object. "
                        + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceException("Unable to read the Analytics drilldown object. "
                        + response);
            } else {
                Object listOfReangeObj = GenericUtils.deserializeObject(httpResponse.getEntity().getContent());
                EntityUtils.consumeQuietly(httpResponse.getEntity());
                if (listOfReangeObj != null && listOfReangeObj instanceof List) {
                    return (List<AnalyticsDrillDownRange>) listOfReangeObj;
                } else {
                    throw new AnalyticsServiceException(getUnexpectedResponseReturnedErrorMsg("preforming drill down range count",
                            drillDownRequest.getTableName(), "list of analytics drill down ranges", listOfReangeObj));
                }
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceException("Error while connecting to the remote service. " + e.getMessage(), e);
        }
    }

    public AnalyticsIterator<Record> searchWithAggregates(int tenantId, String username,
                                                          AggregateRequest aggregateRequest,
                                                          boolean securityEnabled) {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.SEARCH_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.SEARCH_WITH_AGGREGATES_OPERATION)
                .addParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM, String.valueOf(securityEnabled))
                .addParameter(AnalyticsAPIConstants.GROUP_BY_FIELD_PARAM, aggregateRequest.getGroupByField())
                .addParameter(AnalyticsAPIConstants.QUERY, aggregateRequest.getQuery())
                .addParameter(AnalyticsAPIConstants.AGGREGATING_FIELDS, gson.toJson(aggregateRequest.getFields()))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, aggregateRequest.getTableName())
                .addParameter(AnalyticsAPIConstants.AGGREGATE_LEVEL, String.valueOf(aggregateRequest.getAggregateLevel()))
                .addParameter(AnalyticsAPIConstants.AGGREGATE_PARENT_PATH, gson.toJson(aggregateRequest.getParentPath()))
                .addParameter(AnalyticsAPIConstants.AGGREGATE_NO_OF_RECORDS, gson.toJson(aggregateRequest.getNoOfRecords()));
        if (!securityEnabled) {
            builder.addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
        } else {
            builder.addParameter(AnalyticsAPIConstants.USERNAME_PARAM, username);
        }
        try {
            HttpGet getMethod = new HttpGet(builder.build().toString());
            getMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(getMethod);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceUnauthorizedException("Error while searching with aggregates. " + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceException("Error while searching with aggregates. " + response);
            } else {
                return new RemoteRecordIterator(httpResponse.getEntity().getContent());
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceException("Error while connecting to the remote service. " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    public List<AnalyticsIterator<Record>> searchWithAggregates(int tenantId, String username,
                                                          AggregateRequest[] aggregateRequests,
                                                          boolean securityEnabled) {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.SEARCH_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.SEARCH_MULTITABLES_WITH_AGGREGATES_OPERATION)
                .addParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM, String.valueOf(securityEnabled));
        if (!securityEnabled) {
            builder.addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
        } else {
            builder.addParameter(AnalyticsAPIConstants.USERNAME_PARAM, username);
        }
        try {
            HttpPost postMethod = new HttpPost(builder.build().toString());
            postMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            postMethod.setEntity(new ByteArrayEntity(GenericUtils.serializeObject(aggregateRequests)));
            HttpResponse httpResponse = httpClient.execute(postMethod);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceUnauthorizedException("Error while searching with aggregates. " + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceException("Error while searching with aggregates. " + response);
            } else {
                Object aggregateObjs = GenericUtils.deserializeObject(httpResponse.getEntity().getContent());
                List<AnalyticsIterator<Record>> iterators;
                EntityUtils.consumeQuietly(httpResponse.getEntity());
                if (aggregateObjs != null && aggregateObjs instanceof List) {
                    List<List<Record>> aggregatedRecords = (List<List<Record>>) aggregateObjs;
                    iterators = new ArrayList<>();
                    for (List<Record> aggregateRecordsPerTable : aggregatedRecords) {
                        iterators.add(new RecordIterator(aggregateRecordsPerTable));
                    }
                    return iterators;
                } else {
                    throw new AnalyticsServiceException("Error while reading MultiTable Aggregate response.. (unknown format or null..)");
                }
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceException("Error while connecting to the remote service. " + e.getMessage(), e);
        }
    }

    public void reIndex(int tenantId, String username, String tableName, long startTime, long endTime,
                        boolean securityEnabled) {
        URIBuilder builder = new URIBuilder();
        builder.setScheme(protocol).setHost(hostname).setPort(port).setPath(AnalyticsAPIConstants.INDEX_PROCESSOR_SERVICE_URI)
                .addParameter(AnalyticsAPIConstants.OPERATION, AnalyticsAPIConstants.REINDEX_OPERATION)
                .addParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM, String.valueOf(securityEnabled))
                .addParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM, tableName)
                .addParameter(AnalyticsAPIConstants.TIME_FROM_PARAM, String.valueOf(startTime))
                .addParameter(AnalyticsAPIConstants.TIME_TO_PARAM, String.valueOf(endTime));
        if (!securityEnabled) {
            builder.addParameter(AnalyticsAPIConstants.TENANT_ID_PARAM, String.valueOf(tenantId));
        } else {
            builder.addParameter(AnalyticsAPIConstants.USERNAME_PARAM, username);
        }
        try {
            HttpGet getMethod = new HttpGet(builder.build().toString());
            getMethod.addHeader(AnalyticsAPIConstants.SESSION_ID, sessionId);
            HttpResponse httpResponse = httpClient.execute(getMethod);
            if (httpResponse.getStatusLine().getStatusCode() == HttpServletResponse.SC_UNAUTHORIZED) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceUnauthorizedException("Error while re-indexing. " + response);
            } else if (httpResponse.getStatusLine().getStatusCode() != HttpServletResponse.SC_OK) {
                String response = getResponseString(httpResponse);
                throw new AnalyticsServiceException("Error while re-indexing. " + response);
            }
        } catch (URISyntaxException e) {
            throw new AnalyticsServiceException("Malformed URL provided. " + e.getMessage(), e);
        } catch (IOException e) {
            throw new AnalyticsServiceException("Error while connecting to the remote service. " + e.getMessage(), e);
        }
    }

    private static class RecordIterator implements AnalyticsIterator<Record> {

        private List<Record> records;
        private Iterator<Record> iterator;

        public RecordIterator(List<Record> records) {
            this.records = records;
        }

        @Override
        public void close() throws IOException {
            // ignored
        }

        @Override
        public boolean hasNext() {
            if (records == null || records.isEmpty()) {
                return false;
            } else {
                if (iterator == null) {
                    iterator = records.iterator();
                }
                return iterator.hasNext();
            }
        }

        @Override
        public Record next() {
            if (this.hasNext()) {
                return iterator.next();
            } else {
                return null;
            }
        }

        @Override
        public void remove() {
           //ignored
        }
    }
}
