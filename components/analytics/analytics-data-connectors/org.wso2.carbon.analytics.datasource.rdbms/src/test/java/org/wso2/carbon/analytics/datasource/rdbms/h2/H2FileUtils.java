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

import java.io.File;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

/**
 * H2 file database related utility methods.
 */
public class H2FileUtils {

    public static String generateDatabaseTempPathWithDirName(String name) {
        String dirPath = System.getProperty("java.io.tmpdir") + File.separator + name;
        File dirFile = new File(dirPath);
        if (!dirFile.exists()) {
            dirFile.mkdir();
        }
        return dirPath + File.separator + UUID.randomUUID().toString();        
    }
    
    public static void deleteDatabaseTempDir(String name) {
        File dir = new File(System.getProperty("java.io.tmpdir") + File.separator + name);
        if (dir.exists()) {
            FileUtils.deleteQuietly(dir);
        }
    }
    
}
