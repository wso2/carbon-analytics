var dashboardService = dashboardService || {};

//Creates a new cookie
dashboardService.createCookie = function createCookie(name, value, days) {
    if (days) {
        var date = new Date();
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        var expires = "; expires=" + date.toGMTString();
    } else
        var expires = "";
    document.cookie = name + "=" + escape(value) + expires + "; path=/";
}

//Reads the cookie
dashboardService.readCookie = function readCookie(name) {
    var nameEQ = name + "=";
    var ca = document.cookie.split(';');
    for (var i = 0; i < ca.length; i++) {
        var c = ca[i];
        while (c.charAt(0) == ' ')
            c = c.substring(1, c.length);
        if (c.indexOf(nameEQ) == 0)
            return unescape(c.substring(nameEQ.length, c.length));
    }
    return "NA";
}

//erase the cookie
dashboardService.eraseCookie = function eraseCookie(name) {
    dashboardService.createCookie(name, "", -1);
}

//The gadget counter
dashboardService.incrementGadgetCount = function incrementGadgetCount() {
    var gadgetId = dashboardService.readCookie("nextGadgetId");
    var nextGadgetId = Number(gadgetId) + 1;
    dashboardService.createCookie("nextGadgetId", nextGadgetId, 0);
}

//The tab counter
dashboardService.incrementTabCount = function incrementGadgetCount() {
    var gadgetId = dashboardService.readCookie("nextTabId");
    var nextGadgetId = Number(gadgetId) + 1;
    dashboardService.createCookie("nextTabId", nextGadgetId, 0);
}

/**
 * Adds new gadgets. the new gadgets are stored in cookies
 * every gadget has a unique id a combination of "gadget" + next_available_gadgetId
 * Gadget layout is saved containing the tab id in it's name
 * the values in the layout are the variable gadget ID
 */
dashboardService.addGadget = function addGadget(userId, tabId, url, dashboardName, group) {
    var curGadgetId = dashboardService.storeGadget(url);

    var gadgetLayout = dashboardService.getGadgetLayout(userId, tabId, dashboardName);

    // Insert new gadget and save
    dashboardService.setGadgetLayout(userId, tabId, dashboardService.insertGadgetToLayout(gadgetLayout, curGadgetId), dashboardName);


    return true;
}

/**
 * Stores a given gadget URL and returns its unique ID for reference.
 *
 * @param url
 */
dashboardService.storeGadget = function storeGadget(url) {
    var curGadgetId = dashboardService.readCookie("nextGadgetId");
    if (curGadgetId == "NA") {
        curGadgetId = 0;
        var nextGadgetId = curGadgetId + 1;
        dashboardService.createCookie("nextGadgetId", nextGadgetId, 0);
    } else {
        dashboardService.incrementGadgetCount();
    }

    dashboardService.jsonBuilder("gadget-" + curGadgetId, url);

    return curGadgetId;
}

/**
 * Inserts a given gadget ID to an existing layout. Creates a layout using template, if one is not present.
 *
 * @param existingLayout
 * @param gadgetId
 */
dashboardService.insertGadgetToLayout = function (existingLayout, gadgetId) {
    if (existingLayout == "NA") {

        // Create a 3-column JSON layout and make this the first gadget
        existingLayout = {
            "layout":
                    [
                        {
                            "type": "columnContainer",
                            "width": "33%",
                            "layout":
                                    [

                                    ]
                        },
                        {
                            "type": "columnContainer",
                            "width": "33%",
                            "layout":
                                    [

                                    ]
                        },
                        {
                            "type": "columnContainer",
                            "width": "33%",
                            "layout":
                                    [

                                    ]
                        }
                    ]
        };
    } else {
        existingLayout = JSON.parse(existingLayout);
    }
    var newGadgetElement = new Layout();
    newGadgetElement.type = "gadget";
    newGadgetElement.id = gadgetId;

    // Add this new gadget to column 1
    var column1LayoutELementsArray = existingLayout.layout[0].layout;
    column1LayoutELementsArray[column1LayoutELementsArray.length] = newGadgetElement;
    existingLayout.layout[0].layout = column1LayoutELementsArray;

    return JSON.stringify(existingLayout);
}

