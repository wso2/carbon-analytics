/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.analytics.dataservice.core.config;

import org.wso2.carbon.analytics.dataservice.core.indexing.AnalyticsDataIndexer;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the facet configurations of indexing
 */
@XmlRootElement(name = "facet-configuration")
public class AnalyticsFacetConfiguration {
    private boolean enabled;
    private String facetSplitter;
    private String facetDefaultValue;
    private AnalyticsFacetTableConfiguration[] facetTableConfigurations;

    public AnalyticsFacetConfiguration() {
        facetSplitter = ",";
        facetDefaultValue = AnalyticsDataIndexer.EMPTY_FACET_VALUE;
    }

    @XmlAttribute(name = "enabled", required = false)
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @XmlElement(name = "facet-splitter", defaultValue = ",")
    public String getFacetSplitter() {
        return facetSplitter;
    }

    public void setFacetSplitter(String facetSplitter) {
        this.facetSplitter = facetSplitter;
    }

    @XmlElement(name = "facet-default-value", defaultValue = AnalyticsDataIndexer.EMPTY_FACET_VALUE)
    public String getFacetDefaultValue() {
        return facetDefaultValue;
    }

    public void setFacetDefaultValue(String facetDefaultValue) {
        this.facetDefaultValue = facetDefaultValue;
    }

    @XmlElementWrapper(name = "tables")
    @XmlElement(name = "table")
    public AnalyticsFacetTableConfiguration[] getFacetTableConfigurations() {
        return facetTableConfigurations;
    }

    public void setFacetTableConfigurations(AnalyticsFacetTableConfiguration[] facetTableConfigurations) {
        this.facetTableConfigurations = facetTableConfigurations;
    }
}
