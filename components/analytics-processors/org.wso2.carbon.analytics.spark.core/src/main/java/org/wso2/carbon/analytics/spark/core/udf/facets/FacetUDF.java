/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.analytics.spark.core.udf.facets;

/**
 * Represents the Facet UDF.
 * Provides Facet functionality to sparkSQL queries.
 * ex: select x,y,facet2(x,y) from test
 * This will return the corresponding String, concatenating the values of the objects parsed in with commas.
 *
 * TODO: remove unnecessary naming convention for different number of arguments
 * once SparkSQL supports generic UDFs
 */
public class FacetUDF {

    public String facet1(Object facet_1) {
        return FacetUtils.getFacetString(facet_1);
    }

    public String facet2(Object facet_1, Object facet_2) {
        return FacetUtils.getFacetString(facet_1, facet_2);
    }

    public String facet3(Object facet_1, Object facet_2, Object facet_3) {
        return FacetUtils.getFacetString(facet_1, facet_2, facet_3);
    }

    public String facet4(Object facet_1, Object facet_2, Object facet_3, Object facet_4) {
        return FacetUtils.getFacetString(facet_1, facet_2, facet_3, facet_4);
    }

    public String facet5(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5) {
        return FacetUtils.getFacetString(facet_1, facet_2, facet_3, facet_4, facet_5);
    }

    public String facet6(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
            Object facet_6) {
        return FacetUtils.getFacetString(facet_1, facet_2, facet_3, facet_4, facet_5, facet_6);
    }

    public String facet7(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5, Object facet_6,
            Object facet_7) {
        return FacetUtils.getFacetString(facet_1, facet_2, facet_3, facet_4, facet_5, facet_6, facet_7);
    }

    public String facet8(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5, Object facet_6,
            Object facet_7, Object facet_8) {
        return FacetUtils.getFacetString(facet_1, facet_2, facet_3, facet_4, facet_5, facet_6, facet_7, facet_8);
    }

    public String facet9(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5, Object facet_6,
            Object facet_7, Object facet_8, Object facet_9) {
        return FacetUtils
                .getFacetString(facet_1, facet_2, facet_3, facet_4, facet_5, facet_6, facet_7, facet_8, facet_9);
    }

    public String facet10(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
            Object facet_6, Object facet_7, Object facet_8, Object facet_9, Object facet_10) {
        return FacetUtils
                .getFacetString(facet_1, facet_2, facet_3, facet_4, facet_5, facet_6, facet_7, facet_8, facet_9,
                        facet_10);
    }

    public String facet11(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
            Object facet_6, Object facet_7, Object facet_8, Object facet_9, Object facet_10, Object facet_11) {
        return FacetUtils
                .getFacetString(facet_1, facet_2, facet_3, facet_4, facet_5, facet_6, facet_7, facet_8, facet_9,
                        facet_10, facet_11);
    }

    public String facet12(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
            Object facet_6, Object facet_7, Object facet_8, Object facet_9, Object facet_10, Object facet_11,
            Object facet_12) {
        return FacetUtils
                .getFacetString(facet_1, facet_2, facet_3, facet_4, facet_5, facet_6, facet_7, facet_8, facet_9,
                        facet_10, facet_11, facet_12);
    }

    public String facet13(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
            Object facet_6, Object facet_7, Object facet_8, Object facet_9, Object facet_10, Object facet_11,
            Object facet_12, Object facet_13) {
        return FacetUtils
                .getFacetString(facet_1, facet_2, facet_3, facet_4, facet_5, facet_6, facet_7, facet_8, facet_9,
                        facet_10, facet_11, facet_12, facet_13);
    }

    public String facet14(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
            Object facet_6, Object facet_7, Object facet_8, Object facet_9, Object facet_10, Object facet_11,
            Object facet_12, Object facet_13, Object facet_14) {
        return FacetUtils
                .getFacetString(facet_1, facet_2, facet_3, facet_4, facet_5, facet_6, facet_7, facet_8, facet_9,
                        facet_10, facet_11, facet_12, facet_13, facet_14);
    }

    public String facet15(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
            Object facet_6, Object facet_7, Object facet_8, Object facet_9, Object facet_10, Object facet_11,
            Object facet_12, Object facet_13, Object facet_14, Object facet_15) {
        return FacetUtils
                .getFacetString(facet_1, facet_2, facet_3, facet_4, facet_5, facet_6, facet_7, facet_8, facet_9,
                        facet_10, facet_11, facet_12, facet_13, facet_14, facet_15);
    }