/**
 * Adds new tab to the layout
 * @param userId
 * @param tabTitle
 * @param dashboardName
 * @return current tab id
 */
dashboardService.addNewTab = function addNewTab(userId, tabTitle, dashboardName) {
    if (dashboardService.hasTabName(userId, tabTitle, dashboardName)) {
        return 0;
    }
    var curTabId = dashboardService.readCookie("nextTabId");
    if (curTabId == "NA") {
        curTabId = 1;
        var nextTabId = curTabId + 1;
        dashboardService.createCookie("nextTabId", nextTabId, 0);
    } else {
        dashboardService.incrementTabCount();
    }

    if (tabTitle == "") {
        tabTitle = "tab " + curTabId;
    }

    dashboardService.createCookie("tab-" + curTabId, tabTitle, 0);

    var tabLayout = dashboardService.readCookie("tabLayout");
    if (tabLayout == "NA") {
        tabLayout = curTabId;
        dashboardService.createCookie("tabLayout", tabLayout, 0);
    } else {
        tabLayout = tabLayout + "," + curTabId;
        dashboardService.createCookie("tabLayout", tabLayout, 0);
    }

    return curTabId;
}

dashboardService.getGadgetLayout = function getGadgetLayout(userId, tabId, dashboardName) {
    var layoutArray = dashboardService.readCookie("gadgetLayout-" + tabId)
    if (layoutArray == '') {
        layoutArray = 'NA';
    }
    return layoutArray;
}

dashboardService.getGadgetPrefs = function getGadgetPrefs(userId, gadgetId, prefId, dashboardName) {

    return dashboardService.readCookie("gadgetPref-" + gadgetId + "-" + prefId);

}

dashboardService.getGadgetUrlsToLayout = function getGadgetUrlsToLayout(userId, tabId, dashboardName) {

    try {
        var storedLayout = JSON.parse(dashboardService.getGadgetLayout("", tabId, ""));
    } catch(e) {
        // No point going forward.
        return "NA";
    }

    var gadgetIds = new Array();

    // Retrieve URLs corresponding to IDs in the JSON layout
    dashboardService.getUrlArrayFromLayout(storedLayout, gadgetIds);

    if (gadgetIds.length > 0) {
        var urlArray = new Array();

        for (var x = 0; x < gadgetIds.length; x++) {
            urlArray[urlArray.length] = dashboardService.getGadgetUrl("gadget-" + gadgetIds[x]);
        }

        return urlArray;
    }

    return "NA";
}

dashboardService.getUrlArrayFromLayout = function getUrlArrayFromLayout(jsonLayout, gadgetIdStore) {
    var layoutElements = jsonLayout.layout;

    for (var x = 0; x < layoutElements.length; x++) {
        if (layoutElements[x].type == "gadget") {
            gadgetIdStore[gadgetIdStore.length] = layoutElements[x].id
        } else {
            // Container element. Recurse.
            dashboardService.getUrlArrayFromLayout(layoutElements[x], gadgetIdStore);
        }
    }
}

dashboardService.getTabLayout = function getTabLayout(userId, dashboardName) {
    var response = dashboardService.readCookie("tabLayout");
    if (response == "NA") {
        dashboardService.createCookie("tabLayout", "0", 0);
        return "0";
    }
    return response;
}

dashboardService.getTabTitle = function getTabTitle(userId, tabId, dashboardName) {
    if (tabId == 0) {
        return "Home"
    }
    var response = dashboardService.readCookie("tab-" + tabId);
    return response;
}

dashboardService.removeGadget = function removeGadget(userId, tabId, gadgetId, dashboardName) {
    var currentLayout = JSON.parse(dashboardService.getGadgetLayout("", tabId, ""));

    currentLayout.layout = dashboardService.removeGadgetFromLayoutArray(gadgetId, currentLayout.layout);

    dashboardService.setGadgetLayout("", tabId, JSON.stringify(currentLayout), "");

    dashboardService.eraseCookie("gadget-" + gadgetId);

    return true;
}

