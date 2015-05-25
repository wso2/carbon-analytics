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
<script type="text/javascript" src="../eventstream/js/event_stream.js"></script>
<script type="text/javascript" src="../eventstream/js/registry-browser.js"></script>

<script type="text/javascript" src="../resources/js/resource_util.js"></script>
<jsp:include page="../resources/resources-i18n-ajaxprocessor.jsp"/>
<link rel="stylesheet" type="text/css" href="../resources/css/registry.css"/>
<script type="text/javascript" src="../ajax/js/prototype.js"></script>

<script language="JavaScript">
    function selectAllCheckbox(source) {
        var checkboxes = document.getElementsByName('persist');
        for (var i = 0, n = checkboxes.length; i < n; i++) {
            checkboxes[i].checked = source.checked;
        }
    }
</script>
<div>
    <span style="float: left; position: relative; margin-top: 2px;">
        <input type="checkbox" id="eventPersistCheckbox"/> Persist Event Stream
    </span>
</div>
<fmt:bundle basename="org.wso2.carbon.event.stream.ui.i18n.Resources">

    <table id="analyticsIndexTable" class="styledLeft noBorders spacer-bot" style="width:100%" on>
        <thead>
        <tr>
            <th><input type="checkbox" onClick="selectAllCheckbox(this)"/>Persist</th>
            <th>Column Name</th>
            <th>Type</th>
            <th>Primary Key</th>
            <th>Index</th>
            <th>Score Param</th>
        </tr>
        </thead>
        <tbody>
        </tbody>
    </table>
</fmt:bundle>