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

// Execute addNewTab method in session aware manner
function sessionAwareAddNewTab() {
    // if signed user
    if (userId != "null") {
        // Run the task (addNewTab) if the session is valid (Success case)
        sessionAwareFunction(addNewTab,
                             'Session timed out. Please login again',
                             sessionFailureFunction );
    }
    // if unsigned user
    else {
        addNewTab();
    }

}

function addNewTab() {

    showInputWizard(jsi18n["what.should.be.tab.name"], addNewTabNextHandler, addNewTabCancelHandler,"",jsi18n["title.new.tab"]+currentActiveTab,jsi18n["add.new.tab"]);
}
function addNewTabUnsignedUser() {

    showInputDialog(jsi18n["what.should.be.tab.name"], addNewTabOkHandler, addNewTabCancelHandler,"",jsi18n["title.new.tab"]+currentActiveTab,jsi18n["add.new.tab"]);
}

function addNewTabCancelHandler() {
    return true;
}

function addNewTabNextHandler(tabTitle, layout) {
    // Obtain a name for this new Tab
    if (tabTitle == null || tabTitle == "") {
        CARBON.showErrorDialog(jsi18n["no.tab.name"],addNewTab());
        return ;
    }

    var resp = dashboardService.addNewTab(userId, tabTitle, dashboardName);

    gadgetserver.tabLayout.push(resp);

    if (resp == 0) {
        CARBON.showErrorDialog(jsi18n["same.tab.name"]);
    }

    if (tabTitle == "") {
        tabTitle = "Tab " + resp;
    }


    if (resp) {
        // Deactivate all tabs
        var tabLinks = document.getElementById("tabmenu").getElementsByTagName("a");
        for (var x = 0; x < tabLinks.length; x++) {
            tabLinks[x].className = "";
        }

        // Create a new tab
        var tabMenu = document.getElementById("tabmenu");
        tabMenu.innerHTML =
                tabMenu.innerHTML + '<li onclick="makeActive(' + resp + ')"><a class="" id="tab' + resp +
                        '">' + tabTitle + '</a></li>';

        // Clear existing tab content
        document.getElementById("tabContent").innerHTML = ""

        // Updating the global pointer
        currentActiveTab = resp;


        // Making the new tab active
        makeActiveTab(currentActiveTab);

        setLayout(layout);
    }
}
function addNewTabOkHandler(tabTitle) {
    // Obtain a name for this new Tab
    if (tabTitle == null || tabTitle == "") {
        CARBON.showErrorDialog(jsi18n["no.tab.name"]);
        return;
    }

    var resp = dashboardService.addNewTab(userId, tabTitle, dashboardName);

    gadgetserver.tabLayout.push(resp);

    if (resp == 0) {
        CARBON.showErrorDialog(jsi18n["same.tab.name"]);
    }

    if (tabTitle == "") {
        tabTitle = "Tab " + resp;
    }


    if (resp) {
        // Deactivate all tabs
        var tabLinks = document.getElementById("tabmenu").getElementsByTagName("a");
        for (var x = 0; x < tabLinks.length; x++) {
            tabLinks[x].className = "";
        }

        // Create a new tab
        var tabMenu = document.getElementById("tabmenu");
        tabMenu.innerHTML =
                tabMenu.innerHTML + '<li onclick="makeActive(' + resp + ')"><a class="" id="tab' + resp +
                        '">' + tabTitle + '</a></li>';

        // Clear existing tab content
        document.getElementById("tabContent").innerHTML = ""

        // Updating the global pointer
        currentActiveTab = resp;


        // Making the new tab active
        makeActiveTab(currentActiveTab);

    }
}

// Execute duplicateActiveTab method in session aware manner
function sessionAwareDuplicateActiveTab() {
    // if signed user
    if (userId != "null") {
        // Run the task (duplicateActiveTab) if the session is valid (Success case)
        sessionAwareFunction(duplicateActiveTab,
                             'Session timed out. Please login again',
                             sessionFailureFunction);
    }
    // if unsigned user
    else {
        duplicateActiveTab();
    }

}

function duplicateActiveTab() {
    var tabTitle = dashboardService.getTabTitle(userId,currentActiveTab,dashboardName);
    showInputDialog(jsi18n["what.should.be.tab.name"], duplicateActiveTabOkHandler, duplicateActiveTabCancelHandler,"",jsi18n["title.clone.tab"]+tabTitle,jsi18n["clone.tab"]);
}

function duplicateActiveTabOkHandler(tabTitle) {
    // Obtain a name for this new Tab
    if (tabTitle == null || tabTitle == "") {
        CARBON.showErrorDialog(jsi18n["no.tab.name"]);
        return;
    }

    var resp = dashboardService.duplicateTab(userId, dashboardName, currentActiveTab, tabTitle);

    gadgetserver.tabLayout.push(resp);

    if (resp == 0) {
        CARBON.showErrorDialog(jsi18n["same.tab.name"]);
    }

    if (tabTitle == "") {
        tabTitle = "Tab " + resp;
    }

    if (resp) {
        // Deactivate all tabs
        var tabLinks = document.getElementById("tabmenu").getElementsByTagName("a");
        for (var x = 0; x < tabLinks.length; x++) {
            tabLinks[x].className = "";
        }

        // Create a new tab
        var tabMenu = document.getElementById("tabmenu");
        tabMenu.innerHTML =
                tabMenu.innerHTML + '<li onclick="makeActive(' + resp + ')"><a class="" id="tab' + resp +
                        '">' + tabTitle + '</a></li>';

        // Clear existing tab content
        document.getElementById("tabContent").innerHTML = ""

        // Updating the global pointer
        currentActiveTab = resp;

        // Making the new tab active
        makeActive(currentActiveTab);

    }
}

