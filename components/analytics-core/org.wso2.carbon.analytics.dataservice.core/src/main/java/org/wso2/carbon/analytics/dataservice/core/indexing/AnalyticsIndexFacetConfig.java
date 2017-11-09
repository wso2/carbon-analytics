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

package org.wso2.carbon.analytics.dataservice.core.indexing;

import org.wso2.carbon.analytics.dataservice.core.config.AnalyticsFacetConfiguration;
import org.wso2.carbon.analytics.dataservice.core.config.AnalyticsFacetFieldConfiguration;
import org.wso2.carbon.analytics.dataservice.core.config.AnalyticsFacetTableConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * This class contains all the configurations required for facets in Indexing, extracted from bean classes
 */
public class AnalyticsIndexFacetConfig {
    private String facetSplitter;
    private boolean enabled;
    private String facetDefaultValue;
    private Map<String,PerTableFacetConfig> perTableFacetConfigs;

    public AnalyticsIndexFacetConfig(AnalyticsFacetConfiguration facetConfig) {
        this.perTableFacetConfigs = new HashMap<>(0);
        if (facetConfig != null) {
            this.enabled = facetConfig.isEnabled();
            if (this.enabled) {
                this.facetSplitter = facetConfig.getFacetSplitter();
                this.facetDefaultValue = facetConfig.getFacetDefaultValue();
                setFacetTableConfigs(facetConfig.getFacetTableConfigurations());
            } else {
                this.facetSplitter = ",";
                this.facetDefaultValue = AnalyticsDataIndexer.EMPTY_FACET_VALUE;
            }
        } else {
            this.enabled = false;
            this.facetSplitter = ",";
            this.facetDefaultValue = AnalyticsDataIndexer.EMPTY_FACET_VALUE;
        }
    }

    private void setFacetTableConfigs(AnalyticsFacetTableConfiguration[] analyticsTableFacetConfigs) {
        if (analyticsTableFacetConfigs != null) {
            for (AnalyticsFacetTableConfiguration analyticsTableFacetConfig : analyticsTableFacetConfigs) {
                String tableName = analyticsTableFacetConfig.getName();
                if (tableName != null && !tableName.isEmpty()) {
                    PerTableFacetConfig perTableFacetConfig = new PerTableFacetConfig(analyticsTableFacetConfig);
                    this.perTableFacetConfigs.put(tableName, perTableFacetConfig);
                }
            }
        }
    }

    public String getFacetSplitter(String tableName, String fieldName) {
        PerTableFacetConfig perTableFacetConfig = perTableFacetConfigs.get(tableName);
        if (perTableFacetConfig == null) {
            return facetSplitter;
        } else {
            return perTableFacetConfig.getFacetSplitter(fieldName);
        }
    }

    public String getFacetDefaultValue(String tableName, String fieldName) {
        PerTableFacetConfig perTableFacetConfig = perTableFacetConfigs.get(tableName);
        if (perTableFacetConfig == null) {
            return facetDefaultValue;
        } else {
            return perTableFacetConfig.getFacetDefaultValue(fieldName);
        }
    }

    public String getFacetSplitter() {
        return facetSplitter;
    }

    public String getFacetDefaultValue() {
        return facetDefaultValue;
    }


    public boolean isEnabled() {
        return enabled;
    }

    /**
     * This class contains the facet info per table in facet configuration object
     */
    private class PerTableFacetConfig {
        private String facetSplitter;
        private String facetDefaultValue;
        private Map<String, PerFieldFacetConfig> perFieldFacetConfigs;

        public PerTableFacetConfig(AnalyticsFacetTableConfiguration facetTableConfig) {
            this.perFieldFacetConfigs = new HashMap<>(0);
            if (facetTableConfig != null) {
                this.facetDefaultValue = facetTableConfig.getFacetDefaultValue();
                this.facetSplitter = facetTableConfig.getFacetSplitter();
                setFacetFieldConfigs(facetTableConfig);
            }
            if (this.facetDefaultValue == null || this.facetDefaultValue.isEmpty()) {
                this.facetDefaultValue = AnalyticsIndexFacetConfig.this.getFacetDefaultValue();
            }
            if (this.facetSplitter == null || this.facetSplitter.isEmpty()) {
                this.facetSplitter = AnalyticsIndexFacetConfig.this.getFacetSplitter();
            }
        }

        private void setFacetFieldConfigs(AnalyticsFacetTableConfiguration facetTableConfig) {
            AnalyticsFacetFieldConfiguration[] facetFieldConfigs = facetTableConfig.getFacetFieldConfigurations();
            if (facetFieldConfigs != null) {
                for (AnalyticsFacetFieldConfiguration facetFieldConfig : facetFieldConfigs) {
                    String fieldName = facetFieldConfig.getName();
                    if (fieldName != null && !fieldName.isEmpty()) {
                        PerFieldFacetConfig perFieldFacetConfig = new PerFieldFacetConfig(facetFieldConfig);
                        this.perFieldFacetConfigs.put(fieldName, perFieldFacetConfig);
                    }
                }
            }
        }

        public String getFacetSplitter(String fieldName) {
            PerFieldFacetConfig perFieldFacetConfig = perFieldFacetConfigs.get(fieldName);
            if (perFieldFacetConfig == null) {
                return facetSplitter;
            } else {
                return perFieldFacetConfig.getFacetSplitter();
            }
        }

        public String getFacetDefaultValue(String fieldName) {
            PerFieldFacetConfig perFieldFacetConfig = perFieldFacetConfigs.get(fieldName);
            if (perFieldFacetConfig == null) {
                return facetDefaultValue;
            } else {
                return perFieldFacetConfig.getFacetDefaultValue();
            }
        }

        public String getFacetSplitter() {
            return facetSplitter;
        }

        public String getFacetDefaultValue() {
            return facetDefaultValue;
        }

        /**
         * this table contains the facet info related to each field in a table
         */
        private class PerFieldFacetConfig {
            private String facetSplitter;
            private String facetDefaultValue;

            public PerFieldFacetConfig(AnalyticsFacetFieldConfiguration facetFieldConfig) {
                if (facetFieldConfig != null) {
                    this.facetDefaultValue = facetFieldConfig.getFacetDefaultValue();
                    this.facetSplitter = facetFieldConfig.getFacetSplitter();
                }
                if (this.facetSplitter == null || this.facetSplitter.isEmpty()) {
                    this.facetSplitter = PerTableFacetConfig.this.getFacetSplitter();
                }
                if (this.facetDefaultValue == null || this.facetDefaultValue.isEmpty()) {
                    this.facetDefaultValue = PerTableFacetConfig.this.getFacetDefaultValue();
                }
            }

            public String getFacetSplitter() {
                return facetSplitter;
            }

            public String getFacetDefaultValue() {
                return facetDefaultValue;
            }

        }
    }
}
