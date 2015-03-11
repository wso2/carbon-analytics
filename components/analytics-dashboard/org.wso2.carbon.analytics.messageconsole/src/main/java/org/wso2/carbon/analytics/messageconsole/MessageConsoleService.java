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
            RecordGroup[] results = analyticsDataService.get(tenantId, tableName, null, -1, -1, 0, -1);
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
        return new TableBean();
    }
}
