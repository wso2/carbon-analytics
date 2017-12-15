package org.wso2.carbon.event.simulator.core.impl;

import org.wso2.carbon.event.simulator.core.api.*;
import org.wso2.carbon.event.simulator.core.exception.InsufficientAttributesException;
import org.wso2.carbon.event.simulator.core.exception.InvalidConfigException;
import org.wso2.carbon.event.simulator.core.internal.generator.SingleEventGenerator;
import org.wso2.carbon.event.simulator.core.model.*;


import java.util.List;

import org.wso2.carbon.event.simulator.core.api.NotFoundException;

import java.io.InputStream;

import org.wso2.carbon.stream.processor.common.exception.ResourceNotFoundException;
import org.wso2.carbon.stream.processor.common.exception.ResponseMapper;
import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
                            date = "2017-07-20T09:30:14.336Z")
public class SingleApiServiceImpl extends SingleApiService {
    @Override
    public Response runSingleSimulation(String singleEventConfiguration) {
        try {
            SingleEventGenerator.sendEvent(singleEventConfiguration);
            return Response.ok().header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.OK, "Single Event simulation started successfully"))
                    .build();
        } catch (InvalidConfigException | InsufficientAttributesException | ResourceNotFoundException e) {
            return Response.serverError().header("Access-Control-Allow-Origin", "*")
                    .entity(new ResponseMapper(Response.Status.INTERNAL_SERVER_ERROR, e.getMessage())).build();
        }
    }
}
