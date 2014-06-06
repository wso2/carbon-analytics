<%@ taglib prefix="sql_rt" uri="http://java.sun.com/jstl/sql_rt" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>

<% String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);

    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);


%>
<script type="text/javascript">
function showJmxMbeans() {

    //check the availability of JMX credentials
    var valid = true;
    if (jQuery("#jmxServerUrl").val() == "") {
        valid = false;
    }
    if (jQuery("#jmxUserName").val() == "") {
        valid = false;
    }
    if (jQuery("#jmxUserPass").val() == "") {
        valid = false;
    }
    if (!valid) {
        CARBON.showErrorDialog("Please input the JMX server details");
        return;
    }


    jQuery.ajax({
                    url:'jmxAttributes_ajaxprocessor.jsp',
                    data:'requestType=mBeans&jmxServerUrl=' + jQuery("#jmxServerUrl").val() + '&jmxUserName=' + jQuery("#jmxUserName").val() + '&jmxUserPass=' + jQuery("#jmxUserPass").val(),
                    success:function (data) {

                        //check for exceptions
                        if (data.indexOf("Unauthorized access attempt to JMX operation.") != -1) {
                            CARBON.showErrorDialog("Invalid JMX server credentials");
                            return 0;
                        }
                        else if (data.indexOf("Service URL must start with service:jmx:") != -1) {
                            CARBON.showErrorDialog("Invalid JMX server URL");
                            return 0;
                        }

                        //to remove the unnecessary whitespaces
                        data = data.replace(/(^\s+|\s+$)/g, '');
                        var dataArr = data.split("____");

                        var htmlText = "<select id=\"mBeansList\" name=\"mBeansList\" size=\"20\" onclick=\"showJmxMbeanAttributes()\" style=\"width: 100%\"> ";


                        for (var i = 0; i < dataArr.length; i++) {
                            if (dataArr[i] != "") {
                                //get rid of the \n character at the startup of a list
                                if (i == 0) {
                                    while (dataArr[0] == "\n") {
                                        dataArr[i] = dataArr[i].sub(1, dataArr[i].length)
                                    }
                                }
                                htmlText += "<option value=\"" + dataArr[i] + "\" >" + dataArr[i] + "</option>";
                            }
                        }

                        htmlText += "</select>";

                        jQuery('#tester').html(htmlText);

                    }
                });

    //change the label of the button
    jQuery("#jmxMBeansCheckButton").attr('value', 'Reload');
}

function showJmxMbeanAttributes() {


    jQuery.ajax({
                    url:'jmxAttributes_ajaxprocessor.jsp',
                    data:'requestType=atributes&mBean=' + jQuery("#mBeansList option:selected").text() + '&jmxServerUrl=' + jQuery("#jmxServerUrl").val() + '&jmxUserName=' + jQuery("#jmxUserName").val() + '&jmxUserPass=' + jQuery("#jmxUserPass").val(),
                    success:function (data) {

                        //check for exceptions
                        if (data.indexOf("Unauthorized access attempt to JMX operation.") != -1) {
                            CARBON.showErrorDialog("Invalid JMX server credentials");
                            return 0;
                        }
                        else if (data.indexOf("Service URL must start with service:jmx:") != -1) {
                            CARBON.showErrorDialog("Invalid JMX server URL");
                            return 0;
                        }

                        //to remove the unnecessary whitespaces
                        data = data.replace(/(^\s*)|(\s*$)/gi, "");
                        data = data.replace(/[ ]{2,}/gi, " ");
                        data = data.replace(/\n /, "\n");

                        var dataArr = data.split("____");

                        var htmlText = "";

                        for (var i = 0; i < dataArr.length; i++) {
                            if (dataArr[i] != "") {
                                //check if this is a composite data type
                                var compositeData = dataArr[i].split("+;+;");
                                if (compositeData.length > 1) {
                                    for (var k = 0; k < compositeData.length - 1; k++) {
                                        if (k != 0) {
                                            htmlText += "<OPTION value='" + compositeData[0] + "______" + compositeData[k] + "'> " + compositeData[0] + " - " + compositeData[k];
                                        }
                                    }
                                }else {
                                    htmlText += "<OPTION value='" + compositeData[0] + "'> " + dataArr[i];
                                }
                            }
                        }

                        jQuery('#mBeanAttrs').html(htmlText);
                        jQuery('#mBeanAttrs').show();
                        jQuery('#attrSelectionLabel').show();
                    }
                });
}


