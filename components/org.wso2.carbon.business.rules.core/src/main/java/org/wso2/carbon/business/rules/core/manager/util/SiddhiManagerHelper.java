/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.business.rules.core.manager.util;

import org.wso2.carbon.business.rules.core.exceptions.SiddhiAppManagerApiException;
import org.wso2.carbon.business.rules.core.util.TemplateManagerConstants;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.core.Response;

public class SiddhiManagerHelper {
    /**
     * To avoid instantiation
     */
    private SiddhiManagerHelper() {

    }

    /**
     * Gives the name of the given siddhiApp,
     *
     * @param siddhiApp : siddhi app
     * @return String   : siddhi app name
     * @throws SiddhiAppManagerApiException
     */
    public static String getSiddhiAppName(Object siddhiApp) throws SiddhiAppManagerApiException {

        // Regex match and find name
        Pattern siddhiAppNamePattern = Pattern.compile(TemplateManagerConstants.SIDDHI_APP_NAME_REGEX_PATTERN);
        Matcher siddhiAppNameMatcher = siddhiAppNamePattern.matcher(siddhiApp.toString());
        if (siddhiAppNameMatcher.find()) {
            return siddhiAppNameMatcher.group(2);
        }
        throw new SiddhiAppManagerApiException("Invalid SiddhiApp Name Found for siddhi app "+ siddhiApp +" ",
                Response.Status.INTERNAL_SERVER_ERROR);
    }
}
