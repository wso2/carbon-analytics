package org.wso2.carbon.event.processor.admin;

/**
 * Represents mapping between a siddhi stream and a cep stream.
 */
public class StreamConfigurationDto {

    private String streamId;

    // for imported streams : as
    // for exported streams : valueOf
    private String siddhiStreamName;

    private boolean passThroughFlowSupported;

    public StreamConfigurationDto() {
    }


    public StreamConfigurationDto(String streamId, String siddhiStreamName) {
        this.siddhiStreamName = siddhiStreamName;
        this.streamId = streamId;
    }

    public String getStreamId() {
        return streamId;
    }


    public void setStreamId(String streamId) {
        this.streamId = streamId;
    }

    public String getSiddhiStreamName() {
        return siddhiStreamName;
    }

    public void setSiddhiStreamName(String siddhiStreamName) {
        this.siddhiStreamName = siddhiStreamName;
    }

    public boolean isPassThroughFlowSupported() {
        return passThroughFlowSupported;
    }

    public void setPassThroughFlowSupported(boolean passThroughFlowSupported) {
        this.passThroughFlowSupported = passThroughFlowSupported;
    }
}
