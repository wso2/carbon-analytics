<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib uri="http://wso2.org/projects/carbon/taglibs/carbontags.jar" prefix="carbon" %>
<%@ page import="org.apache.axis2.context.ConfigurationContext" %>
<%@ page import="org.wso2.carbon.CarbonConstants" %>
<%@ page import="org.wso2.carbon.bam.jmx.agent.stub.profiles.xsd.Profile" %>
<%@ page import="org.wso2.carbon.bam.jmx.agent.ui.JmxConnector" %>
<%@ page import="org.wso2.carbon.ui.CarbonUIUtil" %>
<%@ page import="org.wso2.carbon.utils.ServerConstants" %>
<%@ page import="org.wso2.carbon.bam.jmx.agent.stub.JmxAgentProfileDoesNotExistExceptionException" %>
<%@ page import="org.wso2.carbon.bam.jmx.agent.stub.JmxAgentJmxProfileExceptionException" %>

<fmt:bundle basename="org.wso2.carbon.bam.jmx.agent.ui.i18n.Resources">

    <carbon:breadcrumb
            label="bam.jmx-agent"
            resourceBundle="org.wso2.carbon.bam.jmx.agent.ui.i18n.Resources"
            topPage="true"
            request="<%=request%>"/>
    <% String serverURL = CarbonUIUtil.getServerURL(config.getServletContext(), session);

        ConfigurationContext configContext =
                (ConfigurationContext) config.getServletContext().
                        getAttribute(CarbonConstants.CONFIGURATION_CONTEXT);
        String cookie = (String) session.getAttribute(ServerConstants.ADMIN_SERVICE_COOKIE);

        JmxConnector connector = new JmxConnector(configContext, serverURL, cookie);


        //if a profile needs to be deleted
        if ("true".equalsIgnoreCase(request.getParameter("deleteProfile"))) {
            try {
                connector.deleteProfile(request.getParameter("profileName"));
            } catch (JmxAgentProfileDoesNotExistExceptionException e) {
                return;
            } catch (JmxAgentJmxProfileExceptionException e) {
                return;
            }
        }

        //if a profile needs to be enabled
        if ("enableProfile".equalsIgnoreCase(request.getParameter("jmxRequestType"))) {

            try {
                connector.enableProfile(request.getParameter("profileName"));
            } catch (JmxAgentProfileDoesNotExistExceptionException e) {
                return;
            }
        }

        //if a profile needs to be disabled
        if ("disableProfile".equalsIgnoreCase(request.getParameter("jmxRequestType"))) {

            try {
                connector.disableProfile(request.getParameter("profileName"));
            } catch (JmxAgentProfileDoesNotExistExceptionException e) {
                return;
            }
        }

        //if the toolbox profile needs t be added
        if("true".equalsIgnoreCase(request.getParameter("addToolbox"))){
            connector.addToolboxProfile();
        }
    %>

    <script type="text/javascript">


        function deleteProfile(profileName) {

            function callDelete() {
                location.href = "index.jsp?deleteProfile=true&profileName=" + profileName
            }

            CARBON.showConfirmationDialog("Are you sure you want to delete " + profileName + "", callDelete, null);
        }

    </script>


    <div id="middle">
        <h2>JMX Monitoring Profiles</h2>

        <div id="workArea">


            <h4><a class="icon-a" href="newProfile.jsp"><i class="icon-add"></i><fmt:message key="add.profile"/></a></h4>
            <h4><a class="icon-a" href="index.jsp?addToolbox=true"><i class="icon-add"></i><fmt:message key="add.default.profile"/></a></h4>
            <table class="styledLeft">
                <thead>
                <tr>
                    <th class="leftCol-med">Profile</th>
                    <th>Version</th>
                    <th>Actions</th>
                </tr>
                </thead>
                <tbody>

                <%
                    //fetch the profiles
                    Profile[] profiles = connector.getAllProfiles();
                    //when all the profiles are deleted
                    if (profiles != null) {
                        //initially no profiles exist
                        if (profiles[0] != null) {
                            for (Profile prf : profiles) {
                                out.println("<tr>");
                                out.println("<td><a href=\"editProfile.jsp?profileName=" + prf.getName() + "\">" + prf.getName() + "</a></td>");
                                out.println("<td>" + prf.getVersion() + ".0.0</td>");
                                out.println("<td>\n");
                                //if the profile is active
                                if (prf.getActive()) {
                                    out.println("                        <a href=\"index.jsp?jmxRequestType=disableProfile&profileName=" + prf.getName() + "\" class=\"icon-a\"><img border=\"0\" alt=\"\" src=\"../admin/images/static-icon.gif\"> Disable</a>\n");
                                } else {
                                    out.println("                        <a href=\"index.jsp?jmxRequestType=enableProfile&profileName=" + prf.getName() + "\" class=\"icon-a\"><img border=\"0\" alt=\"\" src=\"../admin/images/static-icon-disabled.gif\"> Enable</a>\n");
                                }

                                out.println(" <a href=\'editProfile.jsp?profileName=" + prf.getName() + "\'  class=\"icon-a\"><i class=\"icon-edit\"></i>Edit</a>\n");
                                out.println(" <a href=\"#\" onclick='deleteProfile(\"" + prf.getName() + "\");' class=\"icon-a\"><i class=\"icon-delete\"></i>Delete</a>\n");

                                out.print("                    </td>");
                                out.println("</tr>");
                            }
                        }
                    }


                %>

                </tbody>
            </table>
        </div>
    </div>
</fmt:bundle>