/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.analytics.test.osgi;

import static org.ops4j.pax.exam.CoreOptions.maven;
import static org.wso2.carbon.container.options.CarbonDistributionOption.carbonDistribution;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyFile;
import static org.wso2.carbon.container.options.CarbonDistributionOption.copyOSGiLibBundle;

import org.apache.log4j.Logger;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.ExamFactory;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.exam.testng.listener.PaxExam;
import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.test.osgi.util.HTTPResponseMessage;
import org.wso2.carbon.analytics.test.osgi.util.TestUtil;
import org.wso2.carbon.container.CarbonContainerFactory;
import org.wso2.carbon.kernel.CarbonServerInfo;
import org.wso2.msf4j.MicroservicesRegistry;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.inject.Inject;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

/**
 * OSGI Tests for Business Rules Manager.
 */

@Listeners(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
@ExamFactory(CarbonContainerFactory.class)
public class BusinessRulesManagerTestCase {
    private static final Logger logger = Logger.getLogger(BusinessRulesManagerTestCase.class);

    private static final String DEFAULT_USER_NAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin";
    private URI baseURI = URI.create(String.format("https://%s:%d", "localhost", 9743));

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Inject
    private MicroservicesRegistry microservicesRegistry;

    private Option copyPermissionDB() {
        String basedir = System.getProperty("basedir");
        Path carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources", "carbon-context", "carbon.yml");
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources",
                "database", "PERMISSION_DB.h2.db");
        return copyFile(carbonYmlFilePath, Paths.get("wso2", "server", "database", "PERMISSION_DB.h2.db"));
    }

    @Configuration
    public Option[] createConfiguration() {
        logger.info("Running - "+ this.getClass().getName());
        return new Option[]{
                copyPermissionDB(),
                copyOSGiLibBundle(maven()
                        .artifactId("org.wso2.carbon.business.rules.core")
                        .groupId("org.wso2.carbon.analytics")
                        .versionAsInProject()),
                copyOSGiLibBundle(maven()
                        .artifactId("org.wso2.carbon.database.query.manager")
                        .groupId("org.wso2.carbon.analytics-common")
                        .versionAsInProject()),
                copyOSGiLibBundle(maven()
                        .artifactId("org.wso2.carbon.analytics.permissions")
                        .groupId("org.wso2.carbon.analytics-common")
                        .versionAsInProject()),

                carbonDistribution(Paths.get("target", "wso2-streaming-integrator-tooling-" +
                        System.getProperty("carbon.analytic.version")), "server"),
        };
    }

    @Test
    public void testLoadingBusinessRulesWhenThereIsNone() throws Exception {
        logger.info("Listing all existing business rules.");
        String path = "/business-rules/instances/";
        String contentType = "text/plain";
        String method = "GET";

        HTTPResponseMessage httpResponseMessage = sendHRequest("", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        String successContent = httpResponseMessage.getSuccessContent().toString();
        JsonArray jsonArray = new Gson().fromJson(successContent, JsonArray.class);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(jsonArray.get(0).toString(), "\"Unable to find Business Rules\"");
        Assert.assertEquals(jsonArray.get(1).toString(), "\"Could not find any business rule\"");
        Assert.assertEquals(jsonArray.get(2).toString(), "[]");
        Assert.assertEquals(jsonArray.get(3).toString(), "0");
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
    }

    @Test(dependsOnMethods = "testLoadingBusinessRulesWhenThereIsNone")
    public void testLoadingTemplateGroups() throws Exception {
        logger.info("Loading all existing template groups.");
        String path = "/business-rules/template-groups/";
        String contentType = "text/plain";
        String method = "GET";

        HTTPResponseMessage httpResponseMessage = sendHRequest("", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        String successContent = httpResponseMessage.getSuccessContent().toString();
        Gson gson = new Gson();
        JsonArray responseContent = new Gson().fromJson(successContent, JsonArray.class);
        JsonArray templates = gson.fromJson(responseContent.get(2).toString(), JsonArray.class);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        Assert.assertEquals(responseContent.get(0).toString(), "\"Found Template Groups\"");
        Assert.assertEquals(responseContent.get(1).toString(), "\"Loaded available template groups\"");
        Assert.assertEquals(templates.size(), 2);
    }

    @Test(dependsOnMethods = "testLoadingTemplateGroups")
    public void testLoadingSelectedTemplateGroup() throws Exception {
        logger.info("Loading a selected template group.");
        String path = "/business-rules/template-groups/stock-exchange";
        String contentType = "text/plain";
        String method = "GET";

        HTTPResponseMessage httpResponseMessage = sendHRequest("", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        String successContent = httpResponseMessage.getSuccessContent().toString();
        Gson gson = new Gson();
        JsonArray responseContent = gson.fromJson(successContent, JsonArray.class);
        JsonObject templateGroup = responseContent.get(2).getAsJsonObject();
        JsonArray ruleTemplates = gson.fromJson(templateGroup.get("ruleTemplates"), JsonArray.class);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        Assert.assertEquals(responseContent.size(), 3);
        Assert.assertEquals(responseContent.get(0).toString(), "\"Found Template Group\"");
        Assert.assertEquals(responseContent.get(1).toString(), "\"Loaded template group with uuid 'stock-exchange'\"");
        Assert.assertEquals(templateGroup.get("uuid").toString(), "\"stock-exchange\"");
        Assert.assertEquals(templateGroup.get("name").toString(), "\"Stock Exchange\"");
        Assert.assertEquals(templateGroup.get("description").toString(), "\"Domain for stock exchange analytics\"");
        Assert.assertEquals(ruleTemplates.size(), 2);
    }

    @Test(dependsOnMethods = "testLoadingSelectedTemplateGroup")
    public void testLoadingRuleTemplatesFromTemplateGroup() throws Exception {
        logger.info("Loading rule templates of a selected template group.");
        String path = "/business-rules/template-groups/stock-exchange/templates";
        String contentType = "text/plain";
        String method = "GET";

        HTTPResponseMessage httpResponseMessage = sendHRequest("", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        String successContent = httpResponseMessage.getSuccessContent().toString();
        Gson gson = new Gson();
        JsonArray responseContent = gson.fromJson(successContent, JsonArray.class);
        JsonArray ruleTemplates = responseContent.get(2).getAsJsonArray();
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        Assert.assertEquals(responseContent.size(), 3);
        Assert.assertEquals(responseContent.get(0).toString(), "\"Found Rule Templates\"");
        Assert.assertEquals(responseContent.get(1).toString(), "\"Loaded available rule templates for template group " +
                "with uuid 'stock-exchange'\"");
        Assert.assertEquals(ruleTemplates.size(), 2);
    }

    @Test(dependsOnMethods = "testLoadingRuleTemplatesFromTemplateGroup")
    public void testLoadingSelectedRuleTemplateFromTemplateGroup() throws Exception {
        logger.info("Loading a selected rule template from a template group.");
        String path = "/business-rules/template-groups/stock-exchange/templates/stock-exchange-input";
        String contentType = "text/plain";
        String method = "GET";

        HTTPResponseMessage httpResponseMessage = sendHRequest("", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        String successContent = httpResponseMessage.getSuccessContent().toString();
        Gson gson = new Gson();
        JsonArray responseContent = gson.fromJson(successContent, JsonArray.class);
        JsonObject ruleTemplate = responseContent.get(2).getAsJsonObject();
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        Assert.assertEquals(responseContent.size(), 3);
        Assert.assertEquals(responseContent.get(0).toString(), "\"Found Rule Template\"");
        Assert.assertEquals(responseContent.get(1).toString(), "\"Loaded rule template with uuid " +
                "'stock-exchange-input'\"");
        Assert.assertEquals(ruleTemplate.get("uuid").toString(), "\"stock-exchange-input\"");
        Assert.assertEquals(ruleTemplate.get("name").toString(), "\"Stock Exchange Input\"");
        Assert.assertEquals(ruleTemplate.get("type").toString(), "\"input\"");
        Assert.assertEquals(ruleTemplate.get("instanceCount").toString(), "\"many\"");
        Assert.assertEquals(ruleTemplate.get("script").toString(), "\"\"");
        Assert.assertEquals(ruleTemplate.get("description").toString(), "\"configured http source to receive stock " +
                "exchange updates\"");

        JsonArray templates = ruleTemplate.getAsJsonArray("templates");
        Assert.assertEquals(templates.size(), 1);

        JsonObject defaultProperties = ruleTemplate.get("properties").getAsJsonObject().get("receiverUrl")
                .getAsJsonObject();
        Assert.assertEquals(defaultProperties.get("fieldName").toString(), "\"Receiver URL\"");
        Assert.assertEquals(defaultProperties.has("description"), true);
        Assert.assertEquals(defaultProperties.get("defaultValue").toString(),
                "\"https://localhost:8005/stockInputStream\"");
    }

    @Test(dependsOnMethods = "testLoadingSelectedRuleTemplateFromTemplateGroup")
    public void testCreatingBusinessRuleFromTemplate() throws Exception {
        logger.info("Creating a business rule from template.");
        String path = "/business-rules/instances?deploy=false";
        String contentType = "multipart/form-data";
        String method = "POST";

        TestUtil testUtil = new TestUtil(baseURI, path, true, false, method,
                contentType, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        String body = "" +
                "{\"name\":\"BR1\"," +
                "\"uuid\":\"br1\"," +
                "\"type\":\"template\"," +
                "\"templateGroupUUID\":\"3432442\"," +
                "\"ruleTemplateUUID\":\"identifying-continuous-production-decrease\"," +
                "\"properties\":{" +
                "   \"timeInterval\":\"6\"," +
                "   \"timeRangeInput\":\"5\"," +
                "   \"email\":\"example@email.com\"}" +
                "}";
        testUtil.addFormField("businessRule", body);

        HTTPResponseMessage httpResponseMessage = testUtil.getResponse();
        String successContent = httpResponseMessage.getSuccessContent().toString();
        Gson gson= new Gson();
        JsonArray responseContent = gson.fromJson(successContent, JsonArray.class);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        Assert.assertEquals(responseContent.size(), 3);
        Assert.assertEquals(responseContent.get(0).toString(), "\"Saving Successful\"");
        Assert.assertEquals(responseContent.get(1).toString(), "\"Successfully saved the business rule\"");
        Assert.assertEquals(responseContent.get(2).toString(), "1");
    }

    @Test(dependsOnMethods = "testCreatingBusinessRuleFromTemplate")
    public void testLoadingBusinessRules() throws Exception {
        logger.info("Listing all existing business rules.");
        String path = "/business-rules/instances/";
        String contentType = "text/plain";
        String method = "GET";

        HTTPResponseMessage httpResponseMessage = sendHRequest("", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        String successContent = httpResponseMessage.getSuccessContent().toString();
        JsonArray responseContent = new Gson().fromJson(successContent, JsonArray.class);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        Assert.assertEquals(responseContent.size(), 4);
        Assert.assertEquals(responseContent.get(0).toString(), "\"Found Business Rules\"");
        Assert.assertEquals(responseContent.get(1).toString(), "\"Loaded available business rules\"");
        JsonArray businessRule = responseContent.get(2).getAsJsonArray();
        Assert.assertEquals(businessRule.size(), 1);
    }

    @Test(dependsOnMethods = "testLoadingBusinessRules")
    public void testCreatingBusinessRuleFromScratch() throws Exception {
        logger.info("Creating a business rule from scratch.");
        String path = "/business-rules/instances?deploy=false";
        String contentType = "multipart/form-data";
        String method = "POST";
        TestUtil testUtil = new TestUtil(baseURI, path, true, false, method,
                contentType, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        String body = "" +
                "{\"name\":\"BR2\"," +
                "\"uuid\":\"br2\"," +
                "\"type\":\"scratch\"," +
                "\"templateGroupUUID\":\"stock-exchange\"," +
                "\"inputRuleTemplateUUID\":\"stock-exchange-input\"," +
                "\"outputRuleTemplateUUID\":\"stock-exchange-output\"," +
                "\"properties\":{" +
                "   \"inputData\":{" +
                "       \"receiverUrl\":\"https://localhost:8005/stockInputStream\"}," +
                "   \"ruleComponents\":{" +
                "       \"filterRules\":[\"price >= 1000\",\"volume <= 20\"],\"ruleLogic\":[\"1 AND 2\"]}," +
                "   \"outputData\":{\"logMessage\":\"Filtered Stock data\"}," +
                "   \"outputMappings\":{" +
                "       \"companyName\":\"name\"," +
                "       \"companySymbol\":\"symbol\"," +
                "       \"sellingPrice\":\"price\"}" +
                "   }" +
                "}";
        testUtil.addFormField("businessRule", body);
        HTTPResponseMessage httpResponseMessage = testUtil.getResponse();
        String successContent = httpResponseMessage.getSuccessContent().toString();
        Gson gson= new Gson();
        JsonArray responseContent = gson.fromJson(successContent, JsonArray.class);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        Assert.assertEquals(responseContent.size(), 3);
        Assert.assertEquals(responseContent.get(0).toString(), "\"Saving Successful\"");
        Assert.assertEquals(responseContent.get(1).toString(), "\"Successfully saved the business rule\"");
        Assert.assertEquals(responseContent.get(2).toString(), "1");
    }

    @Test(dependsOnMethods = "testCreatingBusinessRuleFromScratch")
    public void testLoadingBusinessRules2() throws Exception {
        logger.info("Listing all existing business rules.");
        String path = "/business-rules/instances/";
        String contentType = "text/plain";
        String method = "GET";

        HTTPResponseMessage httpResponseMessage = sendHRequest("", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        String successContent = httpResponseMessage.getSuccessContent().toString();
        JsonArray responseContent = new Gson().fromJson(successContent, JsonArray.class);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        Assert.assertEquals(responseContent.size(), 4);
        Assert.assertEquals(responseContent.get(0).toString(), "\"Found Business Rules\"");
        Assert.assertEquals(responseContent.get(1).toString(), "\"Loaded available business rules\"");
        JsonArray businessRules = responseContent.get(2).getAsJsonArray();
        Assert.assertEquals(businessRules.size(), 2);
    }

    @Test(dependsOnMethods = "testLoadingBusinessRules2")
    public void testUpdatingBusinessRuleFromTemplate() throws Exception {
        logger.info("Updating an existing business rule created from template.");
        String path = "/business-rules/instances/br1?deploy=false";
        String contentType = "application/json";
        String method = "PUT";
        String body = "" +
                "{" +
                "   \"name\":\"BR1\", " +
                "   \"uuid\":\"br1\", " +
                "   \"type\":\"template\", " +
                "   \"templateGroupUUID\":\"3432442\", " +
                "   \"ruleTemplateUUID\":\"identifying-continuous-production-decrease\", " +
                "   \"properties\":{" +
                "       \"timeInterval\":\"100\", " +
                "       \"timeRangeInput\":\"20\", " +
                "       \"email\":\"example@email.com\", " +
                "       \"validateTimeRange\":\"function validateTimeRange(number) {" +
                "           if (!isNaN(number) && (number > 0)) {" +
                "               return number;" +
                "           } else {" +
                "               throw 'A positive number expected for time range';" +
                "           }" +
                "       }\"," +
                "       \"getUsername\" = \"function getUsername(email) {" +
                "           if (email.match(/\\\\S+@\\\\S+/g)) {" +
                "               if (email.match(/\\\\S+@\\\\S+/g)[0] === email) {" +
                "                   return email.split('@')[0];" +
                "               }" +
                "               throw 'Invalid email address provided';" +
                "           }" +
                "           throw 'Invalid email address provided';" +
                "       }\", " +
                "       \"timeRange\":\"20\", " +
                "       \"username\":\"example\"" +
                "    }" +
                "}";
        HTTPResponseMessage httpResponseMessage = sendHRequest(body, baseURI, path, contentType, method, true,
                DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        String successContent = httpResponseMessage.getSuccessContent().toString();
        Gson gson= new Gson();
        JsonArray responseContent = gson.fromJson(successContent, JsonArray.class);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        Assert.assertEquals(responseContent.size(), 3);
        Assert.assertEquals(responseContent.get(0).toString(), "\"Saving Successful\"");
        Assert.assertEquals(responseContent.get(1).toString(), "\"Successfully updated the business rule\"");
        Assert.assertEquals(responseContent.get(2).toString(), "1");
    }

    @Test(dependsOnMethods = "testUpdatingBusinessRuleFromTemplate")
    public void testUpdatingBusinessRuleFromScratch() throws Exception {
        logger.info("Updating an existing business rule created from scratch.");
        String path = "/business-rules/instances/br2?deploy=false";
        String contentType = "application/json";
        String method = "PUT";
        String body = "" +
                "{" +
                "   \"name\": \"BR2\"," +
                "   \"uuid\": \"br2\"," +
                "   \"type\": \"scratch\"," +
                "   \"templateGroupUUID\": \"stock-exchange\"," +
                "   \"inputRuleTemplateUUID\": \"stock-exchange-input\"," +
                "   \"outputRuleTemplateUUID\": \"stock-exchange-output\"," +
                "   \"properties\": {" +
                "       \"inputData\": {" +
                "           \"receiverUrl\": \"https://localhost:8005/stockInputStream\"" +
                "         }," +
                "       \"ruleComponents\": {" +
                "           \"filterRules\": [" +
                "               \"price >= 10000\"" +
                "            ]," +
                "           \"ruleLogic\": [" +
                "               \"1 \"" +
                "            ]" +
                "       }," +
                "       \"outputData\": {" +
                "           \"logMessage\": \"Filtered Stock data\"" +
                "       }," +
                "       \"outputMappings\": {" +
                "           \"companyName\": \"name\"," +
                "           \"companySymbol\": \"symbol\"," +
                "           \"sellingPrice\": \"price\"" +
                "       }" +
                "   }" +
                "}";
        HTTPResponseMessage httpResponseMessage = sendHRequest(body, baseURI, path, contentType, method, true,
                DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        String successContent = httpResponseMessage.getSuccessContent().toString();
        Gson gson= new Gson();
        JsonArray responseContent = gson.fromJson(successContent, JsonArray.class);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        Assert.assertEquals(responseContent.size(), 3);
        Assert.assertEquals(responseContent.get(0).toString(), "\"Saving Successful\"");
        Assert.assertEquals(responseContent.get(1).toString(), "\"Successfully updated the business rule\"");
        Assert.assertEquals(responseContent.get(2).toString(), "1");
    }

    //@Test(dependsOnMethods = "testUpdatingBusinessRuleFromScratch")
    public void testDeletingBusinessRule() throws Exception {
        logger.info("Deleting an existing business rule.");
        String path = "/business-rules/instances/br1";
        String contentType = "text/plain";
        String method = "DELETE";

        HTTPResponseMessage httpResponseMessage = sendHRequest("", baseURI, path, contentType, method,
                true, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        String successContent = httpResponseMessage.getSuccessContent().toString();
        Gson gson = new Gson();
        JsonArray responseContent = gson.fromJson(successContent, JsonArray.class);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 200);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        Assert.assertEquals(responseContent.size(), 3);
        Assert.assertEquals(responseContent.get(0).toString(), "\"Deletion Successful\"");
        Assert.assertEquals(responseContent.get(1).toString(), "\"Successfully deleted the business rule\"");
        Assert.assertEquals(responseContent.get(2).toString(), "6");
    }

    //@Test(dependsOnMethods = "testDeletingBusinessRule")
    public void testCreatingBusinessRuleFromInvalidTemplateGroupID() throws Exception {
        logger.info("Creating a business rule from a template group with invalid UUID.");
        String path = "/business-rules/instances?deploy=false";
        String contentType = "multipart/form-data";
        String method = "POST";
        TestUtil testUtil = new TestUtil(baseURI, path, true, false, method,
                contentType, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        String body = "" +
                "{\"name\":\"BR1\"," +
                "\"uuid\":\"br1\"," +
                "\"type\":\"template\"," +
                "\"templateGroupUUID\":\"invalidTemplateGrouUUID\"," +
                "\"ruleTemplateUUID\":\"identifying-continuous-production-decrease\"," +
                "\"properties\":{" +
                "   \"timeInterval\":\"6\"," +
                "   \"timeRangeInput\":\"5\"," +
                "   \"email\":\"example@email.com\"}" +
                "}";
        testUtil.addFormField("businessRule", body);
        HTTPResponseMessage httpResponseMessage = testUtil.getResponse();
        String errorContent = httpResponseMessage.getErrorContent().toString();
        Gson gson= new Gson();
        JsonArray responseContent = gson.fromJson(errorContent, JsonArray.class);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 500);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        Assert.assertEquals(responseContent.size(), 3);
        Assert.assertEquals(responseContent.get(0).toString(), "\"Failure Occurred\"");
        Assert.assertEquals(responseContent.get(1).toString(), "\"Failed to create business rule 'BR1'\"");
        Assert.assertEquals(responseContent.get(2).toString(), "5");
    }

    //@Test(dependsOnMethods = "testCreatingBusinessRuleFromInvalidTemplateGroupID")
    public void testCreatingBusinessRuleFromTemplateWithInvalidPropertyValues() throws Exception {
        logger.info("Creating a business rule from a template with invalid property values provided.");
        String path = "/business-rules/instances?deploy=false";
        String contentType = "multipart/form-data";
        String method = "POST";
        TestUtil testUtil = new TestUtil(baseURI, path, true, false, method,
                contentType, DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        String body = "" +
                "{\"name\":\"Invalid Br1\"," +
                "\"uuid\":\"invalid_br1\"," +
                "\"type\":\"template\"," +
                "\"templateGroupUUID\":\"3432442\"," +
                "\"ruleTemplateUUID\":\"identifying-continuous-production-decrease\"," +
                "\"properties\":{" +
                "   \"timeInterval\":\"6\"," +
                "   \"timeRangeInput\":\"5\"," +
                "   \"email\":\"invalidEmailAddress\"}" +
                "}";
        testUtil.addFormField("businessRule", body);
        HTTPResponseMessage httpResponseMessage = testUtil.getResponse();
        String errorContent = httpResponseMessage.getErrorContent().toString();
        Gson gson= new Gson();
        JsonArray responseContent = gson.fromJson(errorContent, JsonArray.class);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 500);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        Assert.assertEquals(responseContent.size(), 3);
        Assert.assertEquals(responseContent.get(0).toString(), "\"Error while executing the script\"");
        Assert.assertEquals(responseContent.get(1).toString(), "\"Invalid email address provided\"");
        Assert.assertEquals(responseContent.get(2).toString(), "7");
    }

    //@Test(dependsOnMethods = "testCreatingBusinessRuleFromTemplateWithInvalidPropertyValues")
    public void testUpdatingBusinessRuleFromTemplateWithInvalidPropertyValues() throws Exception {
        logger.info("Updating an existing business rule created from a template  by " +
                "providing invalid property values.");
        String path = "/business-rules/instances/br1?deploy=false";
        String contentType = "application/json";
        String method = "PUT";
        String body = "" +
                "{" +
                "   \"name\":\"BR1\", " +
                "   \"uuid\":\"br1\", " +
                "   \"type\":\"template\", " +
                "   \"templateGroupUUID\":\"3432442\", " +
                "   \"ruleTemplateUUID\":\"identifying-continuous-production-decrease\", " +
                "   \"properties\":{" +
                "       \"timeInterval\":\"100\", " +
                "       \"timeRangeInput\":\"20\", " +
                "       \"email\":\"invalidEmail\", " +
                "       \"validateTimeRange\":\"function validateTimeRange(number) {" +
                "           if (!isNaN(number) && (number > 0)) {" +
                "               return number;" +
                "           } else {" +
                "               throw 'A positive number expected for time range';" +
                "           }" +
                "       }\"," +
                "       \"getUsername\" = \"function getUsername(email) {" +
                "           if (email.match(/\\\\S+@\\\\S+/g)) {" +
                "               if (email.match(/\\\\S+@\\\\S+/g)[0] === email) {" +
                "                   return email.split('@')[0];" +
                "               }" +
                "               throw 'Invalid email address provided';" +
                "           }" +
                "           throw 'Invalid email address provided';" +
                "       }\", " +
                "       \"timeRange\":\"20\", " +
                "       \"username\":\"example\"" +
                "    }" +
                "}";
        HTTPResponseMessage httpResponseMessage = sendHRequest(body, baseURI, path, contentType, method, true,
                DEFAULT_USER_NAME, DEFAULT_PASSWORD);
        String errorContent = httpResponseMessage.getErrorContent().toString();
        Gson gson= new Gson();
        JsonArray responseContent = gson.fromJson(errorContent, JsonArray.class);
        Assert.assertEquals(httpResponseMessage.getResponseCode(), 500);
        Assert.assertEquals(httpResponseMessage.getContentType(), "application/json");
        Assert.assertEquals(responseContent.size(), 3);
        Assert.assertEquals(responseContent.get(0).toString(), "\"Error while executing the script\"");
        Assert.assertEquals(responseContent.get(1).toString(), "\"Invalid email address provided\"");
        Assert.assertEquals(responseContent.get(2).toString(), "7");
    }

    private HTTPResponseMessage sendHRequest(String body, URI baseURI, String path, String contentType,
                                             String methodType, Boolean auth, String userName, String password) {
        TestUtil testUtil = new TestUtil(baseURI, path, auth, false, methodType,
                contentType, userName, password);
        testUtil.addBodyContent(body);
        return testUtil.getResponse();
    }
}
