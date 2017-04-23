package org.wso2.carbon.event.simulator.core.internal.util;

/**
 * DirectoryDestinationUtil is responsible for setting the destination where the directories 'csvFiles' and
 * 'simulationConfigs' needs to be created
 */
public class DirectoryDestinationUtil {
    private static String directoryDestination;

    public static String getDirectoryPath() {
        return directoryDestination;
    }

    public static void setDirectoryPath(String directoryDestination) {
        DirectoryDestinationUtil.directoryDestination = directoryDestination;
    }
}
