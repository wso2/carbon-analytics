/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.analytics.spark.util;

import org.apache.spark.sql.api.java.JavaSQLContext;
import org.apache.spark.sql.api.java.JavaSchemaRDD;
import org.wso2.carbon.analytics.spark.core.sources.CarbonDatasourceRelation;

/**
 * A colleciton of operations dealing with the WSO2 dataservice tables
 * Created by niranda on 1/14/15.
 */
public class TableOperations {

    /**
     * Reads a table from the underlying datasource and returns a JavaSchemaRDD
     */
    // todo: change the JavaSchemaRDD to CarbonJavaSchemaRDD?
    public static JavaSchemaRDD getTableRDD(JavaSQLContext javaSQLContext, String tableName) {
        CarbonDatasourceRelation relation = new CarbonDatasourceRelation(javaSQLContext, tableName);
        return javaSQLContext.baseRelationToSchemaRDD(relation);
    }

}
