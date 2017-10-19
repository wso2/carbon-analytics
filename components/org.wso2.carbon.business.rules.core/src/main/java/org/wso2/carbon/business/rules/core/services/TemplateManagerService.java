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

package org.wso2.carbon.business.rules.core.services;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.business.rules.core.bean.Artifact;
import org.wso2.carbon.business.rules.core.bean.BusinessRule;
import org.wso2.carbon.business.rules.core.bean.RuleTemplate;
import org.wso2.carbon.business.rules.core.bean.Template;
import org.wso2.carbon.business.rules.core.bean.TemplateGroup;
import org.wso2.carbon.business.rules.core.bean.scratch.BusinessRuleFromScratch;
import org.wso2.carbon.business.rules.core.bean.scratch.BusinessRuleFromScratchProperty;
import org.wso2.carbon.business.rules.core.bean.template.BusinessRuleFromTemplate;
import org.wso2.carbon.business.rules.core.datasource.QueryExecutor;
import org.wso2.carbon.business.rules.core.deployer.SiddhiAppApiHelper;
import org.wso2.carbon.business.rules.core.deployer.configreader.ConfigReader;
import org.wso2.carbon.business.rules.core.exceptions.BusinessRulesDatasourceException;
import org.wso2.carbon.business.rules.core.exceptions.TemplateManagerException;
import org.wso2.carbon.business.rules.core.services.template.BusinessRulesFromTemplate;
import org.wso2.carbon.business.rules.core.util.TemplateManagerConstants;
import org.wso2.carbon.business.rules.core.util.TemplateManagerHelper;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The exposed Template Manager service, which contains methods related to
 * Business Rules from template, and Business Rules from scratch
 */
public class TemplateManagerService implements BusinessRulesService {
    private static final Logger log = LoggerFactory.getLogger(TemplateManagerService.class);
    private SiddhiAppApiHelper siddhiAppApiHelper = null;
    // Available Template Groups from the directory
    private Map<String, TemplateGroup> availableTemplateGroups;
    private Map<String, BusinessRule> availableBusinessRules;
    private Map nodes = null;

    public TemplateManagerService() {
        // Load & store available Template Groups & Business Rules at the time of instantiation
        this.availableTemplateGroups = loadTemplateGroups();
        this.availableBusinessRules = loadBusinessRules();
        this.siddhiAppApiHelper = new SiddhiAppApiHelper();
        ConfigReader configReader = new ConfigReader(TemplateManagerConstants.BUSINESS_RULES);
        nodes = configReader.getNodes();
    }

    /*
    * returns
    * 2 if business rule created and deployed successfully.
    * 1 if business rule is partially deployed.
    * 0 if business rule is not deployed on any node.
    * -1 if business rule creation failed due to internal error.
    * */
    public int createBusinessRuleFromTemplate(BusinessRuleFromTemplate businessRuleFromTemplate, Boolean shouldDeploy) {
        // To store derived artifacts from the templates specified in the given business rule
        Map<String, Artifact> derivedArtifacts;
        String ruleTemplateUUID = businessRuleFromTemplate.getRuleTemplateUUID();
        List<String> nodeList = getNodesList(ruleTemplateUUID);
        if (nodeList == null) {
            log.error("Failed to find configurations of nodes for deploying business rules.");
            return TemplateManagerConstants.OPERATION_FAILED;
        }
        String businessRuleUUID = businessRuleFromTemplate.getUuid();
        int status;
        // Derive artifacts from the business rule definition
        try {
            derivedArtifacts = deriveArtifacts(businessRuleFromTemplate);
        } catch (TemplateManagerException e) {
            log.error("Deriving artifacts for business rule is failed due to " + e.getMessage());
            return TemplateManagerConstants.OPERATION_FAILED;
        }

        // Save business rule definition
        try {
            boolean result = saveBusinessRuleDefinition(businessRuleUUID, businessRuleFromTemplate, TemplateManagerConstants.SAVE_SUCCESSFUL_NOT_DEPLOYED);
            status = result ? TemplateManagerConstants.SAVE_SUCCESSFUL_NOT_DEPLOYED :
                    TemplateManagerConstants.OPERATION_FAILED;
        } catch (TemplateManagerException | UnsupportedEncodingException e) {
            // if save unsuccessful return save unsuccessful and log error.
            log.error("Saving business rule to the database is failed due to " + e.getMessage());
            return TemplateManagerConstants.OPERATION_FAILED;
        }

        // Deploy each artifact o
        if (shouldDeploy) {
            int deployedNodesCount = 0;
            for (String nodeURL : nodeList) {
                // To maintain deployment status of all the artifacts
                boolean isDeployed;
                // Deploy business rule
                isDeployed = deployBusinessRule(nodeURL, derivedArtifacts);
                if (isDeployed) {
                    deployedNodesCount += 1;
                } else {
                    // Don't try to deploy if at least one deployment fails
                    break;
                }
            }
            // Set status with respect to deployed node count
            if (deployedNodesCount == nodeList.size()) {
                // When successfully deployed in every node
                status = TemplateManagerConstants.SAVE_SUCCESSFUL_DEPLOYMENT_SUCCESSFUL;
            } else if (deployedNodesCount == 0) {
                // When not deployed in any node
                status = TemplateManagerConstants.SAVE_SUCCESSFUL_NOT_DEPLOYED;
                status = TemplateManagerConstants.SAVE_SUCCESSFUL_DEPLOYMENT_FAILED;
            } else {
                // When deployed in some of the nodes
                status = TemplateManagerConstants.SAVE_SUCCESSFUL_PARTIALLY_DEPLOYED;
            }
            // TODO: 10/18/17 What should we do if update fails??
            updateDeploymentStatus(businessRuleUUID, status);
        }

        return status;
    }

