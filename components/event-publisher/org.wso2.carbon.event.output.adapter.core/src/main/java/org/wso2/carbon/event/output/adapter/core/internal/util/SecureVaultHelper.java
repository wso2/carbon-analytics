/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.output.adapter.core.internal.util;

import org.apache.axiom.om.OMElement;
import org.w3c.dom.*;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.event.output.adapter.core.exception.OutputEventAdapterException;
import org.wso2.carbon.event.output.adapter.core.internal.EventAdapterConstants;
import org.wso2.carbon.event.output.adapter.core.internal.ds.OutputEventAdapterServiceValueHolder;
import org.wso2.securevault.SecretResolver;
import org.wso2.securevault.SecretResolverFactory;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class SecureVaultHelper {

    private static SecretResolver secretResolver;

    public static void secureResolveDocument(Document doc)
            throws OutputEventAdapterException {
        Element element = doc.getDocumentElement();
        if (element != null) {
            try {
                secureLoadElement(element);
            } catch (CryptoException e) {
                throw new OutputEventAdapterException("Error in secure load of global output event adapter properties: " +
                        e.getMessage(), e);
            }
        }
    }

    public static Document convertToDocument(File file) throws OutputEventAdapterException {
        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        fac.setNamespaceAware(true);
        try {
            return fac.newDocumentBuilder().parse(file);
        } catch (Exception e) {
            throw new OutputEventAdapterException("Error in creating an XML document from file: " +
                    e.getMessage(), e);
        }
    }

    private static void secureLoadElement(Element element)
            throws CryptoException {

        Attr secureAttr = element.getAttributeNodeNS(EventAdapterConstants.SECURE_VAULT_NS,
                EventAdapterConstants.SECRET_ALIAS_ATTR_NAME);
        if (secureAttr != null) {
            element.setTextContent(loadFromSecureVault(secureAttr.getValue()));
            element.removeAttributeNode(secureAttr);
        }
        NodeList childNodes = element.getChildNodes();
        int count = childNodes.getLength();
        Node tmpNode;
        for (int i = 0; i < count; i++) {
            tmpNode = childNodes.item(i);
            if (tmpNode instanceof Element) {
                secureLoadElement((Element) tmpNode);
            }
        }
    }

    private static synchronized String loadFromSecureVault(String alias) {
        if (secretResolver == null) {
            secretResolver = SecretResolverFactory.create((OMElement) null, false);
            secretResolver.init(OutputEventAdapterServiceValueHolder.
                    getSecretCallbackHandlerService().getSecretCallbackHandler());
        }
        return secretResolver.resolve(alias);
    }

}