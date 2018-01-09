/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

import TemplatesEditorConstants from '../constants/TemplatesEditorConstants';

class TemplatesEditorUtilityFunctions {
    /**
     * Gets matching elements for the provided regex in the given text
     * @param text
     * @param regex
     * @returns {Array}
     */
    static getRegexMatches(text, regex) {
        const matches = [];
        let matcher;
        do {
            matcher = regex.exec(text);
            if (matcher) {
                // Get the complete match for a Siddhi stream definition
                if (regex === TemplatesEditorConstants.STREAM_DEFINITION_REGEX) {
                    matches.push(matcher[0]);
                } else {
                    matches.push(matcher[1]);
                }
            }

        } while (matcher);
        return matches;
    }

    /**
     * Checks whether a given object is empty or not
     *
     * @param object
     * @returns {boolean}
     */
    static isEmpty(object) {
        for (let key in object) {
            if (object.hasOwnProperty(key))
                return false;
        }
        return true;
    }

    /**
     * Calculates reading time of a message in milliseconds, for efficiently producing error display timeouts
     * @param text
     * @returns {number}
     */
    static calculateReadingTime(text) {
        const wordsPerMinute = 140;
        const noOfWords = text.split(/\s/g).length;
        const millis = (noOfWords / wordsPerMinute) * 60000;
        return Math.ceil(millis);
    }
}

export default TemplatesEditorUtilityFunctions;
