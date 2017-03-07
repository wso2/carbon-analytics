package org.wso2.carbon.event.publisher.template.deployer.internal.util;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.xml.sax.SAXException;

import java.io.IOException;

public class EventPublisherTemplateDeployerHelper {

    public static void updateRegistryMaps(Registry registry, String artifactId, String streamId)
            throws RegistryException {
        Collection infoCollection = registry.get(EventPublisherTemplateDeployerConstants.META_INFO_COLLECTION_PATH, 0, -1);
        infoCollection.addProperty(artifactId, streamId);
        registry.put(EventPublisherTemplateDeployerConstants.META_INFO_COLLECTION_PATH, infoCollection);

        Resource mappingResource;
        String mappingResourceContent = null;
        String mappingResourcePath = EventPublisherTemplateDeployerConstants.
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
            mappingResourceContent += EventPublisherTemplateDeployerConstants.META_INFO_PUBLISHER_NAME_SEPARATER
                                      + artifactId;
        }

        mappingResource.setMediaType("text/plain");
        mappingResource.setContent(mappingResourceContent);
        registry.put(mappingResourcePath, mappingResource);
    }

    public static boolean arePublisherConfigXmlsSimilar(String configXml1, String configXml2)
            throws IOException, SAXException {
        XMLUnit.setIgnoreWhitespace(true);
        Diff diff = XMLUnit.compareXML(configXml1, configXml2);
        return diff.similar();
    }
}
