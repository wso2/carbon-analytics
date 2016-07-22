/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.jaggeryapp.template.deployer.internal.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xerces.impl.Constants;
import org.wso2.carbon.jaggeryapp.template.deployer.internal.JaggeryappTemplateDeployerConstants;
import org.wso2.carbon.jaggeryapp.template.deployer.internal.JaggeryappTemplateDeployerException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class JaggeryappTemplateDeployerHelper {

    private static final Log log = LogFactory.getLog(JaggeryappTemplateDeployerHelper.class);


    /**
     * Validate the given file path is in the parent directory itself.
     *
     * @param fileName
     */
    public static void validateFilePath(String fileName) throws JaggeryappTemplateDeployerException {
        if (fileName.contains("../") || fileName.contains("..\\")) {
            throw new JaggeryappTemplateDeployerException("File name contains restricted path elements. " + fileName);
        }
    }

    public static DocumentBuilderFactory getSecuredDocumentBuilder() {

        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        documentBuilderFactory.setNamespaceAware(true);
        documentBuilderFactory.setXIncludeAware(false);
        documentBuilderFactory.setExpandEntityReferences(false);
        try {
            documentBuilderFactory.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE, false);
            documentBuilderFactory.setFeature(Constants.SAX_FEATURE_PREFIX + Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE, false);
            documentBuilderFactory.setFeature(Constants.XERCES_FEATURE_PREFIX + Constants.LOAD_EXTERNAL_DTD_FEATURE, false);
        } catch (ParserConfigurationException e) {
            log.error(
                    "Failed to load XML Processor Feature " + Constants.EXTERNAL_GENERAL_ENTITIES_FEATURE + " or " +
                            Constants.EXTERNAL_PARAMETER_ENTITIES_FEATURE + " or " + Constants.LOAD_EXTERNAL_DTD_FEATURE);
        }

        org.apache.xerces.util.SecurityManager securityManager = new org.apache.xerces.util.SecurityManager();
        securityManager.setEntityExpansionLimit(JaggeryappTemplateDeployerConstants.ENTITY_EXPANSION_LIMIT);
        documentBuilderFactory.setAttribute(Constants.XERCES_PROPERTY_PREFIX + Constants.SECURITY_MANAGER_PROPERTY, securityManager);
        return documentBuilderFactory;
    }
}
