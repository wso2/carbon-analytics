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

var gadgetserver = {};

gadgetserver.LayoutManager = function() {
    gadgets.LayoutManager.call(this);
};

gadgetserver.LayoutManager.inherits(gadgets.LayoutManager);

gadgetserver.LayoutManager.prototype.getGadgetChrome = function(gadget) {
    var chromeId = 'gadget-chrome_' + gadget.id;
    return chromeId ? document.getElementById(chromeId) : null;
};


gadgetserver.init = function() {
    // Reset the gadget instance counter
    gadgets.container.nextGadgetInstanceId_ = 0;
    gadgets.container.layoutManager = new gadgetserver.LayoutManager();

    // Setting browser language
    gadgets.container.setLanguage(localLanguage);

    // Setting country
    gadgets.container.setCountry(localCountry);

    // Initializing the pub-sub router
    gadgets.pubsubrouter.init(function(moduleId) {
        return gadgets.container.getGadget(this.getGadgetIdFromModuleId(moduleId)).specUrl;
    });
};

gadgetserver.renderGadgets = function() {
    //The backend service sends Relative URLs from the registry to make it fully qualified
    var gadgetSanUrls = sanitizeUrls(gadgetserver.gadgetSpecUrls);
    getGadgetsJSON(gadgetSanUrls);
};

gadgetserver.renderGadgetsCallBack = function(json) {
    var gadgetSanUrls = sanitizeUrls(gadgetserver.gadgetSpecUrls);
    var metadataArray = json;
    var metaDataOrderedArray = new Array();

    for (var i = 0; i < gadgetSanUrls.length; ++i) {
        var specUrl = gadgetSanUrls[i];
        for (var x = 0; x < metadataArray.gadgets.length; ++x) {
            var metadata = metadataArray.gadgets[x];
            if (specUrl == metadata['url']) {
                metadata['specUrl'] = specUrl;
                metaDataOrderedArray[i] = metadata;
            }
        }

    }
    //To avoid the chance that the urls getting equal
    for (var x = 0; x < metaDataOrderedArray.length; ++x) {
        var metad = metaDataOrderedArray[x];
        var gadget = gadgets.container.createGadget(metad);
        gadget.secureToken = escape(gadgetserver.generateSecureToken(gadget.specUrl));
        gadgets.container.addGadget(gadget);
        gadgets.container.renderGadget(gadget);
    }
    gadgetserver.drawOptionsButton();
};

gadgetserver.refreshGadgetLayout = function() {
    gadgetserver.gadgetLayout = dashboardService.getGadgetLayout(userId, currentActiveTab, dashboardName).split(",");
};

gadgetserver.generateSecureToken = function(gadgetUrl) {
    // TODO: Use a less silly mechanism of mapping a gadget URL to an appid
    var appId = 0;
    for (var i = 0; i < gadgetUrl.length; i++) {
        appId += gadgetUrl.charCodeAt(i);
    }
    var fields = [userId, userId, appId, "shindig", gadgetUrl, "0", "default"];
    for (var i = 0; i < fields.length; i++) {
        // escape each field individually, for metachars in URL
        fields[i] = escape(fields[i]);
    }
    return fields.join(":");
}

gadgetserver.saveNewUserGadgets = function() {
    jQuery("#newGadgetsPane").hide("slow");
    var checkGroup = document.newGadgetForm.checkgroup;
    var madeChange = false;
    if (checkGroup.tagName == 'INPUT') {
        gadgetserver.refreshGadgetLayout();
        if (checkGroup.checked == true) {
            if (gadgetserver.gadgetLayout == null || gadgetserver.gadgetLayout == 'NA') {
                dashboardService.addGadget(userId, currentActiveTab, checkGroup[i].value, dashboardName, 'G1#');
            } else if (gadgetserver.gadgetLayout.length % 3 == 0) {
                dashboardService.addGadget(userId, currentActiveTab, checkGroup[i].value, dashboardName, 'G1#');
            } else if (gadgetserver.gadgetLayout.length % 3 == 1) {
                dashboardService.addGadget(userId, currentActiveTab, checkGroup[i].value, dashboardName, 'G2#');
            } else if (gadgetserver.gadgetLayout.length % 3 == 2) {
                dashboardService.addGadget(userId, currentActiveTab, checkGroup[i].value, dashboardName, 'G3#');
            }
            madeChange = true;
        }
    } else {
        for (i = 0; i < checkGroup.length; i++) {
            gadgetserver.refreshGadgetLayout();
            if (checkGroup[i].checked == true) {
                if (gadgetserver.gadgetLayout == null || gadgetserver.gadgetLayout == 'NA') {
                    dashboardService.addGadget(userId, currentActiveTab, checkGroup[i].value, dashboardName, 'G1#');
                } else if (gadgetserver.gadgetLayout.length % 3 == 0) {
                    dashboardService.addGadget(userId, currentActiveTab, checkGroup[i].value, dashboardName, 'G1#');
                } else if (gadgetserver.gadgetLayout.length % 3 == 1) {
                    dashboardService.addGadget(userId, currentActiveTab, checkGroup[i].value, dashboardName, 'G2#');
                } else if (gadgetserver.gadgetLayout.length % 3 == 2) {
                    dashboardService.addGadget(userId, currentActiveTab, checkGroup[i].value, dashboardName, 'G3#');
                }
                madeChange = true;
            }

        }
    }

    gadgetserver.cancelPane();

    if (madeChange) {
        makeActive(currentActiveTab);
    }
};

