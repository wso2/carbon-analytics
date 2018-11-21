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

package org.wso2.carbon.sp.jobmanager.core.appcreator;

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.utils.ZkUtils;

import java.util.Properties;

/**
 * Helper class to use Kafka.
 * <p>
 * Workaround to delay loading of {@link RackAwareMode.Enforced$} enums at server startup.
 */
class SafeKafkaInvoker {

    /**
     * Invokes {@link AdminUtils#createTopic(ZkUtils, String, int, int, Properties, RackAwareMode)}.
     *
     * @param bootstrapServerURLs bootstrap server URLs
     * @param topicConfig         topic configuration
     * @param topic               topic
     * @param partitions          partitions
     */
    public void createKafkaTopic(String[] bootstrapServerURLs, ZkUtils zkUtils, Properties topicConfig,
                                 String topic, Integer partitions) {
        AdminUtils.createTopic(zkUtils, topic, partitions, bootstrapServerURLs.length,
                topicConfig, RackAwareMode.Enforced$.MODULE$);
    }

    /**
     * Invokes {@link AdminUtils#addPartitions(ZkUtils, String, int, String, boolean, RackAwareMode)}.
     *
     * @param zkUtils    zkUtils
     * @param topic      topic
     * @param partitions partitions
     */
    public void addKafkaPartition(ZkUtils zkUtils, String topic, Integer partitions) {
        AdminUtils.addPartitions(zkUtils, topic, partitions, "", true,
                RackAwareMode.Enforced$.MODULE$);
    }
}
