/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 */

package org.wso2.carbon.analytics.spark.core.exception;

import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

/**
 * This class represents the exceptions occurred during registering custom UDF class registering
 */
public class AnalyticsUDFException extends AnalyticsException {


    private static final long serialVersionUID = 6255792058064362273L;

    public AnalyticsUDFException(String msg) {
        super(msg);
    }

    public AnalyticsUDFException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
