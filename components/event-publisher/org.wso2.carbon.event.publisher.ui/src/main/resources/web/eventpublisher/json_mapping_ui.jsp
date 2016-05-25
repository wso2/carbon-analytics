<%--
  ~ Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<fmt:bundle basename="org.wso2.carbon.event.publisher.ui.i18n.Resources">
    <link type="text/css" href="../eventpublisher/css/eventPublisher.css" rel="stylesheet"/>
    <script type="text/javascript" src="../eventpublisher/js/event_publisher.js"></script>

    <table class="styledLeft noBorders spacer-bot"
           style="width:100%">
        <tbody>
        <tr name="outputJSONMapping">
            <td colspan="3" class="middle-header">
                <fmt:message key="json.mapping"/>
            </td>
        </tr>
        <tr>
            <td class="leftCol-med" colspan="1"><fmt:message key="output.mapping.content"/><span
                    class="required">*</span></td>
            <td colspan="2">
                <input id="inline_json" type="radio" checked="checked" value="content"
                       name="inline_json" onclick="enable_disable_Registry(this)">
                <label for="inline_json"><fmt:message key="inline.input"/></label>
                <input id="registry_json" type="radio" value="reg" name="registry_json"
                       onclick="enable_disable_Registry(this)">
                <label for="registry_json"><fmt:message key="registry.input"/></label>
            </td>
        </tr>
        <tr name="outputJSONMappingInline" id="outputJSONMappingInline">
            <td colspan="3">
                <p>
                    <textarea id="jsonSourceText"
                              style="border:solid 1px rgb(204, 204, 204); width: 99%;
                                     height: 150px; margin-top: 5px;"
                              name="jsonSource" rows="30"></textarea>
                </p>
            </td>
        </tr>
        <tr name="outputJSONMappingRegistry" style="display:none" id="outputJSONMappingRegistry">
            <td class="leftCol-med" colspan="1"><fmt:message key="resource.path"/><span
                    class="required">*</span></td>
            <td colspan="1">
                <input type="text" id="jsonSourceRegistry" class="initE"
                       value=""
                       style="width:100%"/>
            </td>
            <td class="nopadding" style="border:none" colspan="1">
                <a href="#registryBrowserLink" class="registry-picker-icon-link"
                   style="padding-left:20px"
                   onclick="showRegistryBrowser('jsonSourceRegistry','/_system/config');"><fmt:message
                        key="conf.registry"/></a>
                <a href="#registryBrowserLink"
                   class="registry-picker-icon-link"
                   style="padding-left:20px"
                   onclick="showRegistryBrowser('jsonSourceRegistry', '/_system/governance');"><fmt:message
                        key="gov.registry"/></a>
            </td>
        </tr>
        <tr name="outputJSONMappingRegistryCacheTimeout" style="display:none" id="outputJSONMappingRegistryCacheTimeout">
            <td class="leftCol-med" colspan="1"><fmt:message key="cache.timeout"/><span class="required">*</span></td>
            <td colspan="1"><input type="text" id="textCacheTimeout"
                                               class="initE"
                                               value="0"
                                               style="width:100%"/></td>
        </tr>
        </tbody>
    </table>
</fmt:bundle>