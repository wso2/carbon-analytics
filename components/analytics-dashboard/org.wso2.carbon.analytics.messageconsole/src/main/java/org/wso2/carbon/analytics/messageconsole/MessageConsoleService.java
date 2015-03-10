package org.wso2.carbon.analytics.messageconsole;

/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.analytics.messageconsole.exception.MessageConsoleException;
import org.wso2.carbon.analytics.messageconsole.internal.ServiceHolder;

import java.util.List;

public class MessageConsoleService {

    private static final Log logger = LogFactory.getLog(MessageConsoleService.class);

    public MessageConsoleService() {
    }

    public List<String> listTables() throws MessageConsoleException {
        int tenantId = CarbonContext.getThreadLocalCarbonContext().getTenantId();

        if (logger.isDebugEnabled()) {
            logger.debug("Getting table list from data layer for tenant:" + tenantId);
        }
        try {
            return ServiceHolder.getAnalyticsDataService().listTables(tenantId);
        } catch (AnalyticsException e) {
            logger.error("Unable to get table list from Analytics data layer for tenant: " + tenantId, e);
            throw new MessageConsoleException("Unable to get table list from Analytics data layer for tenant: " +
                                              tenantId, e);
        }
    }
}
