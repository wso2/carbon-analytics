package org.wso2.carbon.business.rules.core.api.factories;

import org.wso2.carbon.business.rules.core.api.BusinessRulesApiService;
import org.wso2.carbon.business.rules.core.api.impl.BusinessRulesApiServiceImpl;

public class BusinessRulesApiServiceFactory {
    private final static BusinessRulesApiService service = new BusinessRulesApiServiceImpl();

    public static BusinessRulesApiService getBusinessRulesApi() {
        return service;
    }
}
