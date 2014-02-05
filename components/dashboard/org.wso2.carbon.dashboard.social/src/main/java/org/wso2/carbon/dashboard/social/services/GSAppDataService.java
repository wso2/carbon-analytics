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
package org.wso2.carbon.dashboard.social.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.shindig.auth.SecurityToken;
import org.apache.shindig.common.util.ImmediateFuture;
import org.apache.shindig.protocol.DataCollection;
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.social.opensocial.spi.AppDataService;
import org.apache.shindig.social.opensocial.spi.GroupId;
import org.apache.shindig.social.opensocial.spi.UserId;
import org.wso2.carbon.registry.social.api.SocialDataException;
import org.wso2.carbon.registry.social.api.appdata.AppDataManager;
import org.wso2.carbon.registry.social.impl.appdata.AppDataManagerImpl;

import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;


@Singleton
public class GSAppDataService implements AppDataService {

    private AppDataManager manager = new AppDataManagerImpl();

    @Inject 
    public GSAppDataService(){

    }


    public Future<DataCollection> getPersonData(Set<UserId> userIds, GroupId groupId, String appId,
                                                Set<String> fields, SecurityToken securityToken)
            throws ProtocolException {
        String[] userIdArray = new String[userIds.size()];
        int index = 0;
        for (UserId id : userIds) {
            if (id != null) {
                userIdArray[index++] = id.getUserId(securityToken);
            }
        }
        String groupIdString = groupId.getType().name();
        Map<String, Map<String, String>> resultMap = null;
        try {
            resultMap = manager.getPersonData(userIdArray, groupIdString, appId, fields);
        } catch (SocialDataException e) {
            throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }

        return ImmediateFuture.newInstance(new DataCollection(resultMap));
    }

    public Future<Void> deletePersonData(UserId userId, GroupId groupId, String appId,
                                         Set<String> fields, SecurityToken securityToken)
            throws ProtocolException {
        String userIdString = userId.getUserId(securityToken);
        String groupIdString = groupId.getType().name();
        try {
            manager.deletePersonData(userIdString, groupIdString, appId, fields);
        } catch (SocialDataException e) {
            throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }

        return ImmediateFuture.newInstance(null);
    }

    public Future<Void> updatePersonData(UserId userId, GroupId groupId, String appId,
                                         Set<String> fields, Map<String, String> values,
                                         SecurityToken securityToken) throws ProtocolException {

        String userIdString = userId.getUserId(securityToken);
        String groupIdString = groupId.getType().name();
        try {
            manager.updatePersonData(userIdString, groupIdString, appId, fields, values);
        } catch (SocialDataException e) {
            throw new ProtocolException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }
        return ImmediateFuture.newInstance(null);
    }
}
