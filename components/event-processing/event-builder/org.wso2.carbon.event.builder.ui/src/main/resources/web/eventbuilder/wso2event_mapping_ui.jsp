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
        <tr fromElementKey="inputWso2EventMapping">
            <td colspan="2" class="middle-header">
                <fmt:message key="event.builder.mapping.wso2event"/>
            </td>
        </tr>
        <tr fromElementKey="inputWso2EventMapping">
            <td colspan="2">
                <table>
                    <td class="col-small">
                        <fmt:message key="event.builder.custommapping.enabled"/>
                    </td>
                    <td>
                        <input id="customMappingCheckBox" name="customMapping" type="radio" value="enable"
                               onclick="enableMapping(true);" checked="true" >Enable</input>
                        <input name="customMapping" type="radio" value="disable" onclick="enableMapping(false);" >Disable</input>
                    </td>
                </table>
            </td>
        </tr>
        <tr id="mappingRow" >
            <td colspan="2">
                <h6><fmt:message key="wso2event.mapping.header"/></h6>
                <table class="styledLeft noBorders spacer-bot"
                       id="inputWso2EventDataTable" style="display:none">
                    <thead>
                    <th class="leftCol-med"><fmt:message
                            key="event.builder.property.name"/></th>
                    <th class="leftCol-med"><fmt:message
                            key="event.builder.property.inputtype"/></th>
                    <th class="leftCol-med"><fmt:message
                            key="event.builder.property.valueof"/></th>
                    <th class="leftCol-med"><fmt:message
                            key="event.builder.property.type"/></th>
                    <th><fmt:message key="event.builder.mapping.actions"/></th>
                    </thead>
                    <tbody id="inputWso2EventTBody"></tbody>
                </table>
                <div class="noDataDiv-plain" id="noInputWso2EventData">
                    No WSO2 Event Mappings Defined
                </div>
                <table id="addWso2EventDataTable" class="normal">
                    <tbody>
                    <tr>
                        <td><fmt:message key="event.builder.property.inputtype"/>:
                            <select id="inputWso2EventPropertyInputType">
                                <option value="meta">Meta Data</option>
                                <option value="correlation">Correlation Data</option>
                                <option value="payload">Payload Data</option>
                            </select>
                        </td>
                        <td class="col-small"><fmt:message key="event.builder.property.name"/> :
                        </td>
                        <td>
                            <input type="text" id="inputWso2EventPropertyName"/>
                        </td>
                        <td class="col-small"><fmt:message
                                key="event.builder.property.valueof"/> :
                        </td>
                        <td>
                            <input type="text" id="inputWso2EventPropertyValue"/>
                        </td>
                        <td><fmt:message key="event.builder.property.type"/>:
                            <select id="inputWso2EventPropertyType">
                                <option value="int">int</option>
                                <option value="long">long</option>
                                <option value="double">double</option>
                                <option value="float">float</option>
                                <option value="string">string</option>
                                <option value="boolean">boolean</option>
                            </select>
                        </td>
                        <td><input type="button" class="button"
                                   value="<fmt:message key="add"/>"
                                   onclick="addInputWso2EventProperty()"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>

        </tbody>
    </table>
</fmt:bundle>