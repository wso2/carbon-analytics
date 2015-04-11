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
package org.wso2.carbon.event.receiver.core.internal.type.text.config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexData {
    private Pattern pattern;
    private Matcher matcher;
    private int matchGroupIndex;
    private List<String> types;
    private List<String> defaultValues;
    private String regex;

    public RegexData(String regex) {
        this.regex = regex;
        this.pattern = Pattern.compile(regex);
        this.matchGroupIndex = 0;
        types = new ArrayList<String>();
        defaultValues = new ArrayList<String>();
    }

    public void addMapping(String type, String defaultValue) {
        this.types.add(matchGroupIndex, type);
        this.defaultValues.add(matchGroupIndex, defaultValue);
        matchGroupIndex++;
    }

    public String getDefaultValue() {
        return defaultValues.get(matchGroupIndex);
    }

    public String getType() {
        return types.get(matchGroupIndex);
    }

    public String next() {
        matchGroupIndex++;
        String matchResult;
        if (matchGroupIndex < matcher.groupCount()) {
            matchResult = matcher.group(matchGroupIndex + 1);
        } else {
            matchResult = defaultValues.get(matchGroupIndex);
        }
        return matchResult;
    }

    public boolean hasNext() {
        return matcher.groupCount() > matchGroupIndex + 1;
    }

    public void matchInput(String input) {
        this.matcher = pattern.matcher(input);
        this.matcher.find();
        this.matchGroupIndex = -1;
    }

    public String getRegex() {
        return regex;
    }

}