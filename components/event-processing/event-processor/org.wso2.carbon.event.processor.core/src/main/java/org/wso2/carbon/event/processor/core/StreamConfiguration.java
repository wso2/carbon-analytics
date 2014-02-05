/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.event.processor.core;


import org.wso2.carbon.event.processor.core.internal.util.EventProcessorUtil;

public class StreamConfiguration {
    // name, version mapped to databridge - to interface with outside
    private String name;
    private String version;

    // for imported streams : as
    // for exported streams : valueOf
    private String siddhiStreamName;

    private boolean passThroughFlowSupported;

    public StreamConfiguration(String name, String version) {
        this.name = name;
        if (version == null) {
            this.version = "1.0.0";
            this.siddhiStreamName = name;
        } else {
            this.version = version;
            if ("1.0.0".equals(version)) {
                this.siddhiStreamName = name;
            } else {
                this.siddhiStreamName = name + "_" + version.replaceAll("\\.", "_");
            }
        }

    }

    public StreamConfiguration(String name) {
        this.name = name;
        this.version = "1.0.0";
        this.siddhiStreamName = name;
    }

    public StreamConfiguration(String name, String version, String siddhiStreamName) {
        this.name = name;
        this.siddhiStreamName = siddhiStreamName;
        if (version == null) {
            this.version = "1.0.0";
        } else {
            this.version = version;
        }
    }

    public String getName() {
        return name;
    }

    public String getStreamId() {
        return EventProcessorUtil.getStreamId(name, version);
    }

    public String getVersion() {
        return version;
    }

    public String getSiddhiStreamName() {
        return siddhiStreamName;
    }

    public void setSiddhiStreamName(String siddhiStreamName) {
        this.siddhiStreamName = siddhiStreamName;
    }

    public boolean isPassThroughFlowSupported() {
        return passThroughFlowSupported;
    }

    public void setPassThroughFlowSupported(boolean passThroughFlowSupported) {
        this.passThroughFlowSupported = passThroughFlowSupported;
    }
}