dashboardService.removeGadgetFromLayoutArray = function removeGadgetFromLayoutArray(gadgetId, layoutArray) {

    var modifiedLayoutArray = new Array();

    for (var x = 0; x < layoutArray.length; x++) {
        var currentLayoutElement = layoutArray[x];
        if (currentLayoutElement.type == "gadget") {
            if (!(currentLayoutElement.id == gadgetId)) {
                modifiedLayoutArray[modifiedLayoutArray.length] = currentLayoutElement;
            }
        } else {
            // This is a container element
            currentLayoutElement.layout = dashboardService.removeGadgetFromLayoutArray(gadgetId, currentLayoutElement.layout);
            modifiedLayoutArray[modifiedLayoutArray.length] = currentLayoutElement;
        }
    }

    return modifiedLayoutArray;
}

dashboardService.removeTab = function removeTab(userId, tabId, dashboardName) {
    var curTabLayoutArray = dashboardService.getTabLayout("", "").split(",");
    var newLayout = "";
    for (var i = 0; i < curTabLayoutArray.length; i++) {
        if (curTabLayoutArray[i] != tabId) {
            if (newLayout == "") {
                newLayout = curTabLayoutArray[i];
            } else {
                newLayout = newLayout + "," + curTabLayoutArray[i];
            }
        }
    }

    dashboardService.createCookie("tabLayout", newLayout, 0);

    dashboardService.eraseCookie("tab-" + tabId);
    return true;
}

dashboardService.setGadgetLayout = function setGadgetLayout(userId, tabId, newLayout, dashboardName) {
    try {
        newLayout = decodeURI(newLayout);
    } catch(e) {
    }

    dashboardService.createCookie("gadgetLayout-" + tabId, newLayout, 0);
    return true;
}

dashboardService.setGadgetPrefs = function setGadgetPrefs(userId, gadgetId, prefId, value, dashboardName) {
    dashboardService.createCookie("gadgetPref-" + gadgetId + "-" + prefId, value, 0);
    return true;
}

dashboardService.duplicateTab = function duplicateTab(userId, dashboardName, sourceTabId, newTabName) {
    // Getting the Gadget Layout of the source Tab
    var sourceLayout = JSON.parse(dashboardService.getGadgetLayout(userId, sourceTabId, dashboardName));

    // Creating a new Tab
    var newTabId = dashboardService.addNewTab(userId, newTabName, dashboardName);

    sourceLayout.layout = dashboardService.duplicateGadgetsInLayout(newTabId, sourceLayout.layout);

    dashboardService.setGadgetLayout(userId, newTabId, JSON.stringify(sourceLayout), dashboardName);

    return parseInt(newTabId);
}

dashboardService.duplicateGadgetsInLayout = function duplicateGadgetsInLayout(tabId, layoutElementsArray) {

    for (var x = 0; x < layoutElementsArray.length; x++) {
        var currentLayoutElement = layoutElementsArray[x];
        if (currentLayoutElement.type == "gadget") {
            var gadgetId = currentLayoutElement.id;
            if (gadgetId) {
                currentLayoutElement.id = dashboardService.addGadget(null, tabId, dashboardService.getGadgetUrl(gadgetId), null, null);
            } else {
                // This is a container element. Recurse.
                dashboardService.duplicateGadgetsInLayout(tabId, currentLayoutElement.layout);
            }
        }
    }

    return layoutElementsArray;
}

dashboardService.copyGadget = function copyGadget(userId, tab, dashboardName, sourceGadgetId) {
    dashboardService.jsonBuilder();
    //taking the gadget url from cookie
    //var gadgetUrl = dashboardService.readCookie("gadget-" + sourceGadgetId);
    var gadgetUrl = dashboardService.getGadgetUrl("gadget-" + sourceGadgetId);
    dashboardService.addGadget(null, tab, gadgetUrl, null, "G1#");

}

