/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bam.utils.persistence;

import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.hector.api.Cluster;
import me.prettyprint.hector.api.factory.HFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.cassandra.dataaccess.ClusterConfiguration;
import org.wso2.carbon.cassandra.dataaccess.ClusterConfigurationFactory;
import org.wso2.carbon.cassandra.dataaccess.ClusterInformation;
import org.wso2.carbon.cassandra.dataaccess.DataAccessComponentException;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

// This class is used as a workaround for not being able to use data access service for Cassandra
// The code is directly copied from data access service classes
public class CassandraUtils {

    private static final Log log = LogFactory.getLog(CassandraUtils.class);

    private static final String USERNAME_KEY = "username";
    private static final String PASSWORD_KEY = "password";
    private static final String DEFAULT_HOST = "localhost:9160";
    private static final String LOCAL_HOST_NAME = "localhost";

    public static Cluster createCluster(ClusterInformation clusterInformation) {
        String username = clusterInformation.getUsername();
        String clusterName = clusterInformation.getClusterName();

        Map<String, String> credentials = new HashMap<String, String>();
        credentials.put(USERNAME_KEY, username);
        credentials.put(PASSWORD_KEY, clusterInformation.getPassword());

        CassandraHostConfigurator configurator = clusterInformation.getCassandraHostConfigurator();
        if (configurator == null) {
            configurator = createCassandraHostConfigurator();
        }

        return HFactory.createCluster(clusterName, configurator, credentials);
    }

    private static CassandraHostConfigurator createCassandraHostConfigurator() {

        ClusterConfiguration configuration = ClusterConfigurationFactory.create(loadConfigXML());

        String carbonCassandraRPCPort = null;
        carbonCassandraRPCPort = System.getProperty("cassandra.rpcport");
        String cassandraHosts = null;
        int cassandraDefaultPort = 0;

        if (carbonCassandraRPCPort != null) {
            cassandraHosts = LOCAL_HOST_NAME + ":" + carbonCassandraRPCPort;
            cassandraDefaultPort = Integer.parseInt(carbonCassandraRPCPort);
        } else {
            cassandraHosts = configuration.getNodesString();
        }
        if (cassandraHosts == null || "".equals(cassandraHosts)) {
            cassandraHosts = DEFAULT_HOST;
        }

        CassandraHostConfigurator configurator = new CassandraHostConfigurator(cassandraHosts);
        configurator.setAutoDiscoverHosts(configuration.isAutoDiscovery());
        configurator.setAutoDiscoveryDelayInSeconds(configuration.getAutoDiscoveryDelay());

        if (cassandraDefaultPort > 0 && cassandraDefaultPort < 65536) {
            configurator.setPort(cassandraDefaultPort);
        } else {
            configurator.setPort(configuration.getDefaultPort());
        }
        return configurator;
    }

    private static final String CASSANDRA_COMPONENT_CONF = "/repository/conf/advanced/cassandra-component.xml"; // TODO change


    private static OMElement loadConfigXML() {

        String carbonHome = System.getProperty(ServerConstants.CARBON_HOME);
        String path = carbonHome + CASSANDRA_COMPONENT_CONF;
        BufferedInputStream inputStream = null;
        try {
            File file = new File(path);
            if (!file.exists()) {
                log.info("There is no " + CASSANDRA_COMPONENT_CONF + ". Using the default configuration");
                inputStream = new BufferedInputStream(
                        new ByteArrayInputStream("<Cassandra/>".getBytes()));
            } else {
                inputStream = new BufferedInputStream(new FileInputStream(file));
            }
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            return builder.getDocumentElement();
        } catch (FileNotFoundException e) {
            throw new DataAccessComponentException(CASSANDRA_COMPONENT_CONF + "cannot be found in the path : " + path, e, log);
        } catch (XMLStreamException e) {
            throw new DataAccessComponentException("Invalid XML for " + CASSANDRA_COMPONENT_CONF + " located in " +
                                                   "the path : " + path, e, log);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException ingored) {
            }
        }
    }
}
