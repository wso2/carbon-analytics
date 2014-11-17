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

import java.util.concurrent.TimeUnit;

import org.wso2.carbon.analytics.datasource.core.AnalyticsLockException;

import com.hazelcast.core.HazelcastInstance;

/**
 * Hazelcast (distributed memory) based implementation of {@link LockProvider}.
 */
public class HazelcastLockProvider implements LockProvider {
    
    private HazelcastInstance hazelcast;
    
    public HazelcastLockProvider(HazelcastInstance hazelcast) {
        this.hazelcast = hazelcast;
    }
    
    public HazelcastInstance getHazelcast() {
        return hazelcast;
    }

    @Override
    public Lock getLock(String name) throws AnalyticsLockException {
        return new HazelcastLock(name);
    }

    @Override
    public void clearLock(String name) throws AnalyticsLockException {
        new HazelcastLock(name).release();
    }
    
    /**
     * Hazelcast based {@link Lock} implementation.
     */
    private class HazelcastLock implements Lock {

        private String name;
        
        public HazelcastLock(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        @Override
        public void acquire() throws AnalyticsLockException {
            try {
                getHazelcast().getLock(this.getName()).lock();
            } catch (Exception e) {
                throw new AnalyticsLockException("Error in acquiring lock '" + this.getName() +
                        "': " + e.getMessage(), e);
            }
        }

        @Override
        public void release() throws AnalyticsLockException {
            try {
                getHazelcast().getLock(this.getName()).unlock();
            } catch (Exception e) {
                throw new AnalyticsLockException("Error in acquiring lock '" + this.getName() +
                        "': " + e.getMessage(), e);
            }
        }

        @Override
        public boolean isLocked() throws AnalyticsLockException {
            try {
                return getHazelcast().getLock(this.getName()).isLocked();
            } catch (Exception e) {
                throw new AnalyticsLockException("Error in checking lock '" + this.getName() +
                        "': " + e.getMessage(), e);
            }
        }

        @Override
        public boolean acquire(long lockWaitTimeout) throws AnalyticsLockException {
            try {
                return getHazelcast().getLock(this.getName()).tryLock(lockWaitTimeout, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                throw new AnalyticsLockException("Error in acquiring lock '" + this.getName() +
                        "': " + e.getMessage(), e);
            }
        }
        
    }

}
