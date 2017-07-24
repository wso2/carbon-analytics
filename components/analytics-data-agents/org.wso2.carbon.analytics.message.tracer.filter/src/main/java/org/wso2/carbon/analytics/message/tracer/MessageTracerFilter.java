/**
 * Copyright (c) 2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.analytics.message.tracer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.analytics.message.tracer.data.Message;
import org.wso2.carbon.analytics.message.tracer.data.ServerConfig;
import org.wso2.carbon.analytics.message.tracer.internal.publisher.EventPublisher;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Message Trace Filter.
 * Add this filter to web.xml in any servlet container.
 * Add url , username and password  as an init parameter.
 */
public class MessageTracerFilter implements Filter {

    private static final Log log = LogFactory.getLog(MessageTracerFilter.class);

    private static final String MESSAGE_TYPE = "ServletFilter";
    private static final String HEADER_PREFIX = "header-";
    private static final String METHOD_POST = "POST";

    private static final String REMOTE_HOST = "remote-host";
    public static final String SUCCESS = "success";
    public static final String FAULT = "fault";

    private boolean isDumpMessageBody = true;

    private EventPublisher eventPublisher;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        if (log.isDebugEnabled()) {
            log.debug("Initialize message tracer api client.");
        }
        String url = filterConfig.getInitParameter("url");
        if ((url == null) || (url.isEmpty())) {
            log.error("BAM server URL is null or empty. Please add url as a MessageTracerFilter init parameter.");
            return;
        }
        String username = filterConfig.getInitParameter("username");
        if ((username == null) || (username.isEmpty())) {
            log.error("BAM server username is null or empty. Please add username as a MessageTracerFilter init parameter.");
            return;
        }
        String password = filterConfig.getInitParameter("password");
        if ((password == null) || (password.isEmpty())) {
            log.error("BAM server password is null or empty. Please add username as a MessageTracerFilter init parameter.");
            return;
        }
        String dumpMessageBody = filterConfig.getInitParameter("enableDumpMessageBody");
        if (dumpMessageBody != null) {
            isDumpMessageBody = Boolean.parseBoolean(dumpMessageBody);
            if (log.isDebugEnabled()) {
                log.debug("enableDumpMessageBody is " + isDumpMessageBody + ". Please use true/false to configure.");
            }
        }
        eventPublisher = new EventPublisher(new ServerConfig(url, username, password));
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {

        if (servletRequest instanceof HttpServletRequest) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;

            MultiReadHttpServletRequest bufferedRequest = new MultiReadHttpServletRequest(httpServletRequest);

            Message message = new Message();
            message.setHost(bufferedRequest.getLocalAddr());
            message.setResourceUrl(bufferedRequest.getRequestURI());
            if (isDumpMessageBody) {
                message.setPayload(bufferedRequest.getRequestBody());
            }

            String activityId = bufferedRequest.getHeader(MessageTracerConstants.ACTIVITY_ID_KEY);
            if (activityId == null || activityId.isEmpty()) {
                activityId = String.valueOf(System.nanoTime()) + Math.round(Math.random() * 123456789);
            }

            message.setActivityId(activityId);
            message.setType(MESSAGE_TYPE);
            message.setRequestMethod(bufferedRequest.getMethod());
            Map<String, String> additionalValues = getAdditionalDetails(bufferedRequest);
            message.setAdditionalValues(additionalValues);
            message.setTimestamp(System.currentTimeMillis());

            if (servletResponse instanceof HttpServletResponse) {
                HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
                int status = httpServletResponse.getStatus();
                if (status == 200 || status == 202) {
                    message.setStatus(SUCCESS);
                } else {
                    message.setStatus(FAULT);
                }
            }

            eventPublisher.publish(message);

            filterChain.doFilter(bufferedRequest, servletResponse);
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private Map<String, String> getAdditionalDetails(
            MultiReadHttpServletRequest bufferedRequest) {
        Map<String, String> additionalValues = new HashMap<String, String>();
        additionalValues.put(REMOTE_HOST, bufferedRequest.getRemoteHost());
        additionalValues.putAll(getHeaderDetail(bufferedRequest));
        return additionalValues;
    }

    private Map<String, String> getHeaderDetail(HttpServletRequest servletRequest) {
        Enumeration headerNames = servletRequest.getHeaderNames();
        Map<String, String> headers = new HashMap<String, String>();
        while (headerNames.hasMoreElements()) {
            String headerName = (String) headerNames.nextElement();
            if (!MessageTracerConstants.ACTIVITY_ID_KEY.equals(headerName)) {
                headers.put(HEADER_PREFIX + headerName, servletRequest.getHeader(headerName));
            }
        }
        return headers;
    }

    @Override
    public void destroy() {
        if (log.isDebugEnabled()) {
            log.debug("Do nothing in destroy method.");
        }
    }

    private static class MultiReadHttpServletRequest extends HttpServletRequestWrapper {

        public static final String REQUEST_PARAM_SEPARATOR = "&";
        public static final String REQUEST_PARAM_KEY_VALUE_SEPARATOR = "=";
        public static final String URL_ENCODE_MODE = "UTF-8";
        private final Log log = LogFactory.getLog(MultiReadHttpServletRequest.class);

        private ByteArrayOutputStream cachedBytes;
        private Map<String, List<String>> parameters;

        private MultiReadHttpServletRequest(HttpServletRequest request) throws IOException {
            super(request);
            cachedBytes = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024 * 4];
            int n;
            while (-1 != (n = request.getInputStream().read(buffer))) {
                cachedBytes.write(buffer, 0, n);
            }
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            return new CachedServletInputStream();
        }

        private class CachedServletInputStream extends ServletInputStream {
            private ByteArrayInputStream input;

            public CachedServletInputStream() {
                input = new ByteArrayInputStream(cachedBytes.toByteArray());
            }

            @Override
            public int read() throws IOException {
                return input.read();
            }
        }

        @Override
        public BufferedReader getReader() throws IOException {
            return new BufferedReader(new InputStreamReader(getInputStream()));
        }

        private String getRequestBody() throws IOException {
            StringBuilder inputBuffer = new StringBuilder();
            String line;

            BufferedReader reader = new BufferedReader(new InputStreamReader(getInputStream()));
            try {
                do {
                    line = reader.readLine();
                    if (null != line) {
                        inputBuffer.append(line.trim());
                    }
                } while (line != null);
            } catch (IOException ex) {
                log.error("Unable to get request body from request: " + ex.getMessage(), ex);
            } finally {
                try {
                    reader.close();
                } catch (IOException e) {
                    // Just log error
                    log.warn("Unable to close BufferReader: " + e.getMessage(), e);
                }
            }

            return URLDecoder.decode(inputBuffer.toString().trim(), URL_ENCODE_MODE);
        }

        @Override
        public String getParameter(String name) {
            if (METHOD_POST.equals(super.getMethod())) {
                if (parameters == null) {
                    parameters = getParamMapFromMessageBody();
                }
                if (parameters != null) {
                    List<String> values = parameters.get(name);
                    if ((values != null) && (!values.isEmpty())) {
                        return values.get(0);
                    }
                }
                return null;
            } else {
                return super.getParameter(name);
            }
        }

        @Override
        public Map getParameterMap() {
            if (METHOD_POST.equals(super.getMethod())) {
                if (parameters == null) {
                    parameters = getParamMapFromMessageBody();
                }
                Map<String, String[]> valuesMap = new HashMap<String, String[]>();
                if (parameters != null) {
                    for (String key : parameters.keySet()) {
                        List<String> values = parameters.get(key);
                        valuesMap.put(key, values.toArray(new String[values.size()]));
                    }
                }
                return valuesMap;
            } else {
                return super.getParameterMap();
            }
        }

        @Override
        public Enumeration getParameterNames() {
            if (METHOD_POST.equals(super.getMethod())) {
                if (parameters == null) {
                    parameters = getParamMapFromMessageBody();
                }

                if (parameters != null) {
                    return Collections.enumeration(parameters.keySet());
                }
                return super.getParameterNames();
            } else {
                return super.getParameterNames();
            }
        }

        @Override
        public String[] getParameterValues(String name) {
            if (METHOD_POST.equals(super.getMethod())) {
                if (parameters == null) {
                    parameters = getParamMapFromMessageBody();
                }
                if (parameters != null) {
                    List<String> values = parameters.get(name);
                    if (values != null) {
                        return values.toArray(new String[values.size()]);
                    }
                }
                return null;
            } else {
                return super.getParameterValues(name);
            }
        }

        private Map<String, List<String>> getParamMapFromMessageBody() {

            Map<String, List<String>> paramMap = null;
            try {
                String requestBody = getRequestBody();
                String[] originalRequestParamsPair = requestBody.split(REQUEST_PARAM_SEPARATOR);
                if (originalRequestParamsPair != null) {
                    paramMap = new HashMap<String, List<String>>();
                    for (String paramPair : originalRequestParamsPair) {
                        String[] paramKeyValue = paramPair.split(REQUEST_PARAM_KEY_VALUE_SEPARATOR);
                        if (paramKeyValue != null) {
                            String key = paramKeyValue[0];
                            if (paramMap.containsKey(key)) {
                                List<String> values = paramMap.get(key);
                                values.add(paramKeyValue[1]);
                            } else {
                                List<String> values = new ArrayList<String>(2);
                                values.add(paramKeyValue[1]);
                                paramMap.put(key, values);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                log.error("Error occurred while getting parameters from request " + e.getMessage(), e);
            }
            return paramMap;
        }
    }
}
