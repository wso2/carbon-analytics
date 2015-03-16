package org.wso2.carbon.analytics.messageconsole;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.AnalyticsDSUtils;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.indexing.SearchResultEntry;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.core.rs.Record;
import org.wso2.carbon.analytics.datasource.core.rs.RecordGroup;
import org.wso2.carbon.analytics.messageconsole.beans.ColumnBean;
import org.wso2.carbon.analytics.messageconsole.beans.EntityBean;
import org.wso2.carbon.analytics.messageconsole.beans.RecordBean;
import org.wso2.carbon.analytics.messageconsole.beans.RecordResultBean;
import org.wso2.carbon.analytics.messageconsole.beans.TableBean;
import org.wso2.carbon.analytics.messageconsole.exception.MessageConsoleException;
import org.wso2.carbon.analytics.messageconsole.internal.ServiceHolder;
import org.wso2.carbon.context.CarbonContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessageConsoleService {

    private static final Log logger = LogFactory.getLog(MessageConsoleService.class);
    private static final String LUCENE = "lucene";

    private AnalyticsDataService analyticsDataService;

    public MessageConsoleService() {
        this.analyticsDataService = ServiceHolder.getAnalyticsDataService();
    }

    public List<String> listTables() throws MessageConsoleException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (logger.isDebugEnabled()) {
            logger.debug("Getting table list from data layer for tenant:" + tenantId);
        }
        try {
            return analyticsDataService.listTables(tenantId);
        } catch (Exception e) {
            logger.error("Unable to get table list from Analytics data layer for tenant: " + tenantId, e);
            throw new MessageConsoleException("Unable to get table list from Analytics data layer for tenant: " +
                                              tenantId, e);
        }
    }

    public RecordResultBean getRecords(String tableName, long timeFrom, long timeTo, int startIndex, int recordCount,
                                       String searchQuery)
            throws MessageConsoleException {

        if (logger.isDebugEnabled()) {
            logger.debug("Search Query: " + searchQuery);
            logger.debug("timeFrom: " + timeFrom);
            logger.debug("timeTo: " + timeTo);
            logger.debug("Start Index: " + startIndex);
            logger.debug("Page Size: " + recordCount);
        }

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        RecordResultBean recordResult = new RecordResultBean();
        RecordGroup[] results = null;
        int searchCount = 0;

        if (searchQuery != null && !searchQuery.isEmpty()) {
            try {
                List<SearchResultEntry> searchResults = analyticsDataService.search(tenantId, tableName, LUCENE,
                                                                                    searchQuery, startIndex, recordCount);
                List<String> ids = getRecordIds(searchResults);
                results = analyticsDataService.get(tenantId, tableName, 1, null, ids);
                searchCount = analyticsDataService.searchCount(tenantId, tableName, LUCENE, searchQuery);

                if (logger.isDebugEnabled()) {
                    logger.debug("Query satisfied result count: " + searchResults.size());
                }
            } catch (Exception e) {
                logger.error("Unable to get search indices from Analytics data layer for tenant: " + tenantId +
                             " and for table:" + tableName, e);
                throw new MessageConsoleException("Unable to get indices from Analytics data layer for tenant: " + tenantId +
                                                  " and for table:" + tableName, e);
            }
        } else {
            try {
                results = analyticsDataService.get(tenantId, tableName, 1, null, timeFrom, timeTo, startIndex, recordCount);
            } catch (AnalyticsException e) {
                logger.error("Unable to get records from Analytics data layer for tenant: " + tenantId +
                             " and for table:" + tableName, e);
                throw new MessageConsoleException("Unable to get records from Analytics data layer for tenant: " + tenantId +
                                                  " and for table:" + tableName, e);
            }
        }

        if (results != null) {
            List<RecordBean> recordBeanList = new ArrayList<>();
            List<Record> records;
            try {
                records = AnalyticsDSUtils.listRecords(analyticsDataService, results);
            } catch (Exception e) {
                logger.error("Unable to convert result to record for tenant: " + tenantId +
                             " and for table:" + tableName, e);
                throw new MessageConsoleException("Unable to convert result to record for tenant: " + tenantId +
                                                  " and for table:" + tableName, e);
            }
            if (records != null && !records.isEmpty()) {
                for (Record record : records) {
                    recordBeanList.add(createRecordBean(record));
                }
            }
            RecordBean[] recordBeans = new RecordBean[recordBeanList.size()];
            recordResult.setRecords(recordBeanList.toArray(recordBeans));
            recordResult.setTotalResultCount(searchCount);
        }

        return recordResult;
    }

    private RecordBean createRecordBean(Record record) {
        RecordBean recordBean = new RecordBean();
        recordBean.setRecordId(record.getId());
        recordBean.setTimestamp(record.getTimestamp());
        EntityBean[] entityBeans = new EntityBean[record.getValues().size()];
        int i = 0;
        for (Map.Entry<String, Object> entry : record.getValues().entrySet()) {
            EntityBean entityBean = new EntityBean(entry.getKey(), String.valueOf(entry.getValue()));
            entityBeans[i++] = entityBean;
        }
        recordBean.setEntityBeans(entityBeans);

        return recordBean;
    }

    private List<String> getRecordIds(List<SearchResultEntry> searchResults) {
        List<String> ids = new ArrayList<String>(searchResults.size());
        for (SearchResultEntry searchResult : searchResults) {
            ids.add(searchResult.getId());
        }
        return ids;
    }

    public TableBean getTableInfo(String tableName) {

        TableBean table = new TableBean();
        table.setName("JMX_AGENT_TOOLBOX");

        ColumnBean[] columns = new ColumnBean[8];

        ColumnBean column1 = new ColumnBean();
        column1.setName("heap_mem_init");
        column1.setType("String");
        column1.setPrimary(true);
        columns[0] = column1;

        ColumnBean column2 = new ColumnBean();
        column2.setName("non_heap_mem_used");
        column2.setType("String");
        column2.setPrimary(false);
        columns[1] = column2;

        ColumnBean column3 = new ColumnBean();
        column3.setName("peakThreadCount");
        column3.setType("String");
        column3.setPrimary(false);
        columns[2] = column3;

        ColumnBean column4 = new ColumnBean();
        column4.setName("heap_mem_max");
        column4.setType("String");
        column4.setPrimary(false);
        columns[3] = column4;

        ColumnBean column5 = new ColumnBean();
        column5.setName("heap_mem_used");
        column5.setType("String");
        column5.setPrimary(false);
        columns[4] = column5;

        ColumnBean column6 = new ColumnBean();
        column6.setName("meta_host");
        column6.setType("String");
        column6.setPrimary(false);
        columns[5] = column6;

        ColumnBean column7 = new ColumnBean();
        column7.setName("non_heap_mem_max");
        column7.setType("String");
        column7.setPrimary(false);
        columns[6] = column7;

        ColumnBean column8 = new ColumnBean();
        column8.setName("bam_unique_rec_id");
        column8.setType("String");
        column8.setPrimary(false);
        columns[7] = column8;

        table.setColumns(columns);

        return table;
    }

    public void deleteRecords(String table, String[] recordIds) throws MessageConsoleException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        String ids = Arrays.toString(recordIds);
        if (logger.isDebugEnabled()) {
            logger.debug(ids + " are going to delete from " + table + " in tenant:" + tenantId);
        }
        try {
            analyticsDataService.delete(tenantId, table, Arrays.asList(recordIds));
        } catch (Exception e) {
            logger.error("Unable to delete records" + ids + " from table :" + table, e);
            throw new MessageConsoleException("Unable to delete records" + ids + " from table :" + table, e);
        }
    }

    public void addRecord(String table, String[] columns, String[] values) throws MessageConsoleException {

        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (logger.isDebugEnabled()) {
            logger.info("New record going to add to" + table);
        }
        try {
            AnalyticsSchema schema = analyticsDataService.getTableSchema(tenantId, table);
            Map<String, AnalyticsSchema.ColumnType> columnsMetaInfo = schema.getColumns();

            Map<String, Object> objectMap = new HashMap<>(columns.length);
            for (int i = 0; i < columns.length; i++) {
                String columnName = columns[i];
                String stringValue = values[i];
                if (columnName != null) {
                    AnalyticsSchema.ColumnType columnType = columnsMetaInfo.get(columnName);
                    Object value = stringValue;
                    switch (columnType) {
                        case STRING:
                            break;
                        case INT:
                            value = Integer.valueOf(stringValue);
                            break;
                        case LONG:
                            value = Long.valueOf(stringValue);
                            break;
                        case BOOLEAN:
                            value = Boolean.valueOf(stringValue);
                            break;
                        case FLOAT:
                            value = Float.valueOf(stringValue);
                            break;
                        case DOUBLE:
                            value = Double.valueOf(stringValue);
                            break;
                    }
                    objectMap.put(columnName, value);
                }
            }

            Record record = new Record(tenantId, table, objectMap, System.currentTimeMillis());
            List<Record> records = new ArrayList<Record>(1);
            records.add(record);
            analyticsDataService.put(records);
        } catch (Exception e) {
            logger.error("Unable to add record to table :" + table, e);
            throw new MessageConsoleException("Unable to add record to table :" + table, e);
        }
    }
}
