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

import org.apache.axis2.clustering.ClusteringCommand;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.ClusteringMessage;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.message.tracer.handler.conf.RegistryPersistenceManager;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;


/**
 * This class represents the cluster message used to notify the cluster nodes of
 * data source information changes.
 */
public class MessageTracerStatMessage extends ClusteringMessage {

    private static final Log LOG = LogFactory.getLog(MessageTracerStatMessage.class);
    private static final long serialVersionUID = -3716900058204870390L;

    private int tenantId;

    public void setTenantId(int tenantId) {
        this.tenantId = tenantId;
    }

    public int getTenantId() {
        return tenantId;
    }

    @Override
    public ClusteringCommand getResponse() {
        return null;
    }

    @Override
    public void execute(ConfigurationContext configCtx) throws ClusteringFault {
        try {
            PrivilegedCarbonContext.startTenantFlow();
            PrivilegedCarbonContext ctx = PrivilegedCarbonContext.getThreadLocalCarbonContext();
            if (tenantId == MultitenantConstants.SUPER_TENANT_ID) {
                ctx.setTenantId(MultitenantConstants.SUPER_TENANT_ID);
                ctx.setTenantDomain(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME);
            } else {
                ctx.setTenantId(this.getTenantId(), true);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("Cluster message arrived for tenant: " + this.getTenantId());
            }

            RegistryPersistenceManager registryPersistenceManager = new RegistryPersistenceManager();
            registryPersistenceManager.load(this.getTenantId());
        } finally {
            PrivilegedCarbonContext.endTenantFlow();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!super.equals(obj)) {
            return false;
        }
        MessageTracerStatMessage message = (MessageTracerStatMessage) obj;
        if (tenantId == message.getTenantId()) {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
