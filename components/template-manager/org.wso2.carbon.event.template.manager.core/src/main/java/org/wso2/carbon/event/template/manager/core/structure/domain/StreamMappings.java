package org.wso2.carbon.event.template.manager.core.structure.domain;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class StreamMappings {
    private List<StreamMapping> streamMapping;

    public List<StreamMapping> getStreamMapping() {
        return streamMapping;
    }

    public void setStreamMapping(List<StreamMapping> streamMapping) {
        this.streamMapping = streamMapping;
    }
}
