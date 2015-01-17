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

package org.wso2.carbon.analytics.dataservice.restapi.resources;

import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.indexing.IndexType;
import org.wso2.carbon.analytics.dataservice.indexing.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.restapi.AnalyticsRESTException;
import org.wso2.carbon.analytics.dataservice.restapi.Utils;
import org.wso2.carbon.analytics.dataservice.restapi.beans.IndexTypeBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.QueryBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.RecordBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.RecordGroupBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.SearchResultEntryBean;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.Record;
import org.wso2.carbon.analytics.datasource.core.RecordGroup;

/**
 * The Class AnalyticsResource represents the REST APIs for AnalyticsDataService.
 */
@Path("/")
@Consumes({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML })
@Produces(MediaType.APPLICATION_JSON)
public class AnalyticsResource extends AbstractResource {

	/** The logger. */
	private Log logger = LogFactory.getLog(AnalyticsResource.class);
	
	/** The analytics data service. */
	AnalyticsDataService analyticsDataService;

	/**
	 * Instantiates a new analytics resource.
	 * @throws AnalyticsRESTException the analytics rest exception
	 */
	public AnalyticsResource() throws AnalyticsRESTException {
		analyticsDataService = Utils.getAnalyticsDataService();
		if (analyticsDataService == null) {
			throw new AnalyticsRESTException(
			                                 "AnalyticsRESTException occurred. AnalyticsDataService is null");
		}
	}

