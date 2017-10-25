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

import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.stream.processor.idp.client.core.api.IdPClient;
import org.wso2.carbon.stream.processor.idp.client.core.exception.IdPClientException;
import org.wso2.carbon.stream.processor.idp.client.core.spi.IdPClientInitializer;
import org.wso2.carbon.stream.processor.idp.client.core.utils.IdPClientConstants;
import org.wso2.carbon.stream.processor.idp.client.external.factories.DCRMServiceStubFactory;
import org.wso2.carbon.stream.processor.idp.client.external.factories.OAuth2ServiceStubFactory;
import org.wso2.carbon.stream.processor.idp.client.external.factories.SCIM2ServiceStubFactory;
import org.wso2.carbon.stream.processor.idp.client.external.impl.DCRMServiceStub;
import org.wso2.carbon.stream.processor.idp.client.external.impl.OAuth2ServiceStubs;
import org.wso2.carbon.stream.processor.idp.client.external.impl.SCIM2ServiceStub;

import java.util.HashMap;
import java.util.Map;

@Component(
        name = "org.wso2.carbon.stream.processor.idp.client.external.ExternalIdPClientInitializer",
        immediate = true
)
public class ExternalIdPClientInitializer implements IdPClientInitializer {
    private static final Logger LOG = LoggerFactory.getLogger(ExternalIdPClientInitializer.class);
    private static HashMap<String, String> defaultProperties;

    @Activate
    protected void activate(BundleContext bundleContext) {
        LOG.info("OAuth2 Initializer registered...");
        if (LOG.isDebugEnabled()) {
            LOG.info("OAuth2 Initializer registered...");
        }
        defaultProperties = new HashMap<>();
        defaultProperties.put(ExternalIdPClientConstants.BASE_URL,
                ExternalIdPClientConstants.DEFAULT_BASE_URL);
        defaultProperties.put(ExternalIdPClientConstants.GRANT_TYPE,
                IdPClientConstants.PASSWORD_GRANT_TYPE);
        defaultProperties.put(ExternalIdPClientConstants.KM_DCR_URL,
                ExternalIdPClientConstants.DEFAULT_KM_DCR_URL);
        defaultProperties.put(ExternalIdPClientConstants.KM_TOKEN_URL,
                ExternalIdPClientConstants.DEFAULT_KM_TOKEN_URL);
        defaultProperties.put(ExternalIdPClientConstants.KM_CERT_ALIAS,
                ExternalIdPClientConstants.DEFAULT_KM_CERT_ALIAS);
        defaultProperties.put(ExternalIdPClientConstants.KM_USERNAME,
                ExternalIdPClientConstants.DEFAULT_KM_USERNAME);
        defaultProperties.put(ExternalIdPClientConstants.KM_PASSWORD,
                ExternalIdPClientConstants.DEFAULT_KM_PASSWORD);
        defaultProperties.put(ExternalIdPClientConstants.TOKEN_VALIDITY_PERIOD,
                ExternalIdPClientConstants.DEFAULT_TOKEN_VALIDITY_PERIOD);
        defaultProperties.put(ExternalIdPClientConstants.IDP_BASE_URL,
                ExternalIdPClientConstants.DEFAULT_IDP_BASE_URL);
        defaultProperties.put(ExternalIdPClientConstants.IDP_CERT_ALIAS,
                ExternalIdPClientConstants.DEFAULT_IDP_CERT_ALIAS);
        defaultProperties.put(ExternalIdPClientConstants.IDP_USERNAME,
                ExternalIdPClientConstants.DEFAULT_IDP_USERNAME);
        defaultProperties.put(ExternalIdPClientConstants.IDP_PASSWORD,
                ExternalIdPClientConstants.DEFAULT_IDP_PASSWORD);
        defaultProperties.put(ExternalIdPClientConstants.OIDC_USER_INFO_ALGO,
                ExternalIdPClientConstants.DEFAULT_OIDC_USER_INFO_ALGO);
    }

    @Deactivate
    protected void deactivate(BundleContext bundleContext) {
    }

    @Override
    public String getType() {
        return ExternalIdPClientConstants.EXTERNAL_IDP_CLIENT_TYPE;
    }

