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

package org.wso2.carbon.analytics.restapi.resources;

import com.google.gson.Gson;
import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.api.AnalyticsDataAPI;
import org.wso2.carbon.analytics.api.AnalyticsDataAPIUtil;
import org.wso2.carbon.analytics.dataservice.commons.AggregateRequest;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDataResponse;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRange;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.CategoryDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.SubCategories;
import org.wso2.carbon.analytics.dataservice.core.AnalyticsDataServiceImpl;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsIterator;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.restapi.Constants;
import org.wso2.carbon.analytics.restapi.UnauthenticatedUserException;
import org.wso2.carbon.analytics.restapi.Utils;
import org.wso2.carbon.analytics.restapi.beans.AggregateRequestBean;
import org.wso2.carbon.analytics.restapi.beans.AnalyticsSchemaBean;
import org.wso2.carbon.analytics.restapi.beans.CategoryDrillDownRequestBean;
import org.wso2.carbon.analytics.restapi.beans.ColumnKeyValueBean;
import org.wso2.carbon.analytics.restapi.beans.DrillDownRangeBean;
import org.wso2.carbon.analytics.restapi.beans.DrillDownRequestBean;
import org.wso2.carbon.analytics.restapi.beans.QueryBean;
import org.wso2.carbon.analytics.restapi.beans.RecordBean;
import org.wso2.carbon.analytics.restapi.beans.SubCategoriesBean;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

/**
 * The Class AnalyticsResource represents the REST APIs for
 * AnalyticsDataService.
 */

@Path(Constants.ResourcePath.ROOT_CONTEXT)
public class AnalyticsResource extends AbstractResource {

    private static final int DEFAULT_START_INDEX = 0;
	private static final int DEFAULT_INFINITY_INDEX = -1;
    private static final long DEFAULT_FROM_TIME = Long.MIN_VALUE;
    private static final long DEFAULT_TO_TIME = Long.MAX_VALUE;
    private static final Gson gson = new Gson();
	/** The logger. */
	private static final Log logger = LogFactory.getLog(AnalyticsResource.class);
    private static final String STR_JSON_ARRAY_OPEN_SQUARE_BRACKET = "[";
    private static final String STR_JSON_COMMA = ",";
    private static final String STR_JSON_ARRAY_CLOSING_SQUARE_BRACKET = "]";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String DEFAULT_CHARSET = "UTF-8";

    /**
     * Implements the OPTIONS HTTP method
     * @return The response
     */
    @OPTIONS
    public Response options() {
        return Response.ok().header(HttpHeaders.ALLOW, "GET POST DELETE").build();
    }
    /**
	 * Creates the table.
	 * @param tableBean the table name as a json object
	 * @return the response
	 * @throws AnalyticsException
	 */
	/*@POST
	@Path(Constants.ResourcePath.TABLES)
	@Consumes({ MediaType.APPLICATION_JSON})
	@Produces({ MediaType.APPLICATION_JSON })
	public Response createTable(TableBean tableBean, @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking createTable tableName : " +
			             tableBean.getTableName());
		}
		AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
        String username = authenticate(authHeader);
        if (tableBean != null) {
            if (analyticsDataService.tableExists(username, tableBean.getTableName())) {
                return handleResponse(ResponseStatus.CONFLICT, "table :" + tableBean.getTableName() +
                                                               " already exists");
            }
            analyticsDataService.createTable(username, tableBean.getTableName());
            return handleResponse(ResponseStatus.CREATED,
                                  "Successfully created table: " + tableBean.getTableName());
        } else {
            throw new AnalyticsException("Table name is not defined");
        }
    }*/

