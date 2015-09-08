/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.analytics.spark.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * this class creates the spark classpath by looking at the plugins folder
 */
public class ComputeClasspath {
    private static final String[] REQUIRED_JARS = {
            "apache-zookeeper",
            "cassandra-thrift",
            "chill",
            "com.datastax.driver.core",
            "com.fasterxml.jackson.core.jackson-annotations",
            "com.fasterxml.jackson.core.jackson-core",
            "com.fasterxml.jackson.core.jackson-databind",
            "com.fasterxml.jackson.module.jackson.module.scala",
            "com.google.gson",
            "com.google.guava",
            "com.google.protobuf",
            "com.jayway.jsonpath.json-path",
            "commons-collections",
            "commons-configuration",
            "commons-httpclient",
            "commons-io",
            "commons-lang",
            "com.ning.compress-lzf",
            "com.sun.jersey.jersey-core",
            "com.sun.jersey.jersey-server",
            "h2-database-engine",
            "hadoop-client",
            "hazelcast",
            "hbase-client",
            "hector-core",
            "htrace-core",
            "htrace-core-apache",
            "httpclient",
            "httpcore",
            "io.dropwizard.metrics.core",
            "io.dropwizard.metrics.graphite",
            "io.dropwizard.metrics.json",
            "io.dropwizard.metrics.jvm",
            "javax.cache.wso2",
            "javax.servlet.jsp-api",
            "jaxb",
            "jdbc-pool",
            "jdom",
            "jettison",
            "json",
            "json4s-jackson",
            "json-simple",
            "kryo",
            "libthrift",
            "mesos",
            "minlog",
            "net.minidev.json-smart",
            "netty-all",
            "objenesis",
            "org.apache.commons.lang3",
            "org.apache.commons.math3",
            "org.jboss.netty",
            "org.roaringbitmap.RoaringBitmap",
            "org.scala-lang.scala-library",
            "org.scala-lang.scala-reflect",
            "org.spark-project.protobuf.java",
            "org.wso2.carbon.analytics.api",
            "org.wso2.carbon.analytics.dataservice",
            "org.wso2.carbon.analytics.dataservice.commons",
            "org.wso2.carbon.analytics.datasource.cassandra",
            "org.wso2.carbon.analytics.datasource.commons",
            "org.wso2.carbon.analytics.datasource.core",
            "org.wso2.carbon.analytics.datasource.hbase",
            "org.wso2.carbon.analytics.datasource.rdbms",
            "org.wso2.carbon.analytics.eventsink",
            "org.wso2.carbon.analytics.eventtable",
            "org.wso2.carbon.analytics.io.commons",
            "org.wso2.carbon.analytics.spark.core",
            "org.wso2.carbon.analytics.stream.persistence",
            "org.wso2.carbon.databridge.agent",
            "org.wso2.carbon.databridge.commons",
            "org.wso2.carbon.databridge.commons.binary",
            "org.wso2.carbon.databridge.commons.thrift",
            "org.wso2.carbon.databridge.core",
            "org.wso2.carbon.databridge.receiver.binary",
            "org.wso2.carbon.databridge.receiver.thrift",
            "org.wso2.carbon.databridge.streamdefn.filesystem",
            "org.wso2.carbon.datasource.reader.hadoop",
            "org.wso2.carbon.logging",
            "org.wso2.orbit.asm4.asm4-all",
            "org.xerial.snappy.snappy-java",
            "paranamer",
            "perf4j",
            "poi",
            "protobuf-java-fragment",
            "quartz",
            "slf4j",
            "solr",
            "spark-core",
            "spark-sql",
            "spark-streaming",
            "stream",
            "tomcat",
            "tomcat-catalina-ha",
            "tomcat-el-api",
            "tomcat-jsp-api",
            "tomcat-servlet-api",
            "uncommons-maths",
            "wss4j",
            "xmlbeans",
            "XmlSchema",
            "org.wso2.carbon.ndatasource.common",
            "org.wso2.carbon.ndatasource.core",
            "org.wso2.carbon.ndatasource.datasources",
            "org.wso2.carbon.ndatasource.rdbms",
            "org.wso2.carbon.ntask.core",
            "org.wso2.carbon.ntask.common",
            "org.wso2.carbon.ntask.solutions",
            "lucene",
            "org.wso2.carbon.registry.server",
            "org.wso2.carbon.registry.search",
            "org.wso2.carbon.registry.resource",
            "org.wso2.carbon.registry.properties",
            "org.wso2.carbon.registry.core",
            "org.wso2.carbon.registry.common",
            "org.wso2.carbon.registry.api",
            "axiom",
            "axis2",
            "axis2-json",
            "org.wso2.carbon.base",
            "org.wso2.carbon.cluster.mgt.core",
            "org.wso2.carbon.core.common",
            "org.wso2.carbon.core.services",
            "org.wso2.carbon.core",
            "org.wso2.carbon.databridge.agent",
            "org.wso2.carbon.databridge.commons.binary",
            "org.wso2.carbon.databridge.commons.thrift",
            "org.wso2.carbon.databridge.commons",
            "org.wso2.carbon.databridge.core",
            "org.wso2.carbon.databridge.receiver.binary",
            "org.wso2.carbon.databridge.receiver.thrift",
            "org.wso2.carbon.databridge.streamdefn.filesystem",
            "org.wso2.carbon.datasource.reader.hadoop",
            "org.wso2.carbon.deployment.synchronizer.subversion",
            "org.wso2.carbon.deployment.synchronizer",
            "org.wso2.carbon.email.verification",
            "org.wso2.carbon.event.admin",
            "org.wso2.carbon.event.application.deployer",
            "org.wso2.carbon.event.client",
            "org.wso2.carbon.event.common",
            "org.wso2.carbon.event.core",
            "org.wso2.carbon.event.flow",
            "org.wso2.carbon.event.input.adapter.core",
            "org.wso2.carbon.event.input.adapter.email",
            "org.wso2.carbon.event.input.adapter.filetail",
            "org.wso2.carbon.event.input.adapter.http",
            "org.wso2.carbon.event.input.adapter.jms",
            "org.wso2.carbon.event.input.adapter.kafka",
            "org.wso2.carbon.event.input.adapter.mqtt",
            "org.wso2.carbon.event.input.adapter.soap",
            "org.wso2.carbon.event.input.adapter.websocket.local",
            "org.wso2.carbon.event.input.adapter.websocket",
            "org.wso2.carbon.event.input.adapter.wso2event",
            "org.wso2.carbon.event.output.adapter.cassandra",
            "org.wso2.carbon.event.output.adapter.core",
            "org.wso2.carbon.event.output.adapter.email",
            "org.wso2.carbon.event.output.adapter.http",
            "org.wso2.carbon.event.output.adapter.jms",
            "org.wso2.carbon.event.output.adapter.kafka",
            "org.wso2.carbon.event.output.adapter.logger",
            "org.wso2.carbon.event.output.adapter.mqtt",
            "org.wso2.carbon.event.output.adapter.rdbms",
            "org.wso2.carbon.event.output.adapter.sms",
            "org.wso2.carbon.event.output.adapter.soap",
            "org.wso2.carbon.event.output.adapter.websocket.local",
            "org.wso2.carbon.event.output.adapter.websocket",
            "org.wso2.carbon.event.output.adapter.wso2event",
            "org.wso2.carbon.event.processor.common",
            "org.wso2.carbon.event.processor.core",
            "org.wso2.carbon.event.processor.manager.commons",
            "org.wso2.carbon.event.processor.manager.core",
            "org.wso2.carbon.event.publisher.core",
            "org.wso2.carbon.event.receiver.core",
            "org.wso2.carbon.event.simulator.core",
            "org.wso2.carbon.event.statistics",
            "org.wso2.carbon.event.stream.core",
            "org.wso2.carbon.event.tracer",
            "org.wso2.carbon.utils",
            "org.spark.project.akka.actor",
            "org.spark.project.akka.remote",
            "org.spark.project.akka.slf4j",
            "config"
    };

