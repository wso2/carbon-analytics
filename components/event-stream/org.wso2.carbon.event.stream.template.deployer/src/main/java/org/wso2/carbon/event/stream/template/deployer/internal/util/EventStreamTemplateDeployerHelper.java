package org.wso2.carbon.event.stream.template.deployer.internal.util;

import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.template.manager.core.TemplateDeploymentException;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;
import org.wso2.carbon.event.stream.core.internal.util.EventStreamConstants;
import org.wso2.carbon.event.stream.template.deployer.internal.EventStreamTemplateDeployerValueHolder;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class EventStreamTemplateDeployerHelper {

    public static void updateRegistryMaps(Registry registry, Collection infoCollection,
                                          String artifactId, String streamId)
            throws RegistryException {
        infoCollection.addProperty(artifactId, streamId);
        registry.put(EventStreamTemplateDeployerConstants.META_INFO_COLLECTION_PATH, infoCollection);

        Resource mappingResource;
        String mappingResourceContent = null;
        String mappingResourcePath = EventStreamTemplateDeployerConstants.
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
            mappingResourceContent += EventStreamTemplateDeployerConstants.META_INFO_STREAM_NAME_SEPARATER
                                      + artifactId;
        }

        mappingResource.setMediaType("text/plain");
        mappingResource.setContent(mappingResourceContent);
        registry.put(mappingResourcePath, mappingResource);
    }


    public static void cleanMappingResourceAndUndeploy(Registry registry,
                                                       String mappingResourcePath,
                                                       String artifactId, String streamId)
            throws TemplateDeploymentException {
        try {
            Resource mappingResource = registry.get(mappingResourcePath);
            String mappingResourceContent = new String((byte[]) mappingResource.getContent());

            //Removing artifact ID, along with separator comma.
            int beforeCommaIndex = mappingResourceContent.indexOf(artifactId) - 1;
            int afterCommaIndex = mappingResourceContent.indexOf(artifactId) + artifactId.length();
            if (beforeCommaIndex > 0) {
                mappingResourceContent = mappingResourceContent.replace(
                        EventStreamTemplateDeployerConstants.META_INFO_STREAM_NAME_SEPARATER + artifactId, "");
            } else if (afterCommaIndex < mappingResourceContent.length()) {
                mappingResourceContent = mappingResourceContent.replace(
                        artifactId + EventStreamTemplateDeployerConstants.META_INFO_STREAM_NAME_SEPARATER, "");
            } else {
                mappingResourceContent = mappingResourceContent.replace(artifactId, "");
            }

            if (mappingResourceContent.equals("")) {
                //undeploying existing stream
                EventStreamTemplateDeployerValueHolder.getEventStreamService().removeEventStreamDefinition(
                        streamId.split(EventStreamConstants.STREAM_DEFINITION_DELIMITER)[0]
                        , streamId.split(EventStreamConstants.STREAM_DEFINITION_DELIMITER)[1]);
                //deleting mappingResource
                registry.delete(mappingResourcePath);
            } else {
                mappingResource.setContent(mappingResourceContent);
                registry.put(mappingResourcePath, mappingResource);
            }
        } catch (EventStreamConfigurationException e) {
            throw new TemplateDeploymentException("Failed to get stream definition for StreamID: " + streamId + ", hence deployment failed.", e);
        } catch (RegistryException e) {
            throw new TemplateDeploymentException("Could not load the Registry for Tenant Domain: "
                                                  + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true)
                                                  + ", when trying to undeploy Event Stream with artifact ID: " + artifactId, e);
        }

    }
}
