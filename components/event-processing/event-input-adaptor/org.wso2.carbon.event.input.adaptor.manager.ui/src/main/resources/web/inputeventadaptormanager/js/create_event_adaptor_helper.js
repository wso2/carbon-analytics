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


// this function validate required fields if they are required fields,
// other fields are ignored.In addition to that, the values from each
// field is taken and appended to a string.
// string = propertyName + $ + propertyValue + | +propertyName...

function addEvent(form) {

        var isFieldEmpty = false;
        var inputParameterString = "";
        var inputPropertyCount = 0;

    // all input properties, not required and required are checked
        while (document.getElementById("inputProperty_Required_" + inputPropertyCount) != null ||
        document.getElementById("inputProperty_" + inputPropertyCount) != null) {
    // if required fields are empty
        if ((document.getElementById("inputProperty_Required_" + inputPropertyCount) != null) ) {
        if (document.getElementById("inputProperty_Required_" + inputPropertyCount).value.trim() == "") {
    // values are empty in fields
        isFieldEmpty = true;
        inputParameterString = "";
        break;
        }
else {
    // values are stored in parameter string to send to backend
        var propertyValue = document.getElementById("inputProperty_Required_" + inputPropertyCount).value.trim();
        var propertyName = document.getElementById("inputProperty_Required_" + inputPropertyCount).name;
        inputParameterString = inputParameterString + propertyName + "$=" + propertyValue + "|=";

        }
} else if (document.getElementById("inputProperty_" + inputPropertyCount) != null) {
        var notRequriedPropertyValue = document.getElementById("inputProperty_" + inputPropertyCount).value.trim();
        var notRequiredPropertyName = document.getElementById("inputProperty_" + inputPropertyCount).name;
        if (notRequriedPropertyValue == "") {
        notRequriedPropertyValue = "  ";
        }
inputParameterString = inputParameterString + notRequiredPropertyName + "$=" + notRequriedPropertyValue + "|=";


}
inputPropertyCount++;
}

var reWhiteSpace = new RegExp("^[a-zA-Z0-9_]+$");
// Check for white space
if (!reWhiteSpace.test(document.getElementById("eventNameId").value)) {
        CARBON.showErrorDialog("Invalid character found in event adaptor name.");
        return;
        }
if (isFieldEmpty || (document.getElementById("eventNameId").value.trim() == "")) {
    // empty fields are encountered.
        CARBON.showErrorDialog("Empty inputs fields are not allowed.");
        return;
        }
else {
    // create parameter string
        var selectedIndex = document.getElementById("eventTypeFilter").selectedIndex;
        var selected_text = document.getElementById("eventTypeFilter").options[selectedIndex].text;

        var eventAdaptorName = (document.getElementById("eventNameId").value.trim());

    new Ajax.Request('../inputeventadaptormanager/add_event_ajaxprocessor.jsp', {
        method: 'post',
        asynchronous: false,
        parameters: {eventName: eventAdaptorName, eventType: selected_text,
            inputPropertySet: inputParameterString},
        onSuccess: function (msg) {
            if ("true" == msg.responseText.trim()) {
                form.submit();
            } else {
                CARBON.showErrorDialog("Failed to add event adaptor, Exception: " + msg.responseText.trim());
            }
        }
    })

}

}

function clearTextIn(obj) {
        if (YAHOO.util.Dom.hasClass(obj, 'initE')) {
        YAHOO.util.Dom.removeClass(obj, 'initE');
        YAHOO.util.Dom.addClass(obj, 'normalE');
        textValue = obj.value;
        obj.value = "";
        }
}
function fillTextIn(obj) {
        if (obj.value == "") {
        obj.value = textValue;
        if (YAHOO.util.Dom.hasClass(obj, 'normalE')) {
        YAHOO.util.Dom.removeClass(obj, 'normalE');
        YAHOO.util.Dom.addClass(obj, 'initE');
        }
}
}

