/*
 * Copyright (c) 2005 - 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

var advancedMappingCounter = 0;

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


function loadEventAdapterProperties(messageProperty, eventPublisherInputTable, propertyLoop,
                                           propertyValue, requiredValue, insertRowCount) {

    var tableRow = eventPublisherInputTable.insertRow(insertRowCount);
    var textLabel = tableRow.insertCell(0);
    var displayName = messageProperty.localDisplayName.trim();
    textLabel.innerHTML = displayName;
    var requiredElementId = propertyValue;
    var textPasswordType = "text";
    var hint = ""  ;
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
    } else {

        var option = '';
        jQuery.each(messageProperty.localOptions, function (index, localOption) {
            if (localOption == messageProperty.localDefaultValue) {
                option = option + '<option selected=selected>' + localOption + '</option>';
            }
            else {
                option = option + '<option>' + localOption + '</option>';
            }

        });


        if (hint != undefined && hint != "") {
            inputField.innerHTML = '<div class="' + classType + '"> <select   id="' + requiredElementId + propertyLoop + '" name="' + messageProperty.localKey + '">' + option + '</select><br/> <div class="sectionHelp">' + hint + '</div></div>';
        } else {
            inputField.innerHTML = '<div class="' + classType + '"> <select  id="' + requiredElementId + propertyLoop + '" name="' + messageProperty.localKey + '"  />' + option + ' </div>';
        }
    }
}


function showEventStreamDefinition() {

    var selectedIndex = document.getElementById("streamIdFilter").selectedIndex;
    var streamNameWithVersion = document.getElementById("streamIdFilter").options[selectedIndex].text;
    jQuery.ajax({
                    type:"POST",
                    url:"../eventpublisher/get_streamdefinition_ajaxprocessor.jsp?streamName=" + streamNameWithVersion + "",
                    data:{},
                    contentType:"application/json; charset=utf-8",
                    dataType:"text",
                    async:true,
                    success:function (streamDefinition) {

                        jQuery('#streamDefinitionText').val(streamDefinition.trim());
                    }
                });

    selectedIndex = document.getElementById("mappingTypeFilter").selectedIndex;
    var inputMappingType = document.getElementById("mappingTypeFilter").options[selectedIndex].text;

    var outerDiv = document.getElementById("outerDiv");
    outerDiv.innerHTML = "";

    jQuery.ajax({
        type: "POST",
        url: "../eventpublisher/get_mapping_ui_ajaxprocessor.jsp?mappingType=" + inputMappingType + "&streamNameWithVersion=" + streamNameWithVersion,
        data: {},
        contentType: "text/html; charset=utf-8",
        dataType: "text",
        success: function (ui_content) {
            if (ui_content != null) {
                outerDiv.innerHTML = ui_content;
            }
        }
    });

}


function loadEventAdapterData(adapterSchema) {

    jQuery('#mappingTypeFilter').empty();
    for (var i = 0; i < adapterSchema.localSupportedMessageFormats.length; i++) {
        // for each property, add a text and input field in a row
        jQuery('#mappingTypeFilter').append('<option>' + adapterSchema.localSupportedMessageFormats[i].trim() + '</option>');
    }

    var eventPublisherInputTable = document.getElementById("eventPublisherInputTable");
    // delete message properties related fields
    for (i = eventPublisherInputTable.rows.length - 5; i > 5; i--) {
        eventPublisherInputTable.deleteRow(i);
    }
    var inputProperty = "property_";
    var inputRequiredProperty = "property_Required_";
    var initialRowValue = 6;
    var index=0;
    if (adapterSchema.localOutputEventAdapterStaticProperties != undefined) {

        var tableRow = eventPublisherInputTable.insertRow(initialRowValue);
        var textLabel = tableRow.insertCell(0);
        var header = document.getElementById("staticHeader").getAttribute("name");
        textLabel.innerHTML = '<b><i><span style="color: #666666; ">' + header + '</span></i></b>';
        initialRowValue += 1;

        for (i = 0; i < adapterSchema.localOutputEventAdapterStaticProperties.length; i++) {
            // for each property, add a text and input field in a row
            loadEventAdapterProperties(adapterSchema.localOutputEventAdapterStaticProperties[i], eventPublisherInputTable, i, inputProperty, inputRequiredProperty, initialRowValue + i);
        }
        index = adapterSchema.localOutputEventAdapterStaticProperties.length;
        initialRowValue += adapterSchema.localOutputEventAdapterStaticProperties.length;
    }
    if (adapterSchema.localOutputEventAdapterDynamicProperties != undefined) {

        var tableRow = eventPublisherInputTable.insertRow(initialRowValue);
        var textLabel = tableRow.insertCell(0);
        var header = document.getElementById("dynamicHeader").getAttribute("name");
        textLabel.innerHTML = '<b><i><span style="color: #666666; ">' + header + '</span></i></b>';
        initialRowValue += 1;

        for (i = 0; i < adapterSchema.localOutputEventAdapterDynamicProperties.length; i++) {
            // for each property, add a text and input field in a row
            loadEventAdapterProperties(adapterSchema.localOutputEventAdapterDynamicProperties[i], eventPublisherInputTable, i + index, inputProperty, inputRequiredProperty, initialRowValue + i);
        }
    }
}

function loadEventAdapterRelatedProperties(toPropertyHeader) {

    var selectedIndex = document.getElementById("eventAdapterTypeFilter").selectedIndex;
    var selected_text = document.getElementById("eventAdapterTypeFilter").options[selectedIndex].text;

    jQuery.ajax({
        type:"POST",
        url:"../eventpublisher/get_adapter_properties_ajaxprocessor.jsp?eventAdapterType=" + selected_text + "",
        data:{},
        contentType:"application/json; charset=utf-8",
        dataType:"text",
        async:false,
        success:function (propertiesString) {

            if (propertiesString != null) {
                var jsonObject = JSON.parse(propertiesString);
                loadEventAdapterData(jsonObject);

            }
        }
    });

    showEventStreamDefinition();
}


function handleAdvancedMapping() {
    var outerDiv = document.getElementById("outerDiv");

    if ((advancedMappingCounter % 2) == 0) {
        outerDiv.style.display = "";
    } else {
        outerDiv.style.display = "none";
    }
    advancedMappingCounter = advancedMappingCounter + 1;

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



