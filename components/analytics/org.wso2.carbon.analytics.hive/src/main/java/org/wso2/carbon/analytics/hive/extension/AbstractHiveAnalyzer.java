/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.analytics.hive.extension;

import org.apache.hadoop.hive.metastore.HiveContext;
import org.wso2.carbon.analytics.hive.exception.HiveExecutionException;

public abstract class AbstractHiveAnalyzer {

    public abstract void execute() throws HiveExecutionException;

    public void setProperty(String key, String value) {
        HiveContext.getCurrentContext().setProperty(key, value);
    }

    public String getProperty(String key) {
        return HiveContext.getCurrentContext().getProperty(key);
    }

    public void removeProperty(String key) {
        HiveContext.getCurrentContext().setProperty(key, null);
    }

}
