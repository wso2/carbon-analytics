/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.output.adapter.email.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterFactory;
import org.wso2.carbon.event.output.adapter.email.EmailEventAdapterFactory;
import org.wso2.carbon.utils.ConfigurationContextService;

/**
 * @scr.component component.name="output.Email.AdapterService.component" immediate="true"
 * @scr.reference name="configurationcontext.service"
 * interface="org.wso2.carbon.utils.ConfigurationContextService" cardinality="1..1"
 * policy="dynamic" bind="setConfigurationContextService" unbind="unsetConfigurationContextService"
 */
public class EmailEventAdapterServiceDS {

    private static final Log log = LogFactory.getLog(EmailEventAdapterServiceDS.class);

    /**
     * initialize the email service here service here.
     *
     * @param context
     */
    protected void activate(ComponentContext context) {

        try {
            OutputEventAdapterFactory emailEventAdaptorFactory = new EmailEventAdapterFactory();
            context.getBundleContext().registerService(OutputEventAdapterFactory.class.getName(),
                    emailEventAdaptorFactory, null);
            if (log.isDebugEnabled()) {
                log.debug("Successfully deployed the output Email event adaptor service");
            }
        } catch (RuntimeException e) {
            log.error("Can not create the output Email event adaptor service ", e);
        }
    }

    protected void setConfigurationContextService(
            ConfigurationContextService configurationContextService) {
        EmailEventAdapterServiceValueHolder.registerConfigurationContextService(configurationContextService);
    }

    protected void unsetConfigurationContextService(
            ConfigurationContextService configurationContextService) {

    }


}
