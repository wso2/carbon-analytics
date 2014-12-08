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
 * Analytics lock functionalities.
 */
public interface Lock {
    
    boolean isLocked() throws AnalyticsException;
    
    /**
     * Acquires the lock.
     * @throws AnalyticsException
     */
    void acquire() throws AnalyticsException;
    
    /**
     * Tries to acquire the lock, until lockWaitTimeout is passed.
     * @param lockWaitTimeout The timeout to wait for the lock
     * @return true if the lock is acquired, or we already had it, 
     * false, if the timeout passed waiting for the lock
     * @throws AnalyticsException
     */
    boolean acquire(long lockWaitTimeout) throws AnalyticsException;
    
    /**
     * Releases the lock.
     * @throws AnalyticsException
     */
    void release() throws AnalyticsException;
    
}
