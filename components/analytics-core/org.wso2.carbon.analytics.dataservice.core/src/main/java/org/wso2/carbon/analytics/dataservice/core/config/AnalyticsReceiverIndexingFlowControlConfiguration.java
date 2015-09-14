/**
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.carbon.analytics.dataservice.core.config;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

/**
 * This class represents the analytics receiver indexing flow control configuration.
 */
public class AnalyticsReceiverIndexingFlowControlConfiguration {

    private boolean enabled;
    
    private int recordReceivingHighThreshold;
    
    private int recordReceivingLowThreshold;
    
    @XmlAttribute (name = "enabled")
    public boolean isEnabled() {
        return enabled;
    }
    
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
    
    @XmlElement (name = "recordReceivingHighThreshold")
    public int getRecordReceivingHighThreshold() {
        return recordReceivingHighThreshold;
    }
    
    public void setRecordReceivingHighThreshold(int recordReceivingHighThreshold) {
        this.recordReceivingHighThreshold = recordReceivingHighThreshold;
    }
    
    @XmlElement (name = "recordReceivingLowThreshold")
    public int getRecordReceivingLowThreshold() {
        return recordReceivingLowThreshold;
    }
    
    public void setRecordReceivingLowThreshold(int recordReceivingLowThreshold) {
        this.recordReceivingLowThreshold = recordReceivingLowThreshold;
    }
    
}
