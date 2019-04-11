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

package io.siddhi.distribution.editor.core.util.designview.beans.configs.siddhielements;

import java.util.List;

/**
 * Represents configuration of a Siddhi Trigger.
 */
public class TriggerConfig extends SiddhiElementConfig {

    private String name;
    private String criteria;
    private String criteriaType;
    private List<String> annotationList;

    public TriggerConfig(String id, String name, String criteria, String criteriaType,
                         List<String> annotationList) {

        super(id);
        this.name = name;
        this.criteria = criteria;
        this.criteriaType = criteriaType;
        this.annotationList = annotationList;
    }

    public String getName() {

        return name;
    }

    public String getCriteria() {

        return criteria;
    }

    public List<String> getAnnotationList() {

        return annotationList;
    }

    public String getCriteriaType() {

        return criteriaType;
    }

    public void setTriggerCriteria(String criteria) {

        this.criteria = criteria;
    }
}
