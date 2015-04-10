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
import org.wso2.carbon.analytics.dataservice.SecureAnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRange;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.DrillDownResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.IndexType;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsCategoryPath;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.webservice.beans.AnalyticsCategoryPathBean;
import org.wso2.carbon.analytics.webservice.beans.AnalyticsSchemaBean;
import org.wso2.carbon.analytics.webservice.beans.DrillDownFieldRangeBean;
import org.wso2.carbon.analytics.webservice.beans.DrillDownPathBean;
import org.wso2.carbon.analytics.webservice.beans.DrillDownRangeBean;
import org.wso2.carbon.analytics.webservice.beans.DrillDownRequestBean;
import org.wso2.carbon.analytics.webservice.beans.DrillDownResultBean;
import org.wso2.carbon.analytics.webservice.beans.IndexConfigurationBean;
import org.wso2.carbon.analytics.webservice.beans.IndexEntryBean;
import org.wso2.carbon.analytics.webservice.beans.PerCategoryDrillDownResultBean;
import org.wso2.carbon.analytics.webservice.beans.PerFieldDrillDownResultBean;
import org.wso2.carbon.analytics.webservice.beans.RecordBean;
import org.wso2.carbon.analytics.webservice.beans.RecordValueEntryBean;
import org.wso2.carbon.analytics.webservice.beans.SchemaColumnBean;
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

	/**
	 * Gets the records from record beans.
	 * @param recordBeans
	 *            the record beans
	 * @return the records from record beans
	 * @throws AnalyticsException if the tableName is not specified
	 */
    public static List<Record> getRecords(String username, List<RecordBean> recordBeans) throws AnalyticsException {
        List<Record> records = new ArrayList<>();
        try {
            int tenantId = getTenantId(username);
            for (RecordBean recordBean : recordBeans) {
                if (recordBean.getTableName().isEmpty()) {
                    throw new AnalyticsException("TableName cannot be empty!");
                }
                records.add(new Record(recordBean.getId(), tenantId, recordBean.getTableName(), validateAndReturn(recordBean.getValues())));
            }
        } catch (NullPointerException e) {
            throw new AnalyticsException("TableName cannot be null");
        }
        return records;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> validateAndReturn(RecordValueEntryBean[] values)
            throws AnalyticsIndexException {
        Map<String, Object> valueMap = new LinkedHashMap<>(0);
        for (RecordValueEntryBean recordEntry : values){
                valueMap.put(recordEntry.getFieldName(),recordEntry.getValue());
        }
        return valueMap;
    }

    /**
	 * Gets the records from record beans belongs to a specific table.
	 * @param recordBeans
	 *            the record beans
	 * @return the records from record beans
	 */
	public static List<Record> getRecordsForTable(String username,String tableName, List<RecordBean> recordBeans)
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
	 * @param records the records
	 * @return the list of recordBeans
	 */
	public static List<RecordBean> createRecordBeans(List<Record> records) {
		List<RecordBean> recordBeans = new ArrayList<>();
		for(Record record : records) {
			RecordBean recordBean = createRecordBean(record);
			recordBeans.add(recordBean);
		}
		return recordBeans;
	}

    /**
     * Create a RecordBean object out of a Record object
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
        List<RecordValueEntryBean> beans = new ArrayList<>(0);
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            RecordValueEntryBean bean = new RecordValueEntryBean();
            if (entry.getValue() instanceof AnalyticsCategoryPath) {
                AnalyticsCategoryPath analyticsCategoryPath = (AnalyticsCategoryPath)entry.getValue();
                AnalyticsCategoryPathBean categoryPathBean = new AnalyticsCategoryPathBean();
                categoryPathBean.setWeight(analyticsCategoryPath.getWeight());
                categoryPathBean.setPath(analyticsCategoryPath.getPath());
                bean.setFieldName(entry.getKey());
                bean.setValue(String.valueOf(categoryPathBean));
                beans.add(bean);
            } else {
                bean.setFieldName(entry.getKey());
                bean.setValue(String.valueOf(entry.getValue()));
                beans.add(bean);
            }
        }
        return beans.toArray(new RecordValueEntryBean[beans.size()]);
    }

    /**
	 * Creates the index type bean from index type.
	 * @param indexType
	 *            the index type
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
	 * @param type
	 *            the index type bean
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
	 * Creates the index type bean map fron index type map.
	 * @param indexTypeMap
	 *            the index type map
	 * @return the map
	 */
	public static IndexEntryBean[] createIndexTypeBeanMap(Map<String, IndexType> indexTypeMap) {
		List<IndexEntryBean> indexTypeBeans = new ArrayList<>(0);
		Set<String> columns = indexTypeMap.keySet();
		for (String column : columns) {
            IndexEntryBean bean = new IndexEntryBean();
            bean.setFieldName(column);
            bean.setIndexType(createIndexTypeBean(indexTypeMap.get(column)));
			indexTypeBeans.add(bean);
		}
		return indexTypeBeans.toArray(new IndexEntryBean[indexTypeBeans.size()]);
	}

	/**
	 * Creates the index type map from index type bean map.
	 * @param indexTypeBeans
	 *            the index type bean map
	 * @return the map
	 */
	public static Map<String, IndexType> createIndexTypeMap(IndexEntryBean[] indexTypeBeans) {
		Map<String, IndexType> indexTypeMap = new HashMap<>(0);
		for (IndexEntryBean entryBean : indexTypeBeans) {
			indexTypeMap.put(entryBean.getFieldName(), createIndexType(entryBean.getIndexType()));
		}
		return indexTypeMap;
	}

	/**
	 * Gets the record ids from search results.
	 * @param searchResults the search results
	 * @return the record ids from search results
	 */
	public static List<String> getRecordIds(List<SearchResultEntry> searchResults) {
		List<String> ids = new ArrayList<>();
		for(SearchResultEntry searchResult : searchResults) {
			ids.add(searchResult.getId());
		}
		return ids;
	}

	/**
	 * Gets the complete error message.
	 * @param msg
	 *            the Message
	 * @param e
	 *            the exception
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
     * @param recordGroups the recordGroup array of which the iterators to be returned
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
     * @param analyticsSchemaBean bean table schema to be converted to Analytics Schema.
     * @return Analytics schema
     */
    public static AnalyticsSchema createAnalyticsSchema(AnalyticsSchemaBean analyticsSchemaBean) {
        Map<String, AnalyticsSchema.ColumnType> columnTypes = new HashMap<>();
        for (SchemaColumnBean columnBean : analyticsSchemaBean.getColumns()) {
            columnTypes.put(columnBean.getColumnName(), getColumnType(columnBean.getColumnType()));
        }
        return new AnalyticsSchema(columnTypes, Arrays.asList(analyticsSchemaBean.getPrimaryKeys()));
    }

    /**
     * Create table schema bean from a analytics schema
     * @param analyticsSchema Analytics schema to be converted to table schema bean
     * @return Table schema bean
     */
    public static AnalyticsSchemaBean createTableSchemaBean(AnalyticsSchema analyticsSchema) {
        List<SchemaColumnBean> columnBeans = new ArrayList<>(0);
        List<String> primaryKeys = new ArrayList<>(0);
        if (analyticsSchema != null) {
            if (analyticsSchema.getColumns() != null) {
                for (Map.Entry<String, AnalyticsSchema.ColumnType> columnTypeEntry :
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
        }
        return new AnalyticsSchemaBean(columnBeans.toArray(new SchemaColumnBean[columnBeans.size()]),
                                       primaryKeys.toArray(new String[primaryKeys.size()]));
    }

    public static DrillDownResultBean createDrillDownResultBean(Map<String, List<DrillDownResultEntry>> result) {
        DrillDownResultBean resultBean = new DrillDownResultBean();
        resultBean.setPerFieldEntries(createPerFieldDrillDownResultBean(result));
        return  resultBean;
    }

    private static PerFieldDrillDownResultBean[] createPerFieldDrillDownResultBean(Map<String
            , List<DrillDownResultEntry>> result) {
        List<PerFieldDrillDownResultBean> resultBeans = new ArrayList<>(0);
        for (Map.Entry<String, List<DrillDownResultEntry>> entry : result.entrySet()) {
            PerFieldDrillDownResultBean entryBean = new PerFieldDrillDownResultBean();
            entryBean.setFieldName(entry.getKey());
            entryBean.setCategories(createPerCategoryDrillDownResultBean(entry.getValue()));
            resultBeans.add(entryBean);
        }
        return resultBeans.toArray(new PerFieldDrillDownResultBean[resultBeans.size()]);
    }

    private static PerCategoryDrillDownResultBean[] createPerCategoryDrillDownResultBean
            (List<DrillDownResultEntry> result) {
        List<PerCategoryDrillDownResultBean> beans = new ArrayList<>(0);
        for (DrillDownResultEntry entry : result) {
            PerCategoryDrillDownResultBean bean = new PerCategoryDrillDownResultBean();
            bean.setCategory(entry.getCategory());
            bean.setCategoryPath(entry.getCategoryPath());
            bean.setRecordCount(entry.getRecordCount());
            bean.setRecordIds(entry.getRecordIds().toArray(new String[entry.getRecordIds().size()]));
            beans.add(bean);
        }
        return beans.toArray(new PerCategoryDrillDownResultBean[beans.size()]);
    }

    /**
     * Creates the AnalyticsDrilldownRequest object given a drilldownrequestBean class
     * @param bean bean class which represents the drilldown request.
     * @param withIds whether to include the matching ids or not. this will be false if the user only needs the counts
     * @return Equivalent AnalyticsDrilldownRequest object.
     */
    public static AnalyticsDrillDownRequest createDrilldownRequest(DrillDownRequestBean bean, boolean withIds) {
        AnalyticsDrillDownRequest drillDownRequest = new AnalyticsDrillDownRequest();
        drillDownRequest.setWithIds(withIds);
        drillDownRequest.setTableName(bean.getTableName());
        drillDownRequest.setCategoryCount(bean.getCategoryCount());
        drillDownRequest.setRecordCount(bean.getRecordCount());
        drillDownRequest.setCategoryStartIndex(bean.getCategoryStart());
        drillDownRequest.setRecordStartIndex(bean.getRecordStart());
        drillDownRequest.setLanguage(bean.getLanguage());
        drillDownRequest.setLanguageQuery(bean.getQuery());
        drillDownRequest.setScoreFunction(bean.getScoreFunction());
        drillDownRequest.setCategoryPaths(createCategoryPaths(bean.getCategories()));
        drillDownRequest.setRangeFacets(createDrillDownRanges(bean.getRanges()));
        return drillDownRequest;
    }

    private static Map<String, List<AnalyticsDrillDownRange>> createDrillDownRanges(
            DrillDownFieldRangeBean[] ranges) {
        Map<String, List<AnalyticsDrillDownRange>> result = new LinkedHashMap<>(0);
        for (DrillDownFieldRangeBean entry : ranges) {
            result.put(entry.getFieldName(), createDrillDownRanges(entry.getRanges()));
        }
        return  result;
    }

    private static List<AnalyticsDrillDownRange> createDrillDownRanges(DrillDownRangeBean[] ranges) {
        List<AnalyticsDrillDownRange> result = new ArrayList<>(0);
        for (DrillDownRangeBean rangeBean : ranges) {
            AnalyticsDrillDownRange range = new AnalyticsDrillDownRange(rangeBean.getLabel(),
                                             rangeBean.getFrom(), rangeBean.getTo());
            result.add(range);
        }
        return result;
    }


    private static Map<String , AnalyticsCategoryPath> createCategoryPaths(DrillDownPathBean[] bean) {
        Map<String, AnalyticsCategoryPath> categoryPaths = new LinkedHashMap<>(0);
        for (DrillDownPathBean drillDownPathBean : bean) {
            categoryPaths.put(drillDownPathBean.getCategoryName(),
                              new AnalyticsCategoryPath(drillDownPathBean.getPath()));
        }
        return categoryPaths;
    }

    /**
     * convert a column type bean to ColumnType
     * @param type ColumnType Bean to be converted to ColumnType
     * @return ColumnType instance
     */
    private static AnalyticsSchema.ColumnType getColumnType(String type) {
        switch (type) {
            case BeanColumnType.STRING:
                return AnalyticsSchema.ColumnType.STRING;
            case BeanColumnType.INTEGER:
                return AnalyticsSchema.ColumnType.INTEGER;
            case BeanColumnType.LONG:
                return AnalyticsSchema.ColumnType.LONG;
            case BeanColumnType.FLOAT:
                return AnalyticsSchema.ColumnType.FLOAT;
            case BeanColumnType.DOUBLE:
                return AnalyticsSchema.ColumnType.DOUBLE;
            case BeanColumnType.BOOLEAN:
                return AnalyticsSchema.ColumnType.BOOLEAN;
            case BeanColumnType.BINARY:
                return AnalyticsSchema.ColumnType.BINARY;
            default:
                return AnalyticsSchema.ColumnType.STRING;
        }
    }

    /**
     * convert a column type to bean type
     * @param columnType the ColumnType to be converted to bean type
     * @return ColumnTypeBean instance
     */
    private static String getColumnTypeBean(AnalyticsSchema.ColumnType columnType) {
        switch (columnType) {
            case STRING:
                return BeanColumnType.STRING;
            case INTEGER:
                return BeanColumnType.INTEGER;
            case LONG:
                return BeanColumnType.LONG;
            case FLOAT:
                return BeanColumnType.FLOAT;
            case DOUBLE:
                return BeanColumnType.DOUBLE;
            case BOOLEAN:
                return BeanColumnType.BOOLEAN;
            case BINARY:
                return BeanColumnType.BINARY;
            default:
                return BeanColumnType.STRING;
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

    private class  BeanColumnType {

        public static final String STRING = "STRING";
        public static final String LONG = "LONG";
        public static final String FLOAT = "FLOAT";
        public static final String DOUBLE = "DOUBLE";
        public static final String BOOLEAN = "BOOLEAN";
        public static final String BINARY = "BINARY";
        public static final String INTEGER = "INTEGER";

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
        if (indexConfigurationBean.getIndices() != null) {
            for (IndexEntryBean indexEntryBean : indexConfigurationBean.getIndices()) {
                indexTypeMap.put(indexEntryBean.getFieldName(), createIndexType(indexEntryBean.getIndexType()));
            }
        }
        return indexTypeMap;
    }

    public static List<String> getScoreParam(IndexConfigurationBean indexConfigurationBean) {
        List<String> scoreParams = null;
        if (indexConfigurationBean.getScoreParams() != null) {
            scoreParams = Arrays.asList(indexConfigurationBean.getScoreParams());
        }
        return scoreParams;
    }

    private Object getObject(String value, String type) {
        Object convertedValue;
        switch (type) {
            case BeanColumnType.STRING: {
                convertedValue = String.valueOf(value);
                break;
            }
            case BeanColumnType.INTEGER: {
                if (value == null || value.isEmpty()) {
                    value = "0";
                }
                convertedValue = Integer.valueOf(value);
                break;
            }
            case BeanColumnType.LONG: {
                if (value == null || value.isEmpty()) {
                    value = "0";
                }
                convertedValue = Integer.valueOf(value);
                break;
            }
            case BeanColumnType.BOOLEAN: {
                convertedValue = Boolean.valueOf(value);
                break;
            }
            case BeanColumnType.FLOAT: {
                if (value == null || value.isEmpty()) {
                    value = "0.0";
                }
                convertedValue = Float.valueOf(value);
                break;
            }
            case BeanColumnType.DOUBLE: {
                if (value == null || value.isEmpty()) {
                    value = "0.0";
                }
                convertedValue = Double.valueOf(value);
                break;
            }
            default: {
                convertedValue = value;
            }
        }
        return convertedValue;
    }
}
