package org.wso2.carbon.business.rules.api.factories;

import org.wso2.carbon.business.rules.api.BusinessRuleApiService;
import org.wso2.carbon.business.rules.api.impl.BusinessRuleApiServiceImpl;

public class BusinessRuleApiServiceFactory {
    private final static BusinessRuleApiService service = new BusinessRuleApiServiceImpl();

    public static BusinessRuleApiService getBusinessRuleApi() {
        return service;
    }
}
