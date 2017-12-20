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

package org.wso2.carbon.sp.jobmanager.core.internal;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobManagerBundleActivator implements BundleActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(JobManagerBundleActivator.class);

    private static final String CNF_ERROR_MESSAGE = "Cannot find dependency '{}'. " +
            "Please refer the Stream Processor documentation on configuring a distributed deployment.";

    @Override
    public void start(BundleContext bundleContext) {
        checkDependency("kafka.admin.AdminUtils", "Kafka");
        checkDependency("org.apache.kafka.clients.producer.KafkaProducer", "Kafka Clients");
        checkDependency("com.yammer.metrics.Metrics", "Metrics Core");
        checkDependency("scala.Unit", "Scala Library");
        checkDependency("scala.util.parsing.combinator.Parsers", "Scala Parser Combinators");
        checkDependency("org.apache.zookeeper.ZKUtil", "Zookeeper");
        checkDependency("org.I0Itec.zkclient.Holder", "zk Client");

    }

    private void checkDependency(String className, String dependencyJar) {
        try {
            this.getClass().getClassLoader().loadClass(className);
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            LOGGER.error(CNF_ERROR_MESSAGE, dependencyJar);
            LOGGER.debug(CNF_ERROR_MESSAGE, dependencyJar, e);
        }
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        // No op
    }
}
