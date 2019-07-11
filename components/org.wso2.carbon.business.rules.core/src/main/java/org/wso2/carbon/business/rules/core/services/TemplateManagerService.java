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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import org.wso2.carbon.business.rules.core.datasource.configreader.ConfigReader;
import org.wso2.carbon.business.rules.core.deployer.SiddhiAppApiHelper;
import org.wso2.carbon.business.rules.core.exceptions.BusinessRuleNotFoundException;
import org.wso2.carbon.business.rules.core.exceptions.BusinessRulesDatasourceException;
import org.wso2.carbon.business.rules.core.exceptions.RuleTemplateScriptException;
import org.wso2.carbon.business.rules.core.exceptions.SiddhiAppsApiHelperException;
import org.wso2.carbon.business.rules.core.exceptions.TemplateInstanceCountViolationException;
import org.wso2.carbon.business.rules.core.exceptions.TemplateManagerHelperException;
import org.wso2.carbon.business.rules.core.exceptions.TemplateManagerServiceException;
import org.wso2.carbon.business.rules.core.util.LogEncoder;
import org.wso2.carbon.business.rules.core.util.TemplateManagerConstants;
import org.wso2.carbon.business.rules.core.util.TemplateManagerHelper;
import org.wso2.carbon.streaming.integrator.core.internal.util.SiddhiAppProcessorConstants;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The exposed Template Manager service, which contains methods related to Business Rules from template, and Business
 * Rules from scratch
 */
public class TemplateManagerService implements BusinessRulesService {
    private static final Logger log = LoggerFactory.getLogger(TemplateManagerService.class);
    private static final int DEFAULT_ARTIFACT_COUNT = 1;
    private static SiddhiAppApiHelper siddhiAppApiHelper = new SiddhiAppApiHelper();
    // Available Template Groups from the directory
    private Map<String, TemplateGroup> availableTemplateGroups;
    private Map<String, BusinessRule> availableBusinessRules;
    private Map nodes = null;
    private Gson gson;
    private QueryExecutor queryExecutor;

    public TemplateManagerService() throws TemplateManagerServiceException, RuleTemplateScriptException {
        this.gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
        this.queryExecutor = new QueryExecutor();
        ConfigReader configReader = new ConfigReader();
        this.nodes = configReader.getNodes();
        // Load & store available Template Groups & Business Rules at the time of instantiation
        this.availableTemplateGroups = loadTemplateGroups();
        if (!configReader.getSolutionType().equalsIgnoreCase(SiddhiAppProcessorConstants.WSO2_SERVER_TYPE_SP)) {
            loadAndSaveAnalyticsSolutions(configReader.getSolutionType());
        } else {
            List<String> solutionTypesEnabled = configReader.getSolutionTypesEnabled();
            for (String solutionType : solutionTypesEnabled) {
                loadAndSaveAnalyticsSolutions(solutionType);
            }
        }
        loadBusinessRules();
    }

    public int createBusinessRuleFromTemplate(BusinessRuleFromTemplate businessRuleFromTemplate, Boolean shouldDeploy)
            throws TemplateManagerServiceException, RuleTemplateScriptException,
            TemplateInstanceCountViolationException {
        String ruleTemplateUUID = businessRuleFromTemplate.getRuleTemplateUUID();
        List<String> nodeList = getNodesList(ruleTemplateUUID);
        String businessRuleUUID = businessRuleFromTemplate.getUuid();
        int status = TemplateManagerConstants.SAVED;
        Map<String, Artifact> constructedArtifacts;
        try {
            constructedArtifacts = constructArtifacts(businessRuleFromTemplate);
        } catch (TemplateManagerHelperException e) {
            log.error("Constructing artifacts for business rule was failed. ", e);
            return TemplateManagerConstants.ERROR;
        }
        // Save business rule definition with errors
        try {
            // Save business rule definition with errors
            saveBusinessRuleDefinition(businessRuleUUID, businessRuleFromTemplate, status, DEFAULT_ARTIFACT_COUNT);
            insertRuleTemplate(businessRuleFromTemplate.getRuleTemplateUUID());
            log.info(String.format("Business rule %s saved into the database.", businessRuleFromTemplate.getName()));
        } catch (BusinessRulesDatasourceException e) {
            throw new TemplateManagerServiceException("Saving business rule '" +
                    businessRuleFromTemplate.getName() + "' to the database is failed. ", e);
        }

        // Construct artifacts from the templates specified in the given business rule
        updateArtifactCount(businessRuleUUID, constructedArtifacts.size());

        if (nodeList == null) {
            log.error(String.format("Failed to find configurations of nodes for " +
                            "ruleTemplate %s while deploying the business rule %s ",
                    LogEncoder.removeCRLFCharacters(ruleTemplateUUID),
                    LogEncoder.removeCRLFCharacters(businessRuleFromTemplate.getUuid())));
            return TemplateManagerConstants.ERROR;
        }

        // Deploy each artifact
        if (shouldDeploy) {
            int deployedNodesCount = 0;
            for (String nodeURL : nodeList) {
                try {
                    deployBusinessRule(nodeURL, constructedArtifacts);
                    deployedNodesCount += 1;
                } catch (SiddhiAppsApiHelperException e) {
                    log.error(String.format("Failed to deploy business rule %s ",
                            businessRuleFromTemplate.getName()), e);
                }
            }
            // Set status with respect to deployed node count
            if (deployedNodesCount == nodeList.size()) {
                status = TemplateManagerConstants.DEPLOYED;
            } else if (deployedNodesCount == 0) {
                status = TemplateManagerConstants.DEPLOYMENT_FAILURE;
            } else {
                status = TemplateManagerConstants.PARTIALLY_DEPLOYED;
            }
            updateDeploymentStatus(businessRuleUUID, status);
        }

        return status;
    }

    public int createBusinessRuleFromScratch(BusinessRuleFromScratch businessRuleFromScratch, Boolean toDeploy)
            throws TemplateManagerServiceException, RuleTemplateScriptException,
            TemplateInstanceCountViolationException {
        List<String> nodeList;
        String businessRuleUUID = businessRuleFromScratch.getUuid();
        // Derive input & output siddhiApp artifacts from the templates specified in the given business rule
        Map<String, Artifact> constructedArtifacts;
        try {
            constructedArtifacts = constructArtifacts(businessRuleFromScratch);
        } catch (TemplateManagerHelperException e) {
            log.error("Constructing artifacts for business rule was failed. ", e);
            return TemplateManagerConstants.ERROR;
        }
        int status = TemplateManagerConstants.SAVED;
        try {
            // Save business rule definition with errors
            saveBusinessRuleDefinition(businessRuleUUID, businessRuleFromScratch, status, status);
            insertRuleTemplate(businessRuleFromScratch.getInputRuleTemplateUUID());
            insertRuleTemplate(businessRuleFromScratch.getOutputRuleTemplateUUID());
            log.info(String.format("Business rule %s saved into the database.", businessRuleFromScratch.getName()));
        } catch (BusinessRulesDatasourceException e) {
            throw new TemplateManagerServiceException("Saving business rule '" + businessRuleFromScratch.getName() +
                    "' to the database is failed. ", e);
        }
        updateArtifactCount(businessRuleUUID, constructedArtifacts.size());
        // Get nodes where business rule should be deployed
        nodeList = getNodeListForBusinessRuleFromScratch(businessRuleFromScratch);
        if (nodeList == null) {
            log.error("Failed to find configurations of nodes for deploying business rules.");
            return TemplateManagerConstants.ERROR;
        }
        if (toDeploy) {
            int deployedNodesCount = 0;
            Artifact deployableSiddhiApp;
            try {
                deployableSiddhiApp = buildSiddhiAppFromScratch(constructedArtifacts, businessRuleFromScratch);
            } catch (TemplateManagerHelperException e) {
                log.error("Creating siddhi app for the business rule is failed.", e);
                return TemplateManagerConstants.ERROR;
            }
            for (String nodeURL : nodeList) {
                try {
                    deployBusinessRule(nodeURL, deployableSiddhiApp, businessRuleFromScratch);
                    deployedNodesCount += 1;
                } catch (SiddhiAppsApiHelperException e) {
                    log.error(String.format("Deploying siddhi app %s for business rule" +
                                    " %s is failed. ", LogEncoder.removeCRLFCharacters(deployableSiddhiApp.toString()),
                            LogEncoder.removeCRLFCharacters(businessRuleFromScratch.getUuid())), e);
                }
            }
            if (deployedNodesCount == nodeList.size()) {
                status = TemplateManagerConstants.DEPLOYED;
            } else if (deployedNodesCount == 0) {
                status = TemplateManagerConstants.DEPLOYMENT_FAILURE;
            } else {
                status = TemplateManagerConstants.PARTIALLY_DEPLOYED;
            }
            updateDeploymentStatus(businessRuleUUID, status);
        }
        return status;
    }

