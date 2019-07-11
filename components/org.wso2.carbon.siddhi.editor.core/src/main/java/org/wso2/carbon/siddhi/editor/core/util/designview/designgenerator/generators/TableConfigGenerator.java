/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.siddhi.editor.core.util.designview.designgenerator.generators;

import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.StoreConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.TableConfig;
import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGenerationException;
import io.siddhi.query.api.annotation.Annotation;
import io.siddhi.query.api.definition.TableDefinition;

import java.util.ArrayList;
import java.util.List;

/**
 * Generator to create TableConfig
 */
public class TableConfigGenerator extends CodeSegmentsPreserver {
    /**
     * Generates TableConfig object from the given Siddhi TableDefinition
     *
     * @param tableDefinition Siddhi TableDefinition
     * @return TableConfig object
     */
    public TableConfig generateTableConfig(TableDefinition tableDefinition) throws DesignGenerationException {
        // 'store' and annotations
        StoreConfigGenerator storeConfigGenerator = new StoreConfigGenerator();
        AnnotationConfigGenerator annotationConfigGenerator = new AnnotationConfigGenerator();
        StoreConfig storeConfig = null;
        List<Annotation> annotationListObjects = new ArrayList<>();
        List<String> annotationList = new ArrayList<>();
        for (Annotation annotation : tableDefinition.getAnnotations()) {
            if (annotation.getName().equalsIgnoreCase("STORE")) {
                storeConfig = storeConfigGenerator.generateStoreConfig(annotation);
            } else {
                annotationList.add(annotationConfigGenerator.generateAnnotationConfig(annotation));
                annotationListObjects.add(annotation);
            }
        }
        AttributeConfigListGenerator attributeConfigListGenerator = new AttributeConfigListGenerator();
        TableConfig tableConfig = new TableConfig(tableDefinition.getId(), tableDefinition.getId(),
                attributeConfigListGenerator.generateAttributeConfigList(tableDefinition.getAttributeList()),
                storeConfig, annotationList, annotationListObjects);
        preserveCodeSegmentsOf(storeConfigGenerator, annotationConfigGenerator, attributeConfigListGenerator);
        preserveAndBindCodeSegment(tableDefinition, tableConfig);

        return tableConfig;
    }
}
