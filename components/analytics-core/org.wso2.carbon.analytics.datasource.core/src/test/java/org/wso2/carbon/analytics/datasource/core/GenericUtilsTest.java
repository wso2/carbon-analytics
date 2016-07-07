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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;
import org.wso2.carbon.analytics.datasource.core.util.GenericUtils;

/**
 * This class represents the test operations related to {@link GenericUtils}.
 */
public class GenericUtilsTest {
    
    @Test
    public void testEncodeDecodeNull() throws AnalyticsException {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("C1", null);
        byte[] data = GenericUtils.encodeRecordValues(values);
        Map<String, Object> valuesIn = GenericUtils.decodeRecordValues(data, null);
        valuesIn = GenericUtils.decodeRecordValues(data, null);
        Assert.assertEquals(values, valuesIn);
    }
    
    @Test
    public void testEncodeDecodeEmpty() throws AnalyticsException {
        Map<String, Object> values = new HashMap<String, Object>();
        byte[] data = GenericUtils.encodeRecordValues(values);
        Map<String, Object> valuesIn = GenericUtils.decodeRecordValues(data, null);
        valuesIn = GenericUtils.decodeRecordValues(data, null);
        Assert.assertEquals(values, valuesIn);
    }
    
    @Test (expectedExceptions = AnalyticsException.class)
    public void testDecodeCorruptedData() throws AnalyticsException {
        byte[] data = new byte[] { 54 };
        GenericUtils.decodeRecordValues(data, null);
    }
    
    private byte[] generateBinaryData(int size) {
        byte[] data = new byte[size];
        for (int i = 0; i < size; i++) {
            data[i] = (byte) (Math.random() * Byte.MAX_VALUE);
        }
        return data;
    }
    
    @Test
    public void testEncodeDecodeDataTypes() throws AnalyticsException {
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
        values.put("C11", this.generateBinaryData(1000));
        data = GenericUtils.encodeRecordValues(values);
        valuesIn = GenericUtils.decodeRecordValues(data, null);
        Assert.assertEquals(values, valuesIn);
        values.put("C12", "END");
        data = GenericUtils.encodeRecordValues(values);
        valuesIn = GenericUtils.decodeRecordValues(data, null);
        Assert.assertEquals(values, valuesIn);
    }
    
