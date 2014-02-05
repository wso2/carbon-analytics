/*
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

package org.wso2.carbon.event.output.adaptor.core.config;


import java.util.Map;

/**
 * This class contain the configuration details of the event
 */

public class OutputEventAdaptorConfiguration {

    private String name;

    private String type;

    private boolean enableTracing;

    private boolean enableStatistics;

    private InternalOutputEventAdaptorConfiguration internalOutputEventAdaptorConfiguration = null;

    public InternalOutputEventAdaptorConfiguration getOutputConfiguration() {
        return internalOutputEventAdaptorConfiguration;
    }

    public void setOutputConfiguration(
            InternalOutputEventAdaptorConfiguration internalOutputEventAdaptorConfiguration) {
        this.internalOutputEventAdaptorConfiguration = internalOutputEventAdaptorConfiguration;
    }


    public Map<String, String> getOutputProperties() {
        if (internalOutputEventAdaptorConfiguration != null) {
            return internalOutputEventAdaptorConfiguration.getProperties();
        }
        return null;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isEnableTracing() {
        return enableTracing;
    }

    public void setEnableTracing(boolean enableTracing) {
        this.enableTracing = enableTracing;
    }

    public boolean isEnableStatistics() {
        return enableStatistics;
    }

    public void setEnableStatistics(boolean enableStatistics) {
        this.enableStatistics = enableStatistics;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OutputEventAdaptorConfiguration)) {
            return false;
        }

        OutputEventAdaptorConfiguration that = (OutputEventAdaptorConfiguration) o;

        if (internalOutputEventAdaptorConfiguration != null ? !internalOutputEventAdaptorConfiguration.equals(that.internalOutputEventAdaptorConfiguration) : that.internalOutputEventAdaptorConfiguration != null) {
            return false;
        }
        if (!name.equals(that.name)) {
            return false;
        }
        if (!type.equals(that.type)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (internalOutputEventAdaptorConfiguration != null ? internalOutputEventAdaptorConfiguration.hashCode() : 0);
        return result;
    }
}