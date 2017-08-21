package org.wso2.carbon.stream.processor.template.manager.core.util;

/**
 * Represents a Templated item
 * Eg: SiddhiApp, //todo: Gadget & Dashboard
 */
public class Template {
    private String type;
    private String content;

    public Template(String type, String content) {
        this.type = type;
        this.content = content;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "Template{" +
                "type='" + type + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
