package org.wso2.carbon.business.rules.core.api.impl;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.business.rules.core.api.ApiResponseMessage;
import org.wso2.carbon.business.rules.core.api.BusinessRulesApiService;
import org.wso2.carbon.business.rules.core.api.NotFoundException;
import org.wso2.carbon.business.rules.core.bean.TemplateManagerInstance;
import org.wso2.carbon.business.rules.core.bean.scratch.BusinessRuleFromScratch;
import org.wso2.carbon.business.rules.core.exceptions.TemplateManagerException;
import org.wso2.carbon.business.rules.core.services.TemplateManagerService;
import org.wso2.carbon.business.rules.core.bean.template.BusinessRuleFromTemplate;
import org.wso2.carbon.business.rules.core.util.TemplateManagerHelper;

import java.io.File;
import javax.ws.rs.core.Response;

/**
 * Implementation of Business Rules Api Service
 * **/
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen",
        date = "2017-10-06T14:26:28.099Z")
public class BusinessRulesApiServiceImpl extends BusinessRulesApiService {
    //TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
    private static final Logger log = LoggerFactory.getLogger(BusinessRulesApiServiceImpl.class);
    @Override
    public Response createBusinessRule(String businessRule
    ) throws NotFoundException {

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
        TemplateManagerService service = new TemplateManagerService();
        File file = new File("/home/anusha/WSO2/Projects/BRMS/BusinessRule.json");
        JsonObject jsonObject = null;
        try {
            jsonObject = TemplateManagerHelper.fileToJson(file);
//            BusinessRuleFromScratch businessRuleFromScratch = TemplateManagerHelper.
//                    jsonToBusinessRuleFromScratch(jsonObject);
            BusinessRuleFromTemplate businessRulesFromTemplate = TemplateManagerHelper
                    .jsonToBusinessRuleFromTemplate(jsonObject);

            service.createBusinessRuleFromTemplate(businessRulesFromTemplate);
        } catch (TemplateManagerException e) {
            log.error(e.getMessage());
        }
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response getRuleTemplate(String templateGroupID
            , String ruleTemplateID
    ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response getRuleTemplates(String templateGroupID
    ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response getTemplateGroup(String templateGroupID
    ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }

    @Override
    public Response getTemplateGroups() throws NotFoundException {
        // do some magic!
        TemplateManagerService templateManagerService = TemplateManagerInstance.getInstance();
        return Response.ok().entity(templateManagerService.getTemplateGroups()).build();
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
