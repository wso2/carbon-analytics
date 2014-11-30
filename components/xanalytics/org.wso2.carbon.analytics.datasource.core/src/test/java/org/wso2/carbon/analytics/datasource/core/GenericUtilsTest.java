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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

/**
 * This class represents the test operations related to {@link GenericUtils}.
 */
public class GenericUtilsTest {
    
    @Test
    public void testEncodeDecodeNull() throws AnalyticsDataSourceException {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("C1", null);
        byte[] data = GenericUtils.encodeRecordValues(values);
        Map<String, Object> valuesIn = GenericUtils.decodeRecordValues(data, null);
        valuesIn = GenericUtils.decodeRecordValues(data, null);
        Assert.assertEquals(values, valuesIn);
    }
    
    @Test
    public void testEncodeDecodeEmpty() throws AnalyticsDataSourceException {
        Map<String, Object> values = new HashMap<String, Object>();
        byte[] data = GenericUtils.encodeRecordValues(values);
        Map<String, Object> valuesIn = GenericUtils.decodeRecordValues(data, null);
        valuesIn = GenericUtils.decodeRecordValues(data, null);
        Assert.assertEquals(values, valuesIn);
    }
    
    @Test (expectedExceptions = AnalyticsDataSourceException.class)
    public void testDecodeCorruptedData() throws AnalyticsDataSourceException {
        byte[] data = new byte[] { 54 };
        GenericUtils.decodeRecordValues(data, null);
    }
    
    @Test (expectedExceptions = AnalyticsDataSourceException.class)
    public void testEncodeInvalidValues() throws AnalyticsDataSourceException {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("C1", new BigInteger("55353"));
        GenericUtils.encodeRecordValues(values);
    }
    
    @Test
    public void testEncodeDecodeDataTypes() throws AnalyticsDataSourceException {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("C1", "ABC");
        byte[] data = GenericUtils.encodeRecordValues(values);
        Map<String, Object> valuesIn = GenericUtils.decodeRecordValues(data, null);
        Assert.assertEquals(values, valuesIn);
        values.put("C2", "ABC2");
        data = GenericUtils.encodeRecordValues(values);
        valuesIn = GenericUtils.decodeRecordValues(data, null);
        Assert.assertEquals(values, valuesIn);
        values.put("C3", 434);
        values.put("C4", -5501);
        values.put("C5", 4493855L);
        values.put("C6", true);
        values.put("C7", false);
        values.put("C8", 445.6);
        values.put("C9", 3.14f);
        values.put("C10", null);
        data = GenericUtils.encodeRecordValues(values);
        valuesIn = GenericUtils.decodeRecordValues(data, null);
        Assert.assertEquals(values, valuesIn);
        values.put("C11", "END");
        data = GenericUtils.encodeRecordValues(values);
        valuesIn = GenericUtils.decodeRecordValues(data, null);
        Assert.assertEquals(values, valuesIn);
    }
    
    @Test
    public void testEncodeDecodeWithColumns() throws AnalyticsDataSourceException {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("C1", "ABC");
        values.put("C3", 434);
        values.put("C4", -5501);
        values.put("C5", 4493855L);
        values.put("C6", true);
        values.put("C7", false);
        values.put("C8", 445.6);
        values.put("C9", 3.14f);
        values.put("C10", null);
        byte[] data = GenericUtils.encodeRecordValues(values);
        Set<String> columns = new HashSet<String>();
        columns.add("C1");
        columns.add("C5");
        columns.add("C10");
        Map<String, Object> valuesIn = GenericUtils.decodeRecordValues(data, columns);
        Assert.assertEquals(valuesIn.size(), 3);
        Set<String> columnsIn = new HashSet<String>();
        for (String col : valuesIn.keySet()) {
            columnsIn.add(col);
        }
        Assert.assertEquals(columns, columnsIn);
    }
    
    @Test
    public void testEncodeDecodeDataPerf() throws AnalyticsDataSourceException {
        Map<String, Object> cols = new HashMap<String, Object>();
        for (int i = 0; i < 4; i++) {
            cols.put("Column S - " + i, "OIJFFOWIJ FWOIJF EQF OIJFOIEJF EOIJFOI:JWLIFJ :WOIFJ:OIJ:OXXCW @#$#@2342323 OIJFW");
            cols.put("Column I - " + i, (long) i);
            cols.put("Column D - " + i, i + 0.535);
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