function changeCronType() {
    if (jQuery("#presetCrons option:selected").val() == "custom") {
        jQuery('#cronExprTxtInput').show();
        //jQuery('#presetCrons').hide();
    } else {
        jQuery('#cronExprTxtInput').hide();
        //jQuery('#presetCrons').show();
    }
}

//checks whether the selected profile name exists
//and whether the profile name is formatted correctly
function checkProfileName() {

    jQuery.ajax({
                    url:'jmxAttributes_ajaxprocessor.jsp',
                    data:'requestType=profiles',
                    success:function (data) {

                        //to remove the unnecessary whitespaces
                        data = data.replace(/(^\s+|\s+$)/g, '');
                        var dataArr = data.split("____");

                        var profName = jQuery('#profileName').val();

                        //checks whether the selected profile name exists
                        var existingName = false;
                        for (var i = 0; i < dataArr.length - 1; i++) {
                            if (profName == dataArr[i]) {
                                existingName = true;
                            }
                        }

                        if (existingName) {
                            jQuery('#profileNameExistsErrorMsg').show();
                        }
                        else {
                            jQuery('#profileNameExistsErrorMsg').hide();
                        }

                        //checks whether the profile name is formatted correctly
                        if (!(/^[a-zA-Z0-9]*$/.test(profName))) {
                            jQuery('#profileNameFormattingErrorMsg').show();
                        }
                        else {
                            jQuery('#profileNameFormattingErrorMsg').hide();
                        }

                        // check profile name length
                        if(profName.length > 30) {
                            jQuery('#profileNameLengthErrorMsg').show();
                        } else {
                            jQuery('#profileNameLengthErrorMsg').hide();
                        }
                    }
                });

    return 0;
}

//tests the availability of the DataPublisher for the backend
function testDataReceiverReceiverURLAvailability() {
    var port = jQuery("#pubAddress").val().split(":")[1];
    if (!(port > 0) || !(port < 65535)) {
        CARBON.showErrorDialog("Please enter a valid port range");
        return 0;
    }

    jQuery.ajax({
                    url:'jmxAttributes_ajaxprocessor.jsp',
                    data:'requestType=dpAvailability&dpUrl=' + jQuery("#pubAddress").val() + "&pubConnType=" + jQuery("#pubConnType option:selected").val(),
                    async:true,
                    success:function (data) {

                        //if the DataPublisher is available
                        if (data.indexOf("true") != -1) {
                            CARBON.showInfoDialog("The Data Receiver receiver url is available!");
                        }
                        else {
                            CARBON.showErrorDialog("The Data Receiver receiver url is not available");
                        }

                    }
                });

}
//tests the availability of the DataPublisher secure url for the backend
function testDataReceiverSecureURLAvailability() {
    var port = jQuery("#pubSecureAddress").val().split(":")[1];
    if (!(port > 0) || !(port < 65535)) {
        CARBON.showErrorDialog("Please enter a valid port range");
        return 0;
    }

    jQuery.ajax({
                    url:'jmxAttributes_ajaxprocessor.jsp',
                    data:'requestType=dpAvailability&dpUrl=' + jQuery("#pubSecureAddress").val() + "&pubConnType=" + jQuery("#pubSecureConnType option:selected").val(),
                    async:true,
                    success:function (data) {

                        //if the DataPublisher is available
                        if (data.indexOf("true") != -1) {
                            CARBON.showInfoDialog("The Data Receiver secure url is available!");
                        }
                        else {
                            CARBON.showErrorDialog("The Data Receiver secure url is not available");
                        }

                    }
                });

}
function selectAttribute() {
    var selectedMBean = jQuery("#mBeansList option:selected").text();
    var selectedAttributeEntry = jQuery("#mBeanAttrs option:selected").val();

    //check whether this is a composite attribute
    var isComposite;
    if (selectedAttributeEntry.split("______").length > 1) {
        isComposite = true;
        var compositeData = selectedAttributeEntry.split("______");
    }
    else {
        isComposite = false;
    }

    //if this is a composite entry
    //
    var selectedAttrValue = jQuery("#mBeanAttrs option:selected").val();
    var htmlText;
    if (isComposite) {

        htmlText = "<tr>" +
                       "<td style='word-wrap: break-word;'>" + selectedMBean + " </td>" +
                       "<td title=\"" + selectedAttrValue + "\">" + jQuery("#mBeanAttrs option:selected").text() + " </td>" +
                       "<td><input style=\"width:98%\" type=\"text\" name=\"alias\" value=\"" + selectedMBean + "_" + compositeData[0] + "_" + compositeData[1] + "\" /> " +
                       "<td> <a href=\"#\" onclick='$(this).parent().parent().remove(); return false;' class=\"icon-a\"><i class=\"icon-delete\"></i>Remove</a></td>" +
                       "</td>" +
                       "</tr>";
        jQuery('#selectedJMXAttrs tbody tr:last').after(htmlText);
    }
    else {
         htmlText = "<tr>" +
                       "<td style='word-wrap: break-word;width: 60%' >" + selectedMBean + " </td>" +
                       "<td title=\"" + selectedAttrValue + "\">" + jQuery("#mBeanAttrs option:selected").text() + " </td>" +
                       "<td><input style=\"width:98%\" type=\"text\" name=\"alias\" value=\"" + selectedMBean + "_" + jQuery("#mBeanAttrs option:selected").val() + "\" /> " +
                       "<td> <a href=\"#\" onclick='$(this).parent().parent().remove(); return false;' class=\"icon-a\"><i class=\"icon-delete\"></i>Remove</a></td>" +
                       "</td>" +
                       "</tr>";
        jQuery('#selectedJMXAttrs tbody tr:last').after(htmlText);
    }

    //if the attribute table is not yet visible
    //make it visible
    jQuery('#attributesTable').show();


}

