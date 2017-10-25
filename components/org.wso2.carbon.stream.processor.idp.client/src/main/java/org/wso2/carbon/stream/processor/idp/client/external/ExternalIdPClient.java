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
package org.wso2.carbon.stream.processor.idp.client.external;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import feign.Response;
import feign.gson.GsonDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.stream.processor.idp.client.core.api.IdPClient;
import org.wso2.carbon.stream.processor.idp.client.core.exception.AuthenticationException;
import org.wso2.carbon.stream.processor.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.stream.processor.idp.client.core.models.Group;
import org.wso2.carbon.stream.processor.idp.client.core.utils.IdPClientConstants;
import org.wso2.carbon.stream.processor.idp.client.core.utils.LoginStatus;
import org.wso2.carbon.stream.processor.idp.client.external.dto.DCRClientInfo;
import org.wso2.carbon.stream.processor.idp.client.external.dto.DCRError;
import org.wso2.carbon.stream.processor.idp.client.external.dto.OAuth2IntrospectionResponse;
import org.wso2.carbon.stream.processor.idp.client.external.dto.OAuth2TokenInfo;
import org.wso2.carbon.stream.processor.idp.client.external.dto.SCIMGroupList;
import org.wso2.carbon.stream.processor.idp.client.external.impl.DCRMServiceStub;
import org.wso2.carbon.stream.processor.idp.client.external.impl.OAuth2ServiceStubs;
import org.wso2.carbon.stream.processor.idp.client.external.impl.SCIM2ServiceStub;
import org.wso2.carbon.stream.processor.idp.client.external.models.OAuthApplicationInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExternalIdPClient implements IdPClient {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalIdPClient.class);

    private DCRMServiceStub dcrmServiceStub;
    private OAuth2ServiceStubs oAuth2ServiceStubs;
    private SCIM2ServiceStub scimServiceStub;
    private String baseUrl;
    private String authorizeEndpoint;
    private String grantType;
    private String signingAlgo;
    private Long validaityPeriod;
    private OAuthApplicationInfo oAuthApplicationInfo;

    public ExternalIdPClient(String baseUrl, String authorizeEndpoint, String grantType, String singingAlgo,
                             Long validaityPeriod, DCRMServiceStub dcrmServiceStub,
                             OAuth2ServiceStubs oAuth2ServiceStubs, SCIM2ServiceStub scimServiceStub)
            throws IdPClientException {
        this.baseUrl = baseUrl;
        this.authorizeEndpoint = authorizeEndpoint;
        this.grantType = grantType;
        this.signingAlgo = singingAlgo;
        this.validaityPeriod = validaityPeriod;
        this.dcrmServiceStub = dcrmServiceStub;
        this.oAuth2ServiceStubs = oAuth2ServiceStubs;
        this.scimServiceStub = scimServiceStub;
        createDCRApplication();
    }

    @Override
    public List<Group> getAllGroups() throws IdPClientException {
        Response response = scimServiceStub.getGroups();
        if (response == null) {
            String errorMessage =
                    "Error occurred while retrieving groups. Error : Response is null.";
            LOG.error(errorMessage);
            throw new IdPClientException(errorMessage);
        }
        if (response.status() == 200) {
            try {
                SCIMGroupList scimGroups = (SCIMGroupList) new GsonDecoder().decode(response, SCIMGroupList.class);
                return scimGroups.getResources().stream().map((resource) -> {
                            Group group = new Group();
                            group.setId(resource.getId());
                            group.setDisplayName(resource.getDisplayName());
                            return group;
                        }
                ).collect(Collectors.toList());
            } catch (IOException e) {
                String errorMessage = "Error occurred while parsing the response when retrieving groups.";
                LOG.error(errorMessage, e);
                throw new IdPClientException(errorMessage, e);
            }
        } else {
            String errorMessage = "Error occurred while retrieving groups.";
            LOG.error(errorMessage);
            throw new IdPClientException(errorMessage);
        }
    }

    @Override
    public List<Group> getUsersGroups(String name) throws IdPClientException {
        List<Group> usersGroups = new ArrayList<>();
        Response userResponse = scimServiceStub.searchUser(ExternalIdPClientConstants.FILTER_PREFIX_USER + name);
        if (userResponse == null) {
            String errorMessage =
                    "Error occurred while retrieving user '" + name + "'. Error : Response is null.";
            LOG.error(errorMessage);
            throw new IdPClientException(errorMessage);
        }
        if (userResponse.status() == 200) {
            String responseBody = userResponse.body().toString();
            JsonParser parser = new JsonParser();
            JsonObject parsedResponseBody = (JsonObject) parser.parse(responseBody);
            JsonArray users = (JsonArray) parsedResponseBody.get(ExternalIdPClientConstants.RESOURCES);
            JsonObject scimUser = (JsonObject) users.get(0);
            JsonArray scimGroups = scimUser.get("groups").getAsJsonArray();
            scimGroups.forEach((scimGroup) -> {
                Group group = new Group();
                group.setDisplayName(((JsonObject) scimGroup).get("display").getAsString());
                usersGroups.add(group);
            });
        } else {
            String errorMessage =
                    "Error occurred while retrieving groups of user '" + name + "'. Error : " + getErrorMessage(
                            userResponse);
            LOG.error(errorMessage);
            throw new IdPClientException(errorMessage);
        }
        return usersGroups;
    }

    @Override
    public Map<String, String> login(Map<String, String> properties) throws IdPClientException {
        Map<String, String> returnProperties = new HashMap<>();
        Response response;

        String appName = properties.get(IdPClientConstants.APP_NAME);
        if (IdPClientConstants.AUTHORIZATION_CODE_GRANT_TYPE.equals(grantType)) {
            returnProperties.put(IdPClientConstants.LOGIN_STATUS, LoginStatus.REDIRECTION.name());
            returnProperties.put(IdPClientConstants.CLIENT_ID, this.oAuthApplicationInfo.getClientId());
            returnProperties.put(IdPClientConstants.REDIRECTION_URL, this.authorizeEndpoint);
            returnProperties.put(IdPClientConstants.CALLBACK_URL, this.baseUrl +
                    ExternalIdPClientConstants.CALLBACK_URL + appName);
            return returnProperties;
        } else if (IdPClientConstants.PASSWORD_GRANT_TYPE.equals(grantType)) {
            response = oAuth2ServiceStubs.getTokenServiceStub().generatePasswordGrantAccessToken(
                    properties.get(IdPClientConstants.USERNAME), properties.get(IdPClientConstants.PASSWORD),
                    null, this.validaityPeriod, this.oAuthApplicationInfo.getClientId(),
                    this.oAuthApplicationInfo.getClientSecret());
        } else {
            response = oAuth2ServiceStubs.getTokenServiceStub().generateRefreshGrantAccessToken(
                    properties.get(IdPClientConstants.REFRESH_TOKEN), null, this.validaityPeriod,
                    this.oAuthApplicationInfo.getClientId(), this.oAuthApplicationInfo.getClientSecret());
        }
        if (response == null) {
            throw new IdPClientException("Error occurred while generating an access token. " +
                    "Response is null.");
        }
        if (response.status() == 200) {   //200 - Success
            LOG.debug("A new access token is successfully generated.");
            try {
                OAuth2TokenInfo oAuth2TokenInfo = (OAuth2TokenInfo) new GsonDecoder().decode(response,
                        OAuth2TokenInfo.class);
                returnProperties.put(IdPClientConstants.LOGIN_STATUS, LoginStatus.SUCCESSFUL.name());
                returnProperties.put(IdPClientConstants.ACCESS_TOKEN, oAuth2TokenInfo.getAccessToken());
                returnProperties.put(IdPClientConstants.REFRESH_TOKEN, oAuth2TokenInfo.getRefreshToken());
                returnProperties.put(ExternalIdPClientConstants.TOKEN_ID, oAuth2TokenInfo.getIdToken());
                returnProperties.put(ExternalIdPClientConstants.EXPIRES_IN,
                        Long.toString(oAuth2TokenInfo.getExpiresIn()));
                return returnProperties;
            } catch (IOException e) {
                throw new IdPClientException("Error occurred while parsing token response", e);
            }
        } else if (response.status() == 401) {
            returnProperties.put(IdPClientConstants.LOGIN_STATUS, LoginStatus.FAILURE.name());
            return returnProperties;
        } else {  //Error case
            throw new IdPClientException("Token generation request failed. HTTP error code: " + response.status() +
                    " Error Response Body: " + response.body().toString());
        }
    }

    public Map<String, String> authCodelogin(String appcontext, String code) throws IdPClientException {
        Map<String, String> returnProperties = new HashMap<>();
        Response response = oAuth2ServiceStubs.getTokenServiceStub().generateAuthCodeGrantAccessToken(code,
                this.baseUrl + ExternalIdPClientConstants.CALLBACK_URL + appcontext, null,
                this.validaityPeriod, oAuthApplicationInfo.getClientId(), oAuthApplicationInfo.getClientSecret());
        if (response == null) {
            throw new IdPClientException("Error occurred while generating an access token from code. " +
                    "Response is null.");
        }
        if (response.status() == 200) {   //200 - Success
            LOG.debug("A new access token from code is successfully generated.");
            try {
                OAuth2TokenInfo oAuth2TokenInfo = (OAuth2TokenInfo) new GsonDecoder().decode(response,
                        OAuth2TokenInfo.class);
                returnProperties.put(IdPClientConstants.LOGIN_STATUS, LoginStatus.SUCCESSFUL.name());
                returnProperties.put(IdPClientConstants.ACCESS_TOKEN, oAuth2TokenInfo.getAccessToken());
                returnProperties.put(IdPClientConstants.REFRESH_TOKEN, oAuth2TokenInfo.getRefreshToken());
                returnProperties.put(ExternalIdPClientConstants.TOKEN_ID, oAuth2TokenInfo.getIdToken());
                returnProperties.put(ExternalIdPClientConstants.EXPIRES_IN,
                        Long.toString(oAuth2TokenInfo.getExpiresIn()));
                returnProperties.put(ExternalIdPClientConstants.REDIRECT_URL, this.baseUrl + appcontext);

                Response introspectToken = oAuth2ServiceStubs.getIntrospectionServiceStub()
                        .introspectToken(oAuth2TokenInfo.getAccessToken());
                if (introspectToken.status() == 200) {   //200 - Success
                    OAuth2IntrospectionResponse introspectResponse = (OAuth2IntrospectionResponse) new GsonDecoder()
                            .decode(introspectToken, OAuth2IntrospectionResponse.class);
                    String username = introspectResponse.getUsername();
                    String authUser = username.substring(0, username.indexOf("@carbon.super"));
                    returnProperties.put(IdPClientConstants.AUTH_USER, authUser);
                } else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Unable to get the username from introspection");
                    }
                }
                return returnProperties;
            } catch (IOException e) {
                throw new IdPClientException("Error occurred while parsing token response", e);
            }
        } else if (response.status() == 401) {
            returnProperties.put(IdPClientConstants.LOGIN_STATUS, LoginStatus.FAILURE.name());
            return returnProperties;
        } else {  //Error case
            throw new IdPClientException("Token generation request failed. HTTP error code: " + response.status() +
                    " Error Response Body: " + response.body().toString());
        }
    }

    @Override
    public void logout(String token) throws IdPClientException {
        oAuth2ServiceStubs.getRevokeServiceStub().revokeAccessToken(token, this.oAuthApplicationInfo.getClientId(),
                this.oAuthApplicationInfo.getClientSecret());
    }

    @Override
    public boolean authenticate(String token) throws AuthenticationException, IdPClientException {
        String[] splitToken = token.split(" ");
        String tokenType = splitToken[0];
        String trimmedtoken = splitToken[1];
        Response response;
        if (tokenType.equalsIgnoreCase(ExternalIdPClientConstants.BEARER_PREFIX)) {
            response = oAuth2ServiceStubs.getIntrospectionServiceStub().introspectToken(trimmedtoken);
        } else if (tokenType.equalsIgnoreCase(ExternalIdPClientConstants.BASIC_PREFIX)) {
            byte[] decodedAuthHeader = Base64.getDecoder().decode(trimmedtoken);
            String authHeader = new String(decodedAuthHeader);
            String userName = authHeader.split(":")[0];
            String password = authHeader.split(":")[1];
            response = oAuth2ServiceStubs.getTokenServiceStub().generatePasswordGrantAccessToken(
                    userName, password, null, this.validaityPeriod,
                    oAuthApplicationInfo.getClientId(), oAuthApplicationInfo.getClientSecret()
            );
        } else {
            throw new AuthenticationException("Authentication method '" + tokenType + "' not supported.");
        }
        if (response == null) {
            throw new IdPClientException("Error occurred while authenticating token. Response is null.");
        }
        try {
            if (response.status() == 200) {  //200 - OK
                if (tokenType.equalsIgnoreCase(ExternalIdPClientConstants.BEARER_PREFIX)) {
                    OAuth2IntrospectionResponse introspectResponse = (OAuth2IntrospectionResponse) new GsonDecoder()
                            .decode(response, OAuth2IntrospectionResponse.class);
                    if (introspectResponse.isActive()) {
                        return true;
                    } else {
                        throw new AuthenticationException("The token is not active");
                    }
                } else {
                    return true;
                }
            } else if (response.status() == 400) {  //400 - Known Error
                try {
                    DCRError error = (DCRError) new GsonDecoder().decode(response, DCRError.class);
                    throw new IdPClientException("Error occurred while introspecting the token. Error: " +
                            error.getError() + ". Error Description: " + error.getErrorDescription() +
                            ". Status Code: " + response.status());
                } catch (IOException e) {
                    throw new IdPClientException("Error occurred while parsing the Introspection error message.", e);
                }
            } else {  //Unknown Error
                throw new IdPClientException("Error occurred while autenticating. Error: " +
                        response.body().toString() + " Status Code: " + response.status());
            }
        } catch (IOException e) {
            throw new IdPClientException("Error occured while parsing the authentication response.");
        }
    }

    private void createDCRApplication() throws IdPClientException {
        List<String> grantTypes = new ArrayList<>();
        grantTypes.add(IdPClientConstants.PASSWORD_GRANT_TYPE);
        grantTypes.add(IdPClientConstants.AUTHORIZATION_CODE_GRANT_TYPE);
        grantTypes.add(IdPClientConstants.REFRESH_GRANT_TYPE);
        String callBackUrl = ExternalIdPClientConstants.REGEX_BASE_START + this.baseUrl +
                ExternalIdPClientConstants.CALLBACK_URL + ExternalIdPClientConstants.REGEX_BASE_END;
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating OAuth2 application.");
        }
        DCRClientInfo dcrClientInfo = new DCRClientInfo();
        dcrClientInfo.setClientName(ExternalIdPClientConstants.SP_APPLICATION_NAME);
        dcrClientInfo.setGrantTypes(grantTypes);
        dcrClientInfo.addCallbackUrl(callBackUrl);
        dcrClientInfo.setUserinfoSignedResponseAlg(signingAlgo);

        Response response = dcrmServiceStub.registerApplication(dcrClientInfo);
        if (response == null) {
            throw new IdPClientException("Error occurred while DCR application '" + dcrClientInfo + "' creation. " +
                    "Response is null.");
        }
        if (response.status() == 201) {  //201 - Created
            try {
                this.oAuthApplicationInfo = getOAuthApplicationInfo(response);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("OAuth2 application created: " + oAuthApplicationInfo.toString());
                }
            } catch (IOException e) {
                throw new IdPClientException("Error occurred while parsing the DCR application creation response " +
                        "message.", e);
            }
        } else if (response.status() == 400) {  //400 - Known Error
            try {
                DCRError error = (DCRError) new GsonDecoder().decode(response, DCRError.class);
                if (!error.getErrorDescription().contains("admin_" + ExternalIdPClientConstants.SP_APPLICATION_NAME
                        + " already registered")) {

                    throw new IdPClientException("Error occurred while DCR application creation. Error: " +
                            error.getError() + ". Error Description: " + error.getErrorDescription() + ". Status Code: " +
                            response.status());

                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Service Provider 'admin_" + ExternalIdPClientConstants.SP_APPLICATION_NAME
                            + "' already registered.");
                }
            } catch (IOException e) {
                throw new IdPClientException("Error occurred while parsing the DCR error message.", e);
            }
        } else {  //Unknown Error
            throw new IdPClientException("Error occurred while DCR application creation. Error: " +
                    response.body().toString() + " Status Code: " + response.status());
        }

    }

    private OAuthApplicationInfo getOAuthApplicationInfo(Response response) throws IOException {
        OAuthApplicationInfo oAuthApplicationInfoResponse = new OAuthApplicationInfo();
        DCRClientInfo dcrClientInfoResponse = (DCRClientInfo) new GsonDecoder().decode(response, DCRClientInfo.class);
        oAuthApplicationInfoResponse.setClientName(dcrClientInfoResponse.getClientName());
        oAuthApplicationInfoResponse.setClientId(dcrClientInfoResponse.getClientId());
        oAuthApplicationInfoResponse.setClientSecret(dcrClientInfoResponse.getClientSecret());
        oAuthApplicationInfoResponse.setGrantTypes(dcrClientInfoResponse.getGrantTypes());
        oAuthApplicationInfoResponse.setCallBackURL(dcrClientInfoResponse.getRedirectURIs().get(0));
        return oAuthApplicationInfoResponse;
    }

    private String getErrorMessage(Response response) {
        StringBuilder errorMessage = new StringBuilder(ExternalIdPClientConstants.EMPTY_STRING);
        if (response != null && response.body() != null) {
            try {
                String errorDescription = new Gson().fromJson(response.body().toString(), JsonElement.class)
                        .getAsJsonObject().get("Errors").getAsJsonArray().get(0).getAsJsonObject().get("description")
                        .getAsString();
                errorMessage.append(errorDescription);
            } catch (Exception ex) {
                LOG.error("Error occurred while parsing error response", ex);
            }
        }
        return errorMessage.toString();
    }
}