    public int createBusinessRuleFromScratch(BusinessRuleFromScratch businessRuleFromScratch, Boolean toDeploy) {
        // To store derived artifacts from the templates specified in the given business rule
        Map<String, Artifact> derivedArtifacts;
        List<String> nodeList;
        String businessRuleUUID = businessRuleFromScratch.getUuid();

        // Get nodes where business rule should be deployed
        nodeList = getNodeListForBusinessRuleFromScratch(businessRuleFromScratch);
        if (nodeList == null) {

            log.error("Failed to find configurations of nodes for deploying business rules.");
            return TemplateManagerConstants.OPERATION_FAILED;
        }
        int status;
        // Derive input & output siddhiApp artifacts
        try {
            derivedArtifacts = deriveArtifacts(businessRuleFromScratch);
        } catch (TemplateManagerException e) {
            log.error("Deriving artifacts for business rule is failed due to " + e.getMessage());
            return TemplateManagerConstants.OPERATION_FAILED;
        }

        try {
            saveBusinessRuleDefinition(businessRuleUUID, businessRuleFromScratch,
                    TemplateManagerConstants.SAVE_SUCCESSFUL_NOT_DEPLOYED);
            status = TemplateManagerConstants.SAVE_SUCCESSFUL_NOT_DEPLOYED;
        } catch (TemplateManagerException | UnsupportedEncodingException e) {
            log.error("Saving business rule to the database is failed due to " + e.getMessage());
            return TemplateManagerConstants.OPERATION_FAILED;
        }


        if (toDeploy) {
            int deployedNodesCount = 0;
            Artifact deployableSiddhiApp;
            try {
                deployableSiddhiApp = buildSiddhiAppFromScratch(derivedArtifacts, businessRuleFromScratch);
            } catch (TemplateManagerException e) {
                log.error("Creating siddhi app for the business rule is failed due to " + e
                        .getMessage());
                return status;
            }

            for (String nodeURL : nodeList) {
                // To maintain deployment status of all the artifacts
                boolean isDeployed;
                // This siddhiApp will be deployed finally
                isDeployed = deployBusinessRule(nodeURL, deployableSiddhiApp, businessRuleFromScratch);
                if (isDeployed) {
                    deployedNodesCount += 1;
                }
            }

            if (deployedNodesCount == nodeList.size()) {
                status = TemplateManagerConstants.SAVE_SUCCESSFUL_DEPLOYMENT_SUCCESSFUL;
            } else if (deployedNodesCount == 0) {
                status = TemplateManagerConstants.SAVE_SUCCESSFUL_DEPLOYMENT_FAILED;
            } else {
                status = TemplateManagerConstants.SAVE_SUCCESSFUL_PARTIALLY_DEPLOYED;
            }
            updateDeploymentStatus(businessRuleUUID, status);
        }
        return status;
    }

    public int editBusinessRuleFromTemplate(String uuid, BusinessRuleFromTemplate
            businessRuleFromTemplate, Boolean shouldDeploy) {
        Map<String, Artifact> derivedArtifacts;
        String templateUUID = businessRuleFromTemplate.getRuleTemplateUUID();
        List<String> nodeList = getNodesList(templateUUID);
        String businessRuleUUID = businessRuleFromTemplate.getUuid();
        if (nodeList == null) {
            log.error("Failed to find configurations of nodes for deploying business rules.");
            return TemplateManagerConstants.OPERATION_FAILED;
        }
        // Load all available Business Rules again
        int status = TemplateManagerConstants.OPERATION_FAILED;

        try {
            derivedArtifacts = deriveArtifacts(businessRuleFromTemplate);
        } catch (TemplateManagerException e) {
            log.error("Deriving artifacts for business rule while editing is failed due to " + e.getMessage());
            return TemplateManagerConstants.OPERATION_FAILED;
        }

        try {
            boolean result = overwriteBusinessRuleDefinition(uuid, businessRuleFromTemplate,
                    TemplateManagerConstants.SAVE_SUCCESSFUL_NOT_DEPLOYED);
            if (!result) {
                log.error("Saving updated business rule to the database is unsuccessful");
                return TemplateManagerConstants.OPERATION_FAILED;
            }
            status = TemplateManagerConstants.SAVE_SUCCESSFUL_NOT_DEPLOYED;
        } catch (UnsupportedEncodingException | BusinessRulesDatasourceException e) {
            log.error("Saving updated business rule to the database is failed due to " + e.getMessage());
            return status;
        }

        int deployedNodesCount = 0;
        if (shouldDeploy) {
            for (String nodeURL : nodeList) {
                int deplpyedArtifactCount = 0;
                boolean isArtifactDeployed;
                for (Map.Entry<String, Artifact> artifact : derivedArtifacts.entrySet()) {
                    try {
                        isArtifactDeployed = updateDeployedArtifact(nodeURL, artifact.getKey(), artifact.getValue());
                        if (!isArtifactDeployed) {
                            log.error("Deploying artifact with uuid '" + artifact.getKey() + "' on node '" +
                                    nodeURL + "' " +
                                    "is failed. Hence stopping deploying business rule " +
                                    businessRuleFromTemplate.getUuid() + ".");
                            break;
                        } else {
                            deplpyedArtifactCount += 1;
                        }
                    } catch (TemplateManagerException e) {
                        log.error("Deploying artifact with uuid '" + artifact.getKey() + "' on node '" + nodeURL +
                                "' " +
                                "is failed due to " + e.getMessage() + ". Hence stopping deploying business rule " +
                                businessRuleFromTemplate.getUuid() + ".");
                    }
                }
                if (deplpyedArtifactCount == derivedArtifacts.keySet().size()) {
                    deployedNodesCount += 1;
                }
            }

            if (deployedNodesCount == nodeList.size()) {
                status = TemplateManagerConstants.SAVE_SUCCESSFUL_DEPLOYMENT_SUCCESSFUL;
            } else if (deployedNodesCount == 0) {
                status = TemplateManagerConstants.SAVE_SUCCESSFUL_NOT_DEPLOYED;
            } else {
                status = TemplateManagerConstants.SAVE_SUCCESSFUL_PARTIALLY_DEPLOYED;
            }
            updateDeploymentStatus(businessRuleUUID, status);
        }
        return status;
    }

    public int editBusinessRuleFromScratch(String uuid, BusinessRuleFromScratch businessRuleFromScratch, Boolean
            toDeploy) {

        List<String> nodeList;
        String businessRuleUUID = businessRuleFromScratch.getUuid();
        // Get nodes where business rule should be deployed
        nodeList = getNodeListForBusinessRuleFromScratch(businessRuleFromScratch);
        if (nodeList == null) {
            log.error("Failed to find configurations of nodes for deploying business rule: " + uuid + " .");
            return TemplateManagerConstants.OPERATION_FAILED;
        }

        Map<String, Artifact> derivedArtifacts;
        Artifact deployableSiddhiApp;
        int status = TemplateManagerConstants.OPERATION_FAILED;

        try {
            derivedArtifacts = deriveArtifacts(businessRuleFromScratch);
            deployableSiddhiApp = buildSiddhiAppFromScratch(derivedArtifacts, businessRuleFromScratch);
        } catch (TemplateManagerException e) {
            log.error("Deriving artifacts for business rule: " + uuid + ", while editing is failed due to " + e.getMessage
                    ());
            return status;
        }

        try {
            boolean result = overwriteBusinessRuleDefinition(uuid, businessRuleFromScratch,
                    TemplateManagerConstants
                            .SAVE_SUCCESSFUL_NOT_DEPLOYED);
            if (!result) {
                log.error("Saving updated business rule to the database is unsuccessful for busnessRule: " + uuid);
                return TemplateManagerConstants.OPERATION_FAILED;
            }
            status = TemplateManagerConstants.SAVE_SUCCESSFUL_NOT_DEPLOYED;
        } catch (UnsupportedEncodingException | BusinessRulesDatasourceException e) {
            log.error("Saving updated business rule to the database is failed due to " + e.getMessage());
            return status;
        }

        if (toDeploy) {
            int deployedNodesCount = 0;
            for (String nodeURL : nodeList) {
                boolean isDeployed;
                try {
                    isDeployed = updateDeployedArtifact(nodeURL, businessRuleFromScratch.getUuid(),
                            deployableSiddhiApp);
                    if (isDeployed) {
                        deployedNodesCount += 1;
                    }
                } catch (TemplateManagerException e) {
                    log.error("Deploying siddhi app for the business rule'" + businessRuleFromScratch.getUuid() +
                            "' on " +
                            "node '" + nodeURL + "' " +
                            "is failed due to " + e.getMessage() + ". Hence stopping deploying the business rule.");
                }
            }

            if (deployedNodesCount == nodeList.size()) {
                status = TemplateManagerConstants.SAVE_SUCCESSFUL_DEPLOYMENT_SUCCESSFUL;
            } else if (deployedNodesCount == 0) {
                status = TemplateManagerConstants.SAVE_SUCCESSFUL_NOT_DEPLOYED;
            } else {
                status = TemplateManagerConstants.SAVE_SUCCESSFUL_PARTIALLY_DEPLOYED;
            }
            updateDeploymentStatus(businessRuleUUID, status);
        }
        return status;
    }

