package org.wso2.carbon.databridge.persistence.cassandra.internal.util;

import me.prettyprint.hector.api.ConsistencyLevelPolicy;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.commons.io.FileUtils;
import org.wso2.carbon.databridge.persistence.cassandra.internal.StreamDefnConsistencyLevelPolicy;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.InputStream;
import java.net.*;
import java.util.Enumeration;

/**
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class Utils {

    private static final String STREAMDEFN_XML = "streamdefn.xml";
    private static final String REPLICATION_FACTOR_ELEMENT = "ReplicationFactor";
    private static final String READ_CONSISTENCY_LEVEL_ELEMENT = "ReadConsistencyLevel";
    private static final String WRITE_CONSISTENCY_LEVEL_ELEMENT = "WriteConsistencyLevel";
    private static final String STRATEGY_CLASS_ELEMENT = "StrategyClass";
    private static final String NODE_ID_ELEMENT = "NodeId";
    private static final String KEY_SPACE_NAME_ELEMENT = "keySpaceName";
    private static final String INDEX_KEY_SPACE_NAME_ELEMENT = "eventIndexKeySpaceName";


    private static int replicationFactor;
    private static String readConsistencyLevel;
    private static String writeConsistencyLevel;
    private static String strategyClass;
    private static int nodeId;

    private static String keySpaceName;
    private static String indexKeySpaceName;

    private static ConsistencyLevelPolicy globalConsistencyLevelPolicy;

    public static InetAddress getLocalAddress() throws SocketException, UnknownHostException {
        Enumeration<NetworkInterface> ifaces = NetworkInterface.getNetworkInterfaces();
        while (ifaces.hasMoreElements()) {
            NetworkInterface iface = ifaces.nextElement();
            Enumeration<InetAddress> addresses = iface.getInetAddresses();

            while (addresses.hasMoreElements()) {
                InetAddress addr = addresses.nextElement();
                if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                    return addr;
                }
            }
        }
        return InetAddress.getLocalHost();
    }


    public static void readConfigFile() {


        InputStream in;
        try {
            String configFilePath = CarbonUtils.getCarbonConfigDirPath() + File.separator + "advanced" + File.separator + STREAMDEFN_XML;
            in = FileUtils.openInputStream(new File(configFilePath));
        } catch (Exception e) {
            in = Utils.class.getClassLoader().getResourceAsStream(STREAMDEFN_XML);
        }

        OMXMLParserWrapper builder = OMXMLBuilderFactory.createOMBuilder(in);

        OMElement documentElement = builder.getDocumentElement();

        OMElement replicationFactorEl = documentElement.getFirstChildWithName(new QName(REPLICATION_FACTOR_ELEMENT));
        if (replicationFactorEl != null) {
            replicationFactor = Integer.parseInt(replicationFactorEl.getText());
        }

        OMElement readLevelEl = documentElement.getFirstChildWithName(new QName(READ_CONSISTENCY_LEVEL_ELEMENT));
        if (replicationFactorEl != null) {
            readConsistencyLevel = readLevelEl.getText();
        }

        OMElement writeLevelEl = documentElement.getFirstChildWithName(new QName(WRITE_CONSISTENCY_LEVEL_ELEMENT));
        if (writeLevelEl != null) {
            writeConsistencyLevel = writeLevelEl.getText();
        }

        globalConsistencyLevelPolicy = new StreamDefnConsistencyLevelPolicy(readConsistencyLevel, writeConsistencyLevel);
        
        OMElement strategyEl = documentElement.getFirstChildWithName(new QName(STRATEGY_CLASS_ELEMENT));
        if (strategyEl != null) {
            strategyClass = strategyEl.getText();
        }

         OMElement nodeIdElement = documentElement.getFirstChildWithName(new QName(NODE_ID_ELEMENT));
        if (nodeIdElement != null){
            nodeId = Integer.parseInt(nodeIdElement.getText());
        }else {
            nodeId = DataReceiverConstants.DEFAULT_RECEIVER_NODE_ID;
        }

        OMElement keySpaceElement = documentElement.getFirstChildWithName(new QName(KEY_SPACE_NAME_ELEMENT));
        if (nodeIdElement != null){
            keySpaceName = keySpaceElement.getText();
        }else {
            keySpaceName = DataReceiverConstants.DEFAULT_KEY_SPACE_NAME;
        }

       OMElement indexKeySpaceElement = documentElement.getFirstChildWithName(new QName(INDEX_KEY_SPACE_NAME_ELEMENT));
        if (nodeIdElement != null){
            indexKeySpaceName = indexKeySpaceElement.getText();
        }else {
            indexKeySpaceName = DataReceiverConstants.DEFAULT_INDEX_KEYSPACE_NAME;
        }
    }


    public static ConsistencyLevelPolicy getGlobalConsistencyLevelPolicy() {
        return globalConsistencyLevelPolicy;
    }

    public static String getStrategyClass() {
        return strategyClass;
    }

    public static int getReplicationFactor() {
        return replicationFactor;
    }

    public static String getReadConsistencyLevel() {
        return readConsistencyLevel;
    }

    public static String getWriteConsistencyLevel() {
        return writeConsistencyLevel;
    }

    public static int getNodeId(){
        return nodeId;
    }

    public static String getKeySpaceName() {
        return keySpaceName;
    }

    public static String getIndexKeySpaceName() {
        return indexKeySpaceName;
    }
}
