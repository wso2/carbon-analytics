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

package org.wso2.carbon.analytics.spark.core.udf;

import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.api.java.*;
import org.apache.spark.sql.expressions.UserDefinedAggregateFunction;
import org.wso2.carbon.analytics.spark.core.exception.AnalyticsUDFException;
import org.wso2.carbon.analytics.spark.core.udf.adaptor.*;
import org.wso2.carbon.analytics.spark.core.util.AnalyticsCommonUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * This class creates UDF adaptors and register them based on the number of parameters user has given in the
 * custom UDF class.
 */
public class AnalyticsUDFsRegister {

    private static AnalyticsUDFsRegister analyticsUDFsRegister = new AnalyticsUDFsRegister();

    private AnalyticsUDFsRegister() {
        //private constructor to prevent initialize
    }

    public static AnalyticsUDFsRegister getInstance() {
        return analyticsUDFsRegister;
    }

    public void registerUDAF(String name, Class<? extends UserDefinedAggregateFunction> udafClass, SQLContext sqlContext)
            throws AnalyticsUDFException {
        try {
            sqlContext.udf().register(name, udafClass.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            throw new AnalyticsUDFException("Error registering UDAF: " + e.getMessage(), e);
        }
    }

    /**
     * Create the UDFAdaptor for the given method name in the custom UDF class user has given in
     * the configuration
     *
     * @param udfClass   The class given in the configuration file
     * @param udfMethod  The method of the udfclass on which the UDFadaptor should be created
     * @param sqlContext The spark SQL context in which the UDFs are regsitered
     */
    public void registerUDF(Class<Object> udfClass, Method udfMethod, SQLContext sqlContext)
            throws AnalyticsUDFException {
        Class[] parameterTypes = udfMethod.getParameterTypes();
        Type returnType = udfMethod.getGenericReturnType();
        String methodName = udfMethod.getName();
        int parameterCount = parameterTypes.length;
        switch (parameterCount) {
            case 0: {
                UDF1 udfAdapter = new UDF0Adaptor(udfClass, methodName, parameterTypes);
                sqlContext.udf().register(methodName, udfAdapter, AnalyticsCommonUtils.getDataType(returnType));
                break;
            }
            case 1: {
                UDF1 udfAdapter = new UDF1Adaptor(udfClass, methodName, parameterTypes);
                sqlContext.udf().register(methodName, udfAdapter, AnalyticsCommonUtils.getDataType(returnType));
                break;
            }
            case 2: {
                UDF2 udfAdapter = new UDF2Adaptor(udfClass, methodName, parameterTypes);
                sqlContext.udf().register(methodName, udfAdapter, AnalyticsCommonUtils.getDataType(returnType));
                break;
            }
            case 3: {
                UDF3 udfAdapter = new UDF3Adaptor(udfClass, methodName, parameterTypes);
                sqlContext.udf().register(methodName, udfAdapter, AnalyticsCommonUtils.getDataType(returnType));
                break;
            }
            case 4: {
                UDF4 udfAdapter = new UDF4Adaptor(udfClass, methodName, parameterTypes);
                sqlContext.udf().register(methodName, udfAdapter, AnalyticsCommonUtils.getDataType(returnType));
                break;
            }
            case 5: {
                UDF5 udfAdapter = new UDF5Adaptor(udfClass, methodName, parameterTypes);
                sqlContext.udf().register(methodName, udfAdapter, AnalyticsCommonUtils.getDataType(returnType));
                break;
            }
            case 6: {
                UDF6 udfAdapter = new UDF6Adaptor(udfClass, methodName, parameterTypes);
                sqlContext.udf().register(methodName, udfAdapter, AnalyticsCommonUtils.getDataType(returnType));
                break;
            }
            case 7: {
                UDF7 udfAdapter = new UDF7Adaptor(udfClass, methodName, parameterTypes);
                sqlContext.udf().register(methodName, udfAdapter, AnalyticsCommonUtils.getDataType(returnType));
                break;
            }
            case 8: {
                UDF8 udfAdapter = new UDF8Adaptor(udfClass, methodName, parameterTypes);
                sqlContext.udf().register(methodName, udfAdapter, AnalyticsCommonUtils.getDataType(returnType));
                break;
            }
            case 9: {
                UDF9 udfAdapter = new UDF9Adaptor(udfClass, methodName, parameterTypes);
                sqlContext.udf().register(methodName, udfAdapter, AnalyticsCommonUtils.getDataType(returnType));
                break;
            }
            case 10: {
                UDF10 udfAdapter = new UDF10Adaptor(udfClass, methodName, parameterTypes);
                sqlContext.udf().register(methodName, udfAdapter, AnalyticsCommonUtils.getDataType(returnType));
                break;
            }
            case 11: {
                UDF11 udfAdapter = new UDF11Adaptor(udfClass, methodName, parameterTypes);
                sqlContext.udf().register(methodName, udfAdapter, AnalyticsCommonUtils.getDataType(returnType));
                break;
            }
            case 12: {
                UDF12 udfAdapter = new UDF12Adaptor(udfClass, methodName, parameterTypes);
                sqlContext.udf().register(methodName, udfAdapter, AnalyticsCommonUtils.getDataType(returnType));
                break;
            }
            case 13: {
                UDF13 udfAdapter = new UDF13Adaptor(udfClass, methodName, parameterTypes);
                sqlContext.udf().register(methodName, udfAdapter, AnalyticsCommonUtils.getDataType(returnType));
                break;
            }
            case 14: {
                UDF14 udfAdapter = new UDF14Adaptor(udfClass, methodName, parameterTypes);
                sqlContext.udf().register(methodName, udfAdapter, AnalyticsCommonUtils.getDataType(returnType));
                break;
            }
            case 15: {
                UDF15 udfAdapter = new UDF15Adaptor(udfClass, methodName, parameterTypes);
                sqlContext.udf().register(methodName, udfAdapter, AnalyticsCommonUtils.getDataType(returnType));
                break;
            }
            case 16: {
                UDF16 udfAdapter = new UDF16Adaptor(udfClass, methodName, parameterTypes);
                sqlContext.udf().register(methodName, udfAdapter, AnalyticsCommonUtils.getDataType(returnType));
                break;
            }
            case 17: {
                UDF17 udfAdapter = new UDF17Adaptor(udfClass, methodName, parameterTypes);
                sqlContext.udf().register(methodName, udfAdapter, AnalyticsCommonUtils.getDataType(returnType));
                break;
            }
            case 18: {
                UDF18 udfAdapter = new UDF18Adaptor(udfClass, methodName, parameterTypes);
                sqlContext.udf().register(methodName, udfAdapter, AnalyticsCommonUtils.getDataType(returnType));
                break;
            }
            case 19: {
                UDF19 udfAdapter = new UDF19Adaptor(udfClass, methodName, parameterTypes);
                sqlContext.udf().register(methodName, udfAdapter, AnalyticsCommonUtils.getDataType(returnType));
                break;
            }
            case 20: {
                UDF20 udfAdapter = new UDF20Adaptor(udfClass, methodName, parameterTypes);
                sqlContext.udf().register(methodName, udfAdapter, AnalyticsCommonUtils.getDataType(returnType));
                break;
            }
            case 21: {
                UDF21 udfAdapter = new UDF21Adaptor(udfClass, methodName, parameterTypes);
                sqlContext.udf().register(methodName, udfAdapter, AnalyticsCommonUtils.getDataType(returnType));
                break;
            }
            case 22: {
                UDF22 udfAdapter = new UDF22Adaptor(udfClass, methodName, parameterTypes);
                sqlContext.udf().register(methodName, udfAdapter, AnalyticsCommonUtils.getDataType(returnType));
                break;
            }
            default:
                throw new AnalyticsUDFException("Number of Parameters cannot be supported any of the UDFs");

        }
    }
}
