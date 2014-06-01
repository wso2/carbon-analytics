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

package org.wso2.carbon.databridge.streamdefn.cassandra.datastore;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.core.exception.DataBridgeException;
import org.wso2.carbon.databridge.core.internal.utils.DataBridgeConstants;
import org.wso2.carbon.databridge.streamdefn.cassandra.internal.util.DataSinkConstants;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

public class StreamValidatorUtil {

    private static final Log log = LogFactory.getLog(StreamValidatorUtil.class);

    private static volatile StreamValidatorUtil instance = null;

    private static OMElement configOmElement = null;

    private StreamValidatorUtil() { }

    public static StreamValidatorUtil getInstance() {
        if (instance == null) {
            synchronized (StreamValidatorUtil.class){
                if (instance == null) {
                    instance = new StreamValidatorUtil();
                }
            }
        }
        return instance;
    }

    public boolean isPersistedStream(String streamName, String streamVersion) throws DataBridgeException {
        setConfiguration();
        String streamExpression = streamName + ":" + streamVersion ;
        boolean allStreamsAllowed = this.allStreamsIncludedByDefault();
        ArrayList<String> persistedStreams = this.getPersistedStreamList();
        ArrayList<String> nonPersistedStreams = this.getNonPersistedStreamList();

        if(allStreamsAllowed) {
            if (nonPersistedStreams != null) {
                for (String nonPersistedStream : nonPersistedStreams) {
                    if(this.streamMatchesExpression(streamExpression, nonPersistedStream)) {
                        return false;
                    }
                }
                return true;
            } else {
                return true;
            }

        } else {
            if (persistedStreams != null) {
                for (String persistedStream : persistedStreams) {
                    if(this.streamMatchesExpression(streamExpression, persistedStream)) {
                        return true;
                    }
                }
                return false;
            } else {
                return false;
            }

        }

    }

    private boolean allStreamsIncludedByDefault() throws DataBridgeException {
        try {
            OMElement configXML = configOmElement.getFirstChildWithName(
                    new QName(DataSinkConstants.DATA_SINK_NAMESPACE, DataSinkConstants.DATA_SINK_PERSISTED_STREAMS));
            if(configXML != null) {
                Iterator persistedStreams = configXML.getChildrenWithName(new QName(DataSinkConstants.INCLUDE_STREAMS));
                OMElement persistedStream;
                while (persistedStreams.hasNext()) {
                    persistedStream = (OMElement)persistedStreams.next();
                    if (persistedStream != null) {
                        if ("*".equals(persistedStream.getText().trim())) {
                            return true;
                        }
                    } else {
                        return false;
                    }

                }
                return false;
            } else {
                return false;
            }
        } catch (NumberFormatException ignored) {
            String errorMsg = "Error while getting Cassandra Data Sink Configuration:\n" + ignored.getMessage();
            log.error(errorMsg, ignored);
            throw new DataBridgeException(errorMsg, ignored);
        }
    }

    private ArrayList<String> getPersistedStreamList() throws DataBridgeException {
        try {
            ArrayList<String> output = new ArrayList<String>();
            OMElement configXML = configOmElement.getFirstChildWithName(
                    new QName(DataSinkConstants.DATA_SINK_NAMESPACE, DataSinkConstants.DATA_SINK_PERSISTED_STREAMS));
            if(configXML != null) {
                Iterator persistedStreams = configXML.getChildrenWithName(new QName(DataSinkConstants.INCLUDE_STREAMS));
                OMElement persistedStream;
                while (persistedStreams.hasNext()) {
                    persistedStream = (OMElement)persistedStreams.next();
                    output.add(persistedStream.getText().trim());
                }
                return output;
            } else {
                return null;
            }
        } catch (NumberFormatException ignored) {
            String errorMsg = "Error while getting Cassandra Data Sink Configuration:\n" + ignored.getMessage();
            log.error(errorMsg, ignored);
            throw new DataBridgeException(errorMsg, ignored);
        }
    }

    private ArrayList<String> getNonPersistedStreamList() throws DataBridgeException {
        try {
            ArrayList<String> output = new ArrayList<String>();
            OMElement configXML = configOmElement.getFirstChildWithName(
                    new QName(DataSinkConstants.DATA_SINK_NAMESPACE, DataSinkConstants.DATA_SINK_PERSISTED_STREAMS));
            if(configXML != null) {
                Iterator nonPersistedStreams = configXML.getChildrenWithName(new QName(DataSinkConstants.EXCLUDE_STREAMS));
                OMElement nonPersistedStream;
                while (nonPersistedStreams.hasNext()) {
                    nonPersistedStream = (OMElement)nonPersistedStreams.next();
                    output.add(nonPersistedStream.getText().trim());
                }
                return output;
            } else {
                return null;
            }
        } catch (NumberFormatException ignored) {
            String errorMsg = "Error while getting Cassandra Data Sink Configuration:\n" + ignored.getMessage();
            log.error(errorMsg, ignored);
            throw new DataBridgeException(errorMsg, ignored);
        }
    }

    private boolean streamMatchesExpression (String streamExpression, String stream) {
        if(streamExpression != null && stream != null && streamExpression.split(":") != null) {
            if( stream.split(":").length == 2 ) { // stream name and version both exists
                if(stream.split(":")[0].endsWith("*")) {
                    return streamExpression.split(":")[0].startsWith(stream.substring(0, stream.length() - 1));
                } else {
                    return streamExpression.equals(stream);
                }
            } else if (stream.split(":").length == 1 ) { // only stream name exists
                if(stream.split(":")[0].endsWith("*")) {
                    return streamExpression.split(":")[0].startsWith(stream.substring(0, stream.length() - 1));
                } else {
                    return streamExpression.split(":")[0].equals(stream.split(":")[0]);
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private synchronized static void setConfiguration() throws DataBridgeException {
        if(configOmElement == null) {
            configOmElement = loadConfigXML();
        }
    }

    private static OMElement loadConfigXML() throws DataBridgeException {
        String carbonHome = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
        String path = carbonHome + File.separator + DataBridgeConstants.DATA_BRIDGE_DIR +
                File.separator + DataSinkConstants.DATA_SINK_CONFIG_XML;
        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(new File(path)));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement omElement = builder.getDocumentElement();
            omElement.build();
            return omElement;
        } catch (FileNotFoundException e) {
            String errorMessage = DataSinkConstants.DATA_SINK_CONFIG_XML
                    + "cannot be found in the path : " + path;
            log.error(errorMessage, e);
            throw new DataBridgeException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + DataSinkConstants.DATA_SINK_CONFIG_XML
                    + " located in the path : " + path;
            log.error(errorMessage, e);
            throw new DataBridgeException(errorMessage, e);
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