	/**
	 * Check if the table Exists
	 * @return the response
	 * @throws AnalyticsException
	 */
	@GET
	@Path(Constants.ResourcePath.TABLE_EXISTS)
	@Produces({ MediaType.APPLICATION_JSON })
	public Response tableExists(@QueryParam("table")String tableName,
                                @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking tableExists for table: " + tableName);
		}
        if (tableName == null) {
            throw  new AnalyticsException("Query param 'table' is not provided");
        }
        AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
        String username = authenticate(authHeader);
        boolean tableExists = analyticsDataService.tableExists(username, tableName);
        if (logger.isDebugEnabled()) {
            logger.debug("Table's Existance : " + tableExists);
        }
        if (!tableExists) {
            return handleResponse(ResponseStatus.NON_EXISTENT,
                                  "Table : " + tableName + " does not exist.");
        }
        return handleResponse(ResponseStatus.SUCCESS,
                              "Table : " + tableName + " exists.");
    }

    /**
     * Check if the given recordstore support pagination
     * @return the response
     * @throws AnalyticsException
     */
    @GET
    @Path("pagination/{recordStoreName}")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response paginationSupported(@PathParam("recordStoreName") String recordStoreName,
                                @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking paginationSupported for recordStore: " + recordStoreName);
        }
        AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
        boolean paginationSupported = analyticsDataService.isPaginationSupported(recordStoreName);
        if (logger.isDebugEnabled()) {
            logger.debug("pagination support for record store : " + recordStoreName + " : " + paginationSupported);
        }
        if (!paginationSupported) {
            return handleResponse(ResponseStatus.NON_EXISTENT,
                                  "RecordStore : " + recordStoreName + " does not support pagination.");
        }
        return handleResponse(ResponseStatus.SUCCESS,
                              "RecordStore : " + recordStoreName + " support pagination.");
    }

	/**
	 * List all the tables.
	 * @return the response
	 * @throws AnalyticsException
	 */
	@GET
	@Path(Constants.ResourcePath.TABLES)
	@Produces({ MediaType.APPLICATION_JSON })
	public Response listTables(@HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking listTables");
		}
        AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
        String username = authenticate(authHeader);
        List<String> tables = analyticsDataService.listTables(username);
        if (logger.isDebugEnabled()) {
            logger.debug("Table List : " + tables);
        }
        return Response.ok(tables).build();
    }

    /**
     * List all the record stores in the system.
     * @return the response
     * @throws AnalyticsException
     */
    @GET
    @Path(Constants.ResourcePath.RECORDSTORES)
    @Produces({ MediaType.APPLICATION_JSON })
    public Response listRecordStores(@HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking listRecordStores");
        }
        AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
    //    String username = authenticate(authHeader);
        List<String> recordStoreNames = analyticsDataService.listRecordStoreNames();
        if (logger.isDebugEnabled()) {
            logger.debug("Record store List : " + recordStoreNames);
        }
        return Response.ok(recordStoreNames).build();
    }

    /**
     * Get the record store which a table belongs to
     * @return the response
     * @throws AnalyticsException
     */
    @GET
    @Path("recordstore")
    @Produces({ MediaType.APPLICATION_JSON })
    public Response getRecordStoreByTableName(@QueryParam("table") String tableName,
                                     @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking getRecordStoreByTableName for table: " + tableName);
        }
        if (tableName == null) {
            throw new AnalyticsException("Query param 'table' is not provided");
        }
        AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
            String username = authenticate(authHeader);
        String recordStoreName = analyticsDataService.getRecordStoreNameByTable(username, tableName);
        if (logger.isDebugEnabled()) {
            logger.debug("Record store is : " + recordStoreName + " for table: " + tableName);
        }
        return Response.ok(gson.toJson(recordStoreName)).build();
    }

	/**
	 * Delete table.
	 * @param tableBean the table name as a json object
	 * @return the response
	 * @throws AnalyticsException the analytics exception
	 */
	/*@DELETE
	@Path(Constants.ResourcePath.TABLES)
	@Consumes({ MediaType.APPLICATION_JSON})
	@Produces({ MediaType.APPLICATION_JSON })
	public Response deleteTable(TableBean tableBean, @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking deleteTable for tableName : " +
			             tableBean.getTableName());
		}
        AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
        String username = authenticate(authHeader);
        if (tableBean != null) {
            if (analyticsDataService.tableExists(username, tableBean.getTableName())) {
                analyticsDataService.deleteTable(username, tableBean.getTableName());
                return handleResponse(ResponseStatus.SUCCESS, "Successfully deleted table: " +
                                                              tableBean.getTableName());
            }
            return handleResponse(ResponseStatus.NON_EXISTENT, "table: " + tableBean.getTableName() +
                                                               " does not exists.");
        } else {
            throw new AnalyticsException("The table name is empty");
        }
    }*/

	/**
	 * Inserts or update a list of records to a table. updating happens only if there are matching record ids
	 * @param recordBeans the list of the record beans
	 * @return the response
	 * @throws AnalyticsException
	 */
	/*@POST
	@Consumes({ MediaType.APPLICATION_JSON})
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("tables/{tableName}")
	public StreamingOutput insertRecordsToTable(@PathParam("tableName")String tableName, List<RecordBean> recordBeans,
                                                @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
	                                                           throws AnalyticsException {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking insertRecordsToTable");
		}
		AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
        String username = authenticate(authHeader);
        if (recordBeans != null) {
            if (logger.isDebugEnabled()) {
                for (RecordBean recordBean : recordBeans) {
                    logger.debug(" inserting -- Record Id: " + recordBean.getId() + " values :" +
                                 recordBean.toString() + " to table :" + tableName);
                }
            }
            List<Record> records = Utils.getRecordsForTable(username, tableName, recordBeans);
            analyticsDataService.put(username, records);
            final Iterator<Record> recordIterator = records.iterator();
            return new StreamingOutput() {
                @Override
                public void write(OutputStream outputStream)
                        throws IOException, WebApplicationException {
                    Writer recordWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                    recordWriter.write(STR_JSON_ARRAY_OPEN_SQUARE_BRACKET);
                    while (recordIterator.hasNext()) {
                        recordWriter.write(recordIterator.next().getId());
                        if (recordIterator.hasNext()) {
                            recordWriter.write(STR_JSON_COMMA);
                        }
                    }
                    recordWriter.write(STR_JSON_ARRAY_CLOSING_SQUARE_BRACKET);
                    recordWriter.flush();
                }
            };
        } else {
            throw new AnalyticsException("List of records is empty");
        }
    }*/

	/**
	 * Delete records either the time range, but not both
	 * @param tableName  the table name
	 * @param timeFrom the time from
	 * @param timeTo the time to
	 * @return the response
	 * @throws AnalyticsException
	 */
	/*@DELETE
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("tables/{tableName}/{timeFrom}/{timeTo}")
	public Response deleteRecords(@PathParam("tableName") String tableName,
	                              @PathParam("timeFrom") long timeFrom,
	                              @PathParam("timeTo") long timeTo,
                                  @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking deleteRecords for tableName : " +
			             tableName);
		}
		AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
        String username = authenticate(authHeader);
        if (logger.isDebugEnabled()) {
            logger.debug("deleting the records from " + timeFrom + " to " + timeTo);
        }
        analyticsDataService.delete(username, tableName, timeFrom, timeTo);
        return handleResponse(ResponseStatus.SUCCESS, "Successfully deleted records in table: " +
                                                          tableName);
	}
*/
	/**
	 * Delete records either by ids
	 * @param tableName the table name
	 * @param ids the ids
	 * @return the response
	 * @throws AnalyticsException
	 */
	/*@DELETE
	@Consumes({ MediaType.APPLICATION_JSON})
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("tables/{tableName}")
	public Response deleteRecordsByIds(@PathParam("tableName") String tableName, List<String> ids,
                                       @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
	                                          throws AnalyticsException {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking deleteRecords for tableName : " +
			             tableName);
		}
        AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
        String username = authenticate(authHeader);
        if (ids != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("deleting the records for ids :" + ids);
            }
            analyticsDataService.delete(username, tableName, ids);
            return handleResponse(ResponseStatus.SUCCESS, "Successfully deleted records in table: " +
                                                          tableName);
        } else {
            throw new AnalyticsException("list of ids is empty");
        }
    }*/

	/**
	 * Gets the record count.
	 * @param tableName the table name
	 * @return the record count
	 * @throws AnalyticsException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("tables/{tableName}/recordcount")
	public Response getRecordCount(@PathParam("tableName") String tableName,
                                   @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
	                                                                        throws AnalyticsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking getRecordCount for tableName: " + tableName);
        }
        AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
        String username = authenticate(authHeader);
        long recordCount = analyticsDataService.getRecordCount(username, tableName,
                                                               Long.MIN_VALUE, Long.MAX_VALUE);
        if (logger.isDebugEnabled()) {
            logger.debug("RecordCount for tableName: " + tableName + " is " + recordCount);
        }
        return Response.ok(gson.toJson(recordCount)).build();
	}

	/**
	 * Gets the records.
	 * @param tableName the table name
	 * @param timeFrom the start time
	 * @param timeTo the end time
	 * @param recordsFrom the starting record
	 * @param count the count
	 * @return the record groups
	 * @throws AnalyticsException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("tables/{tableName}/{from}/{to}/{start}/{count}")
	public StreamingOutput getRecords(@PathParam("tableName") String tableName,
	                           @PathParam("from") long timeFrom, @PathParam("to") long timeTo,
	                           @PathParam("start") int recordsFrom, @PathParam("count") int count,
                               @QueryParam("columns") List<String> columns,
                               @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
	                                          throws AnalyticsException {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking getRecords for tableName: " + tableName);
		}
        AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
        String username = authenticate(authHeader);
        List<String> columnList = new ArrayList<>();
        if (columns != null) {
            for (String column : columns) {
                columnList.addAll(Arrays.asList(column.split(Constants.COLUMN_SEPARATOR)));
            }
            if (columnList.isEmpty()) {
                columnList = null;
            }
        }
        final AnalyticsDataResponse resp = analyticsDataService.get(username, tableName, 1, columnList, timeFrom,
                timeTo, recordsFrom, count);
        final Iterator<Record> iterator = AnalyticsDataAPIUtil.responseToIterator(analyticsDataService, resp);
        return new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream)
                    throws IOException, WebApplicationException {
                Writer recordWriter = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                recordWriter.write(STR_JSON_ARRAY_OPEN_SQUARE_BRACKET);
                while (iterator.hasNext()) {
                    RecordBean recordBean = Utils.createRecordBean(iterator.next());
                    recordWriter.write(gson.toJson(recordBean));
                    if (iterator.hasNext()) {
                        recordWriter.write(STR_JSON_COMMA);
                    }
                    if (logger.isDebugEnabled()) {
                        logger.debug("Retrieved -- Record Id: " + recordBean.getId() + " values :" +
                                     recordBean.toString());
                    }
                }
                recordWriter.write(STR_JSON_ARRAY_CLOSING_SQUARE_BRACKET);
                recordWriter.flush();
            }
        };
	}

	/**
	 * Gets the records.
	 * @param tableName the table name
	 * @param timeFrom the time from
	 * @param timeTo  the time to
	 * @return the records
	 * @throws AnalyticsException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("tables/{tableName}/{from}/{to}/{start}")
	public StreamingOutput getRecords(@PathParam("tableName") String tableName,
	                           @PathParam("from") long timeFrom, @PathParam("to") long timeTo,
                               @QueryParam("columns") List<String> columns,
	                           @PathParam("start") int start,
                               @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
	                                                         throws AnalyticsException {
		return getRecords(tableName, timeFrom, timeTo, start, DEFAULT_INFINITY_INDEX, columns, authHeader);
	}

	/**
	 * Gets the records.
	 * @param tableName the table name
	 * @param timeFrom the time from
	 * @param timeTo the time to
	 * @return the records
	 * @throws AnalyticsException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("tables/{tableName}/{from}/{to}")
	public StreamingOutput getRecords(@PathParam("tableName") String tableName,
	                           @PathParam("from") long timeFrom, @PathParam("to") long timeTo,
                               @QueryParam("columns") List<String> columns,
                               @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
	                  throws AnalyticsException {
		return getRecords(tableName, timeFrom, timeTo, DEFAULT_START_INDEX,
                          DEFAULT_INFINITY_INDEX, columns, authHeader);
	}

	/**
	 * Gets the records.
	 * @param tableName  the table name
	 * @param timeFrom the time from
	 * @return the records
	 * @throws AnalyticsException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("tables/{tableName}/{from}")
	public StreamingOutput getRecords(@PathParam("tableName") String tableName,
	                           @PathParam("from") long timeFrom, @QueryParam("columns") List<String> columns,
                               @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
	                                                            throws AnalyticsException {
		return getRecords(tableName, timeFrom, DEFAULT_TO_TIME, DEFAULT_START_INDEX,
		                  DEFAULT_INFINITY_INDEX, columns, authHeader);
	}

	/**
	 * Gets all the records.
	 * @param tableName the table name
	 * @return the records
	 * @throws AnalyticsException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("tables/{tableName}")
	public StreamingOutput getRecords(@PathParam("tableName") String tableName,
                                      @QueryParam("columns") List<String> columns,
                                      @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
	                                                                    throws AnalyticsException {
		return getRecords(tableName, DEFAULT_FROM_TIME, DEFAULT_TO_TIME,
		                  DEFAULT_START_INDEX, DEFAULT_INFINITY_INDEX, columns, authHeader);
	}

    /**
     * Gets the records but user can define the fields/columns he wants.
     * @param tableName the table name
     * @return the records
     * @throws AnalyticsException
     */
    /*@POST
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("tables/{tableName}")
    public StreamingOutput getRecordsWithSpecificColumns(@PathParam("tableName") String tableName,
            GetByRangeBean queryBean, @HeaderParam(AUTHORIZATION_HEADER) String authHeader) throws AnalyticsException {
        return getRecords(tableName, queryBean.getTimeFrom(), queryBean.getTimeTo(),
                          queryBean.getStart(), queryBean.getCount(), queryBean.getFields(), authHeader);
    }*/

    /**
     * Gets the records which match the primary key values batch.
     * @param tableName the table name
     * @param columnKeyValueBean bean containing the columns and values batch
     * @param authHeader the count
     * @return the record groups
     * @throws AnalyticsException
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Consumes({ MediaType.APPLICATION_JSON})
    @Path("tables/{tableName}/keyed_records")
    public StreamingOutput getRecordsWithKeyValues(@PathParam("tableName") String tableName,
                                        ColumnKeyValueBean columnKeyValueBean,
                                      @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking getWithkeyValues for tableName: " + tableName);
        }
        AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
        String username = authenticate(authHeader);
        List<String> columns = (columnKeyValueBean.getColumns() == null || columnKeyValueBean.getColumns().isEmpty())
                ? null : columnKeyValueBean.getColumns();
        final AnalyticsDataResponse resp = analyticsDataService.getWithKeyValues(username, tableName, 1, columns,
                columnKeyValueBean.getValueBatches());
        final Iterator<Record> iterator = AnalyticsDataAPIUtil.responseToIterator(analyticsDataService, resp);
        return new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream)
                    throws IOException, WebApplicationException {
                Writer recordWriter = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                recordWriter.write(STR_JSON_ARRAY_OPEN_SQUARE_BRACKET);
                    while (iterator.hasNext()) {
                        RecordBean recordBean = Utils.createRecordBean(iterator.next());
                        recordWriter.write(gson.toJson(recordBean));
                        if (iterator.hasNext()) {
                            recordWriter.write(STR_JSON_COMMA);
                        }
                        if (logger.isDebugEnabled()) {
                            logger.debug("Retrieved -- Record Id: " + recordBean.getId() + " values :" +
                                         recordBean.toString());
                        }
                    }
                recordWriter.write(STR_JSON_ARRAY_CLOSING_SQUARE_BRACKET);
                recordWriter.flush();
            }
        };
    }

	/**
	 * Inserts or update a list of records. Update only happens if there are matching record ids
	 * @param recordBeans the list of the record beans
	 * @return the response
	 * @throws AnalyticsException
	 */
	/*@POST
	@Consumes({ MediaType.APPLICATION_JSON})
	@Produces({ MediaType.APPLICATION_JSON })
	@Path(Constants.ResourcePath.RECORDS)
	public StreamingOutput insertRecords(List<RecordBean> recordBeans,
                                         @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
	                                                           throws AnalyticsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking insertRecords");
        }
        AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
        String username = authenticate(authHeader);
        if (recordBeans != null) {
            if (logger.isDebugEnabled()) {
                for (RecordBean recordBean : recordBeans) {
                    logger.debug(" inserting -- Record Id: " + recordBean.getId() + " values :" +
                                 recordBean.toString());
                }
            }
            List<Record> records = Utils.getRecords(username, recordBeans);
            analyticsDataService.put(username, records);
            final Iterator<Record> recordIterator = records.iterator();
            return new StreamingOutput() {
                @Override
                public void write(OutputStream outputStream)
                        throws IOException, WebApplicationException {
                    Writer recordWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                    recordWriter.write(STR_JSON_ARRAY_OPEN_SQUARE_BRACKET);
                    while (recordIterator.hasNext()) {
                        recordWriter.write(recordIterator.next().getId());
                        if (recordIterator.hasNext()) {
                            recordWriter.write(STR_JSON_COMMA);
                        }
                    }
                    recordWriter.write(STR_JSON_ARRAY_CLOSING_SQUARE_BRACKET);
                    recordWriter.flush();
                }
            };
        } else {
            throw new AnalyticsException("List of records are empty");
        }
    }*/

	/**
	 * Clear indices.
	 * @param tableName the table name
	 * @return the response
	 * @throws AnalyticsException
	 */
	@DELETE
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("tables/{tableName}/indexData")
	public Response clearIndices(@PathParam("tableName") String tableName,
                                 @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
	                                                                      throws AnalyticsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking clearIndexData for tableName : " +
                         tableName);
        }
        AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
        String username = authenticate(authHeader);
        analyticsDataService.clearIndexData(username, tableName);
        return handleResponse(ResponseStatus.SUCCESS, "Successfully cleared indices in table: " +
                                                          tableName);
	}

	/**
	 * Search records.
	 * @param queryBean the query bean
	 * @return the response
	 * @throws AnalyticsException
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON})
	@Produces({ MediaType.APPLICATION_JSON })
	@Path(Constants.ResourcePath.SEARCH)
	public Response search(QueryBean queryBean, @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking search for tableName : " + queryBean.getTableName());
        }
        AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
        String username = authenticate(authHeader);
        if (queryBean != null) {
            List<SearchResultEntry> searchResults = analyticsDataService.search(username,
                     queryBean.getTableName(), queryBean.getQuery(),
                     queryBean.getStart(), queryBean.getCount(), Utils.getSortedFields(queryBean.getSortByFieldBeans()));
            List<String> ids = Utils.getRecordIds(searchResults);
            List<String> columns = (queryBean.getColumns() == null || queryBean.getColumns().isEmpty()) ? null : queryBean.getColumns();
            AnalyticsDataResponse resp = analyticsDataService.get(username,
                                                                  queryBean.getTableName(), 1, columns, ids);
            Map<String, RecordBean> recordBeans = Utils.createRecordBeans(AnalyticsDataAPIUtil.listRecords(analyticsDataService,
                                                                                            resp));
            List<RecordBean> sortedRecordBeans = Utils.getSortedRecordBeans(recordBeans, searchResults);
            if (logger.isDebugEnabled()) {
                for (Map.Entry<String, RecordBean> entry : recordBeans.entrySet()) {
                    logger.debug("Search Result -- Record Id: " + entry.getKey() + " values :" +
                                 entry.getValue().toString());
                }
            }
            return Response.ok(sortedRecordBeans).build();
        } else {
            throw new AnalyticsException("Search parameters not provided");
        }
    }

    /**
     * Performs the drilldown operation on a given table.
     * @param requestBean request for drilldown which contains all the details to be drilled down
     * @param authHeader basic authentication header base64 encoded
     * @return Map containing with each field and respective list of results
     * @throws AnalyticsException
     */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON})
	@Produces({ MediaType.APPLICATION_JSON })
	@Path(Constants.ResourcePath.DRILLDOWN)
	public Response drillDown(DrillDownRequestBean requestBean,
                              @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking drilldown for tableName : " + requestBean.getTableName());
        }
        AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
        String username = authenticate(authHeader);
        if (requestBean != null) {
            AnalyticsDrillDownRequest request = Utils.createDrilldownRequest(requestBean);
            List<SearchResultEntry> result= analyticsDataService.drillDownSearch(username, request);
            List<String> ids = Utils.getRecordIds(result);
            List<String> columns = requestBean.getColumns() == null || requestBean.getColumns().isEmpty() ? null : requestBean.getColumns();
            AnalyticsDataResponse resp = analyticsDataService.get(username,
                                                                  requestBean.getTableName(), 1, columns, ids);
            Map<String, RecordBean> recordBeans = Utils.createRecordBeans(AnalyticsDataAPIUtil.listRecords(analyticsDataService,
                                                                                            resp));
            List<RecordBean> sortedRecordBeans = Utils.getSortedRecordBeans(recordBeans, result);
            return Response.ok(sortedRecordBeans).build();
        } else {
            throw new AnalyticsException("Drilldown parameters not provided");
        }
    }

    /**
     * Performs the drilldown operation on a given table.
     * @param requestBean request for drilldown which contains all the details to be drilled down
     * @param authHeader basic authentication header base64 encoded
     * @return Map containing with each field and respective list of results
     * @throws AnalyticsException
     */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON})
	@Produces({ MediaType.APPLICATION_JSON })
	@Path(Constants.ResourcePath.DRILLDOWNCOUNT)
	public Response drillDownCount(DrillDownRequestBean requestBean,
                              @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking drilldownCount for tableName : " + requestBean.getTableName());
        }
        AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
        String username = authenticate(authHeader);
        if (requestBean != null) {
            AnalyticsDrillDownRequest request = Utils.createDrilldownRequest(requestBean);
            double result = analyticsDataService.drillDownSearchCount(username, request);
            return Response.ok(result).build();
        } else {
            throw new AnalyticsException("DrilldownCount parameters not provided");
        }
    }

    /**
     * Performs the drilldown operation on a given set of range buckets of a field.
     * @param requestBean request for drilldown which contains all the details to be drilled down
     * @param authHeader basic authentication header base64 encoded
     * @return List of analyticsDrilldownRange objects with scores not-null
     * @throws AnalyticsException
     */
    @POST
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON })
    @Path(Constants.ResourcePath.DRILLDOWNRANGECOUNT)
    public Response drillDownRangeCount(DrillDownRequestBean requestBean,
                                   @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking drilldownRangeCount for tableName : " + requestBean.getTableName());
        }
        AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
        String username = authenticate(authHeader);
        if (requestBean != null) {
            AnalyticsDrillDownRequest request = Utils.createDrilldownRequest(requestBean);
            List<AnalyticsDrillDownRange> ranges = analyticsDataService.drillDownRangeCount(username, request);
            List<DrillDownRangeBean> rangeBeans = Utils.createDrillDownRangeBeans(ranges);
            return Response.ok(rangeBeans).build();
        } else {
            throw new AnalyticsException("DrilldownRangeCount parameters not provided");
        }
    }

    /**
     * Performs the drilldown operation on a given field to get the child categories
     * @param requestBean request for drilldown which contains all the details of the category drilldown
     * @param authHeader basic authentication header base64 encoded
     * @return List of subcategories
     * @throws AnalyticsException
     */
    @POST
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON })
    @Path(Constants.ResourcePath.DRILLDOWNCATEGORIES)
    public Response drillDownCategories(CategoryDrillDownRequestBean requestBean,
                                   @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking drilldownCategories for tableName : " + requestBean.getTableName());
        }
        AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
        String username = authenticate(authHeader);
        if (requestBean != null) {
            CategoryDrillDownRequest request = Utils.createDrilldownRequest(requestBean);
            SubCategories categories = analyticsDataService.drillDownCategories(username, request);
            SubCategoriesBean bean = Utils.createSubCategoriesBean(categories);
            return Response.ok(bean).build();
        } else {
            throw new AnalyticsException("DrilldownCategory parameters not provided");
        }
    }

	/**
	 * Returns the search record count.
	 * @param queryBean the query bean
	 * @return the {@link Response}response
	 * @throws AnalyticsException
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON})
	@Produces({ MediaType.APPLICATION_JSON })
	@Path(Constants.ResourcePath.SEARCH_COUNT)
	public Response searchCount(QueryBean queryBean,
                                @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking search count for tableName : " + queryBean.getTableName());
		}
        AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
        String username = authenticate(authHeader);
        if (queryBean != null) {
            int result = analyticsDataService.searchCount(username, queryBean.getTableName(), queryBean.getQuery());
            if (logger.isDebugEnabled()) {
                logger.debug("Search count : " + result);
            }
            return Response.ok(result).build();
        } else {
            throw new AnalyticsException("Search parameters not found");
        }
	}

	/**
	 * waits till indexing finishes
	 * @param seconds tthe timeout for waiting till response returns
	 * @return the {@link Response}response
	 * @throws AnalyticsException
	 */
	@GET
	@Consumes({ MediaType.APPLICATION_JSON})
	@Produces({ MediaType.APPLICATION_JSON })
	@Path(Constants.ResourcePath.INDEXING_DONE)
	public Response waitForIndexing(@QueryParam("timeout") @DefaultValue(value="-1") long seconds,
                                    @QueryParam("table") String tableName, @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
			throws AnalyticsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking waiting for indexing - timeout : " + seconds + " seconds for table: " + tableName);
        }
        AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
        String username = authenticate(authHeader);
        if (tableName == null) {
            analyticsDataService.waitForIndexing(seconds * Constants.MILLISECONDSPERSECOND);
        } else {
            analyticsDataService.waitForIndexing(username, tableName, seconds * Constants.MILLISECONDSPERSECOND);
        }
        return handleResponse(ResponseStatus.SUCCESS, "Indexing Completed successfully");
	}

    /**
     * Sets a analytics Schema for a table
     * @param tableName table Name of which the analytics schema to be set
     * @param analyticsSchemaBean table schema which represents the analytics schema
     * @return the {@link Response} response
     * @throws AnalyticsException
     */
    @POST
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    @Path("tables/{tableName}/schema")
    public Response setTableSchema(@PathParam("tableName") String tableName,
                                   AnalyticsSchemaBean analyticsSchemaBean,
                                   @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking setTableSchema for tableName : " + tableName);
        }
        AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
        String username = authenticate(authHeader);
        if (analyticsSchemaBean != null) {
            AnalyticsSchema analyticsSchema = Utils.createAnalyticsSchema(analyticsSchemaBean);
            analyticsDataService.setTableSchema(username, tableName, analyticsSchema);
            return handleResponse(ResponseStatus.SUCCESS, "Successfully set table schema for table: "
                                                          + tableName);
        } else {
            throw new AnalyticsException("Table schema is not provided");
        }
    }

    /**
     * Gets the analytics schema of a table
     * @param tableName the table name of which the schema to be retrieved
     * @return Response containing the analytics schema
     * @throws AnalyticsException
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("tables/{tableName}/schema")
    public Response getTableSchema(@PathParam("tableName") String tableName,
                                   @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking getTableSchema for table : " + tableName);
        }
        AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
        String username = authenticate(authHeader);
        AnalyticsSchema analyticsSchema = analyticsDataService.getTableSchema(username, tableName);
        AnalyticsSchemaBean analyticsSchemaBean = Utils.createTableSchemaBean(analyticsSchema);
        return Response.ok(analyticsSchemaBean).build();
    }

    /**
     * Returns the aggregated values of the given fields grouped by the given facet field.
     * @param aggregateRequestBeans the aggregate request bean
     * @return the {@link Response}response
     * @throws AnalyticsException
     */
    @POST
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON })
    @Path(Constants.ResourcePath.MULTI_AGGREGATES)
    public StreamingOutput searchWithAggregates(AggregateRequestBean[] aggregateRequestBeans,
                                @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking search with aggregates for multiple tables");
        }
        AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
        String username = authenticate(authHeader);
        if (aggregateRequestBeans != null) {
            AggregateRequest[] aggregateRequests = Utils.createAggregateRequests(aggregateRequestBeans);
            final List<AnalyticsIterator<Record>> iterators = analyticsDataService.searchWithAggregates(username, aggregateRequests);
            return new StreamingOutput() {
                @Override
                public void write(OutputStream outputStream)
                        throws IOException, WebApplicationException {
                    Writer recordWriter = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                    recordWriter.write(STR_JSON_ARRAY_OPEN_SQUARE_BRACKET);
                    for (int i=0; i < iterators.size(); i++) {
                        recordWriter.write(STR_JSON_ARRAY_OPEN_SQUARE_BRACKET);
                        while (iterators.get(i).hasNext()) {
                            Record record = iterators.get(i).next();
                            RecordBean recordBean = Utils.createRecordBean(record);
                            recordWriter.write(gson.toJson(recordBean));
                            if (iterators.get(i).hasNext()) {
                                recordWriter.write(STR_JSON_COMMA);
                            }
                            if (logger.isDebugEnabled()) {
                                logger.debug("Retrieved -- Record Id: " + recordBean.getId() + " values :" +
                                             recordBean.toString());
                            }
                        }
                        recordWriter.write(STR_JSON_ARRAY_CLOSING_SQUARE_BRACKET);
                        if (i < iterators.size()-1) {
                            recordWriter.write(STR_JSON_COMMA);
                        }
                    }
                    recordWriter.write(STR_JSON_ARRAY_CLOSING_SQUARE_BRACKET);
                    recordWriter.flush();
                }
            };
        } else {
            throw new AnalyticsException("Search parameters not found");
        }
    }

    /**
     * Returns the aggregated values of the given fields grouped by the given facet field.
     * @param aggregateRequestBean the aggregate request bean
     * @return the {@link Response}response
     * @throws AnalyticsException
     */
    @POST
    @Consumes({ MediaType.APPLICATION_JSON})
    @Produces({ MediaType.APPLICATION_JSON })
    @Path(Constants.ResourcePath.AGGREGATES)
    public StreamingOutput searchWithAggregates(AggregateRequestBean aggregateRequestBean,
                                                @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking search with aggregates for tableName : " + aggregateRequestBean.getTableName());
        }
        AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
        String username = authenticate(authHeader);
        if (aggregateRequestBean != null) {
            AggregateRequest aggregateRequest = Utils.createAggregateRequest(aggregateRequestBean);
            final AnalyticsIterator<Record> iterator = analyticsDataService.searchWithAggregates(username, aggregateRequest);
            return new StreamingOutput() {
                @Override
                public void write(OutputStream outputStream)
                        throws IOException, WebApplicationException {
                    Writer recordWriter = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                    recordWriter.write(STR_JSON_ARRAY_OPEN_SQUARE_BRACKET);
                    while (iterator.hasNext()) {
                        Record record = iterator.next();
                        RecordBean recordBean = Utils.createRecordBean(record);
                        recordWriter.write(gson.toJson(recordBean));
                        if (iterator.hasNext()) {
                            recordWriter.write(STR_JSON_COMMA);
                        }
                        if (logger.isDebugEnabled()) {
                            logger.debug("Retrieved -- Record Id: " + recordBean.getId() + " values :" +
                                         recordBean.toString());
                        }
                    }
                    recordWriter.write(STR_JSON_ARRAY_CLOSING_SQUARE_BRACKET);
                    recordWriter.flush();
                }
            };
        } else {
            throw new AnalyticsException("Search parameters not found");
        }
    }

    /**
     * Re-index the records of a given table between given timestamp range.
     * @param tableName the table name
     * @param timeFrom the time from
     * @param timeTo  the time to
     * @throws AnalyticsException
     */
    @POST
    @Produces({ MediaType.APPLICATION_JSON })
    @Path("tables/{tableName}")
    public Response reIndex(@PathParam("tableName") String tableName,
                                      @QueryParam("from") long timeFrom, @QueryParam("to") long timeTo,
                                      @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
        AnalyticsDataAPI analyticsDataService = Utils.getAnalyticsDataAPIs();
        String username = authenticate(authHeader);
        analyticsDataService.reIndex(username, tableName, timeFrom, timeTo);
        return handleResponse(ResponseStatus.SUCCESS, "Successfully started re-indexing for table: "
                                                      + tableName);
    }

    private String authenticate(String authHeader) throws AnalyticsException {

        String username;
        if (authHeader != null && authHeader.startsWith(Constants.BASIC_AUTH_HEADER)) {
            // Authorization: Basic base64credentials
            String base64Credentials = authHeader.substring(Constants.BASIC_AUTH_HEADER.length()).trim();
            String credentials = new String(Base64.decode(base64Credentials),
                                            Charset.forName(DEFAULT_CHARSET));
            // credentials = username:password
            final String[] values = credentials.split(":", 2);
            username = values[0];
            String password = values[1];
            if ("".equals(username) || username == null || "".equals(password) || password == null) {
                throw new UnauthenticatedUserException("Username and password cannot be empty");
            }
            String tenantDomain = MultitenantUtils.getTenantDomain(username);
            String tenantLessUserName = MultitenantUtils.getTenantAwareUsername(username);
            try {
                // get super tenant context and get realm service which is an osgi service
                RealmService realmService = (RealmService) PrivilegedCarbonContext
                        .getThreadLocalCarbonContext().getOSGiService(RealmService.class, null);
                if (realmService != null) {
                    int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
                    if (tenantId == MultitenantConstants.INVALID_TENANT_ID) {
                        throw new UnauthenticatedUserException("Authentication failed - Invalid domain");

                    }
                    // get tenant's user realm
                    UserRealm userRealm = realmService.getTenantUserRealm(tenantId);
                    boolean isAuthenticated = userRealm.getUserStoreManager()
                            .authenticate(tenantLessUserName, password);
                    if (!isAuthenticated) {
                        throw  new UnauthenticatedUserException("User is not authenticated!");
                    } else {
                        return username;
                    }
                }
            } catch (UserStoreException e) {
                throw new AnalyticsException("Error while accessing the user realm of user :"
                                             + username, e);
            }
        } else {
            throw new UnauthenticatedUserException("Invalid authentication header");
        }
        return username;
    }

	/**
	 * Gets the database persist name for a table
	 *
	 * @param tableName the table name of which the persist name to be retrieved
	 * @return Response containing the table persist name in database
	 * @throws AnalyticsException
	 */
	@GET
	@Produces({MediaType.TEXT_PLAIN})
	@Path("tables/{tableName}/persistName")
	public Response getTablePersistName(@PathParam("tableName") String tableName,
										@HeaderParam(AUTHORIZATION_HEADER) String authHeader)
			throws AnalyticsException {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking getTablePersistName for table : " + tableName);
		}

		String username = authenticate(authHeader);
		RealmService realmService = Utils.getRealmService();
		String tenantDomain = MultitenantUtils.getTenantDomain(username);
		try {
			int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
			return Response.ok(GenericUtils.generateTableUUID(tenantId, tableName)).build();
		} catch (UserStoreException e) {
			throw new AnalyticsException("Error while getting tenant ID for user: " + username + "[" + e.getMessage() + "]", e);
		}
	}

	/**
	 * Gets the actual table name from the database persist name for a table
	 *
	 * @param persistName the persisted table name for which the actual name needs to be inferred
	 * @return HTTP response containing the actual table name
	 * @throws AnalyticsException
	 */
	@GET
	@Produces({MediaType.TEXT_PLAIN})
	@Path("tables/{persistName}/actualName")
    public Response getTableActualName(@PathParam("persistName") String persistName,
                                       @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking getTableActualName for table : " + persistName);
        }
        String username = authenticate(authHeader);
		String tenantDomain = MultitenantUtils.getTenantDomain(username);
		List<String> tables = Utils.getAnalyticsDataAPIs().listTables(username);
        try {
            int tenantId = Utils.getRealmService().getTenantManager().getTenantId(tenantDomain);
            for (String tableName : tables) {
                String candidate = GenericUtils.generateTableUUID(tenantId, tableName);
                if (candidate.equals(persistName)) {
                    return Response.ok(tableName).build();
                }
            }
			return Response.status(Response.Status.NOT_FOUND).entity("The table '" + persistName + "' was not found.").build();
        } catch (UserStoreException e) {
            throw new AnalyticsException("Error while getting tenant ID for user: " + username + "[" + e.getMessage() + "]", e);
        }
    }

	/**
	 * To clear table information from in memory cache
	 *
	 * @param tableName table Name of which that cache going to clear
	 * @return the {@link Response} response
	 * @throws AnalyticsException
	 */
	@DELETE
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	@Path("tables/{tableName}/schema-invalidate")
	public Response clearTableCache(@PathParam("tableName") String tableName,
									@HeaderParam(AUTHORIZATION_HEADER) String authHeader)
			throws AnalyticsException {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking schema invalidate for tableName : " + tableName);
		}
		AnalyticsDataServiceImpl dataService;
		Object obj = Utils.getAnalyticsDataService();
		try {
			if (obj instanceof AnalyticsDataServiceImpl) {
				dataService = (AnalyticsDataServiceImpl) obj;
				RealmService realmService = Utils.getRealmService();
				String username = authenticate(authHeader);
				String tenantDomain = MultitenantUtils.getTenantDomain(username);
				int tenantId = realmService.getTenantManager().getTenantId(tenantDomain);
				dataService.invalidateAnalyticsTableInfo(tenantId, tableName);
				return handleResponse(ResponseStatus.SUCCESS, "Successfully clear cache for table: "
						+ tableName);
			} else {
				return handleResponse(ResponseStatus.FAILED, "Unable to get AnalyticsDataServiceImpl obj to clear cache for table: "
						+ tableName);
			}
		} catch (Exception ex) {
			throw new AnalyticsException("Unable to invalidate schema for table:" + tableName);
		}
	}
}
