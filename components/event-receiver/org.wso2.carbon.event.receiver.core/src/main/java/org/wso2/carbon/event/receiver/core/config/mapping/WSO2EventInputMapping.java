/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
package org.wso2.carbon.event.receiver.core.config.mapping;


import org.wso2.carbon.event.receiver.core.config.EventReceiverConstants;
import org.wso2.carbon.event.receiver.core.config.InputMapping;

public class WSO2EventInputMapping extends InputMapping {

    private String fromEventName;
    private String fromEventVersion;
    private boolean arbitraryMapsEnabled = false;

    @Override
    public String getMappingType() {
        return EventReceiverConstants.ER_WSO2EVENT_MAPPING_TYPE;
    }

    public void setFromEventName(String fromEventName) {
        this.fromEventName = fromEventName;
    }

    public String getFromEventName() {
        return fromEventName;
    }

    public void setFromEventVersion(String fromEventVersion) {
        this.fromEventVersion = fromEventVersion;
    }

    public String getFromEventVersion() {
        return fromEventVersion;
    }


    public boolean isArbitraryMapsEnabled() {
        return arbitraryMapsEnabled;
    }

    public void setArbitraryMapsEnabled(boolean arbitraryMapsEnabled) {
        this.arbitraryMapsEnabled = arbitraryMapsEnabled;
    }
}
