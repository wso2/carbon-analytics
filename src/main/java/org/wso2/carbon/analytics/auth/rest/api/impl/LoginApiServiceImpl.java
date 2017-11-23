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
import org.wso2.carbon.analytics.auth.rest.api.LoginApiService;
import org.wso2.carbon.analytics.auth.rest.api.NotFoundException;
import org.wso2.carbon.analytics.auth.rest.api.dto.ErrorDTO;
import org.wso2.carbon.analytics.auth.rest.api.dto.RedirectionDTO;
import org.wso2.carbon.analytics.auth.rest.api.dto.UserDTO;
import org.wso2.carbon.analytics.auth.rest.api.internal.DataHolder;
import org.wso2.carbon.analytics.auth.rest.api.util.AuthUtil;
import org.wso2.carbon.analytics.idp.client.core.api.IdPClient;
import org.wso2.carbon.analytics.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.analytics.idp.client.core.utils.IdPClientConstants;
import org.wso2.carbon.analytics.idp.client.external.ExternalIdPClient;
import org.wso2.carbon.analytics.idp.client.external.ExternalIdPClientConstants;
import org.wso2.msf4j.Request;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

/**
 * Implementation for Login API.
 */
public class LoginApiServiceImpl extends LoginApiService {

    private static final Logger LOG = LoggerFactory.getLogger(LoginApiServiceImpl.class);