    public String facet16(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
            Object facet_6, Object facet_7, Object facet_8, Object facet_9, Object facet_10, Object facet_11,
            Object facet_12, Object facet_13, Object facet_14, Object facet_15, Object facet_16) {
        return FacetUtils
                .getFacetString(facet_1, facet_2, facet_3, facet_4, facet_5, facet_6, facet_7, facet_8, facet_9,
                        facet_10, facet_11, facet_12, facet_13, facet_14, facet_15, facet_16);
    }

    public String facet17(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
            Object facet_6, Object facet_7, Object facet_8, Object facet_9, Object facet_10, Object facet_11,
            Object facet_12, Object facet_13, Object facet_14, Object facet_15, Object facet_16, Object facet_17) {
        return FacetUtils
                .getFacetString(facet_1, facet_2, facet_3, facet_4, facet_5, facet_6, facet_7, facet_8, facet_9,
                        facet_10, facet_11, facet_12, facet_13, facet_14, facet_15, facet_16, facet_17);
    }

    public String facet18(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
            Object facet_6, Object facet_7, Object facet_8, Object facet_9, Object facet_10, Object facet_11,
            Object facet_12, Object facet_13, Object facet_14, Object facet_15, Object facet_16, Object facet_17,
            Object facet_18) {
        return FacetUtils
                .getFacetString(facet_1, facet_2, facet_3, facet_4, facet_5, facet_6, facet_7, facet_8, facet_9,
                        facet_10, facet_11, facet_12, facet_13, facet_14, facet_15, facet_16, facet_17, facet_18);
    }

    public String facet19(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
            Object facet_6, Object facet_7, Object facet_8, Object facet_9, Object facet_10, Object facet_11,
            Object facet_12, Object facet_13, Object facet_14, Object facet_15, Object facet_16, Object facet_17,
            Object facet_18, Object facet_19) {
        return FacetUtils
                .getFacetString(facet_1, facet_2, facet_3, facet_4, facet_5, facet_6, facet_7, facet_8, facet_9,
                        facet_10, facet_11, facet_12, facet_13, facet_14, facet_15, facet_16, facet_17, facet_18,
                        facet_19);
    }

    public String facet20(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
            Object facet_6, Object facet_7, Object facet_8, Object facet_9, Object facet_10, Object facet_11,
            Object facet_12, Object facet_13, Object facet_14, Object facet_15, Object facet_16, Object facet_17,
            Object facet_18, Object facet_19, Object facet_20) {
        return FacetUtils
                .getFacetString(facet_1, facet_2, facet_3, facet_4, facet_5, facet_6, facet_7, facet_8, facet_9,
                        facet_10, facet_11, facet_12, facet_13, facet_14, facet_15, facet_16, facet_17, facet_18,
                        facet_19, facet_20);
    }

    public String facet21(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
            Object facet_6, Object facet_7, Object facet_8, Object facet_9, Object facet_10, Object facet_11,
            Object facet_12, Object facet_13, Object facet_14, Object facet_15, Object facet_16, Object facet_17,
            Object facet_18, Object facet_19, Object facet_20, Object facet_21) {
        return FacetUtils
                .getFacetString(facet_1, facet_2, facet_3, facet_4, facet_5, facet_6, facet_7, facet_8, facet_9,
                        facet_10, facet_11, facet_12, facet_13, facet_14, facet_15, facet_16, facet_17, facet_18,
                        facet_19, facet_20, facet_21);
    }

    public String facet22(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
            Object facet_6, Object facet_7, Object facet_8, Object facet_9, Object facet_10, Object facet_11,
            Object facet_12, Object facet_13, Object facet_14, Object facet_15, Object facet_16, Object facet_17,
            Object facet_18, Object facet_19, Object facet_20, Object facet_21, Object facet_22) {
        return FacetUtils
                .getFacetString(facet_1, facet_2, facet_3, facet_4, facet_5, facet_6, facet_7, facet_8, facet_9,
                        facet_10, facet_11, facet_12, facet_13, facet_14, facet_15, facet_16, facet_17, facet_18,
                        facet_19, facet_20, facet_21, facet_22);
    }
}
