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
package org.wso2.carbon.business.rules.core.api.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.business.rules.core.api.ApiResponseMessage;
import org.wso2.carbon.business.rules.core.api.BusinessRulesApiService;
import org.wso2.carbon.business.rules.core.api.NotFoundException;
import org.wso2.carbon.business.rules.core.bean.BusinessRule;
import org.wso2.carbon.business.rules.core.bean.RuleTemplate;
import org.wso2.carbon.business.rules.core.bean.TemplateGroup;
import org.wso2.carbon.business.rules.core.bean.TemplateManagerInstance;
import org.wso2.carbon.business.rules.core.bean.scratch.BusinessRuleFromScratch;
import org.wso2.carbon.business.rules.core.bean.template.BusinessRuleFromTemplate;
import org.wso2.carbon.business.rules.core.exceptions.BusinessRuleDeploymentException;
import org.wso2.carbon.business.rules.core.exceptions.TemplateManagerServiceException;
import org.wso2.carbon.business.rules.core.services.TemplateManagerService;
import org.wso2.carbon.business.rules.core.util.TemplateManagerConstants;
import org.wso2.carbon.business.rules.core.util.TemplateManagerHelper;

import javax.ws.rs.core.Response;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-10-13T06:19:32.032Z")
public class BusinessRulesApiServiceImpl extends BusinessRulesApiService {
    private static final Logger log = LoggerFactory.getLogger(BusinessRulesApiServiceImpl.class);

    @Override
    public Response createBusinessRule(String businessRule, Boolean shouldDeploy
    ) throws NotFoundException {
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();

        // convert the string received from API, as a json object
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        JsonObject businessRuleJson = gson.fromJson(businessRule, JsonObject.class);
        int status;
        // Check the business rule type of the json object
        if (businessRuleJson.get("type").toString().equals("\"" + TemplateManagerConstants
                .BUSINESS_RULE_TYPE_TEMPLATE + "\"")) {
            // Convert to business rule from template and create
            BusinessRuleFromTemplate businessRuleFromTemplate = TemplateManagerHelper
                    .jsonToBusinessRuleFromTemplate(businessRule);

            try {
                status = templateManagerService.createBusinessRuleFromTemplate(businessRuleFromTemplate, shouldDeploy);
            } catch (TemplateManagerServiceException e) {
                log.error("Failed to create business rule '" + businessRuleFromTemplate.getName() + "' due to " +
                        e.getMessage(), e);
                return Response.serverError().build();
            }
        } else {
            BusinessRuleFromScratch businessRuleFromScratch = TemplateManagerHelper.jsonToBusinessRuleFromScratch
                    (businessRule);

            try {
                status = templateManagerService.createBusinessRuleFromScratch(businessRuleFromScratch, shouldDeploy);
            } catch (TemplateManagerServiceException e) {
                log.error("Failed to create business rule '" + businessRuleFromScratch.getName() + "' due to " +
                        e.getMessage(), e);
                return Response.serverError().build();
            }
        }
        return Response.ok().status(status).build();
    }

