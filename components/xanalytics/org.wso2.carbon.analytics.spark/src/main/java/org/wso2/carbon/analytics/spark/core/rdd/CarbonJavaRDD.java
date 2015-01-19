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

package org.wso2.carbon.analytics.spark.core.rdd;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.rdd.RDD;
import scala.reflect.ClassTag;

/**
 * Created by niranda on 12/11/14.
 */
public class CarbonJavaRDD extends JavaRDD {


    public CarbonJavaRDD(RDD rdd, ClassTag classTag) {
        super(rdd, classTag);
    }

    @Override
    public ClassTag classTag() {
        return null;
    }
}

