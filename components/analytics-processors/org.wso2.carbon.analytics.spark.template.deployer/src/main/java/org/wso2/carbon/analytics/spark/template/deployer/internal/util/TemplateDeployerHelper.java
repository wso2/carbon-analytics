package org.wso2.carbon.analytics.spark.template.deployer.internal.util;

import org.wso2.carbon.analytics.spark.template.deployer.internal.data.model.ExecutionParameters;
import org.wso2.carbon.event.template.manager.core.TemplateDeploymentException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.StringReader;

public class TemplateDeployerHelper {

    /**
     * Generates ExecutionParameters object out of the given xmlString
     * @param xmlString The String to be mapped to ExecutionParameters class
     * @return ExecutionParameters object
     * @throws TemplateDeploymentException
     */
    public static ExecutionParameters getExecutionParameters(String xmlString)
            throws TemplateDeploymentException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ExecutionParameters.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            StringReader reader = new StringReader(xmlString);
            return (ExecutionParameters) jaxbUnmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            throw new TemplateDeploymentException("Error occurred when unmarshalling '" + xmlString + "' to generate Execution Parameters.");
        }
    }
}