</script>

<script type="text/javascript">
    function saveProfile() {


        var valid = true;
        if (document.getElementById('profileName').value == "") {
            valid = false;
        }


        //checks whether the profile name is formatted correctly
        var profName = jQuery('#profileName').val();
        if (!(/^[a-zA-Z0-9]*$/.test(profName))) {
            CARBON.showErrorDialog("Please select a proper profile name. \n A profile name can only contain letters and/or numbers");
            return;
        }


        //check if the profile name already exists
        var profNameExists = false;
        jQuery.ajax({
                        url:'jmxAttributes_ajaxprocessor.jsp',
                        data:'requestType=profiles',
                        async:false,
                        success:function (data) {

                            //to remove the unnecessary whitespaces
                            data = data.replace(/(^\s+|\s+$)/g, '');
                            var dataArr = data.split("____");

                            profName = jQuery('#profileName').val();

                            //checks whether the selected profile name exists
                            var existingName = false;
                            for (i = 0; i < dataArr.length - 1; i++) {
                                if (profName == dataArr[i]) {
                                    existingName = true;
                                }
                            }

                            if (existingName) {
                                CARBON.showErrorDialog("Profile name already in use");
                                valid = false;
                                profNameExists = true;
                                return 0;
                            }

                        }
                    });
        if (profNameExists) {
            return 0;
        }

        if (document.getElementById('pubAddress').value == "") {
            valid = false;
        }
        if (document.getElementById('pubUserName').value == "") {
            valid = false;
        }
        if (document.getElementById('pubUserPass').value == "") {
            valid = false;
        }
        if (document.getElementById('pubSecureAddress').value == "") {
            valid = false;
        }

        //if a custom cron is selected
        if (jQuery('#cronType').val() == "custom" && document.getElementById('#cronExprTxtInput').value == "") {
            valid = false;
        }

        if (document.getElementById('jmxServerUrl').value == "") {
            valid = false;
        }
        if (document.getElementById('jmxUserName').value == "") {
            valid = false;
        }
        if (document.getElementById('jmxUserPass').value == "") {
            valid = false;
        }


        //get the table data
        var tableRows = $("#selectedJMXAttrs tbody tr");
        var array = [];
        tableRows.each(function () {
            var td = $(this).find('td');
            var childArray = [];

            td.each(function () {
                //do not add the <td> with the remove attribute link
                if ($(this).has("a").length) {
                    return 0;
                }
                if ($(this).find("input").length) {
                    childArray.push($(this).find("input").val());
                }
                //if this is the attribute name <td>
                else if ($(this).attr('title') != '') {
                    var data = $(this).attr('title');
                    //check whether it's compositeData
                    var splitData = data.split("______");
                    if (splitData.length > 1) {
                        //if this is composite data
                        //add the attribute name first
                        childArray.push(splitData[0]);
                        //then the key
                        childArray.push(splitData[1]);
                    }
                    else {
                        childArray.push(data);
                    }
                }

                else {
                    //slice is there to remove the last space of the MBean
                    childArray.push($(this).text().slice(0, -1));
                }
            });
            array.push(childArray);
        });

        //check for empty attributes selection. < 2 is used because always an
        //empty entry is created because of the default <tr> tag
        if (array.length < 2) {
            valid = false;
        }

        if (!valid) {
            CARBON.showErrorDialog("Please fill all the mandatory fields");
            return;
        }

        //create the parameters string for selected attributes/mbeans/keys
        var parameterString = "";

        //iterate through the rows
        //skip the first row since it's obsolete
        for (var i = 1; i < array.length; i++) {
            //iterate through the data

            //the data will be in the following format

            //if this is a composite attribute
            //[MBean,Attribute,Key,Alias]

            //if this is not a composite attribute
            //[MBean,Attribute,Alias]

            for (var k = 0; k < array[i].length; k++) {

                parameterString += array[i][k];
                //to get rid of the final ";"
                if (k != array[i].length - 1) {
                    parameterString += ";";
                }
            }

            //to get rid of the final " - "
            if (i != array.length - 1) {
                parameterString += " - ";
            }
        }

        jQuery('#mBeanAttrData').val(parameterString);

        document.getElementById('editProfileForm').submit();

    }

    //contains the code for the initialization of the page
    function setUpPage() {
        jQuery('#pubConnType').val("tcp://");
        jQuery('#pubSecureConnType').val("ssl://");

    }