    @Override
    public IdPClient initializeIdPClient(Map<String, String> properties) throws IdPClientException {
        String dcrEndpoint = (properties.get(ExternalIdPClientConstants.KM_DCR_URL) == null ) ?
                defaultProperties.get(ExternalIdPClientConstants.KM_DCR_URL) :
                properties.get(ExternalIdPClientConstants.KM_DCR_URL);
        String kmUsername = (properties.get(ExternalIdPClientConstants.KM_USERNAME) == null) ?
                defaultProperties.get(ExternalIdPClientConstants.KM_USERNAME) :
                properties.get(ExternalIdPClientConstants.KM_USERNAME);
        String kmPassword = (properties.get(ExternalIdPClientConstants.KM_PASSWORD)== null) ?
                defaultProperties.get(ExternalIdPClientConstants.KM_PASSWORD) :
                properties.get(ExternalIdPClientConstants.KM_PASSWORD);
        String kmCertAlias = (properties.get(ExternalIdPClientConstants.KM_CERT_ALIAS)== null) ?
                defaultProperties.get(ExternalIdPClientConstants.KM_CERT_ALIAS) :
                properties.get(ExternalIdPClientConstants.KM_CERT_ALIAS);
        String kmTokenUrl = (properties.get(ExternalIdPClientConstants.KM_TOKEN_URL)== null) ?
                defaultProperties.get(ExternalIdPClientConstants.KM_TOKEN_URL) :
                properties.get(ExternalIdPClientConstants.KM_TOKEN_URL);

        String idPBaseUrl = (properties.get(ExternalIdPClientConstants.IDP_BASE_URL)== null) ?
                defaultProperties.get(ExternalIdPClientConstants.IDP_BASE_URL) :
                properties.get(ExternalIdPClientConstants.IDP_BASE_URL);
        String idPCertAlias = (properties.get(ExternalIdPClientConstants.IDP_CERT_ALIAS)== null) ?
                defaultProperties.get(ExternalIdPClientConstants.IDP_CERT_ALIAS) :
                properties.get(ExternalIdPClientConstants.IDP_CERT_ALIAS);
        String idPUserName = (properties.get(ExternalIdPClientConstants.IDP_USERNAME)== null) ?
                defaultProperties.get(ExternalIdPClientConstants.IDP_USERNAME) :
                properties.get(ExternalIdPClientConstants.IDP_USERNAME);
        String idPPassword = (properties.get(ExternalIdPClientConstants.IDP_PASSWORD)== null) ?
                defaultProperties.get(ExternalIdPClientConstants.IDP_PASSWORD) :
                properties.get(ExternalIdPClientConstants.IDP_PASSWORD);

        String baseUrl = (properties.get(ExternalIdPClientConstants.BASE_URL)== null) ?
                defaultProperties.get(ExternalIdPClientConstants.BASE_URL) :
                properties.get(ExternalIdPClientConstants.BASE_URL);
        String grantType = (properties.get(ExternalIdPClientConstants.GRANT_TYPE)== null) ?
                defaultProperties.get(ExternalIdPClientConstants.GRANT_TYPE) :
                properties.get(ExternalIdPClientConstants.GRANT_TYPE);
        String signingAlgo = (properties.get(ExternalIdPClientConstants.OIDC_USER_INFO_ALGO)== null) ?
                defaultProperties.get(ExternalIdPClientConstants.OIDC_USER_INFO_ALGO) :
                properties.get(ExternalIdPClientConstants.OIDC_USER_INFO_ALGO);
        String validityPeriod = (properties.get(ExternalIdPClientConstants.TOKEN_VALIDITY_PERIOD)== null) ?
                defaultProperties.get(ExternalIdPClientConstants.TOKEN_VALIDITY_PERIOD) :
                properties.get(ExternalIdPClientConstants.TOKEN_VALIDITY_PERIOD);

        DCRMServiceStub dcrmServiceStub = DCRMServiceStubFactory
                .getDCRMServiceStub(dcrEndpoint, kmUsername, kmPassword, kmCertAlias);
        OAuth2ServiceStubs keyManagerServiceStubs = OAuth2ServiceStubFactory.getKeyManagerServiceStubs(
                kmTokenUrl + ExternalIdPClientConstants.TOKEN_POSTFIX,
                kmTokenUrl + ExternalIdPClientConstants.REVOKE_POSTFIX,
                kmTokenUrl + ExternalIdPClientConstants.INTROSPECT_POSTFIX,
                kmCertAlias, kmUsername, kmPassword);
        SCIM2ServiceStub scimServiceStub = SCIM2ServiceStubFactory
                .getSCIMServiceStub(idPBaseUrl, idPUserName, idPPassword, idPCertAlias);

        return new ExternalIdPClient(baseUrl, kmTokenUrl + ExternalIdPClientConstants.AUTHORIZE_POSTFIX,
                grantType, signingAlgo, Long.valueOf(validityPeriod), dcrmServiceStub, keyManagerServiceStubs,
                scimServiceStub);
    }

}
