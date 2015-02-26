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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.indexing.IndexType;
import org.wso2.carbon.analytics.dataservice.indexing.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.restapi.beans.IndexTypeBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.RecordBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.SearchResultEntryBean;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.rs.Record;
import org.wso2.carbon.context.PrivilegedCarbonContext;

/**
 * This class represents a set of utility functionalities for the analytics REST API.
 */
public class Utils {

	/**
	 * Gets the analytics data service.
	 * @return the analytics data service
	 * @throws AnalyticsRESTException 
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
	 * Gets the records from record beans.
	 * @param recordBeans
	 *            the record beans
	 * @return the records from record beans
	 * @throws AnalyticsRESTException if the tableName is not specified
	 */
	public static List<Record> getRecords(int tenantId, List<RecordBean> recordBeans) throws AnalyticsException {
		List<Record> records = new ArrayList<Record>();
		try{
			for (RecordBean recordBean : recordBeans) {
				if(recordBean.getTableName().isEmpty()){
					throw new AnalyticsException("TableName cannot be empty!");
				}
			records.add(new Record(recordBean.getId(), tenantId, recordBean.getTableName(),
		                           recordBean.getValues(), recordBean.getTimestamp()));
			}
		}catch(NullPointerException e){
			throw new AnalyticsException("TableName cannot be null");
		}
		return records;
	}
	
	/**
	 * Gets the records from record beans belongs to a specific table.
	 * @param recordBeans
	 *            the record beans
	 * @return the records from record beans
	 * @throws AnalyticsRESTException if the tableName is not specified
	 */
	public static List<Record> getRecordsForTable(int tenantId,String tableName, List<RecordBean> recordBeans) {
		List<Record> records = new ArrayList<Record>();
		for (RecordBean recordBean : recordBeans) {
			records.add(new Record(recordBean.getId(), tenantId, tableName,
		                           recordBean.getValues(), recordBean.getTimestamp()));
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
			RecordBean recordBean = new RecordBean();
			recordBean.setId(record.getId());
			recordBean.setTableName(record.getTableName());
			recordBean.setTimestamp(record.getTimestamp());
			recordBean.setValues(record.getValues());
			recordBeans.add(recordBean);
		}
		return recordBeans;
	}
	
	/**
	 * Creates the search result beans from search results.
	 * @param searchResults
	 *            the search results
	 * @return the list
	 */
	public static List<SearchResultEntryBean> createSearchResultBeansFromSearchResults(List<SearchResultEntry> searchResults) {
		List<SearchResultEntryBean> searchResultBeanList = new ArrayList<SearchResultEntryBean>();
		for (SearchResultEntry searchResult : searchResults) {
			SearchResultEntryBean searchResultEntryBean = new SearchResultEntryBean();
			searchResultEntryBean.setId(searchResult.getId());
			searchResultEntryBean.setScore(searchResult.getScore());
			searchResultBeanList.add(searchResultEntryBean);
		}
		return searchResultBeanList;
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
			message.append(". (" + e.getCause().getMessage() + ")");
		} else if (e.getMessage() != null) {
			message.append(". (" + e.getMessage() + ")");
		}
		return message.toString();
	}
}