    public int editBusinessRuleFromTemplate(String uuid, BusinessRuleFromTemplate
            businessRuleFromTemplate, Boolean shouldDeploy)
            throws TemplateManagerServiceException, RuleTemplateScriptException {
        String templateUUID = businessRuleFromTemplate.getRuleTemplateUUID();
        List<String> nodeList = getNodesList(templateUUID);
        String businessRuleUUID = businessRuleFromTemplate.getUuid();
        Map<String, Artifact> derivedArtifacts;
        try {
            derivedArtifacts = constructArtifacts(businessRuleFromTemplate);
        } catch (TemplateManagerHelperException e) {
            log.error("Deriving artifacts for business rule while editing is failed. ", e);
            return TemplateManagerConstants.ERROR;
        }
        int status = TemplateManagerConstants.SAVED;
        try {
            overwriteBusinessRuleDefinition(uuid, businessRuleFromTemplate, status);
            log.info(String.format("Business rule %s updated in the database.", businessRuleFromTemplate.getName()));
        } catch (UnsupportedEncodingException | BusinessRulesDatasourceException e) {
            throw new TemplateManagerServiceException("Saving business rule '" +
                    businessRuleFromTemplate.getName() + "' to the database is failed. ", e);
        }
        if (nodeList == null) {
            log.error("Failed to find configurations of nodes for deploying business rules.");
            return TemplateManagerConstants.ERROR;
        }
        int deployedNodesCount = 0;
        if (shouldDeploy) {
            for (String nodeURL : nodeList) {
                int deployedArtifactCount = 0;
                for (Map.Entry<String, Artifact> artifact : derivedArtifacts.entrySet()) {
                    try {
                        updateDeployedArtifact(nodeURL, artifact.getValue());
                        deployedArtifactCount += 1;
                    } catch (SiddhiAppsApiHelperException e) {
                        log.error(String.format("Deploying artifact with uuid %s is" + " failed. ",
                                LogEncoder.removeCRLFCharacters(artifact.getKey())), e);
                    }
                }
                if (deployedArtifactCount == derivedArtifacts.keySet().size()) {
                    deployedNodesCount += 1;
                }
            }
            if (deployedNodesCount == nodeList.size()) {
                status = TemplateManagerConstants.DEPLOYED;
            } else if (deployedNodesCount == 0) {
                status = TemplateManagerConstants.DEPLOYMENT_FAILURE;
            } else {
                status = TemplateManagerConstants.PARTIALLY_DEPLOYED;
            }
            updateDeploymentStatus(businessRuleUUID, status);
        }
        return status;
    }

    public int editBusinessRuleFromScratch(String uuid, BusinessRuleFromScratch businessRuleFromScratch, Boolean
            toDeploy) throws RuleTemplateScriptException, TemplateManagerServiceException {
        List<String> nodeList;
        String businessRuleUUID = businessRuleFromScratch.getUuid();
        // Get nodes where business rule should be deployed
        nodeList = getNodeListForBusinessRuleFromScratch(businessRuleFromScratch);
        if (nodeList == null) {
            log.error(String.format("Failed to find configurations of nodes for deploying business rule %s .",
                    removeCRLFCharacters(uuid)));
            return TemplateManagerConstants.ERROR;
        }
        Map<String, Artifact> derivedArtifacts;
        Artifact deployableSiddhiApp;
        try {
            derivedArtifacts = constructArtifacts(businessRuleFromScratch);
            deployableSiddhiApp = buildSiddhiAppFromScratch(derivedArtifacts, businessRuleFromScratch);
        } catch (TemplateManagerHelperException e) {
            log.error(String.format("Deriving artifacts for business rule %s while editing, is failed. ",
                    removeCRLFCharacters(businessRuleFromScratch.getUuid())), e);
            return TemplateManagerConstants.ERROR;
        }
        int status = TemplateManagerConstants.SAVED;
        try {
            overwriteBusinessRuleDefinition(uuid, businessRuleFromScratch, status);
            log.info(String.format("Business rule %s updated in the database.",
                    removeCRLFCharacters(businessRuleFromScratch.getName())));
        } catch (UnsupportedEncodingException | BusinessRulesDatasourceException e) {
            throw new TemplateManagerServiceException("Saving business rule '" +
                    businessRuleFromScratch.getName() + "' to the database is failed. ", e);
        }
        if (toDeploy) {
            int deployedNodesCount = 0;
            for (String nodeURL : nodeList) {
                try {
                    updateDeployedArtifact(nodeURL, deployableSiddhiApp);
                    deployedNodesCount += 1;
                } catch (SiddhiAppsApiHelperException e) {
                    log.error(String.format("Deploying siddhi app for the business rule %s on node %s is failed." +
                                    " Hence stopping deploying the business rule.",
                            removeCRLFCharacters(businessRuleFromScratch.getUuid()),
                            removeCRLFCharacters(nodeURL)), e);
                }
            }
            if (deployedNodesCount == nodeList.size()) {
                status = TemplateManagerConstants.DEPLOYED;
            } else if (deployedNodesCount == 0) {
                status = TemplateManagerConstants.DEPLOYMENT_FAILURE;
            } else {
                status = TemplateManagerConstants.PARTIALLY_DEPLOYED;
            }
            updateDeploymentStatus(businessRuleUUID, status);
        }
        return status;
    }

    public BusinessRule findBusinessRule(String businessRuleUUID) throws BusinessRuleNotFoundException {
        for (Map.Entry availableBusinessRule : availableBusinessRules.entrySet()) {
            if (availableBusinessRule.getKey().equals(businessRuleUUID)) {
                return (BusinessRule) availableBusinessRule.getValue();
            }
        }
        throw new BusinessRuleNotFoundException("No Business Rule found with the UUID : " + businessRuleUUID);
    }

    /**
     * Gets deployment information of the business rule with the given UUID
     *
     * @param businessRuleUUID UUID of the business rule
     * @return List of nodes and Siddhi app deployment statuses
     * @throws TemplateManagerServiceException  Exception occurred in Template Manager Service
     * @throws BusinessRulesDatasourceException Exception occurred within the data source
     */
    public List<Map<String, Object>> loadDeploymentInfo(String businessRuleUUID)
            throws TemplateManagerServiceException, BusinessRulesDatasourceException {
        BusinessRule businessRule = loadBusinessRule(businessRuleUUID);
        List<String> deployingNodes;
        if (businessRule instanceof BusinessRuleFromTemplate) {
            deployingNodes = getNodesList(((BusinessRuleFromTemplate) businessRule).getRuleTemplateUUID());
        } else {
            deployingNodes = getNodeListForBusinessRuleFromScratch((BusinessRuleFromScratch) businessRule);
        }
        if (null == deployingNodes) {
            return null;
        }
        return getNodeWiseDeploymentStatuses(deployingNodes, businessRule);
    }

    /**
     * Gets deployment status of the Siddhi apps belonging to the given business rules, in the given list of nodes
     *
     * @param deployingNodes Nodes in which, Siddhi apps of the given BR should be deployed
     * @param businessRule   Business rule object
     * @return Map of Siddhi apps and their deployment statuses
     * @throws BusinessRulesDatasourceException Exception occurred within the data source
     */
    private List<Map<String, Object>> getNodeWiseDeploymentStatuses(List<String> deployingNodes,
                                                                    BusinessRule businessRule)
            throws BusinessRulesDatasourceException {
        int businessRuleStatus = getDeploymentState(businessRule);
        List<Map<String, Object>> nodeWiseDeploymentStatuses = new ArrayList<>();
        for (String nodeURL : deployingNodes) {
            Map<String, Integer> siddhiAppDeploymentStatuses;
            if (businessRule instanceof BusinessRuleFromScratch) {
                siddhiAppDeploymentStatuses = new HashMap<>(1);
                siddhiAppDeploymentStatuses.put(businessRule.getUuid(), getDeploymentStatus(nodeURL,
                        businessRule.getUuid(), businessRuleStatus));
            } else {
                int siddhiAppCount = queryExecutor.executeRetrieveArtifactCountQuery(businessRule.getUuid());
                siddhiAppDeploymentStatuses = new HashMap<>(siddhiAppCount);
                for (int i = 0; i < siddhiAppCount; i++) {
                    String siddhiAppName = businessRule.getUuid() + "_" + i;
                    siddhiAppDeploymentStatuses.put(siddhiAppName, getDeploymentStatus(nodeURL, siddhiAppName,
                            businessRuleStatus));
                }
            }
            Map<String, Object> currentNodeStatuses = new HashMap<>();
            currentNodeStatuses.put("nodeURL", nodeURL);
            currentNodeStatuses.put("siddhiAppStatuses", siddhiAppDeploymentStatuses);
            nodeWiseDeploymentStatuses.add(currentNodeStatuses);
        }
        return nodeWiseDeploymentStatuses;
    }

    /**
     * Gets deployment state of the given Siddhi app, in the given node. 1    - Deployed 0    - Not Deployed -1   - Not
     * Reachable
     *
     * @param nodeURL            URL of the node in which, the deployment status is checked
     * @param siddhiAppName      Name of the Siddhi app, that is checked for deployment
     * @param businessRuleStatus Status of the business rule, which consists the Siddhi app
     * @return Deployment status of the Siddhi app
     */
    private int getDeploymentStatus(String nodeURL, String siddhiAppName, int businessRuleStatus) {
        if (businessRuleStatus == TemplateManagerConstants.SAVED) {
            return TemplateManagerConstants.SIDDHI_APP_NOT_DEPLOYED;
        }
        try {
            if (isDeployedInNode(nodeURL, siddhiAppName)) {
                return TemplateManagerConstants.SIDDHI_APP_DEPLOYED;
            }
            return TemplateManagerConstants.SIDDHI_APP_NOT_DEPLOYED;
        } catch (SiddhiAppsApiHelperException e) {
            if (e.getStatus() == 404) {
                return TemplateManagerConstants.SIDDHI_APP_NOT_DEPLOYED;
            }
            return TemplateManagerConstants.SIDDHI_APP_UNREACHABLE;
        }
    }

