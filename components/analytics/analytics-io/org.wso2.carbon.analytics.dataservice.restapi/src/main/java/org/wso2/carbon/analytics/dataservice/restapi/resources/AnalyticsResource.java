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

import com.google.gson.Gson;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.AnalyticsDSUtils;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.commons.IndexType;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.restapi.Constants;
import org.wso2.carbon.analytics.dataservice.restapi.Utils;
import org.wso2.carbon.analytics.dataservice.restapi.beans.AnalyticsSchemaBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.IndexTypeBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.QueryBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.RecordBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.TableBean;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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
    public static final String STR_JSON_ARRAY_OPEN_SQUARE_BRACKET = "[";
    public static final String STR_JSON_COMMA = ",";
    public static final String STR_JSON_ARRAY_CLOSING_SQUARE_BRACKET = "]";

    /**
	 * Creates the table.
	 * @param tableBean the table name as a json object
	 * @return the response
	 * @throws AnalyticsException
	 */
	@POST
	@Path(Constants.ResourcePath.TABLES)
	@Consumes({ MediaType.APPLICATION_JSON})
	@Produces({ MediaType.APPLICATION_JSON })
	public Response createTable(TableBean tableBean) throws AnalyticsException {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking createTable for tenantId :" + tenantId + " tableName : " +
			             tableBean.getTableName());
		}
		AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
		if (analyticsDataService.tableExists(tenantId, tableBean.getTableName())) {
			return handleResponse(ResponseStatus.CONFLICT, "table :" + tableBean.getTableName() +
			                                               " already exists");
		}
		analyticsDataService.createTable(tenantId, tableBean.getTableName());
		return handleResponse(ResponseStatus.CREATED,
		                      "Successfully created table: " + tableBean.getTableName());
	}
	
	/**
	 * Check if the table Exists
	 * @return the response
	 * @throws AnalyticsException
	 */
	@GET
	@Path(Constants.ResourcePath.TABLE_EXISTS)
	@Produces({ MediaType.APPLICATION_JSON })
	public Response tableExists(@QueryParam("tableName")String tableName) throws AnalyticsException {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking listTables for tenantId :" + tenantId);
		}
		AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
		boolean tableExists = analyticsDataService.tableExists(tenantId, tableName);
		if (logger.isDebugEnabled()) {
			logger.debug("Table's Existance : " + tableExists);
		}
		if(!tableExists) {
			return handleResponse(ResponseStatus.NON_EXISTENT,
			                      "Table : " + tableName + " does not exist.");
		}
		return handleResponse(ResponseStatus.SUCCESS,
		                      "Table : " + tableName + " exists.");
	}

	/**
	 * List all the tables.
	 * @return the response
	 * @throws AnalyticsException
	 */
	@GET
	@Path(Constants.ResourcePath.TABLES)
	@Produces({ MediaType.APPLICATION_JSON })
	public Response listTables() throws AnalyticsException {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking listTables for tenantId :" + tenantId);
		}
		AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
		List<String> tables = analyticsDataService.listTables(tenantId);
		if (logger.isDebugEnabled()) {
			logger.debug("Table List : " + tables);
		}
		return Response.ok(tables).build();
	}

	/**
	 * Delete table.
	 * @param tableBean the table name as a json object
	 * @return the response
	 * @throws AnalyticsException the analytics exception
	 */
	@DELETE
	@Path(Constants.ResourcePath.TABLES)
	@Consumes({ MediaType.APPLICATION_JSON})
	@Produces({ MediaType.APPLICATION_JSON })
	public Response deleteTable(TableBean tableBean) throws AnalyticsException {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking deleteTable for tenantId :" + tenantId + " tableName : " +
			             tableBean.getTableName());
		}
		AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
		if (analyticsDataService.tableExists(tenantId, tableBean.getTableName())) {
			analyticsDataService.deleteTable(-1234, tableBean.getTableName());
			return handleResponse(ResponseStatus.SUCCESS, "Successfully deleted table: " +
			                                              tableBean.getTableName());
		}
		return handleResponse(ResponseStatus.NON_EXISTENT, "table: " + tableBean.getTableName() +
		                                                   " does not exists.");
	}
	
	/**
	 * Inserts or update a list of records to a table. updating happens only if there are matching record ids
	 * @param recordBeans the list of the record beans
	 * @return the response
	 * @throws AnalyticsException
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON})
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("tables/{tableName}")
	public StreamingOutput insertRecordsToTable(@PathParam("tableName")String tableName, List<RecordBean> recordBeans)
	                                                           throws AnalyticsException {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking insertRecordsToTable");
		}
		int tenantId = -1234;
		AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
		if (logger.isDebugEnabled()) {
			for (RecordBean recordBean : recordBeans) {
				logger.debug(" inserting -- Record Id: " + recordBean.getId() + " values :" +
				             recordBean.toString() + " to table :" + tableName);
			}
		}
		List<Record> records = Utils.getRecordsForTable(tenantId, tableName, recordBeans);
		analyticsDataService.put(records);
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
	}

	/**
	 * Delete records either the time range, but not both
	 * @param tableName  the table name
	 * @param timeFrom the time from
	 * @param timeTo the time to
	 * @return the response
	 * @throws AnalyticsException
	 */
	@DELETE
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("tables/{tableName}/{timeFrom}/{timeTo}")
	public Response deleteRecords(@PathParam("tableName") String tableName,
	                              @PathParam("timeFrom") long timeFrom,
	                              @PathParam("timeTo") long timeTo) throws AnalyticsException {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking deleteRecords for tenantId :" + tenantId + " tableName : " +
			             tableName);
		}
		AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
		if (logger.isDebugEnabled()) {
			logger.debug("deleting the records from " + timeFrom + " to " + timeTo);
		}
		analyticsDataService.delete(tenantId, tableName, timeFrom, timeTo);
		return handleResponse(ResponseStatus.SUCCESS, "Successfully deleted records in table: " +
		                                              tableName);
	}

	/**
	 * Delete records either by ids
	 * @param tableName the table name
	 * @param ids the ids
	 * @return the response
	 * @throws AnalyticsException
	 */
	@DELETE
	@Consumes({ MediaType.APPLICATION_JSON})
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("tables/{tableName}")
	public Response deleteRecordsByIds(@PathParam("tableName") String tableName, List<String> ids)
	                                          throws AnalyticsException {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking deleteRecords for tenantId :" + tenantId + " tableName : " +
			             tableName);
		}
		AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
		if (logger.isDebugEnabled()) {
			logger.debug("deleting the records for ids :" + ids);
		}
		analyticsDataService.delete(tenantId, tableName, ids);
		return handleResponse(ResponseStatus.SUCCESS, "Successfully deleted records in table: " +
		                                              tableName);
	}

	/**
	 * Gets the record count.
	 * @param tableName the table name
	 * @return the record count
	 * @throws AnalyticsException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("tables/{tableName}/recordcount")
	public Response getRecordCount(@PathParam("tableName") String tableName)
	                                                                        throws AnalyticsException {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking getRecordCount for tableName: " + tableName + " tenantId :" +
			             tenantId);
		}
		AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
		long recordCount = analyticsDataService.getRecordCount(tenantId, tableName, Long.MIN_VALUE, Long.MAX_VALUE);
		if (logger.isDebugEnabled()) {
			logger.debug("RecordCount for tableName: " + tableName + " is " + recordCount);
		}
		return Response.ok(recordCount).build();
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
	                           @PathParam("start") int recordsFrom, @PathParam("count") int count)
	                                          throws AnalyticsException {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking getRecordGroups for tableName: " + tableName + " tenantId :" +
			             tenantId);
		}
		AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
		final RecordGroup[] recordGroups;
		recordGroups = analyticsDataService.get(tenantId, tableName, 1, null, timeFrom, timeTo, recordsFrom, count);

        final List<Iterator<Record>> iterators = Utils.getRecordIterators(recordGroups, analyticsDataService);
        return new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream)
                    throws IOException, WebApplicationException {
                Writer recordWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                recordWriter.write(STR_JSON_ARRAY_OPEN_SQUARE_BRACKET);
                for(Iterator<Record> iterator : iterators) {
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
	                           @PathParam("start") int start)
	                                                         throws AnalyticsException {
		return getRecords(tableName, timeFrom, timeTo, start, DEFAULT_INFINITY_INDEX);
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
	                           @PathParam("from") long timeFrom, @PathParam("to") long timeTo)
	                  throws AnalyticsException {
		return getRecords(tableName, timeFrom, timeTo, DEFAULT_START_INDEX, DEFAULT_INFINITY_INDEX);
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
	                           @PathParam("from") long timeFrom)
	                                                            throws AnalyticsException {
		return getRecords(tableName, timeFrom, DEFAULT_TO_TIME, DEFAULT_START_INDEX,
		                  DEFAULT_INFINITY_INDEX);
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
	public StreamingOutput getRecords(@PathParam("tableName") String tableName)
	                                                                    throws AnalyticsException {
		return getRecords(tableName, DEFAULT_FROM_TIME, DEFAULT_TO_TIME,
		                  DEFAULT_START_INDEX, DEFAULT_INFINITY_INDEX);
	}

	/**
	 * Inserts or update a list of records. Update only happens if there are matching record ids
	 * @param recordBeans the list of the record beans
	 * @return the response
	 * @throws AnalyticsException
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON})
	@Produces({ MediaType.APPLICATION_JSON })
	@Path(Constants.ResourcePath.RECORDS)
	public StreamingOutput insertRecords(List<RecordBean> recordBeans)
	                                                           throws AnalyticsException {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking insertRecords");
		}
		int tenantId = -1234;
		AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
		if (logger.isDebugEnabled()) {
			for (RecordBean recordBean : recordBeans) {
				logger.debug(" inserting -- Record Id: " + recordBean.getId() + " values :" +
				             recordBean.toString());
			}
		}
		List<Record> records = Utils.getRecords(tenantId, recordBeans);
		analyticsDataService.put(records);
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
	}

	/**
	 * Sets the indices.
	 * @param tableName the table name
	 * @param columnsBean the columns bean containing all the indices
	 * @return the response
	 * @throws AnalyticsException
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON})
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("tables/{tableName}/indices")
	public Response setIndices(@PathParam("tableName") String tableName,
	                           Map<String, IndexTypeBean> columnsBean) throws AnalyticsException {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking setIndices for tenantId :" + tenantId + " tableName : " +
			             tableName);
		}
		AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
		Map<String, IndexType> columns = Utils.createIndexTypeMap(columnsBean);
		if (logger.isDebugEnabled()) {
			logger.debug("Setting indices : " + columns.keySet().toArray());
		}
		analyticsDataService.setIndices(tenantId, tableName, columns);
		return handleResponse(ResponseStatus.CREATED, "Successfully set indices in table: " +
		                                              tableName);
	}

	/**
	 * Gets the indices.
	 * @param tableName the table name
	 * @return the indices
	 * @throws AnalyticsException
	 */
	@GET
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("tables/{tableName}/indices")
	public Response getIndices(@PathParam("tableName") String tableName) throws AnalyticsException {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking getIndices for tenantId :" + tenantId + " tableName : " +
			             tableName);
		}
		AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
		Map<String, IndexType> columns = analyticsDataService.getIndices(tenantId, tableName);
		Map<String, IndexTypeBean> columnsBean = Utils.createIndexTypeBeanMap(columns);
		if (logger.isDebugEnabled()) {
			logger.debug("Getting indices : " + columnsBean.keySet().toArray());
		}
		return Response.ok(columnsBean).build();
	}

	/**
	 * Clear indices.
	 * @param tableName the table name
	 * @return the response
	 * @throws AnalyticsException
	 */
	@DELETE
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("tables/{tableName}/indices")
	public Response clearIndices(@PathParam("tableName") String tableName)
	                                                                      throws AnalyticsException {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking clearIndices for tenantId :" + tenantId + " tableName : " +
			             tableName);
		}
		AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
		analyticsDataService.clearIndices(tenantId, tableName);
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
	public Response search(QueryBean queryBean) throws AnalyticsException {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking search for tenantId :" + tenantId +
			             " tableName : " + queryBean.getTableName());
		}
		AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
		List<SearchResultEntry> searchResults = analyticsDataService.search(tenantId,
		                                                                    queryBean.getTableName(),
		                                                                    queryBean.getLanguage(),
		                                                                    queryBean.getQuery(),
		                                                                    queryBean.getStart(),
		                                                                    queryBean.getCount());
		List<String> ids = Utils.getRecordIds(searchResults);
		RecordGroup[] recordGroups = analyticsDataService.get(-1234, queryBean.getTableName(), 1, null, ids);
		List<RecordBean> recordBeans = Utils.createRecordBeans(AnalyticsDSUtils.listRecords(analyticsDataService,
		                                                                                    recordGroups));
		if (logger.isDebugEnabled()) {
			for (RecordBean recordBean : recordBeans) {
				logger.debug("Search Result -- Record Id: " + recordBean.getId() + " values :" +
				             recordBean.toString());
			}
		}
		return Response.ok(recordBeans).build();
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
	public Response searchCount(QueryBean queryBean) throws AnalyticsException {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking search count for tenantId :" + tenantId +
			             " tableName : " + queryBean.getTableName());
		}
		AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
		int result = analyticsDataService.searchCount(tenantId, queryBean.getTableName(),
		                                              queryBean.getLanguage(), queryBean.getQuery());
		if (logger.isDebugEnabled()) {
			logger.debug("Search count : " + result);
		}
		return Response.ok(result).build();
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
	public Response waitForIndexing(@QueryParam("timeout") @DefaultValue(value="-1") long seconds) 
			throws AnalyticsException {
		int tenantId = -1234;
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking waiting for indexing for tenantId :" + tenantId +
			             " timeout : " + seconds + " seconds");
		}
		AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
		analyticsDataService.waitForIndexing(seconds * Constants.MILLISECONDSPERSECOND);
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
    public Response setTableSchema(@PathParam("tableName") String tableName, AnalyticsSchemaBean analyticsSchemaBean)
            throws AnalyticsException {
        int tenantId = -1234;
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking setTableSchema for tenantId :" + tenantId +
                         " tableName : " + tableName);
        }
        AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
        AnalyticsSchema analyticsSchema =Utils.createAnalyticsSchema(analyticsSchemaBean);
        analyticsDataService.setTableSchema(tenantId, tableName, analyticsSchema);
        return handleResponse(ResponseStatus.SUCCESS,"Successfully set table schema for table: " + tableName);
    }

    /**
     * Gets the analytics schema of a table
     * @param tableName the table name of which the schema to be retreived
     * @return Response containing the analytics schema
     * @throws AnalyticsException
     */
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    @Path("tables/{tableName}/schema")
    public Response getTableSchema(@PathParam("tableName") String tableName)
            throws AnalyticsException{
        int tenantId = -1234;
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking getTableSchema for tenantId :" + tenantId +
                         " table : " + tableName);
        }
        AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
        AnalyticsSchema analyticsSchema = analyticsDataService.getTableSchema(tenantId, tableName);
        AnalyticsSchemaBean analyticsSchemaBean = Utils.createTableSchemaBean(analyticsSchema);
        return Response.ok(analyticsSchemaBean).build();
    }
}
