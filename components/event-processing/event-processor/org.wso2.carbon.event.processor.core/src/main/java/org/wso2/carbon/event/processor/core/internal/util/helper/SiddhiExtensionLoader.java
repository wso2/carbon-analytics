/*
*  Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.carbon.event.processor.core.internal.util.helper;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.utils.ServerConstants;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class SiddhiExtensionLoader {

    private static final String SIDDHI_CONF_DIR = "siddhi";
    private static final String SIDDHI_EXT_CONF = "siddhi.extension";
    private static final Log log = LogFactory.getLog(SiddhiExtensionLoader.class);

    /**
     * Helper method to load the siddhi config
     */
    public static List<Class> loadSiddhiExtensions() {

        List<Class> extensionList = new ArrayList<Class>();
        String carbonHome = System.getProperty(ServerConstants.CARBON_CONFIG_DIR_PATH);
        String path = carbonHome + File.separator + SIDDHI_CONF_DIR + File.separator + SIDDHI_EXT_CONF;

        // if the agent config file not exists then simply return null.
        File agentConfigFile = new File(path);
        if (!agentConfigFile.exists()) {
            return extensionList;
        }
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(new File(path)))));
            String className;

            while ((className = br.readLine()) != null) {
                // Print the content on the console
                try {
                    extensionList.add(Class.forName(className.trim()));
                } catch (ClassNotFoundException e) {
                    log.error("Cannot load Siddhi extension " + className);
                }
            }
        } catch (IOException e) {
            String errorMessage = "Can not close the input stream for " + path;
            log.error(errorMessage, e);
        }

        return extensionList;
    }

}
