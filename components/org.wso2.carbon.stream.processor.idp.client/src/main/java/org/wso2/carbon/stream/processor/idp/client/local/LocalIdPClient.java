/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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
package org.wso2.carbon.stream.processor.idp.client.local;

import org.wso2.carbon.stream.processor.idp.client.core.api.IdPClient;
import org.wso2.carbon.stream.processor.idp.client.core.models.Group;

import java.util.List;
import java.util.Map;

public class LocalIdPClient implements IdPClient {

    public LocalIdPClient() {
    }

    public LocalIdPClient(Map<String, String> properties) {
    }

    @Override
    public List<Group> getAllGroups() {
        return null;
    }

    @Override
    public List<Group> getUsersGroups(String id) {
        return null;
    }

    @Override
    public Map<String, String> login(Map<String, String> properties) {
        return null;
    }

    @Override
    public void logout(String token) {
    }

    @Override
    public boolean authenticate(String token) {
        // Returning true till implementation is finished
        return true;
    }
}