    public int deleteBusinessRule(String uuid, Boolean forceDeleteEnabled) throws BusinessRuleNotFoundException,
            TemplateManagerServiceException {
        this.availableBusinessRules = loadBusinessRulesFromDB();
        BusinessRule businessRule = findBusinessRule(uuid);
        boolean isSuccessfullyUndeployed;
        int status = TemplateManagerConstants.SUCCESSFULLY_DELETED;
        List<String> ruleTemplateIDs = new ArrayList<>();
        // If found Business Rule is from Template
        if (businessRule instanceof BusinessRuleFromTemplate) {
            ruleTemplateIDs.add(((BusinessRuleFromTemplate) businessRule).getRuleTemplateUUID());
            BusinessRuleFromTemplate businessRuleFromTemplate = (BusinessRuleFromTemplate) businessRule;
            Collection<Template> templates;
            templates = getTemplates(businessRuleFromTemplate);
            List<String> nodeList = getNodesList(businessRuleFromTemplate.getRuleTemplateUUID());
            if (nodeList == null) {
                throw new TemplateManagerServiceException("Failed to find configurations of nodes for deploying " +
                        "business rules.");
            }
            int currentState;
            try {
                currentState = getDeploymentState(businessRule);
            } catch (BusinessRulesDatasourceException e) {
                throw new TemplateManagerServiceException("Failed to get status of the business rule '" + uuid
                        + "'. ", e);
            }
            if (currentState != TemplateManagerConstants.SAVED) {
                for (String nodeURL : nodeList) {
                    for (int i = 0; i < templates.size(); i++) {
                        boolean isSiddhiAppUndeployed;
                        String siddhiAppName = businessRuleFromTemplate.getUuid() + "_" + i;
                        try {
                            isSiddhiAppUndeployed = undeploySiddhiApp(nodeURL, siddhiAppName);
                            if (!isSiddhiAppUndeployed) {
                                status = TemplateManagerConstants.PARTIALLY_UNDEPLOYED;
                                break;
                            }
                        } catch (SiddhiAppsApiHelperException e) {
                            log.error(String.format("Failed to undeploy siddhi app of %s of the businessRule %s " +
                                            "from node %s ", removeCRLFCharacters(siddhiAppName),
                                    removeCRLFCharacters(businessRule.getUuid()),
                                    removeCRLFCharacters(nodeURL)), e);
                            status = TemplateManagerConstants.PARTIALLY_UNDEPLOYED;
                            break;
                        }
                    }
                }
            }
        } else {
            BusinessRuleFromScratch businessRuleFromScratch = (BusinessRuleFromScratch) businessRule;
            List<String> nodeList = getNodeListForBusinessRuleFromScratch(businessRuleFromScratch);
            if (nodeList == null) {
                throw new TemplateManagerServiceException("Failed to find configurations of nodes " +
                        "for deploying business rules.");
            }
            ruleTemplateIDs.add(businessRuleFromScratch.getInputRuleTemplateUUID());
            ruleTemplateIDs.add(businessRuleFromScratch.getOutputRuleTemplateUUID());
            int currentState;
            try {
                currentState = getDeploymentState(businessRule);
            } catch (BusinessRulesDatasourceException e) {
                throw new TemplateManagerServiceException("Failed to get status of the business rule '" + uuid
                        + "'. ", e);
            }
            if (currentState != TemplateManagerConstants.SAVED) {
                for (String nodeURL : nodeList) {
                    try {
                        isSuccessfullyUndeployed = undeploySiddhiApp(nodeURL, businessRuleFromScratch.getUuid());
                        if (!isSuccessfullyUndeployed) {
                            status = TemplateManagerConstants.PARTIALLY_UNDEPLOYED;
                        }
                    } catch (SiddhiAppsApiHelperException e) {
                        log.error(String.format("Failed to undeploy siddhi app of %s of the businessRule %s " +
                                        "from node %s ", businessRuleFromScratch.getUuid(),
                                businessRule.getUuid(), nodeURL), e);
                        status = TemplateManagerConstants.PARTIALLY_UNDEPLOYED;
                        break;
                    }
                }
            }
        }
        if (status == TemplateManagerConstants.PARTIALLY_UNDEPLOYED) {
            try {
                queryExecutor.executeUpdateDeploymentStatusQuery(uuid, status);
            } catch (BusinessRulesDatasourceException e) {
                log.error(String.format("Failed to update the deployment status for the business rule with uuid %s " +
                        "on the database after trying to undeploy.", removeCRLFCharacters(uuid)), e);
            }
        }
        try {
            removeBusinessRuleDefinition(uuid);
            log.info(String.format("Business rule %s deleted from the database.",
                    removeCRLFCharacters(businessRule.getName())));
        } catch (BusinessRulesDatasourceException e) {
            throw new TemplateManagerServiceException("Failed to delete business rule with uuid '" +
                    uuid + "'. ", e);
        }
        for (String ruleTemplatUUID : ruleTemplateIDs) {
            deleteRuleTemplate(ruleTemplatUUID);
        }
        return status;
    }


    /**
     * Returns whether the given node list has at least one element, and qualifies for deployment
     *
     * @param nodeList : List of node host and ports
     * @return : Whether the given node list has at least one element
     */
    private boolean hasAtLeastOneNode(List<String> nodeList) {
        return (nodeList != null && !nodeList.isEmpty());
    }

    /**
     * Gets list of node host and port, in which, the given BusinessRuleFromScratch object should be deployed
     *
     * @param businessRuleFromScratch : Business rule from scratch object
     * @return : List of node host and ports to deploy, when valid, otherwise null
     */
    private List<String> getNodeListForBusinessRuleFromScratch(BusinessRuleFromScratch businessRuleFromScratch) {
        List<String> inputNodeList = getNodesList(businessRuleFromScratch.getInputRuleTemplateUUID());
        List<String> outputNodeList = getNodesList(businessRuleFromScratch.getOutputRuleTemplateUUID());

        if (hasAtLeastOneNode(inputNodeList) && hasAtLeastOneNode(outputNodeList)) {
            inputNodeList.removeAll(outputNodeList);
            inputNodeList.addAll(outputNodeList);
            return inputNodeList;
        }
        if (hasAtLeastOneNode(inputNodeList)) {
            return inputNodeList;
        }
        if (hasAtLeastOneNode(outputNodeList)) {
            return outputNodeList;
        }
        log.error(String.format("Failed to find configurations of nodes for deploying business rule %s ",
                removeCRLFCharacters(businessRuleFromScratch.getUuid())));
        return null;
    }

    private void deployBusinessRule(String nodeURL, Map<String, Artifact> derivedArtifacts)
            throws SiddhiAppsApiHelperException {
        for (Map.Entry template : derivedArtifacts.entrySet()) {
            deployArtifact(nodeURL, template.getKey().toString(), (Artifact) template.getValue());
        }
    }

    private void deployBusinessRule(String nodeURL, Artifact deployableSiddhiApp,
                                    BusinessRuleFromScratch businessRuleFromScratch)
            throws SiddhiAppsApiHelperException {
        deploySiddhiApp(nodeURL, businessRuleFromScratch.getUuid(), deployableSiddhiApp);
    }

    @Override
    public int deployOrUndeployBusinessRule(String businessRuleUUID, boolean shouldUndeploy)
            throws RuleTemplateScriptException, TemplateManagerServiceException, BusinessRuleNotFoundException {
        if (!shouldUndeploy) {
            return redeployBusinessRule(businessRuleUUID);
        } else {
            return undeployBusinessRule(businessRuleUUID);
        }
    }

