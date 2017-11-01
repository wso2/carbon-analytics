package org.wso2.carbon.analytics.auth.rest.api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.analytics.auth.rest.api.LogoutApiService;
import org.wso2.carbon.analytics.auth.rest.api.NotFoundException;
import org.wso2.carbon.analytics.auth.rest.api.dto.ErrorDTO;
import org.wso2.carbon.analytics.auth.rest.api.internal.DataHolder;
import org.wso2.carbon.analytics.auth.rest.api.util.AuthUtil;
import org.wso2.carbon.analytics.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.analytics.idp.client.core.utils.IdPClientConstants;
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
                .extractTokenFromHeaders(request.getHeaders(), IdPClientConstants.WSO2_SP_TOKEN_2);
        if (accessToken != null) {
            try {
                Map<String, String> logoutProperties = new HashMap<>();
                logoutProperties.put(IdPClientConstants.APP_NAME, trimmedAppName);
                logoutProperties.put(IdPClientConstants.ACCESS_TOKEN, accessToken);

                DataHolder.getInstance().getIdPClient().logout(logoutProperties);

                // Lets invalidate all the cookies saved.
                NewCookie appContextCookie = AuthUtil
                        .cookieBuilder(IdPClientConstants.WSO2_SP_TOKEN_2, "", appContext, true, true,
                                IdPClientConstants.COOKIE_EXPIRE_TIME);

                NewCookie refreshTokenCookie = AuthUtil
                        .cookieBuilder(IdPClientConstants.WSO2_SP_REFRESH_TOKEN_1, "", appContext, true, false,
                                IdPClientConstants.COOKIE_EXPIRE_TIME);
                NewCookie refreshTokenHttpOnlyCookie = AuthUtil
                        .cookieBuilder(IdPClientConstants.WSO2_SP_REFRESH_TOKEN_2, "", appContext, true, true,
                                IdPClientConstants.COOKIE_EXPIRE_TIME);

                return Response.ok()
                        .cookie(appContextCookie, refreshTokenCookie, refreshTokenHttpOnlyCookie)
                        .build();
            } catch (IdPClientException e) {
                LOG.error("Error in logout for uri '" + appName + "', with token, '" + accessToken + "'.", e);
                ErrorDTO errorDTO = new ErrorDTO();
                errorDTO.setError(IdPClientConstants.Error.INTERNAL_SERVER_ERROR);
                errorDTO.setDescription("Error in logout for uri '" + appName + "', with token, '" + accessToken +
                        "'. Error : '" + e.getMessage() + "'");
                return Response.serverError().entity(errorDTO).build();
            }
        }
        LOG.error("Unable to extract the access token from the request uri '" + appName + "'.");
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setError(IdPClientConstants.Error.INTERNAL_SERVER_ERROR);
        errorDTO.setDescription("Invalid Authorization header. Please provide the Authorization header to proceed.");
        return Response.status(Response.Status.UNAUTHORIZED).entity(errorDTO).build();
    }
}
