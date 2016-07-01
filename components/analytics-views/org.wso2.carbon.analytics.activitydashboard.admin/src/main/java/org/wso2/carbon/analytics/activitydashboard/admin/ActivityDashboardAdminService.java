/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.activitydashboard.admin;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.activitydashboard.admin.bean.ActivitySearchRequest;
import org.wso2.carbon.analytics.activitydashboard.admin.bean.ColumnEntry;
import org.wso2.carbon.analytics.activitydashboard.admin.bean.RecordBean;
import org.wso2.carbon.analytics.activitydashboard.admin.bean.RecordId;
import org.wso2.carbon.analytics.activitydashboard.commons.ExpressionNode;
import org.wso2.carbon.analytics.activitydashboard.commons.Operation;
import org.wso2.carbon.analytics.activitydashboard.commons.Query;
import org.wso2.carbon.analytics.activitydashboard.commons.SearchExpressionTree;
import org.wso2.carbon.analytics.activitydashboard.admin.internal.ServiceHolder;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse.Entry;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.CategoryDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.CategorySearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.exception.AnalyticsIndexException;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.core.AbstractAdmin;

import java.util.*;

/**
 * Admin service for the activity dashboard admin service.
 */
public class ActivityDashboardAdminService extends AbstractAdmin {
    private static final Log logger = LogFactory.getLog(ActivityDashboardAdminService.class);
    private static final String AT_SIGN = "@";

    /**
     * This method is use to get logged in username with tenant domain
     *
     * @return Username with tenant domain
     */
    @Override
    protected String getUsername() {
        return super.getUsername() + AT_SIGN + super.getTenantDomain();
    }

    public String[] getActivities(ActivitySearchRequest activitySearchRequest)
            throws ActivityDashboardException {
        String userName = getUsername();
        Set<String> activityIds;
        CategoryDrillDownRequest categoryDrillDownRequest = new CategoryDrillDownRequest();
        categoryDrillDownRequest.setPath(new String[]{});
        categoryDrillDownRequest.setFieldName(ActivityDashboardConstants.ACTIVITY_ID_FIELD_NAME);
        String timeBasedQuery = getTimeBasedSearchQuery(activitySearchRequest.getFromTime(),
                activitySearchRequest.getToTime());
        ExpressionNode searchRequest = ((SearchExpressionTree)
                GenericUtils.deserializeObject(activitySearchRequest.getSearchTreeExpression())).getRoot();
        if (searchRequest == null) {
            activityIds = searchActivitiesFromAllTables(userName, timeBasedQuery, categoryDrillDownRequest);
        } else {
            activityIds = searchActivities(userName, timeBasedQuery, searchRequest, categoryDrillDownRequest);
        }
        return activityIds.toArray(new String[activityIds.size()]);
    }

    private Set<String> searchActivities(String username, String timeBasedQuery,
                                         ExpressionNode expressionNode,
                                         CategoryDrillDownRequest categoryDrillDownRequest)
            throws ActivityDashboardException {
        if (expressionNode.getLeftExpression() != null) {
            if (!(expressionNode instanceof Operation)) {
                throw new ActivityDashboardException("This node is having an left expression, " +
                        "but the node is not marked as operation!");
            }
            Operation operation = (Operation) expressionNode;
            Set<String> leftNodeActivities = searchActivities(username, timeBasedQuery,
                    expressionNode.getLeftExpression(), categoryDrillDownRequest);
            Set<String> rightNodeActivities = searchActivities(username, timeBasedQuery,
                    expressionNode.getRightExpression(), categoryDrillDownRequest);
            if (operation.getOperator() == Operation.Operator.OR) {
                leftNodeActivities.addAll(rightNodeActivities);
            } else {
                leftNodeActivities.retainAll(rightNodeActivities);
            }
            return leftNodeActivities;
        } else {
            Set<String> activities;
            if (expressionNode instanceof Query) {
                Query query = (Query) expressionNode;
                String completeQuery = getFullSearchQuery(query.getQueryString(), timeBasedQuery);
                activities = getActivityIdsForTable(username, query.getTableName(), completeQuery,
                        categoryDrillDownRequest);
                validateActivityIdsResultLimit(activities.size());
                return activities;
            } else {
                throw new ActivityDashboardException("Invalid search expression provided. " +
                        "The search tree ended with operation type node!");
            }
        }
    }

    private String getFullSearchQuery(String searchQuery, String timeBasedQuery) {
        return "(" + searchQuery + ") AND (" + timeBasedQuery + ")";
    }

    private Set<String> getActivityIdsForTable(String userName, String tableName, String query,
                                               CategoryDrillDownRequest categoryDrillDownRequest)
            throws ActivityDashboardException {
        Set<String> activityIds = new HashSet<>();
        categoryDrillDownRequest.setTableName(tableName);
        categoryDrillDownRequest.setQuery(query);
        try {
            List<CategorySearchResultEntry> searchResultForTable = ServiceHolder.getAnalyticsDataAPI().
                    drillDownCategories(userName, categoryDrillDownRequest).getCategories();
            validateActivityIdsResultLimit(searchResultForTable.size());
            for (CategorySearchResultEntry aSearchResult : searchResultForTable) {
                activityIds.add(aSearchResult.getCategoryValue());
                validateActivityIdsResultLimit(activityIds.size());
            }
            return activityIds;
        } catch (AnalyticsIndexException e) {
            String errorMsg = "Error while fetching the activities for username : " + userName + ", table name : "
                    + tableName + ", query : " + query;
            logger.error(errorMsg, e);
            throw new ActivityDashboardException(errorMsg, e);
        }
    }