    public int redeployBusinessRule(String businessRuleUUID)
            throws RuleTemplateScriptException, TemplateManagerServiceException {
        int status = TemplateManagerConstants.ERROR;
        BusinessRule businessRule;
        try {
            businessRule = queryExecutor.retrieveBusinessRule(businessRuleUUID);
            if (businessRule == null) {
                return status;
            }
        } catch (BusinessRulesDatasourceException e) {
            throw new TemplateManagerServiceException("Failed to get business rule.", e);
        }
        if (businessRule instanceof BusinessRuleFromScratch) {
            BusinessRuleFromScratch businessRuleFromScratch = (BusinessRuleFromScratch) businessRule;
            List<String> nodeList;
            nodeList = getNodeListForBusinessRuleFromScratch(businessRuleFromScratch);
            if (nodeList == null) {
                log.error("Cannot find configurations of nodes for deploying business rules.");
                return TemplateManagerConstants.ERROR;
            }
            Map<String, Artifact> derivedArtifacts;
            Artifact deployableSiddhiApp;
            try {
                derivedArtifacts = constructArtifacts(businessRuleFromScratch);
            } catch (TemplateManagerHelperException e) {
                throw new TemplateManagerServiceException("Failed to construct artifacts for the business rule '" +
                        businessRuleFromScratch.getName() + "'. ", e);
            }
            try {
                deployableSiddhiApp = buildSiddhiAppFromScratch(derivedArtifacts, businessRuleFromScratch);
            } catch (TemplateManagerHelperException e) {
                throw new TemplateManagerServiceException("Failed to build siddhi app for the business rule '" +
                        businessRuleFromScratch.getName() + "'. ", e);
            }
            int deployedNodesCount = 0;
            for (String nodeURL : nodeList) {
                try {
                    updateDeployedArtifact(nodeURL, deployableSiddhiApp);
                    deployedNodesCount += 1;
                } catch (SiddhiAppsApiHelperException e) {
                    log.error(String.format("Failed to update the deployed artifact for business rule %s ",
                            removeCRLFCharacters(businessRuleUUID)), e);
                }
            }
            if (deployedNodesCount == nodeList.size()) {
                status = TemplateManagerConstants.DEPLOYED;
            } else if (deployedNodesCount == 0) {
                status = TemplateManagerConstants.DEPLOYMENT_FAILURE;
            } else {
                status = TemplateManagerConstants.PARTIALLY_DEPLOYED;
            }
            updateDeploymentStatus(businessRuleUUID, status);
        } else if (businessRule instanceof BusinessRuleFromTemplate) {
            BusinessRuleFromTemplate businessRuleFromTemplate = (BusinessRuleFromTemplate) businessRule;
            Map<String, Artifact> derivedArtifacts;
            String templateUUID = businessRuleFromTemplate.getRuleTemplateUUID();
            List<String> nodeList = getNodesList(templateUUID);
            if (nodeList == null) {
                log.error("Failed to find configurations of nodes for deploying business rules.");
                return TemplateManagerConstants.ERROR;
            }
            try {
                derivedArtifacts = constructArtifacts(businessRuleFromTemplate);
            } catch (TemplateManagerHelperException e) {
                log.error("Deriving artifacts for business rule while redeploying is failed. ", e);
                return TemplateManagerConstants.ERROR;
            }
            int deployedNodesCount = 0;
            for (String nodeURL : nodeList) {
                try {
                    updateDeployedArtifacts(nodeURL, derivedArtifacts);
                    deployedNodesCount += 1;
                } catch (SiddhiAppsApiHelperException e) {
                    log.error(String.format("Failed to update the deployed artifact for business rule %s ",
                            removeCRLFCharacters(businessRuleUUID)), e);
                }
            }
            // Set status with respect to deployed node count
            if (deployedNodesCount == nodeList.size()) {
                status = TemplateManagerConstants.DEPLOYED;
            } else if (deployedNodesCount == 0) {
                status = TemplateManagerConstants.DEPLOYMENT_FAILURE;
            } else {
                status = TemplateManagerConstants.PARTIALLY_DEPLOYED;
            }
            updateDeploymentStatus(businessRuleUUID, status);
        }
        return status;
    }

    /**
     * Gets un-deployment state of the given Siddhi app, in the given node.
     *
     * @param businessRulesUUID URL of the node in which, the deployment status is checked
     * @return status              The un-deployment status
     */
    private int undeployBusinessRule(String businessRulesUUID)
            throws TemplateManagerServiceException, BusinessRuleNotFoundException {
        this.availableBusinessRules = loadBusinessRulesFromDB();
        BusinessRule businessRule = findBusinessRule(businessRulesUUID);
        int status;
        // If found Business Rule is from Template
        if (businessRule instanceof BusinessRuleFromTemplate) {
            BusinessRuleFromTemplate businessRuleFromTemplate = (BusinessRuleFromTemplate) businessRule;
            Collection<Template> templates;
            templates = getTemplates(businessRuleFromTemplate);
            List<String> nodeList = getNodesList(businessRuleFromTemplate.getRuleTemplateUUID());
            int nodeCount = 0;
            if (nodeList == null) {
                throw new TemplateManagerServiceException("Failed to find configurations of nodes for deploying " +
                        "business rules.");
            }
            for (String nodeURL : nodeList) {
                for (int i = 0; i < templates.size(); i++) {
                    String siddhiAppName = businessRuleFromTemplate.getUuid() + "_" + i;
                    try {
                        undeploySiddhiApp(nodeURL, siddhiAppName);
                        nodeCount = nodeCount + 1;
                    } catch (SiddhiAppsApiHelperException e) {
                        log.error(String.format("Failed to undeploy siddhi app of %s of the businessRule %s " +
                                        "from node %s ", removeCRLFCharacters(siddhiAppName),
                                removeCRLFCharacters(businessRule.getUuid()),
                                removeCRLFCharacters(nodeURL)));
                        if (e.getStatus() == 404) {
                            nodeCount = nodeCount + 1;
                        }
                    }
                }
            }
            if (nodeCount == (nodeList.size() * templates.size())) {
                status = TemplateManagerConstants.SAVED;
            } else {
                status = TemplateManagerConstants.PARTIALLY_UNDEPLOYED;
            }
            updateDeploymentStatus(businessRulesUUID, status);
        } else {
            BusinessRuleFromScratch businessRuleFromScratch = (BusinessRuleFromScratch) businessRule;
            List<String> nodeList = getNodeListForBusinessRuleFromScratch(businessRuleFromScratch);
            if (nodeList == null) {
                throw new TemplateManagerServiceException("Failed to find configurations of nodes " +
                        "for deploying business rules.");
            }
            int nodeCount = 0;
            for (String nodeURL : nodeList) {
                try {
                    undeploySiddhiApp(nodeURL, businessRuleFromScratch.getUuid());
                    nodeCount = nodeCount + 1;
                } catch (SiddhiAppsApiHelperException e) {
                    log.error(String.format("Failed to undeploy siddhi app of %s of the businessRule %s " +
                                    "from node %s ", businessRuleFromScratch.getUuid(),
                            businessRule.getUuid(), nodeURL), e);
                    if (e.getStatus() == 404) {
                        nodeCount = nodeCount + 1;
                    }
                }
            }
            if (nodeCount == nodeList.size()) {
                status = TemplateManagerConstants.SAVED;
            } else {
                status = TemplateManagerConstants.PARTIALLY_UNDEPLOYED;
            }
            updateDeploymentStatus(businessRulesUUID, status);
        }
        return status;
    }

    private Map<String, TemplateGroup> loadTemplateGroups()
            throws RuleTemplateScriptException, TemplateManagerServiceException {
        File directory = new File(TemplateManagerConstants.TEMPLATES_DIRECTORY);
        // To store UUID and Template Group object
        Map<String, TemplateGroup> templateGroups = new HashMap<>();

        // Files from the directory
        File[] files = directory.listFiles();
        if (files != null) {
            for (final File fileEntry : files) {
                // If file is a valid json file
                if (fileEntry.isFile() && fileEntry.getName().endsWith("json")) {
                    // To store the converted file as an object
                    TemplateGroup templateGroup;
                    try {
                        // convert and store
                        templateGroup = TemplateManagerHelper.jsonToTemplateGroup(TemplateManagerHelper
                                .fileToJson(fileEntry));
                        // If file to object conversion is successful
                        if (templateGroup != null) {
                            TemplateManagerHelper.validateTemplateGroup(templateGroup);
                            // Put to map, as denotable by UUID
                            String uuid = templateGroup.getUuid();
                            if (templateGroups.containsKey(uuid)) {
                                throw new TemplateManagerServiceException("A templateGroup with the uuid " + uuid +
                                        " already exists.");
                            }
                            templateGroups.put(templateGroup.getUuid(), templateGroup);
                        } else {
                            throw new TemplateManagerServiceException("Error in converting the file " +
                                    fileEntry.getName() + " to json");
                        }
                    } catch (TemplateManagerHelperException e) {
                        throw new TemplateManagerServiceException("Error in converting the file " +
                                fileEntry.getName() + " to json", e);
                    }
                }
            }
        }
        return templateGroups;
    }

    /**
     * This method loads all the available business ruels from the database and update their deployment status.
     *
     * @return Map that contains loaded business ruels and their uuids
     * @throws TemplateManagerServiceException
     */
    public Map<String, BusinessRule> loadBusinessRules() throws TemplateManagerServiceException {
        availableBusinessRules = loadBusinessRulesFromDB();
        if (availableBusinessRules != null && availableBusinessRules.size() > 0) {
            updateStatuses();
        }
        return availableBusinessRules;
    }

    private Map<String, BusinessRule> loadBusinessRulesFromDB() throws TemplateManagerServiceException {
        try {
            createTablesIfNotExist();
            return queryExecutor.executeRetrieveAllBusinessRules();
        } catch (BusinessRulesDatasourceException e) {
            throw new TemplateManagerServiceException(e);
        }
    }

    private void createTablesIfNotExist() throws BusinessRulesDatasourceException {
        if (!queryExecutor.isBusinessRulesTableExist()) {
            queryExecutor.createTable();
        }
        if (!queryExecutor.isRuleTemplatesTableExist()) {
            queryExecutor.createRuleTemplatesTable();
        }
    }

