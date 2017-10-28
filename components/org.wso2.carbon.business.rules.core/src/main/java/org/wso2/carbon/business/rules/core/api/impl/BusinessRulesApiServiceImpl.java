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
import org.wso2.carbon.business.rules.core.exceptions.BusinessRuleNotFoundException;
import org.wso2.carbon.business.rules.core.exceptions.RuleTemplateScriptException;
import org.wso2.carbon.business.rules.core.exceptions.SiddhiAppsApiHelperException;
import org.wso2.carbon.business.rules.core.exceptions.TemplateManagerServiceException;
import org.wso2.carbon.business.rules.core.services.TemplateManagerService;
import org.wso2.carbon.business.rules.core.util.TemplateManagerConstants;
import org.wso2.carbon.business.rules.core.util.TemplateManagerHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

/**
 * API implementation
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-10-13T06:19:32.032Z")
public class BusinessRulesApiServiceImpl extends BusinessRulesApiService {
    private static final Logger log = LoggerFactory.getLogger(BusinessRulesApiServiceImpl.class);

    // TODO: 24/10/17 Send a custom response message with custom error codes
    @Override
    public Response createBusinessRule(String businessRule, Boolean shouldDeploy
    ) throws NotFoundException {
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        // TODO: Check deployment failed stuff
        // convert the string received from API, as a json object
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        JsonObject businessRuleJson = gson.fromJson(businessRule, JsonObject.class);
        int status;
        List<Object> responseData = new ArrayList<Object>();
        String businessRuleName;
        // Check the business rule type of the json object
        if (businessRuleJson.get("type").toString().equals("\"" + TemplateManagerConstants
                .BUSINESS_RULE_TYPE_TEMPLATE + "\"")) {
            // Convert to business rule from template and create
            BusinessRuleFromTemplate businessRuleFromTemplate = TemplateManagerHelper
                    .jsonToBusinessRuleFromTemplate(businessRule);

            businessRuleName = businessRuleFromTemplate.getName();

            try {
                status = templateManagerService.createBusinessRuleFromTemplate(businessRuleFromTemplate, shouldDeploy);
            } catch (TemplateManagerServiceException e) {
                log.error("Failed to create business rule '" + businessRuleName + "' due to " +
                        e.getMessage(), e);

                responseData.add("Failure Occured");
                responseData.add("Failed to create business rule '"+businessRuleName+"'");
                responseData.add(TemplateManagerConstants.ERROR);

                return Response.serverError().entity(gson.toJson(responseData)).build();
            } catch (RuleTemplateScriptException e) {
                log.error("Failed to create business rule '" + businessRuleName + "' due to " +
                        e.getMessage(), e);

                responseData.add("Error while processing the script");
                responseData.add("Please re-check the entered values, or the script provided by the administrator");
                responseData.add(TemplateManagerConstants.ERROR);

                return Response.serverError().entity(gson.toJson(responseData)).build();
            }
        } else {
            BusinessRuleFromScratch businessRuleFromScratch = TemplateManagerHelper.jsonToBusinessRuleFromScratch
                    (businessRule);
            businessRuleName = businessRuleFromScratch.getName();

            try {
                status = templateManagerService.createBusinessRuleFromScratch(businessRuleFromScratch, shouldDeploy);
            } catch (TemplateManagerServiceException e) {
                log.error("Failed to create business rule '" + businessRuleName + "' due to " +
                        e.getMessage(), e);

                responseData.add("Failure Occured");
                responseData.add("Failed to create business rule '"+businessRuleName+"'");
                responseData.add(TemplateManagerConstants.ERROR);
                return Response.serverError().entity(gson.toJson(responseData)).build(); //todo:common catch for if&else
            } catch (RuleTemplateScriptException e) {
                log.error("Failed to create business rule '" + businessRuleName + "' due to " +
                        e.getMessage(), e);
                responseData.add("Error while processing the script");
                responseData.add("Please re-check the entered values, or the script provided by the administrator");
                responseData.add(TemplateManagerConstants.ERROR);
                return Response.serverError().entity(gson.toJson(responseData)).build();
            }
        }
//        return Response.ok().status(status).build();
        switch(status){
            case(0):
                responseData.add("Deployment Successful");
                responseData.add("Successfully deployed the business rule");
                break;
            case(1):
                responseData.add("Saving Successful");
                responseData.add("Successfully saved the business rule");
                break;
            case(2):
                responseData.add("Partially Deployed");
                responseData.add("Partially deployed the business rule");
                break;
            case(4):
                responseData.add("Deployment Failure");
                responseData.add("Failed to deploy the business rule");
                break;
            default:
                responseData.add("Unable to Save");
                responseData.add("Failed to save the business rule");
        }
        responseData.add(status);

        return Response.ok().entity(gson.toJson(responseData)).build();
    }

    @Override
    public Response deleteBusinessRule(String businessRuleInstanceID, Boolean forceDelete)
            throws NotFoundException {
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        List<Object> responseData = new ArrayList<Object>();
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        try {
            int status = templateManagerService.deleteBusinessRule(businessRuleInstanceID, forceDelete);
            switch(status){
                case(3):
                    responseData.add("Partially Undeployed");
                    responseData.add("Partially undeployed the business rule");
                    break;
                case(6):
                    responseData.add("Deletion Successful");
                    responseData.add("Successfully deleted the business rule");
                default:
                    responseData.add("Unable to Delete");
                    responseData.add("Unable to delete the business rule");
            }
            responseData.add(status);
            return Response.ok().entity(gson.toJson(responseData)).build();
        } catch (BusinessRuleNotFoundException e) { // TODO: 25/10/17 LOG!
//            return Response.status(Response.Status.NOT_FOUND).build();
            responseData.add("Business Rule Not Found");
            responseData.add("Could not find business rule with uuid '" + businessRuleInstanceID + "'");
            responseData.add(5);
            return Response.status(Response.Status.NOT_FOUND).entity(gson.toJson(responseData)).build();
        } catch (TemplateManagerServiceException e) {
//            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            responseData.add("Internal Server Error");
            responseData.add("There was an error connecting to the server");
            responseData.add(5);
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(gson.toJson(responseData)).build();
        }
    }

    @Override
    public Response getBusinessRules() throws NotFoundException {
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        List<Object> responseData = new ArrayList<Object>();
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        try {
            //templateManagerService.updateStatuses();
            Map<String, BusinessRule> businessRuleMap = templateManagerService.loadBusinessRules();
            if (businessRuleMap == null) {
                log.error("No available business rules found.");
                // return Response.serverError().build();
                responseData.add("Unable to find Business Rules");
                responseData.add("Could not find any business rule");
                responseData.add(null);
                return Response.status(Response.Status.NOT_FOUND).entity(gson.toJson(responseData)).build();
            }
            List list = templateManagerService.loadBusinessRulesWithStatus();

            // return Response.ok().entity(gson.toJson(list)).build();
            responseData.add("Found Business Rules");
            responseData.add("Loaded available business rules");
            responseData.add(list);
            return Response.ok().entity(gson.toJson(responseData)).build();
        } catch (TemplateManagerServiceException e) {
            log.error("Failed to retrieve business rules from the database due to " + e.getMessage(), e);
            // return Response.serverError().build();
            responseData.add("Failed to Retrieve");
            responseData.add("Failed to retrieve business rules from the database");
            responseData.add(null);
            return Response.serverError().entity(gson.toJson(responseData)).build();
        }
    }

    @Override
    public Response getRuleTemplate(String templateGroupID, String ruleTemplateID) throws NotFoundException {
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        List<Object> responseData = new ArrayList<Object>();
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        try {
            RuleTemplate ruleTemplate = templateManagerService.getRuleTemplate(templateGroupID, ruleTemplateID);
            // return Response.ok().entity(ruleTemplate).build();
            responseData.add("Found Rule Template");
            responseData.add("Loaded rule template with uuid '" + ruleTemplateID + "'");
            responseData.add(ruleTemplate);
            return Response.ok().entity(gson.toJson(responseData)).build();
        } catch (TemplateManagerServiceException e) {
            log.error("Failed to load ruleTemplate with the uuid '" + ruleTemplateID + "' in the templateGroup '" +
                    templateGroupID + "' due to " + e.getMessage(), e);
            // return Response.serverError().build();
            responseData.add("Failed to load");
            responseData.add("Failed to load rule template with uuid '" + ruleTemplateID + "'");
            responseData.add(null);
            return Response.serverError().entity(gson.toJson(responseData)).build();
        }
    }

    @Override
    public Response getRuleTemplates(String templateGroupID) throws NotFoundException {
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        List<Object> responseData = new ArrayList<Object>();
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        try {
            // Get rule templates and store without UUIDs
            Map<String, RuleTemplate> ruleTemplates = templateManagerService.getRuleTemplates(templateGroupID);
            ArrayList<RuleTemplate> ruleTemplatesWithoutUUID = new ArrayList();
            for (Map.Entry ruleTemplate : ruleTemplates.entrySet()) {
                ruleTemplatesWithoutUUID.add((RuleTemplate) ruleTemplate.getValue());
            }

            // return Response.ok().entity(gson.toJson(ruleTemplatesWithoutUUID)).build();
            responseData.add("Found Rule Templates");
            responseData.add("Loaded available rule templates for template group with uuid '" + templateGroupID + "'");
            responseData.add(ruleTemplatesWithoutUUID);
            return Response.ok().entity(gson.toJson(responseData)).build();
        } catch (TemplateManagerServiceException e) {
            log.error("Failed to load ruleTemplates of the templateGroup " + templateGroupID + " due to " +
                    e.getMessage(), e);
            // return Response.serverError().build();
            responseData.add("Failed to load");
            responseData.add("Failed to load rule templates of the template group with uuid '" + templateGroupID + "'");
            responseData.add(null);
            return Response.serverError().entity(gson.toJson(responseData)).build();
        }
    }

    @Override
    public Response getTemplateGroup(String templateGroupID
    ) throws NotFoundException {
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        List<Object> responseData = new ArrayList<Object>();
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        try {
            TemplateGroup templateGroup = templateManagerService.getTemplateGroup(templateGroupID);
            // return Response.ok().entity(templateManagerService.getTemplateGroup(templateGroupID)).build();
            responseData.add("Found Template Group");
            responseData.add("Loaded template group with uuid '" + templateGroupID + "'");
            responseData.add(templateGroup);
            return Response.ok().entity(gson.toJson(responseData)).build();
        } catch (TemplateManagerServiceException e) {
            log.error("Failed to load templateGroup with the uuid '" + templateGroupID + "' due to " +
                    e.getMessage(), e);
            // return Response.serverError().build();
            responseData.add("Failed to load");
            responseData.add("Failed to load template group with uuid '" + templateGroupID + "'");
            responseData.add(null);
            return Response.serverError().entity(gson.toJson(responseData)).build();
        }
    }

    @Override
    public Response getTemplateGroups() throws NotFoundException {
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        List<Object> responseData = new ArrayList<Object>();
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

        try {
            // Get template groups and store without UUIDs
            Map<String, TemplateGroup> templateGroups = templateManagerService.getTemplateGroups();
            ArrayList<TemplateGroup> templateGroupsWithoutUUIDs = new ArrayList();
            for (Map.Entry templateGroupUUID : templateGroups.entrySet()) {
                templateGroupsWithoutUUIDs.add((TemplateGroup) templateGroupUUID.getValue());
            }
            // return Response.ok().entity(gson.toJson(templateGroupsArrayList)).build();
            responseData.add("Found Template Groups");
            responseData.add("Loaded available template groups");
            responseData.add(templateGroupsWithoutUUIDs);
            return Response.ok().entity(gson.toJson(responseData)).build();
        } catch(TemplateManagerServiceException e) {
            log.error("Failed to load available template groups due to '" + e.getMessage(), e);
            // return Response.serverError().build();
            responseData.add("Failed to load");
            responseData.add("Failed to load available template groups");
            responseData.add(null);
            return Response.serverError().entity(gson.toJson(responseData)).build();
        }

    }

    @Override
    public Response loadBusinessRule(String businessRuleInstanceID
    ) throws NotFoundException {
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        List<Object> responseData = new ArrayList<Object>();
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        try {
            BusinessRule businessRule = templateManagerService.loadBusinessRule(businessRuleInstanceID);
            // return Response.ok().entity(gson.toJson(businessRule)).build();
            responseData.add("Found Business Rule");
            responseData.add("Loaded business rule with uuid '" + businessRuleInstanceID + "'");
            responseData.add(businessRule);
            return Response.ok().entity(gson.toJson(responseData)).build();
        } catch (TemplateManagerServiceException e) {
            log.error("Failed to load business rule with uuid '" + businessRuleInstanceID + " due to " +
                    e.getMessage(), e);
            // return Response.serverError().build();
            responseData.add("Failed to load");
            responseData.add("Failed to load business rule with uuid '" + businessRuleInstanceID + "'");
            responseData.add(null);
            return Response.serverError().entity(gson.toJson(responseData)).build();
        }
    }

    @Override
    public Response redeployBusinessRule(String businessRuleInstanceID
    ) throws NotFoundException {
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        List<Object> responseData = new ArrayList<Object>();
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        int status;
        try {
            status = templateManagerService.redeployBusinessRule(businessRuleInstanceID);
            // return Response.ok().status(status).build();
            switch(status){
                case(0):
                    responseData.add("Deployment Successful");
                    responseData.add("Successfully deployed the business rule");
                    break;
                case(2):
                    responseData.add("Partially Deployed");
                    responseData.add("Partially deployed the business rule");
                    break;
                case(4):
                    responseData.add("Deployment Failure");
                    responseData.add("Failed to deploy the business rule");
                    break;
                default:
                    responseData.add("Deployment Error");
                    responseData.add("Failed to deploy the business rule");
            }
            responseData.add(status);

            return Response.ok().entity(gson.toJson(responseData)).build();
        } catch (TemplateManagerServiceException e) {
            log.error("Failed to re-deploy the business rule with uuid '" + businessRuleInstanceID + " due to " +
                    e.getMessage(), e);
            // return Response.serverError().build();
            responseData.add("Re-deployment failure");
            responseData.add("Failed to re-deploy the business rule with uuid '" + businessRuleInstanceID + "'");
            responseData.add(null);
            return Response.ok().entity(gson.toJson(responseData)).build();
        } catch (RuleTemplateScriptException e) {
            responseData.add("Error while processing the script");
            responseData.add("Please re-check the entered values, or the script provided by the administrator");
            responseData.add(TemplateManagerConstants.ERROR);
            return Response.serverError().entity(gson.toJson(responseData)).build();
        }
    }


    public Response updateBusinessRule(Object businessRule,
                                       String businessRuleInstanceID, Boolean deploy
    ) throws NotFoundException {
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        List<Object> responseData = new ArrayList<Object>();
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        String businessRuleDefinition = gson.toJson(businessRule);
        JsonObject businessRuleJson = gson.fromJson(businessRuleDefinition, JsonObject.class);
        String businessRuleName;
        int status;

        try {
            if (businessRuleJson.get("type").toString().equals("\"" + TemplateManagerConstants.BUSINESS_RULE_TYPE_TEMPLATE
                    + "\"")) {
                BusinessRuleFromTemplate businessRuleFromTemplate = TemplateManagerHelper
                        .jsonToBusinessRuleFromTemplate(businessRuleDefinition);
                status = templateManagerService.editBusinessRuleFromTemplate(businessRuleInstanceID,
                        businessRuleFromTemplate, deploy);
                businessRuleName = businessRuleFromTemplate.getName();

            } else {
                BusinessRuleFromScratch businessRuleFromScratch = TemplateManagerHelper.jsonToBusinessRuleFromScratch
                        (businessRuleDefinition);
                businessRuleName = businessRuleFromScratch.getName();

                status = templateManagerService.editBusinessRuleFromScratch(businessRuleInstanceID,
                        businessRuleFromScratch, deploy);
            }
            // return Response.ok().status(status).build();
            switch(status){
                case(0):
                    responseData.add("Deployment Successful");
                    responseData.add("Successfully deployed the business rule");
                    break;
                case(1):
                    responseData.add("Saving Successful");
                    responseData.add("Successfully updated the business rule");
                    break;
                case(2):
                    responseData.add("Partially Deployed");
                    responseData.add("Partially deployed the business rule");
                    break;
                case(4):
                    responseData.add("Deployment Failure");
                    responseData.add("Failed to deploy the business rule");
                    break;
                default:
                    responseData.add("Error Saving");
                    responseData.add("Error occured while updating the business rule");
            }
            responseData.add(status);
            return Response.ok().entity(gson.toJson(responseData)).build();
        } catch (TemplateManagerServiceException e) {
            log.error("Failed to update the business rule with uuid '" + businessRuleInstanceID + "' due to " +
                    e.getMessage(), e);
            // return Response.serverError().build();
            responseData.add("Failed to update");
            responseData.add("Failed to update the business rule with uuid '"+ businessRuleInstanceID + "'");
            responseData.add(null);
            return Response.serverError().entity(gson.toJson(responseData)).build();
        } catch (RuleTemplateScriptException e) {
            responseData.add("Error while processing the script");
            responseData.add("Please re-check the entered values, or the script provided by the administrator");
            responseData.add(TemplateManagerConstants.ERROR);
            return Response.serverError().entity(gson.toJson(responseData)).build();
        }
    }
}
