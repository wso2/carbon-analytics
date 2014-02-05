/*
 * Copyright 2013 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.event.output.adaptor.http.internal.ds;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.osgi.service.component.ComponentContext;
import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorFactory;
import org.wso2.carbon.event.output.adaptor.http.HTTPEventAdaptorFactory;

/**
 * @scr.component name="output.http.EventAdaptorService.component" immediate="true"
 */
public class HTTPEventAdaptorServiceDS {
    
    private static final Log log = LogFactory.getLog(HTTPEventAdaptorServiceDS.class);

    protected void activate(ComponentContext context) {
        try {
            HTTPEventAdaptorFactory httpEventAdaptorFactory = new HTTPEventAdaptorFactory();
            context.getBundleContext().registerService(OutputEventAdaptorFactory.class.getName(),
                    httpEventAdaptorFactory, null);
            log.info("Successfully deployed the output HTTP event adaptor service");
        } catch (Throwable e) {
            log.error("Can not create the output HTTP event adaptor service: " + e.getMessage(), e);
        }
    }
    
}