    /**
     * This method is used to get business rules from the database with their statuses which are recorded to the
     * database
     *
     * @return A list of business ruels with their statuses which are recordsd in to the database.
     * @throws TemplateManagerServiceException
     */
    public List<Object[]> loadBusinessRulesWithStatus() throws TemplateManagerServiceException {
        try {
            return queryExecutor.executeRetrieveAllBusinessRulesWithStatus();
        } catch (BusinessRulesDatasourceException e) {
            throw new TemplateManagerServiceException(e);
        }
    }

    /**
     * This method is used to load a given business rule form the database
     *
     * @param businessRuleUUID uuid of the business rule which needs to be loaded.
     * @return Businsess rule instance which is requested.
     * @throws TemplateManagerServiceException
     */
    public BusinessRule loadBusinessRule(String businessRuleUUID) throws TemplateManagerServiceException {
        try {
            return queryExecutor.retrieveBusinessRule(businessRuleUUID);
        } catch (BusinessRulesDatasourceException e) {
            throw new TemplateManagerServiceException(e);
        }
    }

    /**
     * Returns available Template Group objects, denoted by UUIDs
     *
     * @return Map of TemplateGroups
     * @throws TemplateManagerServiceException TemplateManagerException
     */
    public Map<String, TemplateGroup> getTemplateGroups() throws TemplateManagerServiceException {
        return this.availableTemplateGroups;
    }

    /**
     * Gets the Template Group, that has the given UUID
     *
     * @param templateGroupUUID : uuid of the templateGroup
     * @return TemplateGroup
     */
    public TemplateGroup getTemplateGroup(String templateGroupUUID) throws TemplateManagerServiceException {
        for (Map.Entry availableTemplateGroup : availableTemplateGroups.entrySet()) {
            if (availableTemplateGroup.getKey().toString().equals(templateGroupUUID)) {
                return (TemplateGroup) availableTemplateGroup.getValue();
            }
        }
        throw new TemplateManagerServiceException("No template group found with the UUID - " + templateGroupUUID);
    }

    /**
     * Returns RuleTemplate objects belonging to the given Template Group, denoted by UUIDs
     *
     * @param templateGroupUUID : uuid of templateGroup
     * @return Map of RuleTemplates
     */
    public Map<String, RuleTemplate> getRuleTemplates(String templateGroupUUID) throws TemplateManagerServiceException {
        Map<String, RuleTemplate> ruleTemplatesMap = new HashMap<>();
        for (Map.Entry availableTemplateGroup : availableTemplateGroups.entrySet()) {
            // If matching UUID found
            if (availableTemplateGroup.getKey().toString().equals(templateGroupUUID)) {
                TemplateGroup templateGroup = (TemplateGroup) availableTemplateGroup.getValue();
                Collection<RuleTemplate> ruleTemplates = templateGroup.getRuleTemplates();
                // Put all the found Rule Templates denoted by their UUIDs, for returning
                for (RuleTemplate ruleTemplate : ruleTemplates) {
                    ruleTemplatesMap.put(ruleTemplate.getName(), ruleTemplate);
                }
                return ruleTemplatesMap;
            }
        }
        throw new TemplateManagerServiceException("No template group found with the UUID - " + templateGroupUUID);
    }

    /**
     * Gets Rule Template, which belongs to the given Template Group and has the given Rule Template UUID
     *
     * @param templateGroupUUID : uuid of templateGroup
     * @param ruleTemplateUUID  : uuid of ruleTemplate
     * @return RuleTemplate
     */
    public RuleTemplate getRuleTemplate(String templateGroupUUID, String ruleTemplateUUID)
            throws TemplateManagerServiceException {
        TemplateGroup templateGroup = getTemplateGroup(templateGroupUUID);
        for (RuleTemplate ruleTemplate : templateGroup.getRuleTemplates()) {
            if (ruleTemplate.getUuid().equals(ruleTemplateUUID)) {
                return ruleTemplate;
            }
        }
        throw new TemplateManagerServiceException("No rule template found with the UUID - " + ruleTemplateUUID);
    }

    private void updateStatuses() throws TemplateManagerServiceException {
        try {
            Map<String, BusinessRule> businessRules = loadBusinessRulesFromDB();
            for (Map.Entry entry : businessRules.entrySet()) {
                BusinessRule businessRule = (BusinessRule) entry.getValue();
                int status = getDeploymentState(businessRule);
                queryExecutor.executeUpdateDeploymentStatusQuery(businessRule.getUuid(), status);
            }
        } catch (BusinessRulesDatasourceException e) {
            throw new TemplateManagerServiceException("Failed to update statuses of available business rules. ", e);
        }
    }

    private boolean isDeployedInNode(String nodeURL, String siddhiAppName) throws SiddhiAppsApiHelperException {
        String status = siddhiAppApiHelper.getStatus(nodeURL, siddhiAppName);
        return ("active".equalsIgnoreCase(status));
    }

    private int getDeploymentState(BusinessRule businessRule)
            throws BusinessRulesDatasourceException {
        List<String> nodeList;
        int artifactCount = queryExecutor.executeRetrieveArtifactCountQuery(businessRule.getUuid());
        int deployedNodesCount = 0;
        if (businessRule instanceof BusinessRuleFromTemplate) {
            nodeList = getNodesList(((BusinessRuleFromTemplate) businessRule).getRuleTemplateUUID());
        } else {
            nodeList = getNodeListForBusinessRuleFromScratch(((BusinessRuleFromScratch) businessRule));
        }
        if (nodeList == null) {
            return TemplateManagerConstants.ERROR;
        }

        for (String nodeURL : nodeList) {
            if (businessRule instanceof BusinessRuleFromScratch) {
                Integer queriedState = getSiddhiAppDeploymentState(businessRule.getUuid(), nodeURL,
                        businessRule.getUuid());
                if (queriedState != null) {
                    return queriedState;
                }
            } else {
                for (int i = 0; i < artifactCount; i++) {
                    String siddhiAppName = businessRule.getUuid() + "_" + i;
                    Integer queriedState = getSiddhiAppDeploymentState(businessRule.getUuid(), nodeURL, siddhiAppName);
                    if (queriedState != null) {
                        return queriedState;
                    }
                }
            }
            deployedNodesCount += 1;
        }
        if (deployedNodesCount == nodeList.size()) {
            return TemplateManagerConstants.DEPLOYED;
        } else if (deployedNodesCount == 0) {
            return TemplateManagerConstants.SAVED;
        } else {
            return TemplateManagerConstants.PARTIALLY_DEPLOYED;
        }
    }

    /**
     * Gets deployment state of the Siddhi app with the given name, which belongs to the business rule with the given
     * UUID, under the given node
     *
     * @param businessRuleUUID UUID of the business rule
     * @param nodeURL          URL of the node in which, deployment is checked
     * @param siddhiAppName    Name of the Siddhi app, which should be checked for deployment status
     * @return Siddhi app's deployment status
     * @throws BusinessRulesDatasourceException Exception in business rule data source
     */
    private Integer getSiddhiAppDeploymentState(String businessRuleUUID, String nodeURL, String siddhiAppName)
            throws BusinessRulesDatasourceException {
        try {
            if (!isDeployedInNode(nodeURL, siddhiAppName)) {
                return TemplateManagerConstants.SAVED;
            }
        } catch (SiddhiAppsApiHelperException e) {
            if (log.isDebugEnabled()) {
                log.error(String.format("Could not find the siddhi app %s in node %s.",
                        removeCRLFCharacters(siddhiAppName), removeCRLFCharacters(nodeURL)), e);
            }
            int queriedState = queryExecutor.executeRetrieveDeploymentStatus(businessRuleUUID);
            if (queriedState == TemplateManagerConstants.PARTIALLY_DEPLOYED ||
                    queriedState == TemplateManagerConstants.PARTIALLY_UNDEPLOYED) {
                return queriedState;
            }
            if (queriedState == TemplateManagerConstants.DEPLOYED) {
                return TemplateManagerConstants.PARTIALLY_DEPLOYED;
            }
            return TemplateManagerConstants.SAVED;
        }
        return null;
    }

    /**
     * Derives Artifacts from Templates in the given BusinessRuleFromTemplate. - RuleTemplate is found, and its
     * templated properties are replaced with the values directly specified in the properties map, and the values
     * generated from the script - referring to the specified properties
     *
     * @param businessRuleFromTemplate : businessRule derived from a ruleTemplate
     * @return Templates with replaced properties in the content, denoted by their UUIDs
     */
    private Map<String, Artifact> constructArtifacts(BusinessRuleFromTemplate businessRuleFromTemplate)
            throws RuleTemplateScriptException, TemplateManagerHelperException, TemplateManagerServiceException {
        RuleTemplate ruleTemplate = getRuleTemplate(businessRuleFromTemplate.getTemplateGroupUUID(),
                businessRuleFromTemplate.getRuleTemplateUUID());
        // Get available Templates under the Rule Template, which is specified in the Business Rule
        Collection<Template> templatesToBeUsed = getTemplates(businessRuleFromTemplate);
        Map<String, String> replacementValues = businessRuleFromTemplate.getProperties();
        // Process script and get variables when script is present
        if (ruleTemplate.getScript() != null) {
            String templatedScript = ruleTemplate.getScript();
            String runnableScript = TemplateManagerHelper.replaceTemplateString(templatedScript,
                    businessRuleFromTemplate.getProperties());
            Map<String, String> scriptGeneratedVariables = TemplateManagerHelper.
                    getScriptGeneratedVariables(runnableScript);
            replacementValues.putAll(scriptGeneratedVariables);
        }
        Map<String, Artifact> constructedArtifacts = new HashMap<>();
        int i = 0;
        for (Template template : templatesToBeUsed) {
            // If Template is a SiddhiApp
            String newSiddhiAppName = String.format("%s_%d", businessRuleFromTemplate.getUuid(), i);
            if (template.getType().equals(TemplateManagerConstants.TEMPLATE_TYPE_SIDDHI_APP)) {
                // Derive SiddhiApp with the map containing properties for replacement
                Artifact constructedSiddhiApp = constructSiddhiApp(template, replacementValues);
                constructedSiddhiApp.setContent(constructedSiddhiApp.getContent().replaceAll(TemplateManagerConstants
                                .SIDDHI_APP_NAME_REGEX_PATTERN,
                        "@App:name('" + newSiddhiAppName + "') "));
                constructedArtifacts.put(newSiddhiAppName, constructedSiddhiApp);
                i++;
            }
        }
        return constructedArtifacts;
    }

