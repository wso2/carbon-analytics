package org.wso2.carbon.status.dashboard.core.api;

import org.wso2.carbon.status.dashboard.core.model.Worker;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-09-11T07:55:11.886Z")
public abstract class WorkersApiService {
    public abstract Response addWorker(Worker worker
 ) throws NotFoundException;
    public abstract Response deleteWorker(String id
 ) throws NotFoundException;
    public abstract Response getAppHistory(String id
 ,String appName
 ,String period
 ,String type
 ) throws NotFoundException;
    public abstract Response getComponetHistory(String id
 ,String appName
 ,String componentId
 ,String period
 ,String type
 ) throws NotFoundException;
    public abstract Response getSiddhiAppDetails(String id
 ,String appName
 ) throws NotFoundException;
    public abstract Response getSiddhiApps(String id
 ) throws NotFoundException;
    public abstract Response getWorkerConfig(String id
 ) throws NotFoundException;
    public abstract Response getWorkerGeneral(String id
 ) throws NotFoundException;
    public abstract Response getWorkerHistory(String id
 ,String period
 ,String type
 ) throws NotFoundException;
    public abstract Response testConnection(Worker auth
 ,String id
 ) throws NotFoundException;

    public abstract Response updateWorker(String id
 ) throws NotFoundException;
    public abstract Response workersGet() throws NotFoundException;
}
