package org.wso2.carbon.event.output.adaptor.manager.admin.internal.util;


import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorService;

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
 * This class is used to hold the event adaptor service
 */
public final class OutputEventAdaptorHolder {
    private OutputEventAdaptorService eventAdaptorService;
    private static OutputEventAdaptorHolder instance = new OutputEventAdaptorHolder();

    private OutputEventAdaptorHolder() {
    }

    public OutputEventAdaptorService getEventAdaptorService() {
        return eventAdaptorService;
    }

    public static OutputEventAdaptorHolder getInstance() {
        return instance;
    }

    public void registerEventService(OutputEventAdaptorService eventAdaptorService) {
        this.eventAdaptorService = eventAdaptorService;
    }

    public void unRegisterEventService(OutputEventAdaptorService eventAdaptorService) {
        this.eventAdaptorService = null;
    }
}