    public BusinessRule findBusinessRule(String businessRuleUUID) throws TemplateManagerException {
        for (Map.Entry availableBusinessRule : availableBusinessRules.entrySet()) {
            if (availableBusinessRule.getKey().equals(businessRuleUUID)) {
                return (BusinessRule) availableBusinessRule.getValue();

            }
        }

        throw new TemplateManagerException("No Business Rule found with the UUID : " + businessRuleUUID);
    }

    public boolean deleteBusinessRule(String uuid, Boolean forceDeleteEnabled) {
        BusinessRule foundBusinessRule;

        try {
            foundBusinessRule = findBusinessRule(uuid);
        } catch (TemplateManagerException e) {
            log.error("Failed to delete business rule due to " + e.getMessage());
            return false;
        }
        // If found Business Rule is from Template
        if (foundBusinessRule instanceof BusinessRulesFromTemplate) {
            BusinessRuleFromTemplate foundBusinessRuleFromTemplate = (BusinessRuleFromTemplate) foundBusinessRule;
            Collection<Template> templates = null;
            try {
                templates = getTemplates(foundBusinessRuleFromTemplate);
            } catch (TemplateManagerException e) {
                log.error("Failed to delete business rule due to " + e.getMessage());
                return false;
            }

            List<String> nodeList = getNodesList(foundBusinessRuleFromTemplate.getRuleTemplateUUID());
            if (nodeList == null) {
                log.error("Failed to find configurations of nodes for deploying business rules.");
                return false;
            }
            Boolean isSuccessfullyUndeployed = true;
            for (String nodeURL : nodeList) {
                for (int i = 0; i < templates.size(); i++) {
                    boolean isUndeployed = undeploySiddhiApp(nodeURL, foundBusinessRuleFromTemplate.getUuid() +
                            "_" + i);
                    if (!isUndeployed) {
                        isSuccessfullyUndeployed = false;
                    }
                }
            }

            if (isSuccessfullyUndeployed || forceDeleteEnabled) {
                try {
                    return removeBusinessRuleDefinition(uuid);
                } catch (BusinessRulesDatasourceException e) {
                    log.error("Failed to delete business rule with uuid '" + uuid + "' due to " + e.getMessage());
                }
            }
        }

        if (foundBusinessRule instanceof BusinessRuleFromScratch) {
            BusinessRuleFromScratch foundBusinessRuleFromScratch = (BusinessRuleFromScratch) foundBusinessRule;
            String inputTemplateUUID = foundBusinessRuleFromScratch.getInputRuleTemplateUUID();
            String outputTemplateUUID = foundBusinessRuleFromScratch.getOutputRuleTemplateUUID();
            List<String> nodeList = getNodesList(inputTemplateUUID);
            List<String> outputNodeList = getNodesList(outputTemplateUUID);
            if (nodeList != null && outputNodeList != null) {
                nodeList.removeAll(outputNodeList);
                nodeList.addAll(outputNodeList);
            } else {
                log.error("Failed to find configurations of nodes for deploying business rules.");
                return false;
            }
            boolean isCompletelyUndeployed = true;

            for (String nodeURL : nodeList) {
                isCompletelyUndeployed = undeploySiddhiApp(nodeURL, foundBusinessRuleFromScratch.getUuid());
            }

            if (isCompletelyUndeployed | forceDeleteEnabled) {
                try {
                    return removeBusinessRuleDefinition(uuid);
                } catch (BusinessRulesDatasourceException e) {
                    log.error("Failed to delete business rule with uuid '" + uuid + "' due to " + e.getMessage());
                }
            }
        }
        return false;
    }

    private List<String> getNodeListForBusinessRuleFromScratch(BusinessRuleFromScratch businessRuleFromScratch) {
        String inputTemplateUUID = businessRuleFromScratch.getInputRuleTemplateUUID();
        String outputTemplateUUID = businessRuleFromScratch.getOutputRuleTemplateUUID();
        List<String> nodeList = getNodesList(inputTemplateUUID);
        List<String> outputNodeList = getNodesList(outputTemplateUUID);
        String businessRuleUUID = businessRuleFromScratch.getUuid();
        if (nodeList != null && outputNodeList != null) {
            nodeList.removeAll(outputNodeList);
            nodeList.addAll(outputNodeList);
            return nodeList;
        } else {
            log.error("Failed to find configurations of nodes for deploying business rule: " + businessRuleUUID);
            return null;
        }
    }

    private boolean deployBusinessRule(String nodeURL, Map<String, Artifact> derivedArtifacts) {
        boolean state = false;
        for (Map.Entry template : derivedArtifacts.entrySet()) {

            state = deployArtifact(nodeURL, template.getKey().toString(), (Artifact) template.getValue());
            if (!state) {
                log.error("Failed to deploy businessRule : " + template.getKey().toString());
                return false;
            }

        }
        return state;
    }

    private boolean deployBusinessRule(String nodeURL, Artifact deployableSiddhiApp, BusinessRuleFromScratch
            businessRuleFromScratch) {

        boolean state = deploySiddhiApp(nodeURL, businessRuleFromScratch.getUuid(), deployableSiddhiApp);
        if (!state) {
            log.error("Failed to deploy businessRule: " + businessRuleFromScratch.getUuid());
        }
        return state;
    }

