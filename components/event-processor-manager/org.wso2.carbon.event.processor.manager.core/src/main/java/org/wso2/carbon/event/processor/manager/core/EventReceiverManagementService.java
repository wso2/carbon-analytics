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

package org.wso2.carbon.event.processor.manager.core;


public abstract class EventReceiverManagementService implements Manager{
    /**
     * Get the state of the input event adapter service
     * @return state serialised as a byte array
     */
    public abstract  byte[] getState();

    /**
     * Synchronize the state of the input event adapter service
     * @param bytes state of an input event adapter service returned by a call to getState()
     */
    public abstract void syncState(byte[] bytes);

    /**
     * Pause the input event adapter service
     */
    public abstract void pause();

    /**
     * Resume the input event adapter service
     */
    public abstract void resume();


    /**
     * Start event receivers.
     */
    public abstract void start();

    /**
     * Try to start polling event receivers
     */
    public abstract void startPolling();

    /**
     * Check whether the node is the elected receiver coordinator
     * communication with endpoint.
     * @return Returns true if the nodes is te receiver coordinator
     */
    public abstract boolean isReceiverCoordinator();

    public abstract void setReceiverCoordinator(boolean isReceiverCoordinator) ;

    @Override
    public ManagerType getType() {
        return ManagerType.Receiver;
    }
}
