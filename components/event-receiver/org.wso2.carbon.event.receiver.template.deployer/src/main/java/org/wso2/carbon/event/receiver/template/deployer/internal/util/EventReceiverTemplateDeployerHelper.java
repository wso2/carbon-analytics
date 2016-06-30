package org.wso2.carbon.event.receiver.template.deployer.internal.util;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.execution.manager.core.TemplateDeploymentException;
import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

public class EventReceiverTemplateDeployerHelper {

    public static void updateRegistryMaps(Registry registry, String artifactId, String streamId)
            throws RegistryException {
        Collection infoCollection = registry.get(EventReceiverTemplateDeployerConstants.META_INFO_COLLECTION_PATH, 0, -1);
        infoCollection.addProperty(artifactId, streamId);
        registry.put(EventReceiverTemplateDeployerConstants.META_INFO_COLLECTION_PATH, infoCollection);

        Resource mappingResource;
        String mappingResourceContent = null;
        String mappingResourcePath = EventReceiverTemplateDeployerConstants.
                                             META_INFO_COLLECTION_PATH + RegistryConstants.PATH_SEPARATOR + streamId;

        if (registry.resourceExists(mappingResourcePath)) {
            mappingResource = registry.get(mappingResourcePath);
            mappingResourceContent = new String((byte[]) mappingResource.getContent());
        } else {
            mappingResource = registry.newResource();
        }

        if (mappingResourceContent == null) {
            mappingResourceContent = artifactId;
        } else {
            mappingResourceContent += EventReceiverTemplateDeployerConstants.META_INFO_STREAM_NAME_SEPARATER
                                      + artifactId;
        }

        mappingResource.setMediaType("text/plain");
        mappingResource.setContent(mappingResourceContent);
        registry.put(mappingResourcePath, mappingResource);
    }


    public static void cleanMappingResourceAndUndeploy(int tenantId, Registry registry,
                                                       String mappingResourcePath,
                                                       String artifactId, boolean isUndeploy)
            throws TemplateDeploymentException {
        try {
            Resource mappingResource = registry.get(mappingResourcePath);
            String mappingResourceContent = new String((byte[]) mappingResource.getContent());

            //Removing artifact ID, along with separator comma.
            int beforeCommaIndex = mappingResourceContent.indexOf(artifactId) - 1;
            int afterCommaIndex = mappingResourceContent.indexOf(artifactId) + artifactId.length();
            if (beforeCommaIndex > 0) {
                mappingResourceContent = mappingResourceContent.replace(
                        EventReceiverTemplateDeployerConstants.META_INFO_STREAM_NAME_SEPARATER + artifactId, "");
            } else if (afterCommaIndex < mappingResourceContent.length()) {
                mappingResourceContent = mappingResourceContent.replace(
                        artifactId + EventReceiverTemplateDeployerConstants.META_INFO_STREAM_NAME_SEPARATER, "");
            } else {
                mappingResourceContent = mappingResourceContent.replace(artifactId, "");
            }

            if (mappingResourceContent.equals("")) {
                //undeploying existing event receiver
                deleteEventReceiver(tenantId, artifactId);
                //deleting mappingResource
                registry.delete(mappingResourcePath);
            } else {
                mappingResource.setContent(mappingResourceContent);
                registry.put(mappingResourcePath, mappingResource);
            }
        } catch (RegistryException e) {
            throw new TemplateDeploymentException("Could not load the Registry for Tenant Domain: "
                                                  + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true)
                                                  + ", when trying to undeploy Event Stream with artifact ID: " + artifactId, e);
        }

    }

    private static void deleteEventReceiver(int tenantId, String artifactId)
            throws TemplateDeploymentException {
        File executionPlanFile = new File(MultitenantUtils.getAxis2RepositoryPath(tenantId) +
                                          EventReceiverConstants.ER_CONFIG_DIRECTORY + File.separator + artifactId +
                                          EventReceiverConstants.ER_CONFIG_FILE_EXTENSION_WITH_DOT);
        if (executionPlanFile.exists()) {
            if (!executionPlanFile.delete()) {
                throw new TemplateDeploymentException("Unable to successfully delete Event Receiver File : " + executionPlanFile.getName() + " from File System, for Tenant ID : "
                                                      + tenantId);
            }
        }
    }

    public static boolean areReceiverConfigXmlsSimilar(String configXml1, String configXml2)
            throws IOException, SAXException {
        XMLUnit.setIgnoreWhitespace(true);
        Diff diff = XMLUnit.compareXML(configXml1, configXml2);
        return diff.similar();
    }

    public static void cleanRegistryMaps() {

    }
}
