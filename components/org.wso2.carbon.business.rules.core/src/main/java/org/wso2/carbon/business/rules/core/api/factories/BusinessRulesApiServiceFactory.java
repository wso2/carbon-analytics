/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.business.rules.core.api.factories;

import org.wso2.carbon.business.rules.core.api.BusinessRulesApiService;
import org.wso2.carbon.business.rules.core.api.impl.BusinessRulesApiServiceImpl;

/**
 * Factory class for business rules API service
 */
public class BusinessRulesApiServiceFactory {
    private static final BusinessRulesApiService service = new BusinessRulesApiServiceImpl();

    public static BusinessRulesApiService getBusinessRulesApi() {
        return service;
    }
}
