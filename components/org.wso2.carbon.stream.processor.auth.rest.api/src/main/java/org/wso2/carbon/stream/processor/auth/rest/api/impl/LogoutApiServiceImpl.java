package org.wso2.carbon.stream.processor.auth.rest.api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.stream.processor.auth.rest.api.LogoutApiService;
import org.wso2.carbon.stream.processor.auth.rest.api.NotFoundException;
import org.wso2.carbon.stream.processor.auth.rest.api.dto.ErrorDTO;
import org.wso2.carbon.stream.processor.auth.rest.api.internal.DataHolder;
import org.wso2.carbon.stream.processor.auth.rest.api.util.AuthUtil;
import org.wso2.carbon.stream.processor.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.stream.processor.idp.client.core.utils.IdPClientConstants;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

public class LogoutApiServiceImpl extends LogoutApiService {

    private static final Logger LOG = LoggerFactory.getLogger(LogoutApiServiceImpl.class);

    @Override
    public Response logoutAppNamePost(String appName
            , Request request) throws NotFoundException {
        String appContext = "/" + appName;

        String accessToken = AuthUtil
                .extractTokenFromHeaders(request.getHeaders(), IdPClientConstants.WSO2_SP_TOKEN_2);
        if (accessToken != null) {
            try {
                DataHolder.getInstance().getIdPClient().logout(accessToken);

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
                ErrorDTO errorDTO = new ErrorDTO();
                errorDTO.setMessage(e.getMessage());
                LOG.error(e.getMessage(), e);
                return Response.serverError().entity(errorDTO).build();
            }
        }
        ErrorDTO errorDTO = new ErrorDTO();
        errorDTO.setMessage("Invalid Authorization header. Please provide the Authorization header to proceed.");
        return Response.status(Response.Status.UNAUTHORIZED).entity(errorDTO).build();
    }
}