    @Override
    public Response deleteBusinessRule(String businessRuleInstanceID, Boolean forceDelete)
            throws NotFoundException {
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        boolean deleted = false;
        try {
            deleted = templateManagerService.deleteBusinessRule(businessRuleInstanceID, forceDelete);
        } catch (BusinessRuleDeploymentException e) {
            log.error("Failed to delete the business rule '" + businessRuleInstanceID + "' due to " +
                    e.getMessage(), e);
            return Response.serverError().build();
        }
        if (deleted) {
            return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK,
                    "Business Rule deleted " +
                            "successfully!")).build();
        } else {
            return Response.serverError().build();
        }
    }

    @Override
    public Response getBusinessRules() throws NotFoundException {
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        try {
            templateManagerService.updateStatuses();
            Map<String, BusinessRule> businessRuleMap = templateManagerService.loadBusinessRules();
            if (businessRuleMap == null) {
                log.error("No available business rules found.");
                return Response.serverError().build();
            }
            List list = templateManagerService.loadBusinessRulesWithStatus();
            Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
            return Response.ok().entity(gson.toJson(list)).build();
        } catch (TemplateManagerServiceException e) {
            log.error("Failed to retrieve business rules from the database due to " + e.getMessage(), e);
            return Response.serverError().build();
        }
    }

    @Override
    public Response getRuleTemplate(String templateGroupID, String ruleTemplateID) throws NotFoundException {
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        try {
            RuleTemplate ruleTemplate = templateManagerService.getRuleTemplate(templateGroupID, ruleTemplateID);
            return Response.ok().entity(ruleTemplate).build();
        } catch (TemplateManagerServiceException e) {
            log.error("Failed to load ruleTemplate with the uuid '" + ruleTemplateID + "' in the templateGroup '" +
                    templateGroupID + "' due to " + e.getMessage(), e);
            return Response.serverError().build();
        }
    }

    @Override
    public Response getRuleTemplates(String templateGroupID) throws NotFoundException {
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        try {
            // Get rule templates and store without UUIDs
            Map<String, RuleTemplate> ruleTemplates = templateManagerService.getRuleTemplates(templateGroupID);
            ArrayList<RuleTemplate> ruleTemplatesWithoutUUID = new ArrayList();
            for (Map.Entry ruleTemplate : ruleTemplates.entrySet()) {
                ruleTemplatesWithoutUUID.add((RuleTemplate) ruleTemplate.getValue());
            }
            Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

            return Response.ok().entity(gson.toJson(ruleTemplatesWithoutUUID)).build();
        } catch (TemplateManagerServiceException e) {
            log.error("Failed to load ruleTemplates of the templateGroup " + templateGroupID + " due to " +
                    e.getMessage(), e);
            return Response.serverError().build();
        }
    }

    @Override
    public Response getTemplateGroup(String templateGroupID
    ) throws NotFoundException {
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        try {
            TemplateGroup templateGroup = templateManagerService.getTemplateGroup(templateGroupID);
            return Response.ok().entity(templateManagerService.getTemplateGroup(templateGroupID)).build();
        } catch (TemplateManagerServiceException e) {
            log.error("Failed to load templateGroup with the uuid '" + templateGroupID + "' due to " +
                    e.getMessage(), e);
            return Response.serverError().build();
        }
    }

    @Override
    public Response getTemplateGroups() throws NotFoundException {
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        // Get template groups and store without UUIDs
        Map<String, TemplateGroup> templateGroups = templateManagerService.getTemplateGroups();
        ArrayList<TemplateGroup> templateGroupsArrayList = new ArrayList();
        for (Map.Entry templateGroupUUID : templateGroups.entrySet()) {
            templateGroupsArrayList.add((TemplateGroup) templateGroupUUID.getValue());
        }
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        return Response.ok().entity(gson.toJson(templateGroupsArrayList)).build();
    }

    @Override
    public Response loadBusinessRule(String businessRuleInstanceID
    ) throws NotFoundException {
        try {
            TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
            BusinessRule businessRule = templateManagerService.loadBusinessRule(businessRuleInstanceID);
            Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
            return Response.ok().entity(gson.toJson(businessRule)).build();
        } catch (TemplateManagerServiceException e) {
            log.error("Failed to load business rule with uuid '" + businessRuleInstanceID + " due to " +
                    e.getMessage(), e);
            return Response.serverError().build();
        }
    }

    @Override
    public Response redeployBusinessRule(String businessRuleInstanceID
    ) throws NotFoundException {
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        int status;
        try {
            status = templateManagerService.redeployBusinessRule(businessRuleInstanceID);
            return Response.ok().status(status).build();
        } catch (TemplateManagerServiceException e) {
            log.error("Failed to re-deploy the business rule with uuid '" + businessRuleInstanceID + " due to " +
                    e.getMessage(), e);
            return Response.serverError().build();
        }
    }


    public Response updateBusinessRule(String businessRule,
                                       String businessRuleInstanceID, Boolean deploy
    ) throws NotFoundException {
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        JsonObject businessRuleJson = gson.fromJson(businessRule, JsonObject.class);
        int status;

        try {
            if (businessRuleJson.get("type").toString().equals("\"" + TemplateManagerConstants.BUSINESS_RULE_TYPE_TEMPLATE
                    + "\"")) {
                BusinessRuleFromTemplate businessRuleFromTemplate = TemplateManagerHelper
                        .jsonToBusinessRuleFromTemplate(businessRule);
                status = templateManagerService.editBusinessRuleFromTemplate(businessRuleInstanceID,
                        businessRuleFromTemplate, deploy);

            } else {
                BusinessRuleFromScratch businessRuleFromScratch = TemplateManagerHelper.jsonToBusinessRuleFromScratch
                        (businessRule);

                status = templateManagerService.editBusinessRuleFromScratch(businessRuleInstanceID,
                        businessRuleFromScratch, deploy);
            }
            return Response.ok().status(status).build();
        } catch (TemplateManagerServiceException e) {
            log.error("Failed to update the business rule with uuid '" + businessRuleInstanceID + "' due to " +
                    e.getMessage(), e);
            return Response.serverError().build();
        }
    }
}
