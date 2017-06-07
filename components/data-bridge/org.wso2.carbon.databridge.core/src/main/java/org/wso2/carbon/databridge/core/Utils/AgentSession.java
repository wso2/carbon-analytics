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

package org.wso2.carbon.databridge.core.Utils;

import org.wso2.carbon.databridge.commons.Credentials;

/**
 * Agent Client's session
 */
public class AgentSession {
    private String sessionId;
    private Credentials credentials;
    private long createdAt;

    public AgentSession(String sessionId, Credentials credentials) {
        this.sessionId = sessionId;
        this.credentials = credentials;
        this.createdAt = System.currentTimeMillis();
    }

    public AgentSession(String sessionId, Credentials credentials, long createdAt) {
        this.sessionId = sessionId;
        this.credentials = credentials;
        this.createdAt = createdAt;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUsername() {
        return credentials.getUsername();
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "AgentSession{" +
               "sessionId='" + sessionId + '\'' +
               ", username='" + credentials.getUsername() + '\'' +
               ", createdAt=" + createdAt +
               '}';
    }

    public void setCredentials(String userName, String password, String domainName) {
        this.credentials = new Credentials(userName, password);
    }

    public Credentials getCredentials() {
        return credentials;
    }

}