    @Test
    public void testEncodeDecodeWithColumns() throws AnalyticsException {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("C1", "ABC");
        values.put("C3", 434);
        values.put("C4", -5501);
        values.put("C5", 4493855L);
        values.put("CB", this.generateBinaryData(1000));
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
    public void testEncodeDecodeDataPerf() throws AnalyticsException {
        Map<String, Object> cols = new HashMap<String, Object>();
        for (int i = 0; i < 4; i++) {
            cols.put("Column S - " + i, "OIJFFOWIJ FWOIJF EQF OIJFOIEJF EOIJFOI:JWLIFJ :WOIFJ:OIJ:OXXCW @#$#@2342323 OIJFW");
            cols.put("Column I - " + i, (long) i);
            cols.put("Column D - " + i, i + 0.535);
        }
        long start = System.currentTimeMillis();
        int count = 200000;
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
    
    @Test
    public void testObjectSerializeDeserializeOne() throws IOException {
        BigDecimal obj = new BigDecimal(54522.6420);
        byte[] data = GenericUtils.serializeObject(obj);
        BigDecimal objIn = (BigDecimal) GenericUtils.deserializeObject(data);
        Assert.assertEquals(obj, objIn);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        GenericUtils.serializeObject(obj, byteOut);
        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        objIn = (BigDecimal) GenericUtils.deserializeObject(byteIn);
        Assert.assertEquals(obj, objIn);
    }
    
    @Test
    public void testObjectSerializeDeserializeMultiple() throws IOException {
        BigDecimal obj1 = new BigDecimal(Math.E);
        BigDecimal obj2 = new BigDecimal(Math.PI);
        BigDecimal obj3 = new BigDecimal(4491.99014);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        GenericUtils.serializeObject(obj1, byteOut);
        GenericUtils.serializeObject(obj2, byteOut);
        GenericUtils.serializeObject(obj3, byteOut);
        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        BigDecimal objIn1 = (BigDecimal) GenericUtils.deserializeObject(byteIn);
        BigDecimal objIn2 = (BigDecimal) GenericUtils.deserializeObject(byteIn);
        BigDecimal objIn3 = (BigDecimal) GenericUtils.deserializeObject(byteIn);
        Assert.assertEquals(obj1, objIn1);
        Assert.assertEquals(obj2, objIn2);
        Assert.assertEquals(obj3, objIn3);
    }
    
    @Test (expectedExceptions = EOFException.class)
    public void testObjectSerializeDeserializeMultipleWithEOF() throws IOException, EOFException {
        BigDecimal obj1 = new BigDecimal(Math.E);
        BigDecimal obj2 = new BigDecimal(Math.PI);
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        GenericUtils.serializeObject(obj1, byteOut);
        GenericUtils.serializeObject(obj2, byteOut);
        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        BigDecimal objIn1 = (BigDecimal) GenericUtils.deserializeObject(byteIn);
        BigDecimal objIn2 = (BigDecimal) GenericUtils.deserializeObject(byteIn);
        Assert.assertEquals(obj1, objIn1);
        Assert.assertEquals(obj2, objIn2);
        GenericUtils.deserializeObject(byteIn);
    }
    
    @Test
    public void testSerializeDeserializeObjectPerf() throws AnalyticsException, IOException {
        List<Integer> obj = new ArrayList<Integer>(1);
        obj.add(123);
        long start = System.currentTimeMillis();
        int count = 200000;
        byte[] data = null;
        for (int i = 0; i < count; i++) {
            data = GenericUtils.serializeObject(obj);
        }
        long end = System.currentTimeMillis();
        System.out.println("Object Serialize TPS: " + (count) / (double) (end - start) * 1000.0);
        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            GenericUtils.deserializeObject(data);
        }
        end = System.currentTimeMillis();
        System.out.println("Object Deserialize Byte[] TPS: " + (count) / (double) (end - start) * 1000.0);
        start = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            GenericUtils.deserializeObject(new ByteArrayInputStream(data));
        }
        end = System.currentTimeMillis();
        System.out.println("Object Deserialize Input Stream TPS: " + (count) / (double) (end - start) * 1000.0);
    }
    
    @Test (expectedExceptions = Exception.class)
    public void testCheckAndReturnPathInvalid1() throws Exception {
        GenericUtils.checkAndReturnPath("abc/../../xyz");
    }
    
    @Test (expectedExceptions = Exception.class)
    public void testCheckAndReturnPathInvalid2() throws Exception {
        GenericUtils.checkAndReturnPath("../xyz");
    }
    
    @Test (expectedExceptions = Exception.class)
    public void testCheckAndReturnPathInvalid3() throws Exception {
        GenericUtils.checkAndReturnPath("..\\xyz");
    }
    
    @Test (expectedExceptions = Exception.class)
    public void testCheckAndReturnPathInvalid4() throws Exception {
        GenericUtils.checkAndReturnPath("abc\\..\\..\\xyz");
    }
    
    @Test
    public void testCheckAndReturnPathValid1() throws Exception {
        Assert.assertEquals(GenericUtils.checkAndReturnPath("abc"), "abc");
    }
    
    @Test
    public void testCheckAndReturnPathValid2() throws Exception {
        Assert.assertEquals(GenericUtils.checkAndReturnPath("abc/x/y/x"), "abc/x/y/x");
    }
    
    @Test
    public void testCheckAndReturnPathValid3() throws Exception {
        Assert.assertEquals(GenericUtils.checkAndReturnPath("abc\\x\\y\\x"), "abc\\x\\y\\x");
    }
    
    @Test
    public void testCheckAndReturnPathValid4() throws Exception {
        Assert.assertEquals(GenericUtils.checkAndReturnPath("abc.txt"), "abc.txt");
    }
    
    @Test
    public void testCheckAndReturnPathValid5() throws Exception {
        Assert.assertEquals(GenericUtils.checkAndReturnPath("/xyz/abc.txt"), "/xyz/abc.txt");
    }
    
    @Test
    public void testCheckAndReturnPathValid6() throws Exception {
        Assert.assertEquals(GenericUtils.checkAndReturnPath("\\xyz\\abc.txt"), "\\xyz\\abc.txt");
    }
    
}
