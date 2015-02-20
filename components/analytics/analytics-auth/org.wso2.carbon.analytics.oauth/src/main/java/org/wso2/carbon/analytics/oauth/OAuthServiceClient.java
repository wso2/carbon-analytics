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


import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.wso2.carbon.identity.oauth2.stub.OAuth2TokenValidationServiceStub;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2ClientApplicationDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO;
import org.wso2.carbon.identity.oauth2.stub.dto.OAuth2TokenValidationRequestDTO_OAuth2AccessToken;
import org.wso2.carbon.utils.CarbonUtils;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.apache.amber.oauth2.client.OAuthClient;
import org.apache.amber.oauth2.client.URLConnectionClient;
import org.apache.amber.oauth2.client.request.OAuthClientRequest;
import org.apache.amber.oauth2.client.response.OAuthClientResponse;
import org.apache.amber.oauth2.common.exception.OAuthProblemException;
import org.apache.amber.oauth2.common.exception.OAuthSystemException;
import org.apache.amber.oauth2.common.message.types.GrantType;


import org.wso2.carbon.identity.application.common.model.xsd.InboundAuthenticationRequestConfig;
import org.wso2.carbon.identity.application.common.model.xsd.OutboundProvisioningConfig;
import org.wso2.carbon.identity.application.common.model.xsd.Property;
import org.wso2.carbon.identity.application.common.model.xsd.ServiceProvider;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceIdentityApplicationManagementException;
import org.wso2.carbon.identity.application.mgt.stub.IdentityApplicationManagementServiceStub;
import org.wso2.carbon.identity.oauth.OAuthAdminService;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceException;
import org.wso2.carbon.identity.oauth.stub.OAuthAdminServiceStub;
import org.wso2.carbon.identity.oauth.stub.dto.OAuthConsumerAppDTO;
import org.wso2.carbon.identity.oauth2.OAuth2Service;
import org.wso2.carbon.identity.oauth2.dto.OAuth2AccessTokenReqDTO;

public class OAuthServiceClient {

    static final String BEARER_TOKEN_TYPE = "bearer";

    public static OAuth2ClientApplicationDTO validateToken(String tokenString) throws Exception {

        ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        OAuth2TokenValidationServiceStub stub = new OAuth2TokenValidationServiceStub(configContext, "https://localhost:9444/services/OAuth2TokenValidationService");
        CarbonUtils.setBasicAccessSecurityHeaders("admin", "admin", true, stub._getServiceClient());
        org.wso2.carbon.identity.oauth2.stub.dto.OAuth2ClientApplicationDTO validationResponse;

        OAuth2TokenValidationRequestDTO oauthReq = new OAuth2TokenValidationRequestDTO();
        OAuth2TokenValidationRequestDTO_OAuth2AccessToken accessToken =
                new OAuth2TokenValidationRequestDTO_OAuth2AccessToken();
        accessToken.setTokenType(BEARER_TOKEN_TYPE);
        accessToken.setIdentifier(tokenString.substring(7));
        oauthReq.setAccessToken(accessToken);
        validationResponse = stub.findOAuthConsumerIfTokenIsValid(oauthReq);

        System.out.println("validationResponse.getAccessTokenValidationResponse().getValid() = " + validationResponse.getAccessTokenValidationResponse().getValid());

        System.out.println("validationResponse.getConsumerKey() = " + validationResponse.getConsumerKey());
        System.out.println("validationResponse.getAccessTokenValidationResponse().getAuthorizedUser() = " + validationResponse.getAccessTokenValidationResponse().getAuthorizedUser());
        System.out.println("validationResponse.getAccessTokenValidationResponse().getExpiryTime() = " + validationResponse.getAccessTokenValidationResponse().getExpiryTime());

        return validationResponse;
    }

    public static String createToken(String username, String password) {
        OAuthAdminServiceStub serviceClient;
        IdentityApplicationManagementServiceStub stub;
        String appName = String.valueOf(Math.ceil(Math.random() * 1000));

        try {
            serviceClient = new OAuthAdminServiceStub("https://localhost:9444/services/OAuthAdminService");
            CarbonUtils.setBasicAccessSecurityHeaders("admin", "admin", true, serviceClient._getServiceClient());

            stub = new IdentityApplicationManagementServiceStub
                    ("https://localhost:9444/services/IdentityApplicationManagementService");
            CarbonUtils.setBasicAccessSecurityHeaders("admin", "admin", true, stub._getServiceClient());

            OAuthConsumerAppDTO oAuthApplication;
            try {
                oAuthApplication = serviceClient.getOAuthApplicationDataByAppName(appName);
            } catch (RemoteException e) {
                OAuthConsumerAppDTO oAuthConsumerDTO = new OAuthConsumerAppDTO();
                oAuthConsumerDTO.setApplicationName(appName);
                oAuthConsumerDTO.setOAuthVersion("oauth-2.0");
                oAuthConsumerDTO.setGrantTypes("password");

                serviceClient.registerOAuthApplicationData(oAuthConsumerDTO);
                oAuthApplication = serviceClient.getOAuthApplicationDataByAppName(appName);
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
            OAuthClientRequest accessRequest = OAuthClientRequest.tokenLocation("https://localhost:9444/oauth2/token")
                    .setGrantType(GrantType.PASSWORD)
                    .setClientId(consumerKey)
                    .setClientSecret(consumerSecret)
                    .setUsername(username)
                    .setPassword(password)
                    .setScope(appName)
                    .buildBodyMessage();
            OAuthClient oAuthClient = new OAuthClient(new URLConnectionClient());

            OAuthClientResponse oAuthResponse = oAuthClient.accessToken(accessRequest);
            System.out.println("oAuthResponse.getParam(\"access_token\") = " + oAuthResponse.getParam("access_token"));
            return oAuthResponse.getParam("access_token");

        } catch (RemoteException | OAuthAdminServiceException | OAuthProblemException | OAuthSystemException e) {
            e.printStackTrace();
        }
        return null;
    }
}
