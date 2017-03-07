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


function loadEventAdaptorMessageProperties(messageProperty, eventFormatterInputTable, propertyLoop, propertyValue, requiredValue) {

    var property = messageProperty.localDisplayName.trim();
    var tableRow = eventFormatterInputTable.insertRow(eventFormatterInputTable.rows.length);
    var textLabel = tableRow.insertCell(0);
    var displayName = messageProperty.localDisplayName.trim();
    textLabel.innerHTML = displayName;
    var requiredElementId = propertyValue;
    var textPasswordType = "text";
    var hint = ""
    var defaultValue = "";

    if (messageProperty.localRequired) {
        textLabel.innerHTML = displayName + '<span class="required">*</span>';
        requiredElementId = requiredValue;
    }

    if (messageProperty.localSecured) {
        textPasswordType = "password";
    }

    if (messageProperty.localHint != "") {
        hint = messageProperty.localHint;
    }

    if (messageProperty.localDefaultValue != undefined && messageProperty.localDefaultValue != "") {
        defaultValue = messageProperty.localDefaultValue;
    }


    var inputField = tableRow.insertCell(1);
    var classType = 'outputFields';

    if (messageProperty.localOptions == '') {

        if (hint != undefined) {
            inputField.innerHTML = '<div class="' + classType + '"> <input style="width:75%" type="' + textPasswordType + '" id="' + requiredElementId + propertyLoop + '" name="' + messageProperty.localKey + '" value="' + defaultValue + '" class="initE"  /> <br/> <div class="sectionHelp">' + hint + '</div></div>';
        }
        else {
            inputField.innerHTML = '<div class="' + classType + '"> <input style="width:75%" type="' + textPasswordType + '" id="' + requiredElementId + propertyLoop + '" name="' + messageProperty.localKey + '" value="' + defaultValue + '" class="initE"  /> </div>';
        }
    }

    else {

        var option = '';
        jQuery.each(messageProperty.localOptions, function (index, localOption) {
            if (localOption == messageProperty.localDefaultValue) {
                option = option + '<option selected=selected>' + localOption + '</option>';
            }
            else {
                option = option + '<option>' + localOption + '</option>';
            }

        });


        if (hint != undefined) {
            inputField.innerHTML = '<div class="' + classType + '"> <select   id="' + requiredElementId + propertyLoop + '" name="' + messageProperty.localKey + '">' + option + '</select><br/> <div class="sectionHelp">' + hint + '</div></div>';
        }
        else {
            inputField.innerHTML = '<div class="' + classType + '"> <select  id="' + requiredElementId + propertyLoop + '" name="' + messageProperty.localKey + '"  />' + option + ' </div>';
        }


    }


}


function showEventStreamDefinition() {

    var selectedIndex = document.getElementById("streamNameFilter").selectedIndex;
    var selected_text = document.getElementById("streamNameFilter").options[selectedIndex].text;

    jQuery.ajax({
        type: "POST",
        url: "../eventstream/get_streamdefinition_ajaxprocessor.jsp?streamName=" + selected_text + "",
        data: {},
        contentType: "application/json; charset=utf-8",
        dataType: "text",
        async: false,
        success: function (streamDefinition) {

            jQuery('#streamDefinitionText').val(streamDefinition.trim());
        }
    });
}

function loadEventAdaptorRelatedProperties(toPropertyHeader) {

    var selectedIndex = document.getElementById("eventAdaptorNameFilter").selectedIndex;
    var selected_text = document.getElementById("eventAdaptorNameFilter").options[selectedIndex].text;


    jQuery.ajax({
        type: "POST",
        url: "../eventstream/get_mappings_ajaxprocessor.jsp?eventAdaptorName=" + selected_text + "",
        data: {},
        contentType: "application/json; charset=utf-8",
        dataType: "text",
        async: false,
        success: function (mappingTypes) {

            if (mappingTypes != null) {
                mappingTypes = mappingTypes.trim();
                // properties are taken as | separated property names
                var mappings = mappingTypes.split("|=");
                var propertyCount = mappings.length;
                jQuery('#mappingTypeFilter').empty();
                // for each property, add a text and input field in a row
                for (i = 1; i < propertyCount; i++) {
                    if (mappings[i].trim() != "") {
                        jQuery('#mappingTypeFilter').append('<option>' + mappings[i].trim() + '</option>');
                    }
                }

            }
        }
    });


    var eventFormatterInputTable = document.getElementById("eventFormatterInputTable");
// delete message properties related fields
    for (i = eventFormatterInputTable.rows.length - 1; i > 7; i--) {
        eventFormatterInputTable.deleteRow(i);
    }

    jQuery.ajax({
        type: "POST",
        url: "../eventstream/get_Message_Properties_ajaxprocessor.jsp?eventAdaptorName=" + selected_text + "",
        data: {},
        contentType: "application/json; charset=utf-8",
        dataType: "text",
        async: false,
        success: function (propertiesString) {

            if (propertiesString != null) {
                var jsonObject = JSON.parse(propertiesString);

                if (jsonObject != undefined) {
                    var propertyLoop = 0;
                    var inputProperty = "property_";
                    var inputRequiredProperty = "property_Required_";
                    var tableRow = eventFormatterInputTable.insertRow(eventFormatterInputTable.rows.length);
                    tableRow.innerHTML = '<td colspan="2" ><b>' + toPropertyHeader + '</b></td> ';
                    jQuery.each(jsonObject, function (index, messageProperty) {
                        loadEventAdaptorMessageProperties(messageProperty, eventFormatterInputTable, propertyLoop, inputProperty, inputRequiredProperty);
                        propertyLoop = propertyLoop + 1;
                    });
                }

            }
        }
    });

    showMappingContext();
}


