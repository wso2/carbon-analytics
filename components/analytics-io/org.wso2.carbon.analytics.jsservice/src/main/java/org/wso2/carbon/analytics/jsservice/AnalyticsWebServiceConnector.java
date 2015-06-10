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
import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.jsservice.beans.AnalyticsSchemaBean;
import org.wso2.carbon.analytics.jsservice.beans.CategoryDrillDownRequestBean;
import org.wso2.carbon.analytics.jsservice.beans.ColumnKeyValueBean;
import org.wso2.carbon.analytics.jsservice.beans.DrillDownRequestBean;
import org.wso2.carbon.analytics.jsservice.beans.EventBean;
import org.wso2.carbon.analytics.jsservice.beans.QueryBean;
import org.wso2.carbon.analytics.jsservice.beans.Record;
import org.wso2.carbon.analytics.jsservice.beans.ResponseBean;
import org.wso2.carbon.analytics.jsservice.beans.StreamDefinitionBean;
import org.wso2.carbon.analytics.jsservice.beans.StreamDefinitionQueryBean;
import org.wso2.carbon.analytics.jsservice.beans.SubCategoriesBean;
import org.wso2.carbon.analytics.jsservice.exception.JSServiceException;
import org.wso2.carbon.analytics.webservice.stub.AnalyticsWebServiceAnalyticsWebServiceExceptionException;
import org.wso2.carbon.analytics.webservice.stub.AnalyticsWebServiceMalformedStreamDefinitionExceptionException;
import org.wso2.carbon.analytics.webservice.stub.AnalyticsWebServiceStub;
import org.wso2.carbon.analytics.webservice.stub.beans.RecordBean;
import org.wso2.carbon.analytics.webservice.stub.beans.ValuesBatchBean;

import java.lang.reflect.Type;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * This class will expose all the MessageConsoleService stub operations.
 */
public class AnalyticsWebServiceConnector {

    private static final long AXIS2_MIN = Long.MIN_VALUE + 1; //Long.MIN_VALUE is used for unset long variables inside stub
    private Log logger = LogFactory.getLog(AnalyticsWebServiceConnector.class);
    private static final String ANALYTICS_WEB_SERVICE = "AnalyticsWebService";
    private AnalyticsWebServiceStub analyticsWebServiceStub;
    private Gson gson;

