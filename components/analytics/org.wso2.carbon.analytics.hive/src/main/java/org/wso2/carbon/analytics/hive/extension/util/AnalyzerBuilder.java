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
package org.wso2.carbon.analytics.hive.extension.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.hive.HiveConstants;
import org.wso2.carbon.analytics.hive.exception.AnalyzerConfigException;
import org.wso2.carbon.utils.ServerConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.*;

/**
 *Build analyzer from analyzer-config.xml
 */
public class AnalyzerBuilder {
    private static final Log log = LogFactory.getLog(AnalyzerBuilder.class);


    private static Map<String, String> analyzerClasses = new HashMap<String, String>();

    private static Map<String, Set<String>> analyzerParams = new HashMap<String, Set<String>>();



    public static OMElement loadConfigXML() throws AnalyzerConfigException {

        String carbonHome = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
        String path = carbonHome + File.separator + HiveConstants.ANALYZER_CONFIG_XML;

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
            String errorMessage = HiveConstants.ANALYZER_CONFIG_XML
                    + "cannot be found in the path : " + path;
            log.error(errorMessage, e);
            throw new AnalyzerConfigException(errorMessage, e);
        } catch (XMLStreamException e) {
            String errorMessage = "Invalid XML for " + HiveConstants.ANALYZER_CONFIG_XML
                    + " located in the path : " + path;
            log.error(errorMessage, e);
            throw new AnalyzerConfigException(errorMessage, e);
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


    public static void populateAnalyzers(OMElement config) {

        OMElement analyzerElems = config.getFirstChildWithName(
                new QName(HiveConstants.ANALYTICS_NAMESPACE,
                        HiveConstants.ANALYZERS_ELEMENT));

        if (analyzerElems != null) {

                HashSet<String> parameterSet =new HashSet<String>();
            for (Iterator analyzerIterator = analyzerElems.getChildrenWithName(new QName(HiveConstants.ANALYZER_ELEMENT));
                 analyzerIterator.hasNext(); ) {
                OMElement analyzer = (OMElement) analyzerIterator.next();
                OMElement nameElem = analyzer.getFirstChildWithName(new QName(HiveConstants.ANALYTICS_NAMESPACE,HiveConstants.ANALYZER_NAME_ELEMENT));
                OMElement paramElem = analyzer.getFirstChildWithName(new QName(HiveConstants.ANALYTICS_NAMESPACE,HiveConstants.ANALYZER_PARAMS_ELEMENT));
                OMElement classElem = analyzer.getFirstChildWithName(new QName(HiveConstants.ANALYTICS_NAMESPACE,HiveConstants.ANALYZER_CLASS_ELEMENT));

                if(paramElem !=null){
                    String params=paramElem.getText();
                    params=params.replaceAll("\\s+", "");
                    String[] parameters = params.split(",");

                    for (String p : parameters) {
                        parameterSet.add(p);
                    }


                }
                analyzerParams.put(nameElem.getText(), parameterSet);
                analyzerClasses.put(nameElem.getText(),classElem.getText());
            }
        }


    }

    public static Map<String, Set<String>> getAnalyzerParams() {
        return analyzerParams;
    }

    public static void setAnalyzerParams(Map<String, Set<String>> analyzerParams) {
        AnalyzerBuilder.analyzerParams = analyzerParams;
    }

    public static Map<String, String> getAnalyzerClasses() {
        return analyzerClasses;
    }

    public static void setAnalyzerClasses(Map<String, String> analyzerClasses) {
        AnalyzerBuilder.analyzerClasses = analyzerClasses;
    }

}
