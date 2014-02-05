package org.wso2.carbon.event.input.adaptor.manager.admin.internal.util;

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

import org.wso2.carbon.event.input.adaptor.manager.core.InputEventAdaptorManagerService;

/**
 * This class is used to hold the event adaptor manager service
 */
public final class InputEventAdaptorManagerHolder {
    private InputEventAdaptorManagerService inputEventAdaptorManagerService;
    private static InputEventAdaptorManagerHolder instance = new InputEventAdaptorManagerHolder();

    private InputEventAdaptorManagerHolder() {
    }

    public InputEventAdaptorManagerService getInputEventAdaptorManagerService() {
        return inputEventAdaptorManagerService;
    }

    public static InputEventAdaptorManagerHolder getInstance() {
        return instance;
    }

    public void registerEventAdaptorManagerService(
            InputEventAdaptorManagerService inputEventAdaptorManagerService) {
        this.inputEventAdaptorManagerService = inputEventAdaptorManagerService;
    }

    public void unRegisterEventAdaptorManagerService(
            InputEventAdaptorManagerService inputEventAdaptorManagerService) {
        this.inputEventAdaptorManagerService = null;
    }

}
