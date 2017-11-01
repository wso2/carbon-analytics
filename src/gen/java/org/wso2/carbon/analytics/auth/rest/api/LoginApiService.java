package org.wso2.carbon.analytics.auth.rest.api;

import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

/**
 * Login API Service class.
 */
public abstract class LoginApiService {
    public abstract Response loginAppNamePost(String appName
            , String username
            , String password
            , String grantType
            , Boolean rememberMe
            , Request request) throws NotFoundException;

    public abstract Response loginCallbackAppNameGet(String appName
            , Request request) throws NotFoundException;
}
