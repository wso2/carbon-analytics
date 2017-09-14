package org.wso2.carbon.status.dashboard.core.util;

/**
 * .
 */

import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
/**
 * .
 */
public class RestCallUtilImpl {

    public HttpResponse loginRequest(URI uri, String username, String password, MediaType acceptContentType)
            throws APIManagementException {
        if (uri == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        if (username == null) {
            throw new IllegalArgumentException("Username must not be null");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password must not be null");
        }

        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection) uri.toURL().openConnection();
            httpConnection.setRequestMethod(APIMgtConstants.FunctionsConstants.POST);
            httpConnection.setRequestProperty(APIMgtConstants.FunctionsConstants.CONTENT_TYPE,
                    MediaType.APPLICATION_JSON);
            httpConnection.setDoOutput(true);
            if (acceptContentType != null) {
                httpConnection.setRequestProperty(APIMgtConstants.FunctionsConstants.ACCEPT,
                        acceptContentType.toString());
            }

            JSONObject loginInfoJsonObj = new JSONObject();
            loginInfoJsonObj.put(APIMgtConstants.FunctionsConstants.USERNAME, username);
            loginInfoJsonObj.put(APIMgtConstants.FunctionsConstants.PASSWORD, password);

            OutputStream outputStream = httpConnection.getOutputStream();
            outputStream.write(loginInfoJsonObj.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();

            return getResponse(httpConnection);
        } catch (IOException e) {
            throw new APIManagementException("Connection not established properly ", e);
        } catch (JSONException e) {
            throw new APIManagementException("Json error ", e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }


    /**
     * {@inheritDoc}
     */

    public HttpResponse getRequest(URI uri, MediaType acceptContentType, List<String> cookies)
            throws APIManagementException {
        if (uri == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection) uri.toURL().openConnection();
            httpConnection.setRequestMethod(APIMgtConstants.FunctionsConstants.GET);
            httpConnection.setDoOutput(true);
            if (acceptContentType != null) {
                httpConnection.setRequestProperty(APIMgtConstants.FunctionsConstants.ACCEPT,
                        acceptContentType.toString());
            }

            if (cookies != null && !cookies.isEmpty()) {
                for (String cookie : cookies) {
                    httpConnection.addRequestProperty(APIMgtConstants.FunctionsConstants.COOKIE,
                            cookie.split(";", 2)[0]);
                }
            }
            return getResponse(httpConnection);
        } catch (IOException e) {
            throw new APIManagementException("Connection not established properly ", e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    /**
     * {@inheritDoc}
     */

    public HttpResponse postRequest(URI uri, MediaType acceptContentType, List<String> cookies,
                                    Entity entity, MediaType payloadContentType) throws APIManagementException {
        if (uri == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null");
        }
        if (payloadContentType == null) {
            throw new IllegalArgumentException("Payload content type must not be null");
        }
        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection) uri.toURL().openConnection();
            httpConnection.setRequestMethod(APIMgtConstants.FunctionsConstants.POST);
            httpConnection.setRequestProperty(APIMgtConstants.FunctionsConstants.CONTENT_TYPE,
                    payloadContentType.toString());
            httpConnection.setDoOutput(true);
            if (acceptContentType != null) {
                httpConnection.setRequestProperty(APIMgtConstants.FunctionsConstants.ACCEPT,
                        acceptContentType.toString());
            }

            if (cookies != null && !cookies.isEmpty()) {
                for (String cookie : cookies) {
                    httpConnection.addRequestProperty(APIMgtConstants.FunctionsConstants.COOKIE,
                            cookie.split(";", 2)[0]);
                }
            }

            OutputStream outputStream = httpConnection.getOutputStream();
            outputStream.write(entity.getEntity().toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();

            return getResponse(httpConnection);
        } catch (IOException e) {
            throw new APIManagementException("Connection not established properly ", e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    /**
     * {@inheritDoc}
     */

    public HttpResponse putRequest(URI uri, MediaType acceptContentType, List<String> cookies,
                                   Entity entity, MediaType payloadContentType) throws APIManagementException {
        if (uri == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }
        if (entity == null) {
            throw new IllegalArgumentException("Entity must not be null");
        }
        if (payloadContentType == null) {
            throw new IllegalArgumentException("Payload content type must not be null");
        }
        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection) uri.toURL().openConnection();
            httpConnection.setRequestMethod(APIMgtConstants.FunctionsConstants.PUT);
            httpConnection.setRequestProperty(APIMgtConstants.FunctionsConstants.CONTENT_TYPE,
                    payloadContentType.toString());
            httpConnection.setDoOutput(true);
            if (acceptContentType != null) {
                httpConnection.setRequestProperty(APIMgtConstants.FunctionsConstants.ACCEPT,
                        acceptContentType.toString());
            }

            if (cookies != null && !cookies.isEmpty()) {
                for (String cookie : cookies) {
                    httpConnection.addRequestProperty(APIMgtConstants.FunctionsConstants.COOKIE,
                            cookie.split(";", 2)[0]);
                }
            }

            OutputStream outputStream = httpConnection.getOutputStream();
            outputStream.write(entity.toString().getBytes(StandardCharsets.UTF_8));
            outputStream.flush();
            outputStream.close();

            return getResponse(httpConnection);
        } catch (IOException e) {
            throw new APIManagementException("Connection not established properly ", e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    /**
     * {@inheritDoc}
     */

    public HttpResponse deleteRequest(URI uri, MediaType acceptContentType, List<String> cookies)
            throws APIManagementException {
        if (uri == null) {
            throw new IllegalArgumentException("The URI must not be null");
        }

        HttpURLConnection httpConnection = null;
        try {
            httpConnection = (HttpURLConnection) uri.toURL().openConnection();
            httpConnection.setRequestMethod(APIMgtConstants.FunctionsConstants.DELETE);
            httpConnection.setDoOutput(true);
            if (acceptContentType != null) {
                httpConnection.setRequestProperty(APIMgtConstants.FunctionsConstants.ACCEPT,
                        acceptContentType.toString());
            }

            if (cookies != null && !cookies.isEmpty()) {
                for (String cookie : cookies) {
                    httpConnection.addRequestProperty(APIMgtConstants.FunctionsConstants.COOKIE,
                            cookie.split(";", 2)[0]);
                }
            }

            return getResponse(httpConnection);
        } catch (IOException e) {
            throw new APIManagementException("Connection not established properly ", e);
        } finally {
            if (httpConnection != null) {
                httpConnection.disconnect();
            }
        }
    }

    /**
     * To get a response from service.
     *
     * @param httpConnection Connection used to make the request
     * @return HttpResponse from service
     * @throws IOException In case of any failures, when trying to get the response from service
     */
    private HttpResponse getResponse(HttpURLConnection httpConnection) throws IOException {
        HttpResponse response = new HttpResponse();
        response.setResponseCode(httpConnection.getResponseCode());
        response.setResponseMessage(httpConnection.getResponseMessage());
        if (response.getResponseCode() / 100 == 2) {
            try (BufferedReader responseBuffer =
                         new BufferedReader(new InputStreamReader(httpConnection.getInputStream(),
                                 StandardCharsets.UTF_8))) {
                StringBuilder results = new StringBuilder();
                String line;
                while ((line = responseBuffer.readLine()) != null) {
                    results.append(line).append("\n");
                }
                response.setHeaderFields(httpConnection.getHeaderFields());
                response.setResults(results.toString());
            }
        }
        return response;
    }
}
