/*
 * Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.event.receiver.core.internal.management;

import org.wso2.carbon.databridge.commons.Event;
import org.wso2.carbon.event.stream.core.EventProducerCallback;

public abstract class AbstractInputEventDispatcher {

    protected boolean sendToOther = false;

    protected EventProducerCallback callBack;

    public abstract void onEvent(Event event);

    public abstract void shutdown();

    public abstract byte[] getState();

    public abstract void syncState(byte[] bytes);

    public void setCallBack(EventProducerCallback callBack) {
        this.callBack = callBack;
    }

    public EventProducerCallback getCallBack() {
        return callBack;
    }

    public boolean isSendToOther() {
        return sendToOther;
    }

    public void setSendToOther(boolean sendToOther) {
        this.sendToOther = sendToOther;
    }
}
