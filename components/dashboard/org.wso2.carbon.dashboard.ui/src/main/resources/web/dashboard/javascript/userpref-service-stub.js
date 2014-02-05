var dashboardService = dashboardService || {};

dashboardService.addGadget =
function addGadget(userId, tabId, url, dashboardName, group) {
    if (checkSession()) {
        var response = jQuery.ajax({
            type: "POST",
            url: "dashboardServiceClient-ajaxprocessor.jsp",
            data: "func=addGadget&userId=" + userId + "&tabId=" + tabId + "&url=" + url +
                    "&dashboardName=" + dashboardName + "&group=" + group + "&nocache=" + new Date().getTime(),
            async:   false
        }).responseText;

        response = removeCarriageReturns(response);
        return response == "true" || response == "1";
    }
}


dashboardService.addNewTab =
function addNewTab(userId, tabTitle, dashboardName) {
    if (checkSession()) {
        if (dashboardService.hasTabName(userId, tabTitle, dashboardName)) {
            return 0;
        }

        var response = jQuery.ajax({
            type: "POST",
            url: "dashboardServiceClient-ajaxprocessor.jsp",
            data: "func=addNewTab&userId=" + userId + "&tabTitle=" + tabTitle + "&dashboardName=" +
                    dashboardName + "&nocache=" + new Date().getTime(),
            async:   false
        }).responseText;

        response = removeCarriageReturns(response);
        return parseInt(response);
    }
}


dashboardService.getGadgetLayout =
function getGadgetLayout(userId, tabId, dashboardName) {

    var response = jQuery.ajax({
        type: "POST",
        url: "dashboardServiceClient-ajaxprocessor.jsp",
        data: "func=getGadgetLayout&userId=" + userId + "&tabId=" + tabId + "&dashboardName=" +
                dashboardName + "&nocache=" + new Date().getTime(),
        async:   false
    }).responseText;

    response = removeCarriageReturns(response);
    if (response == "") {
        response = "NA";
    }
    return response;
}


dashboardService.getGadgetPrefs =
function getGadgetPrefs(userId, gadgetId, prefId, dashboardName) {

    var response = jQuery.ajax({
        type: "POST",
        url: "dashboardServiceClient-ajaxprocessor.jsp",
        data: "func=getGadgetPrefs&userId=" + userId + "&gadgetId=" + gadgetId + "&prefId=" +
                prefId + "&dashboardName=" + dashboardName + "&nocache=" + new Date().getTime(),
        async:   false
    }).responseText;

    response = removeCarriageReturns(response);
    return response;

}


dashboardService.getGadgetUrlsToLayout =
function getGadgetUrlsToLayout(userId, tabId, dashboardName) {
    var response = jQuery.ajax({
        type: "POST",
        url: "dashboardServiceClient-ajaxprocessor.jsp",
        data: "func=getGadgetUrlsToLayout&userId=" + userId + "&tabId=" + tabId +
                "&dashboardName=" + dashboardName + "&nocache=" + new Date().getTime(),
        async:   false
    }).responseText;

    response = removeCarriageReturns(response);
    return response.split(",");
}


dashboardService.getTabLayout =
function getTabLayout(userId, dashboardName) {
    var response = jQuery.ajax({
        type: "POST",
        url: "dashboardServiceClient-ajaxprocessor.jsp",
        data: "func=getTabLayout&userId=" + userId + "&dashboardName=" + dashboardName +
                "&nocache=" + new Date().getTime(),
        async:   false
    }).responseText;

    response = removeCarriageReturns(response);
    return response;
}


dashboardService.getTabTitle =
function getTabTitle(userId, tabId, dashboardName) {
    var response = jQuery.ajax({
        type: "POST",
        url: "dashboardServiceClient-ajaxprocessor.jsp",
        data: "func=getTabTitle&userId=" + userId + "&tabId=" + tabId +
                "&dashboardName=" +
                dashboardName + "&nocache=" + new Date().getTime(),
        async:   false
    }).responseText;

    response = removeCarriageReturns(response);
    return response;
}


