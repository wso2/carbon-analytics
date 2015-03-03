package org.wso2.carbon.analytics.oauth;

/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.oauth.internal.ServiceHolder;
import org.wso2.carbon.identity.oauth.OAuthAdminService;
import org.wso2.carbon.identity.oauth.common.exception.InvalidOAuthClientException;
import org.wso2.carbon.identity.oauth.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.oauth2.OAuth2Service;
import org.wso2.carbon.identity.oauth2.OAuth2TokenValidationService;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenRespDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2ClientApplicationDTO;
import org.wso2.carbon.identity.oauth2.dto.OAuth2TokenValidationRequestDTO;

import java.rmi.RemoteException;

public class OAuthServiceClient {

    private static final Log logger = LogFactory.getLog(OAuthServiceClient.class);

    static final String BEARER_TOKEN_TYPE = "bearer";

    public OAuthServiceClient() {
    }

    public OAuth2ClientApplicationDTO validateToken(String tokenString) throws Exception {

        OAuth2TokenValidationService stub = new OAuth2TokenValidationService();
        org.wso2.carbon.identity.oauth2.dto.OAuth2ClientApplicationDTO validationResponse;

        OAuth2TokenValidationRequestDTO oauthReq = new OAuth2TokenValidationRequestDTO();
        OAuth2TokenValidationRequestDTO.OAuth2AccessToken accessToken = new OAuth2TokenValidationRequestDTO().new OAuth2AccessToken();
        accessToken.setTokenType(BEARER_TOKEN_TYPE);
        accessToken.setIdentifier(tokenString.substring(7));
        oauthReq.setAccessToken(accessToken);
        validationResponse = stub.findOAuthConsumerIfTokenIsValid(oauthReq);

        System.out.println("validationResponse.getAccessTokenValidationResponse().getValid() = " + validationResponse
                .getAccessTokenValidationResponse().isValid());

        System.out.println("validationResponse.getConsumerKey() = " + validationResponse.getConsumerKey());
        System.out.println("validationResponse.getAccessTokenValidationResponse().getAuthorizedUser() = " + validationResponse.getAccessTokenValidationResponse().getAuthorizedUser());
        System.out.println("validationResponse.getAccessTokenValidationResponse().getExpiryTime() = " + validationResponse.getAccessTokenValidationResponse().getExpiryTime());

        return validationResponse;
    }

