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

var isCtrl = false;
var ctrlKey = 17; // Ctrl key code in keyboard
var leftArrowKey = 37; //Left arrow key code in keyboard
var rightArrowKey = 39;//Right arrow key code in keyboard

var tabTitle;
var tabTitleAnonymous;

$(document).keyup(function (e) {
    if (e.which == ctrlKey) isCtrl = false;
}).keydown(function (e) {
    if (e.which == ctrlKey) isCtrl = true;
    else if (isCtrl && e.which == rightArrowKey ) {
        currentActiveTab++;
        if (userId != "null") {
            //Get the tab name for a tab id in signed-in mode.
            tabTitle = gadgetserver.getTabTitle(currentActiveTab);
            if (tabTitle == null) {
                currentActiveTab++;
            }
        } else {
            //Get the tab name for a tab id in anonymous mode.
            tabTitleAnonymous = dashboardService.getTabTitle(userId, currentActiveTab, dashboardName);
            if (tabTitleAnonymous == 'NA') {
                currentActiveTab++;
        }
    }
        makeActive(currentActiveTab);
    }
    else if (isCtrl && e.which == leftArrowKey ) {
        if (currentActiveTab > 0) {
            currentActiveTab--;
            if (userId != "null") {
                //Get the tab name for a tab id in signed-in mode.
                tabTitle = gadgetserver.getTabTitle(currentActiveTab);
                if (tabTitle == null) {
                    currentActiveTab--;
                }

            } else {
                //Get the tab name for a tab id in anonymous mode.
                tabTitleAnonymous = dashboardService.getTabTitle(userId, currentActiveTab, dashboardName);
                if (tabTitleAnonymous == 'NA') {
                    currentActiveTab--;
                }
            }
            makeActive(currentActiveTab);
        }
        else if (currentActiveTab == 0) {
            makeActive(currentActiveTab);
        }
    }
});
