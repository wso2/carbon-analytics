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
import org.apache.axiom.om.util.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.SecureAnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRange;
import org.wso2.carbon.analytics.dataservice.commons.AnalyticsDrillDownRequest;
import org.wso2.carbon.analytics.dataservice.commons.DrillDownResultEntry;
import org.wso2.carbon.analytics.dataservice.commons.IndexType;
import org.wso2.carbon.analytics.dataservice.commons.SearchResultEntry;
import org.wso2.carbon.analytics.dataservice.restapi.Constants;
import org.wso2.carbon.analytics.dataservice.restapi.UnauthenticatedUserException;
import org.wso2.carbon.analytics.dataservice.restapi.Utils;
import org.wso2.carbon.analytics.dataservice.restapi.beans.AnalyticsSchemaBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.DrillDownRequestBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.DrillDownResultBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.IndexConfigurationBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.PerCategoryDrillDownResultBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.PerFieldDrillDownResultBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.QueryBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.RecordBean;
import org.wso2.carbon.analytics.dataservice.restapi.beans.TableBean;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsCategoryPath;
import org.wso2.carbon.analytics.datasource.commons.AnalyticsSchema;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.RecordGroup;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.base.MultitenantConstants;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.user.api.UserRealm;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.user.core.service.RealmService;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
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
    private static final String STR_JSON_ARRAY_OPEN_SQUARE_BRACKET = "[";
    private static final String STR_JSON_COMMA = ",";
    private static final String STR_JSON_ARRAY_CLOSING_SQUARE_BRACKET = "]";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String DEFAUL_CHARSETT = "UTF-8";

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
	public Response createTable(TableBean tableBean, @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking createTable tableName : " +
			             tableBean.getTableName());
		}
		SecureAnalyticsDataService analyticsDataService = Utils.getSecureAnalyticsDataService();
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
    }

	/**
	 * Check if the table Exists
	 * @return the response
	 * @throws AnalyticsException
	 */
	@GET
	@Path(Constants.ResourcePath.TABLE_EXISTS)
	@Produces({ MediaType.APPLICATION_JSON })
	public Response tableExists(@QueryParam("tableName")String tableName,
                                @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking listTables");
		}
		SecureAnalyticsDataService analyticsDataService = Utils.getSecureAnalyticsDataService();
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
		SecureAnalyticsDataService analyticsDataService = Utils.getSecureAnalyticsDataService();
        String username = authenticate(authHeader);
        List<String> tables = analyticsDataService.listTables(username);
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
	public Response deleteTable(TableBean tableBean, @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking deleteTable for tableName : " +
			             tableBean.getTableName());
		}
		SecureAnalyticsDataService analyticsDataService = Utils.getSecureAnalyticsDataService();
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
	public StreamingOutput insertRecordsToTable(@PathParam("tableName")String tableName, List<RecordBean> recordBeans,
                                                @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
	                                                           throws AnalyticsException {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking insertRecordsToTable");
		}
		AnalyticsDataService analyticsDataService = Utils.getAnalyticsDataService();
        String username = authenticate(authHeader);
        if (recordBeans != null) {
            if (logger.isDebugEnabled()) {
                for (RecordBean recordBean : recordBeans) {
                    logger.debug(" inserting -- Record Id: " + recordBean.getId() + " values :" +
                                 recordBean.toString() + " to table :" + tableName);
                }
            }
            List<Record> records = Utils.getRecordsForTable(username, tableName, recordBeans);
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
        } else {
            throw new AnalyticsException("List of records is empty");
        }
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
	                              @PathParam("timeTo") long timeTo,
                                  @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking deleteRecords for tableName : " +
			             tableName);
		}
		SecureAnalyticsDataService analyticsDataService = Utils.getSecureAnalyticsDataService();
        String username = authenticate(authHeader);
        if (logger.isDebugEnabled()) {
            logger.debug("deleting the records from " + timeFrom + " to " + timeTo);
        }
        analyticsDataService.delete(username, tableName, timeFrom, timeTo);
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
	public Response deleteRecordsByIds(@PathParam("tableName") String tableName, List<String> ids,
                                       @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
	                                          throws AnalyticsException {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking deleteRecords for tableName : " +
			             tableName);
		}
        SecureAnalyticsDataService analyticsDataService = Utils.getSecureAnalyticsDataService();
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
	public Response getRecordCount(@PathParam("tableName") String tableName,
                                   @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
	                                                                        throws AnalyticsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking getRecordCount for tableName: " + tableName);
        }
        SecureAnalyticsDataService analyticsDataService = Utils.getSecureAnalyticsDataService();
        String username = authenticate(authHeader);
        long recordCount = analyticsDataService.getRecordCount(username, tableName,
                                                               Long.MIN_VALUE, Long.MAX_VALUE);
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
	                           @PathParam("start") int recordsFrom, @PathParam("count") int count,
                               @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
	                                          throws AnalyticsException {
		if (logger.isDebugEnabled()) {
			logger.debug("Invoking getRecordGroups for tableName: " + tableName);
		}
		SecureAnalyticsDataService analyticsDataService = Utils.getSecureAnalyticsDataService();
        String username = authenticate(authHeader);
        final RecordGroup[] recordGroups;
        recordGroups = analyticsDataService.get(username, tableName, 1, null, timeFrom, timeTo, recordsFrom, count);

        final List<Iterator<Record>> iterators = Utils.getRecordIterators(recordGroups, analyticsDataService);
        return new StreamingOutput() {
            @Override
            public void write(OutputStream outputStream)
                    throws IOException, WebApplicationException {
                Writer recordWriter = new BufferedWriter(new OutputStreamWriter(outputStream));
                recordWriter.write(STR_JSON_ARRAY_OPEN_SQUARE_BRACKET);
                for (Iterator<Record> iterator : iterators) {
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
	                           @PathParam("start") int start,
                               @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
	                                                         throws AnalyticsException {
		return getRecords(tableName, timeFrom, timeTo, start, DEFAULT_INFINITY_INDEX, authHeader);
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
                               @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
	                  throws AnalyticsException {
		return getRecords(tableName, timeFrom, timeTo, DEFAULT_START_INDEX,
                          DEFAULT_INFINITY_INDEX, authHeader);
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
	                           @PathParam("from") long timeFrom,
                               @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
	                                                            throws AnalyticsException {
		return getRecords(tableName, timeFrom, DEFAULT_TO_TIME, DEFAULT_START_INDEX,
		                  DEFAULT_INFINITY_INDEX, authHeader);
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
                                      @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
	                                                                    throws AnalyticsException {
		return getRecords(tableName, DEFAULT_FROM_TIME, DEFAULT_TO_TIME,
		                  DEFAULT_START_INDEX, DEFAULT_INFINITY_INDEX, authHeader);
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
	public StreamingOutput insertRecords(List<RecordBean> recordBeans,
                                         @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
	                                                           throws AnalyticsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking insertRecords");
        }
        SecureAnalyticsDataService analyticsDataService = Utils.getSecureAnalyticsDataService();
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
    @Path("facets")
    public Response insertFacetRecords(List<RecordBean> recordBeans)
            throws AnalyticsException {
        AnalyticsDataService ads = Utils.getAnalyticsDataService();
        List<Record> l = new ArrayList<Record>();

        String[] str = new String[]{"2015", "Jan", "24"};
        AnalyticsCategoryPath acp = new AnalyticsCategoryPath(str);
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("testField", acp);
        values.put("number", 1);
        values.put("number1", 5);
        Record r = new Record(-1234,"test",values);

        String[] str1 = new String[]{"2015", "Feb", "21"};
        AnalyticsCategoryPath acp1 = new AnalyticsCategoryPath(str1);
        Map<String, Object> values1 = new HashMap<String, Object>();
        values1.put("testField", acp1);
        values1.put("number", 2);
        values1.put("number1", 2);
        Record rr = new Record(-1234,"test",values1);

        String[] str2 = new String[]{"2015", "Feb", "23"};
        AnalyticsCategoryPath acp2 = new AnalyticsCategoryPath(str2);
        Map<String, Object> values2 = new HashMap<String, Object>();
        values2.put("testField", acp2);
        values2.put("number", 3);
        values2.put("number1", 1);
        Record rrr = new Record(-1234,"test",values2);

        l.add(r);
        l.add(rr);
        l.add(rrr);
        ads.put(l);

        List<String> ids = new ArrayList<String>();
        ids.add(r.getId());
        ids.add(rr.getId());
        ids.add(rrr.getId());
        RecordGroup[] aaaa = ads.get(-1234, "test", 0, null, ids);
        List<Record> ddddd = GenericUtils.listRecords(ads, aaaa);
        List<RecordBean> fff = Utils.createRecordBeans(ddddd);

        return Response.ok(fff).build();
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
    @Path("drill/{categories}/{records}")
    public Response drilldown(@PathParam("categories")int categories, @PathParam("records")int records, List<RecordBean> recordBeans)
            throws AnalyticsException {
        AnalyticsDataService ads = Utils.getAnalyticsDataService();
        AnalyticsCategoryPath path = new AnalyticsCategoryPath(new String[]{"2015", "Feb"});
        AnalyticsDrillDownRequest anss = new AnalyticsDrillDownRequest();
        anss.addCategoryPath("testField", path);
        anss.setScoreFunction("2*_weight");
        anss.setLanguageQuery(null);
        anss.setLanguage("lucene");
        anss.setTableName("test");
        anss.setCategoryCount(categories);
        anss.setRecordCount(records);
        anss.setWithIds(true);

        return Response.ok(ads.drillDown(-1234,anss)).build();
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
    @Path("drillrange")
    public Response drilldownRange(List<RecordBean> recordBeans)
            throws AnalyticsException {
        AnalyticsDataService ads = Utils.getAnalyticsDataService();
        AnalyticsDrillDownRequest anss = new AnalyticsDrillDownRequest();
        anss.addRangeFacet("number", new AnalyticsDrillDownRange("-1 --- 1.5", -1, 1.5));
        anss.addRangeFacet("number", new AnalyticsDrillDownRange("1.5 --- 3.1", 1.5, 3.1));
        anss.addRangeFacet("number1", new AnalyticsDrillDownRange("3 --- 6", 3, 6));
        AnalyticsCategoryPath path = new AnalyticsCategoryPath(new String[]{"2015"});
        anss.addCategoryPath("testField", path);
        anss.setLanguageQuery(null);
        anss.setLanguage("lucene");
        anss.setTableName("test");
        anss.setCategoryCount(1);
        anss.setRecordCount(1);
        anss.setWithIds(true);

        DrillDownResultBean dd = new DrillDownResultBean();
        PerCategoryDrillDownResultBean cc1 = new PerCategoryDrillDownResultBean();
        cc1.setCategory("fdfdf");
        cc1.setRecordIds(new String[]{"gf", "fdf"});
        cc1.setRecordCount(2);
        cc1.setCategoryPath(new String[]{"gf", "fdf"});
        PerCategoryDrillDownResultBean cc2 = new PerCategoryDrillDownResultBean();
        cc2.setCategory("fdfdf");
        cc2.setRecordIds(new String[]{"gf", "fdf"});
        cc2.setRecordCount(2);
        cc2.setCategoryPath(new String[]{"gf", "fdf"});

        PerFieldDrillDownResultBean pp = new PerFieldDrillDownResultBean();
        pp.setFieldName("testffff");
        pp.setCategories(new PerCategoryDrillDownResultBean[]{cc1, cc2});
        dd.setPerFieldEntries(new PerFieldDrillDownResultBean[]{pp});
        return Response.ok(dd).build();
    }

    /**
	 * Sets the indices.
	 * @param tableName the table name
	 * @param indexInfo the columns bean containing all the indices
	 * @return the response
	 * @throws AnalyticsException
	 */
	@POST
	@Consumes({ MediaType.APPLICATION_JSON})
	@Produces({ MediaType.APPLICATION_JSON })
	@Path("tables/{tableName}/indices")
	public Response setIndices(@PathParam("tableName") String tableName,
	                           IndexConfigurationBean indexInfo,
                               @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking setIndices for tableName : " +
                         tableName);
        }
        SecureAnalyticsDataService analyticsDataService = Utils.getSecureAnalyticsDataService();
        String username = authenticate(authHeader);
        if (indexInfo != null) {
            Map<String, IndexType> columns = Utils.createIndexTypeMap(indexInfo.getIndices());
            if (logger.isDebugEnabled()) {
                logger.debug("Setting indices : " + columns.keySet());
            }
            analyticsDataService.setIndices(username, tableName, columns,indexInfo.getScoreParams());
            return handleResponse(ResponseStatus.CREATED, "Successfully set indices in table: " +
                                                          tableName);
        } else {
            throw new AnalyticsException("Indices are empty");
        }
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
	public Response getIndices(@PathParam("tableName") String tableName,
                               @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking getIndices for tableName : " +
                         tableName);
        }
        SecureAnalyticsDataService analyticsDataService = Utils.getSecureAnalyticsDataService();
        String username = authenticate(authHeader);
        Map<String, IndexType> columns = analyticsDataService.getIndices(username, tableName);
        List<String> scoreParams = analyticsDataService.getScoreParams(username, tableName);
        IndexConfigurationBean indexConfigurationBean = new IndexConfigurationBean();
        indexConfigurationBean.setIndices(Utils.createIndexTypeBeanMap(columns));
        indexConfigurationBean.setScoreParams(scoreParams);
        if (logger.isDebugEnabled()) {
            logger.debug("Getting indices : " + indexConfigurationBean.getIndices().keySet());
        }
        return Response.ok(indexConfigurationBean).build();
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
	public Response clearIndices(@PathParam("tableName") String tableName,
                                 @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
	                                                                      throws AnalyticsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking clearIndices for tableName : " +
                         tableName);
        }
        SecureAnalyticsDataService analyticsDataService = Utils.getSecureAnalyticsDataService();
        String username = authenticate(authHeader);
        analyticsDataService.clearIndices(username, tableName);
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
        SecureAnalyticsDataService analyticsDataService = Utils.getSecureAnalyticsDataService();
        String username = authenticate(authHeader);
        if (queryBean != null) {
            List<SearchResultEntry> searchResults = analyticsDataService.search(username,
                     queryBean.getTableName(), queryBean.getLanguage(), queryBean.getQuery(),
                     queryBean.getStart(), queryBean.getCount());
            List<String> ids = Utils.getRecordIds(searchResults);
            RecordGroup[] recordGroups = analyticsDataService.get(username,
                                                                  queryBean.getTableName(), 1, null, ids);
            List<RecordBean> recordBeans = Utils.createRecordBeans(GenericUtils.listRecords(analyticsDataService,
                                                                                            recordGroups));
            if (logger.isDebugEnabled()) {
                for (RecordBean recordBean : recordBeans) {
                    logger.debug("Search Result -- Record Id: " + recordBean.getId() + " values :" +
                                 recordBean.toString());
                }
            }
            return Response.ok(recordBeans).build();
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
        SecureAnalyticsDataService analyticsDataService = Utils.getSecureAnalyticsDataService();
        String username = authenticate(authHeader);
        if (requestBean != null) {
            AnalyticsDrillDownRequest request = Utils.createDrilldownRequest(requestBean, true);
            Map<String, List<DrillDownResultEntry>> result = analyticsDataService.drillDown(username, request);
            DrillDownResultBean resultBean = Utils.createDrillDownResultBean(result);
            return Response.ok(resultBean).build();
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
        SecureAnalyticsDataService analyticsDataService = Utils.getSecureAnalyticsDataService();
        String username = authenticate(authHeader);
        if (requestBean != null) {
            AnalyticsDrillDownRequest request = Utils.createDrilldownRequest(requestBean, false);
            Map<String, List<DrillDownResultEntry>> result = analyticsDataService.drillDown(username, request);
            DrillDownResultBean resultBean = Utils.createDrillDownResultBean(result);
            return Response.ok(resultBean).build();
        } else {
            throw new AnalyticsException("Drilldown parameters not provided");
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
		SecureAnalyticsDataService analyticsDataService = Utils.getSecureAnalyticsDataService();
        String username = authenticate(authHeader);
        if (queryBean != null) {
            int result = analyticsDataService.searchCount(username, queryBean.getTableName(),
                                                          queryBean.getLanguage(), queryBean.getQuery());
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
                                    @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
			throws AnalyticsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking waiting for indexing - timeout : " + seconds + " seconds");
        }
        SecureAnalyticsDataService analyticsDataService = Utils.getSecureAnalyticsDataService();
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
    public Response setTableSchema(@PathParam("tableName") String tableName,
                                   AnalyticsSchemaBean analyticsSchemaBean,
                                   @HeaderParam(AUTHORIZATION_HEADER) String authHeader)
            throws AnalyticsException {
        if (logger.isDebugEnabled()) {
            logger.debug("Invoking setTableSchema for tableName : " + tableName);
        }
        SecureAnalyticsDataService analyticsDataService = Utils.getSecureAnalyticsDataService();
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
     * @param tableName the table name of which the schema to be retreived
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
        SecureAnalyticsDataService analyticsDataService = Utils.getSecureAnalyticsDataService();
        String username = authenticate(authHeader);
        AnalyticsSchema analyticsSchema = analyticsDataService.getTableSchema(username, tableName);
        AnalyticsSchemaBean analyticsSchemaBean = Utils.createTableSchemaBean(analyticsSchema);
        return Response.ok(analyticsSchemaBean).build();
    }

    private String authenticate(String authHeader) throws AnalyticsException {

        String username;
        if (authHeader != null && authHeader.startsWith(Constants.BASIC_AUTH_HEADER)) {
            // Authorization: Basic base64credentials
            String base64Credentials = authHeader.substring(Constants.BASIC_AUTH_HEADER.length()).trim();
            String credentials = new String(Base64.decode(base64Credentials),
                                            Charset.forName(DEFAUL_CHARSETT));
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
}