</script>


<div id="middle">
<h2>New Profile</h2>

<body onload="setUpPage()"></body>

<div id="workArea">
<!--  Page content goes here -->


<form action="submitProfile.jsp" method="post" id="editProfileForm">

<div class="sectionHelp">All the fields marked with * are mandatory</div>
<div class="sectionSeperator togglebleTitle">Basic Information</div>
<div class="sectionSub">
<table class="carbonFormTable sectionSub">
    <tr>
        <td colspan="2">
            <div class="sectionHelp">
                Enter basic JMX profile information.
            </div>
        </td>
    </tr>
    <tr>
        <td class="leftCol-small labelField">Name:<span class="required">*</span></td>
        <td>
            <input name="profileName" type="text" id="profileName" onchange="checkProfileName()"
                   class="required" autofocus/>

            <div id="profileNameExistsErrorMsg" style="display:none;color:red"> The profile name
                                                                                exists.
            </div>
            <div id="profileNameFormattingErrorMsg" style="display:none;color:red"> The profile name
                                                                                    can only contain
                                                                                    letters and/or
                                                                                    numbers.
            </div>
            <div id="profileNameLengthErrorMsg" style="display:none;color:red"> The profile name need to be less than 30 characters.
            </div>
        </td>


    </tr>
</table>

<div class="sectionSeperator togglebleTitle">Data Receiver information</div>

