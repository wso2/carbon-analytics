package org.wso2.carbon.event.input.adaptor.manager.core.internal.ds;/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.wso2.carbon.event.input.adaptor.manager.core.internal.CarbonInputEventAdaptorManagerService;

/**
 * To hold the event adaptor manager service
 */
public class InputEventAdaptorManagerValueHolder {

    private static CarbonInputEventAdaptorManagerService carbonEventAdaptorManagerService;

    private InputEventAdaptorManagerValueHolder(){}

    public static void registerCarbonEventAdaptorManagerService(
            CarbonInputEventAdaptorManagerService carbonEventAdaptorManagerService) {

        InputEventAdaptorManagerValueHolder.carbonEventAdaptorManagerService = carbonEventAdaptorManagerService;
    }

    public static CarbonInputEventAdaptorManagerService getCarbonEventAdaptorManagerService() {
        return InputEventAdaptorManagerValueHolder.carbonEventAdaptorManagerService;
    }
}
