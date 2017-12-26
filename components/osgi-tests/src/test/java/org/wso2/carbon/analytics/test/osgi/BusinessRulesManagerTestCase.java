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
import org.wso2.carbon.container.options.CarbonDistributionOption;
import org.wso2.carbon.database.query.manager.exception.QueryMappingNotAvailableException;
import org.wso2.carbon.kernel.CarbonServerInfo;
import org.wso2.msf4j.MicroservicesRegistry;

import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Map;

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
    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(BusinessRulesManagerTestCase.class);

    private static final String DEFAULT_USER_NAME = "admin";
    private static final String DEFAULT_PASSWORD = "admin";
    private URI baseURI = URI.create(String.format("https://%s:%d", "localhost", 9643));

    @Inject
    private CarbonServerInfo carbonServerInfo;

    @Inject
    private MicroservicesRegistry microservicesRegistry;


    private Option copyImportingFileOption() {
        Path carbonYmlFilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources",
                "editor", "samples", "ReceiveAndCount.siddhi");
        return copyFile(carbonYmlFilePath, Paths.get("wso2", "editor", "deployment", "ReceiveAndCount.siddhi"));
    }

    private Option copySampleFileOption() {
        Path carbonYmlFilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources",
                "editor", "samples", "ReceiveAndCount.siddhi");
        return copyFile(carbonYmlFilePath, Paths.get("samples"));
    }

    private Option copySiddhiAppFileOption() {
        Path carbonYmlFilePath;
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = Paths.get(".").toString();
        }
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources",
                "editor", "siddhi-apps", "TestSiddhiApp.siddhi");
        return copyFile(carbonYmlFilePath, Paths.get("wso2", "editor", "deployment", "workspace",
                "TestSiddhiApp.siddhi"));
    }

    private Option copyPermissionDB() {
        String basedir = System.getProperty("basedir");
        Path carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources", "carbon-context", "carbon.yml");
        carbonYmlFilePath = Paths.get(basedir, "src", "test", "resources",
                "database", "PERMISSION_DB.h2.db");
        return copyFile(carbonYmlFilePath, Paths.get("wso2", "dashboard", "database", "PERMISSION_DB.h2.db"));
    }

    @Configuration
    public Option[] createConfiguration() {
        logger.info("Running - "+ this.getClass().getName());
        return new Option[]{
                //copySiddhiAppFileOption(),
                //copySampleFileOption(),
                //copyImportingFileOption(),
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

                carbonDistribution(Paths.get("target", "wso2das-" +
                        System.getProperty("carbon.analytic.version")), "dashboard")/*,
                CarbonDistributionOption.debug(5005)*/
                };
    }

    //@Test
    public void testLoadingBusinessRulesWhenThereIsNone() throws Exception {
        String path = "/business-rules/instances/";
        String contentType = "text/plain";
        String method = "GET";

        logger.info("List exist business rules.");
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

    //@Test
    public void testLoadingTemplateGroups() throws Exception {
        String path = "/business-rules/template-groups/";
        String contentType = "text/plain";
        String method = "GET";

        logger.info("Validating a siddhi app.");
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

    //@Test
    public void testLoadASelectedTemplateGroup() throws Exception {
        String path = "/business-rules/template-groups/stock-exchange";
        String contentType = "text/plain";
        String method = "GET";

        logger.info("Validating a siddhi app.");
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

    //@Test
    public void testLoadingRuleTemplatesFromTemplateGroup() throws Exception {
        String path = "/business-rules/template-groups/stock-exchange/templates";
        String contentType = "text/plain";
        String method = "GET";

        logger.info("Validating a siddhi app.");
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

    //@Test
    public void testLoadingSelectedRuleTemplateFromTemplateGroup() throws Exception {
        String path = "/business-rules/template-groups/stock-exchange/templates/stock-exchange-input";
        String contentType = "text/plain";
        String method = "GET";

        logger.info("Validating a siddhi app.");
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

    @Test
    public void testCreatingBusinessRuleFromTemplate() throws Exception {
        logger.info("Creating a business rule from template..");
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
        String path = "/business-rules/instances/";
        String contentType = "text/plain";
        String method = "GET";

        logger.info("List exist business rules.");
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

    private HTTPResponseMessage sendHRequest(String body, URI baseURI, String path, String contentType,
                                             String methodType, Boolean auth, String userName, String password) {
        TestUtil testUtil = new TestUtil(baseURI, path, auth, false, methodType,
                contentType, userName, password);
        testUtil.addBodyContent(body);
        return testUtil.getResponse();
    }
}