gadgetserver.activateAddButton = function () {
    var actButton = document.getElementById("addButton");
    var checkGroup = document.newGadgetForm.checkgroup;
    if (checkGroup.length > 0) {
        for (var i = 0; i < checkGroup.length; i++) {
            if(actButton.disabled == true) {
                if (checkGroup[i].checked) {
                    actButton.disabled = false;
                    break;
                }
            } else{
                if (checkGroup[i].checked) {
                    actButton.disabled = false;
                    break;
                } else{
                    actButton.disabled = true;
                }
            }
        }
    }

};

gadgetserver.cancelPane = function () {
    var checkGroup = document.newGadgetForm.checkgroup;
    if (checkGroup.length > 0) {
        for (var i = 0; i < checkGroup.length; i++) {
            if (checkGroup[i].checked) {
                checkGroup[i].checked = false;
            }
        }
    }

    jQuery("#newGadgetsPane").hide("slow");
};

gadgetserver.showExtraGadgets = function() {
    var checkGroup = document.newGadgetForm.checkgroup;
    if (checkGroup.length > 0) {
        for (i = 0; i < checkGroup.length; i++) {
            if (checkGroup[i].checked == true) {
                checkGroup[i].checked = false;
            }
        }
        jQuery("#newGadgetsPane").show("slow");
    }
};

gadgetserver.drawOptionsButton = function() {
//function drawOptionsButton() {
    //var tabIds = dashboardService.getTabLayout(userId, dashboardName).split(',');
    var tabIds = gadgetserver.tabLayout;
    var options = {minWidth: 120, arrowSrc: 'images/arrow_right.gif'};
    jQuery.noConflict();

    jQuery('.opts').each(function() {
            //creating a menu without items
            var menu = new jQuery.Menu('#' + jQuery(this).attr('id'), null, options);

            //adding items to the menu
            menu.addItems([
                new jQuery.MenuItem({src: 'Duplicate This Gadget', url: 'javascript:gadgets.container.getGadget(' + jQuery(this).attr('id') + ').copyGadget()'}, options),
                new jQuery.MenuItem({src: ''}), /* separator */
                new jQuery.MenuItem({src: 'Settings', url: 'javascript:gadgets.container.getGadget(' + jQuery(this).attr('id') + ').handleOpenUserPrefsDialog()'}, options),
                new jQuery.MenuItem({src: ''}) /* separator */
            ]);
            var itemWithSubmenu = new jQuery.MenuItem({src: 'Copy To', url: 'javascript:;'}, options);

            var itemArr = new Array();
            var count = 0;
            for (var i = 0; i < tabIds.length; i++) {
                if (userId != "null") {
                    var tabTitle = gadgetserver.getTabTitle(tabIds[i]);
                } else {
                    var tabTitle = dashboardService.getTabTitle(userId, tabIds[i], dashboardName);
                }
                if (currentActiveTab != tabIds[i]) {
                    itemArr[count] = new jQuery.MenuItem({src: tabTitle, url:'javascript:gadgets.container.getGadget(' + jQuery(this).attr('id') + ').moveGadgetToTab(' + tabIds[i] + ')'}, options);
                    count++;
                }
            }

            //creating a menu with items (as child of itemWithSubmenu)
            new jQuery.Menu(itemWithSubmenu, itemArr, options);

            //adding the submenu to the main menu
            menu.addItem(itemWithSubmenu);

            menu.addItem(new jQuery.MenuItem({src: 'About Gadget', url:'javascript:gadgets.container.getGadget(' + jQuery(this).attr('id') + ').handleABoutGadget()'}, options));

        }
    )

};

gadgetserver.getTabTitle = function(tabId) {
    for (var i = 0; i < gadgetserver.tabIdandNames.length; i++) {
        if (gadgetserver.tabIdandNames[i].split("-")[0] == tabId) {
            return gadgetserver.tabIdandNames[i].split("-")[1]
        }
    }
    return null;
};

gadgetserver.getPrefsForGadget = function(gadgetId) {
    for (var i = 0; i < gadgetserver.gadgetIdandPrefs.length; i++) {
        if (gadgetserver.gadgetIdandPrefs[i].split("$")[0] == gadgetId) {
            return gadgetserver.gadgetIdandPrefs[i].split("$")[1];
        }
    }
    return null;
};


