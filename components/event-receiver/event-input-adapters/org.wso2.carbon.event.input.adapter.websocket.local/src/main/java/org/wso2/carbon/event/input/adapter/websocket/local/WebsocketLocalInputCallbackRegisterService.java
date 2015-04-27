/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.input.adapter.websocket.local;

import org.wso2.carbon.event.input.adapter.core.InputEventAdapterListener;

/**
 * This interface is exposed as an OSGI service, which will be invoked by the local websocket endpoint to inform new subscriptions; and do un-subscriptions..
 */
public interface WebsocketLocalInputCallbackRegisterService {

    public void updateAdapterListenerMap(String adaptorName, InputEventAdapterListener eventAdapterListener);

    public InputEventAdapterListener getAdapterListener(String adaptorName);
}
