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

package org.wso2.carbon.sp.jobmanager.core.util;

import kafka.admin.AdminUtils;
import kafka.admin.RackAwareMode;
import kafka.common.TopicExistsException;
import kafka.server.KafkaConfig;
import kafka.server.KafkaServerStartable;
import kafka.utils.ZKStringSerializer$;
import kafka.utils.ZkUtils;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.ZkConnection;
import org.apache.commons.io.FileUtils;
import org.apache.curator.test.TestingServer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;

public class KafkaTestUtil {
    private static final Logger log = Logger.getLogger(KafkaTestUtil.class);
    private static TestingServer zkTestServer;
    private static TestingServer zkTestServer2;
    private static KafkaServerStartable kafkaServer;
    private static KafkaServerStartable kafkaServer2;
    private static final String kafkaLogDir = "tmp_kafka_dir";
    private static final String kafkaLogDir2 = "tmp_kafka_dir2";
    public static final String ZK_SERVER_CON_STRING = "localhost:2181";
    public static final String ZK_SERVER2_CON_STRING = "localhost:2182";
    private static final long CLEANER_BUFFER_SIZE = 2 * 1024 * 1024L;



    public static void cleanLogDir() {
        try {
            File f = new File(kafkaLogDir);
            FileUtils.deleteDirectory(f);
        } catch (IOException e) {
            log.error("Failed to clean up: " + e);
        }
    }

    public static void cleanLogDir2() {
        try {
            File f = new File(kafkaLogDir2);
            FileUtils.deleteDirectory(f);
        } catch (IOException e) {
            log.error("Failed to clean up: " + e);
        }
    }

    //---- private methods --------
    public static void setupKafkaBroker() {
        try {
            // mock zookeeper
            zkTestServer = new TestingServer(2181);
            log.info("Started zookeeper server on 2181");
            // mock kafka
            Properties props = new Properties();
            props.put("broker.id", "0");
            props.put("host.name", "localhost");
            props.put("port", "9092");
            props.put("log.dir", kafkaLogDir);
            props.put("zookeeper.connect", zkTestServer.getConnectString());
            props.put("replica.socket.timeout.ms", "30000");
            props.put("delete.topic.enable", "true");
            props.put("log.cleaner.dedupe.buffer.size", CLEANER_BUFFER_SIZE);
            KafkaConfig config = new KafkaConfig(props);
            kafkaServer = new KafkaServerStartable(config);
            kafkaServer.startup();
            log.info("Started kafka broker on 9092");
        } catch (Exception e) {
            log.error("Error running local Kafka broker / Zookeeper", e);
        }
    }

    public static void setupKafkaBroker2() {
        try {
            // mock zookeeper
            zkTestServer2 = new TestingServer(2182);
            log.info("Started zookeeper server on 2182");
            // mock kafka
            Properties props = new Properties();
            props.put("broker.id", "1");
            props.put("host.name", "localhost");
            props.put("port", "9093");
            props.put("log.dir", kafkaLogDir2);
            props.put("zookeeper.connect", zkTestServer2.getConnectString());
            props.put("replica.socket.timeout.ms", "30000");
            props.put("delete.topic.enable", "true");
            props.put("log.cleaner.dedupe.buffer.size", CLEANER_BUFFER_SIZE);
            KafkaConfig config = new KafkaConfig(props);
            kafkaServer2 = new KafkaServerStartable(config);
            kafkaServer2.startup();
            log.info("Started kafka broker on 9093");
        } catch (Exception e) {
            log.error("Error running local Kafka broker 2", e);
        }
    }

    public static void stopKafkaBroker2() {
        try {
            if (kafkaServer2 != null) {
                kafkaServer2.shutdown();
                kafkaServer2.awaitShutdown();
            }
            Thread.sleep(5000);
            if (zkTestServer2 != null) {
                zkTestServer2.stop();
            }
            Thread.sleep(5000);
            cleanLogDir2();
            log.info("Second Kafka broker stopped.");
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error("Error shutting down 2nd Kafka broker / Zookeeper", e);
        }
    }

    public static void stopKafkaBroker() {
        try {
            if (kafkaServer != null) {
                kafkaServer.shutdown();
                kafkaServer.awaitShutdown();
            }
            Thread.sleep(3000);
            if (zkTestServer != null) {
                zkTestServer.stop();
            }
            Thread.sleep(3000);
            cleanLogDir();
            log.info("Kafka broker stopped.");
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        } catch (IOException e) {
            log.error("Error shutting down Kafka broker / Zookeeper", e);
        }
    }