    private Set<String> searchActivitiesFromAllTables(String userName, String timeBasedQuery,
                                                      CategoryDrillDownRequest categoryDrillDownRequest)
            throws ActivityDashboardException {
        Set<String> activityIds = new HashSet<>();
        try {
            List<String> allTableNames = ServiceHolder.getAnalyticsDataAPI().listTables(userName);
            for (String tableName : allTableNames) {
                activityIds.addAll(getActivityIdsForTable(userName, tableName, timeBasedQuery
                        , categoryDrillDownRequest));
            }
        } catch (AnalyticsException e) {
            String errorMsg = "Error while fetching the activities from in time: " + timeBasedQuery + ".";
            logger.error(errorMsg, e);
            throw new ActivityDashboardException(errorMsg, e);
        }
        return activityIds;
    }

    private String getTimeBasedSearchQuery(long fromTime, long toTime) {
        return ActivityDashboardConstants.TIMESTAMP_FIELD + ":[" + fromTime + " TO " + toTime + "]";
    }

    private void validateActivityIdsResultLimit(long currentSize) throws ActivityDashboardException {
        if (currentSize > ActivityDashboardConstants.MAX_ACTIVITY_ID_LIMIT) {
            throw new ActivityDashboardException("Search result exceeded maximum limit: " +
                    ActivityDashboardConstants.MAX_ACTIVITY_ID_LIMIT);
        }
    }

    public RecordBean getRecord(RecordId id) throws ActivityDashboardException {
        String userName = getUsername();
        List<String> recordIds = new ArrayList<>();
        recordIds.add(id.getRecordId());
        try {
            AnalyticsDataResponse resp = ServiceHolder.getAnalyticsDataAPI().get(userName, id.getTableName(),
                    1, null, recordIds);
            List<Entry> entries = resp.getEntries();
            if (entries.size() == 1) {
                Iterator<Record> recordIterator = ServiceHolder.getAnalyticsDataAPI().readRecords(
                        entries.get(0).getRecordStoreName(), entries.get(0).getRecordGroup());
                if (recordIterator.hasNext()) {
                    Record record = recordIterator.next();
                    return getRecordBean(record);
                } else {
                    throw new ActivityDashboardException("No record found for the record id : "
                            + id.getRecordId() + ", at table : " + id.getTableName());
                }
            } else {
                throw new ActivityDashboardException("Invalid size : " + entries.size() +
                        " of record groups found for the record id : "
                        + id.getRecordId() + ", at table : " + id.getTableName());
            }
        } catch (AnalyticsException e) {
            throw new ActivityDashboardException("Error while trying to fetch record " + id.getFullQualifiedId(), e);
        }
    }

    public RecordId[] getRecordIds(String activityId, String[] searchTableNames) throws ActivityDashboardException {
        List<RecordId> allRecordIds = new ArrayList<>();
        String userName = getUsername();
        AnalyticsDrillDownRequest analyticsdrillDownReq = new AnalyticsDrillDownRequest();
        Map<String, List<String>> categoryPath = new HashMap<>();
        List<String> activityIdPath = new ArrayList<>();
        activityIdPath.add(activityId);
        categoryPath.put(ActivityDashboardConstants.ACTIVITY_ID_FIELD_NAME, activityIdPath);
        analyticsdrillDownReq.setCategoryPaths(categoryPath);
        analyticsdrillDownReq.setTableName(ActivityDashboardConstants.ACTIVITY_ID_FIELD_NAME);
        analyticsdrillDownReq.setRecordCount(ActivityDashboardConstants.MAX_RECORD_COUNT_LIMIT);
        if (searchTableNames.length == 0) {
            searchTableNames = getAllTables();
        }
        for (String aTable : searchTableNames) {
            analyticsdrillDownReq.setTableName(aTable);
            try {
                List<SearchResultEntry> searchResultForTable
                        = ServiceHolder.getAnalyticsDataAPI().drillDownSearch(userName, analyticsdrillDownReq);
                RecordId id;
                for (SearchResultEntry searchResultEntry : searchResultForTable) {
                    id = new RecordId();
                    id.setRecordId(searchResultEntry.getId());
                    id.setTableName(aTable);
                    allRecordIds.add(id);
                }
            } catch (AnalyticsIndexException e) {
                logger.error("Error while fetching records from table : " + aTable + " for activity id : "
                        + activityId, e);
            }
        }
        return allRecordIds.toArray(new RecordId[allRecordIds.size()]);
    }

    public String[] getAllTables() throws ActivityDashboardException {
        String userName = getUsername();
        try {
            List<String> tablesList = ServiceHolder.getAnalyticsDataAPI().listTables(userName);
            return tablesList.toArray(new String[tablesList.size()]);
        } catch (AnalyticsException e) {
            String errorMsg = "Error while listing the tables from analytics store for user : " + userName;
            logger.error(errorMsg, e);
            throw new ActivityDashboardException(errorMsg, e);
        }
    }

    private RecordBean getRecordBean(Record record) {
        RecordBean recordBean = new RecordBean();
        recordBean.setTimeStamp(record.getTimestamp());
        Map<String, Object> columns = record.getNotNullValues();
        ColumnEntry[] columnEntries = new ColumnEntry[columns.size()];
        int columnEntryIndex = 0;
        ColumnEntry columnEntry;
        for (Map.Entry<String, Object> aColumn : columns.entrySet()) {
            columnEntry = new ColumnEntry();
            columnEntry.setName(aColumn.getKey());
            columnEntry.setValue(String.valueOf(aColumn.getValue()));
            columnEntries[columnEntryIndex] = columnEntry;
            columnEntryIndex++;
        }
        recordBean.setColumnEntries(columnEntries);
        return recordBean;
    }
}

