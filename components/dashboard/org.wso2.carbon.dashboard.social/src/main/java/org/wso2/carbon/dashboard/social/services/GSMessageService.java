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
import org.apache.shindig.protocol.ProtocolException;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.social.opensocial.model.Message;
import org.apache.shindig.social.opensocial.model.MessageCollection;
import org.apache.shindig.social.opensocial.spi.CollectionOptions;
import org.apache.shindig.social.opensocial.spi.MessageService;
import org.apache.shindig.social.opensocial.spi.UserId;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Future;


//@Singleton
public class GSMessageService implements MessageService {
   /* @Inject
    public GSMessageService(){
        
    }*/
    public Future<RestfulCollection<MessageCollection>> getMessageCollections(UserId userId,
                                                                              Set<String> strings,
                                                                              CollectionOptions collectionOptions,
                                                                              SecurityToken securityToken)
            throws ProtocolException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<MessageCollection> createMessageCollection(UserId userId,
                                                             MessageCollection messageCollection,
                                                             SecurityToken securityToken)
            throws ProtocolException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<Void> modifyMessageCollection(UserId userId, MessageCollection messageCollection,
                                                SecurityToken securityToken)
            throws ProtocolException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<Void> deleteMessageCollection(UserId userId, String s,
                                                SecurityToken securityToken)
            throws ProtocolException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<RestfulCollection<Message>> getMessages(UserId userId, String s,
                                                          Set<String> strings1,
                                                          List<String> strings2,
                                                          CollectionOptions collectionOptions,
                                                          SecurityToken securityToken)
            throws ProtocolException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<Void> createMessage(UserId userId, String s, String s1, Message message,
                                      SecurityToken securityToken) throws ProtocolException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<Void> deleteMessages(UserId userId, String s, List<String> strings,
                                       SecurityToken securityToken) throws ProtocolException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Future<Void> modifyMessage(UserId userId, String s, String s1, Message message,
                                      SecurityToken securityToken) throws ProtocolException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