    public static void createTopic(String topics[], int numOfPartitions) {
        createTopic(ZK_SERVER_CON_STRING, topics, numOfPartitions);
    }

    public static void createTopic(String connectionString, String topics[], int numOfPartitions) {
        ZkClient zkClient = new ZkClient(connectionString, 30000, 30000, ZKStringSerializer$.MODULE$);
        ZkConnection zkConnection = new ZkConnection(connectionString);
        ZkUtils zkUtils = new ZkUtils(zkClient, zkConnection, false);
        for (String topic : topics) {
            try {
                AdminUtils.createTopic(zkUtils, topic, numOfPartitions, 1, new Properties(),
                        RackAwareMode.Enforced$.MODULE$);
            } catch (TopicExistsException e) {
                log.warn("topic exists for: " + topic);
            }
        }
        zkClient.close();
    }

    public static void deleteTopic(String topics[]) {
       deleteTopic("localhost:2181", topics);
    }

    public static void deleteTopic(String connectionString,  String topics[]) {
        ZkClient zkClient = new ZkClient(connectionString, 30000, 30000, ZKStringSerializer$.MODULE$);
        ZkConnection zkConnection = new ZkConnection(connectionString);
        ZkUtils zkUtils = new ZkUtils(zkClient, zkConnection, false);
        for (String topic : topics) {
            AdminUtils.deleteTopic(zkUtils, topic);
        }
        zkClient.close();
    }

    public static void kafkaPublisher(String topics[], int numOfPartitions, int numberOfEventsPerTopic, boolean
            publishWithPartition, String bootstrapServers, boolean isXML) {
        kafkaPublisher(topics, numOfPartitions, numberOfEventsPerTopic, 1000, publishWithPartition,
                       bootstrapServers, isXML);
    }

    public static void kafkaPublisher(String topics[], int numOfPartitions, int numberOfEventsPerTopic, long sleep,
                                      boolean publishWithPartition, String bootstrapServers, boolean isXML) {
        Properties props = new Properties();
        if (null == bootstrapServers) {
            props.put("bootstrap.servers", "localhost:9092");
        } else {
            props.put("bootstrap.servers", bootstrapServers);
        }
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33554432);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        Producer<String, String> producer = new KafkaProducer<String, String>(props);
        for (String topic : topics) {
            for (int i = 0; i < numberOfEventsPerTopic; i++) {
                String msg;
                if (isXML) {
                    msg = "<events>"
                            + "<event>"
                            + "<symbol>" + topic + "</symbol>"
                            + "<price>12.5</price>"
                            + "<volume>" + i + "</volume>"
                            + "</event>"
                            + "</events>";
                } else {
                    msg = topic + ",12.5," + i;
                }

                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException e) {
                }
                if (numOfPartitions > 1 || publishWithPartition) {
                    log.info("producing: " + msg + " into partition: " + (i % numOfPartitions));
                    producer.send(new ProducerRecord<>(topic, (i % numOfPartitions), null, msg));
                } else {
                    log.info("producing: " + msg);
                    producer.send(new ProducerRecord<>(topic, null, null, msg));
                }
            }
        }
        producer.close();
    }

    public static void publish(String topic, List<String> messages, int numOfPartitions,
                               String bootstrapServers) {
        Properties props = new Properties();
        if (null == bootstrapServers) {
            props.put("bootstrap.servers", "localhost:9092");
        } else {
            props.put("bootstrap.servers", bootstrapServers);
        }
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("batch.size", 16384);
        props.put("linger.ms", 1);
        props.put("buffer.memory", 33559000);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        Producer<String, String> producer = new KafkaProducer<String, String>(props);
        int i = 0;
        for (String message : messages) {
            if (numOfPartitions > 1) {
                log.info("producing: " + message + " into partition: " + (i % numOfPartitions));
                producer.send(new ProducerRecord<>(topic, (i % numOfPartitions), null, message));
            } else {
                log.info("producing: " + message);
                producer.send(new ProducerRecord<>(topic, null, null, message));
            }
            i++;
        }
        producer.flush();
        producer.close();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
