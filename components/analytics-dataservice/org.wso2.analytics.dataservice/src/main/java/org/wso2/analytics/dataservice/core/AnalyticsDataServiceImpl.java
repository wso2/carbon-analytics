/*
 *  Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.analytics.dataservice.core;

import org.wso2.analytics.dataservice.AnalyticsDataService;
import org.wso2.analytics.dataservice.commons.*;
import org.wso2.analytics.dataservice.commons.exception.AnalyticsException;
import org.wso2.analytics.dataservice.utils.AnalyticsUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.util.List;
import java.util.Map;

public class AnalyticsDataServiceImpl implements AnalyticsDataService {

    private AnalyticsDataServiceConfiguration loadAnalyticsDataServiceConfig() throws AnalyticsException {
        try {
            File confFile = new File(AnalyticsUtils.getAnalyticsConfDirectory() +
                    File.separator + AnalyticsDataSourceConstants.ANALYTICS_CONF_DIR +
                    File.separator + AnalyticsDataServiceConstants.ANALYTICS_DS_CONFIG_FILE);
            if (!confFile.exists()) {
                throw new AnalyticsException("Cannot initalize analytics data service, " +
                        "the analytics data service configuration file cannot be found at: " +
                        confFile.getPath());
            }
            JAXBContext ctx = JAXBContext.newInstance(AnalyticsDataServiceConfiguration.class);
            Unmarshaller unmarshaller = ctx.createUnmarshaller();
            return (AnalyticsDataServiceConfiguration) unmarshaller.unmarshal(confFile);
        } catch (JAXBException e) {
            throw new AnalyticsException(
                    "Error in processing analytics data service configuration: " + e.getMessage(), e);
        }
    }

    @Override
    public List<String> listRecordStoreNames() {
        return null;
    }

    @Override
    public void createTable(String recordStoreName, String tableName) throws AnalyticsException {

    }

    @Override
    public void createTable(String tableName) throws AnalyticsException {

    }

    @Override
    public String getRecordStoreNameByTable(String tableName) throws AnalyticsException {
        return null;
    }

    @Override
    public void setTableSchema(String tableName, AnalyticsSchema schema) throws AnalyticsException {

    }

    @Override
    public AnalyticsSchema getTableSchema(String tableName) throws AnalyticsException {
        return null;
    }

    @Override
    public boolean tableExists(String tableName) throws AnalyticsException {
        return false;
    }

    @Override
    public void deleteTable(String tableName) throws AnalyticsException {

    }

    @Override
    public List<String> listTables() throws AnalyticsException {
        return null;
    }

    @Override
    public void put(List<Record> records) throws AnalyticsException {

    }

    @Override
    public AnalyticsDataResponse get(String tableName, int numPartitionsHint, List<String> columns, long timeFrom, long timeTo, int recordsFrom, int recordsCount) throws AnalyticsException {
        return null;
    }

    @Override
    public AnalyticsDataResponse get(String tableName, int numPartitionsHint, List<String> columns, List<String> ids) throws AnalyticsException {
        return null;
    }

    @Override
    public AnalyticsDataResponse getWithKeyValues(String tableName, int numPartitionsHint, List<String> columns, List<Map<String, Object>> valuesBatch) throws AnalyticsException {
        return null;
    }

    @Override
    public AnalyticsIterator<Record> readRecords(String recordStoreName, RecordGroup recordGroup) throws AnalyticsException {
        return null;
    }

    @Override
    public void delete(String tableName, long timeFrom, long timeTo) throws AnalyticsException {

    }

    @Override
    public void delete(String tableName, List<String> ids) throws AnalyticsException {

    }

    @Override
    public void destroy() throws AnalyticsException {

    }
}
