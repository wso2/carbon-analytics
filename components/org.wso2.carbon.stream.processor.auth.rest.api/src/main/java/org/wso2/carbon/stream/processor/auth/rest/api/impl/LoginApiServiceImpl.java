package org.wso2.carbon.stream.processor.auth.rest.api.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.stream.processor.auth.rest.api.LoginApiService;
import org.wso2.carbon.stream.processor.auth.rest.api.NotFoundException;
import org.wso2.carbon.stream.processor.auth.rest.api.dto.ErrorDTO;
import org.wso2.carbon.stream.processor.auth.rest.api.dto.RedirectionDTO;
import org.wso2.carbon.stream.processor.auth.rest.api.dto.UserDTO;
import org.wso2.carbon.stream.processor.auth.rest.api.internal.DataHolder;
import org.wso2.carbon.stream.processor.auth.rest.api.util.AuthUtil;
import org.wso2.carbon.stream.processor.idp.client.core.api.IdPClient;
import org.wso2.carbon.stream.processor.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.stream.processor.idp.client.core.utils.IdPClientConstants;
import org.wso2.carbon.stream.processor.idp.client.core.utils.LoginStatus;
import org.wso2.carbon.stream.processor.idp.client.external.ExternalIdPClient;
import org.wso2.carbon.stream.processor.idp.client.external.ExternalIdPClientConstants;
import org.wso2.msf4j.Request;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

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

            UserDTO UserDTO;
            RedirectionDTO RedirectionDTO;

            String appContext = "/" + appName;
            idPClientProperties.put(IdPClientConstants.APP_NAME, appName);
            idPClientProperties.put(IdPClientConstants.USERNAME, username);
            idPClientProperties.put(IdPClientConstants.PASSWORD, password);
            idPClientProperties.put(IdPClientConstants.GRANT_TYPE, grantType);

            String refToken;
            if (IdPClientConstants.REFRESH_GRANT_TYPE.equals(grantType)) {
                refToken = AuthUtil
                        .extractTokenFromHeaders(request.getHeaders(), IdPClientConstants.WSO2_SP_REFRESH_TOKEN_2);
                if (refToken == null) {
                    ErrorDTO errorDTO = new ErrorDTO();
                    errorDTO.setMessage("Invalid Authorization header. Please provide the Authorization header to proceed.");
                    return Response.status(Response.Status.UNAUTHORIZED).entity(errorDTO).build();
                } else {
                    idPClientProperties.put(IdPClientConstants.REFRESH_TOKEN, refToken);
                }
            }

            Map<String, String> loginResponse = idPClient.login(idPClientProperties);
            String loginStatus = loginResponse.get(IdPClientConstants.LOGIN_STATUS);

            if (loginStatus.equals(LoginStatus.SUCCESSFUL.name())) {
                UserDTO = new UserDTO();
                UserDTO.id(loginResponse.get(IdPClientConstants.USER_ID));
                UserDTO.authUser(loginResponse.get(IdPClientConstants.AUTH_USER));
                UserDTO.isTokenValid(true);

                String accessToken = loginResponse.get(IdPClientConstants.ACCESS_TOKEN);
                String refreshToken = loginResponse.get(IdPClientConstants.REFRESH_TOKEN);
                // The access token is stored as two cookies in client side. One is a normal cookie and other is a http
                // only cookie. Hence we need to split the access token
                String part1 = accessToken.substring(0, accessToken.length() / 2);
                String part2 = accessToken.substring(accessToken.length() / 2);
                NewCookie accessTokenHttpAccessbile = AuthUtil
                        .cookieBuilder(IdPClientConstants.WSO2_SP_TOKEN_1, part1, appContext, true, false, "");
                UserDTO.setPartialAccessToken(part1);
                NewCookie accessTokenhttpOnlyCookie = AuthUtil
                        .cookieBuilder(IdPClientConstants.WSO2_SP_TOKEN_2, part2, appContext, true, true, "");

                NewCookie refreshTokenCookie = null, refreshTokenHttpOnlyCookie = null;
                if (refreshToken != null && rememberMe) {
                    String refTokenPart1 = refreshToken.substring(0, refreshToken.length() / 2);
                    String refTokenPart2 = refreshToken.substring(refreshToken.length() / 2);
                    refreshTokenCookie = AuthUtil
                            .cookieBuilder(IdPClientConstants.WSO2_SP_REFRESH_TOKEN_1, refTokenPart1, appContext,
                                    true, false, "");
                    UserDTO.setPartialRefreshToken(refTokenPart1);
                    refreshTokenHttpOnlyCookie = AuthUtil
                            .cookieBuilder(IdPClientConstants.WSO2_SP_REFRESH_TOKEN_2, refTokenPart2, appContext,
                                    true, true, "");
                    return Response.ok(UserDTO, MediaType.APPLICATION_JSON)
                            .cookie(accessTokenHttpAccessbile, accessTokenhttpOnlyCookie,
                                    refreshTokenCookie, refreshTokenHttpOnlyCookie)
                            .build();
                }
                return Response.ok(UserDTO, MediaType.APPLICATION_JSON)
                        .cookie(accessTokenHttpAccessbile, accessTokenhttpOnlyCookie)
                        .build();
            } else if (loginStatus.equals(LoginStatus.FAILURE.name())) {
                ErrorDTO errorDTO = new ErrorDTO();
                errorDTO.setMessage("Username or Password is invalid. Please check again.");
                return Response.status(Response.Status.UNAUTHORIZED).entity(errorDTO).build();
            } else {
                RedirectionDTO = new RedirectionDTO();
                RedirectionDTO.setClientId(loginResponse.get(ExternalIdPClientConstants.CLIENT_ID));
                RedirectionDTO.setCallbackUrl(loginResponse.get(ExternalIdPClientConstants.CALLBACK_URL_NAME));
                RedirectionDTO.setRedirectUrl(loginResponse.get(ExternalIdPClientConstants.REDIRECT_URL));
                return Response.status(Response.Status.FOUND).entity(RedirectionDTO).build();
            }
        } catch (IdPClientException e) {
            ErrorDTO errorDTO = new ErrorDTO();
            errorDTO.setMessage(e.getMessage());
            LOG.error(e.getMessage(), e);
            return Response.serverError().entity(errorDTO).build();
        }
    }

    @Override
    public Response loginCallbackAppNameGet(String appName, Request request) throws NotFoundException {
        IdPClient idPClient = DataHolder.getInstance().getIdPClient();
        if (idPClient instanceof ExternalIdPClient) {
            try {
                ExternalIdPClient oAuth2IdPClient = (ExternalIdPClient) idPClient;
                String appContext = "/" + appName;
                String requestUrl = (String) request.getProperty(ExternalIdPClientConstants.REQUEST_URL);
                String requestCode = requestUrl.substring(requestUrl.lastIndexOf("?code=") + 6);
                Map<String, String> authCodeloginResponse = oAuth2IdPClient.authCodelogin(appContext, requestCode);
                String loginStatus = authCodeloginResponse.get(IdPClientConstants.LOGIN_STATUS);
                if (loginStatus.equals(LoginStatus.SUCCESSFUL.name())) {
                    UserDTO userDTO = new UserDTO();
                    userDTO.authUser(authCodeloginResponse.get(IdPClientConstants.AUTH_USER));
                    userDTO.isTokenValid(true);
                    String accessToken = authCodeloginResponse.get(IdPClientConstants.ACCESS_TOKEN);
                    // The access token is stored as two cookies in client side. One is a normal cookie and other is a http
                    // only cookie. Hence we need to split the access token
                    String part1 = accessToken.substring(0, accessToken.length() / 2);
                    String part2 = accessToken.substring(accessToken.length() / 2);
                    NewCookie accessTokenHttpAccessbile = AuthUtil
                            .cookieBuilder(IdPClientConstants.WSO2_SP_TOKEN_1, part1, appContext, true, false, "");
                    userDTO.setPartialAccessToken(part1);
                    NewCookie accessTokenhttpOnlyCookie = AuthUtil
                            .cookieBuilder(IdPClientConstants.WSO2_SP_TOKEN_2, part2, appContext, true, true, "");

                    URI targetURIForRedirection = new URI(authCodeloginResponse.get(ExternalIdPClientConstants.REDIRECT_URL));
                    return Response.status(Response.Status.FOUND)
                            .header(HttpHeaders.LOCATION, targetURIForRedirection)
                            .entity(userDTO)
                            .cookie(accessTokenHttpAccessbile, accessTokenhttpOnlyCookie)
                            .build();
                } else {
                    ErrorDTO errorDTO = new ErrorDTO();
                    errorDTO.setMessage("Unable to get the token from the returned code/");
                    return Response.status(Response.Status.UNAUTHORIZED).entity(errorDTO).build();
                }
            } catch (URISyntaxException e) {
                ErrorDTO errorDTO = new ErrorDTO();
                String errorMsg = "Error in redirecting uri for auth code grant type login.";
                errorDTO.setMessage(errorMsg);
                LOG.error(errorMsg);
                return Response.serverError().entity(errorDTO).build();
            } catch (IdPClientException e) {
                ErrorDTO errorDTO = new ErrorDTO();
                errorDTO.setMessage(e.getMessage());
                LOG.error(e.getMessage(), e);
                return Response.serverError().entity(errorDTO).build();
            }
        } else {
            ErrorDTO errorDTO = new ErrorDTO();
            String errorMsg = "This API is only supported for External IS integration with OAuth2 support. " +
                    "IdPClient found is '" + idPClient.getClass().getName();
            errorDTO.setMessage(errorMsg);
            LOG.error(errorMsg);
            return Response.serverError().entity(errorDTO).build();
        }
    }
}
