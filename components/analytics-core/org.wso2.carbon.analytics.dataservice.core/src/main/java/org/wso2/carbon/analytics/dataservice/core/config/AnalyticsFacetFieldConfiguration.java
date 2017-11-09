/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.analytics.dataservice.core.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the table level information for facets in indexing
 */
@XmlRootElement(name = "facet-field")
public class AnalyticsFacetFieldConfiguration {

    private String name;
    private String facetSplitter;
    private String facetDefaultValue;

    public AnalyticsFacetFieldConfiguration() {

    }

    @XmlAttribute(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @XmlElement(name = "facet-splitter")
    public String getFacetSplitter() {
        return facetSplitter;
    }

    public void setFacetSplitter(String facetSplitter) {
        this.facetSplitter = facetSplitter;
    }

    @XmlElement(name = "facet-default-value")
    public String getFacetDefaultValue() {
        return facetDefaultValue;
    }

    public void setFacetDefaultValue(String facetDefaultValue) {
        this.facetDefaultValue = facetDefaultValue;
    }
}
