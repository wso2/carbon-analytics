package org.wso2.carbon.stream.processor.common.exception;

import org.json.JSONObject;

import java.util.List;

import javax.ws.rs.core.Response;

/**
 * ResponseMapper class is used to wrap responses of REST API's
 */
public class ResponseMapper {
    private Response.Status status;
    private String message;
    private JSONObject data;

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

    public JSONObject getData() {
        return data;
    }

    public void setData(JSONObject data) {
        this.data = data;
    }
}
