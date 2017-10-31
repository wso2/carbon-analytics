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
import org.wso2.carbon.business.rules.core.util.TemplateManagerConstants;
import org.wso2.carbon.business.rules.core.util.TemplateManagerHelper;
import org.wso2.carbon.business.rules.core.util.TestUtil;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonObject;

public class BusinessRulesManagerTestcase {
    private final Logger log = LoggerFactory.getLogger(BusinessRulesManagerTestcase.class);

    // TemplateManagerHelper class related
    @Test
    public void validateTemplateGroupTest1() throws TemplateManagerHelperException {
        log.info("BusinessRulesManager Test : Checking valid template groups");
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
        log.info("BusinessRulesManager Test : Checking a valid template group 1");
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
        log.info("BusinessRulesManager Test : Checking a valid template group 2");
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
        log.info("BusinessRulesManager Test : Checking a valid template group 3");
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

    @Test
    public void validateRuleTemplateTypesTest1() throws RuleTemplateScriptException, TemplateManagerHelperException {
        log.info("BusinessRulesManager Test : Checking Rule Templates of valid types");
        String dir = BusinessRulesManagerTestcase.class.getClassLoader().getResource
                ("rule_templates/type/valid")
                .getFile();
        int count = 0;
        try {
            count = TestUtil.ValidateBusinessRules(dir);
        } catch (RuleTemplateScriptException e) {
            Assert.fail("Invalid template group. Hence failing the test.");
        }
        int numberOfValidRuleTemplates = new File(dir).listFiles().length;
        Assert.assertEquals(numberOfValidRuleTemplates, count);
    }

    @Test
    public void validateRuleTemplateTypesTest2() {
        log.info("BusinessRulesManager Test : Checking a Rule Template without a type");
        String dir = BusinessRulesManagerTestcase.class.getClassLoader().getResource
                ("rule_templates/type/invalid").getFile();
        File file = new File(String.format("%s/%s", dir, "type_not_provided.json"));

        JsonObject jsonObject = null;
        try {
            jsonObject = TemplateManagerHelper.fileToJson(file);
        } catch (TemplateManagerHelperException e) {
            Assert.fail("Invalid template group. Hence failing the test.");
        }
        TemplateGroup templateGroup = TemplateManagerHelper.jsonToTemplateGroup(jsonObject);
        boolean isCaught = false;
        try {
            TemplateManagerHelper.validateTemplateGroup(templateGroup);
        } catch (RuleTemplateScriptException e) {
            Assert.fail("Unable to process a valid script. Hence failing the test.");
        } catch (TemplateManagerHelperException e) {
            isCaught = true;
        }

        Assert.assertEquals(isCaught, true);
    }

    @Test
    public void validateRuleTemplateTypesTest3() {
        log.info("BusinessRulesManager Test : Checking a Rule Template with an unknown type");
        String dir = BusinessRulesManagerTestcase.class.getClassLoader().getResource
                ("rule_templates/type/invalid").getFile();
        File file = new File(String.format("%s/%s", dir, "type_unknown.json"));

        JsonObject jsonObject = null;
        try {
            jsonObject = TemplateManagerHelper.fileToJson(file);
        } catch (TemplateManagerHelperException e) {
            Assert.fail("Invalid template group. Hence failing the test.");
        }
        TemplateGroup templateGroup = TemplateManagerHelper.jsonToTemplateGroup(jsonObject);
        boolean isCaught = false;
        try {
            TemplateManagerHelper.validateTemplateGroup(templateGroup);
        } catch (RuleTemplateScriptException e) {
            Assert.fail("Unable to process a valid script. Hence failing the test.");
        } catch (TemplateManagerHelperException e) {
            isCaught = true;
        }

        Assert.assertEquals(isCaught, true);
    }

    @Test
    public void validateRuleTemplateInstanceCountTest1() {
        log.info("BusinessRulesManager Test : validating Rule Template instance counts");
        String dir = BusinessRulesManagerTestcase.class.getClassLoader().getResource
                ("rule_templates/instance_count/valid")
                .getFile();
        int count = 0;
        try {
            count = TestUtil.ValidateBusinessRules(dir);
        } catch (RuleTemplateScriptException|TemplateManagerHelperException e) {
            Assert.fail("Invalid template group found. Hence failing the test.");
        }
        int numberOfValidTemplateGroups = new File(dir).listFiles().length;
        Assert.assertEquals(numberOfValidTemplateGroups, count);
    }

    @Test
    public void validateRuleTemplateInstanceCountTest2() {
        log.info("BusinessRulesManager Test : Checking Rule Template with unknown instance count");
        String dir = BusinessRulesManagerTestcase.class.getClassLoader().getResource
                ("rule_templates/instance_count/invalid").getFile();
        File file = new File(String.format("%s/%s", dir, "instance_count_unknown.json"));

        JsonObject jsonObject = null;
        try {
            jsonObject = TemplateManagerHelper.fileToJson(file);
        } catch (TemplateManagerHelperException e) {
            Assert.fail("Invalid template group. Hence failing the test.");
        }
        TemplateGroup templateGroup = TemplateManagerHelper.jsonToTemplateGroup(jsonObject);
        boolean isCaught = false;

        try {
            TemplateManagerHelper.validateTemplateGroup(templateGroup);
        } catch (RuleTemplateScriptException e) {
            Assert.fail("Unable to process a valid script. Hence failing the test");
        } catch (TemplateManagerHelperException e) {
            isCaught = true;
        }

        Assert.assertEquals(isCaught, true);
    }

    @Test
    public void validateRuleTemplateInstanceCountTest3() {
        log.info("BusinessRulesManager Test : Checking Rule Template without any instance count given");
        String dir = BusinessRulesManagerTestcase.class.getClassLoader().getResource
                ("rule_templates/instance_count/invalid").getFile();
        File file = new File(String.format("%s/%s", dir, "instance_count_not_given.json"));

        JsonObject jsonObject = null;
        try {
            jsonObject = TemplateManagerHelper.fileToJson(file);
        } catch (TemplateManagerHelperException e) {
            Assert.fail("Invalid template group. Hence failing the test.");
        }
        TemplateGroup templateGroup = TemplateManagerHelper.jsonToTemplateGroup(jsonObject);
        boolean isCaught = false;

        try {
            TemplateManagerHelper.validateTemplateGroup(templateGroup);
        } catch (RuleTemplateScriptException e) {
            Assert.fail("Unable to process a valid script. Hence failing the test");
        } catch (TemplateManagerHelperException e) {
            isCaught = true;
        }

        Assert.assertEquals(isCaught, true);
    }

    @Test
    public void validateRuleTemplateScriptTest1() {
        log.info("BusinessRulesManager Test : Checking a Rule Template with a valid script");
        String dir = BusinessRulesManagerTestcase.class.getClassLoader().getResource
                ("rule_templates/script/valid").getFile();
        File file = new File(String.format("%s/%s", dir, "valid_script.json"));

        JsonObject jsonObject = null;
        try {
            jsonObject = TemplateManagerHelper.fileToJson(file);
        } catch (TemplateManagerHelperException e) {
            Assert.fail("Invalid template group. Hence failing the test.");
        }
        TemplateGroup templateGroup = TemplateManagerHelper.jsonToTemplateGroup(jsonObject);

        try {
            TemplateManagerHelper.validateTemplateGroup(templateGroup);
        } catch (RuleTemplateScriptException e) {
            Assert.fail("Unable to process a valid script. Hence failing the test.");
        } catch (TemplateManagerHelperException e) {
            Assert.fail("Unable to convert a valid rule template. Hence failing the test.");
        }
    }

    @Test
    public void validateRuleTemplateScriptTest2() {
        log.info("BusinessRulesManager Test : Checking Rule Templates script with compilation error");
        String dir = BusinessRulesManagerTestcase.class.getClassLoader().getResource
                ("rule_templates/script/invalid").getFile();
        File file = new File(String.format("%s/%s", dir, "script_with_compilation_error.json"));

        JsonObject jsonObject = null;
        try {
            jsonObject = TemplateManagerHelper.fileToJson(file);
        } catch (TemplateManagerHelperException e) {
            Assert.fail("Invalid template group. Hence failing the test.");
        }
        TemplateGroup templateGroup = TemplateManagerHelper.jsonToTemplateGroup(jsonObject);
        boolean isCaught = false;

        try {
            TemplateManagerHelper.validateTemplateGroup(templateGroup);
        } catch (RuleTemplateScriptException e) {
            isCaught = true;
        } catch (TemplateManagerHelperException e) {
            Assert.fail("Unable to convert a valid rule template. Hence failing the test.");
        }

        Assert.assertEquals(isCaught, true);
    }

    @Test
    public void validateRuleTemplateScriptTest3() {
        log.info("BusinessRulesManager Test : Checking Rule Templates script with compilation error");
        String dir = BusinessRulesManagerTestcase.class.getClassLoader().getResource
                ("rule_templates/script/invalid").getFile();
        File file = new File(String.format("%s/%s", dir, "no_replacement_for_a_templated_element_in_script.json"));

        JsonObject jsonObject = null;
        try {
            jsonObject = TemplateManagerHelper.fileToJson(file);
        } catch (TemplateManagerHelperException e) {
            Assert.fail("Invalid template group. Hence failing the test.");
        }
        TemplateGroup templateGroup = TemplateManagerHelper.jsonToTemplateGroup(jsonObject);
        boolean isCaught = false;

        try {
            TemplateManagerHelper.validateTemplateGroup(templateGroup);
        } catch (RuleTemplateScriptException e) {
            Assert.fail("Should not check the script before replacing elements. Hence failing the test");
        } catch (TemplateManagerHelperException e) {
            isCaught = true;
        }

        Assert.assertEquals(isCaught, true);
    }

    @Test
    public void validateTemplatesTest1() {
        log.info("BusinessRulesManager Test : validating with required number of Templates for specific types of " +
                "RuleTemplate");
        String dir = BusinessRulesManagerTestcase.class.getClassLoader().getResource
                ("templates/number_of_templates/valid")
                .getFile();
        int count = 0;
        try {
            count = TestUtil.ValidateBusinessRules(dir);
        } catch (RuleTemplateScriptException|TemplateManagerHelperException e) {
            Assert.fail("Invalid template group found. Hence failing the test.");
        }
        int numberOfValidTemplateGroups = new File(dir).listFiles().length;
        Assert.assertEquals(numberOfValidTemplateGroups, count);
    }

    @Test
    public void validateTemplatesTest2() {
        log.info("BusinessRulesManager Test : validating with many templates under input rule template");
        String dir = BusinessRulesManagerTestcase.class.getClassLoader().getResource
                ("templates/number_of_templates/invalid").getFile();
        File file = new File(String.format("%s/%s", dir, "type_input_many_templates.json"));

        JsonObject jsonObject = null;

        try {
            jsonObject = TemplateManagerHelper.fileToJson(file);
        } catch (TemplateManagerHelperException e) {
            Assert.fail("Failed to convert a valid json file. Hence failing the test");
        }

        TemplateGroup templateGroup = TemplateManagerHelper.jsonToTemplateGroup(jsonObject);
        boolean isCaught = false;

        try {
            TemplateManagerHelper.validateTemplateGroup(templateGroup);
        } catch (RuleTemplateScriptException e) {
            Assert.fail("Failed to process a valid script. Hence failing the test");
        } catch (TemplateManagerHelperException e) {
            isCaught = true;
        }

        Assert.assertEquals(isCaught, true);
    }

    @Test
    public void validateTemplatesTest3() {
        log.info("BusinessRulesManager Test : validating with many templates under output rule template");
        String dir = BusinessRulesManagerTestcase.class.getClassLoader().getResource
                ("templates/number_of_templates/invalid").getFile();
        File file = new File(String.format("%s/%s", dir, "type_output_many_templates.json"));

        JsonObject jsonObject = null;

        try {
            jsonObject = TemplateManagerHelper.fileToJson(file);
        } catch (TemplateManagerHelperException e) {
            Assert.fail("Failed to convert a valid json file. Hence failing the test");
        }

        TemplateGroup templateGroup = TemplateManagerHelper.jsonToTemplateGroup(jsonObject);
        boolean isCaught = false;

        try {
            TemplateManagerHelper.validateTemplateGroup(templateGroup);
        } catch (RuleTemplateScriptException e) {
            Assert.fail("Failed to process a valid script. Hence failing the test");
        } catch (TemplateManagerHelperException e) {
            isCaught = true;
        }

        Assert.assertEquals(isCaught, true);
    }

    @Test
    public void validateTemplatesTest4() {
        log.info("BusinessRulesManager Test : validating with no templates under input rule template");
        String dir = BusinessRulesManagerTestcase.class.getClassLoader().getResource
                ("templates/number_of_templates/invalid").getFile();
        File file = new File(String.format("%s/%s", dir, "type_input_no_templates.json"));

        JsonObject jsonObject = null;

        try {
            jsonObject = TemplateManagerHelper.fileToJson(file);
        } catch (TemplateManagerHelperException e) {
            Assert.fail("Failed to convert a valid json file. Hence failing the test");
        }

        TemplateGroup templateGroup = TemplateManagerHelper.jsonToTemplateGroup(jsonObject);
        boolean isCaught = false;

        try {
            TemplateManagerHelper.validateTemplateGroup(templateGroup);
        } catch (RuleTemplateScriptException e) {
            Assert.fail("Failed to process a valid script. Hence failing the test");
        } catch (TemplateManagerHelperException e) {
            isCaught = true;
        }

        Assert.assertEquals(isCaught, true);
    }

    @Test
    public void validateTemplatesTest5() {
        log.info("BusinessRulesManager Test : validating with no templates under output rule template");
        String dir = BusinessRulesManagerTestcase.class.getClassLoader().getResource
                ("templates/number_of_templates/invalid").getFile();
        File file = new File(String.format("%s/%s", dir, "type_output_no_templates.json"));

        JsonObject jsonObject = null;

        try {
            jsonObject = TemplateManagerHelper.fileToJson(file);
        } catch (TemplateManagerHelperException e) {
            Assert.fail("Failed to convert a valid json file. Hence failing the test");
        }

        TemplateGroup templateGroup = TemplateManagerHelper.jsonToTemplateGroup(jsonObject);
        boolean isCaught = false;

        try {
            TemplateManagerHelper.validateTemplateGroup(templateGroup);
        } catch (RuleTemplateScriptException e) {
            Assert.fail("Failed to process a valid script. Hence failing the test");
        } catch (TemplateManagerHelperException e) {
            isCaught = true;
        }

        Assert.assertEquals(isCaught, true);
    }

    @Test
    public void validateTemplatesTest6() {
        log.info("BusinessRulesManager Test : validating with no templates under template rule template");
        String dir = BusinessRulesManagerTestcase.class.getClassLoader().getResource
                ("templates/number_of_templates/invalid").getFile();
        File file = new File(String.format("%s/%s", dir, "type_template_no_templates.json"));

        JsonObject jsonObject = null;

        try {
            jsonObject = TemplateManagerHelper.fileToJson(file);
        } catch (TemplateManagerHelperException e) {
            Assert.fail("Failed to convert a valid json file. Hence failing the test");
        }

        TemplateGroup templateGroup = TemplateManagerHelper.jsonToTemplateGroup(jsonObject);
        boolean isCaught = false;

        try {
            TemplateManagerHelper.validateTemplateGroup(templateGroup);
        } catch (RuleTemplateScriptException e) {
            Assert.fail("Failed to process a valid script. Hence failing the test");
        } catch (TemplateManagerHelperException e) {
            isCaught = true;
        }

        Assert.assertEquals(isCaught, true);
    }

    @Test
    public void validatePropertyTemplatedElementsTest1() {
        log.info("BusinessRulesManager Test : validating with replacement values provided in properties & script");
        String dir = BusinessRulesManagerTestcase.class.getClassLoader().getResource
                ("property_templated_elements/valid")
                .getFile();
        int count = 0;
        try {
            count = TestUtil.ValidateBusinessRules(dir);
        } catch (RuleTemplateScriptException|TemplateManagerHelperException e) {
            Assert.fail("Invalid template group found. Hence failing the test.");
        }
        int numberOfValidTemplateGroups = new File(dir).listFiles().length;
        Assert.assertEquals(numberOfValidTemplateGroups, count);
    }

    @Test
    public void validatePropertyTemplatedElementsTest2() {
        log.info("BusinessRulesManager Test : validating with no replacement value provided");
        String dir = BusinessRulesManagerTestcase.class.getClassLoader().getResource
                ("property_templated_elements/invalid").getFile();
        File file = new File(String.format("%s/%s", dir, "element_not_found.json"));

        JsonObject jsonObject = null;

        try {
            jsonObject = TemplateManagerHelper.fileToJson(file);
        } catch (TemplateManagerHelperException e) {
            Assert.fail("Failed to convert a valid json file. Hence failing the test");
        }

        TemplateGroup templateGroup = TemplateManagerHelper.jsonToTemplateGroup(jsonObject);
        boolean isCaught = false;

        try {
            TemplateManagerHelper.validateTemplateGroup(templateGroup);
        } catch (RuleTemplateScriptException e) {
            Assert.fail("Failed to process a valid script. Hence failing the test");
        } catch (TemplateManagerHelperException e) {
            isCaught = true;
        }

        Assert.assertEquals(isCaught, true);
    }

    @Test
    public void replaceRegexTest1() {
        log.info("BusinessRulesManager Test : validating with replacements provided");
        String templatedString = "'${replace1} Business ${replace2}' ${replace3} version ${replace4}";
        Map<String,String> replacements = new HashMap<String, String>();
        replacements.put("replace1","WSO2");
        replacements.put("replace2","Rules");
        replacements.put("replace3","Manager");
        replacements.put("replace4","1");

        String replacedString = "";
        try {
            replacedString = TemplateManagerHelper.replaceRegex(
                    templatedString, TemplateManagerConstants.TEMPLATED_ELEMENT_NAME_REGEX_PATTERN, replacements);
        } catch (TemplateManagerHelperException e) {
            Assert.fail("Failed to process when values were provided. Hence failing the test");
        }

        Assert.assertEquals(replacedString, "'WSO2 Business Rules' Manager version 1");
    }

    @Test
    public void replaceRegexTest2() {
        log.info("BusinessRulesManager Test : validating with no replacements provided");
        String templatedString = "'${replace1} Business ${replace2}' ${replace3} version ${replace4}";
        Map<String,String> replacements = new HashMap<String, String>();
        replacements.put("replace1","WSO2");
        replacements.put("replace2","Rules");
        replacements.put("replace3","Manager");

        String replacedString = "";
        boolean isCaught = false;
        try {
            replacedString = TemplateManagerHelper.replaceRegex(
                    templatedString, TemplateManagerConstants.TEMPLATED_ELEMENT_NAME_REGEX_PATTERN, replacements);
        } catch (TemplateManagerHelperException e) {
            isCaught = true;
        }

        Assert.assertEquals(isCaught, true);
    }

    @Test
    public void getScriptGeneratedVariablesTest() {
        log.info("BusinessRulesManager Test : validating script generated variables");
        String script =
                "function function1(){return '1'}" +
                        "/*function function2(){return '2'}*/" +
                        "/*var variable1 = 'variable1';*/" +
                        "var variable2 = 'variable2';" +
                        "var variable3 = 3;" +
                        "// var variable4 = 'variable4';";
        Map<String,String> expectedValues = new HashMap<String,String>();
        expectedValues.put("function1", "function function1(){return '1'}");
        expectedValues.put("variable2", "variable2");
        expectedValues.put("variable3", "3");

        Map<String,String> receivedValues = null;
        try {
            receivedValues = TemplateManagerHelper.getScriptGeneratedVariables(script);
        } catch (RuleTemplateScriptException e) {
            log.info(e.getMessage(),e);
            Assert.fail("Failed to process with a valid script. Hence failing the test");
        }

        for(String variable : receivedValues.keySet()){
            if(expectedValues.containsKey(variable)){
                if(expectedValues.get(variable).equals(receivedValues.get(variable))) {
                    expectedValues.remove(variable);
                }
            }else{
                Assert.fail("Unexpected variable present. Hence failing the test");
            }
        }

        Assert.assertEquals(expectedValues.size(), 0);
    }

    // TemplateManagerService class related

}
