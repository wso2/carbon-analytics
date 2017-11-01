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

package org.wso2.carbon.business.rules.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.business.rules.core.bean.TemplateGroup;
import org.wso2.carbon.business.rules.core.bean.scratch.BusinessRuleFromScratch;
import org.wso2.carbon.business.rules.core.bean.template.BusinessRuleFromTemplate;
import org.wso2.carbon.business.rules.core.exceptions.RuleTemplateScriptException;
import org.wso2.carbon.business.rules.core.exceptions.TemplateManagerHelperException;
import org.wso2.carbon.business.rules.core.util.TemplateManagerHelper;
import org.wso2.carbon.business.rules.core.util.TestUtil;

import java.io.File;

import com.google.gson.JsonObject;

public class BusinessRulesManagerTestcase {
    private final Logger log = LoggerFactory.getLogger(BusinessRulesManagerTestcase.class);

    @Test
    public void validateTemplateGroupTest1() throws TemplateManagerHelperException {
        log.info("BusinessRulesManager Test : validating a templateGroup 1");
        String dir = BusinessRulesManagerTestcase.class.getClassLoader().getResource("valid_template_groups")
                .getFile();
        int count = 0;
        try {
            count = TestUtil.ValidateBusinessRules(dir);
        } catch (RuleTemplateScriptException e) {
            Assert.fail("Invalid template group found. Hence failing the test.");
        }
        int numberOfValidTemplateGroups = new File(dir).listFiles().length;
        Assert.assertEquals(numberOfValidTemplateGroups, count);
    }

    @Test
    public void validateTemplateGroupTest2() throws RuleTemplateScriptException {
        log.info("BusinessRulesManager Test : validating a templateGroup 2");
        String dir = BusinessRulesManagerTestcase.class.getClassLoader().getResource("invalid_template_groups")
                .getFile();
        File file = new File(String.format("%s/%s", dir, "stock-exchange.json"));
        TemplateGroup templateGroup;
        try {
            templateGroup = TemplateManagerHelper.jsonToTemplateGroup(TemplateManagerHelper
                    .fileToJson(file));
            TemplateManagerHelper.validateTemplateGroup(templateGroup);
        } catch (TemplateManagerHelperException e) {
            Assert.assertEquals(e.getLocalizedMessage(), "Invalid TemplateGroup configuration file found. " +
                    "TemplateGroup name  cannot be empty");
        }
    }

    @Test
    public void validateTemplateGroupTest3() throws RuleTemplateScriptException {
        log.info("BusinessRulesManager Test : validating a templateGroup 3");
        String dir = BusinessRulesManagerTestcase.class.getClassLoader().getResource("invalid_template_groups")
                .getFile();
        File file = new File(String.format("%s/%s", dir, "stock-exchange-2.json"));
        TemplateGroup templateGroup;
        try {
            templateGroup = TemplateManagerHelper.jsonToTemplateGroup(TemplateManagerHelper
                    .fileToJson(file));
            TemplateManagerHelper.validateTemplateGroup(templateGroup);
        } catch (TemplateManagerHelperException e) {
            Assert.assertEquals(e.getLocalizedMessage(), "Invalid TemplateGroup configuration file found. " +
                    " UUID cannot be null for templateGroup Stock Exchange");
        }
    }

    @Test
    public void validateTemplateGroupTest4() throws RuleTemplateScriptException {
        log.info("BusinessRulesManager Test : templateGroup validation testcase 4");
        String dir = BusinessRulesManagerTestcase.class.getClassLoader().getResource("invalid_template_groups")
                .getFile();
        File file = new File(String.format("%s/%s", dir, "stock-exchange-3.json"));
        TemplateGroup templateGroup;
        try {
            templateGroup = TemplateManagerHelper.jsonToTemplateGroup(TemplateManagerHelper
                    .fileToJson(file));
            TemplateManagerHelper.validateTemplateGroup(templateGroup);
        } catch (TemplateManagerHelperException e) {
            Assert.assertEquals(e.getLocalizedMessage(), "Invalid rule template - Rule template name is empty ");
        }
    }

    @Test
    public void createBusinssRuleFromAJsonTest1() throws TemplateManagerHelperException {
        log.info("BusinessRulesManager Test : creating a businessRule of type 'template' from a json");
        String dir = BusinessRulesManagerTestcase.class.getClassLoader().getResource("businessRules").getFile();
        File file = new File(String.format("%s/%s", dir, "my-stock-data-analysis.json"));

        JsonObject jsonObject = TemplateManagerHelper.fileToJson(file);
        BusinessRuleFromTemplate businessRule =
                TemplateManagerHelper.jsonToBusinessRuleFromTemplate(jsonObject.get("businessRule").toString());
        Assert.assertEquals(businessRule.getUuid(), "my-stock-data-analysis");
        Assert.assertEquals(businessRule.getName(), "My Stock Data Analysis");
        Assert.assertEquals(businessRule.getTemplateGroupUUID(), "stock-exchange");
        Assert.assertEquals(businessRule.getRuleTemplateUUID(), "stock-data-analysis");
        Assert.assertEquals(businessRule.getType(), "template");
    }

    @Test
    public void createBusinssRuleFromAJsonTest2() throws TemplateManagerHelperException {
        log.info("BusinessRulesManager Test : creating a businessRule of type 'scratch' from a json");
        String dir = BusinessRulesManagerTestcase.class.getClassLoader().getResource("businessRules").getFile();
        File file = new File(String.format("%s/%s", dir, "custom-stock-exchange-analysis-for-wso2.json"));

        JsonObject jsonObject = TemplateManagerHelper.fileToJson(file);
        BusinessRuleFromScratch businessRule =
                TemplateManagerHelper.jsonToBusinessRuleFromScratch(jsonObject.get("businessRule").toString());
        Assert.assertEquals(businessRule.getUuid(), "custom-stock-exchange-analysis-for-wso2");
        Assert.assertEquals(businessRule.getName(), "Custom Stock Exchange Analysis for WSO2");
        Assert.assertEquals(businessRule.getTemplateGroupUUID(), "stock-exchange");
        Assert.assertEquals(businessRule.getInputRuleTemplateUUID(), "stock-exchange-input");
        Assert.assertEquals(businessRule.getOutputRuleTemplateUUID(), "stock-exchange-output");
        Assert.assertEquals(businessRule.getType(), "scratch");
    }
}
