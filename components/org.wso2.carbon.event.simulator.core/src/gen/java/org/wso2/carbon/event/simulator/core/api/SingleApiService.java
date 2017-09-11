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

@javax.annotation.Generated(value = "org.wso2.status.dashboard.core.codegen.languages.JavaMSF4JServerCodegen",
                            date = "2017-07-20T09:30:14.336Z")
public abstract class SingleApiService {
    public abstract Response runSingleSimulation(String body) throws NotFoundException;
}
