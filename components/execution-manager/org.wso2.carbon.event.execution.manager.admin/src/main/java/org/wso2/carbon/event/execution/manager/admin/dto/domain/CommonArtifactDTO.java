package org.wso2.carbon.event.execution.manager.admin.dto.domain;

/**
 * DTO class of CommonArtifact for ExecutionManagerAdminService
 */
public class CommonArtifactDTO {
    private String type;
    private String artifact;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getArtifact() {
        return artifact;
    }

    public void setArtifact(String artifact) {
        this.artifact = artifact;
    }
}
