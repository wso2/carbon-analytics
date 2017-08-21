package org.wso2.carbon.stream.processor.template.manager.core.util;

import java.util.Collection;
import java.util.Map;

/**
 * Represents a RuleTemplate, which consists of one or more Templates
 */
public class RuleTemplate {
    private String name;
    private String type;
    private String instanceCount; // "one" or "many"
    private String script; // Optional
    private String description; // Optional
    private Collection<Template> templates;
    private Map<String, Property> properties; // Name, Property object

    public RuleTemplate(String name, String type, String instanceCount, String script, String description, Collection<Template> templates, Map<String, Property> properties) {
        this.name = name;
        this.type = type;
        this.instanceCount = instanceCount;
        this.script = script;
        this.description = description;
        this.templates = templates;
        this.properties = properties;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getInstanceCount() {
        return instanceCount;
    }

    public void setInstanceCount(String instanceCount) {
        this.instanceCount = instanceCount;
    }

    public String getScript() {
        return script;
    }

    public void setScript(String script) {
        this.script = script;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Collection<Template> getTemplates() {
        return templates;
    }

    public void setTemplates(Collection<Template> templates) {
        this.templates = templates;
    }

    public Map<String, Property> getProperties() {
        return properties;
    }

    public void setProperties(Map<String, Property> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        return "RuleTemplate{" +
                "name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", instanceCount='" + instanceCount + '\'' +
                ", script='" + script + '\'' +
                ", description='" + description + '\'' +
                ", templates=" + templates +
                ", properties=" + properties +
                '}';
    }
}
