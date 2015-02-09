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
package org.wso2.carbon.analytics.datasource.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.testng.annotations.BeforeSuite;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.AnalyticsFileSystem;
import org.wso2.carbon.analytics.datasource.core.AnalyticsFileSystemTest;
import org.wso2.carbon.analytics.datasource.core.InMemoryICFactory;

import javax.naming.Context;
import javax.naming.NamingException;
import java.io.IOException;

public class HDFSAnalyticsFileSystemTest extends AnalyticsFileSystemTest {

    @BeforeSuite
    public void setup() throws IOException, NamingException, AnalyticsException {
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, InMemoryICFactory.class.getName());
        AnalyticsFileSystem afs = new HDFSAnalyticsFileSystem(this.initFS("hdfs", "localhost", "9000"));
        super.init("HDFSDataSource",afs);
    }

    private FileSystem initFS(String protocol, String host, String port) throws IOException, NamingException {
        Configuration config = new Configuration();
        config.set("fs.default.name", protocol + "://" + host + ":" + port);
        return FileSystem.get(config);
    }

}
