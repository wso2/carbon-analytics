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
package org.wso2.carbon.analytics.dataservice.locks;

import org.wso2.carbon.analytics.datasource.core.AnalyticsException;

/**
 * This interface represents a lock provider.
 */
public interface LockProvider {

    /**
     * Returns a lock with the given name, creates if not available already.
     * @param name The name of the lock
     * @return The lock
     * @throws AnalyticsException
     */
    Lock getLock(String name) throws AnalyticsException;
    
    /**
     * Clears the given lock.
     * @param name The name of the lock
     * @throws AnalyticsException
     */
    void clearLock(String name) throws AnalyticsException;
    
}
