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
package org.wso2.carbon.analytics.spark.template.deployer.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.analytics.spark.core.AnalyticsProcessorService;
import org.wso2.carbon.analytics.spark.template.deployer.BatchScriptTemplateDeployer;
import org.wso2.carbon.event.template.manager.core.TemplateDeployer;
import org.wso2.carbon.analytics.spark.template.deployer.internal.BatchScriptDeployerValueHolder;
import org.wso2.carbon.event.stream.core.EventStreamService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

@Component(
         name = "TemplateDeployer.batch.component", 
         immediate = true)
public class BatchScriptDeployerDS {

    private static final Log log = LogFactory.getLog(BatchScriptDeployerDS.class);

    @Activate
    protected void activate(ComponentContext context) {
        try {
            BatchScriptTemplateDeployer templateDeployer = new BatchScriptTemplateDeployer();
            context.getBundleContext().registerService(TemplateDeployer.class.getName(), templateDeployer, null);
        } catch (RuntimeException e) {
            log.error("Couldn't register BatchScriptDeployer service", e);
        }
    }

    @Reference(
             name = "analyticsProcessorService.service", 
             service = org.wso2.carbon.analytics.spark.core.AnalyticsProcessorService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetAnalyticsProcessorService")
    protected void setAnalyticsProcessorService(AnalyticsProcessorService analyticsProcessorService) {
        BatchScriptDeployerValueHolder.setAnalyticsProcessorService(analyticsProcessorService);
    }

    protected void unsetAnalyticsProcessorService(AnalyticsProcessorService analyticsProcessorService) {
        BatchScriptDeployerValueHolder.setAnalyticsProcessorService(null);
    }

    @Reference(
             name = "eventStreamService.service", 
             service = org.wso2.carbon.event.stream.core.EventStreamService.class, 
             cardinality = ReferenceCardinality.MANDATORY, 
             policy = ReferencePolicy.DYNAMIC, 
             unbind = "unsetEventStreamService")
    protected void setEventStreamService(EventStreamService eventStreamService) {
        BatchScriptDeployerValueHolder.setEventStreamService(eventStreamService);
    }

    protected void unsetEventStreamService(EventStreamService eventStreamService) {
        BatchScriptDeployerValueHolder.setEventStreamService(null);
    }
}

