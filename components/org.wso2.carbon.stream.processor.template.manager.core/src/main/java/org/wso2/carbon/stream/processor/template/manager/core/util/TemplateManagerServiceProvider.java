package org.wso2.carbon.stream.processor.template.manager.core.util;

import org.wso2.carbon.stream.processor.template.manager.core.TemplateManagerService;

/**
 * Created by minudika on 18/8/17.
 */
public class TemplateManagerServiceProvider {
    private static TemplateManagerService templateManagerService = new TemplateManagerService();
    private TemplateManagerServiceProvider() {}

    public static TemplateManagerService getTemplateManagerService() {
        return templateManagerService;
    }
}
