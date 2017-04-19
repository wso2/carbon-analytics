package org.wso2.carbon.event.simulator.core.service;

import javax.ws.rs.core.Response;

/**
 * ResponseMapper class is used to wrap responses of REST API's
 */
public class ResponseMapper {
    private Response.Status status;
    private String message;

    public ResponseMapper(Response.Status status, String message) {
        this.status = status;
        this.message = message;
    }

    public Response.Status getStatus() {
        return status;
    }

    public void setStatus(Response.Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
