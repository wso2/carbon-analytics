<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.jmx.agent.ui.JmxConnector" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.bam.jmx.agent.stub.profiles.xsd.Profile" %>
<%@ page import="org.wso2.carbon.bam.jmx.agent.stub.profiles.xsd.MBean" %>
<%@ page import="org.wso2.carbon.bam.jmx.agent.stub.profiles.xsd.MBeanAttribute" %>
<%@ page import="org.wso2.carbon.bam.jmx.agent.stub.profiles.xsd.MBeanAttributeProperty" %>
<%@ page import="org.wso2.carbon.bam.jmx.agent.stub.JmxAgentProfileDoesNotExistExceptionException" %>
<%@ page import="org.wso2.carbon.bam.jmx.agent.stub.JmxAgentJmxProfileExceptionException" %>

<% String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);

    ConfigurationContext configContext =
            (ConfigurationContext) config.getServletContext().
                    getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
    String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

    JmxConnector connector = new JmxConnector(configContext, serverURL, cookie);

    //get the profile name
    String profileName = request.getParameter("profileName");
    //get the profile
    Profile profile;
    try {
        profile = connector.getProfile(profileName);
    } catch (JmxAgentProfileDoesNotExistExceptionException e) {
        out.print(e.getMessage());
        return;
    }catch (JmxAgentJmxProfileExceptionException e) {
        out.print(e.getMessage());
        return;
    }

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
    jQuery('#newAttributesSection').show();
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
                                }
                                else {
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
                   "<td style='word-wrap: break-word;'>" + selectedMBean + "</td>" +
                   "<td title=\"" + selectedAttrValue + "\">" + jQuery("#mBeanAttrs option:selected").text() + " </td>" +
                   "<td><input style=\"width:98%\" type=\"text\" name=\"alias\" value=\"" + selectedMBean + "_" + compositeData[0] + "_" + compositeData[1] + "\" /> " +
                   "<td> <a href=\"#\" onclick='$(this).parent().parent().remove(); return false;' class=\"icon-a\"><i class=\"icon-delete\"></i>Remove</a></td>" +
                   "</td>" +
                   "</tr>";
        jQuery('#selectedJMXAttrs tbody tr:last').after(htmlText);
    }
    else {
        htmlText = "<tr>" +
                   "<td style='word-wrap: break-word;width: 60%' >" + selectedMBean + "</td>" +
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


function saveProfile() {

    //does the necessary processing of the data and sends the profile to be saved
    function processData(){
        var valid = true;

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
                    } else {
                        childArray.push(data);
                    }

                } else {
                    //slice is there to remove the last space of the MBean
                    childArray.push($(this).text());
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

            /**
             *
             * the data will be in the following format
             *
             * if this is a composite attribute
             * [MBean,Attribute,Key,Alias]
             *
             * if this is not a composite attribute
             * [MBean,Attribute,Alias]
             *
             */

            for (var k = 0; k < array[i].length; k++) {

                parameterString += array[i][k];
                //to get rid of the final ";"
                if (k != array[i].length - 1) {

                    parameterString += ";";
                }
            }

            //to get rid of the final "__-__"
            if (i != array.length - 1) {
                parameterString += "__-__";

            }
        }



        jQuery('#mBeanAttrData').val(parameterString);

        document.getElementById('editProfileForm').submit();

    }

    //ask the user whether he needs to change the version of the profile
    function enableVersionChange(){
        jQuery("#incrementVersion").attr('value','true');
        processData();
    }
    CARBON.showConfirmationDialog("Do you want to increment the version?" , enableVersionChange, processData);


}

function changeCronType() {
    if (jQuery("#presetCrons option:selected").val() == "custom") {
        jQuery('#cronExprTxtInput').show();
    } else {
        jQuery('#cronExprTxtInput').hide();
    }
}

