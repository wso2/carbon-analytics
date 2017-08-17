package org.wso2.carbon.stream.processor.template.manager.core.factories;


import org.wso2.carbon.stream.processor.template.manager.core.api.TemplateApiService;
import org.wso2.carbon.stream.processor.template.manager.core.impl.TemplateApiServiceImpl;

public class TemplateApiServiceFactory {
    private final static TemplateApiService service = new TemplateApiServiceImpl();

    public static TemplateApiService getTemplateApi() {
        return service;
    }
}
