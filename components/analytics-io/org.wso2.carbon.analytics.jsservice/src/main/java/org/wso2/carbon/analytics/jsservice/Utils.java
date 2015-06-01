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
import org.wso2.carbon.analytics.jsservice.beans.AnalyticsSchemaBean;
import org.wso2.carbon.analytics.jsservice.beans.CategoryDrillDownRequestBean;
import org.wso2.carbon.analytics.jsservice.beans.ColumnDefinitionBean;
import org.wso2.carbon.analytics.jsservice.beans.ColumnTypeBean;
import org.wso2.carbon.analytics.jsservice.beans.DrillDownPathBean;
import org.wso2.carbon.analytics.jsservice.beans.DrillDownRangeBean;
import org.wso2.carbon.analytics.jsservice.beans.DrillDownRequestBean;
import org.wso2.carbon.analytics.jsservice.beans.EventBean;
import org.wso2.carbon.analytics.jsservice.beans.Record;
import org.wso2.carbon.analytics.jsservice.beans.StreamDefinitionBean;
import org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsCategoryPathBean;
import org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsDrillDownRangeBean;
import org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsDrillDownRequestBean;
import org.wso2.carbon.analytics.webservice.stub.beans.CategoryPathBean;
import org.wso2.carbon.analytics.webservice.stub.beans.CategorySearchResultEntryBean;
import org.wso2.carbon.analytics.webservice.stub.beans.RecordBean;
import org.wso2.carbon.analytics.webservice.stub.beans.RecordValueEntryBean;
import org.wso2.carbon.analytics.webservice.stub.beans.SchemaColumnBean;
import org.wso2.carbon.analytics.webservice.stub.beans.StreamDefAttributeBean;
import org.wso2.carbon.analytics.webservice.stub.beans.SubCategoriesBean;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

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

    public static List<Record> getRecordBeans(RecordBean[] recordBeans) {
        List<Record> records = new ArrayList<>();
        if (recordBeans != null) {
            for (RecordBean recordBean : recordBeans) {
                Record record = new Record();
                record.setId(recordBean.getId());
                record.setTableName(recordBean.getTableName());
                record.setValues(validateAndReturn(recordBean.getValues()));
                records.add(record);
            }
        }
        return records;
    }

    public static List<String> getIds(RecordBean[] beans) {
        List<String> ids = new ArrayList<>();
        if (beans != null) {
            for (RecordBean bean : beans) {
                ids.add(bean.getId());
            }
        }
        return ids;
    }

    private static Map<String, Object> validateAndReturn(RecordValueEntryBean[] values) {
        Map<String, Object> columnValues = new LinkedHashMap<>();
        for (RecordValueEntryBean valueEntryBean : values) {
            switch (valueEntryBean.getType()) {
                case "INTEGER":
                    columnValues.put(valueEntryBean.getFieldName(), valueEntryBean.getIntValue());
                    break;
                case "DOUBLE":
                    columnValues.put(valueEntryBean.getFieldName(), valueEntryBean.getDoubleValue());
                    break;
                case "LONG":
                    columnValues.put(valueEntryBean.getFieldName(), valueEntryBean.getLongValue());
                    break;
                case "FLOAT":
                    columnValues.put(valueEntryBean.getFieldName(), valueEntryBean.getFloatValue());
                    break;
                case "STRING":
                    columnValues.put(valueEntryBean.getFieldName(), valueEntryBean.getStringValue());
                    break;
                case "BOOLEAN":
                    columnValues.put(valueEntryBean.getFieldName(), valueEntryBean.getBooleanValue());
                    break;
                case "FACET":
                    AnalyticsCategoryPathBean categoryPathBean = valueEntryBean.getAnalyticsCategoryPathBeanValue();
                    List<String> facet = new ArrayList<>();
                    facet.addAll(Arrays.asList(categoryPathBean.getPath()));
                    columnValues.put(valueEntryBean.getFieldName(), facet);
                    break;
                default:
                    columnValues.put(valueEntryBean.getFieldName(), valueEntryBean.getStringValue());
            }
        }
        return columnValues;
    }

    public static RecordBean[] getRecords(List<Record> recordBeans) {
        List<RecordBean> records = new ArrayList<>();
        for (Record recordBean : recordBeans) {
            RecordBean record = new RecordBean();
            record.setId(recordBean.getId());
            record.setTableName(recordBean.getTableName());
            record.setValues(getRecordValueEntryBeans(recordBean.getValues()));
            records.add(record);
        }
        return records.toArray(new  RecordBean[records.size()]);
    }
    public static RecordBean[] getRecords(String tableName, List<Record> recordBeans) {
        List<RecordBean> records = new ArrayList<>();
        for (Record recordBean : recordBeans) {
            RecordBean record = new RecordBean();
            record.setId(recordBean.getId());
            record.setTableName(tableName);
            record.setValues(getRecordValueEntryBeans(recordBean.getValues()));
            records.add(record);
        }
        return records.toArray(new  RecordBean[records.size()]);
    }

    private static AnalyticsCategoryPathBean validateAndReturn(Object value) {
        List<String> pathList = (List<String>) value;
        AnalyticsCategoryPathBean categoryPathBean = new AnalyticsCategoryPathBean();
        if (pathList != null && pathList.size() > 0) {
            String[] path = pathList.toArray(new String[pathList.size()]);
            categoryPathBean.setPath(path);
        }
        return categoryPathBean;
    }

    private static RecordValueEntryBean[] getRecordValueEntryBeans(Map<String, Object> values) {
        List<RecordValueEntryBean> recordValueEntryBeans = new ArrayList<>();
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            RecordValueEntryBean recordValueEntryBean = new RecordValueEntryBean();
            recordValueEntryBean.setFieldName(entry.getKey());
            Object value = entry.getValue();
            recordValueEntryBean.setType("STRING");
            if (value != null) {
                recordValueEntryBean.setStringValue(value.toString());
            }
            recordValueEntryBeans.add(recordValueEntryBean);
        }
        return recordValueEntryBeans.toArray(new RecordValueEntryBean[recordValueEntryBeans.size()]);
    }

    /**
     * Create a Analytics schema from a bean class
     *
     * @param analyticsSchemaBean bean table schema to be converted to Analytics Schema.
     * @return Analytics schema
     */
    public static org.wso2.carbon.analytics.
            webservice.stub.beans.AnalyticsSchemaBean createAnalyticsSchema(AnalyticsSchemaBean analyticsSchemaBean) {
        org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsSchemaBean
                schemaBean = new org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsSchemaBean();
        schemaBean.setColumns(createSchemaColumns(analyticsSchemaBean.getColumns()));
        schemaBean.setPrimaryKeys(createPrimaryKeys(analyticsSchemaBean.getPrimaryKeys()));
        return schemaBean;
    }

    private static String[] createPrimaryKeys(List<String> primaryKeys) {
        return primaryKeys.toArray(new String[primaryKeys.size()]);
    }

    private static SchemaColumnBean[] createSchemaColumns(Map<String, ColumnDefinitionBean> columns) {
        List<SchemaColumnBean> columnBeans = new ArrayList<>();
        for (Map.Entry<String, ColumnDefinitionBean> entry : columns.entrySet()) {
            SchemaColumnBean bean = new SchemaColumnBean();
            bean.setColumnName(entry.getKey());
            bean.setColumnType(entry.getValue().getType().toString());
            bean.setIndex(entry.getValue().isIndex());
            bean.setScoreParam(entry.getValue().isScoreParam());
            columnBeans.add(bean);
        }
        return columnBeans.toArray(new SchemaColumnBean[columnBeans.size()]);
    }

    /**
     * Create table schema bean from a analytics schema
     *
     * @param analyticsSchema Analytics schema to be converted to table schema bean
     * @return Table schema bean
     */
    public static AnalyticsSchemaBean createTableSchemaBean(
            org.wso2.carbon.analytics.webservice.stub.beans.AnalyticsSchemaBean analyticsSchema) {
        Map<String, ColumnDefinitionBean> columnDefinitions = new HashMap<>();
        List<String> primaryKeys = new ArrayList<>();
        if (analyticsSchema.getColumns() != null) {
            for (SchemaColumnBean columnBean :
                    analyticsSchema.getColumns()) {
                columnDefinitions.put(columnBean.getColumnName(),
                                      getColumnTypeBean(columnBean.getColumnType(),
                                                        columnBean.getIndex(),
                                                        columnBean.getScoreParam()));
            }
        }
        if (analyticsSchema.getPrimaryKeys() != null) {
            primaryKeys = Arrays.asList(analyticsSchema.getPrimaryKeys());
        }
        return new AnalyticsSchemaBean(columnDefinitions, primaryKeys);
    }

    /**
     * convert a column type to bean type
     *
     * @param columnType the ColumnType to be converted to bean type
     * @return ColumnTypeBean instance
     */
    private static ColumnDefinitionBean getColumnTypeBean(String columnType, boolean isIndexed, boolean isScoreParam) {
        ColumnDefinitionBean bean = new ColumnDefinitionBean();
        switch (columnType) {
            case "STRING":
                bean.setType(ColumnTypeBean.STRING);
                break;
            case "INTEGER":
                bean.setType(ColumnTypeBean.INTEGER);
                break;
            case "LONG":
                bean.setType(ColumnTypeBean.LONG);
                break;
            case "FLOAT":
                bean.setType(ColumnTypeBean.FLOAT);
                break;
            case "DOUBLE":
                bean.setType(ColumnTypeBean.DOUBLE);
                break;
            case "BOOLEAN":
                bean.setType(ColumnTypeBean.BOOLEAN);
                break;
            case "BINARY":
                bean.setType(ColumnTypeBean.BINARY);
                break;
            case "FACET":
                bean.setType(ColumnTypeBean.FACET);
                break;
            default:
                bean.setType(ColumnTypeBean.STRING);
        }

        bean.setIndex(isIndexed);
        bean.setScoreParam(isScoreParam);
        return bean;
    }

    public static org.wso2.carbon.analytics.webservice.stub.beans.CategoryDrillDownRequestBean createCategoryDrillDownRequest(
            String tableName, CategoryDrillDownRequestBean queryBean) {
        org.wso2.carbon.analytics.webservice.stub.beans.CategoryDrillDownRequestBean bean = new
                org.wso2.carbon.analytics.webservice.stub.beans.CategoryDrillDownRequestBean();
        bean.setTableName(tableName);
        bean.setScoreFunction(queryBean.getScoreFunction());
        List<String> drillDownPath = queryBean.getCategoryPath();
        if (drillDownPath != null) {
            bean.setPath(drillDownPath.toArray(new String[drillDownPath.size()]));
        }
        bean.setFieldName(queryBean.getFieldName());
        bean.setQuery(queryBean.getQuery());
        return bean;
    }

    public static AnalyticsDrillDownRequestBean createDrillDownSearchRequest(
            String tableName, DrillDownRequestBean queryBean) {
        AnalyticsDrillDownRequestBean bean = new AnalyticsDrillDownRequestBean();
        bean.setTableName(tableName);
        bean.setQuery(queryBean.getQuery());
        bean.setScoreFunction(queryBean.getScoreFunction());
        bean.setRecordCount(queryBean.getRecordCount());
        bean.setRecordStart(queryBean.getRecordStart());
        bean.setRangeField(queryBean.getRangeField());
        bean.setCategoryPaths(createCategoryPathBeans(queryBean.getCategories()));
        bean.setRanges(createRanges(queryBean.getRanges()));
        return bean;
    }

    private static AnalyticsDrillDownRangeBean[] createRanges(
            List<DrillDownRangeBean> ranges) {
        List<AnalyticsDrillDownRangeBean> beans = new ArrayList<>();
        if (ranges != null) {
            for (DrillDownRangeBean range : ranges) {
                AnalyticsDrillDownRangeBean bean = new AnalyticsDrillDownRangeBean();
                bean.setFrom(range.getFrom());
                bean.setTo(range.getTo());
                bean.setLabel(range.getLabel());
                beans.add(bean);
            }
        }
        return beans.toArray(new AnalyticsDrillDownRangeBean[beans.size()]);
    }

    private static CategoryPathBean[] createCategoryPathBeans(List<DrillDownPathBean> categories) {
        List<CategoryPathBean> beans = new ArrayList<>();
        for (DrillDownPathBean category : categories) {
            CategoryPathBean bean = new CategoryPathBean();
            bean.setFieldName(category.getFieldName());
            bean.setPath(category.getPath());
            beans.add(bean);
        }
        return beans.toArray(new CategoryPathBean[beans.size()]);
    }

    public static org.wso2.carbon.analytics.jsservice.beans.SubCategoriesBean getSubCategories(
            SubCategoriesBean searchResults) {
        org.wso2.carbon.analytics.jsservice.beans.SubCategoriesBean bean =
                new org.wso2.carbon.analytics.jsservice.beans.SubCategoriesBean();
        bean.setCategories(getCategories(searchResults.getCategories()));
        bean.setCategoryPath(searchResults.getPath());
        return bean;
    }

    private static Map<String, Double> getCategories(CategorySearchResultEntryBean[] categories) {
        Map<String, Double> subCategories = new LinkedHashMap<>();
        if (categories != null) {
            for (CategorySearchResultEntryBean bean : categories) {
                subCategories.put(bean.getCategoryName(), bean.getScore());
            }
        }
        return subCategories;
    }

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
            if ("".equals(username) || username == null || "".equals(password) || password == null) {
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

    public static org.wso2.carbon.analytics.webservice.stub.beans.StreamDefinitionBean getStreamDefinition(
            StreamDefinitionBean streamDefinitionBean) {
        org.wso2.carbon.analytics.webservice.stub.beans.StreamDefinitionBean streamDef =
                new org.wso2.carbon.analytics.webservice.stub.beans.StreamDefinitionBean();
        streamDef.setDescription(streamDefinitionBean.getDescription());
        streamDef.setNickName(streamDefinitionBean.getNickName());
        streamDef.setName(streamDefinitionBean.getName());
        streamDef.setVersion(streamDefinitionBean.getVersion());
        List<String> tags = streamDefinitionBean.getTags();
        Map<String, String> metaData = streamDefinitionBean.getMetaData();
        Map<String, String> correlationData = streamDefinitionBean.getCorrelationData();
        Map<String, String> payloadData = streamDefinitionBean.getPayloadData();
        if (tags != null) streamDef.setTags(tags.toArray(new String[tags.size()]));
        if (metaData != null) streamDef.setMetaData(getStreamDefAttributes(metaData));
        if (correlationData != null) streamDef.setCorrelationData(getStreamDefAttributes(correlationData));
        if (payloadData != null) streamDef.setPayloadData(getStreamDefAttributes(payloadData));
        return streamDef;
    }

    private static StreamDefAttributeBean[] getStreamDefAttributes(Map<String, String> metaData) {
        List<StreamDefAttributeBean> attributeBeans = new ArrayList<>();
        for (Map.Entry<String, String> entry : metaData.entrySet()) {
            StreamDefAttributeBean bean = new StreamDefAttributeBean();
            bean.setName(entry.getKey());
            bean.setType(entry.getValue());
            attributeBeans.add(bean);
        }
        return attributeBeans.toArray(new StreamDefAttributeBean[metaData.size()]);
    }

    public static org.wso2.carbon.analytics.webservice.stub.beans.EventBean getStreamEvent(
            EventBean eventBean) {
        org.wso2.carbon.analytics.webservice.stub.beans.EventBean bean =
                new org.wso2.carbon.analytics.webservice.stub.beans.EventBean();
        bean.setTimeStamp(eventBean.getTimeStamp());
        bean.setStreamName(eventBean.getStreamName());
        bean.setStreamVersion(eventBean.getStreamVersion());
        Map<String, Object> metaData = eventBean.getMetaData();
        Map<String, Object> correlationData = eventBean.getCorrelationData();
        Map<String, Object> payloadData = eventBean.getPayloadData();
        Map<String, Object> arbitraryValues = eventBean.getArbitraryDataMap();
        if (metaData != null) bean.setMetaData(getRecordValueEntryBeans(metaData));
        if (correlationData != null) bean.setCorrelationData(getRecordValueEntryBeans(correlationData));
        if (payloadData != null) bean.setPayloadData(getRecordValueEntryBeans(payloadData));
        if (arbitraryValues != null) bean.setArbitraryData(getRecordValueEntryBeans(arbitraryValues));
        return bean;
    }

    public static StreamDefinitionBean getStreamDefinitionBean(
            org.wso2.carbon.analytics.webservice.stub.beans.StreamDefinitionBean streamDefinitionBean) {
        StreamDefinitionBean bean = new StreamDefinitionBean();
        bean.setName(streamDefinitionBean.getName());
        bean.setVersion(streamDefinitionBean.getVersion());
        bean.setDescription(streamDefinitionBean.getDescription());
        bean.setNickName(streamDefinitionBean.getNickName());
        String[] tags = streamDefinitionBean.getTags();
        StreamDefAttributeBean[] metaData = streamDefinitionBean.getMetaData();
        StreamDefAttributeBean[] correlationData = streamDefinitionBean.getCorrelationData();
        StreamDefAttributeBean[] payloadData = streamDefinitionBean.getPayloadData();
        if (tags != null) bean.setTags(Arrays.asList(tags));
        if (metaData != null) bean.setMetaData(getStreamAttributesAsMap(metaData));
        if (correlationData != null) bean.setCorrelationData(getStreamAttributesAsMap(correlationData));
        if (payloadData != null) bean.setPayloadData(getStreamAttributesAsMap(payloadData));
        return bean;
    }

    private static Map<String, String> getStreamAttributesAsMap(StreamDefAttributeBean[] attributeBeans) {
        Map<String, String> attributeMap= new LinkedHashMap<>();
        for (StreamDefAttributeBean bean : attributeBeans) {
            if (bean != null) {
                attributeMap.put(bean.getName(), bean.getType());
            }
        }
        return attributeMap;
    }
}
