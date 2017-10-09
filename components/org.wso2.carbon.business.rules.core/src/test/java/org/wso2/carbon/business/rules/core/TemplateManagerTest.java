package org.wso2.carbon.business.rules.core;

import com.google.gson.JsonObject;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.wso2.carbon.business.rules.core.bean.businessRulesFromScratch.BusinessRuleFromScratch;
import org.wso2.carbon.business.rules.core.bean.businessRulesFromTemplate.BusinessRuleFromTemplate;
import org.wso2.carbon.business.rules.core.deployer.configreader.ConfigReader;
import org.wso2.carbon.business.rules.core.exceptions.TemplateManagerException;
import org.wso2.carbon.business.rules.core.services.TemplateManagerService;
import org.wso2.carbon.business.rules.core.util.TemplateManagerHelper;

import java.io.File;
import java.util.List;

public class TemplateManagerTest {
    public static void main(String args[]) throws TemplateManagerException {
        TemplateManagerService service = new TemplateManagerService();
        File file = new File("/home/anusha/WSO2/Projects/BRMS/BusinessRule.json");
        JsonObject jsonObject = TemplateManagerHelper.fileToJson(file);

        BusinessRuleFromScratch businessRuleFromScratch = TemplateManagerHelper.
                jsonToBusinessRuleFromScratch(jsonObject);

        service.createBusinessRuleFromScratch(businessRuleFromScratch);
//        ConfigReader configReader = new ConfigReader("business.rules");
//        List nodes = configReader.getNodes();

    }
}
