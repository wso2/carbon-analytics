package org.wso2.carbon.event.processor.core.internal.util.helper;

/*
* Copyright 2004,2005 The Apache Software Foundation.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

import me.prettyprint.cassandra.service.CassandraHostConfigurator;
import me.prettyprint.cassandra.service.ThriftCluster;
import me.prettyprint.hector.api.Cluster;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.base.ServerConfiguration;
import org.wso2.carbon.cassandra.dataaccess.DataAccessComponentException;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CassandraConnectionValidator {

    private static CassandraConnectionValidator  cassandraConnectionValidator;
    private static String CARBON_CONFIG_PORT_OFFSET = "Ports.Offset";
    private static int CARBON_DEFAULT_PORT_OFFSET = 0;
    private static final int CASSANDRA_RPC_PORT = 9160;
    private static final String LOCAL_HOST_NAME = "localhost";
    private final List<String> nodes = new ArrayList<String>();
    private String nodesString;

    private static final String CASSANDRA_COMPONENT_CONF = File.separator + "repository" + File.separator + "conf"
                                                           + File.separator + "etc" + File.separator + "cassandra-component.xml";

    private static Log log = LogFactory.getLog(CassandraConnectionValidator.class);

    public CassandraConnectionValidator() {
        setClusterNodes(loadConfigXML());
    }

    public static CassandraConnectionValidator getInstance() {
        if (cassandraConnectionValidator == null) {
            cassandraConnectionValidator = new CassandraConnectionValidator();
        }

        return cassandraConnectionValidator;
    }

    public int readPortOffset() {
        ServerConfiguration carbonConfig = ServerConfiguration.getInstance();
        String portOffset = carbonConfig.getFirstProperty(CARBON_CONFIG_PORT_OFFSET);

        try {
            return ((portOffset != null) ? Integer.parseInt(portOffset.trim()) : CARBON_DEFAULT_PORT_OFFSET);
        } catch (NumberFormatException e) {
            return CARBON_DEFAULT_PORT_OFFSET;
        }
    }

    public boolean checkCassandraConnection(String userName, String password) {
        String cassandraHosts = nodesString;
        if (cassandraHosts == null || "".equals(cassandraHosts)) {
            String connectionPort = CASSANDRA_RPC_PORT + readPortOffset() + "";
            cassandraHosts = LOCAL_HOST_NAME + ":" + connectionPort;
        }

        Map<String, String> credentials = new HashMap<String, String>();
        credentials.put("username", userName);
        credentials.put("password", password);


        CassandraHostConfigurator hostConfigurator = new CassandraHostConfigurator(cassandraHosts);
        hostConfigurator.setRetryDownedHosts(false);
        // this.cluster = HFactory.getOrCreateCluster(clusterName, hostConfigurator, credentials);
        Cluster cluster = new ThriftCluster("test-cluster", hostConfigurator, credentials);
        Set knownPools = cluster.getKnownPoolHosts(true);
        if (knownPools != null && knownPools.size() > 0) {
            return true;
        }

        return false;
    }

    private OMElement loadConfigXML() {

        String carbonHome = CarbonUtils.getCarbonHome();
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

    private void setClusterNodes(OMElement severElement) {
        OMElement cluster = severElement.getFirstChildWithName(new QName("Cluster"));
        if (cluster != null) {
            OMElement nodesElement = cluster.getFirstChildWithName(new QName("Nodes"));
            if (nodesElement != null) {
                String nodesString = nodesElement.getText();
                if (nodesString != null && !"".endsWith(nodesString.trim())) {
                    nodesString = (nodesString.trim());
                    String nodes[] = nodesString.split(",");
                    Collections.addAll(this.nodes, nodes);
                }
            }
        }

    }

}
