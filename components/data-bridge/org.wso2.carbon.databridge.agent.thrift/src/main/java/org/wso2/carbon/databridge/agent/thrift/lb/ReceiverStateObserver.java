package org.wso2.carbon.databridge.agent.thrift.lb;

import org.wso2.carbon.databridge.agent.thrift.util.PublishData;
import org.wso2.carbon.databridge.commons.Event;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Copyright (c) 2009, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public interface ReceiverStateObserver {

    public void notifyConnectionFailure(String receiverUrl, String username, String password);

    public void resendEvents(LinkedBlockingQueue<Event> events);

    public void resendPublishedData(LinkedBlockingQueue<PublishData> publishDataQueue);

    public void notifyConnectionSuccess(String receiverUrl, String username, String password);
}