    public static void main(String[] args) throws Exception {
        if (args.length == 0) {
            throw new Exception("Arguments to the main method should not be empty");
        }

        String carbonHome = args[0];
        String sparkClasspath = "";

        System.out.println(getSparkClasspath(sparkClasspath, carbonHome));
    }

    public static String getSparkClasspath(String sparkClasspath, String carbonHome)
            throws IOException {
        String cp = createInitialSparkClasspath(sparkClasspath, carbonHome, REQUIRED_JARS);
        return cp + addJarsFromLib("", carbonHome) + addJarsFromConfig("", carbonHome);
    }

    public static String[] getSparkClasspathJarsArray(String sparkClasspath, String carbonHome)
            throws IOException {
        return getSparkClasspath(sparkClasspath, carbonHome).split(":");
    }

    public static String getSparkClasspathAbsolute(String sparkClasspath, String carbonHome)
            throws IOException {
        if (carbonHome.endsWith(File.separator)) {
            return getSparkClasspath(sparkClasspath, carbonHome).replace(carbonHome, "." + File.separator);
        } else {
            return getSparkClasspath(sparkClasspath, carbonHome).replace(carbonHome, ".");
        }
    }

    private static String addJarsFromLib(String scp, String carbonHome) {
        File libDir = new File(carbonHome + File.separator + "repository" + File.separator
                               + "components" + File.separator + "lib");
        File[] libJars = listJars(libDir);
        for (File jar : libJars) {
            scp = scp + ":" + jar.getAbsolutePath();
        }
        return scp;
    }

    private static String addJarsFromConfig(String scp, String carbonHome)
            throws IOException {
        File cpFile = new File(carbonHome + File.separator + "repository" + File.separator + "conf"
                               + File.separator + "spark" + File.separator + "add-to-spark-classpath.conf");

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(cpFile));
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    // skip if a comment or an empty line or does not start with "carbon."
                    continue;
                }

                if (line.endsWith(";")) {
                    line = line.substring(0, line.length());
                }

                if (fileExists(line)) {
                    scp = scp + ":" + line;
                } else if (fileExists(carbonHome + File.separator + line)) {
                    scp = scp + ":" + carbonHome + File.separator + line;
                } else {
                    throw new IOException("File not found : " + line);
                }
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
//                        throw e;
                }
            }
        }

        return scp;
    }

    private static boolean fileExists(String path) {
        File tempFile = new File(path);
        return tempFile.exists() && !tempFile.isDirectory();
    }

    private static File[] listJars(File dir) {
        return dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
    }

    private static String createInitialSparkClasspath(String sparkClasspath, String carbonHome,
                                                      String[] requiredJars) {
        File pluginsDir = new File(carbonHome + File.separator + "repository" + File.separator
                                   + "components" + File.separator + "plugins");
        File[] pluginJars = listJars(pluginsDir);

        for (String requiredJar : requiredJars) {
            for (File pluginJar : pluginJars) {
                String plugin = pluginJar.getName();
                if (plugin.split("_")[0].equals(requiredJar)) {
                    if (sparkClasspath.isEmpty()) {
                        sparkClasspath = pluginJar.getAbsolutePath();
                    } else {
                        sparkClasspath = sparkClasspath + ":" + pluginJar.getAbsolutePath();
                    }
                }
            }
        }
        return sparkClasspath;
    }
}
