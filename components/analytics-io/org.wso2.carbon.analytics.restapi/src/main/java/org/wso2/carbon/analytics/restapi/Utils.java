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
import org.wso2.carbon.analytics.dataservice.commons.AggregateField;
import org.wso2.carbon.analytics.dataservice.commons.AggregateRequest;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRange;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.CategoryDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.CategorySearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SortType;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
import org.wso2.carbon.analytics.dataservice.commons.SubCategories;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinitionExt;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.restapi.beans.AggregateFieldBean;
import org.wso2.carbon.analytics.restapi.beans.AggregateRequestBean;
import org.wso2.carbon.analytics.restapi.beans.AnalyticsSchemaBean;
import org.wso2.carbon.analytics.restapi.beans.CategoryDrillDownRequestBean;
import org.wso2.carbon.analytics.restapi.beans.ColumnDefinitionBean;
import org.wso2.carbon.analytics.restapi.beans.ColumnTypeBean;
import org.wso2.carbon.analytics.restapi.beans.DrillDownPathBean;
import org.wso2.carbon.analytics.restapi.beans.DrillDownRangeBean;
import org.wso2.carbon.analytics.restapi.beans.DrillDownRequestBean;
import org.wso2.carbon.analytics.restapi.beans.RecordBean;
import org.wso2.carbon.analytics.restapi.beans.SortByFieldBean;
import org.wso2.carbon.analytics.restapi.beans.SubCategoriesBean;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a set of utility functionalities for the analytics REST API.
 */
