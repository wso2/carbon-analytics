package org.wso2.carbon.stream.processor.template.manager.core.api.factories;

import org.wso2.carbon.stream.processor.template.manager.core.api.BusinessRuleApiService;
import org.wso2.carbon.stream.processor.template.manager.core.api.impl.BusinessRuleApiServiceImpl;

public class BusinessRuleApiServiceFactory {
    private final static BusinessRuleApiService service = new BusinessRuleApiServiceImpl();

    public static BusinessRuleApiService getBusinessRuleApi() {
        return service;
    }
}
