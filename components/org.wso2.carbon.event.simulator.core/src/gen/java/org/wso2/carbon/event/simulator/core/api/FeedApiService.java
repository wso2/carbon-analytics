package org.wso2.carbon.event.simulator.core.api;

import org.wso2.carbon.event.simulator.core.api.*;
import org.wso2.carbon.event.simulator.core.model.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import org.wso2.carbon.event.simulator.core.model.InlineResponse200;

import java.util.List;

import org.wso2.carbon.event.simulator.core.api.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
                            date = "2017-07-20T09:30:14.336Z")
public abstract class FeedApiService {
    public abstract Response addFeedSimulation(String body) throws NotFoundException;

    public abstract Response deleteFeedSimulation(String simulationName) throws NotFoundException;

    public abstract Response getFeedSimulation(String simulationName) throws NotFoundException;

    public abstract Response getFeedSimulations() throws NotFoundException;

    public abstract Response operateFeedSimulation(String action, String simulationName) throws NotFoundException;

    public abstract Response updateFeedSimulation(String simulationName, String body) throws NotFoundException;

    public abstract Response getFeedSimulationStatus(String simulationName) throws NotFoundException;

}
