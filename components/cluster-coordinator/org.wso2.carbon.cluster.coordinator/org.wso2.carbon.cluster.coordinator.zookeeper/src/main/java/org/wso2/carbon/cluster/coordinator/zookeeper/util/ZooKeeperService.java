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

package org.wso2.carbon.cluster.coordinator.zookeeper.util;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.wso2.carbon.cluster.coordinator.commons.exception.ClusterCoordinationException;
import org.wso2.carbon.cluster.coordinator.zookeeper.ZookeeperCoordinationStrategy;

import java.io.IOException;
import java.util.List;

/**
 * The zookeeper service class.
 */
public class ZooKeeperService {

    private ZooKeeper zooKeeper;

    public ZooKeeperService(final String url,
            final ZookeeperCoordinationStrategy.ProcessNodeWatcher processNodeWatcher)
            throws IOException {
        zooKeeper = new ZooKeeper(url, 3000, processNodeWatcher);
    }

    /**
     * Stop the zookeepr service.
     */
    public void stop() {
        try {
            zooKeeper.close();
        } catch (InterruptedException e) {
            throw new ClusterCoordinationException(
                    "Error while stopping zookeeper leader election process", e);
        }
    }

    /**
     * Retrieve data of the a zookeeper node.
     *
     * @param path path of the zookeeper node
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public byte[] getData(String path) throws KeeperException, InterruptedException {
        return zooKeeper.getData(path, true, zooKeeper.exists(path, true));
    }

    /**
     * Create a new zookeeper node.
     *
     * @param node      path od the node
     * @param data      the data node keeps
     * @param watch     Should the nde be watched
     * @param ephimeral The type of the node
     * @return the created node path
     */
    public String createNode(final String node, byte[] data, final boolean watch,
            final boolean ephimeral) {
        String createdNodePath = null;
        try {

            final Stat nodeStat = zooKeeper.exists(node, watch);

            if (nodeStat == null) {
                createdNodePath = zooKeeper.create(node, data, Ids.OPEN_ACL_UNSAFE,
                        (ephimeral ? CreateMode.EPHEMERAL_SEQUENTIAL : CreateMode.PERSISTENT));
            } else {
                createdNodePath = node;
            }

        } catch (KeeperException | InterruptedException e) {
            throw new IllegalStateException(e);
        }

        return createdNodePath;
    }

    /**
     * Get the children under a specific path.
     *
     * @param node  the node path
     * @param watch Should the nodes be watched
     * @return a list of children nodes
     */
    public List<String> getChildren(final String node, final boolean watch) {

        List<String> childNodes = null;

        try {
            childNodes = zooKeeper.getChildren(node, watch);
        } catch (KeeperException | InterruptedException e) {
            throw new IllegalStateException(e);
        }

        return childNodes;
    }

}
