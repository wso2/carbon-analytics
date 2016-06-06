package org.wso2.carbon.event.execution.manager.admin.dto.domain;

public class ScenarioInfoDTO {
    private String type;
    private String description;
    private ParameterDTO[] parameterDTOs;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ParameterDTO[] getParameterDTOs() {
        return parameterDTOs;
    }

    public void setParameterDTOs(ParameterDTO[] parameterDTOs) {
        this.parameterDTOs = parameterDTOs;
    }
}
