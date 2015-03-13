package org.wso2.carbon.event.stream.core.internal.util.helper;

import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.commons.StreamDefinition;
import org.wso2.carbon.event.stream.core.EventStreamDeployer;
import org.wso2.carbon.event.stream.core.exception.EventStreamConfigurationException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class EventStreamConfigurationFileSystemInvoker {
    private static final Log log = LogFactory.getLog(EventStreamConfigurationFileSystemInvoker.class);

    public static void save(StreamDefinition streamDefinition, String filePath, AxisConfiguration axisConfig) {
        OutputStreamWriter writer = null;
        EventStreamDeployer eventStreamDeployer = (EventStreamDeployer)((DeploymentEngine) axisConfig.getConfigurator()).getDeployer("eventstreams", "json");
        File file = new File(filePath);
        try {
            eventStreamDeployer.getDeployedEventStreamFilePaths().add(filePath);
            writer = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            writer.write(streamDefinition.toString());
            eventStreamDeployer.executeManualDeployement(filePath);
        } catch (IOException e) {
            eventStreamDeployer.getDeployedEventStreamFilePaths().remove(filePath);
            log.error("Writing the stream definition " + streamDefinition.getStreamId() + " to file is failed ", e);
        } catch (Exception e) {
            log.error("Deploying the stream definition " + streamDefinition.getStreamId() + " is failed ", e);
        } finally {
            if (writer != null) {
                try {
                    writer.flush();
                    writer.close();
                    log.info("Stream definition configuration for " + streamDefinition.getStreamId() + " saved in the filesystem");
                } catch (IOException e) {
                    log.error("Writing the stream definition " + streamDefinition.getStreamId() + "is failed ", e);
                };
            }
        }
    }

    public static void delete(String fileName, AxisConfiguration axisConfig) throws EventStreamConfigurationException {
        String directoryPath =  new File(axisConfig.getRepository().getPath())
                .getAbsolutePath() + File.separator + "eventstreams";
        String filePath =  directoryPath + File.separator + fileName;
        try {
            File file = new File(filePath);
            EventStreamDeployer eventStreamDeployer = (EventStreamDeployer)((DeploymentEngine) axisConfig.getConfigurator()).getDeployer("eventstreams", "json");
            eventStreamDeployer.getUnDeployedEventStreamFilePaths().add(filePath);
            if(!(file.exists() && file.delete())) {
                eventStreamDeployer.getUnDeployedEventStreamFilePaths().remove(filePath);
                throw new EventStreamConfigurationException("File deleting failed " + filePath);
            }
            eventStreamDeployer.executeManualUndeployement(filePath);
        } catch (Exception e) {
            throw new EventStreamConfigurationException("Error while deleting event stream " + fileName, e);
        }
    }
}
