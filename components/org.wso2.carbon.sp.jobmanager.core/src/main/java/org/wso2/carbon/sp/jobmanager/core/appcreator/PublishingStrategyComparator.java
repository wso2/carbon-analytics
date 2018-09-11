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

package org.wso2.carbon.sp.jobmanager.core.appcreator;

import org.wso2.carbon.sp.jobmanager.core.topology.PublishingStrategyDataHolder;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator for publishing strategies.
 */

public class PublishingStrategyComparator implements Comparator<PublishingStrategyDataHolder>,Serializable  {

    @Override
    public int compare(PublishingStrategyDataHolder t1, PublishingStrategyDataHolder t2) {
        int order = 0;
        if (t1.getStrategy().toString().equals("FIELD_GROUPING") && !t2.getStrategy().toString().equals("FIELD_GROUPING")){
            order = 5;
        }else{
            order = -5;
        }
        return  order;
    }
}