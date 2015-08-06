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

import java.util.ArrayList;
import java.util.List;

/**
 * represents the Facet UDF
 * provides Facet functionality to sparkSQL queries
 * ex: select x,y,facet2(x,y) from test
 *
 * todo: remove unnecessary naming convention for different number of arguments
 * once SparkSQL supports generic UDFs
 */
public class FacetUDF {

    /**
     * Returns an String Array list when given the arguments
     * @param facet_1
     * @return List<String>
     */
    public List<String> facet1(Object facet_1) {
        List<String> facetList = new ArrayList<>();

        facetList.add(String.valueOf(facet_1));
        return facetList;
    }

    public List<String> facet2(Object facet_1, Object facet_2) {
        List<String> facetList = new ArrayList<>();

        facetList.add(String.valueOf(facet_1));
        facetList.add(String.valueOf(facet_2));
        return facetList;
    }

    public List<String> facet3(Object facet_1, Object facet_2, Object facet_3) {
        List<String> facetList = new ArrayList<>();

        facetList.add(String.valueOf(facet_1));
        facetList.add(String.valueOf(facet_2));
        facetList.add(String.valueOf(facet_3));
        return facetList;
    }

    public List<String> facet4(Object facet_1, Object facet_2, Object facet_3, Object facet_4) {
        List<String> facetList = new ArrayList<>();

        facetList.add(String.valueOf(facet_1));
        facetList.add(String.valueOf(facet_2));
        facetList.add(String.valueOf(facet_3));
        facetList.add(String.valueOf(facet_4));
        return facetList;
    }

    public List<String> facet5(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5) {
        List<String> facetList = new ArrayList<>();

        facetList.add(String.valueOf(facet_1));
        facetList.add(String.valueOf(facet_2));
        facetList.add(String.valueOf(facet_3));
        facetList.add(String.valueOf(facet_4));
        facetList.add(String.valueOf(facet_5));
        return facetList;
    }

    public List<String> facet6(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
                               Object facet_6) {
        List<String> facetList = new ArrayList<>();

        facetList.add(String.valueOf(facet_1));
        facetList.add(String.valueOf(facet_2));
        facetList.add(String.valueOf(facet_3));
        facetList.add(String.valueOf(facet_4));
        facetList.add(String.valueOf(facet_5));
        facetList.add(String.valueOf(facet_6));
        return facetList;
    }

    public List<String> facet7(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
                               Object facet_6, Object facet_7) {
        List<String> facetList = new ArrayList<>();

        facetList.add(String.valueOf(facet_1));
        facetList.add(String.valueOf(facet_2));
        facetList.add(String.valueOf(facet_3));
        facetList.add(String.valueOf(facet_4));
        facetList.add(String.valueOf(facet_5));
        facetList.add(String.valueOf(facet_6));
        facetList.add(String.valueOf(facet_7));
        return facetList;
    }

    public List<String> facet8(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
                               Object facet_6, Object facet_7, Object facet_8) {
        List<String> facetList = new ArrayList<>();

        facetList.add(String.valueOf(facet_1));
        facetList.add(String.valueOf(facet_2));
        facetList.add(String.valueOf(facet_3));
        facetList.add(String.valueOf(facet_4));
        facetList.add(String.valueOf(facet_5));
        facetList.add(String.valueOf(facet_6));
        facetList.add(String.valueOf(facet_7));
        facetList.add(String.valueOf(facet_8));
        return facetList;
    }

    public List<String> facet9(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
                               Object facet_6, Object facet_7, Object facet_8, Object facet_9) {
        List<String> facetList = new ArrayList<>();

        facetList.add(String.valueOf(facet_1));
        facetList.add(String.valueOf(facet_2));
        facetList.add(String.valueOf(facet_3));
        facetList.add(String.valueOf(facet_4));
        facetList.add(String.valueOf(facet_5));
        facetList.add(String.valueOf(facet_6));
        facetList.add(String.valueOf(facet_7));
        facetList.add(String.valueOf(facet_8));
        facetList.add(String.valueOf(facet_9));
        return facetList;
    }

    public List<String> facet10(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
                                Object facet_6, Object facet_7, Object facet_8, Object facet_9, Object facet_10) {
        List<String> facetList = new ArrayList<>();

        facetList.add(String.valueOf(facet_1));
        facetList.add(String.valueOf(facet_2));
        facetList.add(String.valueOf(facet_3));
        facetList.add(String.valueOf(facet_4));
        facetList.add(String.valueOf(facet_5));
        facetList.add(String.valueOf(facet_6));
        facetList.add(String.valueOf(facet_7));
        facetList.add(String.valueOf(facet_8));
        facetList.add(String.valueOf(facet_9));
        facetList.add(String.valueOf(facet_10));
        return facetList;
    }

