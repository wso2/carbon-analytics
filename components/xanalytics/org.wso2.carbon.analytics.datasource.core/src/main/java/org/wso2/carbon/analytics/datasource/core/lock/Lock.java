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
package org.wso2.carbon.analytics.datasource.core.lock;

import org.wso2.carbon.analytics.datasource.core.AnalyticsLockException;

/**
 * Analytics lock functionalities.
 */
public interface Lock {
    
    boolean isLocked() throws AnalyticsLockException;
    
    /**
     * Acquires the lock.
     * @throws AnalyticsLockException
     */
    void acquire() throws AnalyticsLockException;
    
    /**
     * Tries to acquire the lock, until lockWaitTimeout is passed.
     * @param lockWaitTimeout The timeout to wait for the lock
     * @return true if the lock is acquired, or we already had it, 
     * false, if the timeout passed waiting for the lock
     * @throws AnalyticsLockException
     */
    boolean acquire(long lockWaitTimeout) throws AnalyticsLockException;
    
    /**
     * Releases the lock.
     * @throws AnalyticsLockException
     */
    void release() throws AnalyticsLockException;
    
}
