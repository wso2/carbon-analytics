/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.sp.distributed.resource.core.util;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.sp.distributed.resource.core.bean.HTTPInterfaceConfig;
import org.wso2.carbon.sp.distributed.resource.core.bean.HeartbeatResponse;
import org.wso2.carbon.sp.distributed.resource.core.bean.ManagerNodeConfig;
import org.wso2.carbon.sp.distributed.resource.core.exception.ResourceNodeException;
import org.wso2.carbon.sp.distributed.resource.core.internal.ServiceDataHolder;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.Response;

/**
 * This will be responsible for discovering the leader, joining the resource pool and keep sending the heartbeats to the
 * leader node. This is a {@link TimerTask}, which get scheduled according to the heartbeatInterval of current leader
 * node.
 */
public class HeartbeatSender extends TimerTask {
    private static final Logger LOG = LoggerFactory.getLogger(HeartbeatSender.class);
    /**
     * Heartbeat endpoint template.
     */
    private static final String HEARTBEAT_ENDPOINT = "http://%s:%s/resourceManager/heartbeat";
    /**
     * Timestamp of the last successful heartbeat.
     */
    private static long lastUpdatedTimestamp;
    /**
     * Timer to schedule heartbeat sending task.
     */
    private final Timer timer;
    /**
     * Instance of {@link Gson} to un/marshall request/response.
     */
    private final Gson gson;

    /**
     * Constructs a new {@link HeartbeatSender} instance.
     *
     * @param timer timer to schedule heartbeat sending task.
     */
    public HeartbeatSender(Timer timer) {
        this.timer = timer;
        this.gson = new Gson();
    }

    /**
     * Getter for last updated timestamp.
     *
     * @return lastUpdatedTimestamp
     */
    private static long getLastUpdatedTimestamp() {
        return lastUpdatedTimestamp;
    }

    /**
     * Method to update the lastUpdatedTimestamp to the {@code System.currentTimeMillis()}
     */
    private static void updateLastUpdatedTimestamp() {
        HeartbeatSender.lastUpdatedTimestamp = System.currentTimeMillis();
    }

    /**
     * Start the Heartbeat updating task.
     */
    @Override
    public void run() {
        boolean heartbeatSent = false;
        do {
            /* If the LeaderNodeConfig is available, Heartbeat should sent to that Leader Node.
             */
            if (ServiceDataHolder.getLeaderNodeConfig() != null) {
                heartbeatSent = sendHeartbeat(ServiceDataHolder.getLeaderNodeConfig().getHttpInterface());
            }
            /* At this point check whether the node was able to connect to the leader successfully. If it failed,
             * Then try to connect to the list of manager nodes available.
             */
            if (!heartbeatSent) {
                for (HTTPInterfaceConfig i : ServiceDataHolder.getResourceManagers()) {
                    heartbeatSent = sendHeartbeat(i);
                    if (heartbeatSent) {
                        break;
                    }
                }
            }
            /* If still couldn't connect to the leader or the other available nodes, the Log that and wait for
             * a given period of time and try to reconnect.
             */
            if (!heartbeatSent) {
                try {
                    LOG.info("Waiting for the resource pool leader.");
                    // TODO: 10/23/17 Make this Thread sleep configurable
                    Thread.sleep(5000);
                } catch (InterruptedException ignored) {
                }
            }
        } while (!heartbeatSent);
    }

