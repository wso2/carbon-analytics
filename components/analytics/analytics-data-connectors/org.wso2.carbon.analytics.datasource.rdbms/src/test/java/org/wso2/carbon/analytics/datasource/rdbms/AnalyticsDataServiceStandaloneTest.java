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

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataServiceImpl;
import org.wso2.carbon.analytics.dataservice.AnalyticsServiceHolder;
import org.wso2.carbon.analytics.dataservice.clustering.AnalyticsClusterManagerImpl;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.fs.AnalyticsFileSystem;
import org.wso2.carbon.analytics.datasource.core.rs.AnalyticsRecordStore;
import org.wso2.carbon.analytics.datasource.rdbms.h2.H2MemDBAnalyticsFileSystemTest;
import org.wso2.carbon.analytics.datasource.rdbms.h2.H2MemDBAnalyticsRecordStoreTest;

/**
 * Standalone test implementation of {@link AnalyticsDataServiceTest}.
 */
public class AnalyticsDataServiceStandaloneTest extends AnalyticsDataServiceTest {

    private H2MemDBAnalyticsRecordStoreTest h2arstest;
    
    private H2MemDBAnalyticsFileSystemTest h2afstest;
    
    @BeforeClass
    public void setup() throws NamingException, AnalyticsException, IOException {
        this.h2arstest = new H2MemDBAnalyticsRecordStoreTest();
        this.h2afstest = new H2MemDBAnalyticsFileSystemTest();
        this.h2arstest.setup();
        this.h2afstest.setup();
        AnalyticsRecordStore ars = this.h2arstest.getARS();
        AnalyticsFileSystem afs = this.h2afstest.getAFS();
        AnalyticsServiceHolder.setHazelcastInstance(null);
        AnalyticsServiceHolder.setAnalyticsClusterManager(new AnalyticsClusterManagerImpl());
        this.init(new AnalyticsDataServiceImpl(ars, afs, 6));
    }
    
    @AfterClass
    public void done() throws NamingException, AnalyticsException, IOException {
        this.service.destroy();
        this.h2arstest.destroy();
        this.h2afstest.destroy();
    }
    
}
