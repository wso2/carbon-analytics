package org.wso2.carbon.business.rules.core;

import org.wso2.carbon.business.rules.core.bean.businessRulesFromScratch.BusinessRuleFromScratch;
import org.wso2.carbon.business.rules.core.bean.businessRulesFromTemplate.BusinessRuleFromTemplate;
import org.wso2.carbon.business.rules.core.exceptions.TemplateManagerException;
import org.wso2.carbon.business.rules.core.util.TemplateManagerHelper;

public class TemplateManagerTest {
    private static String businessRuleFromScratch = "{" +
            " \"businessRule\" : {" +
            "   \"uuid\" : \"custom-stock-exchange-analysis-for-wso2\"," +
            "   \"name\" : \"Custom Stock Exchange Analysis for WSO2\"," +
            "   \"templateGroupUUID\" : \"stock-exchange\"," +
            "   \"inputRuleTemplateUUID\" : \"stock-exchange-input\"," +
            "   \"outputRuleTemplateUUID\" : \"stock-exchange-output\"," +
            "   \"type\" : \"scratch\"," +
            "   \"properties\": {" +
            "     \"inputData\":{" +
            "       \"topicList\":\"SampleStockStream2\"" +
            "     }," +
            "     \"ruleComponents\":{" +
            "       \"filterRules\":[\"price > 1000\",\"volume < 50\",\"Company = WSO2 Inc\"]," +
            "       \"ruleLogic\":[\"[0] Or ([1] And [2])\"]" +
            "     }," +
            "     \"outputData\":{" +
            "       \"resultTopic\":\"SampleResultTopic2\"" +
            "     }," +
            "     \"outputMappings\":{" +
            "       \"name\" : \"companyName\"," +
            "       \"symbol\":\"companySymbol\"" +
            "     }" +
            "   }" +
            " }" +
            "}";
    private static String businessRuleFromTemplate = "{" +
            " \"businessRule\" : {" +
            "   \"uuid\" : \"my-stock-data-analysis\"," +
            "   \"name\" : \"My Stock Data Analysis\"," +
            "   \"templateGroupUUID\" : \"stock-exchange\"," +
            "   \"ruleTemplateUUID\" : \"stock-data-analysis\"," +
            "   \"type\" : \"template\"," +
            "   \"properties\" : {" +
            "     \"sourceTopicList\" : \"StockStream\"," +
            "     \"sourceMapType\" : \"xml\"," +
            "     \"sinkTopic\" : \"resultTopic\"," +
            "     \"sinkMapType\" : \"xml\"," +
            "     \"minShareVolumesMargin\" : \"20\"," +
            "     \"maxShareVolumesMargin\" : \"10000\"" +
            "   }" +
            " }" +
            "}";
    public static void main(String[] args)throws TemplateManagerException{
        BusinessRuleFromScratch businessRuleFromScratch = TemplateManagerHelper.jsonToBusinessRuleFromScratch
                (TemplateManagerTest.businessRuleFromScratch);
        System.out.println(businessRuleFromScratch.toString());

        BusinessRuleFromTemplate businessRuleFromTemplate = TemplateManagerHelper.jsonToBusinessRuleFromTemplate
                (TemplateManagerTest.businessRuleFromTemplate);


    }



}
