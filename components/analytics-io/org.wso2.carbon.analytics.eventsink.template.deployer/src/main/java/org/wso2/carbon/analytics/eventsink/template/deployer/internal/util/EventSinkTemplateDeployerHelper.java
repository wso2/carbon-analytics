package org.wso2.carbon.analytics.eventsink.template.deployer.internal.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.eventsink.AnalyticsEventStore;
import org.wso2.carbon.analytics.eventsink.AnalyticsTableSchema;
import org.wso2.carbon.analytics.eventsink.internal.util.AnalyticsEventSinkConstants;
import org.wso2.carbon.analytics.eventsink.template.deployer.EventSinkTemplateDeployer;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.execution.manager.core.DeployableTemplate;
import org.wso2.carbon.event.execution.manager.core.TemplateDeploymentException;
import org.wso2.carbon.event.stream.core.internal.util.EventStreamConstants;
import org.wso2.carbon.registry.core.Collection;
import org.wso2.carbon.registry.core.Registry;
import org.wso2.carbon.registry.core.RegistryConstants;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.StringReader;
import java.util.List;

public class EventSinkTemplateDeployerHelper {

    private static final Log log = LogFactory.getLog(EventSinkTemplateDeployer.class);

    public static AnalyticsEventStore unmarshallEventSinkConfig(DeployableTemplate template)
            throws TemplateDeploymentException {
        try {
            String eventSinkConfigXml = template.getArtifact();
            if (eventSinkConfigXml == null || eventSinkConfigXml.isEmpty()) {
                throw new TemplateDeploymentException("EventSink configuration in Domain: " + template.getConfiguration().getDomain()
                                                      + ", Scenario: " +template.getConfiguration().getScenario() + "is empty or not available.");
            }

            JAXBContext context = JAXBContext.newInstance(AnalyticsEventStore.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            StringReader reader = new StringReader(eventSinkConfigXml);
            return  (AnalyticsEventStore) unmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            throw new TemplateDeploymentException("Invalid eventSink configuration in Domain: "
                                                  + template.getConfiguration().getDomain() + ", Scenario: "
                                                  + template.getConfiguration().getScenario() + ". Could not unmarshall.", e);
        }
    }


    public static String getEventStreamName(AnalyticsEventStore analyticsEventStore,
                                            DeployableTemplate template)
            throws TemplateDeploymentException {
        List<String> streamIds;
        //template-validation to avoid NPE
        if (analyticsEventStore.getEventSource() == null) {
            throw new TemplateDeploymentException("Invalid EventSink configuration. No EventSource information given in EventSink Configuration. "
                                                  + "For Domain: " + template.getConfiguration().getDomain() + ", for Scenario: "
                                                  + template.getConfiguration().getScenario());
        }
        streamIds = analyticsEventStore.getEventSource().getStreamIds();
        if (streamIds == null || streamIds.isEmpty()) {
            throw new TemplateDeploymentException("Invalid EventSink configuration. No EventSource information given in EventSink Configuration. "
                                                  + "For Domain: " + template.getConfiguration().getDomain() + ", for Scenario: "
                                                  + template.getConfiguration().getScenario());
        }
        //In a valid configuration, all the stream Id's have the same stream name. Here we get the stream name from zero'th element.
        String[] streamIdComponents = streamIds.get(0).split(EventStreamConstants.STREAM_DEFINITION_DELIMITER);
        if (streamIdComponents.length != 2) {
            throw new TemplateDeploymentException("Invalid Stream Id: " + streamIds.get(0) + " found in Event Sink Configuration.");
        }
        return streamIdComponents[0];
    }


    public static void deleteEventSinkConfigurationFile(int tenantId, String streamName)
            throws TemplateDeploymentException {
        File eventSinkFile = new File(MultitenantUtils.getAxis2RepositoryPath(tenantId) +
                                       AnalyticsEventSinkConstants.DEPLOYMENT_DIR_NAME + File.separator + streamName +
                                       AnalyticsEventSinkConstants.DEPLOYMENT_FILE_EXT);
        if (eventSinkFile.exists()) {
            if (!eventSinkFile.delete()) {
                throw new TemplateDeploymentException("Unable to successfully delete Event Store configuration file : " + eventSinkFile.getName() + " for tenant id : "
                         + tenantId);
            }
        }
    }


    public static void cleanMappingResourceAndUndeploy(int tenantId, Registry registry,
                                                       String mappingResourcePath,
                                                       String artifactId, String streamName)
            throws TemplateDeploymentException {
        try {
            Resource mappingResource = registry.get(mappingResourcePath);
            String mappingResourceContent = new String((byte[]) mappingResource.getContent());

            //Removing artifact ID, along with separator comma.
            int beforeCommaIndex = mappingResourceContent.indexOf(artifactId) - 1;
            int afterCommaIndex = mappingResourceContent.indexOf(artifactId) + artifactId.length();
            if (beforeCommaIndex > 0) {
                mappingResourceContent = mappingResourceContent.replace(
                        EventSinkTemplateDeployerConstants.META_INFO_STREAM_NAME_SEPARATER + artifactId, "");
            } else if (afterCommaIndex < mappingResourceContent.length()) {
                mappingResourceContent = mappingResourceContent.replace(
                        artifactId + EventSinkTemplateDeployerConstants.META_INFO_STREAM_NAME_SEPARATER, "");
            } else {
                mappingResourceContent = mappingResourceContent.replace(artifactId, "");
            }

            if (mappingResourceContent.equals("")) {
                //undeploying existing event sink
                deleteEventSinkConfigurationFile(tenantId, streamName);

                //deleting mappingResource
                registry.delete(mappingResourcePath);
            } else {
                mappingResource.setContent(mappingResourceContent);
                registry.put(mappingResourcePath, mappingResource);
            }
        }  catch (RegistryException e) {
            throw new TemplateDeploymentException("Could not load the Registry for Tenant Domain: "
                                                  + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true)
                                                  + ", when trying to undeploy Event Stream with artifact ID: " + artifactId, e);
        }

    }


