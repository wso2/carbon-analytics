package org.wso2.carbon.stream.processor.template.manager.core;

import org.wso2.carbon.stream.processor.template.manager.core.util.Template;
import org.wso2.carbon.stream.processor.template.manager.core.util.TemplateManagerException;

import java.util.Map;

/**
 * Consists of back end methods to handle Templates
 */
public interface TemplateManager { // todo: no need of TemplateManager class anymore
    /**
     * Saves the given root.Template to the directory
     *
     * @param template Given Template Object
     * @param fileName Given Name of the file when saving
     * @throws TemplateManagerException
     */
    public void addTemplate(Template template, String fileName) throws TemplateManagerException;

    /**
     * Deletes the Template with the given name, from the directory
     *
     * @param templateName Given Name of Template
     * @return Deleted Template object
     */
    public Template deleteTemplate(String templateName); // todo: read file & template name. mapping double check

    /**
     * Returns a list of available Templates, from the directory
     *
     * @return List of Template names, and denoting Template objects
     */
    public Map<String, Template> listTemplates();
}