    public int redeployBusinessRule(String businessRuleUUID) {

        int status = TemplateManagerConstants.SAVE_SUCCESSFUL_NOT_DEPLOYED;
        BusinessRule businessRule = new QueryExecutor().executeRetrieveBusinessRule(businessRuleUUID);
        if (businessRule == null) {
            status = TemplateManagerConstants.OPERATION_FAILED;
            return status;
        }

        if (businessRule instanceof BusinessRuleFromScratch) {
            BusinessRuleFromScratch businessRuleFromScratch = (BusinessRuleFromScratch) businessRule;
            String inputTemplateUUID = businessRuleFromScratch.getInputRuleTemplateUUID();
            String outputTemplateUUID = businessRuleFromScratch.getOutputRuleTemplateUUID();
            List<String> nodeList = getNodesList(inputTemplateUUID);
            List<String> outputNodeList = getNodesList(outputTemplateUUID);
            if (nodeList == null && outputNodeList != null) {
                nodeList = outputNodeList;
            } else if (nodeList != null && outputNodeList != null) {
                nodeList.removeAll(outputNodeList);
                nodeList.addAll(outputNodeList);
            } else if (nodeList == null) {
                log.error("Cannot find configurations of nodes for deploying business rules.");
                return TemplateManagerConstants.SAVE_SUCCESSFUL_NOT_DEPLOYED;
            }

            Map<String, Artifact> derivedArtifacts;
            Artifact deployableSiddhiApp;
            try {
                derivedArtifacts = deriveArtifacts(businessRuleFromScratch);
                deployableSiddhiApp = buildSiddhiAppFromScratch(derivedArtifacts, businessRuleFromScratch);
            } catch (TemplateManagerException e) {
                log.error("Deriving artifacts for business rule while redeploying is failed due to " +
                        e.getMessage());
                return TemplateManagerConstants.OPERATION_FAILED;
            }
            int deployedNodesCount = 0;
            for (String nodeURL : nodeList) {
                boolean isDeployed;
                try {
                    isDeployed = updateDeployedArtifact(nodeURL, businessRuleUUID, deployableSiddhiApp);
                    if (isDeployed) {
                        deployedNodesCount++;
                    }
                } catch (TemplateManagerException e) {
                    log.error("Re deploying siddhi app for the business rule'" + businessRuleUUID + "' on " +
                            "node '" + nodeURL + "' " +
                            "is failed due to " + e.getMessage() + ". Hence stopping deploying the business rule.");
                }
            }

            if (deployedNodesCount == nodeList.size()) {
                status = TemplateManagerConstants.SAVE_SUCCESSFUL_DEPLOYMENT_SUCCESSFUL;
            } else if (deployedNodesCount == 0) {
                status = TemplateManagerConstants.SAVE_SUCCESSFUL_NOT_DEPLOYED;
            } else {
                status = TemplateManagerConstants.SAVE_SUCCESSFUL_PARTIALLY_DEPLOYED;
            }
            return status;

        } else if (businessRule instanceof BusinessRuleFromTemplate) {
            BusinessRuleFromTemplate businessRuleFromTemplate = (BusinessRuleFromTemplate) businessRule;
            Map<String, Artifact> derivedArtifacts = null;
            String templateUUID = businessRuleFromTemplate.getRuleTemplateUUID();
            List<String> nodeList = getNodesList(templateUUID);
            if (nodeList == null) {
                log.error("Failed to find configurations of nodes for deploying business rules.");
                return TemplateManagerConstants.OPERATION_FAILED;
            }

            try {
                derivedArtifacts = deriveArtifacts(businessRuleFromTemplate);
            } catch (TemplateManagerException e) {
                log.error("Deriving artifacts for business rule while redeploying is failed due to " + e.getMessage());
                return TemplateManagerConstants.OPERATION_FAILED;
            }

            int deployedNodesCount = 0;
            for (String nodeURL : nodeList) {
                boolean isDeployed;
                isDeployed = deployBusinessRule(nodeURL, derivedArtifacts);

                if (isDeployed) {
                    deployedNodesCount++;
                } else {
                    break;
                }
            }
            // Set status with respect to deployed node count
            if (deployedNodesCount == nodeList.size()) {
                // When successfully deployed in every node
                status = TemplateManagerConstants.SAVE_SUCCESSFUL_DEPLOYMENT_SUCCESSFUL;
            } else if (deployedNodesCount == 0) {
                // When not deployed in any node
                status = TemplateManagerConstants.SAVE_SUCCESSFUL_NOT_DEPLOYED;
            } else {
                // When deployed in some of the nodes
                status = TemplateManagerConstants.SAVE_SUCCESSFUL_PARTIALLY_DEPLOYED;
            }
        }
        updateDeploymentStatus(businessRuleUUID, status);
        return status;
    }

    private Map<String, TemplateGroup> loadTemplateGroups() {

        File directory = new File(TemplateManagerConstants.TEMPLATES_DIRECTORY);
        //todo pull templates in to dashboard resource
        // To store UUID and Template Group object
        Map<String, TemplateGroup> templateGroups = new HashMap<>();

        // Files from the directory
        File[] files = directory.listFiles();
        if (files != null) {
            for (final File fileEntry : files) {
                // If file is a valid json file
                if (fileEntry.isFile() && fileEntry.getName().endsWith("json")) {
                    // To store the converted file as an object
                    TemplateGroup templateGroup = null;

                    // convert and store
                    try {
                        templateGroup = TemplateManagerHelper.jsonToTemplateGroup(TemplateManagerHelper
                                .fileToJson(fileEntry));
                    } catch (TemplateManagerException e) {
                        log.error("Error while the file " + fileEntry.getName(), e);
                    }

                    // If file to object conversion is successful
                    if (templateGroup != null) {
                        try {
                            TemplateManagerHelper.validateTemplateGroup(templateGroup);
                            // Put to map, as denotable by UUID
                            templateGroups.put(templateGroup.getUuid(), templateGroup);
                        } catch (TemplateManagerException e) {
                            // Invalid Template Configuration file is found
                            // Abort loading the current file and continue with the next file
                            log.error("Invalid Template Group configuration file found: " + fileEntry.getName() + " : " +
                                    "" + e.getMessage(), e);
                        }
                    } else {
                        log.error("Error in converting the file " + fileEntry.getName());
                    }

                }
            }
        }

        return templateGroups;
    }

    public Map<String, BusinessRule> loadBusinessRules() {
        QueryExecutor queryExecutor = new QueryExecutor();
        return queryExecutor.executeRetrieveAllBusinessRules();
    }

    public List<Object[]> loadBusinessRulesWithStatus() {
        QueryExecutor queryExecutor = new QueryExecutor();
        return queryExecutor.executeRetrieveAllBusinessRulesWithStatus();
    }

    public BusinessRule loadBusinessRule(String businessRuleUUID) {
        QueryExecutor queryExecutor = new QueryExecutor();
        return queryExecutor.executeRetrieveBusinessRule(businessRuleUUID);
    }

