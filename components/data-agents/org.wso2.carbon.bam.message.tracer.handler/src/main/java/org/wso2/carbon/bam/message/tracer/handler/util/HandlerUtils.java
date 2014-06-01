/**
 * Copyright (c) 2005 - 2013, WSO2 Inc. (http://www.wso2.com) All Rights Reserved.
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
package org.wso2.carbon.bam.message.tracer.handler.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.wso2.carbon.bam.message.tracer.handler.data.TracingInfo;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;

public class HandlerUtils {

    private static Log log = LogFactory.getLog(HandlerUtils.class);

    public static String getUniqueId() {
        return (String.valueOf(System.nanoTime()) + Math.round(Math.random() * 123456789));
    }

    public static void logTracingInfo(TracingInfo tracingInfo) {
        log.info("Massage Info: Transaction id=" + tracingInfo.getActivityId() +
                "  Message direction=" + tracingInfo.getMessageDirection() +
                "  Server name=" + tracingInfo.getHost() +
                "  Timestamp=" + tracingInfo.getTimestamp() +
                "  Service name=" + tracingInfo.getServiceName() +
                "  Operation Name=" + tracingInfo.getOperationName());
    }

    public static Document convertToDocument(File file) throws Exception {
        DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
        //fac.setNamespaceAware(true);
        try {
            return fac.newDocumentBuilder().parse(file);
        } catch (Exception e) {
            throw new Exception("Error in creating an XML document from file: " +
                    e.getMessage(), e);
        }
    }
}
