/*
 * Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

var cal1;
YAHOO.util.Event.onDOMReady(function () {
    cal1 = new YAHOO.widget.Calendar("cal1", "cal1Container", { title: "Choose a date:", close: true });
    cal1.render();
    cal1.selectEvent.subscribe(calSelectHandler, cal1, true)
});
function showCalendar() {
    cal1.show();
}
function calSelectHandler(type, args, obj) {
    var selected = args[0];
    //var selDate = this.toDate(selected[0]);
    var selDate = args[0][0][0] + "/" + args[0][0][1] + "/" + args[0][0][2];
    var activeTime = document.getElementById("expirationTime");
    clearTextIn(activeTime);
    activeTime.value = selDate;
    cal1.hide();
}