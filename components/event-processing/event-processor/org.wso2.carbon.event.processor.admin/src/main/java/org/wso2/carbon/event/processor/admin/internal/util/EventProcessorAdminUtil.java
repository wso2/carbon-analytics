package org.wso2.carbon.event.processor.admin.internal.util;

public class EventProcessorAdminUtil {
    static String STREAM_SEPERATOR = ":";

    public static String getStreamName(String streamId) {
        return streamId.split(STREAM_SEPERATOR)[0];
    }

    public static String getVersion(String streamId) {
        String[] nameVersion = streamId.split(STREAM_SEPERATOR);
        if (nameVersion.length > 1) {
            return nameVersion[1];
        }
        return "1.0.0";
    }

}
