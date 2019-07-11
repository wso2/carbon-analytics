/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.simulator.core.impl;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.PoolInitializationException;
import org.wso2.carbon.analytics.permissions.PermissionProvider;
import org.wso2.carbon.analytics.permissions.bean.Permission;
import org.wso2.carbon.event.simulator.core.api.DatabaseApiService;
import org.wso2.carbon.event.simulator.core.api.NotFoundException;
import org.wso2.carbon.event.simulator.core.internal.generator.database.util.DatabaseConnector;
import org.wso2.carbon.event.simulator.core.model.DBConnectionModel;
import org.wso2.carbon.event.simulator.core.service.EventSimulatorDataHolder;
import org.wso2.carbon.streaming.integrator.common.exception.ResponseMapper;
import org.wso2.msf4j.Request;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import javax.ws.rs.core.Response;


@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
                            date = "2017-07-20T09:30:14.336Z")
public class DatabaseApiServiceImpl extends DatabaseApiService {

    private static final String PERMISSION_APP_NAME = "SIM";
    private static final String MANAGE_SIMULATOR_PERMISSION_STRING = "simulator.manage";
    private static final String VIEW_SIMULATOR_PERMISSION_STRING = "simulator.view";

    public Response getDatabaseTableColumns(DBConnectionModel connectionDetails,
                                            String tableName) throws NotFoundException {
        try {
            List<String> columnNames = DatabaseConnector.retrieveColumnNames(connectionDetails, tableName);
            return Response.status(Response.Status.OK)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept")
                    .entity(columnNames)
                    .build();
        } catch (Throwable e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept")
                    .entity(new ResponseMapper(Response.Status.BAD_REQUEST, e.getMessage()))
                    .build();
        }
    }

    public Response getDatabaseTables(DBConnectionModel connectionDetails) throws NotFoundException {
        try {
            List<String> tableNames = DatabaseConnector.retrieveTableNames(connectionDetails);
            return Response.status(Response.Status.OK)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept")
                    .entity(tableNames)
                    .build();
        } catch (Throwable e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept")
                    .entity(new ResponseMapper(Response.Status.BAD_REQUEST, e.getMessage()))
                    .build();
        }
    }

    public Response testDBConnection(DBConnectionModel connectionDetails) throws NotFoundException {
        try {
            HikariDataSource dataSource = DatabaseConnector.initializeDatasource(connectionDetails);
            Connection dbConnection = dataSource.getConnection();
            dbConnection.close();
            dataSource.close();
            return Response.status(Response.Status.OK)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept")
                    .entity(new ResponseMapper(Response.Status.OK, "Successfully connected to datasource '"
                            + connectionDetails.getDataSourceLocation() + "'."))
                    .build();
        } catch (PoolInitializationException | SQLException e) {
//            if any exception occurs, inform client that the database connection failed
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .header("Access-Control-Allow-Origin", "*")
                    .header("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept")
                    .entity(new ResponseMapper(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage()))
                    .build();
        }
    }

    @Override
    public Response getDatabaseTableColumns(DBConnectionModel body, String tableName, Request request) throws NotFoundException {
        if (getUserName(request) != null && !(getPermissionProvider().hasPermission(getUserName(request), new Permission
                (PERMISSION_APP_NAME, MANAGE_SIMULATOR_PERMISSION_STRING)) || getPermissionProvider().hasPermission
                (getUserName(request), new Permission(PERMISSION_APP_NAME, VIEW_SIMULATOR_PERMISSION_STRING)))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permission to perform the action")
                    .build();
        }
        return getDatabaseTableColumns(body, tableName);
    }

    @Override
    public Response getDatabaseTables(DBConnectionModel body, Request request) throws NotFoundException {
        if (getUserName(request) != null && !(getPermissionProvider().hasPermission(getUserName(request), new Permission
                (PERMISSION_APP_NAME, MANAGE_SIMULATOR_PERMISSION_STRING)) || getPermissionProvider().hasPermission
                (getUserName(request), new Permission(PERMISSION_APP_NAME, VIEW_SIMULATOR_PERMISSION_STRING)))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permission to perform the action")
                    .build();
        }
        return getDatabaseTables(body);
    }

    @Override
    public Response testDBConnection(DBConnectionModel body, Request request) throws NotFoundException {
        if (getUserName(request) != null && !(getPermissionProvider().hasPermission(getUserName(request), new Permission
                (PERMISSION_APP_NAME, MANAGE_SIMULATOR_PERMISSION_STRING)) || getPermissionProvider().hasPermission
                (getUserName(request), new Permission(PERMISSION_APP_NAME, VIEW_SIMULATOR_PERMISSION_STRING)))) {
            return Response.status(Response.Status.UNAUTHORIZED).entity("Insufficient permission to perform the action")
                    .build();
        }
        return testDBConnection(body);
    }

    private static String getUserName(Request request) {
        Object username = request.getProperty("username");
        return username != null ? username.toString() : null;
    }

    private PermissionProvider getPermissionProvider() {
        return EventSimulatorDataHolder.getPermissionProvider();
    }
}
