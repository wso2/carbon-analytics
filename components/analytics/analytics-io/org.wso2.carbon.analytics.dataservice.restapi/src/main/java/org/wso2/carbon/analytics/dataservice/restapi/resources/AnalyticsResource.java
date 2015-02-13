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
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.AnalyticsDSUtils;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.indexing.IndexType;
import org.wso2.carbon.analytics.dataservice.indexing.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.restapi.Utils;
import org.wso2.carbon.analytics.dataservice.restapi.beans.IndexTypeBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.QueryBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.RecordBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.TableBean;
import org.wso2.carbon.analytics.dataservice.restapi.http.methods.PATCH;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.Record;
import org.wso2.carbon.analytics.datasource.core.RecordGroup;

/**
 * The Class AnalyticsResource represents the REST APIs for AnalyticsDataService.
 */
@Path("/")
@Consumes({ MediaType.APPLICATION_JSON})
@Produces({ MediaType.APPLICATION_JSON}) 
public class AnalyticsResource extends AbstractResource {

	private static final int DEFAULT_START_INDEX = 0;
	private static final int DEFAULT_INFINITY_INDEX = -1;
	/** The logger. */
	private Log logger = LogFactory.getLog(AnalyticsResource.class);

	/**
	 * Creates the table.
	 * @param tableName the table name
	 * @return the response
	 */
	@POST
	@Path("tables")
	public Response createTable(TableBean tableBean) {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking createTable for tenantId :" + -1234 +
			             " tableName : " + tableBean.getTableName());
		}
		try {
			AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
			if(analyticsDataService.tableExists(tenantId, tableBean.getTableName())) {
				return handleResponse(ResponseStatus.CONFLICT, "table :" + tableBean.getTableName() + 
				                      " already exists");
			}
			analyticsDataService.createTable(tenantId, tableBean.getTableName());
			return handleResponse(ResponseStatus.CREATED,
			                      "Successfully created table: " + tableBean.getTableName() +
			                              " for tenantId: " + tenantId);
		} catch (AnalyticsException e) {
			String message = "Error while creating table: " + tableBean.getTableName() +
			                         " tenantId: " + tenantId;
			message = Utils.getCompleteErrorMessage(message, e);
			logger.error(message, e);
			return handleResponse(ResponseStatus.FAILED, message);
		}
	}

	/**
	 * List all the tables.
	 * @return the response
	 */
	@GET
	@Path("tables")
	public Response listTables() {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking listTables for tenantId :" + tenantId);
		}
		try {
			AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
			List<String> tables = analyticsDataService.listTables(tenantId);
			if (logger.isDebugEnabled()) {
				logger.debug("Table List : " + tables);
			}
			return Response.ok(tables).build();
		} catch (AnalyticsException e) {
			String message = "Error while listing tables for tenantId: " + tenantId;
			message = Utils.getCompleteErrorMessage(message, e);
			logger.error(message, e);
			return handleResponse(ResponseStatus.FAILED, message);
		}
	}

	/**
	 * Checks if a table exist.
	 * @param tableName the table name
	 * @return the response
	 */
	@GET
	@Path("table_exists")
	public Response tableExist(@QueryParam("tableName") String tableName) {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking tableExist for tenantId :" + tenantId + " tableName : " +
			             tableName);
		}
		try {
			AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
			boolean tableExists = analyticsDataService.tableExists(tenantId, tableName);
			if(tableExists) {
				return handleResponse(ResponseStatus.SUCCESS,
				                      "table :" + tableName + " exists!");
			} else {
				return handleResponse(ResponseStatus.NON_EXISTENT,
						                      "table: " + tableName +
                        " does not exists." );
			}
		} catch (AnalyticsException e) {
			String message = "Error while checking table: " + tableName + " tenantId: " + tenantId;
			message = Utils.getCompleteErrorMessage(message, e);
			logger.error(message, e);
			return handleResponse(ResponseStatus.FAILED, message);
		}
	}

	/**
	 * Delete table.
	 * @param tableName the table name
	 * @return the response
	 */
	@DELETE
	@Path("tables")
	public Response deleteTable(TableBean tableBean) {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking deleteTable for tenantId :" + tenantId +
			             " tableName : " + tableBean.getTableName());
		}
		try {
			AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
			if(analyticsDataService.tableExists(tenantId, tableBean.getTableName())){
				analyticsDataService.deleteTable(-1234, tableBean.getTableName());
				return handleResponse(ResponseStatus.SUCCESS,
				                      "Successfully deleted table: " + tableBean.getTableName() +
				                              " for tenantId: " + -1234);
			}
			return handleResponse(ResponseStatus.NON_EXISTENT,
			                      "table: " + tableBean.getTableName() +
			                              " does not exists." );
			
		} catch (AnalyticsException e) {
			String message =
			                 "Error while deleting table: " + tableBean.getTableName() +
			                         " tenantId: " + -1234;
			message = Utils.getCompleteErrorMessage(message, e);
			logger.error(message, e);
			return handleResponse(ResponseStatus.FAILED, message);
		}
	}

	/**
	 * Delete records either the time range, but not both
	 * @param tableName the table name
	 * @param timeFrom the time from
	 * @param timeTo the time to
	 * @return the response
	 */
	@DELETE
	@Path("tables/{tableName}/{timeFrom}/{timeTo}")
	public Response deleteRecords(@PathParam("tableName") String tableName,
	                              @PathParam("timeFrom") long timeFrom,
	                              @PathParam("timeTo") long timeTo) {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking deleteRecords for tenantId :" + tenantId + " tableName : " +
			             tableName);
		}
		try {
			AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
			if (analyticsDataService.tableExists(tenantId, tableName)) {
				if (logger.isDebugEnabled()) {
					logger.debug("deleting the records from " + timeFrom + " to " + timeTo);
				}
				analyticsDataService.delete(tenantId, tableName, timeFrom, timeTo);
				return handleResponse(ResponseStatus.SUCCESS,
				                      "Successfully deleted records in table: " + tableName +
				                              " for tenantId: " + tenantId);
			}
			return handleResponse(ResponseStatus.NON_EXISTENT, "table: " + tableName +
			                              " does not exists." );
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
	 * Delete records either by ids
	 * @param tableName the table name
	 * @param ids the ids
	 * @return the response
	 */
	@DELETE
	@Path("tables/{tableName}")
	public Response deleteRecordsByIds(@PathParam("tableName") String tableName, List<String> ids) {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking deleteRecords for tenantId :" + tenantId + " tableName : " +
			             tableName);
		}
		try {
			AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
			if (analyticsDataService.tableExists(tenantId, tableName)) {
				if (logger.isDebugEnabled()) {
					logger.debug("deleting the records for ids :" + ids);
				}
				analyticsDataService.delete(tenantId, tableName, ids);
				return handleResponse(ResponseStatus.SUCCESS,
				                      "Successfully deleted records in table: " + tableName +
				                              " for tenantId: " + tenantId);
			}
			return handleResponse(ResponseStatus.NON_EXISTENT, "table: " + tableName +
			                              " does not exists." );
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
	 * Gets the record count.
	 * @param tableName the table name
	 * @return the record count
	 */
	@GET
	@Path("tables/{tableName}/recordcount")
	public Response getRecordCount(@PathParam("tableName") String tableName) {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking getRecordCount for tableName: " + tableName + " tenantId :" +
			             tenantId);
		}
		try {
			AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
			if (analyticsDataService.tableExists(tenantId, tableName)) {
				long recordCount = analyticsDataService.getRecordCount(tenantId, tableName);
				if (logger.isDebugEnabled()) {
					logger.debug("RecordCount for tableName: " + tableName + " is " +
					             recordCount);
				}
				return Response.ok(recordCount).build();
			}
			return handleResponse(ResponseStatus.NON_EXISTENT, "table: " + tableName +
			                                                   " does not exists.");
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
	 * @param tableName the table name
	 * @param timeFrom the start time
	 * @param timeTo the end time
	 * @param recordsFrom the starting record
	 * @param count the count
	 * @return the record groups
	 */
	@GET
	@Path("tables/{tableName}/{from}/{to}/{start}/{count}")
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
			AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
			RecordGroup[] recordGroups;
			if (analyticsDataService.tableExists(tenantId, tableName)) {
				recordGroups = analyticsDataService.get(tenantId, tableName, null, timeFrom,
				                                        timeTo, recordsFrom, count);
				List<RecordBean> recordBeans = Utils
					.createRecordBeans(AnalyticsDSUtils.listRecords(analyticsDataService, recordGroups));
				if (logger.isDebugEnabled()) {
					for(RecordBean recordBean : recordBeans){
						logger.debug("Retrieved -- Record Id: " + recordBean.getId() + " values :" +
					             recordBean.toString());
					}
				}
				return Response.ok(recordBeans).build();
			}
			return handleResponse(ResponseStatus.NON_EXISTENT,
			                      "table: " + tableName + " does not exists." );
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
	@Path("tables/{tableName}/{from}/{to}/{start}")
	public Response getRecords(@PathParam("tableName") String tableName,
	                           @PathParam("from") long timeFrom, @PathParam("to") long timeTo,
	                           @PathParam("start") int start) {
		return getRecords(tableName, timeFrom, timeTo, start, DEFAULT_INFINITY_INDEX);
	}
	
	/**
	 * Gets the records.
	 * @param tableName the table name
	 * @param timeFrom the time from
	 * @param timeTo the time to
	 * @return the records
	 */
	@GET
	@Path("tables/{tableName}/{from}/{to}")
	public Response getRecords(@PathParam("tableName") String tableName,
	                           @PathParam("from") long timeFrom, @PathParam("to") long timeTo) {
		return getRecords(tableName, timeFrom, timeTo, DEFAULT_START_INDEX, DEFAULT_INFINITY_INDEX);
	}
	
	/**
	 * Gets the records.
	 * @param tableName the table name
	 * @param timeFrom the time from
	 * @param timeTo the time to
	 * @return the records
	 */
	@GET
	@Path("tables/{tableName}/{from}")
	public Response getRecords(@PathParam("tableName") String tableName,
	                           @PathParam("from") long timeFrom) {
		return getRecords(tableName, timeFrom, DEFAULT_INFINITY_INDEX, DEFAULT_START_INDEX, DEFAULT_INFINITY_INDEX);
	}
	
	/**
	 * Gets all the records.
	 * @param tableName the table name
	 * @return the records
	 */
	@GET
	@Path("tables/{tableName}")
	public Response getRecords(@PathParam("tableName") String tableName) {
		return getRecords(tableName, DEFAULT_INFINITY_INDEX, DEFAULT_INFINITY_INDEX, 
		                  DEFAULT_START_INDEX, DEFAULT_INFINITY_INDEX); 
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
			AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
			if (logger.isDebugEnabled()) {
				for(RecordBean recordBean : recordBeans){
					logger.debug(" inserting -- Record Id: " + recordBean.getId() + " values :" +
				             recordBean.toString());
				}
			}
			List<Record> records = Utils.getRecords(tenantId, recordBeans);
			analyticsDataService.insert(records);
			return handleResponse(ResponseStatus.CREATED, "Successfully added records");
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
	@PATCH
	@Path("records")
	public Response updateRecords(List<RecordBean> recordBeans) {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking updateRecords");
		}
		int tenantId = -1234;
		try {
			AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
			if (logger.isDebugEnabled()) {
				for(RecordBean recordBean : recordBeans){
					logger.debug(" updating -- Record Id: " + recordBean.getId() + " values :" +
				             recordBean.toString());
				}
			}
			List<Record> records = Utils.getRecords(tenantId, recordBeans);
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
	 * @param columnsBean the columns bean containing all the indices
	 * @return the response
	 */
	@POST
	@Path("tables/{tableName}/indices")
	public Response setIndices(@PathParam("tableName") String tableName,
	                           Map<String, IndexTypeBean> columnsBean) {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking setIndices for tenantId :" + tenantId + " tableName : " +
			             tableName);
		}
		try {
			AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
			if (analyticsDataService.tableExists(tenantId, tableName)) {
				Map<String, IndexType> columns = Utils.createIndexTypeMap(columnsBean);
				if (logger.isDebugEnabled()) {
					logger.debug("Setting indices : " + columns.keySet().toArray() );
				}
				analyticsDataService.setIndices(tenantId, tableName, columns);
				return handleResponse(ResponseStatus.CREATED,
				                      "Successfully set indices in table: " + tableName +
				                              " for tenantId: " + tenantId);
			}
			return handleResponse(ResponseStatus.NON_EXISTENT, "table: " + tableName +
                    " does not exists." );
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
	 * @return the indices
	 */
	@GET
	@Path("tables/{tableName}/indices")
	public Response getIndices(@PathParam("tableName") String tableName) {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking getIndices for tenantId :" + tenantId + " tableName : " +
			             tableName);
		}
		try {
			AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
			if(analyticsDataService.tableExists(tenantId, tableName)){
				Map<String, IndexType> columns = analyticsDataService.getIndices(tenantId, tableName);
				Map<String, IndexTypeBean> columnsBean = Utils.createIndexTypeBeanMap(columns);
				if (logger.isDebugEnabled()) {
					logger.debug("Getting indices : " + columnsBean.keySet().toArray() );
				}
				return Response.ok(columnsBean).build();
			}
			return handleResponse(ResponseStatus.NON_EXISTENT, "table: " + tableName +
                    " does not exists." );
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
	 * @return the response
	 */
	@DELETE
	@Path("tables/{tableName}/indices")
	public Response clearIndices(@PathParam("tableName") String tableName) {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking clearIndices for tenantId :" + tenantId + " tableName : " +
			             tableName);
		}
		try {
			AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
			if(analyticsDataService.tableExists(tenantId, tableName)) {
				analyticsDataService.clearIndices(tenantId, tableName);
				return handleResponse(ResponseStatus.SUCCESS,
			                      "Successfully cleared indices in table: " + tableName +
			                              " for tenantId: " + tenantId);
			}
			return handleResponse(ResponseStatus.NON_EXISTENT, "table: " + tableName +
                    " does not exists." );
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
	 * @param queryBean the query bean
	 * @return the response
	 */
	@POST
	@Path("search")
	public Response search(QueryBean queryBean) {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking search for tenantId :" + queryBean.getTenantId() +
			             " tableName : " + queryBean.getTableName());
		}
		try {
			AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
			List<SearchResultEntry> searchResults =
			                                        analyticsDataService.search(tenantId,
			                                                                    queryBean.getTableName(),
			                                                                    queryBean.getLanguage(),
			                                                                    queryBean.getQuery(),
			                                                                    queryBean.getStart(),
			                                                                    queryBean.getCount());
			List<String> ids = Utils.getRecordIds(searchResults);
			RecordGroup[] recordGroups = analyticsDataService.get(-1234, queryBean.getTableName(), null, ids);
			List<RecordBean> recordBeans = Utils.
					createRecordBeans(AnalyticsDSUtils.listRecords(analyticsDataService, recordGroups));
			if (logger.isDebugEnabled()) {
				for(RecordBean recordBean : recordBeans){
					logger.debug("Search Result -- Record Id: " + recordBean.getId() + " values :" +
				             recordBean.toString());
				}
			}
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
     * @param queryBean the query bean
     * @return the response
     */
    @POST
    @Path("search_count")
    public Response searchCount(QueryBean queryBean) {
    	int tenantId = -1234;
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking search count for tenantId :" + queryBean.getTenantId() +
                         " tableName : " + queryBean.getTableName());
        } 
        try {
        	AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
            int result = analyticsDataService.searchCount(tenantId, queryBean.getTableName(), queryBean.getLanguage(),
                    queryBean.getQuery());
            if (logger.isDebugEnabled()) {
                logger.debug("Search count : " + result);
            } 
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