	/**
	 * Creates the table.
	 * @param queryBean the query bean
	 * @return the response
	 */
	@POST
	@Path("tables")
	public Response createTable(QueryBean queryBean) {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking createTable for tenantId :" + queryBean.getTenantId() +
			             " tableName : " + queryBean.getTableName());
		}
		try {
			analyticsDataService.createTable(queryBean.getTenantId(), queryBean.getTableName());
			return handleResponse(ResponseStatus.SUCCESS,
			                      "Successfully created table: " + queryBean.getTableName() +
			                              " for tenantId: " + queryBean.getTenantId());
		} catch (AnalyticsException e) {
			String message =
			                 "Error while creating table: " + queryBean.getTableName() +
			                         " tenantId: " + queryBean.getTenantId();
			message = Utils.getCompleteErrorMessage(message, e);
			logger.error(message, e);
			return handleResponse(ResponseStatus.FAILED, message);
		}
	}

	/**
	 * Checks if a table exist.
	 *
	 * @param tenantId the tenant id
	 * @param tableName the table name
	 * @return the response
	 */
	@GET
	@Path("tables")
	public Response tableExist(@QueryParam("tenantId") int tenantId,
	                           @QueryParam("tableName") String tableName) {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking tableExist for tenantId :" + tenantId + " tableName : " +
			             tableName);
		}
		try {
			boolean tableExists = analyticsDataService.tableExists(tenantId, tableName);
			return Response.ok(tableExists).build();
		} catch (AnalyticsException e) {
			String message = "Error while checking table: " + tableName + " tenantId: " + tenantId;
			message = Utils.getCompleteErrorMessage(message, e);
			logger.error(message, e);
			return handleResponse(ResponseStatus.FAILED, message);
		}
	}

	/**
	 * Delete table.
	 * @param queryBean the query bean
	 * @return the response
	 */
	@DELETE
	@Path("tables")
	public Response deleteTable(QueryBean queryBean) {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking deleteTable for tenantId :" + queryBean.getTenantId() +
			             " tableName : " + queryBean.getTableName());
		}
		try {
			analyticsDataService.deleteTable(queryBean.getTenantId(), queryBean.getTableName());
			return handleResponse(ResponseStatus.SUCCESS,
			                      "Successfully deleted table: " + queryBean.getTableName() +
			                              " for tenantId: " + queryBean.getTenantId());
		} catch (AnalyticsException e) {
			String message =
			                 "Error while deleting table: " + queryBean.getTableName() +
			                         " tenantId: " + queryBean.getTenantId();
			message = Utils.getCompleteErrorMessage(message, e);
			logger.error(message, e);
			return handleResponse(ResponseStatus.FAILED, message);
		}
	}

	/**
	 * Delete records.
	 * @param tableName the table name
	 * @param tenantId the tenant id
	 * @param timeFrom the time from
	 * @param timeTo the time to
	 * @param ids the ids
	 * @return the response
	 */
	@DELETE
	@Path("tables/{tableName}/records")
	public Response deleteRecords(@PathParam("tableName") String tableName,
	                              @QueryParam("tenantId") int tenantId,
	                              @QueryParam("timeFrom") long timeFrom,
	                              @QueryParam("timeTo") long timeTo,
	                              @QueryParam("ids") List<String> ids) {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking deleteRecords for tenantId :" + tenantId + " tableName : " +
			             tableName);
		}
		try {
			if (ids == null) {
				analyticsDataService.delete(tenantId, tableName, timeFrom, timeTo);
			} else {
				analyticsDataService.delete(tenantId, tableName, ids);
			}
			return handleResponse(ResponseStatus.SUCCESS,
			                      "Successfully deleted records in table: " + tableName +
			                              " for tenantId: " + tenantId);
		} catch (AnalyticsException e) {
			String message =
			                 "Error while deleting recods in table: " + tableName + " tenantId: " +
			                         tenantId;
			message = Utils.getCompleteErrorMessage(message, e);
			logger.error(message, e);
			return handleResponse(ResponseStatus.FAILED, message);
		}
	}

	/**
	 * List tables.
	 * @param tenantId the tenant id
	 * @return the response
	 */
	@GET
	@Path("tables/all")
	public Response listTables(@QueryParam("tenantId") int tenantId) {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking listTables for tenantId :" + tenantId);
		}
		try {
			List<String> tables = analyticsDataService.listTables(tenantId);
			return Response.ok(tables).build();
		} catch (AnalyticsException e) {
			String message = "Error while listing tables for tenantId: " + tenantId;
			message = Utils.getCompleteErrorMessage(message, e);
			logger.error(message, e);
			return handleResponse(ResponseStatus.FAILED, message);
		}
	}

	/**
	 * Gets the record count.
	 * @param tableName the table name
	 * @param tenantId the tenant id
	 * @return the record count
	 */
	@GET
	@Path("tables/{tableName}/count")
	public Response getRecordCount(@PathParam("tableName") String tableName,
	                               @QueryParam("tenantId") int tenantId) {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking getRecordCount for tableName: " + tableName + " tenantId :" +
			             tenantId);
		}
		try {
			long recordCount = analyticsDataService.getRecordCount(tenantId, tableName);
			return Response.ok(recordCount).build();
		} catch (AnalyticsException e) {
			String message =
			                 "Error while retrieving record count for tableName: " + tableName +
			                         " tenantId: " + tenantId;
			message = Utils.getCompleteErrorMessage(message, e);
			logger.error(message, e);
			return handleResponse(ResponseStatus.FAILED, message);
		}
	}

	/**
	 * Gets the record groups.
	 * @param tableName the table name
	 * @param tenantId the tenant id
	 * @param columns the columns
	 * @param ids the ids
	 * @param timeFrom the start time
	 * @param timeTo the end time
	 * @param recordsFrom the starting record
	 * @param count the count
	 * @return the record groups
	 */
	@GET
	@Path("tables/{tableName}/recordgroups")
	public Response getRecordGroups(@PathParam("tableName") String tableName,
	                                @QueryParam("tenantId") int tenantId,
	                                @QueryParam("columns") List<String> columns,
	                                @QueryParam("ids") List<String> ids,
	                                @QueryParam("from") long timeFrom,
	                                @QueryParam("to") long timeTo,
	                                @QueryParam("start") int recordsFrom,
	                                @QueryParam("count") int count) {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking getRecordGroups for tableName: " + tableName + " tenantId :" +
			             tenantId);
		}
		try {
			RecordGroup[] recordGroups;
			if (ids == null) {
				recordGroups =
				               analyticsDataService.get(tenantId, tableName, columns, timeFrom,
				                                        timeTo, recordsFrom, count);
			} else {
				recordGroups = analyticsDataService.get(tenantId, tableName, columns, ids);
			}
			List<RecordGroupBean> recordGroupBeans =
			                                         Utils.createRecordGroupBeansFromRecordGroups(recordGroups);
			return Response.ok(recordGroupBeans).build();
		} catch (AnalyticsException e) {
			String message =
			                 "Error while retrieving recordgroups for tableName: " + tableName +
			                         " tenantId: " + tenantId;
			message = Utils.getCompleteErrorMessage(message, e);
			logger.error(message, e);
			return handleResponse(ResponseStatus.FAILED, message);
		}
	}

	/**
	 * Inserts a list of records.
	 * @param recordBeans the list of the record beans
	 * @return the response
	 */
	@POST
	@Path("records")
	public Response insert(List<RecordBean> recordBeans) {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking addrecords");
		}
		try {
			List<Record> records = Utils.getRecordsFromRecordBeans(recordBeans);
			analyticsDataService.insert(records);
			return handleResponse(ResponseStatus.SUCCESS, "Successfully added records");
		} catch (AnalyticsException e) {
			String message = "Error while adding records";
			message = Utils.getCompleteErrorMessage(message, e);
			logger.error(message, e);
			return handleResponse(ResponseStatus.FAILED, message);
		}
	}

	/**
	 * Updates a list of records.
	 * @param recordBeans the record beans
	 * @return the response
	 */
	@PUT
	@Path("records")
	public Response update(List<RecordBean> recordBeans) {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking updaterecords");
		}
		try {
			List<Record> records = Utils.getRecordsFromRecordBeans(recordBeans);
			analyticsDataService.update(records);
			return handleResponse(ResponseStatus.SUCCESS, "Successfully updated records");
		} catch (AnalyticsException e) {
			String message = "Error while updating records";
			message = Utils.getCompleteErrorMessage(message, e);
			logger.error(message, e);
			return handleResponse(ResponseStatus.FAILED, message);
		}
	}

	/**
	 * Sets the indices.
	 * @param tableName the table name
	 * @param tenantId the tenant id
	 * @param columnsBean the columns bean containing all the indices
	 * @return the response
	 */
	@POST
	@Path("tables/{tableName}/indices")
	public Response setIndices(@PathParam("tableName") String tableName,
	                           @QueryParam("tenantId") int tenantId,
	                           Map<String, IndexTypeBean> columnsBean) {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking setIndices for tenantId :" + tenantId + " tableName : " +
			             tableName);
		}
		try {
			Map<String, IndexType> columns =
			                                 Utils.createIndexTypeMapFronIndexTypeBeanMap(columnsBean);
			analyticsDataService.setIndices(tenantId, tableName, columns);
			return handleResponse(ResponseStatus.SUCCESS, "Successfully set indices in table: " +
			                                              tableName + " for tenantId: " + tenantId);
		} catch (AnalyticsException e) {
			String message =
			                 "Error while setting indices in table: " + tableName + " tenantId: " +
			                         tenantId;
			message = Utils.getCompleteErrorMessage(message, e);
			logger.error(message, e);
			return handleResponse(ResponseStatus.FAILED, message);
		}
	}

	/**
	 * Gets the indices.
	 * @param tableName the table name
	 * @param tenantId the tenant id
	 * @return the indices
	 */
	@GET
	@Path("tables/{tableName}/indices")
	public Response getIndices(@PathParam("tableName") String tableName,
	                           @QueryParam("tenantId") int tenantId) {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking getIndices for tenantId :" + tenantId + " tableName : " +
			             tableName);
		}
		try {
			Map<String, IndexType> columns = analyticsDataService.getIndices(tenantId, tableName);
			Map<String, IndexTypeBean> columnsBean =
			                                         Utils.createIndexTypeBeanMapFronIndexTypeMap(columns);
			return Response.ok(columnsBean).build();
		} catch (AnalyticsException e) {
			String message =
			                 "Error while getting indices in table: " + tableName + " tenantId: " +
			                         tenantId;
			message = Utils.getCompleteErrorMessage(message, e);
			logger.error(message, e);
			return handleResponse(ResponseStatus.FAILED, message);
		}
	}

	/**
	 * Clear indices.
	 * @param tableName the table name
	 * @param tenantId the tenant id
	 * @return the response
	 */
	@DELETE
	@Path("tables/{tableName}/indices")
	public Response clearIndices(@PathParam("tableName") String tableName,
	                             @QueryParam("tenantId") int tenantId) {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking clearIndices for tenantId :" + tenantId + " tableName : " +
			             tableName);
		}
		try {
			analyticsDataService.clearIndices(tenantId, tableName);
			return handleResponse(ResponseStatus.SUCCESS,
			                      "Successfully cleared indices in table: " + tableName +
			                              " for tenantId: " + tenantId);
		} catch (AnalyticsException e) {
			String message =
			                 "Error while clearing indices in table: " + tableName + " tenantId: " +
			                         tenantId;
			message = Utils.getCompleteErrorMessage(message, e);
			logger.error(message, e);
			return handleResponse(ResponseStatus.FAILED, message);
		}
	}

	/**
	 * Search records
	 * @param queryBean the query bean
	 * @return the response
	 */
	@POST
	@Path("search")
	public Response search(QueryBean queryBean) {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking search for tenantId :" + queryBean.getTenantId() +
			             " tableName : " + queryBean.getTableName());
		}
		try {
			List<SearchResultEntry> searchResults =
			                                        analyticsDataService.search(queryBean.getTenantId(),
			                                                                    queryBean.getTableName(),
			                                                                    queryBean.getLanguage(),
			                                                                    queryBean.getQuery(),
			                                                                    queryBean.getStart(),
			                                                                    queryBean.getCount());
			List<SearchResultEntryBean> searchResultsBean =
			                                                Utils.createSearchResultBeansFromSearchResults(searchResults);
			return Response.ok(searchResultsBean).build();
		} catch (AnalyticsException e) {
			String message =
			                 "Error while searching table: " + queryBean.getTableName() +
			                         " tenantId: " + queryBean.getTenantId();
			message = Utils.getCompleteErrorMessage(message, e);
			logger.error(message, e);
			return handleResponse(ResponseStatus.FAILED, message);
		}
	}
}
