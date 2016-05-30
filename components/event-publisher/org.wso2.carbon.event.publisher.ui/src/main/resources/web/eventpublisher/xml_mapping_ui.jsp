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
        <tr name="outputXMLMapping">
            <td colspan="3" class="middle-header">
                <fmt:message key="xml.mapping"/>
            </td>
        </tr>
        <tr>
            <td class="leftCol-med" colspan="1"><fmt:message key="output.mapping.content"/><span
                    class="required">*</span></td>
            <td colspan="2">
                <input id="inline_xml" type="radio" checked="checked" value="content"
                       name="inline_xml" onclick="enable_disable_Registry(this)">
                <label for="inline_xml"><fmt:message key="inline.input"/></label>
                <input id="registry_xml" type="radio" value="reg" name="registry_xml"
                       onclick="enable_disable_Registry(this)">
                <label for="registry_xml"><fmt:message key="registry.input"/></label>
            </td>
        </tr>
        <tr name="outputXMLMappingInline" id="outputXMLMappingInline">
            <td colspan="3">
                <p>
                    <textarea id="xmlSourceText"
                              style="border:solid 1px rgb(204, 204, 204); width: 99%;
                                     height: 150px; margin-top: 5px;"
                              name="xmlSource" rows="30"></textarea>
                </p>
            </td>
        </tr>
        <tr name="outputXMLMappingRegistry" style="display:none" id="outputXMLMappingRegistry">
            <td class="leftCol-med" colspan="1"><fmt:message key="resource.path"/><span
                    class="required">*</span></td>
            <td colspan="1">
                <input type="text" id="xmlSourceRegistry" class="initE" value="" style="width:100%"/>
            </td>
            <td class="nopadding" style="border:none" colspan="1">
                <a href="#registryBrowserLink" class="registry-picker-icon-link"
                   style="padding-left:20px"
                   onclick="showRegistryBrowser('xmlSourceRegistry','/_system/config');"><fmt:message
                        key="conf.registry"/></a>
                <a href="#registryBrowserLink"
                   class="registry-picker-icon-link"
                   style="padding-left:20px"
                   onclick="showRegistryBrowser('xmlSourceRegistry', '/_system/governance');"><fmt:message
                        key="gov.registry"/></a>
            </td>
        </tr>
        <tr name="outputXMLMappingRegistryCacheTimeout" style="display:none" id="outputXMLMappingRegistryCacheTimeout">
            <td class="leftCol-med" colspan="1"><fmt:message key="cache.timeout"/><span class="required">*</span></td>
            <td colspan="1"><input type="text" id="textCacheTimeout"
                                               class="initE"
                                               value="0"
                                               style="width:100%"/></td>
        </tr>
        </tbody>
    </table>
</fmt:bundle>