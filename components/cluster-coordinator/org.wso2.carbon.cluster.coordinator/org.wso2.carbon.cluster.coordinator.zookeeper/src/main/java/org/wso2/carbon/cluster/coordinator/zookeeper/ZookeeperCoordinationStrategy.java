/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.wso2.carbon.cluster.coordinator.zookeeper;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.ZKPaths;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.wso2.carbon.cluster.coordinator.commons.CoordinationStrategy;
import org.wso2.carbon.cluster.coordinator.commons.MemberEventListener;
import org.wso2.carbon.cluster.coordinator.commons.configs.CoordinationPropertyNames;
import org.wso2.carbon.cluster.coordinator.commons.configs.CoordinationStrategyConfiguration;
import org.wso2.carbon.cluster.coordinator.commons.exception.ClusterCoordinationException;
import org.wso2.carbon.cluster.coordinator.commons.node.NodeDetail;
import org.wso2.carbon.cluster.coordinator.zookeeper.util.ZooKeeperService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This class manages the overall process of zookeeper leader election.
 */
public class ZookeeperCoordinationStrategy implements CoordinationStrategy {

    private static final String LEADER_ELECTION_ROOT_NODE = "/election";
    private static final String PROCESS_NODE_PREFIX = "/p_";
    private final ZooKeeperService zooKeeperService;
    private List<MemberEventListener> listeners;
    private String zkURL;

    public ZookeeperCoordinationStrategy() throws IOException {
        this.zkURL = CoordinationStrategyConfiguration.getInstance().getZooKeeperConfigs()
                .get(CoordinationPropertyNames.ZOOKEEPER_CONNECTION_STRING);
        zooKeeperService = new ZooKeeperService(this.zkURL, new ProcessNodeWatcher());
        listeners = new ArrayList<>();
    }

    @Override public List<NodeDetail> getAllNodeDetails(String groupId)
            throws ClusterCoordinationException {
        final List<String> childNodePaths = zooKeeperService
                .getChildren(LEADER_ELECTION_ROOT_NODE + "/" + groupId, false);
        List<NodeDetail> nodeDetails = new ArrayList<>();

        for (String childPath : childNodePaths) {
            byte[] nodeDetailByteArray = null;
            Map<String, Object> propertyMap = null;
            try {
                nodeDetailByteArray = zooKeeperService
                        .getData(LEADER_ELECTION_ROOT_NODE + "/" + groupId + "/" + childPath);
                ByteArrayInputStream byteStream = new ByteArrayInputStream(nodeDetailByteArray);
                ObjectInputStream objStream = new ObjectInputStream(byteStream);
                Object object = objStream.readObject();
                propertyMap = (Map<String, Object>) object;
            } catch (KeeperException e) {
                throw new ClusterCoordinationException("Error while getting node details", e);
            } catch (InterruptedException e) {
                throw new ClusterCoordinationException("Error while getting node details", e);
            } catch (IOException e) {
                throw new ClusterCoordinationException("Error while getting node details", e);
            } catch (ClassNotFoundException e) {
                throw new ClusterCoordinationException("Error while getting node details", e);
            }
            nodeDetails.add(new NodeDetail(childPath, groupId, true, 0, false, propertyMap));
        }
        return nodeDetails;
    }

    @Override public NodeDetail getLeaderNode(String groupId) {
        final List<String> childNodePaths = zooKeeperService
                .getChildren(LEADER_ELECTION_ROOT_NODE + "/" + groupId, false);
        Collections.sort(childNodePaths);
        byte[] nodeDetailByteArray = null;
        Map<String, Object> propertyMap = null;
        try {
            nodeDetailByteArray = zooKeeperService.getData(
                    LEADER_ELECTION_ROOT_NODE + "/" + groupId + "/" + childNodePaths.get(0));
            ByteArrayInputStream byteStream = new ByteArrayInputStream(nodeDetailByteArray);
            ObjectInputStream objStream = new ObjectInputStream(byteStream);
            Object object = objStream.readObject();
            propertyMap = (Map<String, Object>) object;
        } catch (KeeperException e) {
            throw new ClusterCoordinationException("Error while getting leader node", e);
        } catch (InterruptedException e) {
            throw new ClusterCoordinationException("Error while getting leader node", e);
        } catch (IOException e) {
            throw new ClusterCoordinationException("Error while getting leader node", e);
        } catch (ClassNotFoundException e) {
            throw new ClusterCoordinationException("Error while getting leader node", e);
        }
        return new NodeDetail(childNodePaths.get(0), groupId, true, 0, false, propertyMap);
    }

    @Override public void registerEventListener(MemberEventListener memberEventListener) {
        listeners.add(memberEventListener);
    }