    /**
     * Util for removing artifactID (along with the separator comma) from artifactIdList
     * @param artifactId artifact ID, to be removed.
     * @param artifactIdList a comma separated list of artifact IDs.
     * @return artifact ID list, with artifactID being removed.
     */
    public static String removeArtifactIdFromList(String artifactIdList, String artifactId) {
        int beforeCommaIndex = artifactIdList.indexOf(artifactId) - 1;
        int afterCommaIndex = artifactIdList.indexOf(artifactId) + artifactId.length();
        if (beforeCommaIndex > 0) {
            artifactIdList = artifactIdList.replace(
                    EventSinkTemplateDeployerConstants.META_INFO_STREAM_NAME_SEPARATER + artifactId, "");
        } else if (afterCommaIndex < artifactIdList.length()) {
            artifactIdList = artifactIdList.replace(
                    artifactId + EventSinkTemplateDeployerConstants.META_INFO_STREAM_NAME_SEPARATER, "");
        } else {
            artifactIdList = artifactIdList.replace(artifactId, "");
        }
        return artifactIdList;
    }


    public static void updateRegistryMaps(Registry registry, Collection infoCollection,
                                          String artifactId, String streamName)
            throws RegistryException {
        infoCollection.addProperty(artifactId, streamName);
        registry.put(EventSinkTemplateDeployerConstants.META_INFO_COLLECTION_PATH, infoCollection);

        Resource mappingResource;
        String mappingResourceContent = null;
        String mappingResourcePath = EventSinkTemplateDeployerConstants.
                                             META_INFO_COLLECTION_PATH + RegistryConstants.PATH_SEPARATOR + streamName;

        if (registry.resourceExists(mappingResourcePath)) {
            mappingResource = registry.get(mappingResourcePath);
            mappingResourceContent = new String((byte[]) mappingResource.getContent());
        } else {
            mappingResource = registry.newResource();
        }

        if (mappingResourceContent == null) {
            mappingResourceContent = artifactId;
        } else {
            mappingResourceContent += EventSinkTemplateDeployerConstants.META_INFO_STREAM_NAME_SEPARATER
                                      + artifactId;
        }

        mappingResource.setMediaType("text/plain");
        mappingResource.setContent(mappingResourceContent);
        registry.put(mappingResourcePath, mappingResource);
    }


