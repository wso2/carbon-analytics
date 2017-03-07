/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 * CONDITIONS OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */
var elementId;
var rootPath;
function showRegistryBrowser(id, path) {
    elementId = id;
    rootPath = path;
    showResourceTree(id, setValue, path);
}

function showRegistryBrowserWithoutLocalEntries(id, path) {
    elementId = id;
    rootPath = path;
    showResourceTree(id, setValue, path);
}

function showMediationResourceTree(id, path) {
    if ($('local-registry-placeholder')) {
        $('local-registry-placeholder').innerHTML = '<table class="styledLeft"><tbody><tr>' +
            '<td class="leftCol-small" style="border-right:none"><br/>Local Registry</td>' +
            '<td style="border-left:none">' +
            '<div id="local-registry-workArea" name="local-registry-workArea" style="margin-top:5px;margin-bottom:5px;"></div>' +
            '</td></tr></tbody></table>';
        showLocalRegBrowser(id);
        $('local-registry-placeholder').style.display = "";
    } else {
        setTimeout("showMediationResourceTree('" + id + "','" + path + "')", 100);
    }
}

function setValue() {
    if (rootPath == "/_system/config") {
        $(elementId).value = $(elementId).value.replace(rootPath, "conf:");
    } else if (rootPath == "/_system/governance") {
        $(elementId).value = $(elementId).value.replace(rootPath, "gov:");
    }
}