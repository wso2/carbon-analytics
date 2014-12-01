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
package org.wso2.carbon.analytics.datasource.core;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingException;

import org.testng.Assert;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataService;
import org.wso2.carbon.analytics.dataservice.AnalyticsDataServiceImpl;
import org.wso2.carbon.base.MultitenantConstants;

/**
 * This class represents the analytics data service tests.
 */
public class AnalyticsDataServiceTest {

    private AnalyticsDataService service;
    
    @BeforeSuite
    public void setup() throws NamingException, AnalyticsException, IOException {
        AnalyticsDataSource ads = H2FileDBAnalyticsDataSourceTest.cleanupAndCreateDS();
        this.service = new AnalyticsDataServiceImpl(ads);
    }
    
    @Test
    public void testIndexAddRetrieve() throws AnalyticsException {
        this.indexAddRetrieve(MultitenantConstants.SUPER_TENANT_ID);
        this.indexAddRetrieve(1);
        this.indexAddRetrieve(15001);
    }
    
    private void indexAddRetrieve(int tenantId) throws AnalyticsException {
        this.service.clearIndices(tenantId, "T1");
        Set<String> columns = new HashSet<String>();
        columns.add("C1");
        columns.add("C2");
        columns.add("C3");
        service.setIndices(tenantId, "T1", columns);
        Set<String> columnsIn = service.getIndices(tenantId, "T1");
        Assert.assertEquals(columnsIn, columns);
        this.service.clearIndices(tenantId, "T1");
    }
        
}