    public String createToken(String username, String password) throws Exception {

        OAuth2Service ss = ServiceHolder.getoAuth2Service();
//        IdentityApplicationManagementService stub;
        String appName = "BAM-Dashboard";

        try {
            OAuthAdminService serviceClient = new OAuthAdminService();

            /*stub = new IdentityApplicationManagementServiceStub
                    ("https://localhost:9444/services/IdentityApplicationManagementService");
            CarbonUtils.setBasicAccessSecurityHeaders("admin", "admin", true, stub._getServiceClient());*/

            OAuthConsumerAppDTO oAuthApplication = null;
            try {
                OAuthConsumerAppDTO oAuthConsumerDTO = new OAuthConsumerAppDTO();
                oAuthConsumerDTO.setApplicationName(appName);
                oAuthConsumerDTO.setOAuthVersion("oauth-2.0");
                oAuthConsumerDTO.setGrantTypes("password");

                serviceClient.registerOAuthApplicationData(oAuthConsumerDTO);
                oAuthApplication = serviceClient.getOAuthApplicationDataByAppName(appName);
            } catch (InvalidOAuthClientException e) {

                OAuthConsumerAppDTO oAuthConsumerDTO = new OAuthConsumerAppDTO();
                oAuthConsumerDTO.setApplicationName(appName);
                oAuthConsumerDTO.setOAuthVersion("oauth-2.0");
                oAuthConsumerDTO.setGrantTypes("password");

                serviceClient.registerOAuthApplicationData(oAuthConsumerDTO);
                oAuthApplication = serviceClient.getOAuthApplicationDataByAppName(appName);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (oAuthApplication == null) {
                OAuthConsumerAppDTO oAuthConsumerDTO = new OAuthConsumerAppDTO();
                oAuthConsumerDTO.setApplicationName(appName);
                oAuthConsumerDTO.setOAuthVersion("oauth-2.0");
                oAuthConsumerDTO.setGrantTypes("password");

                serviceClient.registerOAuthApplicationData(oAuthConsumerDTO);
                oAuthApplication = serviceClient.getOAuthApplicationDataByAppName(appName);
            }

            String consumerKey = oAuthApplication.getOauthConsumerKey();
            String consumerSecret = oAuthApplication.getOauthConsumerSecret();

            /*ServiceProvider serviceProvider = null;
            try {
                try {
                    serviceProvider = stub.getApplication(appName);
                } catch (RemoteException e) {
                    serviceProvider = new ServiceProvider();
                    serviceProvider.setApplicationName(appName);
                    stub.createApplication(serviceProvider);
                    serviceProvider = stub.getApplication(appName);
                }

                if (serviceProvider == null) {
                    serviceProvider = new ServiceProvider();
                    serviceProvider.setApplicationName(appName);
                    stub.createApplication(serviceProvider);
                    serviceProvider = stub.getApplication(appName);

                    serviceProvider.setOutboundProvisioningConfig(new OutboundProvisioningConfig());
                    List<InboundAuthenticationRequestConfig> authRequestList = new ArrayList<>();

                    if (consumerKey != null) {
                        InboundAuthenticationRequestConfig opicAuthenticationRequest =
                                new InboundAuthenticationRequestConfig();
                        opicAuthenticationRequest.setInboundAuthKey(consumerKey);
                        opicAuthenticationRequest.setInboundAuthType("oauth2");
                        if (consumerSecret != null && !consumerSecret.isEmpty()) {
                            Property property = new Property();
                            property.setName("oauthConsumerSecret");
                            property.setValue(consumerSecret);
                            Property[] properties = {property};
                            opicAuthenticationRequest.setProperties(properties);
                        }
                        authRequestList.add(opicAuthenticationRequest);
                    }


                    if (authRequestList.size() > 0) {
                        serviceProvider.getInboundAuthenticationConfig()
                                .setInboundAuthenticationRequestConfigs(authRequestList.toArray(new InboundAuthenticationRequestConfig[authRequestList.size()]));
                    }

                    try {
                        stub.updateApplication(serviceProvider);
                    } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
                        throw new RuntimeException(e);
                    }

                }
            } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
                throw new RuntimeException(e);
            }*/



            /*if (consumerKey != null) {
                InboundAuthenticationRequestConfig opicAuthenticationRequest =
                        new InboundAuthenticationRequestConfig();
                opicAuthenticationRequest.setInboundAuthKey(consumerKey);
                opicAuthenticationRequest.setInboundAuthType("oauth2");
                if (consumerSecret != null && !consumerSecret.isEmpty()) {
                    Property property = new Property();
                    property.setName("oauthConsumerSecret");
                    property.setValue(consumerSecret);
                    Property[] properties = {property};
                    opicAuthenticationRequest.setProperties(properties);
                }
                authRequestList.add(opicAuthenticationRequest);
            }


            if (authRequestList.size() > 0) {
                serviceProvider.getInboundAuthenticationConfig()
                        .setInboundAuthenticationRequestConfigs(authRequestList.toArray(new InboundAuthenticationRequestConfig[authRequestList.size()]));
            }

            try {
                stub.updateApplication(serviceProvider);
            } catch (IdentityApplicationManagementServiceIdentityApplicationManagementException e) {
                throw new RuntimeException(e);
            }
*/
            OAuth2AccessTokenReqDTO oAuth2AccessTokenReqDTO = new OAuth2AccessTokenReqDTO();
            oAuth2AccessTokenReqDTO.setGrantType("password");
            oAuth2AccessTokenReqDTO.setClientId(consumerKey);
            oAuth2AccessTokenReqDTO.setClientSecret(consumerSecret);
            oAuth2AccessTokenReqDTO.setResourceOwnerUsername(username);
            oAuth2AccessTokenReqDTO.setResourceOwnerPassword(password);
            oAuth2AccessTokenReqDTO.setScope(new String[]{appName});
            OAuth2AccessTokenRespDTO oAuth2AccessTokenRespDTO = ss.issueAccessToken(oAuth2AccessTokenReqDTO);

                    /*OAuthClientRequest
            accessRequest = OAuthClientRequest.tokenLocation("https://localhost:9444/oauth2/token")
                    .setGrantType(GrantType.PASSWORD)
                    .setClientId(consumerKey)
                    .setClientSecret(consumerSecret)
                    .setUsername(username)
                    .setPassword(password)
                    .setScope(appName)
                    .buildBodyMessage();
            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

            OAuthClientResponse oAuthResponse = oAuthClient.accessToken(accessRequest);*/

            System.out.println("oAuth2AccessTokenRespDTO.getAccessToken() = " + oAuth2AccessTokenRespDTO.getAccessToken());
            return oAuth2AccessTokenRespDTO.getAccessToken();

        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
}
