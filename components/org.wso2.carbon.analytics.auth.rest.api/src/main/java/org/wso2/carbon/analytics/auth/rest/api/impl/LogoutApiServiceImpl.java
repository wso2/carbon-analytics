/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.analytics.auth.rest.api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.auth.rest.api.LogoutApiService;
import org.wso2.carbon.analytics.auth.rest.api.NotFoundException;
import org.wso2.carbon.analytics.auth.rest.api.dto.ErrorDTO;
import org.wso2.carbon.analytics.auth.rest.api.internal.DataHolder;
import org.wso2.carbon.analytics.auth.rest.api.util.AuthRESTAPIConstants;
import org.wso2.carbon.analytics.auth.rest.api.util.AuthUtil;
import org.wso2.carbon.analytics.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.analytics.idp.client.core.utils.IdPClientConstants;
import org.wso2.carbon.stream.processor.common.utils.SPConstants;
import org.wso2.msf4j.Request;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

/**
 * Implementation for Logout API.
 */
public class LogoutApiServiceImpl extends LogoutApiService {

    private static final Logger LOG = LoggerFactory.getLogger(LogoutApiServiceImpl.class);

    @Override
    public Response logoutAppNamePost(String appName
            , Request request) throws NotFoundException {

        String trimmedAppName = appName.split("/\\|?")[0];
        String appContext = "/" + trimmedAppName;

        String accessToken = AuthUtil
                .extractTokenFromHeaders(request.getHeaders(), AuthRESTAPIConstants.WSO2_SP_TOKEN);
        if (accessToken != null) {
            try {
                Map<String, String> logoutProperties = new HashMap<>();
                logoutProperties.put(IdPClientConstants.APP_NAME, trimmedAppName);
                logoutProperties.put(IdPClientConstants.ACCESS_TOKEN, accessToken);

                DataHolder.getInstance().getIdPClient().logout(logoutProperties);

                // Lets invalidate all the cookies saved.
                NewCookie appContextCookie = AuthUtil
                        .cookieBuilder(SPConstants.WSO2_SP_TOKEN_2, "", appContext, true, true,
                                0);
                NewCookie logoutContextAccessToken = AuthUtil
                        .cookieBuilder(AuthRESTAPIConstants.WSO2_SP_TOKEN, "",
                                AuthRESTAPIConstants.LOGOUT_CONTEXT + appContext, true, true, 0);
                NewCookie loginContextRefreshToken = AuthUtil
                        .cookieBuilder(AuthRESTAPIConstants.WSO2_SP_REFRESH_TOKEN, "",
                                AuthRESTAPIConstants.LOGIN_CONTEXT + appContext, true, true, 0);

                return Response.ok()
                        .cookie(appContextCookie, logoutContextAccessToken,
                                loginContextRefreshToken)
                        .build();
            } catch (IdPClientException e) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Error in logout for uri '" + appName + "', with token, '" + accessToken + "'.", e);
                }
                ErrorDTO errorDTO = new ErrorDTO();
                errorDTO.setError(IdPClientConstants.Error.INTERNAL_SERVER_ERROR);
                errorDTO.setDescription("Error in logout for uri '" + appName + "', with token, '" + accessToken +
                        "'. Error : '" + e.getMessage() + "'");
                return Response.serverError().entity(errorDTO).build();
            }
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Unable to extract the access token from the request uri '" + appName + "'.");
        }
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setError(IdPClientConstants.Error.INTERNAL_SERVER_ERROR);
        errorDTO.setDescription("Invalid Authorization header. Please provide the Authorization header to proceed.");
        return Response.status(Response.Status.BAD_REQUEST).entity(errorDTO).build();
    }
}
