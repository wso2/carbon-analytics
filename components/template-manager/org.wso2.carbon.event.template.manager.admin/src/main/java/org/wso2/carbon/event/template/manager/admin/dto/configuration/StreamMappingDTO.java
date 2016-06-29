package org.wso2.carbon.event.template.manager.admin.dto.configuration;

/**
 * DTO class of StreamMapping for TemplateManagerAdminService
 */
public class StreamMappingDTO {

    private String fromStream;
    private String toStream;
    private AttributeMappingDTO[] attributeMappingDTOs;

    public String getFromStream() {
        return fromStream;
    }

    public void setFromStream(String fromStream) {
        this.fromStream = fromStream;
    }

    public String getToStream() {
        return toStream;
    }

    public void setToStream(String toStream) {
        this.toStream = toStream;
    }

    public AttributeMappingDTO[] getAttributeMappingDTOs() {
        return attributeMappingDTOs;
    }

    public void setAttributeMappingDTOs(AttributeMappingDTO[] attributeMappingDTOs) {
        this.attributeMappingDTOs = attributeMappingDTOs;
    }
}
