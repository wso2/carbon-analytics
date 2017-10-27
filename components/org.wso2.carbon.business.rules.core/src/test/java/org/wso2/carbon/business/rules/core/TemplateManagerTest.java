package org.wso2.carbon.business.rules.core;

import org.wso2.carbon.business.rules.core.exceptions.RuleTemplateScriptException;
import org.wso2.carbon.business.rules.core.exceptions.TemplateManagerHelperException;

import java.util.HashMap;
import java.util.Map;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

public class TemplateManagerTest {
//    public static void main(String args[]) throws TemplateManagerServiceException {
//        TemplateManagerService service = new TemplateManagerService();
//        File file = new File("/home/anusha/WSO2/Projects/BRMS/BusinessRule.json");
//        JsonObject jsonObject = TemplateManagerHelper.fileToJson(file);
//
//        BusinessRuleFromScratch businessRuleFromScratch = TemplateManagerHelper.
//                jsonToBusinessRuleFromScratch(jsonObject);
//
//        service.createBusinessRuleFromScratch(businessRuleFromScratch);
////        ConfigReader configReader = new ConfigReader("business.rules");
////        List nodes = configReader.getNodes();
//
//    }

//    public static void main(String[] args) {
//        Map<String, String> variables = null;
//        try {
//            variables = getScriptGeneratedVariables(
//                    "var addition = addition(50,f)\n" +
//                            "\n" +
//                            "function addition(val1,val2){\n" +
//                            "\tif(isNaN(val1) || isNaN(val2)){\n" +
//                            "\t\tthrow 'Not a number'\t\n" +
//                            "\t}\n" +
//                            "\treturn val1 + val2\n" +
//                            "}");
//
//            for (String varName : variables.keySet()) {
//                System.out.print("%" + varName + " => ");
//                System.out.println(variables.get(varName));
//            }
//        }catch(Exception e){
//            System.out.println(e.getMessage());
//        }
//
//
//    }
//
//    public static Map<String, String> getScriptGeneratedVariables(String script) throws RuleTemplateScriptException {
//        ScriptEngineManager manager = new ScriptEngineManager();
//        ScriptEngine engine = manager.getEngineByName("JavaScript");
//
//        ScriptContext scriptContext = new SimpleScriptContext();
//        scriptContext.setBindings(engine.createBindings(), ScriptContext.ENGINE_SCOPE);
//        try {
//            // Run script
//            engine.eval(script);
//            Map<String, Object> returnedScriptContextBindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
//
//            // Store binding variable values returned as objects, as strings
//            Map<String, String> variableValues = new HashMap<String, String>();
//            for (Map.Entry variable : returnedScriptContextBindings.entrySet()) {
//                if(variable.getValue() == null){
//                    variableValues.put(variable.getKey().toString(), null);
//                } else {
//                    variableValues.put(variable.getKey().toString(), variable.getValue().toString());
//                }
//            }
//
//            return variableValues;
//        } catch (ScriptException e) {
//            throw new RuleTemplateScriptException("Error occurred while running the script", e);
//        }
//    }
}
