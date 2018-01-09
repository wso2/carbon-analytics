package org.wso2.carbon.event.simulator.core.api;

import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-07-20T09:30:14.336Z")
public abstract class FeedApiService {

    public abstract Response addFeedSimulation(String body, Request request) throws NotFoundException;

    public abstract Response deleteFeedSimulation(String simulationName, Request request) throws NotFoundException;

    public abstract Response getFeedSimulation(String simulationName, Request request) throws NotFoundException;

    public abstract Response getFeedSimulations(Request request) throws NotFoundException;

    public abstract Response operateFeedSimulation(String action, String simulationName, Request request) throws NotFoundException;

    public abstract Response updateFeedSimulation(String simulationName, String body, Request request) throws NotFoundException;

    public abstract Response getFeedSimulationStatus(String simulationName, Request request) throws NotFoundException;

}