    public List<String> facet11(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
                                Object facet_6, Object facet_7, Object facet_8, Object facet_9, Object facet_10,
                                Object facet_11) {
        List<String> facetList = new ArrayList<>();

        facetList.add(String.valueOf(facet_1));
        facetList.add(String.valueOf(facet_2));
        facetList.add(String.valueOf(facet_3));
        facetList.add(String.valueOf(facet_4));
        facetList.add(String.valueOf(facet_5));
        facetList.add(String.valueOf(facet_6));
        facetList.add(String.valueOf(facet_7));
        facetList.add(String.valueOf(facet_8));
        facetList.add(String.valueOf(facet_9));
        facetList.add(String.valueOf(facet_10));
        facetList.add(String.valueOf(facet_11));
        return facetList;
    }

    public List<String> facet12(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
                                Object facet_6, Object facet_7, Object facet_8, Object facet_9, Object facet_10,
                                Object facet_11, Object facet_12) {
        List<String> facetList = new ArrayList<>();

        facetList.add(String.valueOf(facet_1));
        facetList.add(String.valueOf(facet_2));
        facetList.add(String.valueOf(facet_3));
        facetList.add(String.valueOf(facet_4));
        facetList.add(String.valueOf(facet_5));
        facetList.add(String.valueOf(facet_6));
        facetList.add(String.valueOf(facet_7));
        facetList.add(String.valueOf(facet_8));
        facetList.add(String.valueOf(facet_9));
        facetList.add(String.valueOf(facet_10));
        facetList.add(String.valueOf(facet_11));
        facetList.add(String.valueOf(facet_12));
        return facetList;
    }

    public List<String> facet13(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
                                Object facet_6, Object facet_7, Object facet_8, Object facet_9, Object facet_10,
                                Object facet_11, Object facet_12, Object facet_13) {
        List<String> facetList = new ArrayList<>();

        facetList.add(String.valueOf(facet_1));
        facetList.add(String.valueOf(facet_2));
        facetList.add(String.valueOf(facet_3));
        facetList.add(String.valueOf(facet_4));
        facetList.add(String.valueOf(facet_5));
        facetList.add(String.valueOf(facet_6));
        facetList.add(String.valueOf(facet_7));
        facetList.add(String.valueOf(facet_8));
        facetList.add(String.valueOf(facet_9));
        facetList.add(String.valueOf(facet_10));
        facetList.add(String.valueOf(facet_11));
        facetList.add(String.valueOf(facet_12));
        facetList.add(String.valueOf(facet_13));
        return facetList;
    }

    public List<String> facet14(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
                                Object facet_6, Object facet_7, Object facet_8, Object facet_9, Object facet_10,
                                Object facet_11, Object facet_12, Object facet_13, Object facet_14) {
        List<String> facetList = new ArrayList<>();

        facetList.add(String.valueOf(facet_1));
        facetList.add(String.valueOf(facet_2));
        facetList.add(String.valueOf(facet_3));
        facetList.add(String.valueOf(facet_4));
        facetList.add(String.valueOf(facet_5));
        facetList.add(String.valueOf(facet_6));
        facetList.add(String.valueOf(facet_7));
        facetList.add(String.valueOf(facet_8));
        facetList.add(String.valueOf(facet_9));
        facetList.add(String.valueOf(facet_10));
        facetList.add(String.valueOf(facet_11));
        facetList.add(String.valueOf(facet_12));
        facetList.add(String.valueOf(facet_13));
        facetList.add(String.valueOf(facet_14));
        return facetList;
    }

    public List<String> facet15(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
                                Object facet_6, Object facet_7, Object facet_8, Object facet_9, Object facet_10,
                                Object facet_11, Object facet_12, Object facet_13, Object facet_14, Object facet_15) {
        List<String> facetList = new ArrayList<>();

        facetList.add(String.valueOf(facet_1));
        facetList.add(String.valueOf(facet_2));
        facetList.add(String.valueOf(facet_3));
        facetList.add(String.valueOf(facet_4));
        facetList.add(String.valueOf(facet_5));
        facetList.add(String.valueOf(facet_6));
        facetList.add(String.valueOf(facet_7));
        facetList.add(String.valueOf(facet_8));
        facetList.add(String.valueOf(facet_9));
        facetList.add(String.valueOf(facet_10));
        facetList.add(String.valueOf(facet_11));
        facetList.add(String.valueOf(facet_12));
        facetList.add(String.valueOf(facet_13));
        facetList.add(String.valueOf(facet_14));
        facetList.add(String.valueOf(facet_15));
        return facetList;
    }

