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
package org.wso2.carbon.analytics.datasource.core;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.datasource.core.Record.Column;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

/**
 * This class represents the test operations related to {@link GenericUtils}.
 */
public class GenericUtilsTest {
    
    @Test
    public void testEncodeDecodeNull() throws AnalyticsDataSourceException {
        List<Column> values = new ArrayList<Record.Column>();
        values.add(new Column("C1", null));
        byte[] data = GenericUtils.encodeRecordValues(values);
        List<Column> valuesIn = GenericUtils.decodeRecordValues(data, null);
        valuesIn = GenericUtils.decodeRecordValues(data, null);
        Assert.assertEquals(new HashSet<Column>(values), new HashSet<Column>(valuesIn));
    }
    
    @Test
    public void testEncodeDecodeEmpty() throws AnalyticsDataSourceException {
        List<Column> values = new ArrayList<Record.Column>();
        byte[] data = GenericUtils.encodeRecordValues(values);
        List<Column> valuesIn = GenericUtils.decodeRecordValues(data, null);
        valuesIn = GenericUtils.decodeRecordValues(data, null);
        Assert.assertEquals(new HashSet<Column>(values), new HashSet<Column>(valuesIn));
    }
    
    @Test (expectedExceptions = AnalyticsDataSourceException.class)
    public void testDecodeCorruptedData() throws AnalyticsDataSourceException {
        byte[] data = new byte[] { 54 };
        GenericUtils.decodeRecordValues(data, null);
    }
    
    @Test (expectedExceptions = AnalyticsDataSourceException.class)
    public void testEncodeInvalidValues() throws AnalyticsDataSourceException {
        List<Column> values = new ArrayList<Record.Column>();
        values.add(new Column("C1", new BigInteger("55353")));
        GenericUtils.encodeRecordValues(values);
    }
    
    @Test
    public void testEncodeDecodeDataTypes() throws AnalyticsDataSourceException {
        List<Column> values = new ArrayList<Record.Column>();
        values.add(new Column("C1", "ABC"));
        byte[] data = GenericUtils.encodeRecordValues(values);
        List<Column> valuesIn = GenericUtils.decodeRecordValues(data, null);
        Assert.assertEquals(new HashSet<Column>(values), new HashSet<Column>(valuesIn));
        values.add(new Column("C2", "ABC2"));
        data = GenericUtils.encodeRecordValues(values);
        valuesIn = GenericUtils.decodeRecordValues(data, null);
        Assert.assertEquals(new HashSet<Column>(values), new HashSet<Column>(valuesIn));
        values.add(new Column("C3", 434));
        values.add(new Column("C4", -5501));
        values.add(new Column("C5", 4493855L));
        values.add(new Column("C6", true));
        values.add(new Column("C7", false));
        values.add(new Column("C8", 445.6));
        values.add(new Column("C9", 3.14f));
        values.add(new Column("C10", null));
        data = GenericUtils.encodeRecordValues(values);
        valuesIn = GenericUtils.decodeRecordValues(data, null);
        Assert.assertEquals(new HashSet<Column>(values), new HashSet<Column>(valuesIn));
        values.add(new Column("C11", "END"));
        data = GenericUtils.encodeRecordValues(values);
        valuesIn = GenericUtils.decodeRecordValues(data, null);
        Assert.assertEquals(new HashSet<Column>(values), new HashSet<Column>(valuesIn));
    }
    
    @Test
    public void testEncodeDecodeWithColumns() throws AnalyticsDataSourceException {
        List<Column> values = new ArrayList<Record.Column>();
        values.add(new Column("C1", "ABC"));
        values.add(new Column("C3", 434));
        values.add(new Column("C4", -5501));
        values.add(new Column("C5", 4493855L));
        values.add(new Column("C6", true));
        values.add(new Column("C7", false));
        values.add(new Column("C8", 445.6));
        values.add(new Column("C9", 3.14f));
        values.add(new Column("C10", null));
        byte[] data = GenericUtils.encodeRecordValues(values);
        Set<String> columns = new HashSet<String>();
        columns.add("C1");
        columns.add("C5");
        columns.add("C10");
        List<Column> valuesIn = GenericUtils.decodeRecordValues(data, columns);
        Assert.assertEquals(valuesIn.size(), 3);
        Set<String> columnsIn = new HashSet<String>();
        for (Column val : valuesIn) {
            columnsIn.add(val.getName());
        }
        Assert.assertEquals(columns, columnsIn);
    }
    
    @Test
    public void testEncodeDecodeDataPerf() throws AnalyticsDataSourceException {
        List<Column> cols = new ArrayList<Record.Column>();
        for (int i = 0; i < 4; i++) {
            cols.add(new Column("Column S - " + i, "OIJFFOWIJ FWOIJF EQF OIJFOIEJF EOIJFOI:JWLIFJ :WOIFJ:OIJ:OXXCW @#$#@2342323 OIJFW"));
            cols.add(new Column("Column I - " + i, (long) i));
            cols.add(new Column("Column D - " + i, i + 0.535));
        }
        long start = System.currentTimeMillis();
        int count = 100000;
        byte[] data = null;
        for (int i = 0; i < count; i++) {
            data = GenericUtils.encodeRecordValues(cols);
        }
        long end = System.currentTimeMillis();
        System.out.println("Record Encode TPS: " + (count) / (double) (end - start) * 1000.0);
        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            GenericUtils.decodeRecordValues(data, null);
        }
        end = System.currentTimeMillis();
        System.out.println("Record Decode TPS: " + (count) / (double) (end - start) * 1000.0);
    }
    
}
