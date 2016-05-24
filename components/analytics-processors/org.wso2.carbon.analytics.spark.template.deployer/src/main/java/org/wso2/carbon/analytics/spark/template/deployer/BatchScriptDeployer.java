/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.analytics.spark.template.deployer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsPersistenceException;
import org.wso2.carbon.analytics.spark.template.deployer.internal.BatchScriptDeployerConstants;
import org.wso2.carbon.analytics.spark.template.deployer.internal.BatchScriptDeployerValueHolder;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.execution.manager.core.DeployableTemplate;
import org.wso2.carbon.event.execution.manager.core.TemplateDeployer;
import org.wso2.carbon.event.execution.manager.core.TemplateDeploymentException;

public class BatchScriptDeployer implements TemplateDeployer {

    private static final Log log = LogFactory.getLog(BatchScriptDeployer.class);


    @Override
    public String getType() {
        return "batch";
    }

    @Override
    public void deployArtifact(DeployableTemplate template, String artifactName) throws TemplateDeploymentException {
        try {
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            String artifactId = template.getConfiguration().getDomain()
                    + BatchScriptDeployerConstants.CONFIG_NAME_SEPARATOR + template.getConfiguration().getName();
            BatchScriptDeployerValueHolder.getAnalyticsProcessorService().deleteScript(tenantId, artifactId);

            BatchScriptDeployerValueHolder.getAnalyticsProcessorService().saveScript(tenantId,
                    artifactId, template.getArtifact(), template.getConfiguration().getParameterMap().get("CronExpression"));   //todo
        } catch (AnalyticsPersistenceException e) {
            throw new TemplateDeploymentException("Error when saving batch script." + template.getConfiguration().getName(), e);
        }
    }


    @Override
    public void undeployArtifact(String artifactId) throws TemplateDeploymentException {
        try {
            BatchScriptDeployerValueHolder.getAnalyticsProcessorService().deleteScript(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), artifactId);
        } catch (AnalyticsPersistenceException e) {
            throw new TemplateDeploymentException("Error when deleting batch script " + artifactId, e);
        }
    }
}
