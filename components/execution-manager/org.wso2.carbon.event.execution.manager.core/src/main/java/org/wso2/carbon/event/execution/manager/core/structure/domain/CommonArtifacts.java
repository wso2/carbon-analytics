package org.wso2.carbon.event.execution.manager.core.structure.domain;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
public class CommonArtifacts {
    private List<Artifact> artifact;

    public List<Artifact> getArtifact() {
        return artifact;
    }

    public void setArtifact(List<Artifact> artifact) {
        this.artifact = artifact;
    }
}