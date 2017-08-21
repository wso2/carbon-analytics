package org.wso2.carbon.stream.processor.template.manager.core.util;


import org.wso2.carbon.stream.processor.template.manager.core.TemplateManagerService;

/**
 * Singleton class for the exposed root.Template Manager Service
 */
public class TemplateManagerInstance {
    private static TemplateManagerService templateManagerInstance;

    private TemplateManagerInstance() {

    }

    /**
     * @return Singleton Template Manager Service instance
     */
    public static TemplateManagerService getInstance() {
        if (templateManagerInstance == null) {
            templateManagerInstance = new TemplateManagerService();
        }
        return templateManagerInstance;
    }
}
