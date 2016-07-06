package org.wso2.carbon.event.template.manager.core.structure.domain;

import org.wso2.carbon.event.template.manager.core.structure.domain.handler.TemplateHandler;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlMixed;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "template")
public class Template {

    @XmlAttribute(name = "type")
    private String type;

    @XmlAnyElement(TemplateHandler.class)
    @XmlMixed
    private List<Object> content;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    private List<Object> getContent() {
        return content;
    }

    private void setContent(List<Object> content) {
        this.content = content;
    }

    public String getValue() {
        if (this.content != null && !content.isEmpty()) {
            StringBuilder builder = new StringBuilder();
            for (Object obj : content) {
                if (obj instanceof String) {
                    String contentItem = obj.toString();
                    if (!contentItem.trim().isEmpty()) {
                        builder.append(contentItem);
                    }
                }
            }
            String value = builder.toString().trim();
            // Remove duplicate xml entries
            if (!value.isEmpty()) {
                String[] elements = value.split("<\\?.*\\?>");
                if (elements.length > 1) {
                    return elements[elements.length - 1];
                } else {
                    return value;
                }
            }
        }
        return null;
    }
}
