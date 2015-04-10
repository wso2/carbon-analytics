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
package org.wso2.carbon.analytics.datasource.rdbms.h2;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.naming.NamingException;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsFileSystemTest;
import org.wso2.carbon.analytics.datasource.core.fs.AnalyticsFileSystem;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;
import org.wso2.carbon.analytics.datasource.rdbms.RDBMSAnalyticsFileSystem;

/**
 * H2 implementation of analytics file system tests.
 */
public class H2FileDBAnalyticsFileSystemTest extends AnalyticsFileSystemTest {
        
    private AnalyticsFileSystem afs;
    
    public H2FileDBAnalyticsFileSystemTest() {
        System.setProperty(GenericUtils.WSO2_ANALYTICS_CONF_DIRECTORY_SYS_PROP, "src/test/resources/conf");
    }

    @BeforeClass
    public void setup() throws NamingException, AnalyticsException, IOException {
        this.afs = new RDBMSAnalyticsFileSystem();
        Map<String, String> props = new HashMap<String, String>();
        props.put("datasource", "WSO2_ANALYTICS_FS_DB");
        this.afs.init(props);
        this.init("H2FileDBAnalyticsDataSource", this.afs);
    }
    
    public AnalyticsFileSystem getAFS() {
        return this.afs;
    }
    
    @AfterClass
    public void destroy() {
        this.cleanup();
    }
    
}
