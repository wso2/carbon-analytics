/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.datasource.rdbms;

import java.io.IOException;

import javax.naming.NamingException;

import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataServiceImpl;
import org.wso2.carbon.analytics.dataservice.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.clustering.AnalyticsClusterManagerImpl;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsFileSystem;
import org.wso2.carbon.analytics.datasource.core.AnalyticsRecordStore;

import com.hazelcast.core.Hazelcast;

/**
 * Clustered test implementation of {@link AnalyticsDataServiceTest}.
 */
public class AnalyticsDataServiceClusteredTest extends AnalyticsDataServiceTest {

    @BeforeSuite
    public void setup() throws NamingException, AnalyticsException, IOException {
        Hazelcast.shutdownAll();
        AnalyticsRecordStore ars = H2FileDBAnalyticsRecordStoreTest.cleanupAndCreateARS();
        AnalyticsFileSystem afs = H2FileDBAnalyticsFileSystemTest.cleanupAndCreateAFS();
        AnalyticsServiceHolder.setHazelcastInstance(Hazelcast.newHazelcastInstance());
        AnalyticsServiceHolder.setAnalyticsClusterManager(new AnalyticsClusterManagerImpl());
        this.init(new AnalyticsDataServiceImpl(ars, afs));
    }
    
    @AfterSuite
    public void done() throws NamingException, AnalyticsException, IOException {
        this.service.destroy();
        AnalyticsServiceHolder.setAnalyticsClusterManager(null);
        Hazelcast.shutdownAll();
    }
    
}
