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

package org.wso2.carbon.analytics.spark.core.rdd;

import org.apache.spark.Dependency;
import org.apache.spark.InterruptibleIterator;
import org.apache.spark.Partition;
import org.apache.spark.SparkContext;
import org.apache.spark.TaskContext;
import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.catalyst.expressions.Row;
import org.wso2.carbon.analytics.dataservice.dummy.AnalyticsDataServiceImplDummy;
import org.wso2.carbon.analytics.datasource.core.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.Record;
import org.wso2.carbon.analytics.datasource.core.RecordGroup;
import scala.collection.Iterator;
import scala.collection.Seq;
import scala.reflect.ClassTag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static scala.collection.JavaConversions.asScalaIterator;

/**
 * Created by niranda on 12/11/14.
 */
public class CarbonRDD<T> extends RDD implements Serializable {

    AnalyticsDataServiceImplDummy dsDummy = new AnalyticsDataServiceImplDummy();

    public CarbonRDD(SparkContext sc, Seq<Dependency<?>> deps,
                     ClassTag<T> evidence$1) {
        super(sc, deps, evidence$1);
    }
    // todo: meaningful constructors

    /**
     * Construct an RDD with just a one-to-one dependency on one parent
     *
     * @param oneParent
     * @param evidence$2
     */
    public CarbonRDD(RDD<?> oneParent, ClassTag<T> evidence$2) {
        super(oneParent, evidence$2);
    }

//    public CarbonRDD(SparkContext sc) {
////        super(sc, dependencies(), null);
////    }
////
////    public CarbonRDD(RDD<?> oneParent, ClassTag evidence$2) {
////        super(oneParent, evidence$2);
////    }

    @Override
    public Iterator<T> compute(Partition split, TaskContext context) {
        //todo: take the iterator from the datasources
        List<Row> dataRows = new ArrayList<Row>();
        RecordGroup[] rgList;
        List<Record> recordList = null;
        try {
            rgList = dsDummy.get();
            RecordGroup rg = rgList[0];
            recordList = rg.getRecords();
        } catch (AnalyticsException e) {
            e.printStackTrace();
        }

        assert recordList != null;
        for (Record r : recordList) {
//            List<Object> values = new ArrayList<Object>(r.getValues().values());// = (List<Object>) r.getValues().values();
            List<Object> values = new ArrayList<Object>();// = (List<Object>) r.getValues().values();
            Map<String, Object> val = r.getValues();

            values.add(val.get("first_name"));
            values.add(val.get("last_name"));
            values.add(val.get("age"));
            Row newRow = getRow(values);
            dataRows.add(newRow);
        }
        return new InterruptibleIterator(context, asScalaIterator(dataRows.iterator()));
    }

    @Override
    public Partition[] getPartitions() {
        return new CarbonPartition[]{new CarbonPartition()};
    }

    private Row getRow(List<Object> values) {
        org.apache.spark.sql.api.java.Row newRow = org.apache.spark.sql.api.java.Row.create(values);
        return newRow.row();
    }
}
