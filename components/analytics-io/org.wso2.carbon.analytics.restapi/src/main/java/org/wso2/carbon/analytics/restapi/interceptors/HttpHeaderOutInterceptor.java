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

package org.wso2.carbon.analytics.restapi.interceptors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * This class represents the intercepter which add headers for outgoing responses
 */
public class HttpHeaderOutInterceptor extends AbstractPhaseInterceptor<Message> {
    private static final Log logger = LogFactory.getLog(HttpHeaderOutInterceptor.class);

    public HttpHeaderOutInterceptor() {
        super(Phase.POST_PROTOCOL);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleMessage(Message message) throws Fault {
        Map<String, List<?>> headers = (Map<String, List<?>>) message.get(Message.PROTOCOL_HEADERS);
        try {
            headers.put("Cache-Control", Collections.singletonList("no-cache,must-revalidate"));
            headers.put("Cache-Control", Collections.singletonList("post-check=0,pre-check=0"));
            headers.put("Cache-Control", Collections.singletonList("proxy-revalidate"));
            headers.put("pragma", Collections.singletonList("no-cache"));
            headers.put("Expires", Collections.singletonList(0));
            message.put(Message.PROTOCOL_HEADERS, headers);
        } catch (Exception ce) {
            logger.error("Error while adding headers to Response: " + ce.getMessage(), ce);
        }
    }
}