    /**
     * Returns available Template Group objects, denoted by UUIDs
     *
     * @return
     */
    public Map<String, TemplateGroup> getTemplateGroups() {
        return this.availableTemplateGroups;
    }

    /**
     * Gets the Template Group, that has the given UUID
     *
     * @param templateGroupUUID
     * @return
     */
    public TemplateGroup getTemplateGroup(String templateGroupUUID) throws TemplateManagerException {
        for (Map.Entry availableTemplateGroup : availableTemplateGroups.entrySet()) {
            if (availableTemplateGroup.getKey().toString().equals(templateGroupUUID)) {
                return (TemplateGroup) availableTemplateGroup.getValue();
            }
        }
        throw new TemplateManagerException("No template group found with the UUID - " + templateGroupUUID);
    }

    /**
     * Returns RuleTemplate objects belonging to the given Template Group, denoted by UUIDs
     *
     * @param templateGroupUUID
     * @return
     */
    public Map<String, RuleTemplate> getRuleTemplates(String templateGroupUUID) throws TemplateManagerException {
        Map<String, RuleTemplate> ruleTemplates = new HashMap<>();
        for (Map.Entry availableTemplateGroup : availableTemplateGroups.entrySet()) {
            // If matching UUID found
            if (availableTemplateGroup.getKey().toString().equals(templateGroupUUID)) {
                TemplateGroup foundTemplateGroup = (TemplateGroup) availableTemplateGroup.getValue();

                Collection<RuleTemplate> foundRuleTemplates = foundTemplateGroup.getRuleTemplates();

                // Put all the found Rule Templates denoted by their UUIDs, for returning
                for (RuleTemplate foundRuleTemplate : foundRuleTemplates) {
                    ruleTemplates.put(foundRuleTemplate.getName(), foundRuleTemplate);
                }

                return ruleTemplates;
            }
        }

        throw new TemplateManagerException("No template group found with the UUID - " + templateGroupUUID);
    }

    /**
     * Gets Rule Template, which belongs to the given Template Group and has the given Rule Template UUID
     *
     * @param templateGroupUUID
     * @param ruleTemplateUUID
     * @return
     */
    public RuleTemplate getRuleTemplate(String templateGroupUUID, String ruleTemplateUUID)
            throws TemplateManagerException {
        TemplateGroup foundTemplateGroup = getTemplateGroup(templateGroupUUID);
        for (RuleTemplate ruleTemplate : foundTemplateGroup.getRuleTemplates()) {
            if (ruleTemplate.getUuid().equals(ruleTemplateUUID)) {
                return ruleTemplate;
            }
        }

        throw new TemplateManagerException("No rule template found with the UUID - " + ruleTemplateUUID);
    }

    /**
     * Derives Artifacts from Templates in the given BusinessRuleFromTemplate.
     * - RuleTemplate is found, and its templated properties are replaced with the values
     * directly specified in the properties map,
     * and the values generated from the script - referring to the specified properties
     *
     * @param businessRuleFromTemplate
     * @return Templates with replaced properties in the content, denoted by their UUIDs
     */
    private Map<String, Artifact> deriveArtifacts(BusinessRuleFromTemplate businessRuleFromTemplate)
            throws TemplateManagerException {
        // To store derived Artifact types and Artifacts
        Map<String, Artifact> derivedArtifacts = new HashMap<>();

        // Find the RuleTemplate specified in the BusinessRule
        RuleTemplate foundRuleTemplate = getRuleTemplate(businessRuleFromTemplate.getTemplateGroupUUID(),
                businessRuleFromTemplate.getRuleTemplateUUID());
        // Get script with templated elements and replace with values given in the BusinessRule
        String scriptWithTemplatedElements = foundRuleTemplate.getScript();
        String runnableScript = TemplateManagerHelper.replaceRegex(scriptWithTemplatedElements,
                TemplateManagerConstants.TEMPLATED_ELEMENT_NAME_REGEX_PATTERN,
                businessRuleFromTemplate.getProperties());

        // Run the script to get all the contained variables
        Map<String, String> scriptGeneratedVariables = TemplateManagerHelper.
                getScriptGeneratedVariables(runnableScript);

        // Get available Templates under the Rule Template, which is specified in the Business Rule
        Collection<Template> templatesToBeUsed = getTemplates(businessRuleFromTemplate);
        // Get properties to map and replace - as specified in the Business Rule, plus variables from the script
        Map<String, String> propertiesToMap = businessRuleFromTemplate.getProperties();
        propertiesToMap.putAll(scriptGeneratedVariables);
        int i = 0;
        // For each template to be used for the Business Rule
        for (Template template : templatesToBeUsed) {
            // If Template is a SiddhiApp
            if (template.getType().equals(TemplateManagerConstants.TEMPLATE_TYPE_SIDDHI_APP)) {
                // Derive SiddhiApp with the map containing properties for replacement
                Artifact derivedSiddhiApp = deriveSiddhiApp(template, propertiesToMap);
                try {
                    derivedSiddhiApp.setContent(derivedSiddhiApp.getContent().replaceAll(TemplateManagerConstants
                                    .SIDDHI_APP_NAME_REGEX_PATTERN,
                            "@App:name('" + businessRuleFromTemplate.getUuid() + "_" + i + "') "));
                    derivedArtifacts.put(TemplateManagerHelper.getSiddhiAppName(derivedSiddhiApp), derivedSiddhiApp);
                    i++;
                } catch (TemplateManagerException e) {
                    log.error("Error in deriving SiddhiApp", e);
                }
            }
        }

        return derivedArtifacts;
    }

