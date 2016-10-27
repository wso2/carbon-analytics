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
package org.wso2.analytics.engine.core;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.Dependency;
import org.apache.spark.Partition;
import org.apache.spark.SparkContext;
import org.apache.spark.TaskContext;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.Row;
import scala.Serializable;
import scala.collection.Iterator;
import scala.collection.Seq;
import scala.reflect.ClassTag;

import java.util.List;

public class AnalyticsRDD extends RDD<Row> implements Serializable{

    private static final Log log = LogFactory.getLog(AnalyticsRDD.class);
    private static final long serialVersionUID = 5948588299500227997L;

    private String tableName;
    private String incrementalId;
    private List<String> columns;
    private long timeTo;
    private long timeFrom;
    private boolean incrementalEnabled;

    public AnalyticsRDD(String tableName, List<String> columns, long timeFrom, long timeTo, boolean incrementalEnabled, String incrementalId,
                        SparkContext _sc, Seq<Dependency<?>> deps, ClassTag<Row> evidence$1) {
        super(_sc, deps, evidence$1);
        this.tableName = tableName;
        this.timeFrom = timeFrom;
        this.timeTo = timeTo;
        this.columns = columns;
        this.incrementalEnabled = incrementalEnabled;
        this.incrementalId = incrementalId;
    }

    @Override
    public Iterator<Row> compute(Partition partition, TaskContext taskContext) {
        return null;
    }

    @Override
    public Partition[] getPartitions() {
        return new Partition[0];
    }
}
