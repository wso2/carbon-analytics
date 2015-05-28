/*
 * *
 *  * Copyright (c) 2005 - ${YEAR}, WSO2 Inc. (http://www.wso2.com) All Rights Reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */
package org.wso2.carbon.analytics.message.tracer.handler.publish;

import org.apache.axis2.clustering.ClusteringAgent;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.message.tracer.handler.util.ServiceHolder;
import org.wso2.carbon.utils.ConfigurationContextService;

public class ClusterNotifier {

    private static final Log LOG = LogFactory.getLog(ClusterNotifier.class);

    private int tenantId;

    public int getTenantId() {
        return tenantId;
    }

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public void notifyClusterMessageTraceChange() {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Notifying cluster message trace updates.");
        }
        ConfigurationContextService configCtxService = ServiceHolder.getConfigurationContextService();
        if (configCtxService == null) {
            LOG.error("ConfigurationContextService is empty.");
            return;
        }
        ConfigurationContext configCtx = configCtxService.getServerConfigContext();
        ClusteringAgent agent = configCtx.getAxisConfiguration().getClusteringAgent();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Clustering Agent: " + agent);
        }
        if (agent != null) {
            MessageTracerStatMessage msg = new MessageTracerStatMessage();
            msg.setTenantId(this.getTenantId());
            try {
                agent.sendMessage(msg, true);
            } catch (ClusteringFault e) {
                LOG.error("Unable to send cluster message :" + e.getMessage(), e);
            }
        }
    }
}
