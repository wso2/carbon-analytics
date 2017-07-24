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
package org.wso2.carbon.analytics.servlet;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.commons.AggregateField;
import org.wso2.carbon.analytics.dataservice.commons.AggregateRequest;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRange;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.CategoryDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SortByField;
import org.wso2.carbon.analytics.dataservice.commons.SubCategories;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.io.commons.AnalyticsAPIConstants;
import org.wso2.carbon.analytics.servlet.exception.AnalyticsAPIAuthenticationException;
import org.wso2.carbon.analytics.servlet.internal.ServiceHolder;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet to process the analytics search requests and returns the results.
 */
public class AnalyticsSearchProcessor extends HttpServlet {

    private static final long serialVersionUID = 1519852648874078342L;
    private static final Log log = LogFactory.getLog(AnalyticsSearchProcessor.class);
    /**
     * Search the table
     *
     * @param req  HttpRequest which has the required parameters to do the operation.
     * @param resp HttpResponse which returns the result of the intended operation.
     * @throws ServletException
     * @throws IOException
     */
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String sessionId = req.getHeader(AnalyticsAPIConstants.SESSION_ID);
        if (sessionId == null || sessionId.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No session id found, Please login first!");
        } else {
            try {
                ServiceHolder.getAuthenticator().validateSessionId(sessionId);
            } catch (AnalyticsAPIAuthenticationException e) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No session id found, Please login first!");
            }
            String operation = req.getParameter(AnalyticsAPIConstants.OPERATION);
            boolean securityEnabled = Boolean.parseBoolean(req.getParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM));
            int tenantIdParam = MultitenantConstants.INVALID_TENANT_ID;
            if (!securityEnabled)
                tenantIdParam = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.TENANT_ID_PARAM));
            String userName = req.getParameter(AnalyticsAPIConstants.USERNAME_PARAM);
            String tableName = req.getParameter(AnalyticsAPIConstants.TABLE_NAME_PARAM);
            String query = req.getParameter(AnalyticsAPIConstants.QUERY);
            if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.SEARCH_OPERATION)) {
                int start = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.START_PARAM));
                int count = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.COUNT_PARAM));
                Type sortByFieldType = new TypeToken<List<SortByField>>() {}.getType();
                Gson gson = new Gson();
                List<SortByField> sortByFields = gson.fromJson(req.getParameter(AnalyticsAPIConstants.SORT_BY_FIELDS_PARAM), sortByFieldType);
                try {
                    List<SearchResultEntry> searchResult;
                    if (!securityEnabled) searchResult = ServiceHolder.getAnalyticsDataService().search(tenantIdParam,
                            tableName, query, start, count, sortByFields);
                    else
                        searchResult = ServiceHolder.getSecureAnalyticsDataService().search(userName, tableName, query, start, count, sortByFields);
                    //Have to do this because there is possibility of getting sublist which cannot be serialized
                    searchResult = new ArrayList<>(searchResult);
                    resp.getOutputStream().write(GenericUtils.serializeObject(searchResult));
                    resp.setStatus(HttpServletResponse.SC_OK);
                } catch (AnalyticsException e) {
                    resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
                }
            } else if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.SEARCH_COUNT_OPERATION)) {
                try {
                    int count;
                    if (!securityEnabled) count = ServiceHolder.getAnalyticsDataService().searchCount(tenantIdParam,
                            tableName, query);
                    else
                        count = ServiceHolder.getSecureAnalyticsDataService().searchCount(userName, tableName, query);
                    PrintWriter output = resp.getWriter();
                    output.append(AnalyticsAPIConstants.SEARCH_COUNT).append(AnalyticsAPIConstants.SEPARATOR).append(String.valueOf(count));
                    resp.setStatus(HttpServletResponse.SC_OK);
                } catch (AnalyticsException e) {
                    resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
                }
            } else if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.SEARCH_WITH_AGGREGATES_OPERATION)) {
                try {
                    Gson gson = new Gson();
                    AnalyticsIterator<Record> iterator;
                    String groupByField = req.getParameter(AnalyticsAPIConstants.GROUP_BY_FIELD_PARAM);
                    Type aggregateFieldsMapType = new TypeToken<List<AggregateField>>() {}.getType();
                    List<AggregateField> fields = gson.fromJson(req.getParameter(AnalyticsAPIConstants.AGGREGATING_FIELDS),
                                                                aggregateFieldsMapType);
                    String parentPathAsString = req.getParameter(AnalyticsAPIConstants.AGGREGATE_PARENT_PATH);
                    Type aggregateParentPath = new TypeToken<List<String>>(){}.getType();
                    List<String> parentPath = gson.fromJson(parentPathAsString, aggregateParentPath);
                    int aggregateLevel = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.AGGREGATE_LEVEL));
                    int noOfRecords = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.AGGREGATE_NO_OF_RECORDS));
                    AggregateRequest aggregateRequest = new AggregateRequest();
                    aggregateRequest.setTableName(tableName);
                    aggregateRequest.setQuery(query);
                    aggregateRequest.setFields(fields);
                    aggregateRequest.setGroupByField(groupByField);
                    aggregateRequest.setAggregateLevel(aggregateLevel);
                    aggregateRequest.setParentPath(parentPath);
                    aggregateRequest.setNoOfRecords(noOfRecords);
                    if (!securityEnabled) iterator = ServiceHolder.getAnalyticsDataService()
                            .searchWithAggregates(tenantIdParam, aggregateRequest);
                    else
                        iterator = ServiceHolder.getSecureAnalyticsDataService().searchWithAggregates(userName,
                                                                                                               aggregateRequest);
                    while (iterator.hasNext()) {
                        Record record = iterator.next();
                        GenericUtils.serializeObject(record, resp.getOutputStream());
                    }
                    iterator.close();
                    resp.setStatus(HttpServletResponse.SC_OK);
                } catch (AnalyticsException e) {
                    resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
                }
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "unsupported operation performed with get request!");
                log.error("unsupported operation performed : "+ operation + " with get request!");
            }
        }
    }

    /**
     * Send a drill down request and get the results.
     *
     * @param req  HttpRequest which has the required parameters to do the operation.
     * @param resp HttpResponse which returns the result of the intended operation.
     * @throws ServletException
     * @throws IOException
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String sessionId = req.getHeader(AnalyticsAPIConstants.SESSION_ID);
        if (sessionId == null || sessionId.trim().isEmpty()) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No session id found, Please login first!");
        } else {
            try {
                ServiceHolder.getAuthenticator().validateSessionId(sessionId);
            } catch (AnalyticsAPIAuthenticationException e) {
                resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No session id found, Please login first!");
            }
            String operation = req.getParameter(AnalyticsAPIConstants.OPERATION);
            boolean securityEnabled = Boolean.parseBoolean(req.getParameter(AnalyticsAPIConstants.ENABLE_SECURITY_PARAM));
            int tenantIdParam = MultitenantConstants.INVALID_TENANT_ID;
            if (!securityEnabled)
                tenantIdParam = Integer.parseInt(req.getParameter(AnalyticsAPIConstants.TENANT_ID_PARAM));
            String userName = req.getParameter(AnalyticsAPIConstants.USERNAME_PARAM);
            if (operation != null && operation.trim().equalsIgnoreCase(AnalyticsAPIConstants.DRILL_DOWN_SEARCH_OPERATION)) {
                doDrillDownSearch(req, resp, securityEnabled, tenantIdParam, userName);
            } else if (operation != null && operation.trim()
                    .equalsIgnoreCase(AnalyticsAPIConstants.DRILL_DOWN_SEARCH_COUNT_OPERATION)) {
                doDrillDownSearchCount(req, resp, securityEnabled, tenantIdParam, userName);
            } else if (operation != null && operation.trim()
                    .equalsIgnoreCase(AnalyticsAPIConstants.DRILL_DOWN_SEARCH_CATEGORY_OPERATION)) {
                doDrillDownCategories(req, resp, securityEnabled, tenantIdParam, userName);
            } else if (operation != null && operation.trim()
                    .equalsIgnoreCase(AnalyticsAPIConstants.DRILL_DOWN_SEARCH_RANGE_COUNT_OPERATION)) {
                doDrillDownRangeCount(req, resp, securityEnabled, tenantIdParam, userName);
            } else if (operation != null && operation.trim()
                    .equalsIgnoreCase(AnalyticsAPIConstants.SEARCH_MULTITABLES_WITH_AGGREGATES_OPERATION)) {
                doSearchMultiTablesWithAggregates(req, resp, securityEnabled, tenantIdParam, userName);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_ACCEPTABLE, "unsupported operation performed with get request!");
                log.error("unsupported operation performed : "+ operation + " with get request!");
            }
        }
    }

    private void doDrillDownSearch(HttpServletRequest req, HttpServletResponse resp,
                                   boolean securityEnabled, int tenantIdParam, String userName)
            throws IOException {
        ServletInputStream servletInputStream = req.getInputStream();
        try {
            AnalyticsDrillDownRequest drillDownRequest = (AnalyticsDrillDownRequest) GenericUtils.deserializeObject(servletInputStream);
            List<SearchResultEntry> drillDownResult = new ArrayList<>();
            if (!securityEnabled) {
                drillDownResult.addAll(ServiceHolder.getAnalyticsDataService().drillDownSearch(
                        tenantIdParam, drillDownRequest));
            } else {
                drillDownResult.addAll(ServiceHolder.getSecureAnalyticsDataService().drillDownSearch(userName,
                        drillDownRequest));
            }
            GenericUtils.serializeObject(drillDownResult, resp.getOutputStream());
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (AnalyticsException e) {
            resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
        }
    }

    private void doDrillDownSearchCount(HttpServletRequest req, HttpServletResponse resp,
                                        boolean securityEnabled, int tenantIdParam, String userName)
            throws IOException {
        ServletInputStream servletInputStream = req.getInputStream();
        try {
            AnalyticsDrillDownRequest drillDownRequest = (AnalyticsDrillDownRequest) GenericUtils.deserializeObject(servletInputStream);
            double resultCount;
            if (!securityEnabled) {
                resultCount = ServiceHolder.getAnalyticsDataService().drillDownSearchCount(tenantIdParam, drillDownRequest);
            } else {
                resultCount = ServiceHolder.getSecureAnalyticsDataService().drillDownSearchCount(userName, drillDownRequest);
            }
            GenericUtils.serializeObject(resultCount, resp.getOutputStream());
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (AnalyticsException e) {
            resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
        }
    }

    private void doDrillDownCategories(HttpServletRequest req, HttpServletResponse resp,
                                       boolean securityEnabled, int tenantIdParam, String userName)
            throws IOException {
        ServletInputStream servletInputStream = req.getInputStream();
        try {
            CategoryDrillDownRequest drillDownRequest = (CategoryDrillDownRequest) GenericUtils.deserializeObject(servletInputStream);
            SubCategories subCategories;
            if (!securityEnabled) {
                subCategories = ServiceHolder.getAnalyticsDataService().drillDownCategories(tenantIdParam, drillDownRequest);
            } else {
                subCategories = ServiceHolder.getSecureAnalyticsDataService().drillDownCategories(userName, drillDownRequest);
            }
            GenericUtils.serializeObject(subCategories, resp.getOutputStream());
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (AnalyticsException e) {
            resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
        }
    }

    private void doDrillDownRangeCount(HttpServletRequest req, HttpServletResponse resp,
                                       boolean securityEnabled, int tenantIdParam, String userName)
            throws IOException {
        ServletInputStream servletInputStream = req.getInputStream();
        try {
            AnalyticsDrillDownRequest drillDownRequest = (AnalyticsDrillDownRequest) GenericUtils.deserializeObject(servletInputStream);
            List<AnalyticsDrillDownRange> ranges;
            if (!securityEnabled) {
                ranges = ServiceHolder.getAnalyticsDataService().drillDownRangeCount(tenantIdParam, drillDownRequest);
            } else {
                ranges = ServiceHolder.getSecureAnalyticsDataService().drillDownRangeCount(userName, drillDownRequest);
            }
            GenericUtils.serializeObject(ranges, resp.getOutputStream());
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (AnalyticsException e) {
            resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void doSearchMultiTablesWithAggregates(HttpServletRequest req, HttpServletResponse resp,
                                       boolean securityEnabled, int tenantIdParam, String userName)
            throws IOException {
        ServletInputStream servletInputStream = req.getInputStream();
        try {
            AggregateRequest[] requests = (AggregateRequest[]) GenericUtils.deserializeObject(servletInputStream);
            List<AnalyticsIterator<Record>> iterators;
            if (!securityEnabled) {
                iterators = ServiceHolder.getAnalyticsDataService().searchWithAggregates(tenantIdParam, requests);
            } else {
                iterators = ServiceHolder.getSecureAnalyticsDataService().searchWithAggregates(userName, requests);
            }
            List<List<Record>> aggregatedRecords = new ArrayList<>();
            for (AnalyticsIterator<Record> iterator : iterators) {
                aggregatedRecords.add(IteratorUtils.toList(iterator));
            }
            GenericUtils.serializeObject(aggregatedRecords, resp.getOutputStream());
            resp.setStatus(HttpServletResponse.SC_OK);
        } catch (AnalyticsException e) {
            resp.sendError(HttpServletResponse.SC_EXPECTATION_FAILED, e.getMessage());
        }
    }
}
