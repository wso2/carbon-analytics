package org.wso2.carbon.analytics.auth.rest.api;

import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

/**
 * Logout API service.
 */
public abstract class LogoutApiService {
    public abstract Response logoutAppNamePost(String appName
            , Request request) throws NotFoundException;
}
