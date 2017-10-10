package org.wso2.carbon.business.rules.core.api.factories;

import org.wso2.carbon.business.rules.core.api.BusinessRulesApiService;
import org.wso2.carbon.business.rules.core.api.impl.BusinessRulesApiServiceImpl;
/**
 * Business Rule Api Service related
 * **/
public class BusinessRulesApiServiceFactory {
    private static final BusinessRulesApiService service = new BusinessRulesApiServiceImpl();

    public static BusinessRulesApiService getBusinessRulesApi() {
        return service;
    }
}
