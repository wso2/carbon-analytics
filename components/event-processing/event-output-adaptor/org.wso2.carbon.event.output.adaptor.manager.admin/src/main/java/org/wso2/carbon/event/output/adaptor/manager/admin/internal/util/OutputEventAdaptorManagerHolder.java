package org.wso2.carbon.event.output.adaptor.manager.admin.internal.util;

import org.wso2.carbon.event.output.adaptor.manager.core.OutputEventAdaptorManagerService;

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

/**
 * This class is used to hold the event adaptor manager service
 */
public final class OutputEventAdaptorManagerHolder {
    private OutputEventAdaptorManagerService outputEventAdaptorManagerService;
    private static OutputEventAdaptorManagerHolder instance = new OutputEventAdaptorManagerHolder();

    private OutputEventAdaptorManagerHolder() {
    }

    public OutputEventAdaptorManagerService getOutputEventAdaptorManagerService() {
        return outputEventAdaptorManagerService;
    }

    public static OutputEventAdaptorManagerHolder getInstance() {
        return instance;
    }

    public void registerEventAdaptorManagerService(
            OutputEventAdaptorManagerService outputEventAdaptorManagerService) {
        this.outputEventAdaptorManagerService = outputEventAdaptorManagerService;
    }

    public void unRegisterEventAdaptorManagerService(
            OutputEventAdaptorManagerService outputEventAdaptorManagerService) {
        this.outputEventAdaptorManagerService = null;
    }

}
