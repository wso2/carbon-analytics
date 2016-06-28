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
import org.wso2.carbon.analytics.spark.core.util.AnalyticsScript;
import org.wso2.carbon.analytics.spark.template.deployer.internal.BatchScriptDeployerValueHolder;
import org.wso2.carbon.analytics.spark.template.deployer.internal.data.model.ExecutionParameters;
import org.wso2.carbon.analytics.spark.template.deployer.internal.util.TemplateDeployerHelper;
import org.wso2.carbon.context.PrivilegedCarbonContext;
import org.wso2.carbon.event.template.manager.core.DeployableTemplate;
import org.wso2.carbon.event.template.manager.core.TemplateDeployer;
import org.wso2.carbon.event.template.manager.core.TemplateDeploymentException;

import java.util.List;

public class BatchScriptTemplateDeployer implements TemplateDeployer {

    private static final Log log = LogFactory.getLog(BatchScriptTemplateDeployer.class);


    @Override
    public String getType() {
        return "batch";
    }


    @Override
    public void deployArtifact(DeployableTemplate template) throws TemplateDeploymentException {
        try {
            if (template == null) {
                throw new TemplateDeploymentException("No artifact received to be deployed.");
            }
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();

            String artifactId = template.getArtifactId();
            ExecutionParameters executionParameters = TemplateDeployerHelper.getExecutionParameters(template.getArtifact());

            BatchScriptDeployerValueHolder.getAnalyticsProcessorService().deleteScript(tenantId, artifactId);

            BatchScriptDeployerValueHolder.getAnalyticsProcessorService().saveScript(tenantId,
                    artifactId, executionParameters.getSparkScript(), executionParameters.getCron());
        } catch (AnalyticsPersistenceException e) {
            throw new TemplateDeploymentException("Error when deploying batch script." +
                                                  template.getConfiguration().getName() + "for tenant domain " +
                                                  PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true), e);
        }
    }


    @Override
    public void deployIfNotDoneAlready(DeployableTemplate template)
            throws TemplateDeploymentException {
        try {
            if (template == null) {
                throw new TemplateDeploymentException("No artifact received to be deployed.");
            }
            String planName = template.getArtifactId();
            int tenantId = PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId();
            List<AnalyticsScript> analyticsScripts = BatchScriptDeployerValueHolder.getAnalyticsProcessorService().getAllScripts(tenantId);
            if (analyticsScripts == null || analyticsScripts.isEmpty()) {
                deployArtifact(template);
            } else {
                if(log.isDebugEnabled()) {
                    log.debug("Common Artifact: " + planName + " of Domain " + template.getConfiguration().getDomain()
                              + " was not deployed as it is already being deployed.");
                }
            }
        } catch (AnalyticsPersistenceException e) {
            throw new TemplateDeploymentException("Error when obtaining all Spark scripts for tenant domain "
                                                  + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true), e);
        }
    }


    @Override
    public void undeployArtifact(String artifactId) throws TemplateDeploymentException {
        try {
            BatchScriptDeployerValueHolder.getAnalyticsProcessorService().deleteScript(PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantId(), artifactId);
        } catch (AnalyticsPersistenceException e) {
            throw new TemplateDeploymentException("Error when deleting batch script " + artifactId +
                                                  "for tenant domain " + PrivilegedCarbonContext.getThreadLocalCarbonContext().getTenantDomain(true), e);
        }
    }

}
