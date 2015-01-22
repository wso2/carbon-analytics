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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import org.wso2.carbon.analytics.datasource.core.Record;
import org.wso2.carbon.analytics.datasource.core.RecordGroup;
import org.wso2.carbon.analytics.dataservice.AnalyticsDSUtils;
import org.wso2.carbon.context.PrivilegedCarbonContext;

/**
 * REST API utility class.
 */
public class Utils {
    
    private static AnalyticsDataService analyticsDS;

	/**
	 * Gets the analytics data service.
	 * @return the analytics data service
	 */
	public static AnalyticsDataService getAnalyticsDataService() {
	    if (analyticsDS == null) {
		    analyticsDS = (AnalyticsDataService) PrivilegedCarbonContext.
		        getThreadLocalCarbonContext().getOSGiService(AnalyticsDataService.class, null);
	    }
	    return analyticsDS;
	}

	/**
	 * Gets the record from record bean.
	 * @param recordBean
	 *            the record bean
	 * @return the record from record bean
	 */
	public static Record getRecordFromRecordBean(RecordBean recordBean) {
		Record record =
		                new Record(recordBean.getTenantId(), recordBean.getTableName(),
		                           recordBean.getValues(), recordBean.getTimestamp());
		return record;
	}

	/**
	 * Gets the records from record beans.
	 * @param recordBeans
	 *            the record beans
	 * @return the records from record beans
	 */
	public static List<Record> getRecordsFromRecordBeans(List<RecordBean> recordBeans) {
		List<Record> records = new ArrayList<Record>();
		for (RecordBean recordBean : recordBeans) {
			records.add(getRecordFromRecordBean(recordBean));
		}
		return records;
	}
	
	/**
	 * Creates the record beans from records.
	 * @param records the records
	 * @return the list of recordBeans
	 */
	public static List<RecordBean> createRecordBeansFromRecords(List<Record> records) {
		List<RecordBean> recordBeans = new ArrayList<RecordBean>();
		for(Record record : records) {
			recordBeans.add(createRecordBeanFromRecord(record));
		}
		return recordBeans;
	}

	/**
	 * Creates the record bean from record.
	 * @param record
	 *            the record
	 * @return the record bean
	 */
	public static RecordBean createRecordBeanFromRecord(Record record) {
		RecordBean recordBean = new RecordBean();
		recordBean.setId(record.getId());
		recordBean.setTableName(record.getTableName());
		recordBean.setTenantId(record.getTenantId());
		recordBean.setTimestamp(record.getTimestamp());
		recordBean.setValues(record.getValues());
		return recordBean;
	}

	/**
	 * Creates the search result bean from search result.
	 * @param searchResultEntry
	 *            the search result entry
	 * @return the search result entry bean
	 */
	public static SearchResultEntryBean createSearchResultBeanFromSearchResult(SearchResultEntry searchResultEntry) {
		SearchResultEntryBean searchResultEntryBean = new SearchResultEntryBean();
		searchResultEntryBean.setId(searchResultEntry.getId());
		searchResultEntryBean.setScore(searchResultEntry.getScore());
		return searchResultEntryBean;
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
			searchResultBeanList.add(createSearchResultBeanFromSearchResult(searchResult));
		}
		return searchResultBeanList;
	}

	/**
	 * Creates the index type bean from index type.
	 * @param indexType
	 *            the index type
	 * @return the index type bean
	 */
	public static IndexTypeBean createIndexTypeBeanFromIndexType(IndexType indexType) {
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
	public static IndexType createIndexTypeFromIndexTypeBean(IndexTypeBean indexTypeBean) {
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
	public static Map<String, IndexTypeBean> createIndexTypeBeanMapFronIndexTypeMap(Map<String, IndexType> indexTypeMap) {
		Map<String, IndexTypeBean> indexTypeBeanMap = new HashMap<String, IndexTypeBean>();
		Set<String> columns = indexTypeMap.keySet();
		for (String column : columns) {
			indexTypeBeanMap.put(column, createIndexTypeBeanFromIndexType(indexTypeMap.get(column)));
		}
		return indexTypeBeanMap;
	}

	/**
	 * Creates the index type map from index type bean map.
	 * @param indexTypeBeanMap
	 *            the index type bean map
	 * @return the map
	 */
	public static Map<String, IndexType> createIndexTypeMapFronIndexTypeBeanMap(Map<String, IndexTypeBean> indexTypeBeanMap) {
		Map<String, IndexType> indexTypeMap = new HashMap<String, IndexType>();
		Set<String> columns = indexTypeBeanMap.keySet();
		for (String column : columns) {
			indexTypeMap.put(column, createIndexTypeFromIndexTypeBean(indexTypeBeanMap.get(column)));
		}
		return indexTypeMap;
	}
	
	/**
	 * Gets the all record beans from record groups.
	 *
	 * @param recordGroups the record groups
	 * @return the all record beans from record groups
	 * @throws AnalyticsException the analytics exception
	 */
	public static List<RecordBean> getAllRecordBeansFromRecordGroups(RecordGroup[] recordGroups) 
			throws AnalyticsException {
		return createRecordBeansFromRecords(AnalyticsDSUtils.listRecords(getAnalyticsDataService(), recordGroups));
	}
	
	/**
	 * Gets the record ids from search results.
	 * @param searchResults the search results
	 * @return the record ids from search results
	 */
	public static List<String> getRecordIdsFromSearchResults(List<SearchResultEntry> searchResults) {
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
		String message = null;
		if (e.getCause() != null) {
			message = msg.concat(". (" + e.getCause().getMessage() + ")");
		} else if (e.getMessage() != null) {
			message = msg.concat(". (" + e.getMessage() + ")");
		}
		if (msg.contains("Tenant ID cannot be -1")) {
			message = msg.replace("Tenant ID cannot be -1", "Tenant domain is invalid");
		}
		return message;
	}
	
	/**
	 * Gets the time stamp from string.
	 *
	 * @param stringTimeStamp the string time stamp
	 * @return the time stamp from string
	 * @throws AnalyticsException the analytics exception
	 */
	public static long getTimeStampFromString(String stringTimeStamp) throws AnalyticsException{
		SimpleDateFormat dateFormat = new SimpleDateFormat(Constants.TIMESTAMP_PATTERN);
	    Date parsedDate;
        try {
	        parsedDate = dateFormat.parse(stringTimeStamp);
	        return parsedDate.getTime();
        } catch (ParseException e) {
	       throw new AnalyticsException("Error occurred while parsing the timestamps", e);
        }
	}
}