    @Override
    public Response loginAppNamePost(String appName
            , String username
            , String password
            , String grantType
            , Boolean rememberMe
            , Request request) throws NotFoundException {
        try {
            if (rememberMe == null) {
                rememberMe = false;
            }

            IdPClient idPClient = DataHolder.getInstance().getIdPClient();
            Map<String, String> idPClientProperties = new HashMap<>();

            UserDTO userDTO;
            RedirectionDTO redirectionDTO;

            String trimmedAppName = appName.split("/\\|?")[0];
            String appContext = "/" + trimmedAppName;

            idPClientProperties.put(IdPClientConstants.APP_NAME, trimmedAppName);
            idPClientProperties.put(IdPClientConstants.GRANT_TYPE, grantType);
            String refToken;
            if (IdPClientConstants.REFRESH_GRANT_TYPE.equals(grantType)) {
                refToken = AuthUtil
                        .extractTokenFromHeaders(request.getHeaders(), IdPClientConstants.WSO2_SP_REFRESH_TOKEN_2);
                if (refToken == null) {
                    LOG.error("Unable to extract refresh token from the header for the request '" + appName);
                    ErrorDTO errorDTO = new ErrorDTO();
                    errorDTO.setError(IdPClientConstants.Error.INVALID_CREDENTIALS);
                    errorDTO.setDescription("Invalid Authorization header. Please provide the Authorization " +
                            "header to proceed.");
                    return Response.status(Response.Status.UNAUTHORIZED).entity(errorDTO).build();
                } else {
                    idPClientProperties.put(IdPClientConstants.REFRESH_TOKEN, refToken);
                }
            } else if (IdPClientConstants.PASSWORD_GRANT_TYPE.equals(grantType)) {
                idPClientProperties.put(IdPClientConstants.USERNAME, username);
                idPClientProperties.put(IdPClientConstants.PASSWORD, password);
            } else {
                LOG.error("Grant type '" + grantType + "' is not supported.");
                ErrorDTO errorDTO = new ErrorDTO();
                errorDTO.setError(IdPClientConstants.Error.GRANT_TYPE_NOT_SUPPORTED);
                errorDTO.setDescription("Grant type '" + grantType + "' is not supported.");
                return Response.serverError().entity(errorDTO).build();
            }

            Map<String, String> loginResponse = idPClient.login(idPClientProperties);
            String loginStatus = loginResponse.get(IdPClientConstants.LOGIN_STATUS);

            switch (loginStatus) {
                case IdPClientConstants.LoginStatus.LOGIN_SUCCESS:
                    userDTO = new UserDTO();
                    userDTO.authUser(loginResponse.get(IdPClientConstants.AUTH_USER));

                    int validityPeriod = 0;
                    try {
                        validityPeriod = Integer.parseInt(loginResponse.get(IdPClientConstants.VALIDITY_PERIOD));
                    } catch (NumberFormatException e) {
                        LOG.error("Error in login to the uri '" + appName + "' in getting validity period of the " +
                                "session", e);
                        ErrorDTO errorDTO = new ErrorDTO();
                        errorDTO.setError(IdPClientConstants.Error.INTERNAL_SERVER_ERROR);
                        errorDTO.setDescription("Error in login to the uri '" + appName + "'. Error: " +
                                e.getMessage());
                        return Response.serverError().entity(errorDTO).build();
                    }
                    userDTO.validityPeriod(validityPeriod);

                    String accessToken = loginResponse.get(IdPClientConstants.ACCESS_TOKEN);
                    String refreshToken = loginResponse.get(IdPClientConstants.REFRESH_TOKEN);
                    // The access token is stored as two cookies in client side. One is a normal cookie and other
                    // is a http only cookie. Hence we need to split the access token
                    String part1 = accessToken.substring(0, accessToken.length() / 2);
                    String part2 = accessToken.substring(accessToken.length() / 2);
                    NewCookie accessTokenHttpAccessbile = AuthUtil
                            .cookieBuilder(IdPClientConstants.WSO2_SP_TOKEN_1, part1, appContext, true, false, "");
                    userDTO.setPartialAccessToken(part1);
                    NewCookie accessTokenhttpOnlyCookie = AuthUtil
                            .cookieBuilder(IdPClientConstants.WSO2_SP_TOKEN_2, part2, appContext, true, true, "");

                    if (refreshToken != null && rememberMe) {
                        NewCookie refreshTokenCookie, refreshTokenHttpOnlyCookie;
                        String refTokenPart1 = refreshToken.substring(0, refreshToken.length() / 2);
                        String refTokenPart2 = refreshToken.substring(refreshToken.length() / 2);
                        refreshTokenCookie = AuthUtil
                                .cookieBuilder(IdPClientConstants.WSO2_SP_REFRESH_TOKEN_1, refTokenPart1, appContext,
                                        true, false, "");
                        userDTO.setPartialRefreshToken(refTokenPart1);
                        refreshTokenHttpOnlyCookie = AuthUtil
                                .cookieBuilder(IdPClientConstants.WSO2_SP_REFRESH_TOKEN_2, refTokenPart2, appContext,
                                        true, true, "");
                        return Response.ok(userDTO, MediaType.APPLICATION_JSON)
                                .cookie(accessTokenHttpAccessbile, accessTokenhttpOnlyCookie,
                                        refreshTokenCookie, refreshTokenHttpOnlyCookie)
                                .build();
                    }
                    return Response.ok(userDTO, MediaType.APPLICATION_JSON)
                            .cookie(accessTokenHttpAccessbile, accessTokenhttpOnlyCookie)
                            .build();
                case IdPClientConstants.LoginStatus.LOGIN_FAILURE:
                    LOG.error("Authentication failure for user '" + username + "' when accessing uri '" + appName);
                    ErrorDTO errorDTO = new ErrorDTO();
                    errorDTO.setError(IdPClientConstants.Error.INVALID_CREDENTIALS);
                    errorDTO.setDescription("Username or Password is invalid. Please check again.");
                    return Response.status(Response.Status.UNAUTHORIZED).entity(errorDTO).build();
                default:
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Authentication redirection for the uri '" + appName);
                    }
                    redirectionDTO = new RedirectionDTO();
                    redirectionDTO.setClientId(loginResponse.get(ExternalIdPClientConstants.CLIENT_ID));
                    redirectionDTO.setCallbackUrl(loginResponse.get(ExternalIdPClientConstants.CALLBACK_URL_NAME));
                    redirectionDTO.setRedirectUrl(loginResponse.get(ExternalIdPClientConstants.REDIRECT_URL));
                    return Response.status(Response.Status.FOUND).entity(redirectionDTO).build();
            }
        } catch (IdPClientException e) {
            LOG.error("Error in login to the uri '" + appName + "'", e);
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setError(IdPClientConstants.Error.INTERNAL_SERVER_ERROR);
            errorDTO.setDescription("Error in login to the uri '" + appName + "'. Error: " + e.getMessage());
            return Response.serverError().entity(errorDTO).build();
        }
    }

    @Override
    public Response loginCallbackAppNameGet(String appName, Request request) throws NotFoundException {
        IdPClient idPClient = DataHolder.getInstance().getIdPClient();
        if (idPClient instanceof ExternalIdPClient) {
            String trimmedAppName = appName.split("/\\|?")[0];
            String appContext = "/" + trimmedAppName;

            String requestUrl = (String) request.getProperty(ExternalIdPClientConstants.REQUEST_URL);
            String requestCode = requestUrl.substring(requestUrl.lastIndexOf("?code=") + 6);
            try {
                ExternalIdPClient oAuth2IdPClient = (ExternalIdPClient) idPClient;
                Map<String, String> authCodeloginResponse = oAuth2IdPClient.authCodeLogin(trimmedAppName, requestCode);
                String loginStatus = authCodeloginResponse.get(IdPClientConstants.LOGIN_STATUS);
                if (loginStatus.equals(IdPClientConstants.LoginStatus.LOGIN_SUCCESS)) {
                    UserDTO userDTO = new UserDTO();
                    userDTO.authUser(authCodeloginResponse.get(IdPClientConstants.AUTH_USER));
                    String accessToken = authCodeloginResponse.get(IdPClientConstants.ACCESS_TOKEN);
                    // The access token is stored as two cookies in client side. One is a normal cookie and other
                    // is a http only cookie. Hence we need to split the access token
                    String part1 = accessToken.substring(0, accessToken.length() / 2);
                    String part2 = accessToken.substring(accessToken.length() / 2);
                    NewCookie accessTokenHttpAccessbile = AuthUtil
                            .cookieBuilder(IdPClientConstants.WSO2_SP_TOKEN_1, part1, appContext, true, false, "");
                    userDTO.setPartialAccessToken(part1);
                    NewCookie accessTokenhttpOnlyCookie = AuthUtil
                            .cookieBuilder(IdPClientConstants.WSO2_SP_TOKEN_2, part2, appContext, true, true, "");

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Login callback uri '" + appName + "' is redirected to '" +
                                authCodeloginResponse.get(ExternalIdPClientConstants.REDIRECT_URL));
                    }

                    URI targetURIForRedirection = new URI(authCodeloginResponse
                            .get(ExternalIdPClientConstants.REDIRECT_URL));
                    return Response.status(Response.Status.FOUND)
                            .header(HttpHeaders.LOCATION, targetURIForRedirection)
                            .entity(userDTO)
                            .cookie(accessTokenHttpAccessbile, accessTokenhttpOnlyCookie)
                            .build();
                } else {
                    LOG.error("Unable to get the token from the returned code '" + requestCode + "', for callback " +
                            "uri '" + appName + "'");
                    ErrorDTO errorDTO = new ErrorDTO();
                    errorDTO.setError(IdPClientConstants.Error.INVALID_CREDENTIALS);
                    errorDTO.setDescription("Unable to get the token from the returned code '" + requestCode + "'");
                    return Response.status(Response.Status.UNAUTHORIZED).entity(errorDTO).build();
                }

            } catch (URISyntaxException e) {
                LOG.error("Error in redirecting uri '" + appName + "' for auth code grant type login.", e);
                ErrorDTO errorDTO = new ErrorDTO();
                errorDTO.setError(IdPClientConstants.Error.INTERNAL_SERVER_ERROR);
                errorDTO.setDescription("Error in redirecting uri for auth code grant type login. Error: '"
                        + e.getMessage() + "'.");
                return Response.serverError().entity(errorDTO).build();
            } catch (IdPClientException e) {
                LOG.error("Error in accessing token from the code '" + requestCode + "', for uri '" + appName, e);
                ErrorDTO errorDTO = new ErrorDTO();
                errorDTO.setDescription("Error in accessing token from the code for uri '" + appName + "'. Error : '"
                        + e.getMessage() + "'");
                return Response.serverError().entity(errorDTO).build();
            }
        } else {
            String errorMsg = "This API is only supported for External IS integration with OAuth2 support. " +
                    "IdPClient found is '" + idPClient.getClass().getName();
            LOG.error(errorMsg);
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setError(IdPClientConstants.Error.INTERNAL_SERVER_ERROR);
            errorDTO.setDescription(errorMsg);
            return Response.serverError().entity(errorDTO).build();
        }
    }
}
