package org.wso2.carbon.application.deployer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.wso2.carbon.application.deployer.internal.GadgetTemplateDeployerConstants;
import org.wso2.carbon.application.deployer.internal.GadgetTemplateDeployerException;
import org.wso2.carbon.application.deployer.internal.util.GadgetTemplateDeployerUtility;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.event.execution.manager.core.DeployableTemplate;
import org.wso2.carbon.event.execution.manager.core.TemplateDeployer;
import org.wso2.carbon.event.execution.manager.core.TemplateDeploymentException;
import org.wso2.carbon.utils.CarbonUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class GadgetTemplateDeployer implements TemplateDeployer {
    private static final Log log = LogFactory.getLog(GadgetTemplateDeployer.class);

    @Override
    public String getType() {
        return "gadget";
    }


    @Override
    public void deployArtifact(DeployableTemplate template) throws TemplateDeploymentException {

        String planName = template.getArtifactId();
        String content = template.getArtifact();

        Map<String, String> artifacts = new HashMap<>();
        Map<String, String> properties = new HashMap<>();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new InputSource(new StringReader(content)));
            NodeList configNodes = document.getElementsByTagName(GadgetTemplateDeployerConstants.CONFIG_TAG);
            if (configNodes.getLength() > 0) {
                Node configNode = configNodes.item(0);
                if (configNode.hasChildNodes()) {

                    // Extract the details
                    NodeList nodeList = configNode.getChildNodes();
                    for (int i = 0; i < nodeList.getLength(); i++) {
                        Node node = nodeList.item(i);
                        if (GadgetTemplateDeployerConstants.PROPERTIES_TAG.equalsIgnoreCase(node.getNodeName()) && node.hasChildNodes()) {
                            // Properties
                            NodeList propertiesNodeList = node.getChildNodes();
                            for (int j = 0; j < propertiesNodeList.getLength(); j++) {
                                Node propertyNode = propertiesNodeList.item(j);
                                if (GadgetTemplateDeployerConstants.PROPERTY_TAG.equalsIgnoreCase(propertyNode.getNodeName())) {
                                    Attr attr = (Attr) propertyNode.getAttributes().getNamedItem(GadgetTemplateDeployerConstants.NAME_ATTRIBUTE);
                                    properties.put(attr.getValue(), propertyNode.getFirstChild().getNodeValue());
                                }
                            }
                        } else if (GadgetTemplateDeployerConstants.ARTIFACTS_TAG.equalsIgnoreCase(node.getNodeName()) && node.hasChildNodes()) {
                            NodeList artifactNodeList = node.getChildNodes();
                            for (int j = 0; j < artifactNodeList.getLength(); j++) {
                                Node artifactNode = artifactNodeList.item(j);
                                if (GadgetTemplateDeployerConstants.ARTIFACT_TAG.equalsIgnoreCase(artifactNode.getNodeName())) {
                                    Attr attr = (Attr) artifactNode.getAttributes().getNamedItem(GadgetTemplateDeployerConstants.FILE_ATTRIBUTE);
                                    artifacts.put(attr.getValue(), artifactNode.getFirstChild().getNodeValue());
                                }
                            }
                        }
                    }

                    // Deploy the gadget
                    // Create destination
                    String storePath = getArtifactPath("gadget");
                    File destination = new File(storePath + properties.get(GadgetTemplateDeployerConstants.DIRECTORY_NAME));

                    // Copy the static files
                    String path = new StringBuilder(CarbonUtils.getCarbonConfigDirPath())
                            .append(File.separator).append("execution-manager").append(File.separator)
                            .append("gadget-templates").append(File.separator)
                            .append(properties.get(GadgetTemplateDeployerConstants.TEMPLATE_DIRECTORY)).toString();
                    File templateDirectory = new File(path);

                    // Copy all the default templates
                    GadgetTemplateDeployerUtility.copyFolder(templateDirectory, destination);

                    // Save the artifacts
                    for (String fileName : artifacts.keySet()) {
                        File targetFile = new File(destination, fileName);
                        try (PrintWriter writer = new PrintWriter(targetFile)) {
                            writer.print(artifacts.get(fileName));
                        }
                    }

                    log.info("Gadget [" + planName + "] has been deployed at " + destination
                            .getAbsolutePath());
                }
            }
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new GadgetTemplateDeployerException(e.getMessage(), e);
        }
    }

    @Override
    public void deployIfNotDoneAlready(DeployableTemplate template) throws TemplateDeploymentException {

        log.info("DeployIfNotDoneAlready:" + template.getArtifactId());
    }


    @Override
    public void undeployArtifact(String artifactId) throws TemplateDeploymentException {

        log.info("UnDeployIfNotDoneAlready:" + artifactId);
    }

    /**
     * Returns the absolute path for the artifact store location.
     *
     * @param artifactName name of the artifact.
     * @return path of the artifact
     */
    protected String getArtifactPath(String artifactName) {
        String carbonRepository = CarbonUtils.getCarbonRepository();
        StringBuilder sb = new StringBuilder(carbonRepository);
        sb.append("jaggeryapps").append(File.separator).append(GadgetTemplateDeployerConstants.APP_NAME).append(File.separator)
                .append("store").append(File.separator)
                .append(CarbonContext.getThreadLocalCarbonContext().getTenantDomain()).append(File.separator)
                /*.append(GadgetTemplateDeployerConstants.DEFAULT_STORE_TYPE).append(File.separator)*/.append(artifactName)
                .append(File.separator);
        return sb.toString();
    }
}