//contains the code for the initialization of the page
function setUpPage(dPReceiverConnectionTyp, dPSecureUrlConnectionType) {
    jQuery('#pubConnType').val(dPReceiverConnectionTyp);
    jQuery('#pubSecureConnType').val(dPSecureUrlConnectionType);

}

</script>


<div id="middle">
<h2>Edit Profile - <%=profile.getName()%> (Current version:<%=profile.getVersion()%>.0.0)
</h2>
<%--set the value of the connection type on startup --%>
<body onload="setUpPage('<%out.print(profile.getDpReceiverConnectionType());%>','<%out.print(profile.getDpSecureUrlConnectionType());%>')"></body>
<div id="workArea">
<!--  Page content goes here -->
<form action="submitProfile.jsp" method="post" id="editProfileForm">

<div class="sectionHelp">All the fields marked with * are mandatory</div>
<div class=”sectionSub”>
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

            <input name="pubAddress" type="text" id="pubAddress"
                   value="<%=profile.getDpReceiverAddress()%>"/>
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

            <input name="pubSecureAddress" type="text" id="pubSecureAddress"
                   value="<%=profile.getDpSecureAddress()%>"/>
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
        <td><input name="pubUserName" type="text" id="pubUserName"
                   value="<%=profile.getDpUserName()%>" class="required"/>

            <div class="sectionHelp">
                Enter the data receiver user name.
            </div>
        </td>
    </tr>

    <tr>
        <td class="labelField">Password:<span class="required">*</span></td>
        <td><input name="pubUserPass" type="password" id="pubUserPass"
                   value="<%=profile.getDpPassword()%>" class="required"/>

            <div class="sectionHelp">
                Enter the data receiver password.
            </div>
        </td>
    </tr>

    <tr>
        <td class="labelField">Schedule:<span class="required">*</span></td>
        <td>


            <%--</div>--%>
            <div id="presetCrons">
                <select name="presetCronExpr" id="presetCronExpr" onclick="changeCronType()">
                    <option value="0/2 * * ? * *">once every 2 seconds (0/2 * * ? * *)</option>
                    <option value="0/5 * * ? * *">once every 5 seconds (0/5 * * ? * *)</option>
                    <option value="0/15 * * ? * *">once every 15 seconds (0/15 * * ? * *)</option>
                    <option value="custom">Custom</option>
                </select>

                <%--Display the current value--%>
                Current selection is <%out.print(profile.getCronExpression());%>
            </div>

            <input name="cronExprTxtInput" style="display:none" type="text" id="cronExprTxtInput"
                   class="required"/>

            <div class="sectionHelp" style="margin-top: 10px">
                Cron expression for task scheduling.
            </div>


        </td>
    </tr>


</table>
<%--</div>--%>
<!-- sectionSub Div -->
<div class="sectionSeperator togglebleTitle">JMX server information</div>

<table class="carbonFormTable sectionSub">

    <tr>
        <td class="labelField leftCol-small">Server URL:<span class="required">*</span></td>
        <td><input name="jmxServerUrl" type="text" value="<%=profile.getUrl()%>"
                   id="jmxServerUrl"/>

            <div class="sectionHelp">
                Enter the monitoring URL for the JMX server.
                <%--</div>--%>
        </td>
    </tr>

    <tr>
        <td class="labelField">User Name:<span class="required">*</span></td>
        <td><input name="jmxUserName" type="text" value="<%=profile.getUserName()%>"
                   id="jmxUserName"/>

            <div class="sectionHelp">
                Enter the user name for the JMX server.
                <%--</div>--%>
        </td>
    </tr>

    <tr>
        <td class="labelField">Password:<span class="required">*</span></td>
        <td><input name="jmxUserPass" type="password" value="<%=profile.getPass()%>"
                   id="jmxUserPass"/>

            <div class="sectionHelp">
                Enter the password for the JMX server.
                <%--</div>--%>
        </td>
    </tr>


</table>


