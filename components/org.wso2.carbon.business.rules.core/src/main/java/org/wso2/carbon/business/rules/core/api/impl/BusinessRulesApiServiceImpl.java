package org.wso2.carbon.business.rules.core.api.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.wso2.carbon.business.rules.core.api.ApiResponseMessage;
import org.wso2.carbon.business.rules.core.api.BusinessRulesApiService;
import org.wso2.carbon.business.rules.core.api.NotFoundException;
import org.wso2.carbon.business.rules.core.bean.RuleTemplate;
import org.wso2.carbon.business.rules.core.bean.TemplateGroup;
import org.wso2.carbon.business.rules.core.bean.TemplateManagerInstance;
import org.wso2.carbon.business.rules.core.exceptions.TemplateManagerException;
import org.wso2.carbon.business.rules.core.services.TemplateManagerService;
import java.util.ArrayList;
import java.util.Map;
import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-10-06T14:26:28.099Z")
public class BusinessRulesApiServiceImpl extends BusinessRulesApiService {

    @Override
    public Response createBusinessRule(String businessRule
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response deleteBusinessRule(String businessRuleInstanceID
, Boolean forceDelete
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response getBusinessRules() throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response getRuleTemplate(String templateGroupID
, String ruleTemplateID
 ) throws NotFoundException {
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        try {
            RuleTemplate ruleTemplate = templateManagerService.getRuleTemplate(templateGroupID, ruleTemplateID);
            return Response.ok().entity(ruleTemplate).build();
        } catch (TemplateManagerException e) {
            // todo: do error handlings like this everywhere
            return Response.status(404).entity(new ApiResponseMessage(ApiResponseMessage.ERROR, e.getMessage()))
                    .build();
        }
    }
    @Override
    public Response getRuleTemplates(String templateGroupID
 ) throws NotFoundException {
        // do some magic!
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        try {
            // Get rule templates and store without UUIDs
            Map<String, RuleTemplate> ruleTemplates = templateManagerService.getRuleTemplates(templateGroupID);
            ArrayList<RuleTemplate> ruleTemplatesWithoutUUID = new ArrayList();
            for(String ruleTemplateUUID : ruleTemplates.keySet()){
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
    public Response updateBusinessRule(String businessRuleInstanceID
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