    /**
     * Derives input and output siddhi apps, that would be combined to create the final SiddhiApp artifact
     *
     * @param businessRuleFromScratch : businessRule derived form scratch
     * @return Map of Artifacts
     * @throws TemplateManagerServiceException : Exception occurred in TemplateManagerService
     */
    private Map<String, Artifact> constructArtifacts(BusinessRuleFromScratch businessRuleFromScratch) throws
            RuleTemplateScriptException, TemplateManagerHelperException, TemplateManagerServiceException {
        RuleTemplate inputRuleTemplate = getRuleTemplate(businessRuleFromScratch.getTemplateGroupUUID(),
                businessRuleFromScratch.getInputRuleTemplateUUID());
        RuleTemplate outputRuleTemplate = getRuleTemplate(businessRuleFromScratch.getTemplateGroupUUID(),
                businessRuleFromScratch.getOutputRuleTemplateUUID());
        Map<String, String> inputPropertiesToMap = businessRuleFromScratch.getProperties().getInputData();
        Map<String, String> outputPropertiesToMap = businessRuleFromScratch.getProperties().getOutputData();
        // Process script and get variables when script is present
        if (inputRuleTemplate.getScript() != null) {
            String inputRuleTemplateScript = inputRuleTemplate.getScript();
            String runnableInputScript = TemplateManagerHelper.replaceTemplateString(inputRuleTemplateScript,
                    businessRuleFromScratch.getProperties().getInputData());
            Map<String, String> inputScriptGeneratedVariables = TemplateManagerHelper.getScriptGeneratedVariables
                    (runnableInputScript);
            inputPropertiesToMap.putAll(inputScriptGeneratedVariables);
        }
        if (outputRuleTemplate.getScript() != null) {
            String outputRuleTemplateScript = outputRuleTemplate.getScript();
            String runnableOutputScript = TemplateManagerHelper.replaceTemplateString(outputRuleTemplateScript,
                    businessRuleFromScratch.getProperties().getOutputData());
            Map<String, String> outputScriptGeneratedVariables = TemplateManagerHelper.getScriptGeneratedVariables
                    (runnableOutputScript);
            outputPropertiesToMap.putAll(outputScriptGeneratedVariables);
        }

        // Get input & output templates, from the Rule Templates specified in the Business Rule
        List<Template> inputOutputTemplatesList = (ArrayList<Template>) getTemplates(businessRuleFromScratch);
        Artifact generatedInputSiddhiAppArtifact = deriveArtifactForBusinessRuleFromScratch(inputOutputTemplatesList.get
                (0), inputPropertiesToMap);
        Artifact generatedOutputSiddhiAppArtifact = deriveArtifactForBusinessRuleFromScratch(inputOutputTemplatesList
                .get(1), outputPropertiesToMap);

        Map<String, Artifact> constructedArtifacts = new HashMap<>();
        constructedArtifacts.put("inputArtifact", generatedInputSiddhiAppArtifact);
        constructedArtifacts.put("outputArtifact", generatedOutputSiddhiAppArtifact);
        return constructedArtifacts;
    }

    private Artifact buildSiddhiAppFromScratch(Map<String, Artifact> derivedTemplates,
                                               BusinessRuleFromScratch businessRuleFromScratch) throws
            TemplateManagerHelperException, TemplateManagerServiceException {
        // Get input & Output rule template collection
        List<RuleTemplate> inputOutputRuleTemplates = getInputOutputRuleTemplates(businessRuleFromScratch);
        // Get properties
        BusinessRuleFromScratchProperty property = businessRuleFromScratch.getProperties();
        // Get ruleComponents
        Map<String, String[]> ruleComponents = property.getRuleComponents();
        // Get filterRules
        String[] filterRules = ruleComponents.get("filterRules");
        String finalRuleLogic;
        if (filterRules.length == 0) {
            finalRuleLogic = "";
        } else {
            // Get ruleLogic
            String[] ruleLogic = ruleComponents.get("ruleLogic");
            // Replace ruleLogic templated values with filter rules
            Map<String, String> replacementValues = new HashMap<>();
            for (int i = 0; i < filterRules.length; i++) {
                replacementValues.put(Integer.toString(i + 1), filterRules[i]);
            }
            // Final rule logic
            finalRuleLogic = TemplateManagerHelper.replaceRegex(ruleLogic[0], TemplateManagerConstants
                    .SIDDHI_APP_RULE_LOGIC_PATTERN, replacementValues);
            finalRuleLogic = "[" + finalRuleLogic + "]";
        }
        // Get Output mapping attributes
        Map<String, String> outputMappingMap = property.getOutputMappings();
        String[] outputMappingMapKeySet = outputMappingMap.keySet().toArray(new String[0]);
        StringBuilder mapping = new StringBuilder();
        // Generate output mapping string
        for (String anOutputMappingMapKeySet : outputMappingMapKeySet) {
            mapping.append(outputMappingMap.get(anOutputMappingMapKeySet))
                    .append(" as ").append(anOutputMappingMapKeySet).append(", ");
        }
        String mappingString = mapping.toString().replaceAll(", $", "");
        // Get ruleTemplates
        RuleTemplate[] ruleTemplates = inputOutputRuleTemplates.toArray(new RuleTemplate[0]);
        // Get input template exposed stream definition
        String inputTemplateStreamDefinition = ruleTemplates[0].getTemplates().toArray(new Template[0])[0]
                .getExposedStreamDefinition();
        // Get output template exposed stream definition
        String outputTemplateStreamDefinition = ruleTemplates[1].getTemplates().toArray(new Template[0])[0]
                .getExposedStreamDefinition();
        // Get stream name
        String inputStreamName = inputTemplateStreamDefinition.split("\\s+")[2].split("\\(")[0];
        // Get output stream name
        String outputStreamName = outputTemplateStreamDefinition.split("\\s+")[2].split("\\(")[0];
        Map<String, String> replacement = new HashMap<>();
        String siddhiAppTemplate = TemplateManagerConstants.SIDDHI_APP_TEMPLATE;
        // Generate replacement values for template
        replacement.put("inputTemplate", derivedTemplates.get("inputArtifact").getContent());
        replacement.put("outputTemplate", derivedTemplates.get("outputArtifact").getContent());
        replacement.put("inputStreamName", inputStreamName);
        replacement.put("logic", finalRuleLogic);
        replacement.put("mapping", mappingString);
        replacement.put("outputStreamName", outputStreamName);
        // Create siddhi app to be deployed
        String content = TemplateManagerHelper.replaceTemplateString(siddhiAppTemplate, replacement);
        // Add the businessRule name as siddhi app name
        content = content.replace("appName", businessRuleFromScratch.getUuid());
        String appType = "siddhiApp";

        return new Artifact(appType, content, null);
    }

    /**
     * Gives the list of Templates, that should be used by the given BusinessRuleFromTemplate
     *
     * @param businessRuleFromTemplate Given Business Rule
     * @return Collection of Templates
     */
    private Collection<Template> getTemplates(BusinessRuleFromTemplate businessRuleFromTemplate)
            throws TemplateManagerServiceException {
        RuleTemplate ruleTemplate = getRuleTemplate(businessRuleFromTemplate);
        // Get Templates from the found Rule Template
        return ruleTemplate.getTemplates();
    }

