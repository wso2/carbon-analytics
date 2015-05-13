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

package org.wso2.carbon.analytics.spark.core.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.sources.RelationProvider;
import scala.collection.immutable.Map;
import scala.runtime.AbstractFunction0;

/**
 * Created by niranda on 5/12/15.
 */
public class AnalyticsRelationProvider implements RelationProvider {

    private static final Log log = LogFactory.getLog(AnalyticsRelationProvider.class);

    /**
     * Returns a new base relation with the given parameters.
     * Note: the parameters' keywords are case insensitive and this insensitivity is enforced
     * by the Map that is passed to the function.
     *
     * @param sqlContext
     * @param parameters
     */
    @Override
    public AnalyticsRelation createRelation(SQLContext sqlContext, Map<String, String> parameters) {
        String tenantId = parameters.getOrElse(AnalyticsConstants.TENANT_ID, new AbstractFunction0<String>() {
            public String apply() {
                return "-1234";
            }
        });
        String tableName = parameters.getOrElse(AnalyticsConstants.TABLE_NAME, null);
        if (tableName == null) {
            log.error("Empty table name");
        }
        String schema = parameters.getOrElse(AnalyticsConstants.SCHEMA_STRING, null);
        if (schema == null) {
            log.error("Empty schema string");
        }
        return new AnalyticsRelation(Integer.parseInt(tenantId), tableName, sqlContext, schema);
    }

}
