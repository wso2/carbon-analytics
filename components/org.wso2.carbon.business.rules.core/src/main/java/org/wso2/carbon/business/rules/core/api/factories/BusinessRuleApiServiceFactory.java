package org.wso2.carbon.business.rules.core.api.factories;

import org.wso2.carbon.business.rules.core.api.BusinessRuleApiService;
import org.wso2.carbon.business.rules.core.api.impl.BusinessRuleApiServiceImpl;

public class BusinessRuleApiServiceFactory {
    private final static BusinessRuleApiService service = new BusinessRuleApiServiceImpl();

    public static BusinessRuleApiService getBusinessRuleApi() {
        return service;
    }
}