    /**
     * Derives input and output siddhi apps, that would be combined to create the final SiddhiApp artifact
     *
     * @param businessRuleFromScratch
     * @return
     * @throws TemplateManagerException
     */
    private Map<String, Artifact> deriveArtifacts(BusinessRuleFromScratch businessRuleFromScratch) throws
            TemplateManagerException {
        Map<String, Artifact> derivedArtifacts = new HashMap<>();

        // Get input & output Rule Templates
        RuleTemplate inputRuleTemplate = getRuleTemplate(businessRuleFromScratch.getTemplateGroupUUID(),
                businessRuleFromScratch.getInputRuleTemplateUUID());
        RuleTemplate outputRuleTemplate = getRuleTemplate(businessRuleFromScratch.getTemplateGroupUUID(),
                businessRuleFromScratch.getOutputRuleTemplateUUID());

        // Get scripts of input & output Rule Templates
        String inputRuleTemplateScript = inputRuleTemplate.getScript();
        String outputRuleTemplateScript = outputRuleTemplate.getScript();

        // Get runnable scripts of input & output Rule Templates
        String runnableInputScript = TemplateManagerHelper.replaceRegex(inputRuleTemplateScript,
                TemplateManagerConstants.TEMPLATED_ELEMENT_NAME_REGEX_PATTERN, businessRuleFromScratch.getProperties()
                        .getInputData());
        String runnableOutputScript = TemplateManagerHelper.replaceRegex(outputRuleTemplateScript,
                TemplateManagerConstants.TEMPLATED_ELEMENT_NAME_REGEX_PATTERN, businessRuleFromScratch.getProperties()
                        .getOutputData());

        // Get variables generated after running input & output scripts
        Map<String, String> inputScriptGeneratedVariables = TemplateManagerHelper.getScriptGeneratedVariables
                (runnableInputScript);
        Map<String, String> outputScriptGeneratedVariables = TemplateManagerHelper.getScriptGeneratedVariables
                (runnableOutputScript);

        // Get input & output templates, specified in the specific Rule Templates that are specified in the
        // Business Rule
        ArrayList<Template> inputOutputTemplatesToBeUsed = (ArrayList<Template>) getTemplates(businessRuleFromScratch);
        Template[] inputOutputTemplatesArrayToBeUsed = inputOutputTemplatesToBeUsed.toArray(new Template[0]);

        // Input & Output properties to map with templated elements in templates (given + script generated replacements)
        Map<String, String> inputPropertiesToMap = businessRuleFromScratch.getProperties().getInputData();
        inputPropertiesToMap.putAll(inputScriptGeneratedVariables);
        Map<String, String> outputPropertiesToMap = businessRuleFromScratch.getProperties().getOutputData();
        outputPropertiesToMap.putAll(outputScriptGeneratedVariables);

        // Property maps array list, that should be mapped with Templates array
        ArrayList<Map<String, String>> propertiesToMap = new ArrayList<Map<String, String>>() {
            {
                add(inputPropertiesToMap);
                add(outputPropertiesToMap);
            }
        };

        // Derive either input or output artifact and put into hash map
        for (int i = 0; i < inputOutputTemplatesArrayToBeUsed.length; i++) {
            if (inputOutputTemplatesArrayToBeUsed[i].getType().equals(TemplateManagerConstants
                    .TEMPLATE_TYPE_SIDDHI_APP)) {
                // Derive SiddhiApp template
                Artifact derivedSiddhiApp = deriveSiddhiAppForBusinessRuleFromScratch(
                        inputOutputTemplatesArrayToBeUsed[i], propertiesToMap.get(i));
                // Put SiddhiApp's name and content to derivedTemplates HashMap
                if (i == 0) {
                    derivedArtifacts.put("inputArtifact",
                            derivedSiddhiApp);
                }
                if (i == 1) {
                    derivedArtifacts.put("outputArtifact",
                            derivedSiddhiApp);
                }
            }
        }
        return derivedArtifacts;
    }


    private Artifact buildSiddhiAppFromScratch(Map<String, Artifact> derivedTemplates,
                                               BusinessRuleFromScratch businessRuleFromScratch)
            throws
            TemplateManagerException {

        //read siddhi_app_template file
        ClassLoader classLoader = this.getClass().getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream("siddhi-app-template.json");
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonObject = null;
        try {
            jsonObject = (JsonObject) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            log.error(e.getMessage());
        }

        // Get input & Output rule template collection
        Collection<RuleTemplate> inputOutputRuleTemplates = getInputOutputRuleTemplates(businessRuleFromScratch);
        // Get properties
        BusinessRuleFromScratchProperty property = businessRuleFromScratch.getProperties();
        // Get ruleComponents
        Map<String, String[]> ruleComponents = property.getRuleComponents();
        // Get filterRules
        String[] filterRules = ruleComponents.get("filterRules");
        // Get ruleLogic
        String[] ruleLogic = ruleComponents.get("ruleLogic");
        // Replace ruleLogic templated values with filter rules
        Map<String, String> replacementValues = new HashMap<>();
        for (int i = 0; i < filterRules.length; i++) {
            replacementValues.put(Integer.toString(i + 1), filterRules[i]);
        }
        // Final rule logic
        String finalRuleLogic = TemplateManagerHelper.replaceRegex(ruleLogic[0], TemplateManagerConstants
                        .SIDDHI_APP_RULE_LOGIC_PATTERN,
                replacementValues);
        // Get Output mapping attributes
        Map<String, String> outputMappingMap = property.getOutputMappings();

        String[] outputMappingMapKeySet = outputMappingMap.keySet().toArray(new String[0]);
        StringBuilder mapping = new StringBuilder();
        // Generate output mapping string
        for (String anOutputMappingMapKeySet : outputMappingMapKeySet) {
            mapping.append(outputMappingMap.get(anOutputMappingMapKeySet))
                    .append(" as ").append(anOutputMappingMapKeySet).append(", ");
        }
        String mappingString = mapping.toString();
        mappingString = mapping.toString().replaceAll(", $", "");
        // Get ruleTemplates
        RuleTemplate[] ruleTemplates = inputOutputRuleTemplates.toArray(new RuleTemplate[0]);
        // Get input template exposed stream definition
        String inputTemplateStreamDefinition = ruleTemplates[0].getTemplates().toArray(new Template[0])[0]
                .getExposedStreamDefinition();
        // Get output template exposed stream definition
        String outputTemplateStreamDefinition = ruleTemplates[1].getTemplates().toArray(new Template[0])[0]
                .getExposedStreamDefinition();
        // Get stream name
        String inputStreamName = inputTemplateStreamDefinition.split(" ")[2].split("\\(")[0];
        // Get output stream name
        String outputStreamName = outputTemplateStreamDefinition.split(" ")[2].split("\\(")[0];

        Map<String, String> replacement = new HashMap<>();
        String siddhiAppTemplate = null;
        // Load siddhi app template
        if (jsonObject != null) {
            siddhiAppTemplate = jsonObject.get("siddhi-app-template").toString();
        }
        // Generate replacement values for template
        replacement.put("inputTemplate", derivedTemplates.get("inputArtifact").getContent());
        replacement.put("outputTemplate", derivedTemplates.get("outputArtifact").getContent());
        replacement.put("inputStreamName", inputStreamName);
        replacement.put("logic", finalRuleLogic);
        replacement.put("mapping", mappingString);
        replacement.put("outputStreamName", outputStreamName);
        // Create siddhi app to be deployed
        String content = TemplateManagerHelper.replaceRegex(siddhiAppTemplate, TemplateManagerConstants
                .TEMPLATED_ELEMENT_NAME_REGEX_PATTERN, replacement);
        // Add the businessRule name as siddhi app name
        content = content.replace("appName", businessRuleFromScratch.getUuid());
        String appType = "siddhiApp";
        Artifact siddhiApp = new Artifact(appType, content, "");

        return siddhiApp;
    }