function showMappingContext() {

    var selectedIndex = document.getElementById("mappingTypeFilter").selectedIndex;
    var selected_text = document.getElementById("mappingTypeFilter").options[selectedIndex].text;

    var innerDiv1 = document.getElementById("innerDiv1");
    var innerDiv2 = document.getElementById("innerDiv2");
    var innerDiv3 = document.getElementById("innerDiv3");
    var innerDiv4 = document.getElementById("innerDiv4");
    var innerDiv5 = document.getElementById("innerDiv5");

    if (selected_text == 'wso2event') {
        innerDiv1.style.display = "";
        innerDiv2.style.display = "none";
        innerDiv3.style.display = "none";
        innerDiv4.style.display = "none";
        innerDiv5.style.display = "none";
    }

    else if (selected_text == 'text') {
        innerDiv1.style.display = "none";
        innerDiv2.style.display = "";
        innerDiv3.style.display = "none";
        innerDiv4.style.display = "none";
        innerDiv5.style.display = "none";
    }

    else if (selected_text == 'xml') {
        innerDiv1.style.display = "none";
        innerDiv2.style.display = "none";
        innerDiv3.style.display = "";
        innerDiv4.style.display = "none";
        innerDiv5.style.display = "none";
    }

    else if (selected_text == 'map') {
        innerDiv1.style.display = "none";
        innerDiv2.style.display = "none";
        innerDiv3.style.display = "none";
        innerDiv4.style.display = "";
        innerDiv5.style.display = "none";
    }
    else if (selected_text == 'json') {
        innerDiv1.style.display = "none";
        innerDiv2.style.display = "none";
        innerDiv3.style.display = "none";
        innerDiv4.style.display = "none";
        innerDiv5.style.display = "";
    }

}


function enable_disable_Registry(obj) {

    if (jQuery(obj).attr('id') == "registry_text") {
        if ((jQuery(obj).is(':checked'))) {
            var innerInlineRow = document.getElementById("outputTextMappingInline");
            var innerRegistryRow = document.getElementById("outputTextMappingRegistry");
            var inlineRadio = document.getElementById("inline_text");
            inlineRadio.checked = false;
            innerInlineRow.style.display = "none";
            innerRegistryRow.style.display = "";

        }
    }

    else if (jQuery(obj).attr('id') == "inline_text") {
        if ((jQuery(obj).is(':checked'))) {

            var innerInlineRow = document.getElementById("outputTextMappingInline");
            var innerRegistryRow = document.getElementById("outputTextMappingRegistry");
            var registryRadio = document.getElementById("registry_text");
            registryRadio.checked = false;
            innerInlineRow.style.display = "";
            innerRegistryRow.style.display = "none";
        }

    }

    else if (jQuery(obj).attr('id') == "registry_xml") {
        if ((jQuery(obj).is(':checked'))) {
            var innerInlineRow = document.getElementById("outputXMLMappingInline");
            var innerRegistryRow = document.getElementById("outputXMLMappingRegistry");
            var inlineRadio = document.getElementById("inline_xml");
            inlineRadio.checked = false;
            innerInlineRow.style.display = "none";
            innerRegistryRow.style.display = "";
        }
    }

    else if (jQuery(obj).attr('id') == "inline_xml") {
        if ((jQuery(obj).is(':checked'))) {
            var innerInlineRow = document.getElementById("outputXMLMappingInline");
            var innerRegistryRow = document.getElementById("outputXMLMappingRegistry");
            var registryRadio = document.getElementById("registry_xml");
            registryRadio.checked = false;
            innerInlineRow.style.display = "";
            innerRegistryRow.style.display = "none";
        }

    }

    else if (jQuery(obj).attr('id') == "registry_json") {
        if ((jQuery(obj).is(':checked'))) {
            var innerInlineRow = document.getElementById("outputJSONMappingInline");
            var innerRegistryRow = document.getElementById("outputJSONMappingRegistry");
            var inlineRadio = document.getElementById("inline_json");
            inlineRadio.checked = false;
            innerInlineRow.style.display = "none";
            innerRegistryRow.style.display = "";
        }
    }

    else if (jQuery(obj).attr('id') == "inline_json") {
        if ((jQuery(obj).is(':checked'))) {
            var innerInlineRow = document.getElementById("outputJSONMappingInline");
            var innerRegistryRow = document.getElementById("outputJSONMappingRegistry");
            var registryRadio = document.getElementById("registry_json");
            registryRadio.checked = false;
            innerInlineRow.style.display = "";
            innerRegistryRow.style.display = "none";
        }

    }

}



