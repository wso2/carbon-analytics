/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.bam.message.tracer.handler.conf;

import org.w3c.dom.Document;
import org.wso2.carbon.bam.message.tracer.handler.util.HandlerUtils;
import org.wso2.carbon.utils.CarbonUtils;

import javax.xml.bind.JAXBContext;
import java.io.File;

public class MessageTracerConfigurationManager {

    private static final String MSG_TRACER_FILE = "message-tracer-config.xml";

    private static MessageTracerConfiguration configuration;

    private MessageTracerConfigurationManager() {}

    public static MessageTracerConfiguration getMessageTracerConfiguration() throws Exception {
		try {
			File msgTracerConfigFile = new File(CarbonUtils.getCarbonConfigDirPath() + File.separator + "etc" +
                    File.separator + MSG_TRACER_FILE);
			if (msgTracerConfigFile.exists()) {
                Document doc = HandlerUtils.convertToDocument(msgTracerConfigFile);
                JAXBContext ctx = JAXBContext.newInstance(MessageTracerConfiguration.class);
                configuration = (MessageTracerConfiguration) ctx.createUnmarshaller().unmarshal(doc);
			}
		} catch (Exception e) {
			throw new Exception("Error in initializing message tracer configuration: " +
		            e.getMessage(), e);
		}
        return configuration;
	}
    
}