    public static AnalyticsEventStore getExistingEventStore(int tenantId, String streamName)
            throws TemplateDeploymentException {
        File eventSinkFile = new File(MultitenantUtils.getAxis2RepositoryPath(tenantId) +
                                      AnalyticsEventSinkConstants.DEPLOYMENT_DIR_NAME + File.separator + streamName +
                                      AnalyticsEventSinkConstants.DEPLOYMENT_FILE_EXT);
        if (eventSinkFile.exists()) {
            try {
                JAXBContext context = JAXBContext.newInstance(AnalyticsEventStore.class);
                Unmarshaller un = context.createUnmarshaller();
                return (AnalyticsEventStore) un.unmarshal(eventSinkFile);
            } catch (JAXBException e) {
                throw new TemplateDeploymentException("Error while unmarshalling the configuration from file : "
                                                      + eventSinkFile.getPath(), e);
            }
        }
        return null;
    }


    public static void cleanTableSchemaRegistryRecords(Registry registry, String artifactId)
            throws TemplateDeploymentException {
        try {
            if (registry.resourceExists(EventSinkTemplateDeployerConstants.ARTIFACT_ID_TO_COLUMN_DEF_KEYS_COLLECTION_PATH
                                        + RegistryConstants.PATH_SEPARATOR + artifactId)) {
                Resource columnDefKeyResource = registry.get(EventSinkTemplateDeployerConstants.ARTIFACT_ID_TO_COLUMN_DEF_KEYS_COLLECTION_PATH
                                                             + RegistryConstants.PATH_SEPARATOR + artifactId);
                String columnDefKeys;
                if (columnDefKeyResource.getContent() != null) {
                    columnDefKeys = new String((byte[]) columnDefKeyResource.getContent());
                    String[] columnDefKeyArr = columnDefKeys.split(EventSinkTemplateDeployerConstants.COLUMN_DEF_KEY_SEPARATOR);
                    for (int i = 0; i < columnDefKeyArr.length; i++) {
                        if (registry.resourceExists(EventSinkTemplateDeployerConstants.COLUMN_DEF_KEY_TO_ARTIFACT_IDS_COLLECTION_PATH
                                                    + RegistryConstants.PATH_SEPARATOR + columnDefKeyArr[i])) {
                            Resource artifactListResource = registry.get(EventSinkTemplateDeployerConstants.COLUMN_DEF_KEY_TO_ARTIFACT_IDS_COLLECTION_PATH
                                                                         + RegistryConstants.PATH_SEPARATOR + columnDefKeyArr[i]);
                            String artifacts = new String((byte[]) artifactListResource.getContent());
                            artifacts = EventSinkTemplateDeployerHelper.removeArtifactIdFromList(artifacts, artifactId);
                            if (artifacts.isEmpty()) {
                                registry.delete(EventSinkTemplateDeployerConstants.COLUMN_DEF_KEY_TO_ARTIFACT_IDS_COLLECTION_PATH
                                                + RegistryConstants.PATH_SEPARATOR + columnDefKeyArr[i]);
                                if (registry.resourceExists(EventSinkTemplateDeployerConstants.COLUMN_DEF_KEY_TO_COLUMN_DEF_HASH_RESOURCE_PATH)) {
                                    Resource propertyContainer = registry.get(EventSinkTemplateDeployerConstants.COLUMN_DEF_KEY_TO_COLUMN_DEF_HASH_RESOURCE_PATH);
                                    propertyContainer.removeProperty(columnDefKeyArr[i]);
                                    registry.put(EventSinkTemplateDeployerConstants.COLUMN_DEF_KEY_TO_COLUMN_DEF_HASH_RESOURCE_PATH, propertyContainer);
                                }
                            } else {
                                artifactListResource.setContent(artifacts);
                                registry.put(EventSinkTemplateDeployerConstants.COLUMN_DEF_KEY_TO_ARTIFACT_IDS_COLLECTION_PATH
                                             + RegistryConstants.PATH_SEPARATOR + columnDefKeyArr[i], artifactListResource);
                            }
                        } else {
                            log.warn("No record available in registry as which artifact uses the Column Definition, with key: " + columnDefKeyArr[i]
                                     + ". As a result, it might get overwritten by subsequent Event Sink Configuration deployments.");
                        }
                    }
                }
                registry.delete(EventSinkTemplateDeployerConstants.ARTIFACT_ID_TO_COLUMN_DEF_KEYS_COLLECTION_PATH
                                + RegistryConstants.PATH_SEPARATOR + artifactId);
            }
            String artifactIdToStreamIdsResourcePath = EventSinkTemplateDeployerConstants.ARTIFACT_ID_TO_STREAM_IDS_COLLECTION_PATH
                                                       + RegistryConstants.PATH_SEPARATOR + artifactId;
            if (registry.resourceExists(artifactIdToStreamIdsResourcePath)) {
                Resource streamIdResource = registry.get(artifactIdToStreamIdsResourcePath);
                String streamIds;
                if (streamIdResource.getContent() != null) {
                    streamIds = new String((byte[]) streamIdResource.getContent());
                    String[] streamIdArr = streamIds.split(EventSinkTemplateDeployerConstants.SEPARATOR);
                    for (int i = 0; i < streamIdArr.length; i++) {
                        if (registry.resourceExists(EventSinkTemplateDeployerConstants.STREAM_ID_TO_ARTIFACT_IDS_COLLECTION_PATH
                                                    + RegistryConstants.PATH_SEPARATOR + streamIdArr[i])) {
                            Resource artifactListResource = registry.get(EventSinkTemplateDeployerConstants.STREAM_ID_TO_ARTIFACT_IDS_COLLECTION_PATH
                                                                         + RegistryConstants.PATH_SEPARATOR + streamIdArr[i]);
                            String artifacts = new String((byte[]) artifactListResource.getContent());
                            artifacts = EventSinkTemplateDeployerHelper.removeArtifactIdFromList(artifacts, artifactId);
                            artifactListResource.setContent(artifacts);
                            registry.put(EventSinkTemplateDeployerConstants.STREAM_ID_TO_ARTIFACT_IDS_COLLECTION_PATH
                                         + RegistryConstants.PATH_SEPARATOR + streamIdArr[i], artifactListResource);
                            if (artifacts.isEmpty()) {
                                registry.delete(EventSinkTemplateDeployerConstants.STREAM_ID_TO_ARTIFACT_IDS_COLLECTION_PATH
                                                + RegistryConstants.PATH_SEPARATOR + streamIdArr[i]);
                            }
                        }
                    }
                }
                registry.delete(artifactIdToStreamIdsResourcePath);
            }
        } catch (RegistryException e) {
            throw new TemplateDeploymentException("Failed to clean registry records for Artifact ID: " + artifactId, e);
        }


    }

