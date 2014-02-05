package org.wso2.carbon.analytics.hive.extension.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.hive.HiveConstants;
import org.wso2.carbon.analytics.hive.exception.AnalyzerConfigException;
import org.wso2.carbon.analytics.hive.extension.AnalyzerMeta;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class AnalyzerFactory {

    private static final Log log = LogFactory.getLog(AnalyzerFactory.class);

    private static Map<String, AnalyzerMeta> analyzers = new HashMap<String, AnalyzerMeta>();

    public static void loadAnalyzers(String path) throws AnalyzerConfigException {

        BufferedInputStream inputStream = null;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(new File(path)));
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            OMElement omElement = builder.getDocumentElement();
            omElement.build();

            OMElement analyzerElems = omElement.getFirstChildWithName(
                    new QName(HiveConstants.ANALYTICS_NAMESPACE,
                            HiveConstants.ANALYZERS_ELEMENT));

            if (analyzerElems != null) {

                HashSet<String> parameterSet = new HashSet<String>();
                for (Iterator analyzerIterator = analyzerElems.getChildrenWithName(new QName(HiveConstants.ANALYZER_ELEMENT));
                     analyzerIterator.hasNext(); ) {
                    OMElement analyzerElem = (OMElement) analyzerIterator.next();
                    OMElement nameElem = analyzerElem.getFirstChildWithName(new QName(HiveConstants.ANALYTICS_NAMESPACE, HiveConstants.ANALYZER_NAME_ELEMENT));
                    OMElement paramElem = analyzerElem.getFirstChildWithName(new QName(HiveConstants.ANALYTICS_NAMESPACE, HiveConstants.ANALYZER_PARAMS_ELEMENT));
                    OMElement classElem = analyzerElem.getFirstChildWithName(new QName(HiveConstants.ANALYTICS_NAMESPACE, HiveConstants.ANALYZER_CLASS_ELEMENT));

                    if (paramElem != null) {
                        String params = paramElem.getText();
                        params = params.replaceAll("\\s+", "");
                        String[] parameters = params.split(",");

                        for (String p : parameters) {
                            parameterSet.add(p);
                        }

                    }

                    AnalyzerMeta analyzer = new AnalyzerMeta();
                    analyzer.setAnalyzerName(nameElem.getText());
                    analyzer.setClassAnalyzer(classElem.getText());
                    analyzer.setParameters(parameterSet);
                    AnalyzerHolder.addAnalyzer(nameElem.getText(), analyzer);
                }
            }

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

}
