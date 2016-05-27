package org.wso2.carbon.event.execution.manager.admin.dto.domain;

public class ScenarioInfoDTO {
    private String name;
    private String description;
    private ParameterDTO[] parameterDTOs;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