    public List<String> facet16(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
                                Object facet_6, Object facet_7, Object facet_8, Object facet_9, Object facet_10,
                                Object facet_11, Object facet_12, Object facet_13, Object facet_14, Object facet_15,
                                Object facet_16) {
        List<String> facetList = new ArrayList<>();

        facetList.add(String.valueOf(facet_1));
        facetList.add(String.valueOf(facet_2));
        facetList.add(String.valueOf(facet_3));
        facetList.add(String.valueOf(facet_4));
        facetList.add(String.valueOf(facet_5));
        facetList.add(String.valueOf(facet_6));
        facetList.add(String.valueOf(facet_7));
        facetList.add(String.valueOf(facet_8));
        facetList.add(String.valueOf(facet_9));
        facetList.add(String.valueOf(facet_10));
        facetList.add(String.valueOf(facet_11));
        facetList.add(String.valueOf(facet_12));
        facetList.add(String.valueOf(facet_13));
        facetList.add(String.valueOf(facet_14));
        facetList.add(String.valueOf(facet_15));
        facetList.add(String.valueOf(facet_16));
        return facetList;
    }

    public List<String> facet17(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
                                Object facet_6, Object facet_7, Object facet_8, Object facet_9, Object facet_10,
                                Object facet_11, Object facet_12, Object facet_13, Object facet_14, Object facet_15,
                                Object facet_16, Object facet_17) {
        List<String> facetList = new ArrayList<>();

        facetList.add(String.valueOf(facet_1));
        facetList.add(String.valueOf(facet_2));
        facetList.add(String.valueOf(facet_3));
        facetList.add(String.valueOf(facet_4));
        facetList.add(String.valueOf(facet_5));
        facetList.add(String.valueOf(facet_6));
        facetList.add(String.valueOf(facet_7));
        facetList.add(String.valueOf(facet_8));
        facetList.add(String.valueOf(facet_9));
        facetList.add(String.valueOf(facet_10));
        facetList.add(String.valueOf(facet_11));
        facetList.add(String.valueOf(facet_12));
        facetList.add(String.valueOf(facet_13));
        facetList.add(String.valueOf(facet_14));
        facetList.add(String.valueOf(facet_15));
        facetList.add(String.valueOf(facet_16));
        facetList.add(String.valueOf(facet_17));
        return facetList;
    }

    public List<String> facet18(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
                                Object facet_6, Object facet_7, Object facet_8, Object facet_9, Object facet_10,
                                Object facet_11, Object facet_12, Object facet_13, Object facet_14, Object facet_15,
                                Object facet_16, Object facet_17, Object facet_18) {
        List<String> facetList = new ArrayList<>();

        facetList.add(String.valueOf(facet_1));
        facetList.add(String.valueOf(facet_2));
        facetList.add(String.valueOf(facet_3));
        facetList.add(String.valueOf(facet_4));
        facetList.add(String.valueOf(facet_5));
        facetList.add(String.valueOf(facet_6));
        facetList.add(String.valueOf(facet_7));
        facetList.add(String.valueOf(facet_8));
        facetList.add(String.valueOf(facet_9));
        facetList.add(String.valueOf(facet_10));
        facetList.add(String.valueOf(facet_11));
        facetList.add(String.valueOf(facet_12));
        facetList.add(String.valueOf(facet_13));
        facetList.add(String.valueOf(facet_14));
        facetList.add(String.valueOf(facet_15));
        facetList.add(String.valueOf(facet_16));
        facetList.add(String.valueOf(facet_17));
        facetList.add(String.valueOf(facet_18));
        return facetList;
    }

    public List<String> facet19(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
                                Object facet_6, Object facet_7, Object facet_8, Object facet_9, Object facet_10,
                                Object facet_11, Object facet_12, Object facet_13, Object facet_14, Object facet_15,
                                Object facet_16, Object facet_17, Object facet_18, Object facet_19) {
        List<String> facetList = new ArrayList<>();

        facetList.add(String.valueOf(facet_1));
        facetList.add(String.valueOf(facet_2));
        facetList.add(String.valueOf(facet_3));
        facetList.add(String.valueOf(facet_4));
        facetList.add(String.valueOf(facet_5));
        facetList.add(String.valueOf(facet_6));
        facetList.add(String.valueOf(facet_7));
        facetList.add(String.valueOf(facet_8));
        facetList.add(String.valueOf(facet_9));
        facetList.add(String.valueOf(facet_10));
        facetList.add(String.valueOf(facet_11));
        facetList.add(String.valueOf(facet_12));
        facetList.add(String.valueOf(facet_13));
        facetList.add(String.valueOf(facet_14));
        facetList.add(String.valueOf(facet_15));
        facetList.add(String.valueOf(facet_16));
        facetList.add(String.valueOf(facet_17));
        facetList.add(String.valueOf(facet_18));
        facetList.add(String.valueOf(facet_19));
        return facetList;
    }

