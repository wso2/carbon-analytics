package org.wso2.carbon.event.execution.manager.admin.dto.domain;

/**
 * DTO class of TemplateConfiguration element
 */
public class ScenarioDTO {
    private String name;
    private String description;
    private TemplateDTO[] templateDTOs;
    private StreamMappingDTO[] streamMappingDTOs;
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

    public TemplateDTO[] getTemplateDTOs() {
        return templateDTOs;
    }

    public void setTemplateDTOs(TemplateDTO[] templateDTOs) {
        this.templateDTOs = templateDTOs;
    }

    public StreamMappingDTO[] getStreamMappingDTOs() {
        return streamMappingDTOs;
    }

    public void setStreamMappingDTOs(StreamMappingDTO[] streamMappingDTOs) {
        this.streamMappingDTOs = streamMappingDTOs;
    }

    public ParameterDTO[] getParameterDTOs() {
        return parameterDTOs;
    }

    public void setParameterDTOs(ParameterDTO[] parameterDTOs) {
        this.parameterDTOs = parameterDTOs;
    }
}