    /**
     * Returns a unique ID for a given Column.
     * @param column
     * @param streamName
     * @return
     */
    public static String getKeyForColumn(AnalyticsTableSchema.Column column, String streamName) {
        return new StringBuilder().append(streamName).append(EventSinkTemplateDeployerConstants.COLUMN_KEY_COMPONENT_SEPARATOR)
                .append(column.getColumnName()).append(EventSinkTemplateDeployerConstants.COLUMN_KEY_COMPONENT_SEPARATOR)
                .append(column.getType()).toString();
    }


    public static void addToArtifactIdToColumnDefMap(Registry registry, String artifactId, String incomingColKey)
            throws TemplateDeploymentException {
        try {
            Resource columnDefKeyResource;
            String columnDefKeyResourcePath = EventSinkTemplateDeployerConstants.ARTIFACT_ID_TO_COLUMN_DEF_KEYS_COLLECTION_PATH
                                              + RegistryConstants.PATH_SEPARATOR + artifactId;
            if (registry.resourceExists(columnDefKeyResourcePath)) {
                columnDefKeyResource = registry.get(columnDefKeyResourcePath);
            } else {
                columnDefKeyResource = registry.newResource();
            }
            String columnDefKeys;
            if (columnDefKeyResource.getContent() == null) {
                columnDefKeys = incomingColKey;
            } else {
                columnDefKeys = new String((byte[]) columnDefKeyResource.getContent());
                columnDefKeys += EventSinkTemplateDeployerConstants.COLUMN_DEF_KEY_SEPARATOR + incomingColKey;
            }
            columnDefKeyResource.setMediaType("text/plain");
            columnDefKeyResource.setContent(columnDefKeys);
            registry.put(columnDefKeyResourcePath, columnDefKeyResource);
        } catch (RegistryException e) {
            throw new TemplateDeploymentException("Failed to update registry when deploying artifact with ID: " + artifactId);
        }
    }