dashboardService.moveGadgetToTab = function moveGadgetToTab(userId, tab, dashboardName, sourceGadgetId) {

    //takeing the gadget url from cookie
    //var gadgetUrl = dashboardService.readCookie("gadget-" + sourceGadgetId);
    var gadgetUrl = dashboardService.getGadgetUrl("gadget-" + sourceGadgetId);
    dashboardService.addGadget(null, tab, gadgetUrl, null, "G1#");

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

dashboardService.isNewUser = function isNewUser() {
    var curTabId = dashboardService.readCookie("nextGadgetId");
    return (curTabId == "NA");
}

dashboardService.isValidTab = function isValidTab(userId, currentActiveTab, dashboardName) {
    var title = dashboardService.getTabTitle(userId, currentActiveTab, dashboardName);
    if (title == 'NA') {
        return false;
    }

    return true;
}

dashboardService.jsonBuilder = function jsonBuilder(gId, gUrl) {
    var gadget = {};
    gadget['gUrl'] = gUrl;
    var gadgets = dashboardService.getGadgetJson();
    if (gadgets == null) {
        gadgets = {};
    }
    gadgets[gId] = gadget;
    dashboardService.saveGadgetJson(gadgets);
}

dashboardService.saveGadgetJson = function saveGadgetJson(gJson) {
    dashboardService.createCookie("gadgetJson", JSON.stringify(gJson), 0);
}

dashboardService.getGadgetJson = function getGadgetJson() {
    var gadgetJsonText = dashboardService.readCookie("gadgetJson");
    if (gadgetJsonText == 'NA') {
        return null;
    } else {
        return JSON.parse(gadgetJsonText);
    }
}

dashboardService.getGadgetUrl = function getGadgetUrl(gId) {
    var gadgetJsonObj = dashboardService.getGadgetJson();
    return gadgetJsonObj[gId].gUrl;
}

dashboardService.populateDefaultThreeColumnLayout = function populateDefaultThreeColumnLayout(userId, tabId) {
    var threeColumnTemplate = {
        "layout":
                [
                    {
                        "type": "columnContainer",
                        "width": "33%",
                        "layout":
                                [

                                ]
                    },
                    {
                        "type": "columnContainer",
                        "width": "33%",
                        "layout":
                                [

                                ]
                    },
                    {
                        "type": "columnContainer",
                        "width": "33%",
                        "layout":
                                [

                                ]
                    }
                ]
    };

    var column1Layout = new Array();
    var column2Layout = new Array();
    var column3Layout = new Array();

    var p = 0;
    for (p = 0; p < gadgetserver.gadgetSpecUrls.length; p = p + 3) {
        var newGadget = new Layout();
        newGadget.type = "gadget";
        newGadget.id = dashboardService.storeGadget(gadgetserver.gadgetSpecUrls[p]);

        column1Layout[column1Layout.length] = newGadget;
    }

    threeColumnTemplate.layout[0].layout = column1Layout;

    for (p = 1; p < gadgetserver.gadgetSpecUrls.length; p = p + 3) {
        var newGadget = new Layout();
        newGadget.type = "gadget";
        newGadget.id = dashboardService.storeGadget(gadgetserver.gadgetSpecUrls[p]);

        column2Layout[column2Layout.length] = newGadget;
    }

    threeColumnTemplate.layout[1].layout = column2Layout;

    for (p = 2; p < gadgetserver.gadgetSpecUrls.length; p = p + 3) {
        var newGadget = new Layout();
        newGadget.type = "gadget";
        newGadget.id = dashboardService.storeGadget(gadgetserver.gadgetSpecUrls[p]);

        column3Layout[column3Layout.length] = newGadget;
    }

    threeColumnTemplate.layout[2].layout = column3Layout;

    // Store this layout
    dashboardService.setGadgetLayout(userId, currentActiveTab, JSON.stringify(threeColumnTemplate), null);

    return dashboardService.getGadgetLayout(userId, currentActiveTab, null);
}