    @Override public void joinGroup(String groupId, Map<String, Object> propertiesMap) {
        final String rootNodePath = zooKeeperService
                .createNode(LEADER_ELECTION_ROOT_NODE, new byte[0], false, false);
        if (rootNodePath == null) {
            throw new IllegalStateException(
                    "Unable to create/access leader election root node with path: "
                            + LEADER_ELECTION_ROOT_NODE);
        }
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(byteOut);
            out.writeObject(propertiesMap);
            out.flush();
        } catch (IOException e) {
            throw new ClusterCoordinationException("Error while getting property map", e);
        }
        byte[] dataByteArray = byteOut.toByteArray();
        String processNodePath = zooKeeperService
                .createNode(rootNodePath + "/" + groupId + PROCESS_NODE_PREFIX, dataByteArray,
                        false, true);
        if (processNodePath == null) {
            throw new IllegalStateException(
                    "Unable to create/access process node with path: " + LEADER_ELECTION_ROOT_NODE);
        }
        CuratorFramework client = null;
        PathChildrenCache cache = null;
        client = CuratorFrameworkFactory
                .newClient(this.zkURL, new ExponentialBackoffRetry(1000, 3));
        client.start();
        cache = new PathChildrenCache(client, LEADER_ELECTION_ROOT_NODE + "/" + groupId, true);
        try {
            cache.start();
        } catch (Exception e) {
            throw new ClusterCoordinationException("Error while starting the path cache", e);
        }
        String watchedNode = attemptForLeaderPosition(groupId, processNodePath);
        addListener(cache, groupId, processNodePath, watchedNode,
                getLeaderNode(groupId).getNodeId());
    }

    /**
     * Stop the process.
     */
    public void stop() {
        zooKeeperService.stop();
    }

    /**
     * The callback lass for node events. This method will be called in the events of node addition, removal, and
     * coordinator changed.
     *
     * @param cache           The pathChilderenCache
     * @param groupId         Group Id of the group node keeps watch on
     * @param processNodePath Node path of the current node
     * @param watchedNode     Node path of the watched node
     * @param leaderNode      Leader node of the current group
     */
    private void addListener(PathChildrenCache cache, final String groupId,
            final String processNodePath, final String watchedNode, final String leaderNode) {
        final PathChildrenCacheListener listener = new PathChildrenCacheListener() {
            String groupID = groupId;
            String currentLeader = leaderNode;
            String watchNode = watchedNode;
            String processNode = processNodePath;

            public synchronized void childEvent(CuratorFramework client,
                    PathChildrenCacheEvent event) throws Exception {
                switch (event.getType()) {
                case CHILD_ADDED: {
                    currentLeader = getLeaderNode(groupID).getNodeId();
                    for (MemberEventListener listener : listeners) {
                        boolean isCoordinator = false;
                        if (currentLeader.equals(event.getData().getPath()
                                .substring(event.getData().getPath().lastIndexOf('/') + 1))) {
                            isCoordinator = true;
                        }
                        if (event.getData().getPath().toLowerCase(Locale.ENGLISH)
                                .contains(listener.getGroupId().toLowerCase(Locale.ENGLISH))) {
                            listener.memberAdded(new NodeDetail(
                                    ZKPaths.getNodeFromPath(event.getData().getPath()),
                                    listener.getGroupId(), isCoordinator, 0, false, null));
                        }
                    }
                    break;
                }
                case CHILD_REMOVED: {
                    for (MemberEventListener listener : listeners) {
                        boolean isCoordinator = false;

                        if (currentLeader.equals(event.getData().getPath()
                                .substring(event.getData().getPath().lastIndexOf('/') + 1))) {
                            isCoordinator = true;
                        }
                        if (groupID.equals(listener.getGroupId())) {
                            listener.memberRemoved(new NodeDetail(
                                    ZKPaths.getNodeFromPath(event.getData().getPath()),
                                    listener.getGroupId(), isCoordinator, 0, false, null));
                        }
                    }
                    final List<String> childNodePaths = zooKeeperService
                            .getChildren(LEADER_ELECTION_ROOT_NODE + "/" + groupID, false);
                    Collections.sort(childNodePaths);
                    if (event.getData().getPath().equalsIgnoreCase(watchNode)) {
                        watchNode = attemptForLeaderPosition(groupID, processNode);
                    }
                    if (!currentLeader.equals(childNodePaths.get(0))) {
                        for (MemberEventListener listener : listeners) {

                            if (event.getData().getPath().toLowerCase(Locale.ENGLISH)
                                    .contains(listener.getGroupId().toLowerCase(Locale.ENGLISH))) {
                                listener.coordinatorChanged(
                                        new NodeDetail(childNodePaths.get(0), listener.getGroupId(),
                                                true, 0, false, null));
                            }
                        }
                        currentLeader = childNodePaths.get(0);
                    }

                    break;
                }
                default:
                    break;
                }
            }
        };
        cache.getListenable().addListener(listener);
    }

    /**
     * The node tries to be the leader.
     *
     * @param groupID         The group ID of the node
     * @param processNodePath The path of the current node
     * @return The path of the node, which current node keeps the watch on
     */
    private String attemptForLeaderPosition(String groupID, String processNodePath) {

        final List<String> childNodePaths = zooKeeperService
                .getChildren(LEADER_ELECTION_ROOT_NODE + "/" + groupID, false);
        String watchedNodePath = "";
        Collections.sort(childNodePaths);

        int index = childNodePaths
                .indexOf(processNodePath.substring(processNodePath.lastIndexOf('/') + 1));
        if (index != 0) {
            final String watchedNodeShortPath = childNodePaths.get(index - 1);
            watchedNodePath =
                    LEADER_ELECTION_ROOT_NODE + "/" + groupID + "/" + watchedNodeShortPath;
        }
        return watchedNodePath;
    }

    /**
     * Watcher classes needed to create nodes.
     */
    public static class ProcessNodeWatcher implements Watcher {
        @Override public void process(WatchedEvent event) {

        }
    }
}
