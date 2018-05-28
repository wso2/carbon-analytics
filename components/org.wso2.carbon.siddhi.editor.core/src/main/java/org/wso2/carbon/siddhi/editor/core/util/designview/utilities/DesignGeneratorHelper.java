/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.siddhi.editor.core.util.designview.utilities;

import org.wso2.carbon.siddhi.editor.core.util.designview.exceptions.DesignGeneratorHelperException;
import org.wso2.siddhi.query.api.annotation.Element;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Has helper methods for Code to Design generation
 */
public class DesignGeneratorHelper {

    /**
     * Avoids instantiation
     */
    private DesignGeneratorHelper() {
    }

    /**
     * Generates Edge ID using the parent ID and the child ID, that are connected to this edge
     * @param parentID  ID of the parent node
     * @param childID   ID of the child node
     * @return          ID of the edge
     */
    public static String generateEdgeID(String parentID, String childID) {
        return String.format("%s_%s", parentID, childID);
    }

    /**
     * Returns whether the given value should be treated as a string (should be wrapped within quotes) or not
     * @param element   Element, whose value is checked
     * @return          true - when the value should be treated a string. Otherwise, false
     */
    public static boolean isStringValue(Element element) {
        return true;
    }

    /**
     * Gets a list of matches for the given regex Pattern, in the given string
     * @param stringToCheck                         String, whose content is searched for available matches
     * @param regexPattern                          Compiled regex pattern
     * @return                                      List of matched elements, each as String
     * @throws DesignGeneratorHelperException      When no matches are found
     */
    public static List<String> getRegexMatches(String stringToCheck, Pattern regexPattern)
            throws DesignGeneratorHelperException {
        List<String> matches = new ArrayList<>();
        Matcher regexMatcher = regexPattern.matcher(stringToCheck);
        // When an element with regex is is found
        while (regexMatcher.find()) {
            matches.add(regexMatcher.group(1));
        }
        if (matches.isEmpty()) {
            throw new DesignGeneratorHelperException(
                    String.format("No matches found for regex: %s  in string: %s",stringToCheck, regexPattern));
        }
        return matches;
    }

}
