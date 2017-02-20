/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.databridge.core.internal.utils;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.core.conf.DataBridgeConfiguration;
import org.wso2.carbon.databridge.core.exception.DataBridgeConfigurationException;
import org.wso2.carbon.kernel.utils.Utils;
import org.yaml.snakeyaml.Yaml;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Helper class to build Agent Server Initial Configurations
 */
public final class DataBridgeCoreBuilder {
    private static final Log log = LogFactory.getLog(DataBridgeCoreBuilder.class);

    private DataBridgeCoreBuilder() {
    }

    // TODO: 2/2/17 stream definitions are temporarily loaded from a file in <product-sp>/deployment
    public static List<String> loadStreamDefinitionXML() throws DataBridgeConfigurationException {
        List<String> streamDefinitionList = new ArrayList<String>();
//        String carbonHome = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
//        String path = carbonHome + File.separator + DataBridgeConstants.DATA_BRIDGE_DIR +
//                File.separator + DataBridgeConstants.STREAM_DEFINITIONS_XML;
        String path = Utils.getCarbonHome().toString()+ File.separator+ "deployment"+ File.separator+ "stream-definitions.yaml";
        Yaml yaml = new Yaml();
        File file = new File(path);
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            Map<String,List<String>> streams = (Map<String, List<String>>) yaml.load(fileInputStream);
            streamDefinitionList.addAll(streams.get(DataBridgeConstants.STREAM_DEFINITIONS_ELEMENT));
        } catch (FileNotFoundException e) {
            log.error("File "+path+" could not be found", e);
        }


        /*File file = new File(path);
        if (file.exists() && !file.isDirectory()) {
            OMElement config = loadXML(path, DataBridgeConstants.STREAM_DEFINITIONS_XML);
            if (config != null) {
                if (!(new QName(DataBridgeConstants.DATA_BRIDGE_NAMESPACE, DataBridgeConstants.STREAM_DEFINITIONS_ELEMENT)).equals(config.getQName())) {
                    throw new DataBridgeConfigurationException("Wrong configuration added in " + DataBridgeConstants.STREAM_DEFINITIONS_XML);
                }
                for (Iterator streamDefinitionIterator = config.getChildElements();
                     streamDefinitionIterator.hasNext(); ) {
                    OMElement streamDefinition = (OMElement) streamDefinitionIterator.next();
                    String domainName = streamDefinition.getAttributeValue(new QName(DataBridgeConstants.DOMAIN_NAME_ATTRIBUTE));

                    if (domainName == null || domainName.equals("")) {
                        domainName = MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;
                    }
                    streamDefinitionList.add(new String[]{domainName, streamDefinition.getText()});
                }
            }
        }*/

        return streamDefinitionList;
    }

    public static String getDatabridgeConfigPath() {
        // TODO: 2/14/17 data-bridge-config.yaml loaded from <product-sp>/resources
//        String carbonHome = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
//        return carbonHome + File.separator + DataBridgeConstants.DATA_BRIDGE_DIR + File.separator + DataBridgeConstants.DATA_BRIDGE_CONFIG_XML;
        File filePath = new File("src" + File.separator + "test" + File.separator + "resources");
        if (!filePath.exists()) {
            filePath = new File("components" + File.separator + "data-bridge" + File.separator + "org.wso2.carbon.databridge.agent" + File.separator + "src" + File.separator + "test" + File.separator + "resources");
        }
        if (!(filePath.exists())) {
            filePath = new File(Utils.getCarbonHome() + File.separator + "resources");
        }
        return filePath.getAbsolutePath() + File.separator + "data-bridge-config.yaml";
    }

    public static OMElement loadXML(String path, String fileName) throws DataBridgeConfigurationException {
        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(new File(path)));
            XMLStreamReader parser = XMLInputFactory.newInstance().createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement omElement = builder.getDocumentElement();
            omElement.build();
            return omElement;
        } catch (FileNotFoundException e) {
            String errorMessage = fileName
                    + " cannot be found in the path : " + path;
            log.error(errorMessage, e);
            throw new DataBridgeConfigurationException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + fileName
                    + " located in the path : " + path;
            log.error(errorMessage, e);
            throw new DataBridgeConfigurationException(errorMessage, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                String errorMessage = "Can not close the input stream";
                log.error(errorMessage, e);
            }
        }
    }
}
