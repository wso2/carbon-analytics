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

package org.wso2.carbon.business.rules.core.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.business.rules.core.exceptions.RuleTemplateScriptException;
import org.wso2.carbon.business.rules.core.exceptions.TemplateManagerServiceException;
import org.wso2.carbon.business.rules.core.services.TemplateManagerService;

/**
 * Singleton class for the exposed root.Template Manager Service
 */
public class TemplateManagerInstance {
    private static TemplateManagerService templateManagerInstance;
    private static Logger log = LoggerFactory.getLogger(TemplateManagerInstance.class);

    static {
        try {
            templateManagerInstance = new TemplateManagerService();
        } catch (TemplateManagerServiceException e) {
            log.error("Cannot instantiate a TemplateManagerService instance. ", e);
        } catch (RuleTemplateScriptException e) {
            log.error("Cannot instantiate a TemplateManagerService instance. ", e);
        }
    }

    private TemplateManagerInstance() {

    }

    /**
     * @return Singleton Template Manager Service instance
     */
    public static TemplateManagerService getInstance() {
        return templateManagerInstance;
    }
}
