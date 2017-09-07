package org.wso2.carbon.event.simulator.core.api;

import org.wso2.carbon.event.simulator.core.api.*;
import org.wso2.carbon.event.simulator.core.model.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;


import java.util.List;

import org.wso2.carbon.event.simulator.core.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
                            date = "2017-07-20T09:30:14.336Z")
public abstract class DatabaseApiService {
    public abstract Response getDatabaseTableColumns(DBConnectionModel body, String tableName) throws NotFoundException;

    public abstract Response getDatabaseTables(DBConnectionModel body) throws NotFoundException;

    public abstract Response testDBConnection(DBConnectionModel body) throws NotFoundException;
}
