/*
*  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.analytics.datasource.hbase;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.junit.AfterClass;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsRecordStoreTest;

import java.io.IOException;

public class HBaseAnalyticsRecordStoreTest extends AnalyticsRecordStoreTest {

    private HBaseAnalyticsRecordStore store;

    @BeforeClass
    public void setup() throws AnalyticsException, IOException {
        this.store = new HBaseAnalyticsRecordStore(this.init(), this.createConfig());
        super.init("HBaseAnalyticsRecordStore", store);
    }

    private Connection init() throws IOException {
        Configuration config = new Configuration();
        config.set("hbase.master", "localhost:60000");
        return ConnectionFactory.createConnection(config);
    }

    private HBaseAnalyticsConfigurationEntry createConfig() {
        HBaseAnalyticsConfigurationEntry entry = new HBaseAnalyticsConfigurationEntry();
        entry.setBatchSize(5000);
        return entry;
    }

    @AfterClass
    public void destroy() throws AnalyticsException {
        if (this.store != null) {
            this.store.destroy();
        }
    }

    public HBaseAnalyticsRecordStore getStore() {
        return store;
    }

}
