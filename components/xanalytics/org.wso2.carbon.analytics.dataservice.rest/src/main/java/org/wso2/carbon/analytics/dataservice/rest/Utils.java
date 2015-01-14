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

package org.wso2.carbon.analytics.dataservice.rest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.indexing.IndexType;
import org.wso2.carbon.analytics.dataservice.indexing.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.rest.beans.IndexTypeBean;
import org.wso2.carbon.analytics.dataservice.rest.beans.RecordBean;
import org.wso2.carbon.analytics.dataservice.rest.beans.RecordGroupBean;
import org.wso2.carbon.analytics.dataservice.rest.beans.SearchResultEntryBean;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.Record;
import org.wso2.carbon.analytics.datasource.core.RecordGroup;
import org.wso2.carbon.context.PrivilegedCarbonContext;

public class Utils {

	private Utils() {

	}

	public static AnalyticsDataService getAnalyticsDataService() {
		return (AnalyticsDataService) PrivilegedCarbonContext.getThreadLocalCarbonContext()
		                                                     .getOSGiService(AnalyticsDataService.class,
		                                                                     null);
	}

	public static Record getRecordFromRecordBean(RecordBean recordBean) {

		Record record =
		                new Record(recordBean.getTenantId(), recordBean.getTableName(),
		                           recordBean.getValues(), recordBean.getTimestamp());
		return record;
	}

	public static List<Record> getRecordsFromRecordBeans(List<RecordBean> recordBeans) {

		List<Record> records = new ArrayList<Record>();
		for ( RecordBean recordBean : recordBeans) {
			records.add(getRecordFromRecordBean(recordBean));
		}
		return records;
	}

	public static RecordBean createRecordBeanFromRecord(Record record) {
		RecordBean recordBean = new RecordBean();
		recordBean.setId(record.getId());
		recordBean.setTableName(record.getTableName());
		recordBean.setTenantId(record.getTenantId());
		recordBean.setTimestamp(record.getTimestamp());
		recordBean.setValues(record.getValues());
		return recordBean;
	}

	public static RecordGroupBean createRecordGroupBeanFromRecordGroup(RecordGroup recordGroup)
	                                                         throws AnalyticsException {
		RecordGroupBean recordGroupBean = new RecordGroupBean();
		recordGroupBean.setLocations(recordGroup.getLocations());
		List<Record> recordList = recordGroup.getRecords();
		List<RecordBean> recordBeanList = new ArrayList<RecordBean>();
		for (Record record : recordList) {
			recordBeanList.add(createRecordBeanFromRecord(record));
		}
		recordGroupBean.setRecords(recordBeanList);
		return recordGroupBean;
	}

	public static List<RecordGroupBean> createRecordGroupBeansFromRecordGroups ( RecordGroup[] recordGroups) 
			throws AnalyticsException {
		List<RecordGroupBean> recordGroupBeans = new ArrayList<RecordGroupBean>();
		for ( RecordGroup recordGroup : recordGroups) {
			recordGroupBeans.add(createRecordGroupBeanFromRecordGroup(recordGroup));
		}
		return recordGroupBeans;
	}
	public static SearchResultEntryBean createSearchResultBeanFromSearchResult(SearchResultEntry 
	                                                                           searchResultEntry) {
		SearchResultEntryBean searchResultEntryBean = new SearchResultEntryBean();
		searchResultEntryBean.setId(searchResultEntry.getId());
		searchResultEntryBean.setScore(searchResultEntry.getScore());
		return searchResultEntryBean;
	}

	public static List<SearchResultEntryBean> createSearchResultBeansFromSearchResults(List<SearchResultEntry> searchResults) {
		List<SearchResultEntryBean> searchResultBeanList = new ArrayList<SearchResultEntryBean>();
		for (SearchResultEntry searchResult : searchResults) {
			searchResultBeanList.add(createSearchResultBeanFromSearchResult(searchResult));
		}
		return searchResultBeanList;
	}

	public static IndexTypeBean createIndexTypeBeanFromIndexType( IndexType indexType) {
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
	
	public static IndexType createIndexTypeFromIndexTypeBean( IndexTypeBean indexTypeBean) {
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
	
	public static Map<String, IndexTypeBean> createIndexTypeBeanMapFronIndexTypeMap(Map<String, 
	                                                                                IndexType> indexTypeMap) {
		Map<String, IndexTypeBean> indexTypeBeanMap = new HashMap<String, IndexTypeBean>();
		Set<String> columns = indexTypeMap.keySet();
		for(String column : columns) {
			indexTypeBeanMap.put(column, createIndexTypeBeanFromIndexType(indexTypeMap.get(column)));
		}
		return indexTypeBeanMap;
	}
	
	public static Map<String, IndexType> createIndexTypeMapFronIndexTypeBeanMap(Map<String, 
	                                                                                IndexTypeBean> indexTypeBeanMap) {
		Map<String, IndexType> indexTypeMap = new HashMap<String, IndexType>();
		Set<String> columns = indexTypeBeanMap.keySet();
		for(String column : columns) {
			indexTypeMap.put(column, createIndexTypeFromIndexTypeBean(indexTypeBeanMap.get(column)));
		}
		return indexTypeMap;
	}
	
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
}
