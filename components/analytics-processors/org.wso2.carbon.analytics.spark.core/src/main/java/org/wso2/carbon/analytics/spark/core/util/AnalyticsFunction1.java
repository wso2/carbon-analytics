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

import org.apache.spark.sql.Row;
import org.apache.spark.sql.types.StructType;
import org.wso2.carbon.analytics.datasource.commons.Record;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.spark.core.internal.ServiceHolder;
import scala.collection.Iterator;
import scala.runtime.AbstractFunction1;
import scala.runtime.BoxedUnit;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AnalyticsFunction1 extends AbstractFunction1<Iterator<Row>, BoxedUnit>
        implements Serializable {

    private static final long serialVersionUID = -1919222653470217466L;
    private int tId;
    private String tName;
    private StructType sch;

    public AnalyticsFunction1(int tId, String tName, StructType sch) {
        this.tId = tId;
        this.tName = tName;
        this.sch = sch;
    }

    /**
     * Apply the body of this function to the argument.
     *
     * @param iterator
     * @return the result of function application.
     */
    @Override
    public BoxedUnit apply(Iterator<Row> iterator) {
        List<Record> records = new ArrayList<>();
        while (iterator.hasNext()) {
            if (records.size() == AnalyticsConstants.MAX_RECORDS) {
                try {
                    ServiceHolder.getAnalyticsDataService().put(records);
                } catch (AnalyticsException e) {
//                                log.error("Error while inserting data into table " + tableName, e);
                    e.printStackTrace();
                }
                records.clear();
            } else {
                Row row = iterator.next();
                records.add(new Record(this.tId, this.tName,
                                       convertRowAndSchemaToValuesMap(row, this.sch)));
            }
        }

        if (!records.isEmpty()) {
            try {
                ServiceHolder.getAnalyticsDataService().put(records);
            } catch (AnalyticsException e) {
//                                log.error("Error while inserting data into table " + tableName, e);
                e.printStackTrace();
            }
        }
        return BoxedUnit.UNIT;
    }

    private Map<String, Object> convertRowAndSchemaToValuesMap(Row row, StructType schema) {
        String[] colNames = schema.fieldNames();
        Map<String, Object> result = new HashMap<>();
        for (int i = 0; i < row.length(); i++) {
            result.put(colNames[i], row.get(i));
        }
        return result;
    }

}
