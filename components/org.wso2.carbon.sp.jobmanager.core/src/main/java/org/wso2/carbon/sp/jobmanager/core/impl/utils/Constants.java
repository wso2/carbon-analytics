/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.sp.jobmanager.core.impl.utils;

/**
 * Class to define SQL queries and constants.
 */
public class Constants {
    public static final String PERMISSION_APP_NAME = "MON";
    public static final String PERMISSION_SUFFIX_VIEWER = ".viewer";
    public static final String PERMISSION_SUFFIX_MANAGER = ".manager";
    public static final String MANAGER_KEY_GENERATOR = "_";
    public static final String MANAGERID = "MANAGERID";
    public static final String HOST = "HOST";
    public static final String PORT = "PORT";
    public static final String INTEGER_TYPE = "integerType";
    public static final String STRING_TYPE = "stringType";
    public static final String DOUBLE_TYPE = "doubleType";
    public static final String LONG_TYPE = "longType";
    public static final String FLOAT_TYPE = "floatType";
    public static final String BOOL_TYPE = "booleanType";

    private Constants() {
        //preventing initialization
    }

}