    public static final int TYPE_CLEAR_INDEX_DATA = 1;
//    public static final int TYPE_CREATE_TABLE = 2;
//    public static final int TYPE_DELETE_BY_ID = 3;
//    public static final int TYPE_DELETE_BY_RANGE = 4;
//    public static final int TYPE_DELETE_TABLE = 5;
    public static final int TYPE_GET_RECORD_COUNT = 6;
    public static final int TYPE_GET_BY_ID = 7;
    public static final int TYPE_GET_BY_RANGE = 8;
    public static final int TYPE_LIST_TABLES = 9;
    public static final int TYPE_GET_SCHEMA = 10;
//    public static final int TYPE_PUT_RECORDS_TO_TABLE = 11;
    public static final int TYPE_PUT_RECORDS = 12;
    public static final int TYPE_SEARCH = 13;
    public static final int TYPE_SEARCH_COUNT = 14;
//    public static final int TYPE_SET_SCHEMA = 15;
    public static final int TYPE_TABLE_EXISTS = 16;
    public static final int TYPE_WAIT_FOR_INDEXING = 17;
    public static final int TYPE_PAGINATION_SUPPORTED = 18;
    public static final int TYPE_DRILLDOWN_CATEGORIES = 19;
    public static final int TYPE_DRILLDOWN_SEARCH = 20;
    public static final int TYPE_DRILLDOWN_SEARCH_COUNT = 21;
    public static final int TYPE_ADD_STREAM_DEFINITION = 22;
    public static final int TYPE_GET_STREAM_DEFINITION = 23;
    public static final int TYPE_PUBLISH_EVENT = 24;

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
            logger.error("Unable to create AnalyticsWebServiceStub.", axisFault);
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
            logger.error("Unable to create AnalyticsWebServiceStub.", axisFault);
        }
    }

    /**
     * This constructor will be used for Jaggery based stub invocation
     * @param analyticsStub Stub created by jaggery
     */

    public AnalyticsWebServiceConnector(AnalyticsWebServiceStub analyticsStub) {
        gson = new Gson();
        analyticsWebServiceStub = analyticsStub;
    }

    /*public String createTable(String tableName) {
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
            } catch (RemoteException e) {
                logger.error("Failed to create table: " + e.getMessage(), e);
                return gson.toJson(handleResponse(ResponseStatus.FAILED, "Failed to create Table: " +
                                                                          e.getMessage()));
            } catch (AnalyticsWebServiceAnalyticsWebServiceExceptionException e) {
                logger.error("Failed to create table: " + e.getFaultMessage(), e);
                return gson.toJson(handleResponse(ResponseStatus.FAILED, "Failed to create Table: " +
                                                                         e.getFaultMessage()));
            }
        } else {
            return gson.toJson(handleResponse(ResponseStatus.NON_EXISTENT, "table is not defined"));
        }
    }*/

    public ResponseBean tableExists(String tableName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking tableExists for table: " + tableName);
        }
        try {
            boolean tableExists = analyticsWebServiceStub.tableExists(tableName);
            if (logger.isDebugEnabled()) {
                logger.debug("Table's Existance : " + tableExists);
            }
            if (!tableExists) {
                return handleResponse(ResponseStatus.NON_EXISTENT,
                                      "Table : " + tableName + " does not exist.");
            }
        } catch (RemoteException e) {
            logger.error("Failed to check the existance of the table: " + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to check the existance of table: " +
                                                         tableName + ": " + e.getMessage());
        } catch (AnalyticsWebServiceAnalyticsWebServiceExceptionException e) {
            logger.error("Failed to check the existance of the table: " + e.getFaultMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to check the existance of table: " +
                                                         tableName + ": " + e.getFaultMessage());
        }
        return handleResponse(ResponseStatus.SUCCESS,
                              "Table : " + tableName + " exists.");
    }

    public ResponseBean addStreamDefinition(String streamDefAsString) {
        if (logger.isDebugEnabled()) {
            logger.debug("invoking addStreamDefinition");
        }
        try {
            if (streamDefAsString != null && !streamDefAsString.isEmpty()) {
                StreamDefinitionBean streamDefinitionBean = gson.fromJson(streamDefAsString, StreamDefinitionBean.class);
                String streamId = analyticsWebServiceStub.addStreamDefinition(Utils.getStreamDefinition(streamDefinitionBean));
                return handleResponse(ResponseStatus.CREATED, streamId);
            } else {
                return handleResponse(ResponseStatus.NON_EXISTENT, "StreamDefinition is not given");
            }
        } catch (RemoteException e) {
            logger.error("Failed to add the stream definition: " + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to add the stream definition: " +
                                                                     ": " + e.getMessage());
        } catch (AnalyticsWebServiceAnalyticsWebServiceExceptionException e) {
            logger.error("Failed to add the stream definition: " + e.getFaultMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to add the stream definition: " +
                                                                     ": " + e.getFaultMessage());
        } catch (AnalyticsWebServiceMalformedStreamDefinitionExceptionException e) {
            logger.error("Failed to add the stream definition: " + e.getFaultMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to add the stream definition: " +
                                                                     ": " + e.getFaultMessage());
        }
    }

    public ResponseBean getStreamDefinition(String requestAsString) {
        try {
            if (requestAsString != null && !requestAsString.isEmpty()) {
                StreamDefinitionQueryBean queryBean = gson.fromJson(requestAsString, StreamDefinitionQueryBean.class);
                if (logger.isDebugEnabled()) {
                    logger.debug("invoking getStreamDefinition for name: " + queryBean.getName() + " version: " +
                                 queryBean.getVersion());
                }
                org.wso2.carbon.analytics.webservice.stub.beans.StreamDefinitionBean streamDefinitionBean =
                        analyticsWebServiceStub.getStreamDefinition(queryBean.getName(), queryBean.getVersion());
                StreamDefinitionBean streamDefinition = Utils.getStreamDefinitionBean(streamDefinitionBean);
                return handleResponse(ResponseStatus.SUCCESS, gson.toJson(streamDefinition));
            } else {
                return handleResponse(ResponseStatus.NON_EXISTENT, "Name of the Stream is not given");
            }
        } catch (RemoteException e) {
            logger.error("Failed to get the stream definition: " + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to get the stream definition: " +
                                                         ": " + e.getMessage());
        } catch (AnalyticsWebServiceAnalyticsWebServiceExceptionException e) {
            logger.error("Failed to get the stream definition: " + e.getFaultMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to get the stream definition: " +
                                                         ": " + e.getFaultMessage());
        }
    }

    public ResponseBean publishEvent(String eventAsString) {
        if (logger.isDebugEnabled()) {
            logger.debug("invoking publishEvent");
        }
        try {
            if (eventAsString != null && !eventAsString.isEmpty()) {
                EventBean eventBean = gson.fromJson(eventAsString, EventBean.class);
                if (logger.isDebugEnabled()) {
                    logger.debug("publishing event: stream : " + eventBean.getStreamName() + ", version: " +
                                 eventBean.getStreamVersion());
                }
                analyticsWebServiceStub.publishEvent(Utils.getStreamEvent(eventBean));
                return handleResponse(ResponseStatus.SUCCESS, "Event published successfully");

            } else {
                return handleResponse(ResponseStatus.NON_EXISTENT, "Stream event is not provided");
            }
        } catch (RemoteException e) {
            logger.error("Failed to publish event: " + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to publish event: " +
                                                         ": " + e.getMessage());
        } catch (AnalyticsWebServiceAnalyticsWebServiceExceptionException e) {
            logger.error("Failed to publish event: " + e.getFaultMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to publish event: " +
                                                         ": " + e.getFaultMessage());
        }
    }

    public ResponseBean getTableList() {
        String[] tableList;
        try {
            tableList = analyticsWebServiceStub.listTables();
        } catch (RemoteException e) {
            logger.error("Unable to get table list:" + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Unable to get table list: " + e.getMessage());
        } catch (AnalyticsWebServiceAnalyticsWebServiceExceptionException e) {
            logger.error("Unable to get table list:" + e.getFaultMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Unable to get table list: " + e.getFaultMessage());
        }
        if (tableList == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Received an empty table name list!");
            }
            tableList = new String[0];
        }
        return handleResponse(ResponseStatus.SUCCESS, gson.toJson(tableList));
    }

    /*public String deleteTable(String tableName) {
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
            } catch (RemoteException e) {
                logger.error("Unable to delete table: " + e.getMessage(), e);
                return gson.toJson(handleResponse(ResponseStatus.FAILED, "Failed to delete table: " +
                                                                         tableName + ": " + e.getMessage()));
            } catch (AnalyticsWebServiceAnalyticsWebServiceExceptionException e) {
                logger.error("Unable to delete table: " + e.getFaultMessage(), e);
                return gson.toJson(handleResponse(ResponseStatus.FAILED, "Failed to delete table: " +
                                                                         tableName + ": " + e.getFaultMessage()));
            }
        } else {
            return gson.toJson(handleResponse(ResponseStatus.NON_EXISTENT, "Table: " + tableName +
                                                                           " does not exist"));
        }
    }*/

    /*public String deleteRecordsByRange(String tableName, long timeFrom, long timeTo) {
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
        } catch (RemoteException e) {
            logger.error("Failed to delete records by range: " + e.getMessage(), e);
            return gson.toJson(handleResponse(ResponseStatus.FAILED, "Failed to delete records by range for table: " +
                                                                     tableName + ": " + e.getMessage()));
        } catch (AnalyticsWebServiceAnalyticsWebServiceExceptionException e) {
            logger.error("Failed to delete records by range: " + e.getFaultMessage(), e);
            return gson.toJson(handleResponse(ResponseStatus.FAILED, "Failed to delete records by range for table: " +
                                                                     tableName + ": " + e.getFaultMessage()));
        }
    }*/

    /*public String deleteRecordsByIds(String tableName, String idsAsString) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking deleteRecords for tableName : " +
                         tableName);
        }

        if (idsAsString != null) {
            try {
                Type idsListType = new TypeToken<List<String>>(){}.getType();
                List<String> ids = gson.fromJson(idsAsString, idsListType);
                if (logger.isDebugEnabled()) {
                    logger.debug("deleting the records for ids :" + ids);
                }
                analyticsWebServiceStub.deleteByIds(tableName, ids.toArray(new String[ids.size()]));
                return gson.toJson(handleResponse(ResponseStatus.SUCCESS, "Successfully deleted records in table: " +
                                                                          tableName));
            } catch (RemoteException e) {
                logger.error("Failed to delete records by ids: " + e.getMessage(), e);
                return gson.toJson(handleResponse(ResponseStatus.FAILED, "Failed to delete records by IDs for table: " +
                                                                         tableName + ": " + e.getMessage()));
            } catch (AnalyticsWebServiceAnalyticsWebServiceExceptionException e) {
                logger.error("Failed to delete records by ids: " + e.getFaultMessage(), e);
                return gson.toJson(handleResponse(ResponseStatus.FAILED, "Failed to delete records by IDs for table: " +
                                                                         tableName + ": " + e.getFaultMessage()));
            }
        } else {
            return gson.toJson(handleResponse(ResponseStatus.FAILED, "Id list is empty"));
        }
    }*/

    public ResponseBean getRecordCount(String tableName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking getRecordCount for tableName: " + tableName);
        }
        try {
            long recordCount = analyticsWebServiceStub.getRecordCount(tableName, AXIS2_MIN, Long.MAX_VALUE);
            if (logger.isDebugEnabled()) {
                logger.debug("RecordCount for tableName: " + tableName + " is " + recordCount);
            }
            return handleResponse(ResponseStatus.SUCCESS, (new Long(recordCount)).toString());
        } catch (RemoteException e) {
            logger.error("Failed to get record count for table: " + tableName + ": " + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to get record count for table: " +
                                                         tableName + ": " + e.getMessage());
        } catch (AnalyticsWebServiceAnalyticsWebServiceExceptionException e) {
            logger.error("Failed to get record count for table: " + tableName + ": " + e.getFaultMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to get record count for table: " +
                                                         tableName + ": " + e.getFaultMessage());
        }
    }

    public ResponseBean getRecordsByRange(String tableName, String timeFrom, String timeTo, String recordsFrom,
                                    String count, String columns) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking getRecordByRange for tableName: " + tableName);
        }
        try {
            long from = validateNumericValue("timeFrom", timeFrom);
            long to = validateNumericValue("timeTo", timeTo);
            int start = validateNumericValue("start", recordsFrom).intValue();
            int recordCount = validateNumericValue("count", count).intValue();
            RecordBean[] recordBeans;
            String[] columnList = null;
            if (columns != null) {
                Type columnType = new TypeToken<String[]>() {
                }.getType();
                columnList = gson.fromJson(columns, columnType);
            }
            recordBeans = analyticsWebServiceStub.getByRange(tableName, 1, columnList,
                                                                              from, to, start, recordCount);
            List<Record> records = Utils.getRecordBeans(recordBeans);
            return handleResponse(ResponseStatus.SUCCESS, gson.toJson(records));
        } catch (RemoteException e) {
            logger.error("failed to get records from table: '" + tableName + "', " + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to get records from table: '" +
                                                         tableName + "', " + e.getMessage());
        } catch (AnalyticsWebServiceAnalyticsWebServiceExceptionException e) {
            logger.error("failed to get records from table: '" + tableName + "', " + e.getFaultMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to get records from table: '" +
                                                         tableName + "', " + e.getFaultMessage());
        } catch (JSServiceException e) {
            logger.error("failed to get records from table: '" + tableName + "', " + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to get records from table: '" +
                                                         tableName + "', " + e.getMessage());
        }
    }

    public ResponseBean getWithKeyValues(String tableName, String valuesBatch) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking getRecordByRange for tableName: " + tableName);
        }
        try {
            if (valuesBatch != null) {
                ColumnKeyValueBean columnKeyValueBean = gson.fromJson(valuesBatch, ColumnKeyValueBean.class);
                List<Map<String, Object>> valueBatchList = columnKeyValueBean.getValueBatches();
                if (valueBatchList != null && !valueBatchList.isEmpty()) {
                    RecordBean[] recordBeans;
                    String[] columns = columnKeyValueBean.getColumns();
                    ValuesBatchBean[] valuesBatchBeans = Utils.getValuesBatch(valueBatchList);
                    recordBeans = analyticsWebServiceStub.getWithKeyValues(tableName, 1, columns, valuesBatchBeans);
                    List<Record> records = Utils.getRecordBeans(recordBeans);
                    return handleResponse(ResponseStatus.SUCCESS, gson.toJson(records));
                } else {
                    throw new JSServiceException("Values batch is null or empty");
                }
            } else {
                throw new JSServiceException("Values batch is not provided");
            }
        } catch (RemoteException e) {
            logger.error("failed to get records from table: '" + tableName + "', " + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to get records from table: '" +
                                                         tableName + "', " + e.getMessage());
        } catch (AnalyticsWebServiceAnalyticsWebServiceExceptionException e) {
            logger.error("failed to get records from table: '" + tableName + "', " + e.getFaultMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to get records from table: '" +
                                                         tableName + "', " + e.getFaultMessage());
        } catch (JSServiceException e) {
            logger.error("failed to get records from table: '" + tableName + "', " + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to get records from table: '" +
                                                         tableName + "', " + e.getMessage());
        }
    }

    private Long validateNumericValue(String field, String value) throws JSServiceException {
        if (value == null || !NumberUtils.isNumber(value)) {
            throw new JSServiceException("'" + field + "' is not numeric (value is: " + value + ")");
        } else {
            return Long.parseLong(value);
        }
    }

    public ResponseBean getRecordsByIds(String tableName, String idsAsString) {
        if (idsAsString != null && !idsAsString.isEmpty()) {
            try {
                Type idsType = new TypeToken<List<String>>() {
                }.getType();
                List<String> ids = gson.fromJson(idsAsString, idsType);
                String[] idArray = ids.toArray(new String[ids.size()]);
                if (logger.isDebugEnabled()) {
                    logger.debug("Invoking getRecordsByIds for tableName: " + tableName);
                }
                RecordBean[] recordBeans = analyticsWebServiceStub.getById(tableName, 1, null, idArray);
                List<Record> records = Utils.getRecordBeans(recordBeans);
                return handleResponse(ResponseStatus.SUCCESS, gson.toJson(records));
            } catch (RemoteException e) {
                logger.error("failed to get records from table: " + tableName + " : " + e.getMessage(), e);
                return handleResponse(ResponseStatus.FAILED, "Failed to get records from table: " +
                                                             tableName + ": " + e.getMessage());
            } catch (AnalyticsWebServiceAnalyticsWebServiceExceptionException e) {
                logger.error("failed to get records from table: " + tableName + " : " + e.getFaultMessage(), e);
                return handleResponse(ResponseStatus.FAILED, "Failed to get records from table: " +
                                                             tableName + ": " + e.getFaultMessage());
            }
        } else {
            return handleResponse(ResponseStatus.FAILED, "Id list is empty");
        }
    }

    /*public String insertRecords(String recordsAsString) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking insertRecords");
        }

        if (recordsAsString != null) {
            try {
                Type recordListType = new TypeToken<List<Record>>(){}.getType();
                List<Record> records = gson.fromJson(recordsAsString, recordListType);
                if (logger.isDebugEnabled()) {
                    for (Record record : records) {
                        logger.debug(" inserting -- Record Id: " + record.getId() + " values :" +
                                     record.toString());
                    }
                }
                RecordBean[] recordBeans = Utils.getRecords(records);
                RecordBean[] beansWithIds = analyticsWebServiceStub.put(recordBeans);
                List<String> ids = Utils.getIds(beansWithIds);
                return gson.toJson(ids);
            } catch (RemoteException e) {
                logger.error("Failed to put records: " + e.getMessage(), e);
                return gson.toJson(handleResponse(ResponseStatus.FAILED, "Failed to put records: " + e.getMessage()));
            } catch (AnalyticsWebServiceAnalyticsWebServiceExceptionException e) {
                logger.error("Failed to put records: " + e.getFaultMessage(), e);
                return gson.toJson(handleResponse(ResponseStatus.FAILED, "Failed to put records: " + e.getFaultMessage()));
            }
        } else {
            return gson.toJson(handleResponse(ResponseStatus.FAILED, "Record list is empty"));
        }
    }*/

    /*public String insertRecordsToTable(String tableName, String recordsAsString) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking insertRecordsToTable");
        }

        if (recordsAsString != null && tableName != null) {
            try {
                Type recordListType = new TypeToken<List<Record>>(){}.getType();
                List<Record> records = gson.fromJson(recordsAsString, recordListType);
                if (logger.isDebugEnabled()) {
                    for (Record record : records) {
                        logger.debug(" inserting -- Record Id: " + record.getId() + " values :" +
                                     record.toString() + "to table: " + tableName);
                    }
                }
                RecordBean[] recordBeans = Utils.getRecords(tableName, records);
                RecordBean[] beansWithIds = analyticsWebServiceStub.put(recordBeans);
                List<String> ids = Utils.getIds(beansWithIds);
                return gson.toJson(ids);
            } catch (RemoteException e) {
                logger.error("Failed to put records: " + e.getMessage(), e);
                return gson.toJson(handleResponse(ResponseStatus.FAILED, "Failed to put records: " + e.getMessage()));
            } catch (AnalyticsWebServiceAnalyticsWebServiceExceptionException e) {
                logger.error("Failed to put records: " + e.getFaultMessage(), e);
                return gson.toJson(handleResponse(ResponseStatus.FAILED, "Failed to put records: " + e.getFaultMessage()));
            }
        } else {
            String errorMsg = "";
            if (recordsAsString == null){
                errorMsg = gson.toJson(handleResponse(ResponseStatus.FAILED, "Record list is empty"));
            } else if (tableName == null) {
                errorMsg =  gson.toJson(handleResponse(ResponseStatus.FAILED, "tableName is not provided"));
            }
            return errorMsg;
        }
    }*/

    public ResponseBean clearIndexData(String tableName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking clearIndexData for tableName : " +
                         tableName);
        }
        try {
            analyticsWebServiceStub.clearIndices(tableName);
            return handleResponse(ResponseStatus.SUCCESS, "Successfully cleared indices in table: " +
                                                          tableName);
        } catch (RemoteException e) {
            logger.error("Failed to clear indices for table: " + tableName + ": " + e.getMessage());
            return handleResponse(ResponseStatus.FAILED, "Failed to clear indices for table: " +
                                                         tableName + ": " + e.getMessage());
        } catch (AnalyticsWebServiceAnalyticsWebServiceExceptionException e) {
            logger.error("Failed to clear indices for table: " + tableName + ": " + e.getFaultMessage());
            return handleResponse(ResponseStatus.FAILED, "Failed to clear indices for table: " +
                                                         tableName + ": " + e.getFaultMessage());
        }
    }

    public ResponseBean search(String tableName, String queryAsString) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking search for tableName : " + tableName);
        }
        if (queryAsString != null && !queryAsString.isEmpty()) {
            try {
                QueryBean queryBean = gson.fromJson(queryAsString, QueryBean.class);
                RecordBean[] searchResults = analyticsWebServiceStub.search(tableName, queryBean.getQuery(),
                                                                            queryBean.getStart(),
                                                                            queryBean.getCount());
                List<Record> records = Utils.getRecordBeans(searchResults);
                if (logger.isDebugEnabled()) {
                    for (Record record : records) {
                        logger.debug("Search Result -- Record Id: " + record.getId() + " values :" +
                                     record.toString());
                    }
                }
                return handleResponse(ResponseStatus.SUCCESS, gson.toJson(records));
            } catch (RemoteException e) {
                logger.error("Failed to perform search on table: " + tableName + " : " +
                             e.getMessage(), e);
                return handleResponse(ResponseStatus.FAILED,
                                      "Failed to perform search on table: " + tableName + ": " + e.getMessage());
            } catch (AnalyticsWebServiceAnalyticsWebServiceExceptionException e) {
                logger.error("Failed to perform search on table: " + tableName + " : " +
                             e.getFaultMessage(), e);
                return handleResponse(ResponseStatus.FAILED,
                                      "Failed to perform search on table: " + tableName + ": "
                                      + e.getFaultMessage());
            }
        } else {
            return handleResponse(ResponseStatus.FAILED, "Search parameters are not provided");
        }
    }

    public ResponseBean searchCount(String tableName, String queryAsString) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking search count for tableName : " + tableName);
        }
        if (queryAsString != null && !queryAsString.isEmpty()) {
            try {
                QueryBean queryBean = gson.fromJson(queryAsString, QueryBean.class);
                int result = analyticsWebServiceStub.searchCount(tableName, queryBean.getQuery());
                if (logger.isDebugEnabled()) {
                    logger.debug("Search count : " + result);
                }
                return handleResponse(ResponseStatus.SUCCESS, gson.toJson(result));
            } catch (RemoteException e) {
                logger.error("Failed to get the record count for table: " + tableName +
                             " : " + e.getMessage(), e);
                return handleResponse(ResponseStatus.FAILED,
                                      " Failed to get the record count for table: " + tableName + ": " + e.getMessage());
            } catch (AnalyticsWebServiceAnalyticsWebServiceExceptionException e) {
                logger.error("Failed to get the record count for table: " + tableName +
                             " : " + e.getFaultMessage(), e);
                return handleResponse(ResponseStatus.FAILED,
                                      " Failed to get the record count for table: " + tableName + ": " + e.getFaultMessage());
            }
        } else {
            return handleResponse(ResponseStatus.FAILED, " Search parameters not provided");
        }
    }

    public ResponseBean waitForIndexing(long seconds) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking waiting for indexing - timeout : " + seconds + " seconds");
        }
        try {
            analyticsWebServiceStub.waitForIndexing(seconds * Constants.MILLISECONDSPERSECOND);
            return handleResponse(ResponseStatus.SUCCESS, "Indexing Completed successfully");
        } catch (RemoteException e) {
            logger.error("Failed to wait till indexing finishes: " + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED,
                                  "Failed to wait till indexing finishes: " + e.getMessage());
        } catch (AnalyticsWebServiceAnalyticsWebServiceExceptionException e) {
            logger.error("Failed to wait till indexing finishes: " + e.getFaultMessage(), e);
            return handleResponse(ResponseStatus.FAILED,
                                  "Failed to wait till indexing finishes: " + e.getFaultMessage());
        }
    }

    /*public String setTableSchema(String tableName, String schemaAsString) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking setTableSchema for tableName : " + tableName);
        }
        if (schemaAsString != null) {
            try {
                AnalyticsSchemaBean analyticsSchemaBean = gson.fromJson(schemaAsString, AnalyticsSchemaBean.class);
                org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsSchemaBean
                        analyticsSchema = Utils.createAnalyticsSchema(analyticsSchemaBean);
                analyticsWebServiceStub.setTableSchema(tableName, analyticsSchema);
                return gson.toJson(handleResponse(ResponseStatus.SUCCESS, "Successfully set table schema for table: "
                                                                          + tableName));
            } catch (RemoteException e) {
                logger.error("Failed to set the table schema for table: " + tableName + " : " + e.getMessage(), e);
                return gson.toJson(handleResponse(ResponseStatus.FAILED, " Failed to set table schema for table: " +
                                                  tableName + ": " + e.getMessage()));
            } catch (AnalyticsWebServiceAnalyticsWebServiceExceptionException e) {
                logger.error("Failed to set the table schema for table: " + tableName + " : " + e.getFaultMessage(), e);
                return gson.toJson(handleResponse(ResponseStatus.FAILED, " Failed to set table schema for table: " +
                                                                         tableName + ": " + e.getFaultMessage()));
            }
        } else {
            return gson.toJson(handleResponse(ResponseStatus.FAILED,"Table schema is not provided"));
        }
    }*/

    public ResponseBean getTableSchema(String tableName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking getTableSchema for table : " + tableName);
        }
        try {
            org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsSchemaBean
                    analyticsSchema = analyticsWebServiceStub.getTableSchema(tableName);
            AnalyticsSchemaBean analyticsSchemaBean = Utils.createTableSchemaBean(analyticsSchema);
            return handleResponse(ResponseStatus.SUCCESS, gson.toJson(analyticsSchemaBean));
        } catch (RemoteException e) {
            logger.error("Failed to get the table schema for table: " + tableName + " : " + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to get the table schema for table: " +
                                                         tableName + ": " + e.getMessage());
        } catch (AnalyticsWebServiceAnalyticsWebServiceExceptionException e) {
            logger.error("Failed to get the table schema for table: " + tableName + " : " + e.getFaultMessage(), e);
            return handleResponse(ResponseStatus.FAILED, "Failed to get the table schema for table: " +
                                                         tableName + ": " + e.getFaultMessage());
        }
    }

    public ResponseBean isPaginationSupported() {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking isPaginationSupported");
        }
        try {
            return handleResponse(ResponseStatus.SUCCESS, gson.toJson(analyticsWebServiceStub.isPaginationSupported()));
        } catch (RemoteException e) {
            logger.error("Failed to check pagination support" + e.getMessage(), e);
            return handleResponse(ResponseStatus.FAILED,
                                  "Failed to check pagination support: " + e.getMessage());
        }
    }

    public ResponseBean drillDownCategories(String tableName, String queryAsString) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking drillDownCategories for tableName : " + tableName);
        }
        if (queryAsString != null && !queryAsString.isEmpty()) {
            try {
                CategoryDrillDownRequestBean queryBean =
                         gson.fromJson(queryAsString, CategoryDrillDownRequestBean.class);
                org.wso2.carbon.analytics.webservice.stub.beans.CategoryDrillDownRequestBean requestBean =
                        Utils.createCategoryDrillDownRequest(tableName, queryBean);
                org.wso2.carbon.analytics.webservice.stub.beans.SubCategoriesBean searchResults =
                        analyticsWebServiceStub.drillDownCategories(requestBean);
                SubCategoriesBean subCategories = Utils.getSubCategories(searchResults);
                if (logger.isDebugEnabled()) {
                    logger.debug("DrilldownCategory Result -- path: " + Arrays.toString(subCategories.getCategoryPath()) +
                                 " values :" + subCategories.getCategories());

                }
                return handleResponse(ResponseStatus.SUCCESS, gson.toJson(subCategories));
            } catch (RemoteException e) {
                logger.error("Failed to perform categoryDrilldown on table: " + tableName + " : " +
                             e.getMessage(), e);
                return handleResponse(ResponseStatus.FAILED,
                                      "Failed to perform Category Drilldown on table: " +
                                      tableName + ": " + e.getMessage());
            } catch (AnalyticsWebServiceAnalyticsWebServiceExceptionException e) {
                logger.error("Failed to perform categoryDrilldown on table: " + tableName + " : " +
                             e.getFaultMessage(), e);
                return handleResponse(ResponseStatus.FAILED,
                                      "Failed to perform Category Drilldown on table: " +
                                      tableName + ": " + e.getFaultMessage());
            }
        } else {
            return handleResponse(ResponseStatus.FAILED, "Category drilldown parameters " +
                                                         "are not provided");
        }
    }

    public ResponseBean drillDownSearch(String tableName, String queryAsString) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking drillDownCategories for tableName : " + tableName);
        }
        if (queryAsString != null && !queryAsString.isEmpty()) {
            try {
                DrillDownRequestBean queryBean =
                        gson.fromJson(queryAsString,DrillDownRequestBean.class);
                org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsDrillDownRequestBean requestBean =
                        Utils.createDrillDownSearchRequest(tableName, queryBean);
                RecordBean[] records =
                        analyticsWebServiceStub.drillDownSearch(requestBean);
                List<Record> recordBeans = Utils.getRecordBeans(records);
                if (logger.isDebugEnabled()) {
                    for (Record record : recordBeans) {
                        logger.debug("Drilldown Search Result -- Record Id: " + record.getId() + " values :" +
                                     record.toString());
                    }
                }
                return handleResponse(ResponseStatus.SUCCESS, gson.toJson(recordBeans));
            } catch (RemoteException e) {
                logger.error("Failed to perform DrilldownSearch on table: " + tableName + " : " +
                             e.getMessage(), e);
                return handleResponse(ResponseStatus.FAILED,
                                      "Failed to perform DrilldownSearch on table: " +
                                      tableName + ": " + e.getMessage());
            } catch (AnalyticsWebServiceAnalyticsWebServiceExceptionException e) {
                logger.error("Failed to perform DrilldownSearch on table: " + tableName + " : " +
                             e.getFaultMessage(), e);
                return handleResponse(ResponseStatus.FAILED,
                                      "Failed to perform DrilldownSearch on table: " +
                                      tableName + ": " + e.getFaultMessage());
            }
        } else {
            return handleResponse(ResponseStatus.FAILED, "drilldownSearch parameters " +
                                                         "are not provided");
        }
    }

    public ResponseBean drillDownSearchCount(String tableName, String queryAsString) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking drillDownCategories for tableName : " + tableName);
        }
        if (queryAsString != null && !queryAsString.isEmpty()) {
            try {
                DrillDownRequestBean queryBean =
                        gson.fromJson(queryAsString,DrillDownRequestBean.class);
                org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsDrillDownRequestBean requestBean =
                        Utils.createDrillDownSearchRequest(tableName, queryBean);
                double count =
                        analyticsWebServiceStub.drillDownSearchCount(requestBean);
                if (logger.isDebugEnabled()) {
                    logger.debug("Search count Result -- Record Count: " + count);
                }
                return handleResponse(ResponseStatus.SUCCESS, gson.toJson(count));
            } catch (RemoteException e) {
                logger.error("Failed to perform DrilldownSearch Count on table: " + tableName + " : " +
                             e.getMessage(), e);
                return handleResponse(ResponseStatus.FAILED,
                                      "Failed to perform DrilldownSearch Count on table: " +
                                      tableName + ": " + e.getMessage());
            } catch (AnalyticsWebServiceAnalyticsWebServiceExceptionException e) {
                logger.error("Failed to perform DrilldownSearch Count on table: " + tableName + " : " +
                             e.getFaultMessage(), e);
                return handleResponse(ResponseStatus.FAILED,
                                      "Failed to perform DrilldownSearch Count on table: " +
                                      tableName + ": " + e.getFaultMessage());
            }
        } else {
            return handleResponse(ResponseStatus.FAILED, "drilldownSearch parameters " +
                                                         "are not provided");
        }
    }

    public ResponseBean handleResponse(ResponseStatus responseStatus, String message) {
        ResponseBean response;
        switch (responseStatus) {
            case CONFLICT:
                response = getResponseMessage(Constants.Status.FAILED, 409, message);
                break;
            case CREATED:
                response = getResponseMessage(Constants.Status.CREATED, 201, message);
                break;
            case SUCCESS:
                response = getResponseMessage(Constants.Status.SUCCESS, 200, message);
                break;
            case FAILED:
                response = getResponseMessage(Constants.Status.FAILED, 500, message);
                break;
            case INVALID:
                response = getResponseMessage(Constants.Status.FAILED, 400, message);
                break;
            case FORBIDDEN:
                response = getResponseMessage(Constants.Status.UNAUTHORIZED, 403, message);
                break;
            case UNAUTHENTICATED:
                response = getResponseMessage(Constants.Status.UNAUTHENTICATED, 403, message);
                break;
            case NON_EXISTENT:
                response = getResponseMessage(Constants.Status.NON_EXISTENT, 404, message);
                break;
            default:
                response = getResponseMessage(Constants.Status.FAILED, 500, message);
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
    private ResponseBean getResponseMessage(String status, int statusCode, String message) {
        ResponseBean standardResponse = new ResponseBean(status, statusCode);
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
