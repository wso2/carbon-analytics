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
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.rs.Record;
import org.wso2.carbon.analytics.datasource.core.rs.RecordGroup;
import org.wso2.carbon.analytics.messageconsole.beans.ColumnBean;
import org.wso2.carbon.analytics.messageconsole.beans.EntityBean;
import org.wso2.carbon.analytics.messageconsole.beans.RecordBean;
import org.wso2.carbon.analytics.messageconsole.beans.TableBean;
import org.wso2.carbon.analytics.messageconsole.exception.MessageConsoleException;
import org.wso2.carbon.analytics.messageconsole.internal.ServiceHolder;
import org.wso2.carbon.context.CarbonContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MessageConsoleService {

    private static final Log logger = LogFactory.getLog(MessageConsoleService.class);

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
        } catch (AnalyticsException e) {
            logger.error("Unable to get table list from Analytics data layer for tenant: " + tenantId, e);
            throw new MessageConsoleException("Unable to get table list from Analytics data layer for tenant: " +
                                              tenantId, e);
        }
    }

    public RecordBean[] getRecords(String tableName) throws MessageConsoleException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();
        List<RecordBean> recordBeanList = new ArrayList<>();
        try {
            RecordGroup[] results = analyticsDataService.get(tenantId, tableName, 1, null, 
                    Long.MIN_VALUE, Long.MAX_VALUE, 0, -1);
            if (results != null) {
                List<Record> records = AnalyticsDSUtils.listRecords(analyticsDataService, results);
                if (records != null && !records.isEmpty()) {
                    for (Record record : records) {
                        recordBeanList.add(createRecordBean(record));
                    }
                }
            }
        } catch (AnalyticsException e) {
            logger.error("Unable to get records from Analytics data layer for tenant: " + tenantId + " and for " +
                         "table:" + tableName, e);
            throw new MessageConsoleException("Unable to get records from Analytics data layer for tenant: " + tenantId + " and for " +
                                              "table:" + tableName, e);
        }

        RecordBean[] recordBeans = new RecordBean[recordBeanList.size()];

        return recordBeanList.toArray(recordBeans);
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
        column8.setName("recordId");
        column8.setType("String");
        column8.setPrimary(false);
        columns[7] = column8;

        table.setColumns(columns);

        return table;
    }
}
