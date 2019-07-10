/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.streaming.integrator.core.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import io.siddhi.core.util.statistics.metrics.Level;

import java.util.Objects;

/**
 * StatsEnable
 */
@javax.annotation.Generated(value = "io.swagger.codegen.languages.JavaMSF4JServerCodegen", date = "2017-11-02T13:49:11.445Z")
public class StatsEnable {
    @JsonProperty("enabledStatLevel")
    private String enabledStatLevel = null;
    private Level enabledSiddhiStatLevel = null;
    @JsonProperty("statsEnable")
    private Boolean statsEnable = null;

    public void setStatsEnable(Boolean statsEnable) {
        this.statsEnable = statsEnable;
        if (statsEnable) {
            this.enabledSiddhiStatLevel = Level.BASIC;
        } else {
            this.enabledSiddhiStatLevel = Level.OFF;
        }
    }

    public void setEnabledStatLevel() {
        this.enabledSiddhiStatLevel = Level.valueOf(enabledStatLevel);
        if (this.enabledSiddhiStatLevel.compareTo(Level.OFF) != 0) {
            this.statsEnable = true;
        } else {
            this.statsEnable = false;
        }
    }

    @ApiModelProperty(required = true, value = "")
    public Boolean getStatsEnable() {
        return statsEnable;
    }

    /**
     * Get statsEnable
     *
     * @return statsEnable
     **/
    @ApiModelProperty(required = true, value = "")
    public Level getEnabledSiddhiStatLevel() {
        if (enabledSiddhiStatLevel == null) {
            if (enabledStatLevel == null) {
                if (statsEnable) {
                    enabledSiddhiStatLevel = Level.DETAIL;
                } else {
                    enabledSiddhiStatLevel = Level.OFF;
                }
            } else {
                enabledSiddhiStatLevel = Level.valueOf(enabledStatLevel);
            }
        }
        return enabledSiddhiStatLevel;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StatsEnable statsEnable = (StatsEnable) o;
        return this.enabledSiddhiStatLevel.compareTo(statsEnable.enabledSiddhiStatLevel) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(enabledSiddhiStatLevel);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class StatsEnable {\n");
        sb.append("    enabledStatLevel: ").append(toIndentedString(enabledSiddhiStatLevel.toString())).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }
}

