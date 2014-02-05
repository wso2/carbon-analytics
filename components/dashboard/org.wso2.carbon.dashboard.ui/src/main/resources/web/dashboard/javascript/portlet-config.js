/*
 * Copyright 2005-2009 WSO2, Inc. http://wso2.com
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
jQuery(document).ready(function() {

    renderLayout();     

    var settings = {};
    var portal;

    // Initialise the container
    gadgetserver.init();
    gadgetserver.renderGadgets();  

    $(".connect").sortable({
        connectWith: ['.connect'],
        opacity: 0.7,
        revert: true,
        scroll: true,
        delay: 250,
        placeholder: 'block-hover',
        tolerance: 'pointer',
        update: function(event, ui) {
            persistLayout();

        }
    });

    //drawOptionsButton();
});

function clearMenuAndHeader() {
    // Removing the menu panel
    var menuPanel = document.getElementById("menu-panel");
    menuPanel.parentNode.removeChild(menuPanel);

    // Removing the header
    var headerElement = document.getElementById("header");
    headerElement.parentNode.removeChild(headerElement);

    // Fixing body height
    var bodyElement = document.getElementById("body")
    bodyElement.style.height = "500px";
}

function getFFVersion() {
    var versionData = navigator.userAgent || navigator.appVersion;
    var index = versionData.indexOf("Firefox");
    var ffVersion = 0;
    if (index == -1)
        ffVersion = 0;
    ffVersion = parseFloat(versionData.substring(index + "Firefox".length + 1));
    return ffVersion;
}
