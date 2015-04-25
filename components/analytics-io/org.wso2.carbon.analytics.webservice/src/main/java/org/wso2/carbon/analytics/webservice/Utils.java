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

package org.wso2.carbon.analytics.webservice;

import org.wso2.carbon.analytics.dataservice.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.commons.IndexType;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema.ColumnType;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.webservice.beans.AnalyticsCategoryPathBean;
import org.wso2.carbon.analytics.webservice.beans.AnalyticsSchemaBean;
import org.wso2.carbon.analytics.webservice.beans.IndexConfigurationBean;
import org.wso2.carbon.analytics.webservice.beans.IndexEntryBean;
import org.wso2.carbon.analytics.webservice.beans.RecordBean;
import org.wso2.carbon.analytics.webservice.beans.RecordValueEntryBean;
import org.wso2.carbon.analytics.webservice.beans.SchemaColumnBean;
import org.wso2.carbon.user.api.UserStoreException;
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
     * Gets the records from record beans.
     *
     * @param recordBeans the record beans
     * @return the records from record beans
     * @throws AnalyticsException if the tableName is not specified
     */
    public static List<Record> getRecords(String username, List<RecordBean> recordBeans) throws AnalyticsException {
        List<Record> records = new ArrayList<>();
        int tenantId = getTenantId(username);
        for (RecordBean recordBean : recordBeans) {
            if (recordBean.getTableName().isEmpty()) {
                throw new AnalyticsException("TableName cannot be empty!");
            }
            records.add(new Record(recordBean.getId(), tenantId, recordBean.getTableName(), validateAndReturn(recordBean.getValues())));
        }
        return records;
    }

    private static Map<String, Object> validateAndReturn(RecordValueEntryBean[] values)
            throws AnalyticsIndexException {
        Map<String, Object> valueMap = new LinkedHashMap<>();
        for (RecordValueEntryBean recordEntry : values) {
            valueMap.put(recordEntry.getFieldName(), getValue(recordEntry));
        }
        return valueMap;
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
        recordBean.setValues(createRecordEntryBeans(record.getValues()));
        return recordBean;
    }

    private static RecordValueEntryBean[] createRecordEntryBeans(Map<String, Object> values) {
        List<RecordValueEntryBean> beans = new ArrayList<>(values.size());
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            RecordValueEntryBean bean = new RecordValueEntryBean();
            if (entry.getValue() instanceof List) {
                List<String> analyticsCategoryPath = (List<String>) entry.getValue();
                AnalyticsCategoryPathBean categoryPathBean = new AnalyticsCategoryPathBean();
                categoryPathBean.setPath(analyticsCategoryPath.toArray(new String[analyticsCategoryPath.size()]));
                bean.setFieldName(entry.getKey());
                bean.setAnalyticsCategoryPathBeanValue(categoryPathBean);
                bean.setType(RecordValueEntryBean.FACET);
                beans.add(bean);
            } else {
                beans.add(getRecordValueEntryBean(entry.getKey(), entry.getValue()));
            }
        }
        return beans.toArray(new RecordValueEntryBean[beans.size()]);
    }

    /**
     * Creates the index type bean from index type.
     *
     * @param indexType the index type
     * @return the index type bean
     */
    public static String createIndexTypeBean(IndexType indexType) {
        switch (indexType) {
            case BOOLEAN:
                return BeanIndexType.BOOLEAN;
            case FLOAT:
                return BeanIndexType.FLOAT;
            case DOUBLE:
                return BeanIndexType.DOUBLE;
            case INTEGER:
                return BeanIndexType.INTEGER;
            case LONG:
                return BeanIndexType.LONG;
            case STRING:
                return BeanIndexType.STRING;
            case FACET:
                return BeanIndexType.FACET;
            default:
                return BeanIndexType.STRING;
        }
    }

    /**
     * Creates the index type from index type bean.
     *
     * @param type the index type bean
     * @return the index type
     */

    public static IndexType createIndexType(String type) {
        switch (type) {
            case BeanIndexType.BOOLEAN:
                return IndexType.BOOLEAN;
            case BeanIndexType.FLOAT:
                return IndexType.FLOAT;
            case BeanIndexType.DOUBLE:
                return IndexType.DOUBLE;
            case BeanIndexType.INTEGER:
                return IndexType.INTEGER;
            case BeanIndexType.LONG:
                return IndexType.LONG;
            case BeanIndexType.STRING:
                return IndexType.STRING;
            case BeanIndexType.FACET:
                return IndexType.FACET;
            default:
                return IndexType.STRING;
        }
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
     * Create a Analytics schema from a bean class
     *
     * @param analyticsSchemaBean bean table schema to be converted to Analytics Schema.
     * @return Analytics schema
     */
    public static AnalyticsSchema createAnalyticsSchema(AnalyticsSchemaBean analyticsSchemaBean) {
        Map<String, ColumnType> columnTypes = null;
        if (analyticsSchemaBean == null) {
            return null;
        }
        if (analyticsSchemaBean.getColumns() != null) {
            columnTypes = new HashMap<>();
            for (SchemaColumnBean columnBean : analyticsSchemaBean.getColumns()) {
                columnTypes.put(columnBean.getColumnName(), getColumnType(columnBean.getColumnType()));
            }
        }
        List<String> primaryKeys = null;
        if (analyticsSchemaBean.getPrimaryKeys() != null) {
            primaryKeys = Arrays.asList(analyticsSchemaBean.getPrimaryKeys());
        }
        return new AnalyticsSchema(columnTypes, primaryKeys);
    }

    /**
     * Create table schema bean from a analytics schema
     *
     * @param analyticsSchema Analytics schema to be converted to table schema bean
     * @return Table schema bean
     */
    public static AnalyticsSchemaBean createTableSchemaBean(AnalyticsSchema analyticsSchema) {

        if (analyticsSchema == null) {
            return null;
        }
        List<SchemaColumnBean> columnBeans = new ArrayList<>();
        List<String> primaryKeys = new ArrayList<>();
        if (analyticsSchema.getColumns() != null) {
            for (Map.Entry<String, ColumnType> columnTypeEntry :
                    analyticsSchema.getColumns().entrySet()) {
                SchemaColumnBean bean = new SchemaColumnBean();
                bean.setColumnName(columnTypeEntry.getKey());
                bean.setColumnType(getColumnTypeBean(columnTypeEntry.getValue()));
                columnBeans.add(bean);
            }
        }
        if (analyticsSchema.getPrimaryKeys() != null) {
            primaryKeys = analyticsSchema.getPrimaryKeys();
        }
        return new AnalyticsSchemaBean(columnBeans.toArray(new SchemaColumnBean[columnBeans.size()]),
                                       primaryKeys.toArray(new String[primaryKeys.size()]));
    }

    /**
     * convert a column type bean to ColumnType
     *
     * @param type ColumnType Bean to be converted to ColumnType
     * @return ColumnType instance
     */
    private static ColumnType getColumnType(String type) {
        switch (type) {
            case RecordValueEntryBean.STRING:
                return ColumnType.STRING;
            case RecordValueEntryBean.INTEGER:
                return ColumnType.INTEGER;
            case RecordValueEntryBean.LONG:
                return ColumnType.LONG;
            case RecordValueEntryBean.FLOAT:
                return ColumnType.FLOAT;
            case RecordValueEntryBean.DOUBLE:
                return ColumnType.DOUBLE;
            case RecordValueEntryBean.BOOLEAN:
                return ColumnType.BOOLEAN;
            case RecordValueEntryBean.BINARY:
                return ColumnType.BINARY;
            case RecordValueEntryBean.FACET:
                return ColumnType.FACET;
            default:
                return ColumnType.STRING;
        }
    }

    /**
     * convert a column type to bean type
     *
     * @param columnType the ColumnType to be converted to bean type
     * @return ColumnTypeBean instance
     */
    private static String getColumnTypeBean(ColumnType columnType) {
        switch (columnType) {
            case STRING:
                return RecordValueEntryBean.STRING;
            case INTEGER:
                return RecordValueEntryBean.INTEGER;
            case LONG:
                return RecordValueEntryBean.LONG;
            case FLOAT:
                return RecordValueEntryBean.FLOAT;
            case DOUBLE:
                return RecordValueEntryBean.DOUBLE;
            case BOOLEAN:
                return RecordValueEntryBean.BOOLEAN;
            case BINARY:
                return RecordValueEntryBean.BINARY;
            case FACET:
                return RecordValueEntryBean.FACET;
            default:
                return RecordValueEntryBean.STRING;
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

    private static class BeanIndexType {
        public static final String STRING = "STRING";
        public static final String LONG = "LONG";
        public static final String FLOAT = "FLOAT";
        public static final String DOUBLE = "DOUBLE";
        public static final String BOOLEAN = "BOOLEAN";
        public static final String BINARY = "BINARY";
        public static final String INTEGER = "INTEGER";
        public static final String FACET = "FACET";
        public static final String SCOREPARAM = "SCOREPARAM";
    }

    public static IndexConfigurationBean getIndexConfiguration(Map<String, IndexType> indices, List<String>
            scoreParams) {
        IndexConfigurationBean indexConfigurationBean = new IndexConfigurationBean();
        IndexEntryBean[] indexEntryBeans = new IndexEntryBean[indices.size()];
        int i = 0;
        for (Map.Entry<String, IndexType> index : indices.entrySet()) {
            IndexEntryBean indexEntryBean = new IndexEntryBean();
            indexEntryBean.setFieldName(index.getKey());
            indexEntryBean.setIndexType(index.getValue().name());
            indexEntryBeans[i++] = indexEntryBean;
        }
        indexConfigurationBean.setIndices(indexEntryBeans);
        if (scoreParams != null) {
            String[] scoreParamsArray = new String[scoreParams.size()];
            scoreParamsArray = scoreParams.toArray(scoreParamsArray);
            indexConfigurationBean.setScoreParams(scoreParamsArray);
        }
        return indexConfigurationBean;
    }

    public static Map<String, IndexType> getIndices(IndexConfigurationBean indexConfigurationBean) {
        Map<String, IndexType> indexTypeMap = new HashMap<>();
        if (indexConfigurationBean != null && indexConfigurationBean.getIndices() != null) {
            for (IndexEntryBean indexEntryBean : indexConfigurationBean.getIndices()) {
                indexTypeMap.put(indexEntryBean.getFieldName(), createIndexType(indexEntryBean.getIndexType()));
            }
        }
        return indexTypeMap;
    }

    public static List<String> getScoreParam(IndexConfigurationBean indexConfigurationBean) {
        List<String> scoreParams = null;
        if (indexConfigurationBean != null && indexConfigurationBean.getScoreParams() != null) {
            scoreParams = Arrays.asList(indexConfigurationBean.getScoreParams());
        }
        return scoreParams;
    }

    private static Object getValue(RecordValueEntryBean recordValueEntryBean) {
        Object resultObj;
        switch (recordValueEntryBean.getType()) {
            case RecordValueEntryBean.STRING: {
                resultObj = recordValueEntryBean.getStringValue();
                break;
            }
            case RecordValueEntryBean.INTEGER: {
                resultObj = recordValueEntryBean.getIntValue();
                break;
            }
            case RecordValueEntryBean.LONG: {
                resultObj = recordValueEntryBean.getLongValue();
                break;
            }
            case RecordValueEntryBean.BOOLEAN: {
                resultObj = recordValueEntryBean.getBooleanValue();
                break;
            }
            case RecordValueEntryBean.FLOAT: {
                resultObj = recordValueEntryBean.getFloatValue();
                break;
            }
            case RecordValueEntryBean.DOUBLE: {
                resultObj = recordValueEntryBean.getDoubleValue();
                break;
            }
            case RecordValueEntryBean.FACET: {
                List<String> analyticsCategoryPath = new ArrayList<>();
                if (recordValueEntryBean.getAnalyticsCategoryPathBeanValue() != null) {
                    analyticsCategoryPath.addAll(Arrays.asList(recordValueEntryBean.getAnalyticsCategoryPathBeanValue().getPath()));
                }
                resultObj = analyticsCategoryPath;
                break;
            }
            default: {
                resultObj = recordValueEntryBean.getStringValue();
            }
        }
        return resultObj;
    }

    private static RecordValueEntryBean getRecordValueEntryBean(String fieldName, Object value) {
        RecordValueEntryBean recordValueEntryBean = new RecordValueEntryBean();
        recordValueEntryBean.setFieldName(fieldName);
        if (value != null) {
            switch (value.getClass().toString().toUpperCase()) {
                case RecordValueEntryBean.STRING: {
                    recordValueEntryBean.setStringValue(String.valueOf(value));
                    recordValueEntryBean.setType(RecordValueEntryBean.STRING);
                    break;
                }
                case RecordValueEntryBean.INTEGER: {
                    recordValueEntryBean.setIntValue(Integer.valueOf(String.valueOf(value)));
                    recordValueEntryBean.setType(RecordValueEntryBean.INTEGER);
                    break;
                }
                case RecordValueEntryBean.LONG: {
                    recordValueEntryBean.setLongValue(Long.valueOf(String.valueOf(value)));
                    recordValueEntryBean.setType(RecordValueEntryBean.LONG);
                    break;
                }
                case RecordValueEntryBean.BOOLEAN: {
                    recordValueEntryBean.setBooleanValue(Boolean.valueOf(String.valueOf(value)));
                    recordValueEntryBean.setType(RecordValueEntryBean.BOOLEAN);
                    break;
                }
                case RecordValueEntryBean.FLOAT: {
                    recordValueEntryBean.setFloatValue(Float.valueOf(String.valueOf(value)));
                    recordValueEntryBean.setType(RecordValueEntryBean.FLOAT);
                    break;
                }
                case RecordValueEntryBean.DOUBLE: {
                    recordValueEntryBean.setDoubleValue(Double.valueOf(String.valueOf(value)));
                    recordValueEntryBean.setType(RecordValueEntryBean.DOUBLE);
                    break;
                }
                default: {
                    recordValueEntryBean.setStringValue(String.valueOf(value));
                    recordValueEntryBean.setType(RecordValueEntryBean.STRING);
                }
            }
        }
        return recordValueEntryBean;
    }
}