<table class="carbonFormTable sectionSub">

    <tr>
        <td class="labelField leftCol-small">Receiver address:<span class="required">*</span></td>
        <td>
            <select name="pubConnType" id="pubConnType">
                <option value="tcp://">tcp://</option>
                <option value="https://">https://</option>
                <option value="http://">http://</option>
                <option value="ssl://">ssl://</option>
            </select>

            <input name="pubAddress" type="text" id="pubAddress" value="127.0.0.1:7611"/>
            <button type="button" onclick="testDataReceiverReceiverURLAvailability()">Check
                                                                                      Connection
            </button>

            <div class="sectionHelp">
                Enter the receiver address in the format "IP_Address:Port".
            </div>
        </td>
    </tr>
    <tr>
        <td class="labelField leftCol-small">Secure address:<span class="required">*</span></td>
        <td>
            <select name="pubSecureConnType" id="pubSecureConnType">
                <option value="https://">https://</option>
                <option value="ssl://">ssl://</option>
            </select>

            <input name="pubSecureAddress" type="text" id="pubSecureAddress" value="127.0.0.1:7711" />
            <button type="button" onclick="testDataReceiverSecureURLAvailability()">Check
                                                                                    Connection
            </button>

            <div class="sectionHelp">
                Enter the secure address in the format "IP_Address:Port".
            </div>
        </td>
    </tr>

    <tr>
        <td class="labelField">User Name:<span class="required">*</span></td>
        <td><input name="pubUserName" type="text" id="pubUserName" class="required"/>

            <div class="sectionHelp">
                Enter the data receiver user name.
            </div>
        </td>
    </tr>

    <tr>
        <td class="labelField">Password:<span class="required">*</span></td>
        <td><input name="pubUserPass" type="password" id="pubUserPass" class="required"/>

            <div class="sectionHelp">
                Enter the data receiver password.
            </div>
        </td>
    </tr>

    <tr>
        <td class="labelField">Schedule:<span class="required">*</span></td>
        <td>

            <div id="presetCrons">
                <select name="presetCronExpr" id="presetCronExpr" onclick="changeCronType()">
                    <option value="0/2 * * ? * *">once every 2 seconds (0/2 * * ? * *)</option>
                    <option value="0/5 * * ? * *">once every 5 seconds (0/5 * * ? * *)</option>
                    <option value="0/15 * * ? * *">once every 15 seconds (0/15 * * ? * *)</option>
                    <option value="custom">Custom</option>
                </select>
            </div>


            <input name="cronExprTxtInput" style="display:none" type="text" id="cronExprTxtInput"
                   class="required"/>
        </td>
    </tr>


</table>
<%--</div><!-- sectionSub Div -->--%>

<div class="sectionSeperator togglebleTitle">JMX server information</div>

<table class="carbonFormTable sectionSub">

    <tr>
        <td class="labelField leftCol-small">Server URL:<span class="required">*</span></td>
        <td><input name="jmxServerUrl" type="text" id="jmxServerUrl"/>

            <div class="sectionHelp">
                Enter the monitoring URL for the JMX server.
            </div>
        </td>
    </tr>

    <tr>
        <td class="labelField">User Name:<span class="required">*</span></td>
        <td><input name="jmxUserName" type="text" id="jmxUserName"/>

            <div class="sectionHelp">
                Enter the user name for the JMX server.
            </div>
        </td>
    </tr>

    <tr>
        <td class="labelField">Password:<span class="required">*</span></td>
        <td><input name="jmxUserPass" type="password" id="jmxUserPass"/>

            <div class="sectionHelp">
                Enter the password for the JMX server.
            </div>
        </td>
    </tr>


</table>
<table class="carbonFormTable">
    <tr>
        <td><input id="jmxMBeansCheckButton" type="button" value="Load MBeans"
                   onclick="showJmxMbeans()"/>
        </td>


    </tr>
    <tr>
        <td>
            <div id="tester"></div>
        </td>
    </tr>
</table>
<table class="carbonFormTable" style="margin-top:10px;">

    <tr>
        <td class="labelField">
            <div id="attrSelectionLabel" style="display:none;">Select attributes:</div>
        </td>

    </tr>
    <tr>
        <td>
            <select id="mBeanAttrs" onchange="selectAttribute()" onfocus="this.selectedIndex = -1;"
                    name="options" style="width: 100%;display:none">


            </select>
        </td>
    </tr>

</table>

<%--the table to show the currently selected attributes--%>
<div id="attributesTable" style="width: 100%;display:none">
    <h4>Selected Attributes:</h4>
    <table class="styledLeft" id="selectedJMXAttrs"
           style="word-wrap:break-word;table-layout:fixed;">
        <thead>
        <tr>
            <th>MBean</th>
            <th style="width: 15%">Attribute</th>
            <th>Alias</th>
            <th style="width: 90px;">Actions</th>
        </tr>
        </thead>
        <tbody>
        <tr></tr>
        </tbody>
    </table>
</div>


<div class="buttonRow" style="margin-left:0">
    <input type="button" value="Save" onclick="saveProfile()"/>
    <input type="button" value="Cancel" onClick="location.href='index.jsp'"/>
    <input type="HIDDEN" name="newProfile" value="true">
    <input id="mBeanAttrData" type="HIDDEN" name="mBeanAttrData" value="">
</div>
</div>

</form>
</div>
</div>