// event adaptor properties are taken from back-end and render according to fields
function showEventProperties(propertiesHeader) {

        var eventInputTable = document.getElementById("eventInputTable");
//    jQuery('input[name=eventTypeFilter]').val()
        var selectedIndex = document.getElementById("eventTypeFilter").selectedIndex;
        var selected_text = document.getElementById("eventTypeFilter").options[selectedIndex].text;

    // delete all rows except first two; event name, event type
        for (i = eventInputTable.rows.length - 1; i > 1; i--) {
        eventInputTable.deleteRow(i);
        }

    jQuery.ajax({
        type:"POST",
        url:"../inputeventadaptormanager/get_properties_ajaxprocessor.jsp?eventType=" + selected_text + "",
        data:{},
contentType:"application/json; charset=utf-8",
dataType:"text",
async:false,
success:function (msg) {
        if (msg != null) {

        var jsonObject = JSON.parse(msg);
        var inputEventProperties = jsonObject.localInputEventAdaptorPropertyDtos;

        var tableRow = eventInputTable.insertRow(eventInputTable.rows.length);
        if (inputEventProperties != undefined) {
        var eventInputPropertyLoop = 0;
        var inputProperty = "inputProperty_";
        var inputRequiredProperty = "inputProperty_Required_";
        var inputOptionProperty = "inputOptionProperty";

        tableRow.innerHTML = '<td colspan="2" ><b>'+propertiesHeader+'</b></td> ';

            jQuery.each(inputEventProperties, function (index,
        inputEventProperty) {

        loadEventProperties('input', inputEventProperty, eventInputTable, eventInputPropertyLoop, inputProperty, inputRequiredProperty, 'inputFields')
        eventInputPropertyLoop = eventInputPropertyLoop + 1;

        });
}
}
}
});
}

function loadEventProperties(propertyType, eventProperty, eventInputTable,
eventPropertyLoop, propertyValue, requiredValue, classType
) {

        var property = eventProperty.localDisplayName.trim();
        var tableRow = eventInputTable.insertRow(eventInputTable.rows.length);
        var textLabel = tableRow.insertCell(0);
        var displayName = eventProperty.localDisplayName.trim();
        textLabel.innerHTML = displayName;
        var requiredElementId = propertyValue;
        var textPasswordType = "text";
        var hint = ""
        var defaultValue = "";

        if (eventProperty.localRequired) {
        textLabel.innerHTML = displayName + '<span class="required">*</span>';
        requiredElementId = requiredValue;
        }

if (eventProperty.localSecured) {
        textPasswordType = "password";
        }

if (eventProperty.localHint != "") {
        hint = eventProperty.localHint;
        }

if (eventProperty.localDefaultValue != undefined && eventProperty.localDefaultValue != "") {
        defaultValue = eventProperty.localDefaultValue;
        }


var inputField = tableRow.insertCell(1);

if (eventProperty.localOptions == '') {

        if (hint != undefined) {
        inputField.innerHTML = '<div class="' + classType + '"> <input style="width:75%" type="' + textPasswordType + '" id="' + requiredElementId + eventPropertyLoop + '" name="' + eventProperty.localKey + '" value="' + defaultValue + '" class="initE"  /> <br/> <div class="sectionHelp">' + hint + '</div></div>';
        }
else {
        inputField.innerHTML = '<div class="' + classType + '"> <input style="width:75%" type="' + textPasswordType + '" id="' + requiredElementId + eventPropertyLoop + '" name="' + eventProperty.localKey + '" value="' + defaultValue + '" class="initE"  /> </div>';
        }
}

else {

        var option = '';
    jQuery.each(eventProperty.localOptions, function (index, localOption) {
        if (localOption == eventProperty.localDefaultValue) {
        option = option + '<option selected=selected>' + localOption + '</option>';
        }
else {
        option = option + '<option>' + localOption + '</option>';
        }

});


if (hint != undefined) {
        inputField.innerHTML = '<div class="' + classType + '"> <select  id="' + requiredElementId + eventPropertyLoop + '" name="' + eventProperty.localKey + '" />' + option + ' <br/> <div class="sectionHelp">' + hint + '</div></div>';
        }
else {
        inputField.innerHTML = '<div class="' + classType + '"> <select  id="' + requiredElementId + eventPropertyLoop + '" name="' + eventProperty.localKey + '" />' + option + ' </div>';
        }
}
}
