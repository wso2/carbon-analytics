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
import org.wso2.carbon.business.rules.core.exceptions.TemplateManagerException;
import org.wso2.carbon.business.rules.core.services.TemplateManagerService;
import org.wso2.carbon.business.rules.core.util.TemplateManagerConstants;
import org.wso2.carbon.business.rules.core.util.TemplateManagerHelper;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Map;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-10-13T06:19:32.032Z")
public class BusinessRulesApiServiceImpl extends BusinessRulesApiService {
    private static final Logger log = LoggerFactory.getLogger(BusinessRulesApiServiceImpl.class);

    @Override
    public Response createBusinessRule(String businessRule, Boolean deploy
    ) throws NotFoundException {
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();

        // convert the string received from API, as a json object
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        JsonObject businessRuleJson = gson.fromJson(businessRule, JsonObject.class);

        // Check the business rule type of the json object
        if (businessRuleJson.get("type").toString().equals("\""+TemplateManagerConstants
                .BUSINESS_RULE_TYPE_TEMPLATE+"\"")) {
            // Convert to business rule from template and create
            BusinessRuleFromTemplate businessRuleFromTemplate = TemplateManagerHelper
                    .jsonToBusinessRuleFromTemplate(businessRule);

            templateManagerService.createBusinessRuleFromTemplate(businessRuleFromTemplate);
        } else {
            BusinessRuleFromScratch businessRuleFromScratch = TemplateManagerHelper.jsonToBusinessRuleFromScratch
                    (businessRule);

            templateManagerService.createBusinessRuleFromScratch(businessRuleFromScratch);
        }

        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "Successfully created!")).build();
    }

    @Override
    public Response deleteBusinessRule(String businessRuleInstanceID
            , Boolean forceDelete
    ) throws NotFoundException {
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        boolean deleted = templateManagerService.deleteBusinessRule(businessRuleInstanceID);
        if (deleted) {
            return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "Business Rule deleted " +
                    "successfully!")).build();
        } else {
            return Response.status(500).build();
        }
    }

    @Override
    public Response getBusinessRules() throws NotFoundException {
        // todo: connect with 'loadBusinessRules()' in backend
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        Map<String, BusinessRule> businessRuleMap = templateManagerService.loadBusinessRules();
        if (businessRuleMap == null) {
            return Response.serverError().build();
        }

        ArrayList<BusinessRule> businessRulesWithoutUUID = new ArrayList();
        for(String businessRuleUUID : businessRuleMap.keySet()){
            businessRulesWithoutUUID.add(businessRuleMap.get(businessRuleUUID));
        }
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

        return Response.ok().entity(gson.toJson(businessRulesWithoutUUID)).build();
    }

    @Override
    public Response getRuleTemplate(String templateGroupID, String ruleTemplateID ) throws NotFoundException {
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        try {
            RuleTemplate ruleTemplate = templateManagerService.getRuleTemplate(templateGroupID, ruleTemplateID);
            return Response.ok().entity(ruleTemplate).build();
        } catch (TemplateManagerException e) {
            return Response.status(404).entity(new ApiResponseMessage(ApiResponseMessage.ERROR,
                    e.getMessage())).build();
        }
    }

    @Override
    public Response getRuleTemplates(String templateGroupID ) throws NotFoundException {
        // do some magic!
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        try {
            // Get rule templates and store without UUIDs
            Map<String, RuleTemplate> ruleTemplates = templateManagerService.getRuleTemplates(templateGroupID);
            ArrayList<RuleTemplate> ruleTemplatesWithoutUUID = new ArrayList();
            for (String ruleTemplateUUID : ruleTemplates.keySet()) {
                ruleTemplatesWithoutUUID.add(ruleTemplates.get(ruleTemplateUUID));
            }
            Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

            return Response.ok().entity(gson.toJson(ruleTemplatesWithoutUUID)).build();
        } catch (TemplateManagerException e) {
            return Response.status(404).build();
        }
    }

    @Override
    public Response getTemplateGroup(String templateGroupID
    ) throws NotFoundException {
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        try {
            TemplateGroup templateGroup = templateManagerService.getTemplateGroup(templateGroupID);
            return Response.ok().entity(templateManagerService.getTemplateGroup(templateGroupID)).build();
        } catch (TemplateManagerException e) {
            return Response.status(404).build();
        }
    }

    @Override
    public Response getTemplateGroups() throws NotFoundException {
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        // Get template groups and store without UUIDs
        Map<String, TemplateGroup> templateGroups = templateManagerService.getTemplateGroups();
        ArrayList<TemplateGroup> templateGroupsArrayList = new ArrayList();
        for (String templateGroupUUID : templateGroups.keySet()) {
            templateGroupsArrayList.add(templateGroups.get(templateGroupUUID));
        }
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

        return Response.ok().entity(gson.toJson(templateGroupsArrayList)).build();
    }

    @Override
    public Response loadBusinessRule(String businessRuleInstanceID
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response redeployBusinessRule(String businessRuleInstanceID
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response updateBusinessRule(String businessRule
, String businessRuleInstanceID
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
