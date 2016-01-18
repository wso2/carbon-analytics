<%@ page import="org.wso2.carbon.analytics.stream.persistence.stub.EventStreamPersistenceAdminServiceStub" %>
<%@ page import="org.wso2.carbon.event.stream.ui.EventStreamUIUtils" %>
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
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<script type="text/javascript" src="../eventstream/js/event_stream.js"></script>
<script type="text/javascript" src="../eventstream/js/registry-browser.js"></script>

<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>
<style type="text/css">
    fieldset {
        border: none;
        padding: 0;
    }
</style>

<%
    EventStreamPersistenceAdminServiceStub persistenceAdminServiceInnerStub =
            EventStreamUIUtils.getEventStreamPersistenceAdminService(config, session, request);
    if (EventStreamUIUtils.isEventStreamPersistenceAdminServiceAvailable(persistenceAdminServiceInnerStub)) {
        String[] recordStoreNames = persistenceAdminServiceInnerStub.listRecordStoreNames();
        pageContext.setAttribute("recordStoreNames", recordStoreNames, PageContext.PAGE_SCOPE);
    }
%>

<fmt:bundle basename="org.wso2.carbon.event.stream.ui.i18n.Resources">
    <table class="styledLeft noBorders spacer-bot" style="width:100%">
        <tbody>
        <tr>
            <td class="middle-header">
                <div>
                    <span style="float: left; position: relative; margin-top: 2px;">
                        <input type="checkbox" id="eventPersistCheckbox" onchange="enableAttribute(this)"/><fmt:message
                            key="persist.event.stream"/>
                    </span>
                </div>
            </td>
        </tr>
        </tbody>
    </table>
    <fieldset id="attributeFieldSet">
        <table class="styledLeft noBorders spacer-bot" style="width:100%">
            <tbody>
            <tr>
                <td class="col-small"><h6><fmt:message key="recordstore.name"/></h6>
                    <select id="recordStoreSelect">
                        <c:forEach items="${recordStoreNames}" var="name">
                            <option value="<c:out value="${name}" />"><c:out value="${name}"/></option>
                        </c:forEach>
                    </select>
                </td>
            </tr>
            </tbody>
        </table>
        <table class="styledLeft noBorders spacer-bot" style="width:100%">
            <tbody>
            <tr id="metaData">
                <td>
                    <h6><fmt:message key="attribute.data.type.meta"/></h6>

                    <table id="metaIndexTable" class="styledLeft noBorders spacer-bot">
                        <thead>
                        <tr>
                            <th><input type="checkbox" id="metaPersistCheckbox"
                                       onchange="checkAllMeta(this, 'meta')"/><fmt:message
                                    key="attribute.persist"/></th>
                            <th><fmt:message key="attribute.name"/></th>
                            <th><fmt:message key="attribute.type"/></th>
                            <th><fmt:message key="attribute.primay"/></th>
                            <th><fmt:message key="attribute.index"/></th>
                            <th><fmt:message key="attribute.scoreParam"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        </tbody>
                    </table>
                    <div class="noDataDiv-plain" id="noOutputMetaIndexData">
                        <fmt:message key="no.meta.attributes.defined"/>
                    </div>
                </td>
            </tr>
            <tr id="correlationData">
                <td>
                    <h6><fmt:message key="attribute.data.type.correlation"/></h6>
                    <table id="correlationIndexTable" class="styledLeft noBorders spacer-bot">
                        <thead>
                        <tr>
                            <th><input type="checkbox" id="correlationPersistCheckbox" onchange="checkAllMeta(this,
                        'correlation')"/><fmt:message
                                    key="attribute.persist"/></th>
                            <th><fmt:message key="attribute.name"/></th>
                            <th><fmt:message key="attribute.type"/></th>
                            <th><fmt:message key="attribute.primay"/></th>
                            <th><fmt:message key="attribute.index"/></th>
                            <th><fmt:message key="attribute.scoreParam"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        </tbody>
                    </table>
                    <div class="noDataDiv-plain" id="noOutputCorrelationIndexData">
                        <fmt:message key="no.correlation.attributes.defined"/>
                    </div>
                </td>
            </tr>
            <tr id="payloadData">
                <td>
                    <h6><fmt:message key="attribute.data.type.payload"/></h6>

                    <table id="payloadIndexTable" class="styledLeft noBorders spacer-bot">
                        <thead>
                        <tr>
                            <th><input type="checkbox" id="payloadPersistCheckbox" onchange="checkAllMeta(this,
                        'payload')"/><fmt:message
                                    key="attribute.persist"/></th>
                            <th><fmt:message key="attribute.name"/></th>
                            <th><fmt:message key="attribute.type"/></th>
                            <th><fmt:message key="attribute.primay"/></th>
                            <th><fmt:message key="attribute.index"/></th>
                            <th><fmt:message key="attribute.scoreParam"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        </tbody>
                    </table>
                    <div class="noDataDiv-plain" id="noOutputPayloadIndexData">
                        <fmt:message key="no.payload.attributes.defined"/>
                    </div>
                </td>
            </tr>
            <tr>
                <td>
                    <h6><fmt:message key="attribute.data.type.arbitrary"/></h6>

                    <div class="noDataDiv-plain" id="noOutputArbitraryData">
                        <fmt:message key="no.arbitrary.attributes.defined"/>
                    </div>
                    <table id="arbitraryIndexTable" class="styledLeft noBorders spacer-bot" style="display:none">
                        <thead>
                        <tr>
                            <th><fmt:message key="attribute.name"/></th>
                            <th><fmt:message key="attribute.type"/></th>
                            <th><fmt:message key="attribute.primay"/></th>
                            <th><fmt:message key="attribute.index"/></th>
                            <th><fmt:message key="attribute.scoreParam"/></th>
                            <th><fmt:message key="actions"/></th>
                        </tr>
                        </thead>
                        <tbody>
                        </tbody>
                    </table>
                    <table id="addArbitraryData" class="normal">
                        <tbody>
                        <tr>
                            <td class="col-small"><fmt:message key="attribute.name"/> : <input type="text"
                                                                                               id="outputArbitraryDataPropName"/>
                            </td>
                            <td class="col-small"><fmt:message key="attribute.type"/> :
                                <select id="outputArbitraryDataPropType" onchange="changeScoreParam(this)">
                                    <option value="INTEGER">INTEGER</option>
                                    <option value="STRING">STRING</option>
                                    <option value="FACET">FACET</option>
                                    <option value="LONG">LONG</option>
                                    <option value="BOOLEAN">BOOLEAN</option>
                                    <option value="FLOAT">FLOAT</option>
                                    <option value="DOUBLE">DOUBLE</option>
                                </select>
                            </td>
                            <td class="col-small"><fmt:message key="attribute.primay"/> : <input type="checkbox"
                                                                                                 id="eventPersistPrimaryKeyCheckbox"/>
                            </td>
                            <td class="col-small"><fmt:message key="attribute.index"/> : <input type="checkbox"
                                                                                                id="eventPersistIndexCheckbox"/>
                            </td>
                            <td class="col-small"><fmt:message key="attribute.scoreParam"/> : <input type="checkbox"
                                                                                                     id="eventPersistScoreParamCheckbox"/>
                            </td>
                            <td><input type="button" class="button" value="<fmt:message key="add"/>"
                                       onclick="addArbitraryAttribute()"/>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </td>
            </tr>
            </tbody>
        </table>
        <table class="styledLeft noBorders spacer-bot" style="width:100%">
            <tr>
                <td align="left">
                    <input type="button" class="ninjaButton" value="<fmt:message key="advanced.options"/>"
                           onclick="showHideAttribute()" style="float: left"/>
                    <div class="noDataDiv-advanced" id="advancedOptions" style="display: none">
                        <input type="checkbox" id="schemaReplaceCheckbox"/><fmt:message key="merge.schema"/>
                    </div>
                </td>
            </tr>
        </table>
    </fieldset>
</fmt:bundle>