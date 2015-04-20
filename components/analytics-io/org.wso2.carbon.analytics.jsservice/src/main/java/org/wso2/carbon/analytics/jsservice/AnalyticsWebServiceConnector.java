/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.analytics.jsservice;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.jsservice.beans.AnalyticsSchemaBean;
import org.wso2.carbon.analytics.jsservice.beans.IndexConfigurationBean;
import org.wso2.carbon.analytics.jsservice.beans.QueryBean;
import org.wso2.carbon.analytics.jsservice.beans.Record;
import org.wso2.carbon.analytics.jsservice.beans.ResponseBean;
import org.wso2.carbon.analytics.webservice.stub.AnalyticsWebServiceStub;
import org.wso2.carbon.analytics.webservice.stub.beans.RecordBean;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

/**
 * This class will expose all the MessageConsoleService stub operations.
 */
public class AnalyticsWebServiceConnector {

    private Log logger = LogFactory.getLog(AnalyticsWebServiceConnector.class);
    private static final String ANALYTICS_WEB_SERVICE = "AnalyticsWebService";
    private AnalyticsWebServiceStub analyticsWebServiceStub;
    private Gson gson;

    public static final String TYPE_CLEAR_INDICES = "clearIndices";
    public static final String TYPE_CREATE_TABLE = "createTable";
    public static final String TYPE_DELETE_BY_ID = "deleteByIds";
    public static final String TYPE_DELETE_BY_RANGE = "deleteByRange";
    public static final String TYPE_DELETE_TABLE = "deleteTable";
    public static final String TYPE_GET_INDICES = "getIndices";
    public static final String TYPE_GET_RECORD_COUNT = "getCount";
    public static final String TYPE_GET_BY_ID = "getByIds";
    public static final String TYPE_GET_BY_RANGE = "getByRange";
    public static final String TYPE_LIST_TABLES = "listTables";
    public static final String TYPE_GET_SCHEMA = "getSchema";
    public static final String TYPE_PUT_RECORDS = "putRecords";
    public static final String TYPE_SEARCH = "search";
    public static final String TYPE_SEARCH_COUNT = "searchCount";
    public static final String TYPE_SET_INDICES = "setIndices";
    public static final String TYPE_SET_SCHEMA = "setSchema";
    public static final String TYPE_TABLE_EXISTS = "tableExists";
    public static final String TYPE_WAIT_FOR_INDEXING = "waitForIndexing";

