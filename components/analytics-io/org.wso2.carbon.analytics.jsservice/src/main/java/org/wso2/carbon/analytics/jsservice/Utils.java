/*
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

package org.wso2.carbon.analytics.jsservice;

import org.apache.axiom.om.util.Base64;
import org.apache.commons.collections.IteratorUtils;
import org.wso2.carbon.analytics.dataservice.commons.AggregateRequest;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRange;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.CategoryDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.CategorySearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SortType;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
import org.wso2.carbon.analytics.dataservice.commons.SubCategories;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinition;
import org.wso2.carbon.analytics.datasource.commons.ColumnDefinitionExt;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.jsservice.beans.AggregateField;
import org.wso2.carbon.analytics.jsservice.beans.AggregateRequestBean;
import org.wso2.carbon.analytics.jsservice.beans.AnalyticsSchemaBean;
import org.wso2.carbon.analytics.jsservice.beans.CategoryDrillDownRequestBean;
import org.wso2.carbon.analytics.jsservice.beans.ColumnDefinitionBean;
import org.wso2.carbon.analytics.jsservice.beans.ColumnTypeBean;
import org.wso2.carbon.analytics.jsservice.beans.DrillDownPathBean;
import org.wso2.carbon.analytics.jsservice.beans.DrillDownRangeBean;
import org.wso2.carbon.analytics.jsservice.beans.DrillDownRequestBean;
import org.wso2.carbon.analytics.jsservice.beans.EventBean;
import org.wso2.carbon.analytics.jsservice.beans.RecordBean;
import org.wso2.carbon.analytics.jsservice.beans.SortByFieldBean;
import org.wso2.carbon.analytics.jsservice.beans.StreamDefinitionBean;
import org.wso2.carbon.analytics.jsservice.beans.SubCategoriesBean;
import org.wso2.carbon.analytics.jsservice.exception.JSServiceException;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.databridge.commons.Attribute;
import org.wso2.carbon.databridge.commons.AttributeType;
import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.databridge.commons.exception.MalformedStreamDefinitionException;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.math.BigDecimal;
import java.nio.charset.Charset;
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

    private static final String DEFAULT_CHARSET = "UTF-8";

    public static List<RecordBean> getRecordBeans(List<Record> records) {
        List<RecordBean> recordBeans = new ArrayList<>();
        if (records != null) {
            for (Record record : records) {
                RecordBean recordBean = getRecordBean(record);
                recordBeans.add(recordBean);
            }
        }
        return recordBeans;
    }

    private static RecordBean getRecordBean(Record record) {
        RecordBean recordBean = new RecordBean();
        recordBean.setId(record.getId());
        recordBean.setTableName(record.getTableName());
        recordBean.setTimestamp(record.getTimestamp());
        recordBean.setValues(record.getValues());
        return recordBean;
    }

    public static List<String> getIds(List<SearchResultEntry> searchResults) {
        List<String> ids = new ArrayList<>();
        if (searchResults != null) {
            for (SearchResultEntry resultEntry : searchResults) {
                ids.add(resultEntry.getId());
            }
        }
        return ids;
    }

    /**
     * Create a Analytics schema from a bean class
     *
     * @param schemaBean bean table schema to be converted to Analytics Schema.
     * @return Analytics schema
     */

    public static AnalyticsSchema getAnalyticsSchema(AnalyticsSchemaBean schemaBean) {
        Map<String, ColumnDefinitionBean> columnBeans = schemaBean.getColumns();
        List<String> primaryKeys = schemaBean.getPrimaryKeys();
        List<ColumnDefinition> columnDefinitions = new ArrayList<>();
        if (columnBeans != null) {
            for (Map.Entry<String, ColumnDefinitionBean> columnBean : columnBeans.entrySet()) {
                ColumnDefinitionExt columnDefinition = new ColumnDefinitionExt(columnBean.getKey(),
                                                                         getColumnType(columnBean.getValue().getType()),
                                                                         columnBean.getValue().isIndex(),
                                                                         columnBean.getValue().isScoreParam(),
                                                                         columnBean.getValue().isFacet());
                //To make backward compatible with DAS 3.0.0 and DAS 3.0.1, see DAS-402
                if (columnBean.getValue().getType() == ColumnTypeBean.FACET) {
                    columnDefinition.setFacet(true);
                }
                columnDefinitions.add(columnDefinition);
            }
        }
        return new AnalyticsSchema(columnDefinitions, primaryKeys);
    }

    private static AnalyticsSchema.ColumnType getColumnType(ColumnTypeBean columnType) {
        switch (columnType) {
            case STRING:
                return AnalyticsSchema.ColumnType.STRING;
            case INTEGER:
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
     * Create table schema bean from a analytics schema
     *
     * @param analyticsSchema Analytics schema to be converted to table schema bean
     * @return Table schema bean
     */
    public static AnalyticsSchemaBean createTableSchemaBean(AnalyticsSchema analyticsSchema) {
        Map<String, ColumnDefinitionBean> columnDefinitions = new HashMap<>();
        List<String> primaryKeys = new ArrayList<>();
        if (analyticsSchema.getColumns() != null) {
            for (Map.Entry<String, ColumnDefinition> columnBean : analyticsSchema.getColumns().entrySet()) {
                columnDefinitions.put(columnBean.getKey(),
                                      getColumnTypeBean(columnBean.getValue().getType(),
                                                        columnBean.getValue().isIndexed(),
                                                        columnBean.getValue().isScoreParam(),
                                                        columnBean.getValue().isFacet()));
            }
        }
        if (analyticsSchema.getPrimaryKeys() != null) {
            primaryKeys = analyticsSchema.getPrimaryKeys();
        }
        return new AnalyticsSchemaBean(columnDefinitions, primaryKeys);
    }

    /**
     * convert a column type to bean type
     *
     * @param columnType the ColumnType to be converted to bean type
     * @return ColumnTypeBean instance
     */
    private static ColumnDefinitionBean getColumnTypeBean(AnalyticsSchema.ColumnType columnType, boolean isIndexed,
                                                          boolean isScoreParam, boolean isFacet) {
        ColumnDefinitionBean bean = new ColumnDefinitionBean();
        switch (columnType) {
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

        bean.setIndex(isIndexed);
        bean.setScoreParam(isScoreParam);
        bean.setFacet(isFacet);
        return bean;
    }

    public static CategoryDrillDownRequest createCategoryDrillDownRequest(
            String tableName, CategoryDrillDownRequestBean queryBean) {
        CategoryDrillDownRequest request = new CategoryDrillDownRequest();
        request.setTableName(tableName);
        request.setScoreFunction(queryBean.getScoreFunction());
        List<String> drillDownPath = queryBean.getCategoryPath();
        if (drillDownPath != null) {
            request.setPath(drillDownPath.toArray(new String[drillDownPath.size()]));
        }
        request.setFieldName(queryBean.getFieldName());
        request.setQuery(queryBean.getQuery());
        request.setStart(queryBean.getStart());
        request.setCount(queryBean.getCount());
        return request;
    }

    public static AnalyticsDrillDownRequest createDrillDownSearchRequest(
            String tableName, DrillDownRequestBean queryBean) throws AnalyticsException {
        AnalyticsDrillDownRequest request = new AnalyticsDrillDownRequest();
        request.setTableName(tableName);
        request.setQuery(queryBean.getQuery());
        request.setScoreFunction(queryBean.getScoreFunction());
        request.setRecordCount(queryBean.getRecordCount());
        request.setRecordStartIndex(queryBean.getRecordStart());
        request.setRangeField(queryBean.getRangeField());
        request.setCategoryPaths(createCategoryPathBeans(queryBean.getCategories()));
        request.setRanges(createRanges(queryBean.getRanges()));
        request.setSortByFields(getSortedFields(queryBean.getSortBy()));
        return request;
    }

    private static List<AnalyticsDrillDownRange> createRanges(
            List<DrillDownRangeBean> ranges) {
        List<AnalyticsDrillDownRange> beans = new ArrayList<>();
        if (ranges != null) {
            for (DrillDownRangeBean range : ranges) {
                AnalyticsDrillDownRange bean = new AnalyticsDrillDownRange();
                bean.setFrom(range.getFrom());
                bean.setTo(range.getTo());
                bean.setLabel(range.getLabel());
                beans.add(bean);
            }
        }
        return beans;
    }

    private static Map<String, List<String>> createCategoryPathBeans(List<DrillDownPathBean> categories) {
        Map<String, List<String>> beans = new HashMap<>();
        if (categories != null) {
            for (DrillDownPathBean category : categories) {
                List<String> path = new ArrayList<>(Arrays.asList(category.getPath()));
                beans.put(category.getFieldName(), path);
            }
        }
        return beans;
    }

    public static SubCategoriesBean getSubCategories(
            SubCategories searchResults) {
        SubCategoriesBean bean =
                new SubCategoriesBean();
        bean.setCategories(getCategories(searchResults.getCategories()));
        bean.setCategoryPath(searchResults.getPath());
        bean.setCategoryCount(searchResults.getCategoryCount());
        return bean;
    }

    private static Map<String, Double> getCategories(List<CategorySearchResultEntry> categories) {
        Map<String, Double> subCategories = new LinkedHashMap<>();
        if (categories != null) {
            for (CategorySearchResultEntry bean : categories) {
                subCategories.put(bean.getCategoryValue(), bean.getScore());
            }
        }
        return subCategories;
    }

    /**
     * Authenticate an user given the auth header (basic auth ) Note : This method is used in jaggery page, Do not remove.
     * @param authHeader
     * @return
     * @throws UnauthenticatedUserException
     */
    public static String[] authenticate(String authHeader) throws UnauthenticatedUserException{
        String credentials[];
        if (authHeader != null && authHeader.startsWith(Constants.BASIC_AUTH_HEADER)) {
            // Authorization: Basic base64credentials
            String base64Credentials = authHeader.substring(Constants.BASIC_AUTH_HEADER.length()).trim();
            String tempCredentials = new String(Base64.decode(base64Credentials),
                                                Charset.forName(DEFAULT_CHARSET));
            // credentials = username:password
            final String[] values = tempCredentials.split(":", 2);
            String username = values[0];
            String password = values[1];
            credentials = values;
            if (username == null || password == null || "".equals(username) || "".equals(password)) { //first check null then empty
                throw new UnauthenticatedUserException("Username and password cannot be empty");
            }
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            String tenantLessUserName = MultitenantUtils.getTenantAwareUsername(username);
            try {
                // get super tenant context and get realm service which is an osgi service
                RealmService realmService = (RealmService) PrivilegedCarbonContext
                        .getThreadLocalCarbonContext().getOSGiService(RealmService.class, null);
                if (realmService != null) {
                    int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
                    if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
                        throw new UnauthenticatedUserException("Authentication failed - Invalid domain");
                    }
                    // get tenant's user realm
                    UserRealm userRealm = realmService.getTenantUserRealm(tenantId);
                    boolean isAuthenticated = userRealm.getUserStoreManager()
                            .authenticate(tenantLessUserName, password);
                    if (!isAuthenticated) {
                        throw  new UnauthenticatedUserException("User is not authenticated!");
                    } else {
                        return credentials;
                    }
                }
            } catch (UserStoreException e) {
                throw new UnauthenticatedUserException("Error while accessing the user realm of user :"
                                             + username, e);
            }
        } else {
            throw new UnauthenticatedUserException("Invalid authentication header");
        }
        return credentials;
    }

    public static StreamDefinition getStreamDefinition(
            StreamDefinitionBean streamDefinitionBean)
            throws MalformedStreamDefinitionException, JSServiceException {
        StreamDefinition streamDef =
                new StreamDefinition(streamDefinitionBean.getName(), streamDefinitionBean.getVersion());
        streamDef.setDescription(streamDefinitionBean.getDescription());
        streamDef.setNickName(streamDefinitionBean.getNickName());
        List<String> tags = streamDefinitionBean.getTags();
        Map<String, String> metaData = streamDefinitionBean.getMetaData();
        Map<String, String> correlationData = streamDefinitionBean.getCorrelationData();
        Map<String, String> payloadData = streamDefinitionBean.getPayloadData();
        if (tags != null) streamDef.setTags(tags);
        if (metaData != null) streamDef.setMetaData(getStreamDefAttributes(metaData));
        if (correlationData != null) streamDef.setCorrelationData(getStreamDefAttributes(correlationData));
        if (payloadData != null) streamDef.setPayloadData(getStreamDefAttributes(payloadData));
        return streamDef;
    }

    private static List<Attribute> getStreamDefAttributes(Map<String, String> metaData)
            throws JSServiceException {
        List<Attribute> attributeBeans = new ArrayList<>();
        for (Map.Entry<String, String> entry : metaData.entrySet()) {
            Attribute bean = new Attribute(entry.getKey(), getAttributeType(entry.getValue()));
            attributeBeans.add(bean);
        }
        return attributeBeans;
    }

    private static AttributeType getAttributeType(String type) throws JSServiceException {
        if (type != null) {
            switch (type) {
                case Constants.STRING_TYPE: {
                    return AttributeType.STRING;
                }
                case Constants.BOOLEAN_TYPE: {
                    return AttributeType.BOOL;
                }
                case Constants.FLOAT_TYPE: {
                    return AttributeType.FLOAT;
                }
                case Constants.DOUBLE_TYPE: {
                    return AttributeType.DOUBLE;
                }
                case Constants.INTEGER_TYPE: {
                    return AttributeType.INT;
                }
                case Constants.LONG_TYPE: {
                    return AttributeType.LONG;
                }
                default: {
                    throw new JSServiceException("Unkown type found while reading stream definition bean.");
                }
            }
        } else {
            throw new JSServiceException("Type is not defined.");
        }
    }

    public static Event getStreamEvent(StreamDefinition streamDefinition, EventBean eventBean)
            throws JSServiceException {
        Event event =
                new Event();
        event.setTimeStamp(eventBean.getTimeStamp());
        event.setStreamId(streamDefinition.getStreamId());
        Map<String, Object> metaData = eventBean.getMetaData();
        Map<String, Object> correlationData = eventBean.getCorrelationData();
        Map<String, Object> payloadData = eventBean.getPayloadData();
        Map<String, Object> arbitraryValues = eventBean.getArbitraryDataMap();
        if (metaData != null) event.setMetaData(getEventData(metaData, streamDefinition.getMetaData()));
        if (correlationData != null) event.setCorrelationData(getEventData(correlationData, streamDefinition.getCorrelationData()));
        if (payloadData != null) event.setPayloadData(getEventData(payloadData, streamDefinition.getPayloadData()));
        if (arbitraryValues != null) event.setArbitraryDataMap(getArbitraryValues(arbitraryValues));
        return event;
    }

    private static Map<String, String> getArbitraryValues(Map<String, Object> arbitraryValues) {
        Map<String, String> values = new HashMap<>();
        for (Map.Entry<String, Object> entry : arbitraryValues.entrySet()) {
            values.put(entry.getKey(), entry.getValue().toString());
        }
        return values;
    }

    private static Object[] getEventData(Map<String, Object> entries, List<Attribute> columns)
            throws JSServiceException {
        List<Object> values = new ArrayList<>();
        if (entries.keySet() != null) {
            for (Attribute column : columns) {
                if (column != null) {
                    AttributeType type = column.getType();
                    String columnName = column.getName();
                    Object value = entries.get(columnName);
                    if (value != null) {
                        switch (type) {
                            case DOUBLE:
                                values.add(new BigDecimal(value.toString()).doubleValue());
                                break;
                            case INT:
                                values.add(new BigDecimal(value.toString()).intValue());
                                break;
                            case BOOL:
                                values.add(Boolean.parseBoolean(value.toString()));
                                break;
                            case LONG:
                                values.add(new BigDecimal(value.toString()).longValue());
                                break;
                            case FLOAT:
                                values.add(new BigDecimal(value.toString()).floatValue());
                                break;
                            case STRING:
                                values.add(value.toString());
                                break;
                            default:
                                throw new JSServiceException("DataType is not valid for [" +
                                                                       columnName);
                        }
                    } else {
                        throw new JSServiceException("value is not given for field: " +
                                                               columnName);
                    }
                } else {
                    throw new JSServiceException("Record Values are null");
                }
            }
        }
        return values.toArray(new Object[values.size()]);
    }
    public static StreamDefinitionBean getStreamDefinitionBean(StreamDefinition streamDefinition) {
        StreamDefinitionBean bean = new StreamDefinitionBean();
        bean.setName(streamDefinition.getName());
        bean.setVersion(streamDefinition.getVersion());
        bean.setDescription(streamDefinition.getDescription());
        bean.setNickName(streamDefinition.getNickName());
        List<String> tags = streamDefinition.getTags();
        List<Attribute> metaData = streamDefinition.getMetaData();
        List<Attribute> correlationData = streamDefinition.getCorrelationData();
        List<Attribute> payloadData = streamDefinition.getPayloadData();
        if (tags != null) bean.setTags(tags);
        if (metaData != null) bean.setMetaData(getStreamAttributesAsMap(metaData));
        if (correlationData != null) bean.setCorrelationData(getStreamAttributesAsMap(correlationData));
        if (payloadData != null) bean.setPayloadData(getStreamAttributesAsMap(payloadData));
        return bean;
    }

    private static Map<String, String> getStreamAttributesAsMap(List<Attribute> attributes) {
        Map<String, String> attributeMap= new LinkedHashMap<>();
        for (Attribute attribute : attributes) {
            if (attribute != null) {
                attributeMap.put(attribute.getName(), gerAttribute(attribute.getType()));
            }
        }
        return attributeMap;
    }

    private static String gerAttribute(AttributeType type) {
        switch (type) {
            case DOUBLE:
                return Constants.DOUBLE_TYPE;
            case STRING:
                return Constants.STRING_TYPE;
            case INT:
                return Constants.INTEGER_TYPE;
            case FLOAT:
                return Constants.FLOAT_TYPE;
            case LONG:
                return Constants.LONG_TYPE;
            case BOOL:
                return Constants.BOOLEAN_TYPE;
            default:
                return Constants.STRING_TYPE;
        }
    }

    public static AggregateRequest getAggregateRequest(AggregateRequestBean aggregateRequest) {
        AggregateRequest request = new AggregateRequest();
        request.setGroupByField(aggregateRequest.getGroupByField());
        request.setQuery(aggregateRequest.getQuery());
        request.setTableName(aggregateRequest.getTableName());
        request.setFields(createAggregateFieds(aggregateRequest.getAggregateFields()));
        request.setAggregateLevel(aggregateRequest.getAggregateLevel());
        request.setParentPath(aggregateRequest.getParentPath());
        request.setNoOfRecords(aggregateRequest.getNoOfRecords());
        return request;
    }

    public static AggregateRequest getAggregateRequest(AggregateRequestBean aggregateRequest, String tableName) {
        AggregateRequest request = new AggregateRequest();
        request.setGroupByField(aggregateRequest.getGroupByField());
        request.setQuery(aggregateRequest.getQuery());
        if (tableName != null && !tableName.isEmpty()) {
            request.setTableName(tableName);
        } else {
            request.setTableName(aggregateRequest.getTableName());
        }
        request.setFields(createAggregateFieds(aggregateRequest.getAggregateFields()));
        request.setAggregateLevel(aggregateRequest.getAggregateLevel());
        request.setParentPath(aggregateRequest.getParentPath());
        request.setNoOfRecords(aggregateRequest.getNoOfRecords());
        return request;
    }

    private static List<org.wso2.carbon.analytics.dataservice.commons.AggregateField> createAggregateFieds(List<AggregateField> fields) {
        List<org.wso2.carbon.analytics.dataservice.commons.AggregateField> analyticsAggregateFields = new ArrayList<>();
        for (AggregateField field : fields) {
            org.wso2.carbon.analytics.dataservice.commons.AggregateField aggregateField = new org.wso2.carbon.analytics.dataservice.commons.AggregateField();
            // this is only to make backward compatible with older versions of aggregate apis
            if (field.getFieldName() != null && !field.getFieldName().isEmpty()) {
                aggregateField.setAggregateVariables(new String[]{field.getFieldName()});
            } else {
                aggregateField.setAggregateVariables(field.getFields());
            }
            aggregateField.setAggregateFunction(field.getAggregate());
            aggregateField.setAlias(field.getAlias());
            analyticsAggregateFields.add(aggregateField);
        }
        return analyticsAggregateFields;
    }

    @SuppressWarnings("unchecked")
    public static List<Record> createList(AnalyticsIterator<Record> iterator) {
        List<Record> records = new ArrayList<>();
        records.addAll(IteratorUtils.toList(iterator));
        return records;
    }

    public static AggregateRequest[] getAggregateRequests(
            AggregateRequestBean[] aggregateRequests) {
        List<AggregateRequest> requests = new ArrayList<>();
        if (aggregateRequests != null) {
            for (AggregateRequestBean requestBean : aggregateRequests) {
                requests.add(getAggregateRequest(requestBean));
            }
        }
        return requests.toArray(new AggregateRequest[requests.size()]);
    }

    public static List<List<RecordBean>> getAggregatedRecordsForMultipleTables(
            List<AnalyticsIterator<Record>> iterators) {
        List<List<RecordBean>> aggregatedRecords = new ArrayList<>();
        for (AnalyticsIterator<Record> iterator : iterators) {
            aggregatedRecords.add(getRecordBeans(createList(iterator)));
        }
        return aggregatedRecords;
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

    private static SortType getSortType(String fieldName, String sortBy) throws AnalyticsException {
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
                    throw new AnalyticsException("Unknown SORT order: " + sortBy + "for field: " + fieldName);
            }
        } else {
            throw new AnalyticsException("sortType cannot be null for field: " + fieldName);
        }
        return sortType;
    }

    public static Map<String, RecordBean> getRecordBeanKeyedWithIds(List<Record> records) {
        Map<String, RecordBean> recordBeanMap = new HashMap<>();
        for (Record record : records) {
            RecordBean recordBean = Utils.getRecordBean(record);
            recordBeanMap.put(recordBean.getId(), recordBean);
        }
        return recordBeanMap;
    }

    public static List<RecordBean> getSortedRecordBeans(Map<String, RecordBean> recordBeanMap,
                                                        List<SearchResultEntry> searchResults) {
        List<RecordBean> sortedRecords = new ArrayList<>();
        for (SearchResultEntry entry : searchResults) {
            sortedRecords.add(recordBeanMap.get(entry.getId()));
        }
        return sortedRecords;
    }

    public static boolean startTenantFlow(int tenantId) {
        int currentTenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
        if (currentTenantId == org.wso2.carbon.utils.multitenancy.MultitenantConstants.INVALID_TENANT_ID ||
            (currentTenantId == org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_ID && currentTenantId != tenantId)) {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext.getThreadLocalCarbonContext().setTenantId(tenantId, true);
            return true;
        }
        return false;
    }

    public static List<DrillDownRangeBean> getDrilldownRangeBean(
            List<AnalyticsDrillDownRange> searchResults) {
        List<DrillDownRangeBean> rangeBeans = new ArrayList<>();
        if (searchResults != null) {
            for (AnalyticsDrillDownRange range : searchResults) {
                rangeBeans.add(createDrilldownRangeBean(range));
            }
        }
        return rangeBeans;
    }

    private static DrillDownRangeBean createDrilldownRangeBean(AnalyticsDrillDownRange range) {
        DrillDownRangeBean bean = new DrillDownRangeBean(range.getLabel(), range.getFrom(), range.getTo(), range.getScore());
        return  bean;
    }
}
