/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.analytics.spark.core.udf;

import org.apache.spark.sql.api.java.UDF3;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by niranda on 5/29/15.
 */
public class CompositeID implements UDF3<Integer, Integer, Integer, List<String>>, Serializable{

    private static final long serialVersionUID = -9073977448886578865L;

    @Override
    public List<String> call(Integer integer, Integer integer2, Integer integer3) throws Exception {
        ArrayList<String> arr = new ArrayList<>();
        arr.add(integer.toString());
        arr.add(integer2.toString());
        arr.add(integer3.toString());

        return arr;
    }
}