    /**
     * Gives the list of Templates, that should be used by the given BusinessRuleFromTemplate
     *
     * @param businessRuleFromTemplate Given Business Rule
     * @return
     */
    private Collection<Template> getTemplates(BusinessRuleFromTemplate businessRuleFromTemplate)
            throws TemplateManagerException {
        RuleTemplate foundRuleTemplate = getRuleTemplate(businessRuleFromTemplate);
        // Get Templates from the found Rule Template
        Collection<Template> templates = foundRuleTemplate.getTemplates();

        return templates;
    }

    /**
     * Gets Templates from the Rule Template, mentioned in the given Business Rule from scratch
     *
     * @param businessRuleFromScratch
     * @return
     * @throws TemplateManagerException
     */
    private Collection<Template> getTemplates(BusinessRuleFromScratch businessRuleFromScratch) throws
            TemplateManagerException {

        Collection<RuleTemplate> inputOutputRuleTemplates = getInputOutputRuleTemplates(businessRuleFromScratch);

        // To store templates, from Input & Output Rule Templates
        Collection<Template> templates = new ArrayList<>();
        for (RuleTemplate ruleTemplate : inputOutputRuleTemplates) {
            // Only one Template will be present in a Rule Template
            ArrayList<Template> templateInRuleTemplate = (ArrayList<Template>) ruleTemplate.getTemplates();
            templates.add(templateInRuleTemplate.get(0));
        }

        return templates;
    }

    /**
     * Derives a SiddhiApp by replacing given SiddhiApp template's templated properties with the given values
     *
     * @param siddhiAppTemplate      Templated SiddhiApp
     * @param templatedElementValues Values for replacing templated elements, that are derived by the script from the
     *                               TemplateGroup / directly entered by the user (when no script is present)
     * @return
     */
    private Artifact deriveSiddhiApp(Template siddhiAppTemplate, Map<String, String> templatedElementValues)
            throws TemplateManagerException {
        // SiddhiApp content, that contains templated elements
        String templatedSiddhiAppString = siddhiAppTemplate.getContent();
        // Replace templated elements in SiddhiApp content
        String derivedSiddhiAppString = TemplateManagerHelper.replaceRegex(templatedSiddhiAppString,
                TemplateManagerConstants.TEMPLATED_ELEMENT_NAME_REGEX_PATTERN, templatedElementValues);
        // No exposed stream definition for SiddhiApp of type 'template'. Only present in types 'input' / 'output'

        return new Artifact(TemplateManagerConstants.TEMPLATE_TYPE_SIDDHI_APP,
                derivedSiddhiAppString, null);
    }

    /**
     * @param siddhiAppTemplate
     * @param templatedElementValues
     * @return
     * @throws TemplateManagerException
     */
    private Artifact deriveSiddhiAppForBusinessRuleFromScratch(Template siddhiAppTemplate, Map<String, String>
            templatedElementValues)
            throws TemplateManagerException {
        String derivedSiddhiAppString;
        // SiddhiApp content, that contains templated elements
        String templatedSiddhiAppString = siddhiAppTemplate.getContent();
        // Remove name from template
        templatedSiddhiAppString = templatedSiddhiAppString.replaceFirst(TemplateManagerConstants
                .SIDDHI_APP_NAME_REGEX_PATTERN, "");
        // Replace templated elements in SiddhiApp content
        derivedSiddhiAppString = TemplateManagerHelper.replaceRegex(templatedSiddhiAppString,
                TemplateManagerConstants.TEMPLATED_ELEMENT_NAME_REGEX_PATTERN, templatedElementValues);
        // No exposed stream definition for SiddhiApp of type 'template'. Only present in types 'input' / 'output'

        return new Artifact(TemplateManagerConstants.TEMPLATE_TYPE_SIDDHI_APP,
                derivedSiddhiAppString, null);
    }

    /**
     * @param businessRuleFromTemplate
     * @return
     * @throws TemplateManagerException
     */
    private RuleTemplate getRuleTemplate(BusinessRuleFromTemplate businessRuleFromTemplate)
            throws TemplateManagerException {
        String templateGroupUUID = businessRuleFromTemplate.getTemplateGroupUUID();
        String ruleTemplateUUID = businessRuleFromTemplate.getRuleTemplateUUID();

        TemplateGroup foundTemplateGroup = this.availableTemplateGroups.get(templateGroupUUID);
        RuleTemplate foundRuleTemplate = null;

        // A Template Group has been found with the given UUID
        if (foundTemplateGroup != null) {
            for (RuleTemplate ruleTemplate : foundTemplateGroup.getRuleTemplates()) {
                if (ruleTemplate.getUuid().equals(ruleTemplateUUID)) {
                    foundRuleTemplate = ruleTemplate;
                    break;
                }
            }
            // If a Rule Template has been found
            if (foundRuleTemplate != null) {
                return foundRuleTemplate;
            } else {
                throw new TemplateManagerException("No rule template found with the given uuid");
            }
        } else {
            throw new TemplateManagerException("No template group found with the given uuid");
        }

    }

    /**
     * Gives input & output Rule Templates in a list, specified in the given Business Rule from scratch
     * First member of list denotes Input Rule Template
     * Second member of list denotes Output Rule Template
     *
     * @param businessRuleFromScratch
     * @return
     * @throws TemplateManagerException
     */
    private Collection<RuleTemplate> getInputOutputRuleTemplates(BusinessRuleFromScratch businessRuleFromScratch) throws
            TemplateManagerException {
        // Find the Rule Template, specified in the Business Rule
        String templateGroupUUID = businessRuleFromScratch.getTemplateGroupUUID();
        TemplateGroup foundTemplateGroup = this.availableTemplateGroups.get(templateGroupUUID);
        // Store input & output rule templates
        Collection<RuleTemplate> foundInputOutputRuleTemplates = new ArrayList<>();
        String[] inputAndOutputRuleTemplateUUIDs = new String[2];
        inputAndOutputRuleTemplateUUIDs[0] = businessRuleFromScratch.getInputRuleTemplateUUID();
        inputAndOutputRuleTemplateUUIDs[1] = businessRuleFromScratch.getOutputRuleTemplateUUID();

        // If specified Template Group is found
        if (foundTemplateGroup != null) {
            for (RuleTemplate ruleTemplate : foundTemplateGroup.getRuleTemplates()) {
                // Add only input / output Rule Templates to the list
                for (String ruleTemplateUUID : inputAndOutputRuleTemplateUUIDs) {
                    if (ruleTemplate.getUuid().equals(ruleTemplateUUID)) {
                        foundInputOutputRuleTemplates.add(ruleTemplate);
                    }
                }
            }
            if (!foundInputOutputRuleTemplates.isEmpty()) {
                return foundInputOutputRuleTemplates;
            } else {
                throw new TemplateManagerException("No input / output rule template(s) found with the given uuid");
            }
        } else {
            throw new TemplateManagerException("No template group found with the given uuid");
        }
    }

