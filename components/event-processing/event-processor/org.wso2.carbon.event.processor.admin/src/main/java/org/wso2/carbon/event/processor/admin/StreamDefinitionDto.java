package org.wso2.carbon.event.processor.admin;

public class StreamDefinitionDto {

    private String name;
    private String[] metaData;

    public String[] getMetaData() {
        return metaData;
    }

    public void setMetaData(String[] metaData) {
        this.metaData = metaData;
    }

    public String[] getCorrelationData() {
        return correlationData;
    }

    public void setCorrelationData(String[] correlationData) {
        this.correlationData = correlationData;
    }

    public String[] getPayloadData() {
        return payloadData;
    }

    public void setPayloadData(String[] payloadData) {
        this.payloadData = payloadData;
    }

    private String[] correlationData;
    private String[] payloadData;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
