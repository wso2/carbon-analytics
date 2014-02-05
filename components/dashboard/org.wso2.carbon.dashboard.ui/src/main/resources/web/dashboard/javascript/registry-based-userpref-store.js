/*
 * Copyright 2005-2008 WSO2, Inc. http://wso2.com
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

/**
 * @fileoverview Implements the gadgets.UserPrefStore interface using a WSO2 Registry based implementation.
 *
 */

/**
 * WSO2 Registry based user preference store.
 * @constructor
 */
gadgets.RegistryBasedUserPrefStore = function() {
    gadgets.UserPrefStore.call(this);
};

gadgets.prefsObj = {};
gadgets.prefsObj.gadgetsCol = {};

gadgets.RegistryBasedUserPrefStore.inherits(gadgets.UserPrefStore);

gadgets.RegistryBasedUserPrefStore.prototype.USER_PREFS_PREFIX =
        'gadgetUserPrefs-';

gadgets.RegistryBasedUserPrefStore.prototype.getPrefs = function(gadget) {
    var userPrefs = {};

    var prefId = this.USER_PREFS_PREFIX + gadget.id;

    // Using the JS Stub to do a Synchronous call
    if (userId != "null") {
        var response = gadgetserver.getPrefsForGadget(gadget.id);
        /*        if (response == null) {
         var response = dashboardService.getGadgetPrefs(userId, gadget.id, prefId, dashboardName);
         }*/
    } else {
        var response = dashboardService.getGadgetPrefs(userId, gadget.id, prefId, dashboardName);
    }

    if (response != "NA" && response != null) {
        var pairs = response.split('&');
        for (var i = 0; i < pairs.length; i++) {
            var nameValue = pairs[i].split('=');
            var name = decodeURIComponent(nameValue[0]);
            var value = decodeURIComponent(nameValue[1]);
            userPrefs[name] = value;
        }
    }

    return userPrefs;
};

gadgets.RegistryBasedUserPrefStore.prototype.savePrefs = function(gadget) {
    var pairs = [];
    for (var name in gadget.getUserPrefs()) {
        var value = gadget.getUserPref(name);
        var pair = encodeURIComponent(name) + '=' + encodeURIComponent(value);
        pairs.push(pair);
    }

    var prefId = this.USER_PREFS_PREFIX + gadget.id;
    //   var value = encodeHex(pairs.join('&'));
    var value = pairs.join('&');

    if (userId != "null") {
        var userPrefs = gadgetserver.getPrefsForGadget(gadget.id);
        if (value != "" && value != "=undefined") {
            if (userPrefs != value) {
                var thisGadget = {};
                thisGadget["prefId"] = prefId;
                thisGadget["value"] = value;
                gadgets.prefsObj.gadgetsCol[gadget.id] = thisGadget;
                dashboardService.setGadgetPrefs(userId, gadget.id, prefId, value, dashboardName);
            }
        }
    } else {
        // Calling the backend service via JS stub
        dashboardService.setGadgetPrefs(userId, gadget.id, prefId, value, dashboardName);
    }

    gadgets.prefsObj;
};


gadgets.Container.prototype.userPrefStore =
        new gadgets.RegistryBasedUserPrefStore();