    /**
     * @param uuid
     * @param businessRuleFromTemplate
     * @param deploymentStatus
     * @return
     * @throws TemplateManagerException
     * @throws UnsupportedEncodingException
     */
    private boolean saveBusinessRuleDefinition(String uuid, BusinessRuleFromTemplate businessRuleFromTemplate, int
            deploymentStatus)
            throws
            TemplateManagerException, UnsupportedEncodingException {
        QueryExecutor queryExecutor = new QueryExecutor();
        byte[] businessRule = TemplateManagerHelper.businessRuleFromTemplateToJson(businessRuleFromTemplate)
                .getBytes("UTF-8");
        // convert String into InputStream
        return queryExecutor.executeInsertQuery(uuid, businessRule, deploymentStatus);
    }

    /**
     * @param uuid
     * @param businessRuleFromScratch
     * @param deploymentStatus
     * @throws TemplateManagerException
     * @throws UnsupportedEncodingException
     */
    private void saveBusinessRuleDefinition(String uuid, BusinessRuleFromScratch businessRuleFromScratch, int
            deploymentStatus)
            throws
            TemplateManagerException, UnsupportedEncodingException {
        QueryExecutor queryExecutor = new QueryExecutor();
        byte[] businessRule = TemplateManagerHelper.businessRuleFromScratchToJson(businessRuleFromScratch)
                .getBytes("UTF-8");
        queryExecutor.executeInsertQuery(uuid, businessRule, deploymentStatus);
    }

    private void updateDeploymentStatus(String businessRuleUUID, int deploymentStatus) {
        QueryExecutor queryExecutor = new QueryExecutor();
        queryExecutor.executeUpdateDeploymentStatusQuery(businessRuleUUID, deploymentStatus);
    }


    /**
     * @param nodeURL
     * @param uuid
     * @param template
     * @return
     */
    private boolean deployArtifact(String nodeURL, String uuid, Artifact template) {
        boolean state = false;
        if (template.getType().equals(TemplateManagerConstants.TEMPLATE_TYPE_SIDDHI_APP)) {
            state = deploySiddhiApp(nodeURL, uuid, template);

            if (!state) {
                log.error("Failed to deploy artifact: " + uuid);
            }
        }
        return state;
    }

    /**
     * @param nodeURL
     * @param siddhiAppName
     * @param siddhiApp
     * @return
     */
    private boolean deploySiddhiApp(String nodeURL, String siddhiAppName, Artifact siddhiApp) {

        String deployableSiddhiApp;
        if (!siddhiApp.getContent().startsWith("@")) {
            deployableSiddhiApp = siddhiApp.getContent().substring(1, siddhiApp.getContent().length() - 1);
        } else {
            deployableSiddhiApp = siddhiApp.getContent();
        }
        boolean state = siddhiAppApiHelper.deploySiddhiApp(nodeURL, deployableSiddhiApp);
        if (!state) {
            log.error("Failed to deploy siddhiApp: " + siddhiAppName);
        }
        return state;
    }

    /**
     * @param uuid
     * @param businessRuleFromTemplate
     * @param deploymentStatus
     * @return
     * @throws UnsupportedEncodingException
     * @throws BusinessRulesDatasourceException
     */
    private boolean overwriteBusinessRuleDefinition(String uuid, BusinessRuleFromTemplate businessRuleFromTemplate,
                                                    int deploymentStatus)
            throws UnsupportedEncodingException, BusinessRulesDatasourceException {
        QueryExecutor queryExecutor = new QueryExecutor();
        byte[] businessRule = businessRuleFromTemplate.toString().getBytes("UTF-8");
        return queryExecutor.executeUpdateBusinessRuleQuery(uuid, businessRule, deploymentStatus);
    }

    private boolean overwriteBusinessRuleDefinition(String uuid, BusinessRuleFromScratch businessRuleFromScratch,
                                                    int deploymentStatus) throws
            UnsupportedEncodingException, BusinessRulesDatasourceException {
        QueryExecutor queryExecutor = new QueryExecutor();
        byte[] businessRule = businessRuleFromScratch.toString().getBytes("UTF-8");
        return queryExecutor.executeUpdateBusinessRuleQuery(uuid, businessRule, deploymentStatus);
    }

    /**
     * @param nodeURL
     * @param uuid
     * @param template
     * @return
     * @throws TemplateManagerException
     */
    private boolean updateDeployedArtifact(String nodeURL, String uuid, Artifact template) throws
            TemplateManagerException {
        boolean state = false;
        if (template.getType().equals(TemplateManagerConstants.TEMPLATE_TYPE_SIDDHI_APP)) {

            state = updateDeployedSiddhiApp(nodeURL, uuid, template);
            if (state) {
                log.error("Failed to deploy ");
            }
        }
        return state;
    }

    /**
     * @param nodeURL
     * @param uuid
     * @param siddhiApp
     * @return
     * @throws TemplateManagerException
     */
    private boolean updateDeployedSiddhiApp(String nodeURL, String uuid, Artifact siddhiApp) throws
            TemplateManagerException {
        boolean state;
        SiddhiAppApiHelper siddhiAppApiHelper = new SiddhiAppApiHelper();
        state = siddhiAppApiHelper.update(nodeURL, siddhiApp.getContent());
        if (!state) {
            log.error("Failed to update siddhiApp :" + uuid);
        }
        return state;
    }

    /**
     * @param nodeURL
     * @param uuid
     * @return
     */
    private boolean undeploySiddhiApp(String nodeURL, String uuid) {
        SiddhiAppApiHelper siddhiAppApiHelper = new SiddhiAppApiHelper();
        Boolean isSucessfullyUndeployed;
        isSucessfullyUndeployed = siddhiAppApiHelper.delete(nodeURL, uuid);
        return isSucessfullyUndeployed;
    }

    /**
     * @param uuid
     * @return
     * @throws BusinessRulesDatasourceException
     */
    private boolean removeBusinessRuleDefinition(String uuid) throws
            BusinessRulesDatasourceException {
        QueryExecutor queryExecutor = new QueryExecutor();
        return queryExecutor.executeDeleteQuery(uuid);
    }

    private List<String> getNodesList(String ruleTemplateUUID) {
        List<String> nodeList = new ArrayList<>();
        if (nodes == null) {
            return null;
        }
        for (Object object : nodes.keySet()) {
            String node = object.toString();
            Object templates = nodes.get(node);
            if (templates instanceof List) {
                for (Object uuid : (List) templates) {
                    if (ruleTemplateUUID.equals(uuid.toString())) {
                        nodeList.add(node);
                        break;
                    }
                }
            }
        }
        return nodeList;
    }
}
