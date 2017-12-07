/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.status.dashboard.core.api.utils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

/**
 * SSLSocketFactory implementation
 */
public final class AMSSLSocketFactory extends SSLSocketFactory {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(AMSSLSocketFactory.class);
    private static final Map<String, SSLSocketFactory> sslSocketFactories = new HashMap<>();
    private final SSLSocketFactory socketFactory;

    private AMSSLSocketFactory(String certAlias) throws RuntimeException {

        if (certAlias == null) {
            throw new RuntimeException("Certificate alias is either null or empty");
        }

        try {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, new TrustManager[]{new AMTrustManager()}, new SecureRandom());
            this.socketFactory = sc.getSocketFactory();
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            throw new RuntimeException("Error occurred while creating SSL Socket Factory", e);
        }
    }

    public static synchronized SSLSocketFactory getSSLSocketFactory(String certAlias)
            throws RuntimeException {
        if (!sslSocketFactories.containsKey(certAlias)) {
            sslSocketFactories.put(certAlias, new AMSSLSocketFactory(certAlias));
        }
        return sslSocketFactories.get(certAlias);
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return socketFactory.getDefaultCipherSuites();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return socketFactory.getSupportedCipherSuites();
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose)
            throws IOException {
        return socketFactory.createSocket(s, host, port, autoClose);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return socketFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return socketFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort)
            throws IOException {
        return socketFactory.createSocket(host, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort)
            throws IOException {
        return socketFactory.createSocket(address, port, localAddress, localPort);
    }

    private static class AMTrustManager implements X509TrustManager {

        static final X509Certificate[] X_509_CERTIFICATES = new X509Certificate[0];

        public void checkClientTrusted(X509Certificate[] xcs, String password) throws CertificateException {
            char[] passphrase = "wso2carbon".toCharArray(); //password
            KeyStore keystore = null;
            FileInputStream fileInputStream = null;
            try {
                keystore = KeyStore.getInstance("JKS");
                fileInputStream = new FileInputStream(System.getProperty("carbon.home") +
                        "/resources/security/" + "client-truststore.jks");
                keystore.load(fileInputStream, passphrase); //path
                TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                tmf.init(keystore);
            } catch (NoSuchAlgorithmException e) {
                logger.error("No such algorithm in while write output in test server connector ", e);
            } catch (FileNotFoundException e) {
                logger.error("Authentication files not found", e);
            } catch (KeyStoreException e) {
                logger.error("Keystore exception in while trying to sent test request ", e);
            } catch (IOException e) {
                logger.error("IOException when trying to send test request ", e);
            } finally {
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e) {
                        //ignore
                    }
                }
            }
        }

        public void checkServerTrusted(X509Certificate[] xcs, String string) throws CertificateException {
            //trust all
        }

        public X509Certificate[] getAcceptedIssuers() {
            return X_509_CERTIFICATES;
        }
    }
}
