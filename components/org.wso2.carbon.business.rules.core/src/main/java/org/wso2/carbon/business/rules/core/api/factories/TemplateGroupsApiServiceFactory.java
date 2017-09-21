package org.wso2.carbon.business.rules.core.api.factories;

import org.wso2.carbon.business.rules.core.api.TemplateGroupsApiService;
import org.wso2.carbon.business.rules.core.api.impl.TemplateGroupsApiServiceImpl;

public class TemplateGroupsApiServiceFactory {
    private final static TemplateGroupsApiService service = new TemplateGroupsApiServiceImpl();

    public static TemplateGroupsApiService getTemplateGroupsApi() {
        return service;
    }
}
