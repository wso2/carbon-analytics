/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.analytics.restapi;

import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.SecureAnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRange;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.CategoryDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.CategorySearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.IndexType;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SubCategories;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.restapi.beans.AnalyticsSchemaBean;
import org.wso2.carbon.analytics.restapi.beans.CategoryDrillDownRequestBean;
import org.wso2.carbon.analytics.restapi.beans.ColumnTypeBean;
import org.wso2.carbon.analytics.restapi.beans.DrillDownPathBean;
import org.wso2.carbon.analytics.restapi.beans.DrillDownRangeBean;
import org.wso2.carbon.analytics.restapi.beans.DrillDownRequestBean;
import org.wso2.carbon.analytics.restapi.beans.IndexTypeBean;
import org.wso2.carbon.analytics.restapi.beans.RecordBean;
import org.wso2.carbon.analytics.restapi.beans.SubCategoriesBean;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class represents a set of utility functionalities for the analytics REST API.
 */
public class Utils {

    public static final int CATEGORYPATH_FIELD_COUNT = 2;
    public static final float DEFAUL_CATEGORYPATH_WEIGHT = 1.0f;

    /**
     * Gets the analytics data service.
     *
     * @return the analytics data service
     * @throws AnalyticsException
     */
    public static AnalyticsDataService getAnalyticsDataService() throws AnalyticsException {
        AnalyticsDataService analyticsDataService;
        analyticsDataService = (AnalyticsDataService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(AnalyticsDataService.class,
                                null);
        if (analyticsDataService == null) {
            throw new AnalyticsException("AnalyticsDataService is not available.");
        }
        return analyticsDataService;
    }

    /**
     * Gets the analytics data service.
     *
     * @return the analytics data service
     * @throws AnalyticsException
     */
    public static AnalyticsDataAPI getAnalyticsDataAPIs()
            throws AnalyticsException {
        AnalyticsDataAPI analyticsDataAPI;
        analyticsDataAPI = (AnalyticsDataAPI) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(AnalyticsDataAPI.class,
                                null);
        if (analyticsDataAPI == null) {
            throw new AnalyticsException("Analytics Data API is not available.");
        }
        return analyticsDataAPI;
    }

    /**
     * Gets the records from record beans.
     *
     * @param recordBeans the record beans
     * @return the records from record beans
     * @throws AnalyticsException if the tableName is not specified
     */
    public static List<Record> getRecords(String username, List<RecordBean> recordBeans)
            throws AnalyticsException {
        List<Record> records = new ArrayList<>();
        int tenantId = getTenantId(username);
        for (RecordBean recordBean : recordBeans) {
            if (recordBean.getTableName() == null || recordBean.getTableName().isEmpty()) {
                throw new AnalyticsException("TableName cannot be empty!");
            }
            records.add(new Record(recordBean.getId(), tenantId, recordBean.getTableName(), validateAndReturn(recordBean.getValues())));
        }
        return records;
    }

    private static Map<String, Object> validateAndReturn(Map<String, Object> values)
            throws AnalyticsIndexException {
        Map<String, Object> valueMap = new LinkedHashMap<>(0);
        for (Map.Entry<String, Object> recordEntry : values.entrySet()) {
            if (recordEntry.getValue() instanceof List) {
                List<String> pathList = (List<String>) recordEntry.getValue();
                if (pathList.size() > 0) {
                    valueMap.put(recordEntry.getKey(), pathList);
                } else {
                    throw new AnalyticsIndexException("Category path cannot be empty");
                }
            } else {
                valueMap.put(recordEntry.getKey(), recordEntry.getValue());
            }
        }
        return valueMap;
    }

    /**
     * Gets the records from record beans belongs to a specific table.
     *
     * @param recordBeans the record beans
     * @return the records from record beans
     */
    public static List<Record> getRecordsForTable(String username, String tableName,
                                                  List<RecordBean> recordBeans)
            throws AnalyticsException {
        List<Record> records = new ArrayList<>();
        for (RecordBean recordBean : recordBeans) {
            records.add(new Record(recordBean.getId(), getTenantId(username), tableName,
                                   validateAndReturn(recordBean.getValues())));
        }
        return records;
    }

    /**
     * Creates the record beans from records.
     *
     * @param records the records
     * @return the list of recordBeans
     */
    public static List<RecordBean> createRecordBeans(List<Record> records) {
        List<RecordBean> recordBeans = new ArrayList<>();
        for (Record record : records) {
            RecordBean recordBean = createRecordBean(record);
            recordBeans.add(recordBean);
        }
        return recordBeans;
    }

    /**
     * Create a RecordBean object out of a Record object
     *
     * @param record the record object
     * @return RecordBean object
     */
    public static RecordBean createRecordBean(Record record) {
        RecordBean recordBean = new RecordBean();
        recordBean.setId(record.getId());
        recordBean.setTableName(record.getTableName());
        recordBean.setTimestamp(record.getTimestamp());
        recordBean.setValues(record.getValues());
        return recordBean;
    }

    /**
     * Creates the index type bean from index type.
     *
     * @param indexType the index type
     * @return the index type bean
     */
    public static IndexTypeBean createIndexTypeBean(IndexType indexType) {
        switch (indexType) {
            case BOOLEAN:
                return IndexTypeBean.BOOLEAN;
            case FLOAT:
                return IndexTypeBean.FLOAT;
            case DOUBLE:
                return IndexTypeBean.DOUBLE;
            case INTEGER:
                return IndexTypeBean.INTEGER;
            case LONG:
                return IndexTypeBean.LONG;
            case STRING:
                return IndexTypeBean.STRING;
            case FACET:
                return IndexTypeBean.FACET;
            default:
                return IndexTypeBean.STRING;
        }
    }

    /**
     * Creates the index type from index type bean.
     *
     * @param indexTypeBean the index type bean
     * @return the index type
     */
    public static IndexType createIndexType(IndexTypeBean indexTypeBean) {
        switch (indexTypeBean) {
            case BOOLEAN:
                return IndexType.BOOLEAN;
            case FLOAT:
                return IndexType.FLOAT;
            case DOUBLE:
                return IndexType.DOUBLE;
            case INTEGER:
                return IndexType.INTEGER;
            case LONG:
                return IndexType.LONG;
            case STRING:
                return IndexType.STRING;
            case FACET:
                return IndexType.FACET;
            default:
                return IndexType.STRING;
        }
    }

    /**
     * Creates the index type bean map fron index type map.
     *
     * @param indexTypeMap the index type map
     * @return the map
     */
    public static Map<String, IndexTypeBean> createIndexTypeBeanMap(
            Map<String, IndexType> indexTypeMap) {
        Map<String, IndexTypeBean> indexTypeBeanMap = new HashMap<>();
        Set<String> columns = indexTypeMap.keySet();
        for (String column : columns) {
            indexTypeBeanMap.put(column, createIndexTypeBean(indexTypeMap.get(column)));
        }
        return indexTypeBeanMap;
    }

    /**
     * Creates the index type map from index type bean map.
     *
     * @param indexTypeBeanMap the index type bean map
     * @return the map
     */
    public static Map<String, IndexType> createIndexTypeMap(
            Map<String, IndexTypeBean> indexTypeBeanMap) {
        Map<String, IndexType> indexTypeMap = new HashMap<>();
        Set<String> columns = indexTypeBeanMap.keySet();
        for (String column : columns) {
            indexTypeMap.put(column, createIndexType(indexTypeBeanMap.get(column)));
        }
        return indexTypeMap;
    }

    /**
     * Gets the record ids from search results.
     *
     * @param searchResults the search results
     * @return the record ids from search results
     */
    public static List<String> getRecordIds(List<SearchResultEntry> searchResults) {
        List<String> ids = new ArrayList<>();
        for (SearchResultEntry searchResult : searchResults) {
            ids.add(searchResult.getId());
        }
        return ids;
    }

    /**
     * Gets the complete error message.
     *
     * @param msg the Message
     * @param e   the exception
     * @return the complete error message
     */
    public static String getCompleteErrorMessage(String msg, Exception e) {
        StringBuilder message = new StringBuilder(msg);
        if (e.getCause() != null) {
            message.append(". (");
            message.append(e.getCause().getMessage());
            message.append(")");
        } else if (e.getMessage() != null) {
            message.append(". (");
            message.append(e.getMessage());
            message.append(")");
        }
        return message.toString();
    }

    /**
     * Returns the list of iterators given RecordGroups as a parameter
     *
     * @param recordGroups         the recordGroup array of which the iterators to be returned
     * @param analyticsDataService the AnalyticsDataService instance
     * @return list of Iterators of Records
     * @throws AnalyticsException
     */
    public static List<Iterator<Record>> getRecordIterators(RecordGroup[] recordGroups,
                                                            SecureAnalyticsDataService analyticsDataService)
            throws AnalyticsException {

        List<Iterator<Record>> iterators = new ArrayList<>();
        for (RecordGroup recordGroup : recordGroups) {
            iterators.add(analyticsDataService.readRecords(recordGroup));
        }

        return iterators;
    }

    /**
     * Create a Analytics schema from a bean class
     *
     * @param analyticsSchemaBean bean table schema to be converted to Analytics Schema.
     * @return Analytics schema
     */
    public static AnalyticsSchema createAnalyticsSchema(AnalyticsSchemaBean analyticsSchemaBean) {
        Map<String, AnalyticsSchema.ColumnType> columnTypes = new HashMap<>();
        for (Map.Entry<String, ColumnTypeBean> columnEntry : analyticsSchemaBean.getColumns().entrySet()) {
            columnTypes.put(columnEntry.getKey(), getColumnType(columnEntry.getValue()));
        }
        return new AnalyticsSchema(columnTypes, analyticsSchemaBean.getPrimaryKeys());
    }

    /**
     * Create table schema bean from a analytics schema
     *
     * @param analyticsSchema Analytics schema to be converted to table schema bean
     * @return Table schema bean
     */
    public static AnalyticsSchemaBean createTableSchemaBean(AnalyticsSchema analyticsSchema) {
        Map<String, ColumnTypeBean> columnTypeBeanTypes = new HashMap<>();
        List<String> primaryKeys = new ArrayList<>();
        if (analyticsSchema.getColumns() != null) {
            for (Map.Entry<String, AnalyticsSchema.ColumnType> columnTypeEntry :
                    analyticsSchema.getColumns().entrySet()) {
                columnTypeBeanTypes.put(columnTypeEntry.getKey(), getColumnTypeBean(columnTypeEntry.getValue()));
            }
        }
        if (analyticsSchema.getPrimaryKeys() != null) {
            primaryKeys = analyticsSchema.getPrimaryKeys();
        }
        return new AnalyticsSchemaBean(columnTypeBeanTypes, primaryKeys);
    }

    /**
     * Creates the AnalyticsDrilldownRequest object given a drilldownrequestBean class
     *
     * @param bean bean class which represents the drilldown request.
     * @return Equivalent AnalyticsDrilldownRequest object.
     */
    public static AnalyticsDrillDownRequest createDrilldownRequest(DrillDownRequestBean bean) {
        AnalyticsDrillDownRequest drillDownRequest = new AnalyticsDrillDownRequest();
        drillDownRequest.setTableName(bean.getTableName());
        drillDownRequest.setRecordCount(bean.getRecordCount());
        drillDownRequest.setRecordStartIndex(bean.getRecordStart());
        drillDownRequest.setQuery(bean.getQuery());
        drillDownRequest.setScoreFunction(bean.getScoreFunction());
        drillDownRequest.setCategoryPaths(createCategoryPaths(bean.getCategories()));
        drillDownRequest.setRanges(createDrillDownRanges(bean.getRanges()));
        drillDownRequest.setRangeField(bean.getRangeField());
        return drillDownRequest;
    }

    public static CategoryDrillDownRequest createDrilldownRequest(CategoryDrillDownRequestBean bean) {
        CategoryDrillDownRequest drillDownRequest = new CategoryDrillDownRequest();
        drillDownRequest.setTableName(bean.getTableName());
        drillDownRequest.setFieldName(bean.getFieldName());
        drillDownRequest.setQuery(bean.getQuery());
        drillDownRequest.setScoreFunction(bean.getScoreFunction());
        List<String> path = bean.getCategoryPath();
        if (path != null) {
            drillDownRequest.setPath(path.toArray(new String[path.size()]));
        }
        return drillDownRequest;
    }

    public static SubCategoriesBean createSubCategoriesBean(SubCategories categories) {
        SubCategoriesBean bean = new SubCategoriesBean();
        bean.setCategoryPath(categories.getPath());
        List<String> subCategories = new ArrayList<>();
        for (CategorySearchResultEntry entry : categories.getCategories()) {
            subCategories.add(entry.getCategoryName());
        }
        bean.setCategories(subCategories);
        return bean;
    }

    private static List<AnalyticsDrillDownRange> createDrillDownRanges(
            List<DrillDownRangeBean> ranges) {
        List<AnalyticsDrillDownRange> result = new ArrayList<>();
        for (DrillDownRangeBean rangeBean : ranges) {
            AnalyticsDrillDownRange range = new AnalyticsDrillDownRange(rangeBean.getLabel(),
                     rangeBean.getFrom(), rangeBean.getTo());
            result.add(range);
        }
        return result;
    }

    public static List<DrillDownRangeBean> createDrillDownRangeBeans(
            List<AnalyticsDrillDownRange> ranges) {
        List<DrillDownRangeBean> result = new ArrayList<>();
        for (AnalyticsDrillDownRange range : ranges) {
            DrillDownRangeBean bean = new DrillDownRangeBean(range.getLabel(),
                      range.getFrom(), range.getTo(), range.getScore());
            result.add(bean);
        }
        return result;
    }


    private static Map<String, List<String>> createCategoryPaths(
            List<DrillDownPathBean> bean) {
        Map<String, List<String>> categoryPaths = new LinkedHashMap<>();
        for (DrillDownPathBean drillDownPathBean : bean) {
            String[] path = drillDownPathBean.getPath();
            if (path == null) {
                path = new String[]{};
            }
            categoryPaths.put(drillDownPathBean.getFieldName(),
                              Arrays.asList(path));
        }
        return categoryPaths;
    }

    /**
     * convert a column type bean to ColumnType
     *
     * @param columnTypeBean ColumnType Bean to be converted to ColumnType
     * @return ColumnType instance
     */
    private static AnalyticsSchema.ColumnType getColumnType(ColumnTypeBean columnTypeBean) {
        switch (columnTypeBean) {
            case STRING:
                return AnalyticsSchema.ColumnType.STRING;
            case INT:
                return AnalyticsSchema.ColumnType.INTEGER;
            case LONG:
                return AnalyticsSchema.ColumnType.LONG;
            case FLOAT:
                return AnalyticsSchema.ColumnType.FLOAT;
            case DOUBLE:
                return AnalyticsSchema.ColumnType.DOUBLE;
            case BOOLEAN:
                return AnalyticsSchema.ColumnType.BOOLEAN;
            case BINARY:
                return AnalyticsSchema.ColumnType.BINARY;
            default:
                return AnalyticsSchema.ColumnType.STRING;
        }
    }

    /**
     * convert a column type to bean type
     *
     * @param columnType the ColumnType to be converted to bean type
     * @return ColumnTypeBean instance
     */
    private static ColumnTypeBean getColumnTypeBean(AnalyticsSchema.ColumnType columnType) {
        switch (columnType) {
            case STRING:
                return ColumnTypeBean.STRING;
            case INTEGER:
                return ColumnTypeBean.INT;
            case LONG:
                return ColumnTypeBean.LONG;
            case FLOAT:
                return ColumnTypeBean.FLOAT;
            case DOUBLE:
                return ColumnTypeBean.DOUBLE;
            case BOOLEAN:
                return ColumnTypeBean.BOOLEAN;
            case BINARY:
                return ColumnTypeBean.BINARY;
            default:
                return ColumnTypeBean.STRING;
        }
    }

    private static int getTenantId(String username) throws AnalyticsException {
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            return AnalyticsServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            throw new AnalyticsException("Unable to get tenantId for user: " + username, e);
        }
    }
}
