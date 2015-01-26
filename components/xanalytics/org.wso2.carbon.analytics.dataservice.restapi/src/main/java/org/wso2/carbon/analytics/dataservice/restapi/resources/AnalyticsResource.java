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
	 *
	 * @param tableName the table name
	 * @return the response
	 */
	@POST
	@Path("{tableName}")
	public Response createTable(@PathParam("tableName")String tableName) {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking createTable for tenantId :" + -1234 +
			             " tableName : " + tableName);
		}
		try {
			analyticsDataService.createTable(tenantId, tableName);
			return handleResponse(ResponseStatus.SUCCESS,
			                      "Successfully created table: " + tableName +
			                              " for tenantId: " + tenantId);
		} catch (AnalyticsException e) {
			String message =
			                 "Error while creating table: " + tableName +
			                         " tenantId: " + tenantId;
			message = Utils.getCompleteErrorMessage(message, e);
			logger.error(message, e);
			return handleResponse(ResponseStatus.FAILED, message);
		}
	}

	/**
	 * Checks if a table exist.
	 *
	 * @param tableName the table name
	 * @return the response
	 */
	@GET
	@Path("{tableName}")
	public Response tableExist(@PathParam("tableName") String tableName) {
		int tenantId = -1234;
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
	 *
	 * @param tableName the table name
	 * @return the response
	 */
	@DELETE
	@Path("{tableName}")
	public Response deleteTable(@PathParam("tableName")String tableName) {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking deleteTable for tenantId :" + -1234 +
			             " tableName : " + tableName);
		}
		try {
			analyticsDataService.deleteTable(-1234, tableName);
			return handleResponse(ResponseStatus.SUCCESS,
			                      "Successfully deleted table: " + tableName +
			                              " for tenantId: " + -1234);
		} catch (AnalyticsException e) {
			String message =
			                 "Error while deleting table: " + tableName +
			                         " tenantId: " + -1234;
			message = Utils.getCompleteErrorMessage(message, e);
			logger.error(message, e);
			return handleResponse(ResponseStatus.FAILED, message);
		}
	}

	/**
	 * Delete records.
	 *
	 * @param tableName the table name
	 * @param timeFrom the time from
	 * @param timeTo the time to
	 * @param ids the ids
	 * @return the response
	 */
	@DELETE
	@Path("records/{tableName}/{timeFrom}/{timeTo}")
	public Response deleteRecords(@PathParam("tableName") String tableName,
	                              @PathParam("timeFrom") long timeFrom,
	                              @PathParam("timeTo") long timeTo,
	                              List<String> ids) {
		int tenantId = -1234;
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
	 * List all the tables.
	 *
	 * @param tenantI the tenant i
	 * @return the response
	 */
	@GET
	@Path("tables")
	public Response listTables(@QueryParam("tenantId") int tenantI) {
		int tenantId = -1234;
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
	 *
	 * @param tableName the table name
	 * @param tenantI the tenant i
	 * @return the record count
	 */
	@GET
	@Path("count/{tableName}")
	public Response getRecordCount(@PathParam("tableName") String tableName,
	                               @QueryParam("tenantId") int tenantI) {
		int tenantId = -1234;
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
	 * Gets the records.
	 *
	 * @param tableName the table name
	 * @param timeFrom the start time
	 * @param timeTo the end time
	 * @param recordsFrom the starting record
	 * @param count the count
	 * @return the record groups
	 */
	@GET
	@Path("records/{tableName}/{from}/{to}/{start}/{count}")
	public Response getRecords(@PathParam("tableName") String tableName,
	                                @PathParam("from") long timeFrom,
	                                @PathParam("to") long timeTo,
	                                @PathParam("start") int recordsFrom,
	                                @PathParam("count") int count) {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking getRecordGroups for tableName: " + tableName + " tenantId :" +
			             tenantId);
		}
		try {
			RecordGroup[] recordGroups;
			recordGroups = analyticsDataService.get(tenantId, tableName, null, timeFrom,
				                                        timeTo, recordsFrom, count);
			List<RecordBean> recordBeans = Utils.getAllRecordBeansFromRecordGroups(analyticsDataService, 
			                                                                       recordGroups);
			return Response.ok(recordBeans).build();
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
	 * Gets the records.
	 * @param tableName the table name
	 * @param timeFrom the time from
	 * @param timeTo the time to
	 * @return the records
	 */
	@GET
	@Path("records/{tableName}/{from}/{to}")
	public Response getRecords(@PathParam("tableName") String tableName,
	                           @PathParam("from") long timeFrom, @PathParam("to") long timeTo) {
		return getRecords(tableName, timeFrom, timeTo, 0, -1);
	}
	
	/**
	 * Gets all the records.
	 * @param tableName the table name
	 * @return the records
	 */
	@GET
	@Path("records/{tableName}")
	public Response getRecords(@PathParam("tableName") String tableName) {
		return getRecords(tableName, -1, -1, 0, -1);
	}

	/**
	 * Gets the records, But this is a POST request, since we have to send ids in the content.
	 *
	 * @param tableName the table name
	 * @param ids the ids
	 * @return the records by ids
	 */
	@POST
	@Path("records/{tableName}")
	public Response getRecordsByIds(@PathParam("tableName") String tableName,
	                                List<String> ids) {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking getRecordsByIds for tableName: " + tableName + " tenantId :" +
			             tenantId);
		}
		try {
			RecordGroup[] recordGroups;
			recordGroups = analyticsDataService.get(tenantId, tableName, null, ids);
			List<RecordBean> recordBeans = Utils.getAllRecordBeansFromRecordGroups(analyticsDataService, 
			                                                                       recordGroups);
			return Response.ok(recordBeans).build();
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
	public Response insertRecords(List<RecordBean> recordBeans) {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking insertRecords");
		}
		int tenantId = -1234;
		try {
			List<Record> records = Utils.getRecordsFromRecordBeans(tenantId, recordBeans);
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
	public Response updateRecords(List<RecordBean> recordBeans) {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking updateRecords");
		}
		int tenantId = -1234;
		try {
			List<Record> records = Utils.getRecordsFromRecordBeans(tenantId, recordBeans);
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
	 *
	 * @param tableName the table name
	 * @param tenantI the tenant i
	 * @param columnsBean the columns bean containing all the indices
	 * @return the response
	 */
	@POST
	@Path("indices/{tableName}")
	public Response setIndices(@PathParam("tableName") String tableName,
	                           @QueryParam("tenantId") int tenantI,
	                           Map<String, IndexTypeBean> columnsBean) {
		int tenantId = -1234;
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
	 *
	 * @param tableName the table name
	 * @param tenantI the tenant i
	 * @return the indices
	 */
	@GET
	@Path("indices/{tableName}")
	public Response getIndices(@PathParam("tableName") String tableName,
	                           @QueryParam("tenantId") int tenantI) {
		int tenantId = -1234;
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
	 *
	 * @param tableName the table name
	 * @param tenantI the tenant i
	 * @return the response
	 */
	@DELETE
	@Path("indices/{tableName}")
	public Response clearIndices(@PathParam("tableName") String tableName,
	                             @QueryParam("tenantId") int tenantI) {
		int tenantId = -1234;
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
	 * Search records.
	 *
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
		int tenantId = -1234;
		try {
			List<SearchResultEntry> searchResults =
			                                        analyticsDataService.search(tenantId,
			                                                                    queryBean.getTableName(),
			                                                                    queryBean.getLanguage(),
			                                                                    queryBean.getQuery(),
			                                                                    queryBean.getStart(),
			                                                                    queryBean.getCount());
			List<String> ids = Utils.getRecordIdsFromSearchResults(searchResults);
			RecordGroup[] recordGroups = analyticsDataService.get(-1234, queryBean.getTableName(), null, ids);
			List<RecordBean> recordBeans = Utils.getAllRecordBeansFromRecordGroups(analyticsDataService, 
			                                                                       recordGroups);
			return Response.ok(recordBeans).build();
		} catch (AnalyticsException e) {
			String message =
			                 "Error while searching table: " + queryBean.getTableName() +
			                         " tenantId: " + tenantId;
			message = Utils.getCompleteErrorMessage(message, e);
			logger.error(message, e);
			return handleResponse(ResponseStatus.FAILED, message);
		}
	}
	
	/**
     * Returns the search record count.
     *
     * @param queryBean the query bean
     * @return the response
     */
    @POST
    @Path("search_count")
    public Response searchCount(QueryBean queryBean) {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking search count for tenantId :" + queryBean.getTenantId() +
                         " tableName : " + queryBean.getTableName());
        }
        int tenantId = -1234;
        try {
            int result = analyticsDataService.searchCount(tenantId, queryBean.getTableName(), queryBean.getLanguage(),
                    queryBean.getQuery());
            return Response.ok(result).build();
        } catch (AnalyticsException e) {
            String message = "Error in search count; table: " + queryBean.getTableName() + " tenantId: "
                    + tenantId;
            message = Utils.getCompleteErrorMessage(message, e);
            logger.error(message, e);
            return handleResponse(ResponseStatus.FAILED, message);
        }
    }
	
}