    public AnalyticsWebServiceConnector(ConfigurationContext configCtx, String backendServerURL, String cookie) {
        try {
            String analyticsWebServiceUrl = backendServerURL + ANALYTICS_WEB_SERVICE;
            analyticsWebServiceStub = new AnalyticsWebServiceStub(configCtx, analyticsWebServiceUrl);
            ServiceClient analyticsServiceClient = analyticsWebServiceStub._getServiceClient();
            Options options = analyticsServiceClient.getOptions();
            options.setManageSession(true);
            options.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);
            gson = new Gson();
        } catch (AxisFault axisFault) {
            logger.error("Unable to create MessageConsoleStub.", axisFault);
        }
    }

    public AnalyticsWebServiceConnector(ConfigurationContext configCtx, String backendServerURL,
                                        String username, String password) {
        try {
            String analyticsWebServiceUrl = backendServerURL + ANALYTICS_WEB_SERVICE;
            analyticsWebServiceStub = new AnalyticsWebServiceStub(configCtx, analyticsWebServiceUrl);
            ServiceClient analyticsServiceClient = analyticsWebServiceStub._getServiceClient();
            Options options = analyticsServiceClient.getOptions();
            options.setManageSession(true);
            options.setUserName(username);
            options.setPassword(password);
            gson = new Gson();
        } catch (AxisFault axisFault) {
            logger.error("Unable to create MessageConsoleStub.", axisFault);
        }
    }

    public String createTable(String tableName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking createTable tableName : " +
                         tableName);
        }
        if (tableName != null) {
            try {
                if (analyticsWebServiceStub.tableExists(tableName)) {
                    return gson.toJson(handleResponse(ResponseStatus.CONFLICT, "table :" + tableName +
                                                                               " already exists"));
                }
                analyticsWebServiceStub.createTable(tableName);
                return gson.toJson(handleResponse(ResponseStatus.CREATED,
                                                  "Successfully created table: " + tableName));
            } catch (Exception e) {
                logger.error("Failed to create table: " + e.getMessage(), e);
                return gson.toJson(handleResponse(ResponseStatus.FAILED, "Failed to create Table: " +
                                                                          e.getMessage()));
            }
        } else {
            return gson.toJson(handleResponse(ResponseStatus.NON_EXISTENT, "table is not defined"));
        }
    }

    public String tableExists(String tableName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking listTables");
        }
        try {
            boolean tableExists = analyticsWebServiceStub.tableExists(tableName);
            if (logger.isDebugEnabled()) {
                logger.debug("Table's Existance : " + tableExists);
            }
            if (!tableExists) {
                return gson.toJson(handleResponse(ResponseStatus.NON_EXISTENT,
                                                  "Table : " + tableName + " does not exist."));
            }
        } catch (Exception e) {
            logger.error("Failed to check the existance of the table: " + e.getMessage(), e);
            gson.toJson(handleResponse(ResponseStatus.FAILED, "Failed to check the existance of table: " +
                                                              tableName));
        }
        return gson.toJson(handleResponse(ResponseStatus.SUCCESS,
                              "Table : " + tableName + " exists."));
    }

    public String getTableList() {
        Gson gson = new Gson();
        String[] tableList;
        try {
            tableList = analyticsWebServiceStub.listTables();
        } catch (Exception e) {
            logger.error("Unable to get table list:" + e.getMessage(), e);
            return gson.toJson(handleResponse(ResponseStatus.FAILED, "Unable to get table list"));
        }
        if (tableList == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Received an empty table name list!");
            }
            tableList = new String[0];
        }
        return gson.toJson(tableList);
    }

    public String deleteTable(String tableName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking deleteTable for tableName : " +
                         tableName);
        }
        if (tableName != null) {
            try {
                if (analyticsWebServiceStub.tableExists(tableName)) {
                    analyticsWebServiceStub.deleteTable(tableName);
                    return gson.toJson(handleResponse(ResponseStatus.SUCCESS, "Successfully deleted table: " +
                                                                  tableName));
                }
                return gson.toJson(handleResponse(ResponseStatus.NON_EXISTENT, "table: " + tableName +
                                                                   " does not exists."));
            } catch (Exception e) {
                logger.error("Unable to delete table: " + e.getMessage(), e);
                return gson.toJson(handleResponse(ResponseStatus.FAILED, "Failed to delete table: " +
                                                                         tableName));
            }
        } else {
            return gson.toJson(handleResponse(ResponseStatus.NON_EXISTENT, "Table: " + tableName +
                                                                           " does not exist"));
        }
    }

    public String deleteRecordsByRange(String tableName, long timeFrom, long timeTo) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking deleteRecords for tableName : " +
                         tableName);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("deleting the records from " + timeFrom + " to " + timeTo);
        }
        try {
            analyticsWebServiceStub.deleteByRange(tableName, timeFrom, timeTo);
            return gson.toJson(handleResponse(ResponseStatus.SUCCESS, "Successfully deleted records in table: " +
                                                                      tableName));
        } catch (Exception e) {
            logger.error("Failed to delete records by range: " + e.getMessage(), e);
            return gson.toJson(handleResponse(ResponseStatus.FAILED,
                                              "Failed to delete records by range for table: " + tableName));
        }
    }

    public String deleteRecordsByIds(String tableName, String idsAsString) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking deleteRecords for tableName : " +
                         tableName);
        }
        Type idsListType = new TypeToken<List<String>>(){}.getType();
        List<String> ids = gson.fromJson(idsAsString, idsListType);
        if (ids != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("deleting the records for ids :" + ids);
            }
            try {
                analyticsWebServiceStub.deleteByIds(tableName, ids.toArray(new String[ids.size()]));
                return gson.toJson(handleResponse(ResponseStatus.SUCCESS, "Successfully deleted records in table: " +
                                                                          tableName));
            } catch (Exception e) {
                logger.error("Failed to delete records by ids: " + e.getMessage(), e);
                return gson.toJson(handleResponse(ResponseStatus.FAILED,
                                                  "Failed to delete records by IDs for table: " + tableName));
            }
        } else {
            return gson.toJson(handleResponse(ResponseStatus.FAILED, "Id list is empty"));
        }
    }

    public String getRecordCount(String tableName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking getRecordCount for tableName: " + tableName);
        }
        try {
            long recordCount = analyticsWebServiceStub.getRecordCount(tableName, Long.MIN_VALUE, Long.MAX_VALUE);
            if (logger.isDebugEnabled()) {
                logger.debug("RecordCount for tableName: " + tableName + " is " + recordCount);
            }
            return gson.toJson(handleResponse(ResponseStatus.SUCCESS, (new Long(recordCount)).toString()));
        } catch (Exception e) {
            logger.error("Failed to get record count for table: " + tableName);
            return gson.toJson(handleResponse(ResponseStatus.FAILED, "Failed to get record count for table: " +
                                                                     tableName));
        }
    }

    public String getRecordsByRange(String tableName, long timeFrom, long timeTo, int recordsFrom, int count) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking getRecordByRange for tableName: " + tableName);
        }
        try {
            RecordBean[] recordBeans = analyticsWebServiceStub.getByRange(tableName, 1, null,
                                                                          timeFrom, timeTo, recordsFrom, count);
            List<Record> records = Utils.getRecordBeans(recordBeans);
            return gson.toJson(records);
        } catch (Exception e) {
            logger.error("failed to get records from table: " + tableName + " : " + e.getMessage(), e);
            return gson.toJson(handleResponse(ResponseStatus.FAILED, "Failed to get records from table: " + tableName));
        }
    }

    public String getRecordsByIds(String tableName, String idsAsString) {
        Type idsType = new TypeToken<List<String>>(){}.getType();
        List<String> ids = gson.fromJson(idsAsString, idsType);
        String[] idArray = ids.toArray(new String[ids.size()]);
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking getRecordsByIds for tableName: " + tableName);
        }
        try {
            RecordBean[] recordBeans = analyticsWebServiceStub.getById(tableName, 1, null, idArray);
            List<Record> records = Utils.getRecordBeans(recordBeans);
            return gson.toJson(records);
        } catch (Exception e) {
            logger.error("failed to get records from table: " + tableName + " : " + e.getMessage(), e);
            return gson.toJson(handleResponse(ResponseStatus.FAILED, "Failed to get records from table: " +
                                                                     tableName));
        }
    }

    public String insertRecords(String recordsAsString) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking insertRecords");
        }
        Type recordListType = new TypeToken<List<Record>>(){}.getType();
        List<Record> records = gson.fromJson(recordsAsString, recordListType);
        if (records != null) {
            if (logger.isDebugEnabled()) {
                for (Record record : records) {
                    logger.debug(" inserting -- Record Id: " + record.getId() + " values :" +
                                 record.toString());
                }
            }
            RecordBean[] recordBeans = Utils.getRecords(records);
            try {
                analyticsWebServiceStub.put(recordBeans);
                return gson.toJson(handleResponse(ResponseStatus.CREATED, "Successfully created records"));
            } catch (Exception e) {
                logger.error("Failed to put records: " + e.getMessage(), e);
                return gson.toJson(handleResponse(ResponseStatus.FAILED, "Failed to put records"));
            }
        } else {
            return gson.toJson(handleResponse(ResponseStatus.FAILED, "Record list is empty"));
        }
    }

    public String setIndices(String tableName,
                               String indexInfoAsString) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking setIndices for tableName : " +
                         tableName);
        }
        IndexConfigurationBean indexInfo = gson.fromJson(indexInfoAsString, IndexConfigurationBean.class);
        if (indexInfo != null) {
            org.wso2.carbon.analytics.webservice.stub.beans.IndexConfigurationBean
                    indexConfigurationBean = Utils.createIndexConfiguration(indexInfo);
            try {
                analyticsWebServiceStub.setIndices(tableName, indexConfigurationBean);
                return gson.toJson(handleResponse(ResponseStatus.CREATED, "Successfully set indices in table: " +
                                                              tableName));
            } catch (Exception e) {
                logger.error("Failed to set indices for table: " + tableName + " : " + e.getMessage(), e);
                return gson.toJson(handleResponse(ResponseStatus.FAILED,
                                                  "Failed to set indices for table: " + tableName));
            }
        } else {
            return gson.toJson(handleResponse(ResponseStatus.FAILED, "Index information is not provided"));
        }
    }

    public String getIndices(String tableName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking getIndices for tableName : " +
                         tableName);
        }
        try {
            org.wso2.carbon.analytics.webservice.
                    stub.beans.IndexConfigurationBean indexInfo = analyticsWebServiceStub.getIndices(tableName);
            String[] scoreParams = indexInfo.getScoreParams();
            IndexConfigurationBean indexConfigurationBean = new IndexConfigurationBean();
            indexConfigurationBean.setIndices(Utils.createIndexTypeBeanMap(indexInfo.getIndices()));
            indexConfigurationBean.setScoreParams(Arrays.asList(scoreParams));
            if (logger.isDebugEnabled()) {
                logger.debug("Getting indices : " + indexConfigurationBean.getIndices().keySet());
            }
            return gson.toJson(indexConfigurationBean);
        } catch (Exception e) {
            logger.error("Failed to get indices for table: " + tableName + " : " + e.getMessage(), e);
            return gson.toJson(handleResponse(ResponseStatus.FAILED, "Failed to get indices for table: " +
                                                                     tableName));
        }
    }

    public String clearIndices(String tableName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking clearIndices for tableName : " +
                         tableName);
        }
        try {
            analyticsWebServiceStub.clearIndices(tableName);
            return gson.toJson(handleResponse(ResponseStatus.SUCCESS, "Successfully cleared indices in table: " +
                                                                      tableName));
        } catch (Exception e) {
            logger.error("Failed to clear indices for table: " + tableName);
            return gson.toJson(handleResponse(ResponseStatus.FAILED, "Failed to clear indices for table: " +
                                                                     tableName));
        }
    }

    public String search(String queryAsString) {
        QueryBean queryBean = gson.fromJson(queryAsString, QueryBean.class);
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking search for tableName : " + queryBean.getTableName());
        }
        if (queryBean != null) {
            try {
                RecordBean[] searchResults = analyticsWebServiceStub.search(queryBean.getTableName(),
                                                                            queryBean.getLanguage(), queryBean.getQuery(), queryBean.getStart(),
                                                                            queryBean.getCount());
                List<Record> records = Utils.getRecordBeans(searchResults);
                if (logger.isDebugEnabled()) {
                    for (Record record : records) {
                        logger.debug("Search Result -- Record Id: " + record.getId() + " values :" +
                                     record.toString());
                    }
                }
                return gson.toJson(records);
            } catch (Exception e) {
                logger.error("Failed to perform search on table: " + queryBean.getTableName() + " : " +
                             e.getMessage(), e);
                return gson.toJson(handleResponse(ResponseStatus.FAILED,
                          "Failed to perform search on table: " + queryBean.getTableName()));
            }
        } else {
            return gson.toJson(handleResponse(ResponseStatus.FAILED, "Search parameters are not provided"));
        }
    }

    public String searchCount(String queryAsString) {
        QueryBean queryBean = gson.fromJson(queryAsString, QueryBean.class);
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking search count for tableName : " + queryBean.getTableName());
        }
        if (queryBean != null) {
            try {
                int result = analyticsWebServiceStub.searchCount(queryBean.getTableName(),
                                                                 queryBean.getLanguage(), queryBean.getQuery());
                if (logger.isDebugEnabled()) {
                    logger.debug("Search count : " + result);
                }
                return gson.toJson(result);
            } catch (Exception e) {
                logger.error("Failed to get the record count for table: " + queryBean.getTableName() +
                             " : " + e.getMessage(), e);
                return gson.toJson(handleResponse(ResponseStatus.FAILED,
                            " Failed to get the record count for table: " + queryBean.getTableName()));
            }
        } else {
            return gson.toJson(handleResponse(ResponseStatus.FAILED, " Search parameters not provided"));
        }
    }

    public String waitForIndexing(long seconds) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking waiting for indexing - timeout : " + seconds + " seconds");
        }
        try {
            analyticsWebServiceStub.waitForIndexing(seconds * Constants.MILLISECONDSPERSECOND);
            return gson.toJson(handleResponse(ResponseStatus.SUCCESS, "Indexing Completed successfully"));
        } catch (Exception e) {
            logger.error("Failed to wait till indexing finishes: " + e.getMessage(), e);
            return gson.toJson(handleResponse(ResponseStatus.FAILED, "Failed to wait till indexing finishes"));
        }
    }

    public String setTableSchema(String tableName,
                                   String schemaAsString) {
        AnalyticsSchemaBean analyticsSchemaBean = gson.fromJson(schemaAsString, AnalyticsSchemaBean.class);
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking setTableSchema for tableName : " + tableName);
        }
        if (analyticsSchemaBean != null) {
            org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsSchemaBean
                    analyticsSchema = Utils.createAnalyticsSchema(analyticsSchemaBean);
            try {
                analyticsWebServiceStub.setTableSchema(tableName, analyticsSchema);
                return gson.toJson(handleResponse(ResponseStatus.SUCCESS, "Successfully set table schema for table: "
                                                                          + tableName));
            } catch (Exception e) {
                logger.error("Failed to set the table schema for table: " + tableName + " : " + e.getMessage(), e);
                return gson.toJson(handleResponse(ResponseStatus.FAILED,
                                                  " Failed to set table schema for table: " + tableName));
            }
        } else {
            return gson.toJson(handleResponse(ResponseStatus.FAILED,"Table schema is not provided"));
        }
    }

    public String getTableSchema(String tableName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking getTableSchema for table : " + tableName);
        }
        try {
            org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsSchemaBean
                    analyticsSchema = analyticsWebServiceStub.getTableSchema(tableName);
            AnalyticsSchemaBean analyticsSchemaBean = Utils.createTableSchemaBean(analyticsSchema);
            return gson.toJson(analyticsSchemaBean);
        } catch (Exception e) {
            logger.error("Failed to get the table schema for table: " + tableName + " : " + e.getMessage(), e);
            return gson.toJson(handleResponse(ResponseStatus.FAILED,
                                              "Failed to get the table schema for table: " + tableName));
        }
    }

    public ResponseBean handleResponse(ResponseStatus responseStatus, String message) {
        ResponseBean response;
        switch (responseStatus) {
            case CONFLICT:
                response = getResponseMessage(Constants.Status.FAILED, message);
                break;
            case CREATED:
                response = getResponseMessage(Constants.Status.CREATED, message);
                break;
            case SUCCESS:
                response = getResponseMessage(Constants.Status.SUCCESS, message);
                break;
            case FAILED:
                response = getResponseMessage(Constants.Status.FAILED, message);
                break;
            case INVALID:
                response = getResponseMessage(Constants.Status.FAILED, message);
                break;
            case FORBIDDEN:
                response = getResponseMessage(Constants.Status.UNAUTHORIZED, message);
                break;
            case UNAUTHENTICATED:
                response = getResponseMessage(Constants.Status.UNAUTHORIZED, message);
                break;
            case NON_EXISTENT:
                response = getResponseMessage(Constants.Status.NON_EXISTENT, message);
                break;
            default:
                response = getResponseMessage(Constants.Status.FAILED, message);
                break;
        }
        return response;
    }

    /**
     * Gets the response message.
     * @param status the status
     * @param message the message
     * @return the response message
     */
    private ResponseBean getResponseMessage(String status, String message) {
        ResponseBean standardResponse = new ResponseBean(status);
        if (message != null) {
            standardResponse.setMessage(message);
        }
        return standardResponse;
    }

    /**
     * The Enum ResponseStatus.
     */
    public enum ResponseStatus {
        /** The "conflict" response */
        CONFLICT,
        /** The "created" response */
        CREATED,
        /** The "success" response. */
        SUCCESS,
        /** The "failed" response. */
        FAILED,
        /** The "invalid" response. */
        INVALID,
        /** The "forbidden" response. */
        FORBIDDEN,
        /** The "forbidden" response. */
        UNAUTHENTICATED,
        /** The "non existent" response. */
        NON_EXISTENT
    }
}
