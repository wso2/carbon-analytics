/*
 *  Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package org.wso2.carbon.si.management.icp.impl;

import com.google.gson.JsonObject;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.wso2.carbon.si.management.icp.utils.Constants;
import org.wso2.carbon.si.management.icp.utils.HttpUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ICPHeartbeatComponent {

    private static final Log log = LogFactory.getLog(ICPHeartbeatComponent.class);
    private final String dashboardURL;
    private final int heartbeatInterval;
    private final String groupId;
    private final String nodeId;

    public ICPHeartbeatComponent(String dashboardURL, int heartbeatInterval, String groupId, String nodeId) {
        this.dashboardURL = dashboardURL;
        this.heartbeatInterval = heartbeatInterval;
        this.groupId = groupId;
        this.nodeId = nodeId;
    }

    public void invokeHeartbeatExecutorService() {
        String heartbeatApiUrl = dashboardURL + Constants.SLASH + Constants.HEARTBEAT;
        String mgtApiUrl = DataHolder.getInstance().getSiddhiHost() + Constants.MANAGEMENT + Constants.SLASH;
        final HttpPost httpPost = new HttpPost(heartbeatApiUrl);

        JsonObject heartbeatPayload = new JsonObject();
        heartbeatPayload.addProperty(Constants.PRODUCT, Constants.PRODUCT_SI);
        heartbeatPayload.addProperty(Constants.GROUP_ID, groupId);
        heartbeatPayload.addProperty(Constants.NODE_ID, nodeId);
        heartbeatPayload.addProperty(Constants.INTERVAL, heartbeatInterval);
        heartbeatPayload.addProperty(Constants.MGT_API_URL, mgtApiUrl);

        httpPost.setHeader(Constants.ACCEPT, Constants.HEADER_VALUE_APPLICATION_JSON);
        httpPost.setHeader(Constants.CONTENT_TYPE, Constants.HEADER_VALUE_APPLICATION_JSON);

        ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        Runnable runnableTask = () -> {
            try (CloseableHttpClient client = HttpClients.custom().setSSLSocketFactory(
                    new SSLConnectionSocketFactory(
                            SSLContexts.custom().loadTrustMaterial(null,
                                    (TrustStrategy) new TrustSelfSignedStrategy()).build(),
                            NoopHostnameVerifier.INSTANCE)).build()) {
                final StringEntity entity = new StringEntity(heartbeatPayload.toString());
                httpPost.setEntity(entity);
                CloseableHttpResponse response = client.execute(httpPost);
                JsonObject jsonResponse = HttpUtils.getJsonResponse(response);
                if (jsonResponse != null && jsonResponse.get("status").getAsString().equals("success")) {
                    log.debug("Heartbeat sent successfully.");
                } else {
                    log.error("Error occurred while sending the heartbeat.");
                }
            } catch (KeyStoreException | NoSuchAlgorithmException | KeyManagementException e) {
                log.error("SSL configuration error: {}", e);
            } catch (UnsupportedEncodingException e) {
                log.error("Error encoding heartbeat payload: {}", e);
            } catch (IOException e) {
                log.error("I/O error while sending heartbeat: {}", e);
            }
        };
        scheduledExecutorService.scheduleAtFixedRate(runnableTask, 1, heartbeatInterval, TimeUnit.SECONDS);
    }

}