<%--the table to show the currently selected attributes--%>
<div id="attributesTable" style="width: 100%;">
    <table class="carbonFormTable sectionSub">

        <tr>
            <td class="labelField leftCol-small"><h4>Attributes:</h4><span class="required"></span>
            </td>
            <td><input id="jmxMBeansCheckButton" type="button" value="Add More"
                       onclick="showJmxMbeans()"/></td>
        </tr>

    </table>

    <table class="carbonFormTable sectionSub" style="width: 100%;display:none"
           id="newAttributesSection">
        <tr>
            <td>
                <div id="tester"></div>
            </td>
        </tr>
        <tr>
            <td>
                <select id="mBeanAttrs" onchange="selectAttribute()"
                        onfocus="this.selectedIndex = -1;" name="options"
                        style="width: 100%;display:none">

                </select>
            </td>
        </tr>

    </table>


    <table class="styledLeft" id="selectedJMXAttrs" style="word-wrap:break-word;table-layout:fixed;">
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
                <%
                    //add the data
                    //traverse through the mBeans
                    for (MBean mBean : profile.getSelectedMBeans()) {
                        for(MBeanAttribute mBeanAttribute : mBean.getAttributes()){

                                    /* If compositeData:
                                      AttrName - keyname - Alias
                                    else:
                                      AttrName - Alias */

                            /* If MBean is a simple type */
                            if(mBeanAttribute.getAliasName() != null) {
                %>
                            <tr>
                            <%--Add the MBean name--%>
                                <td>
                                    <%out.print(mBean.getMBeanName());%>
                                </td>
                                <td title='<%out.print(mBeanAttribute.getAttributeName());%>'>
                                    <%out.print(mBeanAttribute.getAttributeName());%>
                                </td>
                                <td>
                                    <input style="width:98%" type="text" name="alias"
                                        value="<%out.print(mBeanAttribute.getAliasName());%>">
                                </td>
                                <%--Add the remove button--%>
                                <td>
                                    <a href="#" onclick='$(this).parent().parent().remove(); return false;'
                                        class="icon-a"><i class="icon-delete"></i>Remove</a>
                                </td>
                            </tr>
                                <%
                            }
                            /* If MBean is a composite type*/
                            else {
                                 %>
                                        <%
                                            for (MBeanAttributeProperty property : mBeanAttribute.getProperties()) {
                                        %>
                                    <tr>
                                    <%--Add the MBean name--%>
                                                <td>
                                                    <%out.print(mBean.getMBeanName());%>
                                                </td>
                                                <td title='<%out.print(mBeanAttribute.getAttributeName());%>______<%out.print(property.getPropertyName());%>'>
                                                    <%out.print(mBeanAttribute.getAttributeName());%> - <%out.print(property.getPropertyName());%>
                                                </td>
                                                <td>
                                                    <input style="width:98%" type="text" name="alias"
                                                            value="<%out.print(property.getAliasName());%>">
                                                </td>
                                                <%--Add the remove button--%>
                                                <td>
                                                    <a href="#" onclick='$(this).parent().parent().remove(); return false;'
                                                    class="icon-a"><i class="icon-delete"></i>Remove</a>
                                                </td>
                                    </tr>
                                        <% } %>

                <%
                            }
                          }
                      }
                %>
        </tbody>
    </table>
</div>


</div>
<!-- sectionSub Div -->


<div class="buttonRow" style="margin-left:2px;margin-right:-1px">
    <input type="button" value="Save" onclick="saveProfile()"/>
    <input type="button" value="Cancel" onClick="location.href='index.jsp'"/>
    <input type="HIDDEN" name="newProfile" value="false">
    <input type="HIDDEN" id="incrementVersion" name="incrementVersion" value="false">
    <input type="HIDDEN" name="profileName" value="<%=profile.getName()%>">
    <input id="mBeanAttrData" type="HIDDEN" name="mBeanAttrData" value="">
</div>
</form>
</div>

</div>