    /**
     * Method to send the heartbeat to the leader node.
     *
     * @param config host:port configuration of the candidate leader node.
     * @return whether successfully connected to the leader node or not.
     */
    private boolean sendHeartbeat(HTTPInterfaceConfig config) {
        HeartbeatResponse hbRes;
        Response response = null;
        boolean connected = false;
        try {
            /* If this resource node was previously connected to a Leader, and if all the leaders went offline for some
             * reason, then keep retrying for (leader.getHeartbeatInterval() * leader.getHeartbeatMaxRetry()) time,
             * and un deploying all the Siddhi apps deployed in this node to avoid unnecessary processing.
             */
            if (ServiceDataHolder.getLeaderNodeConfig() != null) {
                ManagerNodeConfig leader = ServiceDataHolder.getLeaderNodeConfig();
                if ((System.currentTimeMillis() - getLastUpdatedTimestamp())
                        > (leader.getHeartbeatInterval() * leader.getHeartbeatMaxRetry())) {
                    LOG.warn(String.format("Couldn't connect to the leader node for %s*%s milliseconds. Hence, " +
                                    "cleaning up deployed Siddhi apps.",
                            leader.getHeartbeatInterval(), leader.getHeartbeatMaxRetry()));
                    ResourceUtils.cleanSiddhiAppsDirectory();
                    ServiceDataHolder.getCurrentNodeConfig().setState(ResourceConstants.STATE_NEW);
                }
            }
            // Send request to the heartbeat endpoint.
            response = HTTPClientUtil.doPostRequest(
                    String.format(HEARTBEAT_ENDPOINT, config.getHost(), config.getPort()),
                    ServiceDataHolder.getCurrentNodeConfig()
            );
            switch (response.code()) {
                case 200:
                    updateLastUpdatedTimestamp();
                    hbRes = gson.fromJson(response.body().string(), HeartbeatResponse.class);
                    ServiceDataHolder.setLeaderNodeConfig(hbRes.getLeader());
                    /* Response will also contain list of managers which are connected to managers cluster.
                     * This might contain managers which are not specified in Resource nodes "resourceManagers"
                     * We'll add those to the resourceManagers list as well, so that managers can later be added
                     * w/o needing to specify them in the resource node.
                     */
                    ServiceDataHolder.getResourceManagers().addAll(hbRes.getConnectedManagers());
                    if (ResourceConstants.STATE_NEW.equalsIgnoreCase(hbRes.getJoinedState())) {
                        if (!ResourceConstants.STATE_NEW.equalsIgnoreCase(ServiceDataHolder.getCurrentNodeConfig()
                                .getState())) {
                            // If the node joins the resource pool as a new node, then un-deploy any existing apps.
                            ResourceUtils.cleanSiddhiAppsDirectory();
                        }
                        ServiceDataHolder.getCurrentNodeConfig().setState(ResourceConstants.STATE_EXISTS);
                        LOG.info("Successfully connected to leader node " + hbRes.getLeader() + " as a new resource.");
                    } else if (ResourceConstants.STATE_EXISTS.equalsIgnoreCase(hbRes.getJoinedState())) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Heartbeat sent to leader node " + hbRes.getLeader());
                        }
                        ServiceDataHolder.getCurrentNodeConfig().setState(ResourceConstants.STATE_EXISTS);
                    } else if (ResourceConstants.STATE_REJECTED.equalsIgnoreCase(hbRes.getJoinedState())) {
                        throw new ResourceNodeException(String.format("Leader@{host:%s, port:%s} rejected resource %s" +
                                        " from joining the resource pool.", config.getHost(), config.getPort(),
                                ServiceDataHolder.getCurrentNodeConfig()));
                    } else {
                        throw new ResourceNodeException(String.format("Unknown resource node state(%s) returned from " +
                                        "the Leader@{host:%s, port:%s} while sending heartbeat.", hbRes.getJoinedState(),
                                config.getHost(), config.getPort()));
                    }
                    /* When to send the next heartbeat, will depend on the current leaders "heartbeatInterval".
                     * So that, we don't have to worry about different leaders having different heartbeat check
                     * intervals (in case).
                     */
                    timer.schedule(new HeartbeatSender(timer), hbRes.getLeader().getHeartbeatInterval());
                    connected = true;
                    break;
                case 301:
                    // 301 will redirect to the current leader. Therefore, try that before going into next iteration.
                    hbRes = gson.fromJson(response.body().string(), HeartbeatResponse.class);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Redirecting to the current leader node at:" + hbRes.getLeader());
                    }
                    connected = sendHeartbeat(hbRes.getLeader().getHttpInterface());
                    break;
                default:
                    // In case of a 4XX or 5XX, try the next available manager.
                    break;
            }
        } catch (IOException e) {
            LOG.warn("Error occurred while connecting to ManagerNode@:" + config);
        } finally {
            if (response != null) {
                response.close();
            }
        }
        return connected;
    }
}