public class Utils {

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
     * Gets User Realm OSGI service.
     *
     * @return the relam service instance.
     * @throws AnalyticsException
     */
    public static RealmService getRealmService()
            throws AnalyticsException {
        RealmService realmService;
        realmService = (RealmService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(RealmService.class, null);
        if (realmService == null) {
            throw new AnalyticsException("RealmService is not available.");
        }
        return realmService;
    }

    /**
     * Gets AnalyticsData Service OSGI service instance.
     *
     * @return the analytics data service instance.
     * @throws AnalyticsException
     */
    public static AnalyticsDataService getAnalyticsDataService() throws AnalyticsException {
        AnalyticsDataService analyticsDataService;
        analyticsDataService = (AnalyticsDataService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
                .getOSGiService(AnalyticsDataService.class, null);
        if (analyticsDataService == null) {
            throw new AnalyticsException("AnalyticsDataService is not available.");
        }
        return analyticsDataService;
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

    @SuppressWarnings("unchecked")
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
     * @return the Map of recordBeans <id, recordBean>
     */
    public static Map<String, RecordBean> createRecordBeans(List<Record> records) {
        Map<String, RecordBean> recordBeans = new HashMap<>();
        for (Record record : records) {
            RecordBean recordBean = createRecordBean(record);
            recordBeans.put(recordBean.getId(), recordBean);
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
     * Create a Analytics schema from a bean class
     *
     * @param analyticsSchemaBean bean table schema to be converted to Analytics Schema.
     * @return Analytics schema
     */
    public static AnalyticsSchema createAnalyticsSchema(AnalyticsSchemaBean analyticsSchemaBean) {
        List<ColumnDefinition> columnDefinitions = new ArrayList<>();
        for (Map.Entry<String, ColumnDefinitionBean> entry : analyticsSchemaBean.getColumns().entrySet()) {
            columnDefinitions.add(getColumnType(entry.getKey(), entry.getValue()));
        }
        return new AnalyticsSchema(columnDefinitions, analyticsSchemaBean.getPrimaryKeys());
    }

    /**
     * Create table schema bean from a analytics schema
     *
     * @param analyticsSchema Analytics schema to be converted to table schema bean
     * @return Table schema bean
     */
    public static AnalyticsSchemaBean createTableSchemaBean(AnalyticsSchema analyticsSchema) {
        Map<String, ColumnDefinitionBean> columnDefinitions = new LinkedHashMap<>();
        List<String> primaryKeys = new ArrayList<>();
        if (analyticsSchema.getColumns() != null) {
            for (Map.Entry<String, ColumnDefinition> columns :
                    analyticsSchema.getColumns().entrySet()) {
                columnDefinitions.put(columns.getKey(), getColumnTypeBean(columns.getValue()));
            }
        }
        if (analyticsSchema.getPrimaryKeys() != null) {
            primaryKeys = analyticsSchema.getPrimaryKeys();
        }
        return new AnalyticsSchemaBean(columnDefinitions, primaryKeys);
    }

    /**
     * Creates the AnalyticsDrilldownRequest object given a drilldownrequestBean class
     *
     * @param bean bean class which represents the drilldown request.
     * @return Equivalent AnalyticsDrilldownRequest object.
     */
    public static AnalyticsDrillDownRequest createDrilldownRequest(DrillDownRequestBean bean)
            throws AnalyticsException {
        AnalyticsDrillDownRequest drillDownRequest = new AnalyticsDrillDownRequest();
        drillDownRequest.setTableName(bean.getTableName());
        drillDownRequest.setRecordCount(bean.getRecordCount());
        drillDownRequest.setRecordStartIndex(bean.getRecordStart());
        drillDownRequest.setQuery(bean.getQuery());
        drillDownRequest.setScoreFunction(bean.getScoreFunction());
        drillDownRequest.setCategoryPaths(createCategoryPaths(bean.getCategories()));
        drillDownRequest.setRanges(createDrillDownRanges(bean.getRanges()));
        drillDownRequest.setRangeField(bean.getRangeField());
        drillDownRequest.setSortByFields(Utils.getSortedFields(bean.getSortByFields()));
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
        drillDownRequest.setStart(bean.getStart());
        drillDownRequest.setCount(bean.getCount());
        return drillDownRequest;
    }

    public static SubCategoriesBean createSubCategoriesBean(SubCategories subCategories) {
        SubCategoriesBean bean = new SubCategoriesBean();
        bean.setCategoryPath(subCategories.getPath());
        Map<String, Double> categories = new LinkedHashMap<>();
        for (CategorySearchResultEntry entry : subCategories.getCategories()) {
            categories.put(entry.getCategoryValue(), entry.getScore());
        }
        bean.setCategories(categories);
        bean.setCategoryCount(subCategories.getCategoryCount());
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
                              new ArrayList<>(Arrays.asList(path)));
        }
        return categoryPaths;
    }

    /**
     * Converts a column type bean to ColumnType.
     *
     * @param name The name of the column
     * @param columnDefinitionBean ColumnType Bean to be converted to ColumnType
     * @return ColumnType instance
     */
    private static ColumnDefinition getColumnType(String name, ColumnDefinitionBean columnDefinitionBean) {
        ColumnDefinitionExt columnDefinition = new ColumnDefinitionExt();
        switch (columnDefinitionBean.getType()) {
            case STRING:
                columnDefinition.setType(AnalyticsSchema.ColumnType.STRING);
                break;
            case INTEGER:
                columnDefinition.setType(AnalyticsSchema.ColumnType.INTEGER);
                break;
            case LONG:
                columnDefinition.setType(AnalyticsSchema.ColumnType.LONG);
                break;
            case FLOAT:
                columnDefinition.setType(AnalyticsSchema.ColumnType.FLOAT);
                break;
            case DOUBLE:
                columnDefinition.setType(AnalyticsSchema.ColumnType.DOUBLE);
                break;
            case BOOLEAN:
                columnDefinition.setType(AnalyticsSchema.ColumnType.BOOLEAN);
                break;
            case BINARY:
                columnDefinition.setType(AnalyticsSchema.ColumnType.BINARY);
                break;
            default:
                columnDefinition.setType(AnalyticsSchema.ColumnType.STRING);
        }
        columnDefinition.setName(name);
        columnDefinition.setIndexed(columnDefinitionBean.isIndex());
        columnDefinition.setScoreParam(columnDefinitionBean.isScoreParam());
        //This is to be backward compatible with DAS 3.0.0/3.0.1
        if (columnDefinitionBean.getType() == ColumnTypeBean.FACET) {
            columnDefinition.setFacet(true);
        } else {
            columnDefinition.setFacet(columnDefinitionBean.isFacet());
        }
        return columnDefinition;
    }

    /**
     * convert a column type to bean type
     *
     * @param columnDefinition the ColumnType to be converted to bean type
     * @return ColumnTypeBean instance
     */
    private static ColumnDefinitionBean getColumnTypeBean(ColumnDefinition columnDefinition) {
        ColumnDefinitionBean bean = new ColumnDefinitionBean();
        switch (columnDefinition.getType()) {
            case STRING:
                bean.setType(ColumnTypeBean.STRING);
                break;
            case INTEGER:
                bean.setType(ColumnTypeBean.INTEGER);
                break;
            case LONG:
                bean.setType(ColumnTypeBean.LONG);
                break;
            case FLOAT:
                bean.setType(ColumnTypeBean.FLOAT);
                break;
            case DOUBLE:
                bean.setType(ColumnTypeBean.DOUBLE);
                break;
            case BOOLEAN:
                bean.setType(ColumnTypeBean.BOOLEAN);
                break;
            case BINARY:
                bean.setType(ColumnTypeBean.BINARY);
                break;
            default:
                bean.setType(ColumnTypeBean.STRING);
        }
        bean.setIndex(columnDefinition.isIndexed());
        bean.setScoreParam(columnDefinition.isScoreParam());
        bean.setFacet(columnDefinition.isFacet());
        return bean;
    }

    private static int getTenantId(String username) throws AnalyticsException {
        try {
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            return AnalyticsServiceHolder.getRealmService().getTenantManager().getTenantId(tenantDomain);
        } catch (UserStoreException e) {
            throw new AnalyticsException("Unable to get tenantId for user: " + username, e);
        }
    }

    public static AggregateRequest createAggregateRequest(
            AggregateRequestBean aggregateRequestBean) {
        AggregateRequest request = new AggregateRequest();
        request.setTableName(aggregateRequestBean.getTableName());
        request.setQuery(aggregateRequestBean.getQuery());
        request.setGroupByField(aggregateRequestBean.getGroupByField());
        request.setFields(createAggregatingFields(aggregateRequestBean.getFields()));
        request.setAggregateLevel(aggregateRequestBean.getAggregateLevel());
        request.setParentPath(aggregateRequestBean.getParentPath());
        request.setNoOfRecords(aggregateRequestBean.getNoOfRecords());
        return request;
    }

    /*public static AggregateRequest createAggregateRequest(
            AggregateRequestBean aggregateRequestBean, String tableName) {
        AggregateRequest request = new AggregateRequest();
        request.setTableName(tableName);
        request.setQuery(aggregateRequestBean.getQuery());
        request.setGroupByField(aggregateRequestBean.getGroupByField());
        request.setFields(createAggregatingFields(aggregateRequestBean.getFields()));
        request.setAggregateLevel(aggregateRequestBean.getAggregateLevel());
        request.setParentPath(aggregateRequestBean.getParentPath());
        request.setNoOfRecords(aggregateRequestBean.getNoOfRecords());
        return request;
    }
*/
    private static List<AggregateField> createAggregatingFields(List<AggregateFieldBean> fields) {
        List<AggregateField> aggregateFields = new ArrayList<>();
        for (AggregateFieldBean fieldBean : fields) {
            AggregateField aggregateField;
            // this is only to make backward compatible with older versions of aggregate apis
            if (fieldBean.getFieldName() != null && !fieldBean.getFieldName().isEmpty()) {
                aggregateField = new AggregateField(new String[]{fieldBean.getFieldName()},
                        fieldBean.getAggregate(), fieldBean.getAlias());
            } else {
                aggregateField = new AggregateField(fieldBean.getFields(),
                                                                   fieldBean.getAggregate(), fieldBean.getAlias());
            }
            aggregateFields.add(aggregateField);
        }
        return aggregateFields;
    }

    public static AggregateRequest[] createAggregateRequests(AggregateRequestBean[] aggregateRequestBeans) {
        List<AggregateRequest> requests = new ArrayList<>();
        if (aggregateRequestBeans != null) {
            for (AggregateRequestBean requestBean : aggregateRequestBeans) {
                requests.add(createAggregateRequest(requestBean));
            }
        }
        return requests.toArray(new AggregateRequest[requests.size()]);
    }

    public static List<SortByField> getSortedFields(List<SortByFieldBean> sortByFieldBeans)
            throws AnalyticsException {
        List<SortByField> sortByFields = new ArrayList<>();
        if (sortByFieldBeans != null) {
            for (SortByFieldBean sortByFieldBean : sortByFieldBeans) {
                SortByField sortByField = new SortByField(sortByFieldBean.getField(),
                                                          getSortType(sortByFieldBean.getField(), sortByFieldBean.getSortType()));
                sortByFields.add(sortByField);
            }
        }
        return sortByFields;
    }

    private static SortType getSortType(String field, String sortBy) throws AnalyticsException {
        SortType sortType;
        if (sortBy != null) {
            switch (sortBy) {
                case "ASC":
                    sortType = SortType.ASC;
                    break;
                case "DESC":
                    sortType = SortType.DESC;
                    break;
                default:
                    throw new AnalyticsException("Unknown SORT order: " + sortBy + " for field: " + field);
            }
        } else {
            throw new AnalyticsException("sortType cannot be null for field: " + field);
        }
        return sortType;
    }

    public static List<RecordBean> getSortedRecordBeans(Map<String, RecordBean> recordBeans,
                                                        List<SearchResultEntry> searchResults) {
        List<RecordBean> sortedRecords = new ArrayList<>();
        for (SearchResultEntry searchResultEntry : searchResults) {
            sortedRecords.add(recordBeans.get(searchResultEntry.getId()));
        }
        return sortedRecords;
    }
}
