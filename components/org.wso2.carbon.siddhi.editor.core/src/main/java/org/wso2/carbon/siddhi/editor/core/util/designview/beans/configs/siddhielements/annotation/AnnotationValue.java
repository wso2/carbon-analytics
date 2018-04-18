/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.siddhi.editor.core.util.designview.beans.configs.siddhielements.annotation;

/**
 * Represents a value of Siddhi annotation.
 * This is either treated as a string (surrounded with quotes), or not
 */
public class AnnotationValue {
    private String value;
    private boolean isString;

    public AnnotationValue(String value, boolean isString) {
        this.value = value;
        this.isString = isString;
    }

    public String getValue() {
        return value;
    }

    public boolean isString() {
        return isString;
    }
}
