/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.analytics.test.osgi.util;

import io.netty.handler.codec.http.HttpMethod;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;


/**
 * Util class for test cases.
 */
public class TestUtil {
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(TestUtil.class);

    public static HTTPResponseMessage sendHRequest(String body, URI baseURI, String path, String contentType,
                                                   String methodType, Boolean auth, String userName, String password) {
        try {
            HttpURLConnection urlConn = null;
            try {
                urlConn = TestUtil.generateRequest(baseURI, path, methodType, false);
            } catch (IOException e) {
                TestUtil.handleException("IOException occurred while running the HttpsSourceTestCaseForSSL", e);
            }
            if (auth) {
                TestUtil.setHeader(urlConn, "Authorization",
                        "Basic " + java.util.Base64.getEncoder().
                                encodeToString((userName + ":" + password).getBytes()));
            }
            if (contentType != null) {
                TestUtil.setHeader(urlConn, "Content-Type", contentType);
            }
            TestUtil.setHeader(urlConn, "HTTP_METHOD", methodType);
            if (methodType.equals(HttpMethod.POST.name()) || methodType.equals(HttpMethod.PUT.name())) {
                TestUtil.writeContent(urlConn, body);
            }
            assert urlConn != null;
            HTTPResponseMessage httpResponseMessage = new HTTPResponseMessage(urlConn.getResponseCode(),
                    urlConn.getContentType(), urlConn.getResponseMessage());
            urlConn.disconnect();
            return httpResponseMessage;
        } catch (IOException e) {
            TestUtil.handleException("IOException occurred while running the HttpsSourceTestCaseForSSL", e);
        }
        return new HTTPResponseMessage();
    }

    private static void writeContent(HttpURLConnection urlConn, String content) throws IOException {
        OutputStreamWriter out = new OutputStreamWriter(
                urlConn.getOutputStream());
        out.write(content);
        out.close();
    }

    private static HttpURLConnection generateRequest(URI baseURI, String path, String method, boolean keepAlive)
            throws IOException {
        URL url = baseURI.resolve(path).toURL();
        HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
        urlConn.setRequestMethod(method);
        if (method.equals(HttpMethod.POST.name()) || method.equals(HttpMethod.PUT.name())) {
            urlConn.setDoOutput(true);
        }
        if (keepAlive) {
            urlConn.setRequestProperty("Connection", "Keep-Alive");
        }
        return urlConn;
    }

    private static void setHeader(HttpURLConnection urlConnection, String key, String value) {
        urlConnection.setRequestProperty(key, value);
    }

    private static void handleException(String msg, Exception ex) {
        logger.error(msg, ex);
    }

}
