/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.databridge.core.internal.authentication.session;

import com.google.common.base.Function;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.wso2.carbon.databridge.core.Utils.AgentSession;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * Cache that contains all agent sessions
 */
public class SessionCache {

    private LoadingCache<SessionBean, AgentSession> cache;

    public SessionCache(int expirationTimeInMinutes) {
        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(expirationTimeInMinutes, TimeUnit.MINUTES)
                .build(CacheLoader.from(new SessionFunction()));
    }

    static class SessionFunction implements Function<SessionBean, AgentSession> {
        @Override
        public AgentSession apply(SessionBean sessionBean) {
            return new AgentSession(sessionBean.getSessionId(), sessionBean.getCredentials());
        }
    }

    public AgentSession getSession(SessionBean sessionBean) {
        try {
            return cache.get(sessionBean);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public void removeSession(String sessionId) {
        cache.invalidate(new SessionBean(sessionId));
    }
}
