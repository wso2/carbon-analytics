/*
 *  Copyright (c) 2016 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.datasource.cassandra;
import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

import java.util.HashMap;
import java.util.Map;

public class CassandraUtilsTest {

    @Test
    public void generateCreateKeyspaceQuerySimpleStrategyTest() throws AnalyticsException {
        Map<String, String> properties = new HashMap<>();
        properties.put("temp", "dummy");
        String query = CassandraUtils.generateCreateKeyspaceQuery("ARS", properties);
        String expectedResult = "CREATE KEYSPACE IF NOT EXISTS ARS WITH REPLICATION = {'class':'SimpleStrategy', 'replication_factor':3}";
        Assert.assertEquals(query, expectedResult);
    }

    @Test
    public void generateCreateKeyspaceQueryNetworkStrategyTest() throws AnalyticsException {
        Map<String, String> properties = new HashMap<>();
        properties.put("class", "NetworkTopologyStrategy");
        String query = CassandraUtils.generateCreateKeyspaceQuery("ARS", properties);
        String expectedResult = "CREATE KEYSPACE IF NOT EXISTS ARS WITH REPLICATION = {'class':'NetworkTopologyStrategy'}";
        Assert.assertEquals(query, expectedResult);
    }
}
