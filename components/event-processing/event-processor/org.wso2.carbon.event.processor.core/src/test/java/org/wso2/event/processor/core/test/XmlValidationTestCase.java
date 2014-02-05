package org.wso2.event.processor.core.test;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.Test;
import org.wso2.carbon.event.processor.core.ExecutionPlanConfiguration;
import org.wso2.carbon.event.processor.core.exception.ExecutionPlanConfigurationException;
import org.wso2.carbon.event.processor.core.internal.util.helper.EventProcessorConfigurationHelper;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class XmlValidationTestCase {
    private static final Log log = LogFactory.getLog(XmlValidationTestCase.class);

    @Test
    public void testOMElementParsing() throws DeploymentException {
        OMElement om = getExecutionPlanOMElement(xmlFile);
        ExecutionPlanConfiguration config = EventProcessorConfigurationHelper.fromOM(om);
        log.info(config.toString());
        // converting back to xml
        OMElement out = EventProcessorConfigurationHelper.toOM(config);
        log.info(out.toString());
    }

    @Test
    public void testOMElementValidation() throws ExecutionPlanConfigurationException, DeploymentException {
        OMElement om = getExecutionPlanOMElement(xmlFile);
        EventProcessorConfigurationHelper.validateExecutionPlanConfiguration(om);
    }


    private OMElement getExecutionPlanOMElement(String file)
            throws DeploymentException {
        OMElement executionPlanElement;
        InputStream inputStream = null;
        try {
            inputStream = new ByteArrayInputStream(file.getBytes());
            XMLStreamReader parser = XMLInputFactory.newInstance().
                    createXMLStreamReader(inputStream);
            StAXOMBuilder builder = new StAXOMBuilder(parser);
            executionPlanElement = builder.getDocumentElement();
            executionPlanElement.build();

        } catch (Exception e) {
            String errorMessage = "unable to parse xml";
            throw new DeploymentException(errorMessage, e);
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.error("", e);
            }
        }
        return executionPlanElement;
    }


    String xmlFile = "<executionPlan name=\"KPIAnalyzer\" xmlns=\"http://wso2.org/carbon/eventprocessor\">\n" +
                     "    <description>\n" +
                     "        Notifies when a user purchases more then 3 phones for the total price higher than $2500.\n" +
                     "    </description>\n" +
                     "    <siddhiConfiguration>\n" +
                     "        <property name=\"siddhi.persistence.snapshot.time.interval.minutes\">0</property>\n" +
                     "        <property name=\"siddhi.enable.distributed.processing\">false</property>\n" +
                     "    </siddhiConfiguration>\n" +
                     "\n" +
                     "\n" +
                     "    <importedStreams>\n" +
                     "        <stream name=\"stockStream\" version=\"1.2.0\" as=\"someName\"/>\n" +
                     "        <!--todo add other parameters-->\n" +
                     "        <stream name=\"phoneRetailStream\"  version=\"1.2.0\"/>\n" +
                     "    </importedStreams>\n" +
                     "\n" +
                     "\n" +
                     "    <queryExpressions>\n" +
                     "        from someName[totalPrice>200 and quantity>1]#window.tableExt:persistence(\"tableWindow\",\n" +
                     "        \"cepdb3.ceptable10\", \"add\", buyer)\n" +
                     "        insert expired-events into highPurchaseStream\n" +
                     "        buyer,brand, quantity, totalPrice;\n" +
                     "    </queryExpressions>\n" +
                     "\n" +
                     "\n" +
                     "    <exportedStreams>\n" +
                     "        <stream valueOf=\"highPurchaseStream\" name=\"newname\" version=\"1.2.0\"/>\n" +
                     "    </exportedStreams>\n" +
                     "\n" +
                     "</executionPlan>\n";
}
