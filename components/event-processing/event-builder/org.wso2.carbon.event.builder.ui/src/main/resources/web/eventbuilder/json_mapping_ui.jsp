<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%--
  ~ Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~  WSO2 Inc. licenses this file to you under the Apache License,
  ~  Version 2.0 (the "License"); you may not use this file except
  ~  in compliance with the License.
  ~  You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~  Unless required by applicable law or agreed to in writing,
  ~  software distributed under the License is distributed on an
  ~  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~  KIND, either express or implied.  See the License for the
  ~  specific language governing permissions and limitations
  ~  under the License.
  --%>

<fmt:bundle basename="org.wso2.carbon.event.builder.ui.i18n.Resources">
    <link type="text/css" href="css/cep.css" rel="stylesheet"/>
    <script type="text/javascript" src="../eventbuilder/js/event_builders.js"></script>

    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr fromElementKey="inputJsonMapping">
            <td colspan="2" class="middle-header">
                <fmt:message key="event.builder.mapping.json"/>
            </td>
        </tr>
        <tr fromElementKey="inputJsonMapping">
            <td colspan="2">

                <h6><fmt:message key="jsonpath.expression.header"/></h6>
                <table class="styledLeft noBorders spacer-bot"
                       id="inputJsonpathExprTable" style="display:none">
                    <thead>
                    <th class="leftCol-med"><fmt:message
                            key="event.builder.property.jsonpath"/></th>
                    <th class="leftCol-med"><fmt:message
                            key="event.builder.property.mappedto"/></th>
                    <th class="leftCol-med"><fmt:message
                            key="event.builder.property.type"/></th>
                    <th class="leftCol-med"><fmt:message
                            key="event.builder.property.default"/></th>
                    <th><fmt:message key="event.builder.mapping.actions"/></th>
                    </thead>
                    <tbody id="inputJsonpathExprTBody"></tbody>
                </table>
                <div class="noDataDiv-plain" id="noInputProperties">
                    No JSONPath expressions properties Defined
                </div>
                <table id="addJsonpathExprTable" class="normal">
                    <tbody>
                    <tr>
                        <td class="col-small"><fmt:message
                                key="event.builder.property.jsonpath"/> :
                        </td>
                        <td>
                            <input type="text" id="inputPropertyValue"/>
                        </td>
                        <td class="col-small"><fmt:message key="event.builder.property.valueof"/> :
                        </td>
                        <td>
                            <input type="text" id="inputPropertyName"/>
                        </td>
                        <td><fmt:message key="event.builder.property.type"/>:
                            <select id="inputPropertyType">
                                <option value="int">int</option>
                                <option value="long">long</option>
                                <option value="double">double</option>
                                <option value="float">float</option>
                                <option value="string">string</option>
                                <option value="boolean">boolean</option>
                            </select>
                        </td>
                        <td class="col-small"><fmt:message
                                key="event.builder.property.default"/></td>
                        <td><input type="text" id="inputPropertyDefault"/></td>
                        <td><input type="button" class="button"
                                   value="<fmt:message key="add"/>"
                                   onclick="addInputJsonProperty()"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>

        </tbody>
    </table>
</fmt:bundle>