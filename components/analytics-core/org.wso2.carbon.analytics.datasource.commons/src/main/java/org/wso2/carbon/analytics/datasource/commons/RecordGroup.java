/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.analytics.datasource.commons;

import org.wso2.carbon.analytics.datasource.commons.exception.AnalyticsException;

import java.io.Serializable;


/**
 * This interface represents a record group, which represents the availability set of records local to a common environment. 
 */
public interface RecordGroup extends Serializable {
    
    /**
     * Returns all the locations this record group is situation at.
     * @return The list of hosts
     * @throws AnalyticsException
     */
    String[] getLocations() throws AnalyticsException;
    
}