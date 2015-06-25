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

package org.wso2.carbon.analytics.spark.core.udf.adaptor;

import org.apache.spark.sql.api.java.UDF1;
import org.apache.spark.sql.types.DataType;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsUDFException;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsCommonUtils;

import java.lang.reflect.Method;

/**
 * This class represents custom UDF type 1 adaptor
 */
public class UDF1Adaptor implements UDF1 {

    private static final long serialVersionUID = -2485065879318027533L;
    private Class<Object> udfClass;
    private String udfMethodName;
    private Class[] parameterTypes;

    public UDF1Adaptor(Class<Object> udfClass, String udfMethodName, Class[] parameterTypes)
            throws AnalyticsUDFException {
        this.udfClass = udfClass;
        this.udfMethodName = udfMethodName;
        this.parameterTypes = parameterTypes;

    }

    @Override
    public Object call(Object o) throws Exception {
        Object udfInstance = udfClass.newInstance();
        Method udfMethod = udfClass.getDeclaredMethod(udfMethodName, parameterTypes);
        return udfMethod.invoke(udfInstance, o);
    }
}
