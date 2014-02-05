/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.bam.utils.config;

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ConfigurationUtils {

    private static InputStream bamConfigFile = null;
    private static List<CFConfigBean> configBeans = null;

    public synchronized static InputStream getBAMConfigFile() throws FileNotFoundException {
        if (bamConfigFile == null) {
            String filePath = CarbonUtils.getCarbonConfigDirPath() + File.separator
                              + ConfigurationConstants.BAM_CONFIG_FILE_NAME;
            bamConfigFile = new FileInputStream(filePath);
        }
        return bamConfigFile;
    }

    public static synchronized List<CFConfigBean> getCFConfigurations()
            throws Exception {
        if (configBeans != null) {
            return configBeans;
        }

        OMXMLParserWrapper builder = null;
        try {
            builder = OMXMLBuilderFactory.createOMBuilder(ConfigurationUtils.getBAMConfigFile());
        } catch (FileNotFoundException e) {
            throw new ConfigurationException("bam configuration file not found", e);
        }

        OMElement documentElement = builder.getDocumentElement();
        return getCFConfigurations(documentElement);

    }

    public static List<CFConfigBean> getCFConfigurations(OMElement documentElement) throws Exception {
//        OMElement cFIndexes = documentElement.getFirstChildWithName(
//                ConfigurationConstants.INDEX_QNAME);
        Iterator cfIterator = documentElement.getChildrenWithName(ConfigurationConstants.CF_QNAME);

        List<CFConfigBean> localConfigBeans = new ArrayList<CFConfigBean>();
        while (cfIterator.hasNext()) {

            Object object = cfIterator.next();

            if (object instanceof OMElement) {

                OMElement cf = (OMElement) object;
                CFConfigBean configBean = new CFConfigBean();
                String name = cf.getAttribute(new QName("name")).getAttributeValue();
                configBean.setCfName(name);
                OMAttribute defaultCF = cf.getAttribute(ConfigurationConstants.DEFAULT_CF_QNAME);

                if (defaultCF != null) {

                    configBean.setDefaultCF(Boolean.parseBoolean(defaultCF.getAttributeValue()));

                    if (!Boolean.parseBoolean(defaultCF.getAttributeValue())) {  // If not default CF this is a secondary CF
                        configBean.setPrimaryCF(Boolean.TRUE);
                    }

                } else { // If not explicitly defined this is not a default CF as well
                    configBean.setPrimaryCF(Boolean.TRUE);
                }

                OMElement granularity = cf.getFirstChildWithName(
                        ConfigurationConstants.GRANULARITY_QNAME);
                configBean.setGranularity(granularity.getText());
                OMElement rowKey = cf.getFirstChildWithName(ConfigurationConstants.ROWKEY_QNAME);

                if (rowKey != null) {

                    List<KeyPart> keyParts = new ArrayList<KeyPart>();
                    Iterator partIterator = rowKey.getChildrenWithName(
                            ConfigurationConstants.PART_QNAME);

                    while (partIterator.hasNext()) {
                        Object part = partIterator.next();

                        if (part instanceof OMElement) {
                            OMElement partEl = (OMElement) part;

                            OMAttribute partName = partEl.getAttribute(ConfigurationConstants.NAME);

                            if (partName == null) {
                                throw new ConfigurationException("Part name must be present..");
                            }

                            OMAttribute storeIndex = partEl.getAttribute(
                                    ConfigurationConstants.STORE_INDEX);

                            boolean isStoreIndex = false;
                            if (storeIndex != null) {
                                try {
                                    isStoreIndex = Boolean.parseBoolean(storeIndex.
                                            getAttributeValue());
                                } catch (Exception e) {
                                    //ignored. Used default value 'false'
                                }
                            }

                            KeyPart keyPart = new KeyPart(partName.getAttributeValue(), "",  isStoreIndex);
                            keyParts.add(keyPart);

                        }

                    }
/*                String[] split = rowKey.getText().split("\\+");
                List<String> rowKeys = Arrays.asList(split);*/
                    configBean.setRowKeyParts(keyParts);

                }

                OMElement indexRowKey = cf.getFirstChildWithName(
                        ConfigurationConstants.INDEX_ROW_KEY_QNAME);

                if (indexRowKey != null) {

                    configBean.setIndexRowKey(indexRowKey.getText());

                }

                localConfigBeans.add(configBean);

            }

        }
        // this is a good practice, to prevent an exception in the middle returning an invalid config Beans object
        configBeans = localConfigBeans;
        return configBeans;
    }

}
