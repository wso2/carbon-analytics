/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.analytics.webservice.beans;

import java.io.Serializable;

/**
 * This class represents a single value entry in a record
 */
public class RecordValueEntryBean implements Serializable {

    private static final long serialVersionUID = -2107337898692937535L;
    private String fieldName;
    private int intValue;
    private String stringValue;
    private boolean booleanValue;
    private long longValue;
    private float floatValue;
    private double doubleValue;
    private AnalyticsCategoryPathBean analyticsCategoryPathBeanValue;
    private String value;
    private String type;

    public static final String STRING = "STRING";
    public static final String LONG = "LONG";
    public static final String FLOAT = "FLOAT";
    public static final String DOUBLE = "DOUBLE";
    public static final String BOOLEAN = "BOOLEAN";
    public static final String BINARY = "BINARY";
    public static final String INTEGER = "INTEGER";
    public static final String FACET = "FACET";

    public RecordValueEntryBean() {
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public int getIntValue() {
        return intValue;
    }

    public void setIntValue(int intValue) {
        this.value = String.valueOf(intValue);
        this.intValue = intValue;
    }

    public String getStringValue() {
        return stringValue;
    }

    public void setStringValue(String stringValue) {
        this.value = stringValue;
        this.stringValue = stringValue;
    }

    public boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(boolean booleanValue) {
        this.value = String.valueOf(booleanValue);
        this.booleanValue = booleanValue;
    }

    public long getLongValue() {
        return longValue;
    }

    public void setLongValue(long longValue) {
        this.value = String.valueOf(longValue);
        this.longValue = longValue;
    }

    public float getFloatValue() {
        return floatValue;
    }

    public void setFloatValue(float floatValue) {
        this.value = String.valueOf(floatValue);
        this.floatValue = floatValue;
    }

    public double getDoubleValue() {
        return doubleValue;
    }

    public void setDoubleValue(double doubleValue) {
        this.value = String.valueOf(doubleValue);
        this.doubleValue = doubleValue;
    }

    public AnalyticsCategoryPathBean getAnalyticsCategoryPathBeanValue() {
        return analyticsCategoryPathBeanValue;
    }

    public void setAnalyticsCategoryPathBeanValue(
            AnalyticsCategoryPathBean analyticsCategoryPathBeanValue) {
        this.analyticsCategoryPathBeanValue = analyticsCategoryPathBeanValue;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return value;
    }
}