dashboardService.isReadOnlyMode =
function isReadOnlyMode(userId) {
    var response = jQuery.ajax({
        type: "POST",
        url: "dashboardServiceClient-ajaxprocessor.jsp",
        data: "func=isReadOnlyMode&userId=" + userId + "&nocache=" + new Date().getTime(),
        async:   false
    }).responseText;

    response = removeCarriageReturns(response);
    return response == "true" || response == "1";
}


dashboardService.removeGadget =
function removeGadget(userId, tabId, gadgetId, dashboardName) {
    if (checkSession()) {
        var response = jQuery.ajax({
            type: "POST",
            url: "dashboardServiceClient-ajaxprocessor.jsp",
            data: "func=removeGadget&userId=" + userId + "&tabId=" + tabId +
                    "&dashboardName=" +
                    dashboardName + "&gadgetId=" + gadgetId + "&nocache=" + new Date().getTime(),
            async:   false
        }).responseText;

        response = removeCarriageReturns(response);
        return response == "true" || response == "1";
    }
}


dashboardService.removeTab =
function removeTab(userId, tabId, dashboardName) {
    if (checkSession()) {
        var response = jQuery.ajax({
            type: "POST",
            url: "dashboardServiceClient-ajaxprocessor.jsp",
            data: "func=removeTab&userId=" + userId + "&tabId=" + tabId +
                    "&dashboardName=" +
                    dashboardName + "&nocache=" + new Date().getTime(),
            async:   false
        }).responseText;

        response = removeCarriageReturns(response);
        return response == "true" || response == "1";
    }
}


dashboardService.setGadgetLayout =
function setGadgetLayout(userId, tabId, newLayout, dashboardName) {
    var response = jQuery.ajax({
        type: "POST",
        url: "dashboardServiceClient-ajaxprocessor.jsp",
        data: "func=setGadgetLayout&userId=" + userId + "&tabId=" + tabId +
                "&dashboardName=" +
                dashboardName + "&newLayout=" + newLayout + "&nocache=" + new Date().getTime(),
        async:   false
    }).responseText;

    response = removeCarriageReturns(response);
    return response == "true" || response == "1";
}

dashboardService.setGadgetPrefs =
function setGadgetPrefs(userId, gadgetId, prefId, value, dashboardName) {
    var val = encodeHex(value);
    var response = jQuery.ajax({
        type: "POST",
        url: "dashboardServiceClient-ajaxprocessor.jsp",
        data: "func=setGadgetPrefs&userId=" + userId + "&gadgetId=" + gadgetId + "&prefId=" +
                prefId + "&value=" + val + "&dashboardName=" + dashboardName + "&nocache=" +
                new Date().getTime(),
        async:   false
    }).responseText;

    response = removeCarriageReturns(response);
    return response == "true" || response == "1";
}


dashboardService.duplicateTab = function duplicateTab(userId, dashboardName, sourceTabId, newTabName) {
    if (checkSession()) {
        var response = jQuery.ajax({
            type: "POST",
            url: "dashboardServiceClient-ajaxprocessor.jsp",
            data: "func=duplicateTab&userId=" + userId + "&dashboardName=" + dashboardName + "&sourceTabId=" +
                    sourceTabId + "&newTabName=" + newTabName + "&nocache=" + new Date().getTime(),
            async:   false
        }).responseText;

        response = removeCarriageReturns(response);
        return parseInt(response);
    }
}

dashboardService.copyGadget = function copyGadget(userId, tab, dashboardName, sourceGadgetId) {
    if (checkSession()) {
        var response = jQuery.ajax({
            type: "POST",
            url: "dashboardServiceClient-ajaxprocessor.jsp",
            data: "func=copyGadget&userId=" + userId + "&dashboardName=" + dashboardName + "&sourceGadgetId=" +
                    sourceGadgetId + "&tab=" + tab + "&nocache=" + new Date().getTime(),
            async:   false
        }).responseText;

        response = removeCarriageReturns(response);
        return response == "true" || response == "1";
    }
}