    public List<String> facet20(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
                                Object facet_6, Object facet_7, Object facet_8, Object facet_9, Object facet_10,
                                Object facet_11, Object facet_12, Object facet_13, Object facet_14, Object facet_15,
                                Object facet_16, Object facet_17, Object facet_18, Object facet_19, Object facet_20) {
        List<String> facetList = new ArrayList<>();

        facetList.add(String.valueOf(facet_1));
        facetList.add(String.valueOf(facet_2));
        facetList.add(String.valueOf(facet_3));
        facetList.add(String.valueOf(facet_4));
        facetList.add(String.valueOf(facet_5));
        facetList.add(String.valueOf(facet_6));
        facetList.add(String.valueOf(facet_7));
        facetList.add(String.valueOf(facet_8));
        facetList.add(String.valueOf(facet_9));
        facetList.add(String.valueOf(facet_10));
        facetList.add(String.valueOf(facet_11));
        facetList.add(String.valueOf(facet_12));
        facetList.add(String.valueOf(facet_13));
        facetList.add(String.valueOf(facet_14));
        facetList.add(String.valueOf(facet_15));
        facetList.add(String.valueOf(facet_16));
        facetList.add(String.valueOf(facet_17));
        facetList.add(String.valueOf(facet_18));
        facetList.add(String.valueOf(facet_19));
        facetList.add(String.valueOf(facet_20));
        return facetList;
    }

    public List<String> facet21(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
                                Object facet_6, Object facet_7, Object facet_8, Object facet_9, Object facet_10,
                                Object facet_11, Object facet_12, Object facet_13, Object facet_14, Object facet_15,
                                Object facet_16, Object facet_17, Object facet_18, Object facet_19, Object facet_20,
                                Object facet_21) {
        List<String> facetList = new ArrayList<>();

        facetList.add(String.valueOf(facet_1));
        facetList.add(String.valueOf(facet_2));
        facetList.add(String.valueOf(facet_3));
        facetList.add(String.valueOf(facet_4));
        facetList.add(String.valueOf(facet_5));
        facetList.add(String.valueOf(facet_6));
        facetList.add(String.valueOf(facet_7));
        facetList.add(String.valueOf(facet_8));
        facetList.add(String.valueOf(facet_9));
        facetList.add(String.valueOf(facet_10));
        facetList.add(String.valueOf(facet_11));
        facetList.add(String.valueOf(facet_12));
        facetList.add(String.valueOf(facet_13));
        facetList.add(String.valueOf(facet_14));
        facetList.add(String.valueOf(facet_15));
        facetList.add(String.valueOf(facet_16));
        facetList.add(String.valueOf(facet_17));
        facetList.add(String.valueOf(facet_18));
        facetList.add(String.valueOf(facet_19));
        facetList.add(String.valueOf(facet_20));
        facetList.add(String.valueOf(facet_21));
        return facetList;
    }

    public List<String> facet22(Object facet_1, Object facet_2, Object facet_3, Object facet_4, Object facet_5,
                                Object facet_6, Object facet_7, Object facet_8, Object facet_9, Object facet_10,
                                Object facet_11, Object facet_12, Object facet_13, Object facet_14, Object facet_15,
                                Object facet_16, Object facet_17, Object facet_18, Object facet_19, Object facet_20,
                                Object facet_21, Object facet_22) {
        List<String> facetList = new ArrayList<>();

        facetList.add(String.valueOf(facet_1));
        facetList.add(String.valueOf(facet_2));
        facetList.add(String.valueOf(facet_3));
        facetList.add(String.valueOf(facet_4));
        facetList.add(String.valueOf(facet_5));
        facetList.add(String.valueOf(facet_6));
        facetList.add(String.valueOf(facet_7));
        facetList.add(String.valueOf(facet_8));
        facetList.add(String.valueOf(facet_9));
        facetList.add(String.valueOf(facet_10));
        facetList.add(String.valueOf(facet_11));
        facetList.add(String.valueOf(facet_12));
        facetList.add(String.valueOf(facet_13));
        facetList.add(String.valueOf(facet_14));
        facetList.add(String.valueOf(facet_15));
        facetList.add(String.valueOf(facet_16));
        facetList.add(String.valueOf(facet_17));
        facetList.add(String.valueOf(facet_18));
        facetList.add(String.valueOf(facet_19));
        facetList.add(String.valueOf(facet_20));
        facetList.add(String.valueOf(facet_21));
        facetList.add(String.valueOf(facet_22));
        return facetList;
    }

}
