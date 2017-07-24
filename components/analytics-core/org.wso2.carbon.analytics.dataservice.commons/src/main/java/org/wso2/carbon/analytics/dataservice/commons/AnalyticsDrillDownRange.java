/*
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

package org.wso2.carbon.analytics.dataservice.commons;

import java.io.Serializable;

/**
 * This class represents a range for numbers, includes details about the bounds of the range.
 * To cover all cases, double values are used
 */
public class AnalyticsDrillDownRange implements Serializable{

    private static final long serialVersionUID = 2158861592169651853L;
    
    private String label;
    private double from;
    private double to;
    private double score;

    public AnalyticsDrillDownRange() { }
    
    public AnalyticsDrillDownRange(String label, double from, double to) {
        this.label = label;
        this.from = from;
        this.to = to;
    }

    public AnalyticsDrillDownRange(String label, double from, double to, double score) {
        this.label = label;
        this.from = from;
        this.to = to;
        this.score = score;
    }

    public double getFrom() {
        return from;
    }

    public void setFrom(double from) {
        this.from = from;
    }

    public double getTo() {
        return to;
    }

    public void setTo(double to) {
        this.to = to;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
