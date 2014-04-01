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
package org.wso2.carbon.event.output.adaptor.manager.core.internal.ds;


import org.wso2.carbon.event.output.adaptor.core.OutputEventAdaptorService;

/**
 * This method is used to hold the event adaptor service
 */
public class OutputEventAdaptorHolder {

    private static OutputEventAdaptorHolder instance = new OutputEventAdaptorHolder();

    private OutputEventAdaptorService eventAdaptorService;

    public static OutputEventAdaptorHolder getInstance() {
        return instance;
    }

    public void setOutputEventAdaptorService(
            OutputEventAdaptorService outputEventAdaptorService) {
        this.eventAdaptorService = outputEventAdaptorService;
    }

    public void unSetTOutputEventAdaptorService() {
        this.eventAdaptorService = null;
    }

    public OutputEventAdaptorService getOutputEventAdaptorService() {
        return this.eventAdaptorService;
    }
}
