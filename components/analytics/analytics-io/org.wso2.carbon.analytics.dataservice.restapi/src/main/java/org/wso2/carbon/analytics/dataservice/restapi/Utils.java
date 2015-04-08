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

package org.wso2.carbon.analytics.dataservice.restapi;

import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.SecureAnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRange;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.DrillDownResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.IndexType;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.dataservice.restapi.beans.AnalyticsSchemaBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.DrillDownPathBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.ColumnTypeBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.DrillDownRangeBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.DrillDownRequestBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.DrillDownResultBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.IndexTypeBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.PerCategoryDrillDownResultBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.PerFieldDrillDownResultBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.RecordBean;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsCategoryPath;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
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
	 * @return the analytics data service
	 * @throws AnalyticsException
	 */
	public static AnalyticsDataService getAnalyticsDataService() throws AnalyticsException {
		AnalyticsDataService analyticsDataService;
		analyticsDataService = (AnalyticsDataService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
		                                                     .getOSGiService(AnalyticsDataService.class,
		                                                                     null);
		if(analyticsDataService == null) {
			throw new AnalyticsException("AnalyticsDataService is not available.");
		}
		return analyticsDataService;
	}

    /**
	 * Gets the analytics data service.
	 * @return the analytics data service
	 * @throws AnalyticsException
	 */
	public static SecureAnalyticsDataService getSecureAnalyticsDataService() throws AnalyticsException {
		SecureAnalyticsDataService analyticsDataService;
		analyticsDataService = (SecureAnalyticsDataService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
		                                                     .getOSGiService(SecureAnalyticsDataService.class,
		                                                                     null);
		if(analyticsDataService == null) {
			throw new AnalyticsException("AnalyticsDataService is not available.");
		}
		return analyticsDataService;
	}

	/**
	 * Gets the records from record beans.
	 * @param recordBeans
	 *            the record beans
	 * @return the records from record beans
	 * @throws AnalyticsException if the tableName is not specified
	 */
	public static List<Record> getRecords(String username, List<RecordBean> recordBeans) throws AnalyticsException {
		List<Record> records = new ArrayList<Record>();
		try{
			for (RecordBean recordBean : recordBeans) {
				if(recordBean.getTableName().isEmpty()){
					throw new AnalyticsException("TableName cannot be empty!");
				}
			records.add(new Record(recordBean.getId(), getTenantId(username), recordBean.getTableName(),
		                           validateAndReturn(recordBean.getValues())));
			}
		}catch(NullPointerException e){
			throw new AnalyticsException("TableName cannot be null");
		}
		return records;
	}

    @SuppressWarnings("unchecked")
    private static Map<String, Object> validateAndReturn(Map<String, Object> values)
            throws AnalyticsIndexException {
        Map<String, Object> valueMap = new LinkedHashMap<String, Object>(0);
        for (Map.Entry<String, Object> recordEntry : values.entrySet()){
            //TODO : AnalyticsCategoryPath is mapped to a linkedList by jackson json.
            // Currently checking the type and convert it manually to categoryPath type.
            if (recordEntry.getValue() instanceof LinkedHashMap) {
                Map<String, Object> keyValPairMap = (LinkedHashMap<String, Object>) recordEntry.getValue();
                List<String> pathList = (ArrayList<String>) keyValPairMap.get(Constants.FacetAttributes.PATH);
                Object weightObj = keyValPairMap.get(Constants.FacetAttributes.WEIGHT);
                Number weight;
                if (weightObj instanceof Integer) {
                    weight = (Integer) weightObj;
                } else if (weightObj instanceof Double) {
                    weight = (Double) weightObj;
                } else if (weightObj == null) {
                    weight = DEFAUL_CATEGORYPATH_WEIGHT;
                }
                else {
                    throw new AnalyticsIndexException("Category Weight should be a float/integer value");
                }
                if (pathList != null && pathList.size() > 0) {
                    String[] path = pathList.toArray(new String[pathList.size()]);
                    if (keyValPairMap.keySet().size() <= CATEGORYPATH_FIELD_COUNT) {
                        AnalyticsCategoryPath categoryPath = new
                                AnalyticsCategoryPath(path);
                        categoryPath.setWeight(weight.floatValue());
                        valueMap.put(recordEntry.getKey(), categoryPath);
                    }
                } else {
                    throw new AnalyticsIndexException("Category path cannot be empty");
                }
            } else {
                valueMap.put(recordEntry.getKey(),recordEntry.getValue());
            }
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
		List<Record> records = new ArrayList<Record>();
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
		List<RecordBean> recordBeans = new ArrayList<RecordBean>();
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
        recordBean.setValues(record.getValues());
        return recordBean;
    }

	/**
	 * Creates the index type bean from index type.
	 * @param indexType
	 *            the index type
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
            case SCOREPARAM:
                return IndexTypeBean.SCOREPARAM;
			default:
				return IndexTypeBean.STRING;
		}
	}

	/**
	 * Creates the index type from index type bean.
	 * @param indexTypeBean
	 *            the index type bean
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
            case SCOREPARAM:
                return IndexType.SCOREPARAM;
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
	public static Map<String, IndexTypeBean> createIndexTypeBeanMap(Map<String, IndexType> indexTypeMap) {
		Map<String, IndexTypeBean> indexTypeBeanMap = new HashMap<String, IndexTypeBean>();
		Set<String> columns = indexTypeMap.keySet();
		for (String column : columns) {
			indexTypeBeanMap.put(column, createIndexTypeBean(indexTypeMap.get(column)));
		}
		return indexTypeBeanMap;
	}

	/**
	 * Creates the index type map from index type bean map.
	 * @param indexTypeBeanMap
	 *            the index type bean map
	 * @return the map
	 */
	public static Map<String, IndexType> createIndexTypeMap(Map<String, IndexTypeBean> indexTypeBeanMap) {
		Map<String, IndexType> indexTypeMap = new HashMap<String, IndexType>();
		Set<String> columns = indexTypeBeanMap.keySet();
		for (String column : columns) {
			indexTypeMap.put(column, createIndexType(indexTypeBeanMap.get(column)));
		}
		return indexTypeMap;
	}

	/**
	 * Gets the record ids from search results.
	 * @param searchResults the search results
	 * @return the record ids from search results
	 */
	public static List<String> getRecordIds(List<SearchResultEntry> searchResults) {
		List<String> ids = new ArrayList<String>();
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

        List<Iterator<Record>> iterators = new ArrayList<Iterator<Record>>();
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
        Map<String, AnalyticsSchema.ColumnType> columnTypes = new HashMap<String, AnalyticsSchema.ColumnType>();
        for (Map.Entry<String, ColumnTypeBean> columnEntry : analyticsSchemaBean.getColumns().entrySet()) {
            columnTypes.put(columnEntry.getKey(), getColumnType(columnEntry.getValue()));
        }
        return new AnalyticsSchema(columnTypes, analyticsSchemaBean.getPrimaryKeys());
    }

    /**
     * Create table schema bean from a analytics schema
     * @param analyticsSchema Analytics schema to be converted to table schema bean
     * @return Table schema bean
     */
    public static AnalyticsSchemaBean createTableSchemaBean(AnalyticsSchema analyticsSchema) {
        Map<String, ColumnTypeBean> columnTypeBeanTypes = new HashMap<String, ColumnTypeBean>();
        List<String> primaryKeys = new ArrayList<String>();
        if (analyticsSchema.getColumns() != null) {
            for (Map.Entry<String, AnalyticsSchema.ColumnType> columnTypeEntry :
                    analyticsSchema.getColumns().entrySet()) {
                columnTypeBeanTypes.put(columnTypeEntry.getKey(), getColumnTypeBean(columnTypeEntry.getValue()));
            }
        }
        if (analyticsSchema.getPrimaryKeys() != null) {
            primaryKeys = analyticsSchema.getPrimaryKeys();
        }
        return new AnalyticsSchemaBean(columnTypeBeanTypes,primaryKeys);
    }

    public static DrillDownResultBean createDrillDownResultBean(Map<String, List<DrillDownResultEntry>> result) {
        DrillDownResultBean resultBean = new DrillDownResultBean();
        resultBean.setPerFieldEntries(createPerFieldDrillDownResultBean(result));
        return  resultBean;
    }

    private static PerFieldDrillDownResultBean[] createPerFieldDrillDownResultBean(Map<String
            , List<DrillDownResultEntry>> result) {
        List<PerFieldDrillDownResultBean> resultBeans = new ArrayList<PerFieldDrillDownResultBean>(0);
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
        List<PerCategoryDrillDownResultBean> beans = new ArrayList<PerCategoryDrillDownResultBean>(0);
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
            Map<String, List<DrillDownRangeBean>> ranges) {
        Map<String, List<AnalyticsDrillDownRange>> result = new LinkedHashMap<String,
                List<AnalyticsDrillDownRange>>(0);
        for (Map.Entry<String, List<DrillDownRangeBean>> entry : ranges.entrySet()) {
            result.put(entry.getKey(), createDrillDownRanges(entry.getValue()));
        }
        return  result;
    }

    private static List<AnalyticsDrillDownRange> createDrillDownRanges(List<DrillDownRangeBean> ranges) {
        List<AnalyticsDrillDownRange> result = new ArrayList<AnalyticsDrillDownRange>(0);
        for (DrillDownRangeBean rangeBean : ranges) {
            AnalyticsDrillDownRange range = new AnalyticsDrillDownRange(rangeBean.getLabel(),
                                             rangeBean.getFrom(), rangeBean.getTo());
            result.add(range);
        }
        return result;
    }


    private static Map<String , AnalyticsCategoryPath> createCategoryPaths(List<DrillDownPathBean> bean) {
        Map<String, AnalyticsCategoryPath> categoryPaths = new LinkedHashMap<String, AnalyticsCategoryPath>(0);
        for (DrillDownPathBean drillDownPathBean : bean) {
            categoryPaths.put(drillDownPathBean.getCategoryName(),
                              new AnalyticsCategoryPath(drillDownPathBean.getPath()));
        }
        return categoryPaths;
    }

    /**
     * convert a column type bean to ColumnType
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
