<%--
  ~ Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<script>
    /*
     This landing page has been used to open the execution manager in a new tab,
     since we cannot specify _blank target hyperlinks on carbon console menu items
     */
    var executionUrl = window.location.origin + "/carbon/execution-manager/domains_ajaxprocessor.jsp";
    var consoleUrl = window.location.origin + "/carbon/admin/index.jsp";
    var win = window.open(executionUrl, '_blank');
    win.focus();
    window.location.href = consoleUrl;
</script>