dashboardService.moveGadgetToTab = function moveGadgetToTab(userId, tab, dashboardName, sourceGadgetId) {
    if (checkSession()) {
        var response = jQuery.ajax({
            type: "POST",
            url: "dashboardServiceClient-ajaxprocessor.jsp",
            data: "func=moveGadget&userId=" + userId + "&dashboardName=" + dashboardName + "&sourceGadgetId=" +
                    sourceGadgetId + "&tab=" + tab + "&nocache=" + new Date().getTime(),
            async:   false
        }).responseText;

        response = removeCarriageReturns(response);
        return response == "true" || response == "1";
    }
}

dashboardService.hasTabName = function hasTabName(userId, tabName, dashboardName) {

    //takeing the gadget url from cookie
    var tabIds = dashboardService.getTabLayout(userId, dashboardName).split(',');
    for (var x = 0; x < tabIds.length; x++) {
        var tabTitle = dashboardService.getTabTitle(userId, tabIds[x], dashboardName);
        if (tabName == tabTitle) {
            return true;
        }
    }
    return false;
}

//This method is not valid for a logged in user
dashboardService.isNewUser = function isNewUser() {
    return true;
}

dashboardService.isValidTab = function isValidTab(userId, currentActiveTab, dashboardName) {
    var title = dashboardService.getTabTitle(userId, currentActiveTab, dashboardName);
    if (title == 'null') {
        return false;
    }

    return true;
}

dashboardService.populateDefaultThreeColumnLayout = function populateDefaultThreeColumnLayout(userId, tabId) {
    var response = jQuery.ajax({
        type: "POST",
        url: "dashboardServiceClient-ajaxprocessor.jsp",
        data: "func=populateDefaultThreeColumnLayout&userId=" + userId + "&tabId=" + tabId +
                "&nocache=" + new Date().getTime(),
        async:   false
    }).responseText;

    response = removeCarriageReturns(response);
    return response;
}
dashboardService.populateCustomLayouts = function populateCustomLayouts(userId, tabId, layout) {
    var response = jQuery.ajax({
        type: "POST",
        url: "dashboardServiceClient-ajaxprocessor.jsp",
        data: "func=populateCustomLayouts&userId=" + userId + "&tabId=" + tabId + "&layout=" + layout + "&dashboardName=" + dashboardName +
                "&nocache=" + new Date().getTime(),
        async:   false
    }).responseText;

    response = removeCarriageReturns(response);
    return response;
}

dashboardService.getTabContentAsJson = function getTabContentAsJson(userId, dashboardName, tDomain, tabId) {
    var response = jQuery.ajax({
        type: "POST",
        url: "dashboardServiceClient-ajaxprocessor.jsp",
        data: "func=getTabContentAsJson&userId=" + userId + "&dashboardName=" + dashboardName + "&tDomain=" + tDomain + "&tabId=" + tabId + "&nocache=" + new Date().getTime(),
        async:   false
    }).responseText;

    response = removeCarriageReturns(response);
    return response;
}

dashboardService.getTabLayoutWithNames = function getTabContentAsJson(userId, dashboardName) {
    var response = jQuery.ajax({
        type: "POST",
        url: "dashboardServiceClient-ajaxprocessor.jsp",
        data: "func=getTabLayoutWithNames&userId=" + userId + "&dashboardName=" + dashboardName + "&nocache=" + new Date().getTime(),
        async:   false
    }).responseText;

    response = removeCarriageReturns(response);
    return response;
}

dashboardService.populateDashboardTab = function populateDashboardTab(tabId) {
    var response = jQuery.ajax({
        type: "POST",
        url: "dashboardServiceClient-ajaxprocessor.jsp",
        data: "func=populateDashboardTab&tabId=" + tabId + "&nocache=" + new Date().getTime(),
        async:   false
    }).responseText;

    response = removeCarriageReturns(response);
    return response;
}