function duplicateActiveTabCancelHandler() {
    return true;
}

function makeActiveTab(tabId) {

    var tabLinks = document.getElementById("tabmenu").getElementsByTagName("a");
    for (var x = 0; x < tabLinks.length; x++) {
        tabLinks[x].className = "";
    }

    document.getElementById("tab" + tabId).className = "active";

    // Clear existing tab content
    document.getElementById("tabContent").innerHTML = ""
    document.getElementById("tabContent").style.display = ""

    document.getElementById("maximizedGadget").innerHTML = ""
    document.getElementById("maximizedGadget").style.display = "none"

    // Updating the global pointer
    currentActiveTab = tabId;


}

function makeActive(tabId) {
    // if signed user
    if (userId != "null") {
        // Run the task (tabActivator) if the session is valid (Success case)
        sessionAwareFunction(function() { tabActivator(tabId); },
                             'Session timed out. Please login again',
                             sessionFailureFunction );
    }
    // if unsigned user
    else {
        tabActivator(tabId);
    }

}

// This is used as makeActive() failure function
// This method runs if session time out happened
function sessionFailureFunction() {
    if (dashboardName == null) {
        CARBON.showErrorDialog(jsi18n["session.invalid"]);
        redirectToHttpsUrl('../gsusermgt/login.jsp', backendHttpsPort);
    } else {
        CARBON.showErrorDialog(jsi18n["session.invalid"]);
        redirectToHttpsUrl('../admin/login.jsp', backendHttpsPort);
    }
}

// This is the Tab Activator and it is called inside makeActive() as the success function
function tabActivator(tabId) {
    // if(checkSession(dashboardName)){

    var tabLinks = document.getElementById("tabmenu").getElementsByTagName("a");
    for (var x = 0; x < tabLinks.length; x++) {
        tabLinks[x].className = "";
    }

    document.getElementById("tab" + tabId).className = "active";

    // Clear existing tab content
    document.getElementById("tabContent").innerHTML = ""
    document.getElementById("tabContent").style.display = ""

    document.getElementById("maximizedGadget").innerHTML = ""
    document.getElementById("maximizedGadget").style.display = "none"

    // Updating the global pointer
    currentActiveTab = tabId;
    window.location = 'index.jsp?tab='+tabId+'&name=' + dashboardName;
    // Set the container view to default
    gadgets.container.setView("default");

    if (userId != "null") {

        // Populate the resources to registry if not already done
        var populateStatus = dashboardService.populateDashboardTab(tabId);

        var jsonContent = JSON.parse(dashboardService.getTabContentAsJson(userId, dashboardName, tDomain, tabId));
        gadgetserver.gadgetLayout = jsonContent.gadgetLayout.split(",");
        gadgetserver.gadgetIdandPrefs = getGadgetIdsWithPrefs(jsonContent.gadgets).split("#");
        gadgetserver.tabIdandNames = dashboardService.getTabLayoutWithNames(userId, dashboardName).split(",");
    }


    // Load gadgets for the active tab
    renderLayout();

    // Initialise the container                              //drawOptionsButton();
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

}

function getGadgetIdsWithPrefs(gadgets) {
    var result = '';

    for (var i in gadgets) {
        result += gadgets[i].gadgetId + "$" + gadgets[i].gadgetPrefs;
        result += "#";
    }
    return result.substring(0, result.length - 1);
}

function getGadgetIdsWithPrefs(gadgets) {
    var result = '';

    for (var i in gadgets) {
        result += gadgets[i].gadgetId + "$" + gadgets[i].gadgetPrefs;
        result += "#";
    }
    return result.substring(0, result.length - 1);
}

// Execute removeActiveTab method in session aware manner
function sessionAwareRemoveActiveTab() {
    // if signed user
    if (userId != "null") {
        // Run the task (tabActivator) if the session is valid (Success case)
        sessionAwareFunction(removeActiveTab,
                             'Session timed out. Please login again',
                             sessionFailureFunction);
    }
    // if unsigned user
    else {
        removeActiveTab();
    }

}

function removeActiveTab() {
    if (currentActiveTab == 0) {
        var gheight = 160;
        if(getInternetExplorerVersion() != -1){//IE
            gheight = 270;
        }
        var strDialog = "<div id='dialog' title='WSO2 Carbon'><div id='messagebox-error'><p>" +
                        jsi18n["cannot.delete.tab"] + "</p></div></div>";

            jQuery("#dcontainer").html(strDialog);

            jQuery("#dialog").dialog({
                close:function() {
                    jQuery(this).dialog('destroy').remove();
                    jQuery("#dcontainer").empty();
                    if (closeCallback && typeof closeCallback == "function") {
                        closeCallback();
                    }
                    return false;
                },
                buttons:{
                    "OK":function() {
                        jQuery(this).dialog("destroy").remove();
                        jQuery("#dcontainer").empty();
                        if (callback && typeof callback == "function")
                            callback();
                        return false;
                    }
                },
                height:gheight,
                width:490,
                minHeight:gheight,
                minWidth:330,
                modal:true
            });

    }else {
        confirmDialog(jsi18n["remove.tab"], removeActiveTabAction, currentActiveTab, jsi18n["delete.tab"])
    }}

function removeActiveTabAction(tabId) {

        var resp = dashboardService.removeTab(userId, currentActiveTab, dashboardName);

        if (resp) {
            // Refresh page
            window.location = 'index.jsp?name=' + dashboardName;
        } else {
            CARBON.showErrorDialog(jsi18n["failed.removing.active.tab"]);
        }
}

//Call the method which will change the layout of a tab.
function setLayout(layout) {
    dashboardService.populateCustomLayouts(userId, currentActiveTab, layout, dashboardName);
    makeActive(currentActiveTab);
}

