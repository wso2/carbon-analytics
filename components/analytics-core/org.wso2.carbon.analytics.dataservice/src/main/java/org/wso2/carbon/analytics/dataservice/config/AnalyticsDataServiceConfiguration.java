/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.dataservice.config;

import org.wso2.carbon.analytics.dataservice.Constants;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class represents the configuration for analytics data service.
 */
@XmlRootElement (name = "analytics-dataservice-configuration")
public class AnalyticsDataServiceConfiguration {
    
    private AnalyticsRecordStoreConfiguration[] analyticsRecordStoreConfigurations;
    
    private AnalyticsFileSystemConfiguration analyticsFileSystem;
    
    private AnalyticsLuceneAnalyzerConfiguration luceneAnalyzerConfiguration;

    private AnalyticsDataPurgingConfiguration analyticsDataPurgingConfiguration;
    
    private String primaryRecordStore;
    
    private int shardCount;
    
    private int recordsBatchSize = Constants.RECORDS_BATCH_SIZE;
    
    @XmlElement (name = "analytics-record-store", nillable = false)
    public AnalyticsRecordStoreConfiguration[] getAnalyticsRecordStoreConfigurations() {
        return analyticsRecordStoreConfigurations;
    }

    public void setAnalyticsRecordStoreConfigurations(AnalyticsRecordStoreConfiguration[] analyticsRecordStoreConfigurations) {
        this.analyticsRecordStoreConfigurations = analyticsRecordStoreConfigurations;
    }

    @XmlElement (name = "analytics-file-system", nillable = false)
    public AnalyticsFileSystemConfiguration getAnalyticsFileSystemConfiguration() {
        return analyticsFileSystem;
    }

    public void setAnalyticsFileSystemConfiguration(AnalyticsFileSystemConfiguration analyticsFileSystem) {
        this.analyticsFileSystem = analyticsFileSystem;
    }
    
    @XmlElement (name = "analytics-lucene-analyzer")
	public AnalyticsLuceneAnalyzerConfiguration getLuceneAnalyzerConfiguration() {
		return luceneAnalyzerConfiguration;
	}

	public void setLuceneAnalyzerConfiguration(AnalyticsLuceneAnalyzerConfiguration luceneAnalyzerConfiguration) {
		this.luceneAnalyzerConfiguration = luceneAnalyzerConfiguration;
	}

    public AnalyticsFileSystemConfiguration getAnalyticsFileSystem() {
        return analyticsFileSystem;
    }

    public void setAnalyticsFileSystem(AnalyticsFileSystemConfiguration analyticsFileSystem) {
        this.analyticsFileSystem = analyticsFileSystem;
    }
    
    @XmlElement (nillable = false, required = true)
    public String getPrimaryRecordStore() {
        return primaryRecordStore;
    }

    public void setPrimaryRecordStore(String primaryRecordStore) {
        this.primaryRecordStore = primaryRecordStore;
    }
    
    @XmlElement (name = "shardCount", defaultValue = "6")
    public int getShardCount() {
        return shardCount;
    }
    
    public void setShardCount(int shardCount) {
        this.shardCount = shardCount;
    }

    @XmlElement (name = "recordsBatchSize", defaultValue = "1000")
    public int getRecordsBatchSize() {
        return recordsBatchSize;
    }

    public void setRecordsBatchSize(int recordsBatchSize) {
        this.recordsBatchSize = recordsBatchSize;
    }

    @XmlElement(name = "analytics-data-purging")
    public AnalyticsDataPurgingConfiguration getAnalyticsDataPurgingConfiguration() {
        return analyticsDataPurgingConfiguration;
    }

    public void setAnalyticsDataPurgingConfiguration(
            AnalyticsDataPurgingConfiguration analyticsDataPurgingConfiguration) {
        this.analyticsDataPurgingConfiguration = analyticsDataPurgingConfiguration;
    }
    
}
