package org.wso2.carbon.status.dashboard.core.util;

import java.util.List;
import java.util.Map;

/**
 * Class to represents basic Http response properties.
 */
public class HttpResponse {

    private int responseCode;
    private String responseMessage;
    private Map<String, List<String>> headerFields;
    private String results;

    /**
     * Constructor.
     */
    public HttpResponse() {

    }

    /**
     * To get response code.
     *
     * @return Response code (int value)
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * To set a response code.
     *
     * @param responseCode Response code (int value)
     */
    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    /**
     * To get response message.
     *
     * @return Response message
     */
    public String getResponseMessage() {
        return responseMessage;
    }

    /**
     * To set a response message.
     *
     * @param responseMessage Response message
     */
    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    /**
     * To get all the header fields as a {@code Map<String, List<String>>}.
     *
     * @return Map of header fields
     */
    public Map<String, List<String>> getHeaderFields() {
        return headerFields;
    }

    /**
     * To set header fields.
     *
     * @param headerFields Map of header fields
     */
    public void setHeaderFields(Map<String, List<String>> headerFields) {
        this.headerFields = headerFields;
    }

    /**
     * To get http results.
     *
     * @return Results String
     */
    public String getResults() {
        return results;
    }

    /**
     * To set http results.
     *
     * @param results Results String
     */
    public void setResults(String results) {
        this.results = results;
    }
}