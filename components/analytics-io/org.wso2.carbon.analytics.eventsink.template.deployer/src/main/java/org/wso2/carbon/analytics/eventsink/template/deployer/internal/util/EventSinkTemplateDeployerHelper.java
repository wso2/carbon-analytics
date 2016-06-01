package org.wso2.carbon.analytics.eventsink.template.deployer.internal.util;

import org.wso2.carbon.analytics.eventsink.AnalyticsEventStore;
import org.wso2.carbon.analytics.eventsink.internal.util.AnalyticsEventSinkConstants;
import org.wso2.carbon.event.execution.manager.core.DeployableTemplate;
import org.wso2.carbon.event.execution.manager.core.TemplateDeploymentException;
import org.wso2.carbon.event.stream.core.internal.util.EventStreamConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.StringReader;
import java.util.List;

public class EventSinkTemplateDeployerHelper {

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
}
