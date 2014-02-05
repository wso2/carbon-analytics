/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.carbon.dashboard.common.oauth;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shindig.common.crypto.BasicBlobCrypter;
import org.apache.shindig.common.crypto.BlobCrypter;
import org.apache.shindig.common.crypto.Crypto;
import org.apache.shindig.common.util.ResourceLoader;
import org.apache.shindig.gadgets.http.HttpFetcher;
import org.apache.shindig.gadgets.oauth.BasicOAuthStoreConsumerKeyAndSecret;
import org.apache.shindig.gadgets.oauth.OAuthFetcherConfig;
import org.apache.shindig.gadgets.oauth.OAuthRequest;
import org.apache.shindig.gadgets.oauth.OAuthStore;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.dashboard.common.DashboardConstants;
import org.wso2.carbon.dashboard.common.OAuthUtils;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;


public class GSOAuthModule extends AbstractModule {
    //private static final Logger logger = Logger.getLogger(OAuthModule.class.getName());
    private static Log logger = LogFactory.getLog(GSOAuthModule.class);

    //private static final String OAUTH_CONFIG = "config/oauth.json";
    private static final String OAUTH_SIGNING_KEY_FILE = "shindig.signing.key-file";
    private static final String OAUTH_SIGNING_KEY_NAME = "shindig.signing.key-name";
    private static final String OAUTH_CALLBACK_URL = "shindig.signing.global-callback-url";


    @Override
    protected void configure() {
        // Used for encrypting client-side OAuth state.
        bind(BlobCrypter.class).annotatedWith(Names.named(OAuthFetcherConfig.OAUTH_STATE_CRYPTER))
                .toProvider(OAuthCrypterProvider.class);

        // Used for persistent storage of OAuth access tokens.
        bind(OAuthStore.class).toProvider(OAuthStoreProvider.class);
        bind(OAuthRequest.class).toProvider(OAuthRequestProvider.class);
    }

    @Singleton
    public static class OAuthCrypterProvider implements Provider<BlobCrypter> {

        private final BlobCrypter crypter;

        @Inject
        public OAuthCrypterProvider(@Named("shindig.signing.state-key") String stateCrypterPath)
                throws IOException {
            if (StringUtils.isBlank(stateCrypterPath)) {
                logger.info("Using random key for OAuth client-side state encryption");
                crypter = new BasicBlobCrypter(Crypto.getRandomBytes(BasicBlobCrypter.MASTER_KEY_MIN_LEN));
            } else {
                logger.info("Using file " + stateCrypterPath + " for OAuth client-side state encryption");
                crypter = new BasicBlobCrypter(new File(stateCrypterPath));
            }
        }

        public BlobCrypter get() {
            return crypter;
        }
    }

    public static class OAuthRequestProvider implements Provider<OAuthRequest> {
        private final HttpFetcher fetcher;
        private final OAuthFetcherConfig config;

        @Inject
        public OAuthRequestProvider(HttpFetcher fetcher, OAuthFetcherConfig config) {
            this.fetcher = fetcher;
            this.config = config;
        }

        public OAuthRequest get() {
            return new OAuthRequest(config, fetcher);
        }
    }

    @Singleton
    public static class OAuthStoreProvider implements Provider<OAuthStore> {

        private final RegistryBasedOAuthStore store;
        private final HashMap<Integer, RegistryBasedOAuthStore> mtStore;

        @Inject
        public OAuthStoreProvider(
                @Named(OAUTH_SIGNING_KEY_FILE) String signingKeyFile,
                @Named(OAUTH_SIGNING_KEY_NAME) String signingKeyName,
                @Named(OAUTH_CALLBACK_URL) String defaultCallbackUrl) {
            //store = new RegistryBasedOAuthStore();
            mtStore = new HashMap<Integer, RegistryBasedOAuthStore>();
            store = createNewStore();

            loadDefaultKey(signingKeyFile, signingKeyName);
            store.setDefaultCallbackUrl(defaultCallbackUrl);
            loadConsumers();
        }

        private void loadDefaultKey(String signingKeyFile, String signingKeyName) {
            BasicOAuthStoreConsumerKeyAndSecret key = null;
            if (!StringUtils.isBlank(signingKeyFile)) {
                try {
                    logger.info("Loading OAuth signing key from " + signingKeyFile);
                    String privateKey = IOUtils.toString(ResourceLoader.open(signingKeyFile), "UTF-8");
                    privateKey = RegistryBasedOAuthStore.convertFromOpenSsl(privateKey);
                    key = new BasicOAuthStoreConsumerKeyAndSecret(null, privateKey, BasicOAuthStoreConsumerKeyAndSecret.KeyType.RSA_PRIVATE,
                            signingKeyName, null);
                } catch (Throwable t) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Couldn't load key file " + signingKeyFile, t);
                    }
                }
            }
            if (key != null) {
                store.setDefaultKey(key);
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("Couldn't load OAuth signing key.  To create a key, run:\n" +
                            "  openssl req -newkey rsa:1024 -days 365 -nodes -x509 -keyout testkey.pem \\\n" +
                            "     -out testkey.pem -subj '/CN=mytestkey'\n" +
                            "  openssl pkcs8 -in testkey.pem -out oauthkey.pem -topk8 -nocrypt -outform PEM\n" +
                            '\n' +
                            "Then edit gadgets.properties and add these lines:\n" +
                            OAUTH_SIGNING_KEY_FILE + "=<path-to-oauthkey.pem>\n" +
                            OAUTH_SIGNING_KEY_NAME + "=mykey\n");
                }
            }
        }

        private void loadConsumers() {
            try {
//                String oauthConfigString = ResourceLoader.getContent(OAUTH_CONFIG);
                Integer tId = PrivilegedCarbonContext.getCurrentContext().getTenantId();
                Registry reg = OAuthUtils.getRegistry(tId);
                store.initFromConfigString(reg, (Collection) reg.get(DashboardConstants.OAUTH_KEY_STORE));
            } catch (Throwable t) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Failed to initialize OAuth consumers", t);
                }
            }
        }

        public OAuthStore get() {
            //return store;
            return getStore();
        }

        public OAuthStore getStore() {
            Integer tId = null;
            try {
                tId = PrivilegedCarbonContext.getCurrentContext().getTenantId();
                return mtStore.get(tId);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                return null;
            }

        }

        public OAuthStore getStore(int tenantId) {
            OAuthStore store = mtStore.get(tenantId);
            if (store == null) {
                store = createNewStore();
            }
            return store;
        }

        public RegistryBasedOAuthStore createNewStore() {
            RegistryBasedOAuthStore newStore = new RegistryBasedOAuthStore();
            Integer tId = null;
            try {
                tId = PrivilegedCarbonContext.getCurrentContext().getTenantId();
                mtStore.put(tId, newStore);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            return newStore;
        }
    }
}