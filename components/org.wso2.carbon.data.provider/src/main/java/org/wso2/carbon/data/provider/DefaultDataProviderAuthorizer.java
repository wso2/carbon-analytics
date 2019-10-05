/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.data.provider;

import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.data.provider.bean.DataProviderConfigRoot;
import org.wso2.carbon.data.provider.exception.DataProviderException;

/**
 * Default implementation for Data provider Authorizer.
 */
@Component(
        service = DataProviderAuthorizer.class,
        immediate = true
)
public class DefaultDataProviderAuthorizer implements DataProviderAuthorizer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultDataProviderAuthorizer.class);
    @Override
    public boolean authorize(DataProviderConfigRoot dataProviderConfigRoot) throws DataProviderException {
        LOGGER.debug("Authorized via the '{}' class.", this.getClass().getName());
        return true;
    }
}
