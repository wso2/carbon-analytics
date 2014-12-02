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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.wso2.carbon.analytics.datasource.core.AnalyticsLockException;

/**
 * Local in-memory implementation of {@link LockProvider}.
 */
public class LocalInMemoryLockProvider implements LockProvider {
    
    private Map<String, Lock> locks = new ConcurrentHashMap<String, Lock>();

    @Override
    public Lock getLock(String name) throws AnalyticsLockException {
        Lock lock = this.locks.get(name);
        if (lock == null) {
            synchronized (this.locks) {
                if (lock == null) {
                    lock = new LocalInMemoryLock(name);
                    this.locks.put(name, lock);
                }
            }
        }
        return lock;
    }

    /**
     * Local in-memory {@link Lock} implementation.
     */
    private class LocalInMemoryLock implements Lock {
        
        private String name;

        private ReentrantLock lock = new ReentrantLock();
        
        public LocalInMemoryLock(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        @Override
        public void acquire() throws AnalyticsLockException {
            try {
                this.lock.lock();
            } catch (Exception e) {
                throw new AnalyticsLockException("Error in acquiring lock '" + this.getName() +
                        "': " + e.getMessage(), e);
            }
        }

        @Override
        public void release() throws AnalyticsLockException {
            try {
                this.lock.unlock();
            } catch (Exception e) {
                e.printStackTrace();
                throw new AnalyticsLockException("Error in releasing lock '" + this.getName() +
                        "': " + e.getMessage(), e);
            }
        }

        @Override
        public boolean isLocked() throws AnalyticsLockException {
            try {
                return this.lock.isLocked();
            } catch (Exception e) {
                throw new AnalyticsLockException("Error in checking lock '" + this.getName() +
                        "': " + e.getMessage(), e);
            }      
        }

        @Override
        public boolean acquire(long lockWaitTimeout) throws AnalyticsLockException {
            try {
                return this.lock.tryLock(lockWaitTimeout, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                throw new AnalyticsLockException("Error in acquiring lock '" + this.getName() +
                        "': " + e.getMessage(), e);
            }
        }
        
    }

    @Override
    public void clearLock(String name) throws AnalyticsLockException {
        Lock lock = locks.get(name);
        if (lock != null) {
            lock.release();
            locks.remove(name);
        }
    }
    
}