    public static void updateArtifactAndStreamIdMappings(Registry registry, String artifactId,
                                                         List<String> streamIds)
            throws TemplateDeploymentException {
        try {
            Resource artIdToStreamIdsMapResource = registry.newResource();
            StringBuilder idListBuilder = new StringBuilder();
            for (String streamId : streamIds) {
                idListBuilder.append(streamId).append(EventSinkTemplateDeployerConstants.SEPARATOR);
            }
            if (idListBuilder.length() - 1 == idListBuilder.lastIndexOf(EventSinkTemplateDeployerConstants.SEPARATOR)) {
                idListBuilder.deleteCharAt(idListBuilder.length() - 1);
            }
            artIdToStreamIdsMapResource.setMediaType("text/plain");
            artIdToStreamIdsMapResource.setContent(idListBuilder.toString());

            String artIdToStreamIdsMapResourcePath = EventSinkTemplateDeployerConstants.ARTIFACT_ID_TO_STREAM_IDS_COLLECTION_PATH
                                                       + RegistryConstants.PATH_SEPARATOR + artifactId;
            registry.put(artIdToStreamIdsMapResourcePath, artIdToStreamIdsMapResource);

            for (String streamId : streamIds) {
                String resourcePath = EventSinkTemplateDeployerConstants.STREAM_ID_TO_ARTIFACT_IDS_COLLECTION_PATH
                                      + RegistryConstants.PATH_SEPARATOR + streamId;
                Resource artifactIdsResource;
                if (registry.resourceExists(resourcePath)) {
                    artifactIdsResource = registry.get(resourcePath);
                } else {
                    artifactIdsResource = registry.newResource();
                }

                String artifactIds;
                if (artifactIdsResource.getContent() != null) {
                    artifactIds = new String((byte[]) artifactIdsResource.getContent());
                    artifactIds += EventSinkTemplateDeployerConstants.SEPARATOR + artifactId;
                } else {
                    artifactIds = artifactId;
                }

                artifactIdsResource.setMediaType("text/plain");
                artifactIdsResource.setContent(artifactIds);
                registry.put(resourcePath, artifactIdsResource);
            }

        } catch (RegistryException e) {
            throw new TemplateDeploymentException("Could not load the Registry for Tenant Domain: "
                                                  + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true)
                                                  + ", when trying to undeploy Event Stream with artifact ID: " + artifactId, e);
        }
    }
}
