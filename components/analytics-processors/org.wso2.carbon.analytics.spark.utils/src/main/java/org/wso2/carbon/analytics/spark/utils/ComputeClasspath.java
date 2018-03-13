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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * this class creates the spark classpath by looking at the plugins folder
 */
public class ComputeClasspath {
    private static final Log log = LogFactory.getLog(ComputeClasspath.class);
    private static final String SEP = System.getProperty("os.name").toLowerCase().contains("win") ? ";" : ":";
    private static final String CONF_DIRECTORY_PATH = "carbon.config.dir.path";

    private static Set<String> additionalJars = new HashSet<>();

    public static void addAdditionalJarToClasspath(String jarName) {
        additionalJars.add(jarName);
    }

    private static Set<String> getAdditionalJars() {
        return additionalJars;
    }

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
        if (!isDirectory(carbonHome)) {
            throw new IOException(
                    "CarbonHome specified, does not exists :" + carbonHome);
        }
        return getSparkClasspath(sparkClasspath, carbonHome, new String[0]);
    }

    public static String getSparkClasspath(String sparkClasspath, String carbonHome,
                                           String[] excludeJars)
            throws IOException {
        if (!isDirectory(carbonHome)) {
            throw new IOException(
                    "CarbonHome specified, does not exists :" + carbonHome);
        }

        Set<String> requiredJars = getCarbonJars(carbonHome);
        String cp = createInitialSparkClasspath(sparkClasspath, carbonHome, requiredJars, SEP, excludeJars);
        return cp + addJarsFromDropins("", carbonHome, SEP) + addJarsFromLib("", carbonHome, SEP) +
               addJarsFromEndorsedLib("", carbonHome, SEP) + addJarsFromConfig("", carbonHome, SEP);
    }

    private static Set<String> getCarbonJars(String carbonHome) {
        Set<String> result = new HashSet<>();

        // Add the default list of jars
        Collections.addAll(result, populateDefaultJarsList());

        // Add additional jars
        result.addAll(getAdditionalJars());

        // Read from the file
        String confDirPath = System.getProperty(CONF_DIRECTORY_PATH);
        File jarsFile;
        if (confDirPath != null && !confDirPath.isEmpty()) {
            jarsFile = new File(confDirPath + File.separator + "analytics" + File.separator + "spark" + File.separator
                    + "carbon-spark-classpath.conf");
        } else {
            jarsFile = new File(
                    carbonHome + File.separator + "repository" + File.separator + "conf" + File.separator + "analytics"
                            + File.separator + "spark" + File.separator + "carbon-spark-classpath.conf");
        }

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(jarsFile), StandardCharsets.UTF_8));
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
                result.add(line);
            }
        } catch (IOException e) {
            log.warn("carbon-spark-classpath.conf file not found! Using the factory list of Carbon jars");
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                log.error("Unable to close the Buffered Reader for carbon-spark-classpath.conf");
            }
        }

        return result;
    }


    private static String addJarsFromLib(String scp, String carbonHome, String separator) {
        File libDir = new File(carbonHome + File.separator + "repository" + File.separator
                               + "components" + File.separator + "lib");
        File[] libJars = listJars(libDir);
        return appendFilesToString(scp, libJars, separator);
    }

    private static String appendFilesToString(String initialString, File[] files, String separator) {
        StringBuilder buf = new StringBuilder();
        buf.append(initialString);
        for (File file : files) {
            buf.append(separator);
            buf.append(file.getAbsolutePath());
        }
        return buf.toString();
    }

    private static String addJarsFromDropins(String scp, String carbonHome, String separator) {
        File libDir = new File(carbonHome + File.separator + "repository" + File.separator
                               + "components" + File.separator + "dropins");
        File[] libJars = listJars(libDir);
        return appendFilesToString(scp, libJars, separator);
    }

    private static String addJarsFromEndorsedLib(String scp, String carbonHome, String separator) {
        File libDir = new File(carbonHome + File.separator + "lib" + File.separator + "endorsed");
        File[] libJars = listJars(libDir);
        return appendFilesToString(scp, libJars, separator);
    }

    private static String addJarsFromConfig(String scp, String carbonHome, String separator)
            throws IOException {

        String confDirPath = System.getProperty(CONF_DIRECTORY_PATH);
        File cpFile;
        if (confDirPath != null && !confDirPath.isEmpty()) {
            cpFile = new File(confDirPath + File.separator + "analytics" + File.separator + "spark" + File.separator
                    + "external-spark-classpath.conf");
        } else {
            cpFile = new File(
                    carbonHome + File.separator + "repository" + File.separator + "conf" + File.separator + "analytics"
                            + File.separator + "spark" + File.separator + "external-spark-classpath.conf");
        }

        BufferedReader reader = null;
        StringBuilder buf = new StringBuilder();
        buf.append(scp);
        try {
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(cpFile), StandardCharsets.UTF_8));
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
                    buf.append(separator);
                    buf.append(line);
                } else if (fileExists(carbonHome + File.separator + line)) {
                    buf.append(separator);
                    buf.append(carbonHome);
                    buf.append(File.separator);
                    buf.append(line);
                } else {
                    throw new IOException("File not found : " + line);
                }
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.error("Unable to close the Buffered Reader for external-spark-classpath.conf");
                }
            }
        }

        return buf.toString();
    }

    private static boolean fileExists(String path) {
        File tempFile = new File(path);
        return tempFile.exists() && !tempFile.isDirectory() && tempFile.isAbsolute();
    }

    private static boolean isDirectory(String path) {
        File tempFile = new File(path);
        return tempFile.exists() && tempFile.isDirectory();
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
                                                      Set<String> requiredJars, String separator,
                                                      String[] excludeJars) {
        File pluginsDir = new File(carbonHome + File.separator + "repository" + File.separator + "components" +
                                   File.separator + "plugins");
        File[] pluginJars = listJars(pluginsDir);
        StringBuilder buf = new StringBuilder();

        if (!sparkClasspath.isEmpty()) {
            buf.append(sparkClasspath);
            buf.append(separator);
        }

        int i = 0;
        for (String requiredJar : requiredJars) {
            ArrayList<String> matchingJars = new ArrayList<>();
            for (File pluginJar : pluginJars) {
                String plugin = pluginJar.getName();
                String jarName = plugin.split("_")[0];
                if (containsInArray(excludeJars, jarName)) {
                    continue;
                }
                if (jarName.equals(requiredJar)) {
                    matchingJars.add(pluginJar.getAbsolutePath());
                }
            }

            if (matchingJars.size() > 0) {
                Collections.sort(matchingJars);
                String topJar = matchingJars.get(matchingJars.size() - 1);
                buf.append(topJar);
                if (i < requiredJars.size() - 1) {
                    buf.append(separator);
                }
                matchingJars.clear();
            }
            i++;
        }
        return buf.toString();
    }

    private static boolean containsInArray(String[] arr, String str) {
        for (String s : arr) {
            if (s.equals(str)) {
                return true;
            }
        }
        return false;
    }

    private static String[] populateDefaultJarsList() {
        return new String[]{
                "apache-zookeeper",
                "axiom",
                "axis2",
                "axis2-json",
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
                "com.ning.compress-lzf",
                "com.sun.jersey.jersey-core",
                "com.sun.jersey.jersey-server",
                "commons-cli",
                "commons-codec",
                "commons-collections",
                "commons-configuration",
                "commons-httpclient",
                "commons-io",
                "commons-lang",
                "config",
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
                "json-simple",
                "json4s-jackson",
                "kryo",
                "libthrift",
                "lucene",
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
                "org.spark.project.akka.actor",
                "org.spark.project.akka.remote",
                "org.spark.project.akka.slf4j",
                "org.wso2.carbon.analytics.api",
                "org.wso2.carbon.analytics.dataservice.commons",
                "org.wso2.carbon.analytics.dataservice.core",
                "org.wso2.carbon.analytics.datasource.cassandra",
                "org.wso2.carbon.analytics.datasource.commons",
                "org.wso2.carbon.analytics.datasource.core",
                "org.wso2.carbon.analytics.datasource.hbase",
                "org.wso2.carbon.analytics.datasource.rdbms",
                "org.wso2.carbon.analytics.eventsink",
                "org.wso2.carbon.analytics.eventtable",
                "org.wso2.carbon.analytics.io.commons",
                "org.wso2.carbon.analytics.spark.core",
                "org.wso2.carbon.analytics.spark.event",
                "org.wso2.carbon.analytics.stream.persistence",
                "org.wso2.carbon.base",
                "org.wso2.carbon.cluster.mgt.core",
                "org.wso2.carbon.identity.user.store.configuration",
                "org.wso2.carbon.identity.user.store.configuration.deployer",
                "org.wso2.carbon.user.api",
                "org.wso2.carbon.user.core",
                "org.wso2.carbon.user.mgt",
                "org.wso2.carbon.user.mgt.common",
                "org.wso2.carbon.core",
                "org.wso2.carbon.core.common",
                "org.wso2.carbon.core.services",
                "org.wso2.carbon.databridge.agent",
                "org.wso2.carbon.databridge.commons",
                "org.wso2.carbon.databridge.commons.binary",
                "org.wso2.carbon.databridge.commons.thrift",
                "org.wso2.carbon.databridge.core",
                "org.wso2.carbon.databridge.receiver.binary",
                "org.wso2.carbon.databridge.receiver.thrift",
                "org.wso2.carbon.databridge.streamdefn.filesystem",
                "org.wso2.carbon.datasource.reader.cassandra",
                "org.wso2.carbon.datasource.reader.hadoop",
                "org.wso2.carbon.deployment.synchronizer",
                "org.wso2.carbon.deployment.synchronizer.subversion",
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
                "org.wso2.carbon.event.input.adapter.websocket",
                "org.wso2.carbon.event.input.adapter.websocket.local",
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
                "org.wso2.carbon.event.output.adapter.websocket",
                "org.wso2.carbon.event.output.adapter.websocket.local",
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
                "org.wso2.carbon.logging",
                "org.wso2.carbon.ndatasource.common",
                "org.wso2.carbon.ndatasource.core",
                "org.wso2.carbon.ndatasource.datasources",
                "org.wso2.carbon.ndatasource.rdbms",
                "org.wso2.carbon.ntask.common",
                "org.wso2.carbon.ntask.core",
                "org.wso2.carbon.ntask.solutions",
                "org.wso2.carbon.registry.api",
                "org.wso2.carbon.registry.common",
                "org.wso2.carbon.registry.core",
                "org.wso2.carbon.registry.properties",
                "org.wso2.carbon.registry.resource",
                "org.wso2.carbon.registry.search",
                "org.wso2.carbon.registry.server",
                "org.wso2.carbon.utils",
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
                "commons-pool",
                "disruptor",
                "org.eclipse.osgi",
                "bigqueue"
        };
    }
}