    /**
     * Gets Templates from the Rule Template, mentioned in the given Business Rule from scratch
     *
     * @param businessRuleFromScratch : businessRule derived from scratch
     * @return Collection of Templates
     * @throws TemplateManagerServiceException : Exception occurred in TemplateManagerService
     */
    private List<Template> getTemplates(BusinessRuleFromScratch businessRuleFromScratch) throws
            TemplateManagerServiceException {
        List<RuleTemplate> inputOutputRuleTemplates = getInputOutputRuleTemplates(businessRuleFromScratch);
        // To store templates, from Input & Output Rule Templates
        List<Template> templates = new ArrayList<>();
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
     * @return Artifact
     */
    private Artifact constructSiddhiApp(Template siddhiAppTemplate, Map<String, String> templatedElementValues)
            throws TemplateManagerHelperException {
        // SiddhiApp content, that contains templated elements
        String templatedSiddhiAppContent = siddhiAppTemplate.getContent();
        // Replace templated elements in SiddhiApp content
        String siddhiAppContent = TemplateManagerHelper
                .replaceTemplateString(templatedSiddhiAppContent, templatedElementValues);
        // No exposed stream definition for SiddhiApp of type 'template'. Only present in types 'input' and 'output'
        return new Artifact(TemplateManagerConstants.TEMPLATE_TYPE_SIDDHI_APP, siddhiAppContent, null);
    }

    /**
     * @param siddhiAppTemplate      : Template SiddhiApp
     * @param templatedElementValues : Provided values for templated fields
     * @return Artifact
     * @throws TemplateManagerHelperException : Exception occurred in TemplateManagerHelper
     */
    private Artifact deriveArtifactForBusinessRuleFromScratch(Template siddhiAppTemplate, Map<String, String>
            templatedElementValues)
            throws TemplateManagerHelperException {
        String siddhiAppContent;
        // SiddhiApp content, that contains templated elements
        String templatedSiddhiAppContent = siddhiAppTemplate.getContent();
        // Remove name from template
        templatedSiddhiAppContent = templatedSiddhiAppContent.replaceFirst(TemplateManagerConstants
                .SIDDHI_APP_NAME_REGEX_PATTERN, "");
        // Replace templated elements in SiddhiApp content
        siddhiAppContent = TemplateManagerHelper
                .replaceTemplateString(templatedSiddhiAppContent, templatedElementValues);
        // No exposed stream definition for a Artifact, It is only a parameter for Template.
        return new Artifact(TemplateManagerConstants.TEMPLATE_TYPE_SIDDHI_APP, siddhiAppContent, null);
    }

    /**
     * @param businessRuleFromTemplate : business rule derived from a template
     * @return : RuleTemplate
     * @throws TemplateManagerServiceException : Exception occurred in TemplateManagerService
     */
    private RuleTemplate getRuleTemplate(BusinessRuleFromTemplate businessRuleFromTemplate)
            throws TemplateManagerServiceException {
        String templateGroupUUID = businessRuleFromTemplate.getTemplateGroupUUID();
        String ruleTemplateUUID = businessRuleFromTemplate.getRuleTemplateUUID();
        TemplateGroup templateGroup = this.availableTemplateGroups.get(templateGroupUUID);
        RuleTemplate foundRuleTemplate = null;
        // A Template Group has been found with the given UUID
        if (templateGroup != null) {
            for (RuleTemplate ruleTemplate : templateGroup.getRuleTemplates()) {
                if (ruleTemplate.getUuid().equals(ruleTemplateUUID)) {
                    foundRuleTemplate = ruleTemplate;
                    break;
                }
            }
            // If a Rule Template has been found
            if (foundRuleTemplate != null) {
                return foundRuleTemplate;
            } else {
                throw new TemplateManagerServiceException("No rule template found with the given uuid");
            }
        } else {
            throw new TemplateManagerServiceException("No template group found with the given uuid");
        }

    }

    /**
     * Gives input & output Rule Templates in a list, specified in the given Business Rule from scratch First member of
     * list denotes Input Rule Template Second member of list denotes Output Rule Template
     *
     * @param businessRuleFromScratch : businessRule derived from scratch
     * @return : Collection of RuleTemplates
     * @throws TemplateManagerServiceException : Exception occurred in TemplateManagerService
     */
    private List<RuleTemplate> getInputOutputRuleTemplates(BusinessRuleFromScratch businessRuleFromScratch) throws
            TemplateManagerServiceException {
        // Find the Rule Template, specified in the Business Rule
        String templateGroupUUID = businessRuleFromScratch.getTemplateGroupUUID();
        TemplateGroup templateGroup = this.availableTemplateGroups.get(templateGroupUUID);
        // Store input & output rule templates
        List<RuleTemplate> inputOutputRuleTemplates = new ArrayList<>();
        String[] inputAndOutputRuleTemplateUUIDs = new String[2];
        inputAndOutputRuleTemplateUUIDs[0] = businessRuleFromScratch.getInputRuleTemplateUUID();
        inputAndOutputRuleTemplateUUIDs[1] = businessRuleFromScratch.getOutputRuleTemplateUUID();
        // If specified Template Group is found
        if (templateGroup != null) {
            for (RuleTemplate ruleTemplate : templateGroup.getRuleTemplates()) {
                // Add only input / output Rule Templates to the list
                for (String ruleTemplateUUID : inputAndOutputRuleTemplateUUIDs) {
                    if (ruleTemplate.getUuid().equals(ruleTemplateUUID)) {
                        inputOutputRuleTemplates.add(ruleTemplate);
                    }
                }
            }
            if (!inputOutputRuleTemplates.isEmpty()) {
                return inputOutputRuleTemplates;
            } else {
                throw new TemplateManagerServiceException("No input / output rule template(s) found with the " +
                        "given uuid");
            }
        } else {
            throw new TemplateManagerServiceException("No template group found with the given uuid");
        }
    }

    /**
     * @param businessRuleUUID         : uuid of the businessRule
     * @param businessRuleFromTemplate : businessRule derived from a template
     * @param deploymentStatus         : status of the businessRule
     * @return true or false
     * @throws TemplateManagerServiceException : Exception occured in TemplateManagerService
     */
    private boolean saveBusinessRuleDefinition(String businessRuleUUID,
                                               BusinessRuleFromTemplate businessRuleFromTemplate, int deploymentStatus,
                                               int artifactCount)
            throws BusinessRulesDatasourceException, TemplateManagerServiceException,
            TemplateInstanceCountViolationException {
        byte[] businessRule;
        resolveInstanceCount(businessRuleFromTemplate);
        try {
            businessRule = TemplateManagerHelper.businessRuleFromTemplateToJson(businessRuleFromTemplate)
                    .getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new TemplateManagerServiceException("Failed to convert business rule " + businessRuleUUID +
                    " to json.", e);
        }
        // convert String into InputStream
        return queryExecutor.executeInsertQuery(businessRuleUUID, businessRule, deploymentStatus, artifactCount);
    }

    /**
     * @param businessRuleUUID        : uuid of the businessRule
     * @param businessRuleFromScratch : businessRule derived from scratch
     * @param deploymentStatus        : status of the business rule
     * @throws TemplateManagerServiceException : Exception occurred in TemplateManagerService
     */
    private boolean saveBusinessRuleDefinition(String businessRuleUUID, BusinessRuleFromScratch businessRuleFromScratch,
                                               int deploymentStatus, int artifactCount)
            throws BusinessRulesDatasourceException, TemplateManagerServiceException,
            TemplateInstanceCountViolationException {
        byte[] businessRule;
        resolveInstanceCount(businessRuleFromScratch);
        try {
            businessRule = TemplateManagerHelper.businessRuleFromScratchToJson(businessRuleFromScratch)
                    .getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new TemplateManagerServiceException("Failed to convert business rule to json. ", e);
        }
        return queryExecutor.executeInsertQuery(businessRuleUUID, businessRule, deploymentStatus, artifactCount);
    }

    /**
     * @param businessRuleUUID unique id of the business rule
     * @param deploymentStatus latest status of the given business rule
     */
    public void updateDeploymentStatus(String businessRuleUUID, int deploymentStatus) {
        try {
            queryExecutor.executeUpdateDeploymentStatusQuery(businessRuleUUID, deploymentStatus);
        } catch (BusinessRulesDatasourceException e) {
            log.error(String.format("Failed to update the state of business rule %s ",
                    removeCRLFCharacters(businessRuleUUID)), e);
        }
    }

    /**
     * @param nodeURL          : URL of the node on which artifacts are going to be deployed
     * @param businessRuleUUID : uuid of the businessRule to which artifact belongs
     * @param siddhiApp        : siddhi app to be deployed
     */
    private void deployArtifact(String nodeURL, String businessRuleUUID, Artifact siddhiApp) throws
            SiddhiAppsApiHelperException {
        if (siddhiApp.getType().equals(TemplateManagerConstants.TEMPLATE_TYPE_SIDDHI_APP)) {
            deploySiddhiApp(nodeURL, businessRuleUUID, siddhiApp);
        }
    }

    /**
     * @param nodeURL       : URL of the node on which artifacts are going to be deployed
     * @param siddhiAppName : name of the siddhiApp which is going to be deployed
     * @param siddhiApp     : siddhiApp which is going to be deployed
     */
    private void deploySiddhiApp(String nodeURL, String siddhiAppName, Artifact siddhiApp)
            throws SiddhiAppsApiHelperException {
        String deployableSiddhiApp;
        if (!siddhiApp.getContent().trim().startsWith("@")) {
            deployableSiddhiApp = siddhiApp.getContent().substring(1, siddhiApp.getContent().length() - 1);
        } else {
            deployableSiddhiApp = siddhiApp.getContent();
        }
        siddhiAppApiHelper.deploySiddhiApp(nodeURL, deployableSiddhiApp);
    }

    /**
     * @param businessRuleUUID         : UUID of the businessRule
     * @param businessRuleFromTemplate : businessRule derived from a template
     * @param deploymentStatus         : status of the businessRule
     * @return true or false
     * @throws UnsupportedEncodingException     : occurs when utf-8 encoding is not supported
     * @throws BusinessRulesDatasourceException : occurs when accessing to the businessRulesDatabase
     */
    private boolean overwriteBusinessRuleDefinition(String businessRuleUUID,
                                                    BusinessRuleFromTemplate businessRuleFromTemplate,
                                                    int deploymentStatus)
            throws UnsupportedEncodingException, BusinessRulesDatasourceException {
        String businessRuleJSON = gson.toJson(businessRuleFromTemplate, BusinessRuleFromTemplate.class);
        byte[] businessRule = businessRuleJSON.getBytes("UTF-8");
        return queryExecutor.executeUpdateBusinessRuleQuery(businessRuleUUID, businessRule, deploymentStatus);
    }

    private boolean overwriteBusinessRuleDefinition(String uuid, BusinessRuleFromScratch businessRuleFromScratch,
                                                    int deploymentStatus) throws
            UnsupportedEncodingException, BusinessRulesDatasourceException {
        String businessRuleJSON = gson.toJson(businessRuleFromScratch, BusinessRuleFromScratch.class);
        byte[] businessRule = businessRuleJSON.getBytes("UTF-8");
        return queryExecutor.executeUpdateBusinessRuleQuery(uuid, businessRule, deploymentStatus);
    }

    /**
     * @param nodeURL  : URL of the node on which artifacts are going to be updated
     * @param artifact : Artifact to be updated
     * @throws SiddhiAppsApiHelperException : occurs when dealing with SiddhiAppsApi
     */
    private void updateDeployedArtifact(String nodeURL, Artifact artifact) throws
            SiddhiAppsApiHelperException {
        if (artifact.getType().equals(TemplateManagerConstants.TEMPLATE_TYPE_SIDDHI_APP)) {
            updateDeployedSiddhiApp(nodeURL, artifact);
        }
    }

    /**
     * @param nodeURL   : URL of the node on which artifacts are going to be updated
     * @param artifacts : Mao of artifacts to be updated
     * @throws SiddhiAppsApiHelperException : occurs when dealing with SiddhiAppsApi
     */
    private void updateDeployedArtifacts(String nodeURL, Map<String, Artifact> artifacts) throws
            SiddhiAppsApiHelperException {
        for (Map.Entry entry : artifacts.entrySet()) {
            Artifact artifact = (Artifact) entry.getValue();
            if (artifact.getType().equals(TemplateManagerConstants.TEMPLATE_TYPE_SIDDHI_APP)) {
                updateDeployedSiddhiApp(nodeURL, artifact);
            }
        }
    }

    /**
     * @param nodeURL   : URL of the node on which artifacts are going to be deployed
     * @param siddhiApp : siddhiApp to be updated.
     * @throws SiddhiAppsApiHelperException : occurs when dealing with SiddhiAppsApi
     */
    private void updateDeployedSiddhiApp(String nodeURL, Artifact siddhiApp) throws SiddhiAppsApiHelperException {
        siddhiAppApiHelper.update(nodeURL, siddhiApp.getContent());

    }

    /**
     * @param nodeURL          : URL of the node from which siddhiApp is going to be un-deployed
     * @param businessRuleUUID : UUID of the businessRule to which the siddhiApp belongs.
     * @return : true or false
     */
    private boolean undeploySiddhiApp(String nodeURL, String businessRuleUUID) throws SiddhiAppsApiHelperException {
        return siddhiAppApiHelper.delete(nodeURL, businessRuleUUID);
    }

    /**
     * @param businessRuleUUID : uuid of the businessRule which is going to be deleted.
     * @return true or false
     * @throws BusinessRulesDatasourceException : occurs when accessing to the businessRulesDatabase
     */
    private boolean removeBusinessRuleDefinition(String businessRuleUUID) throws
            BusinessRulesDatasourceException {
        return queryExecutor.executeDeleteQuery(businessRuleUUID);
    }

    private void updateArtifactCount(String businessRuleUUID, int artifactCount)
            throws TemplateManagerServiceException {
        try {
            queryExecutor.executeUpdateArtifactCountQuery(businessRuleUUID, artifactCount);
        } catch (BusinessRulesDatasourceException e) {
            throw new TemplateManagerServiceException("Saving business rule '" +
                    businessRuleUUID + "' to the database is failed. ", e);
        }
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
                List templateList = (List) templates;
                if (!templateList.contains(ruleTemplateUUID.toString())) {
                    return new ArrayList<String>(nodes.keySet());
                } else {
                    for (Object uuid : templateList) {
                        if (ruleTemplateUUID.equals(uuid.toString())) {
                            nodeList.add(node);
                            break;
                        }
                    }
                }
            }
        }
        return nodeList;
    }

    private void resolveInstanceCount(BusinessRule businessRule)
            throws TemplateManagerServiceException, TemplateInstanceCountViolationException {
        int count;
        if (businessRule instanceof BusinessRuleFromTemplate) {
            BusinessRuleFromTemplate businessRuleFromTemplate = (BusinessRuleFromTemplate) businessRule;
            String instanceLimit = getRuleTemplate(businessRuleFromTemplate).getInstanceCount();
            count = getInstanceCount(businessRuleFromTemplate.getRuleTemplateUUID());
            if (TemplateManagerConstants.ONE.equalsIgnoreCase(instanceLimit) && count >= 1) {
                throw new TemplateInstanceCountViolationException("Rule Template '" +
                        (businessRuleFromTemplate.getRuleTemplateUUID() + "' can be instantiated " +
                                "only once and has already been instantiated."));
            }
        } else if (businessRule instanceof BusinessRuleFromScratch) {
            BusinessRuleFromScratch businessRuleFromScratch = (BusinessRuleFromScratch) businessRule;
            List<RuleTemplate> ruleTemplates = getInputOutputRuleTemplates(businessRuleFromScratch);
            String instanceLimit;
            for (RuleTemplate ruleTemplate : ruleTemplates) {
                instanceLimit = ruleTemplate.getInstanceCount();
                count = getInstanceCount(ruleTemplate.getUuid());
                if (TemplateManagerConstants.ONE.equalsIgnoreCase(instanceLimit) && count >= 1) {
                    throw new TemplateInstanceCountViolationException("Rule Template '" + (ruleTemplate.getUuid() +
                            "' can be instantiated only once and has already been instantiated."));
                }
            }
        }
    }

    private void insertRuleTemplate(String uuid) {
        try {
            queryExecutor.executeInsertRuleTemplateQuery(uuid);
        } catch (BusinessRulesDatasourceException e) {
            log.error(String.format("Failed to insert the rule template '%s' to the database", uuid), e);
        }
    }

    private void deleteRuleTemplate(String uuid) {
        try {
            queryExecutor.executeDeleteRuleTemplateQuery(uuid);
        } catch (BusinessRulesDatasourceException e) {
            log.error(String.format("Failed to delete the rule template '%s' to the database", uuid), e);
        }
    }

    private int getInstanceCount(String uuid) {
        int count = 0;
        try {
            List<String> ruleTemplates = queryExecutor.executeRetrieveAllRuleTemplates();
            for (String templateID : ruleTemplates) {
                if (uuid.equalsIgnoreCase(templateID)) {
                    count += 1;
                }
            }
        } catch (BusinessRulesDatasourceException e) {
            log.error(String.format("Failed to get instance count of the rule template '%s'", uuid), e);
        }
        return count;
    }

    private String removeCRLFCharacters(String str) {
        if (str != null) {
            str = str.replace('\n', '_').replace('\r', '_');
        }
        return str;
    }

    private void loadAndSaveAnalyticsSolutions(String solutionType) throws TemplateManagerServiceException {
        if (Objects.nonNull(availableTemplateGroups.get(solutionType))) {
            TemplateGroup templateGroup = availableTemplateGroups.get(solutionType);
            List<RuleTemplate> ruleTemplates = templateGroup.getRuleTemplates();

            for (RuleTemplate ruleTemplate : ruleTemplates) {
                BusinessRuleFromTemplate businessRuleFromTemplate = replacePropertiesWithDefaultValues
                        (ruleTemplate, templateGroup.getUuid());
                try {
                    createTablesIfNotExist();
                    BusinessRule businessRule = queryExecutor.retrieveBusinessRule(ruleTemplate.getUuid());

                    if (businessRule == null) {
                        saveBusinessRuleDefinition(ruleTemplate.getUuid(), businessRuleFromTemplate,
                                TemplateManagerConstants.DEPLOYED, DEFAULT_ARTIFACT_COUNT);
                        insertRuleTemplate(ruleTemplate.getUuid());
                    }
                } catch (BusinessRulesDatasourceException | TemplateInstanceCountViolationException e) {
                    throw new TemplateManagerServiceException("Saving business rule '" +
                            businessRuleFromTemplate.getName() + "' to the database is failed for solution type "
                            + solutionType, e);
                }
            }
        }
    }

    private BusinessRuleFromTemplate replacePropertiesWithDefaultValues(RuleTemplate ruleTemplate,
                                                                        String templateGroupUUID) {
        Map<String, String> properties = ruleTemplate.getProperties().entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey, e -> e.getValue().getDefaultValue()));
        return new BusinessRuleFromTemplate(ruleTemplate.getUuid(), ruleTemplate.getName(), templateGroupUUID,
                ruleTemplate.getType(), ruleTemplate.getUuid(), properties);
    }

}