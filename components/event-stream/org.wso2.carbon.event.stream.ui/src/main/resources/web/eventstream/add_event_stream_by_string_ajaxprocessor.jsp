<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License"); you may not
  ~ use this file except in compliance with the License. You may obtain a copy
  ~ of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software distributed
  ~ under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
  ~ CONDITIONS OF ANY KIND, either express or implied.  See the License for the
  ~ specific language governing permissions and limitations under the License.
  --%>
<%@ page import="org.wso2.carbon.event.stream.stub.EventStreamAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.stream.ui.EventStreamUIUtils" %>


<%

    String eventStreamDefinitionDtoString = null;

    try {
        EventStreamAdminServiceStub stub = EventStreamUIUtils.getEventStreamAdminService(config, session, request);
        stub.addEventStreamDefinitionAsString(request.getParameter("eventStreamDefinitionString"));

        eventStreamDefinitionDtoString = "{\"success\":\"true\"}";
    } catch (Exception e) {

        char         c = 0;
        int          i;
        int          len = e.getMessage().length();
        StringBuilder escapeString = new StringBuilder(len + 4);
        String       t;

        escapeString.append('"');
        for (i = 0; i < len; i += 1) {
            c = e.getMessage().charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    escapeString.append('\\');
                    escapeString.append(c);
                    break;
                case '/':
                    //                if (b == '<') {
                    escapeString.append('\\');
                    //                }
                    escapeString.append(c);
                    break;
                case '\b':
                    escapeString.append("\\b");
                    break;
                case '\t':
                    escapeString.append("\\t");
                    break;
                case '\n':
                    escapeString.append("\\n");
                    break;
                case '\f':
                    escapeString.append("\\f");
                    break;
                case '\r':
                    escapeString.append("\\r");
                    break;
                default:
                    if (c < ' ') {
                        t = "000" + Integer.toHexString(c);
                        escapeString.append("\\u" + t.substring(t.length() - 4));
                    } else {
                        escapeString.append(c);
                    }
            }
        }
        escapeString.append('"');
        eventStreamDefinitionDtoString = "{\"success\":\"fail\",\"message\":" +escapeString.toString()+ "}";
    }
%>


<%=eventStreamDefinitionDtoString%>
