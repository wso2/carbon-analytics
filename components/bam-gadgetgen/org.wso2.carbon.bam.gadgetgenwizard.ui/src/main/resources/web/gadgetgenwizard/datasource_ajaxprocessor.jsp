<%
    String jdbcurl = (session.getAttribute("jdbcurl") != null) ? ((String[]) session.getAttribute("jdbcurl")) [0] : "";
    String driver = (session.getAttribute("driver") != null) ? ((String[]) session.getAttribute("driver")) [0] : "";
    String username = (session.getAttribute("username") != null) ? ((String[]) session.getAttribute("username")) [0] : "";
    String password = (session.getAttribute("password") != null) ? ((String[]) session.getAttribute("password")) [0] : "";
%>

<fmt:bundle basename="org.wso2.carbon.bam.gadgetgenwizard.ui.i18n.Resources">
    <carbon:breadcrumb label="main.gadgetgenwizard"
                       resourceBundle="org.wso2.carbon.bam.gadgetgenwizard.ui.i18n.Resources"
                       topPage="false" request="<%=request%>"/>
    <carbon:jsi18n
            resourceBundle="org.wso2.carbon.bam.gadgetgenwizard.ui.i18n.JSResources"
            request="<%=request%>" />


    <script type="text/javascript">
        $("#validate").click(function() {
            if (!validate()) {
                CARBON.showErrorDialog(jsi18n["invalid.details"]);
                return;
            }
            $.post("validate_db_conn_ajaxprocessor.jsp", $("form").serialize(), function(html) {
                var success = !(html.toLowerCase().match(/error/));
                if (success) {
                    CARBON.showInfoDialog(jsi18n["conn.valid"]);
                } else {
                    CARBON.showErrorDialog(jsi18n["conn.invalid"] + html);
                }
            });

        });

        $("#jdbclbl").html(jsi18n["jdbc.label"] + "<span style=\"color: red; \">*</span>");
        $("#driverlbl").html(jsi18n["driver.label"] + "<span style=\"color: red; \">*</span>");
        $("#usernamelbl").html(jsi18n["username.label"] + "<span style=\"color: red; \">*</span>");
        $("#passwordlbl").html(jsi18n["password.label"]);


    </script>

    <table class="normal">
        <tbody>
        <tr>
            <td id="jdbclbl">
            </td>
            <td><input class="validate" type="text" name="jdbcurl" value="<%=jdbcurl%>" style="width:400px"/></td>
        </tr>
        <tr>
            <td id="driverlbl"></td>
            <td><input class="validate" type="text" name="driver" value="<%=driver%>" style="width:400px"/></td>
        </tr>
        <tr>
            <td id="usernamelbl"></td>
            <td><input class="validate" type="text" name="username" value="<%=username%>" style="width:400px"/></td>
        </tr>
        <tr>
            <td id="passwordlbl"></td>
            <td><input type="password" name="password" value="<%=password%>" style="width:400px"></td>
        </tr>
        <tr>
            <td><input type="button" class="button" value="Validate Connection" id="validate"/></td>
        </tr>
        <input type="hidden" name="page" id="page" value="1">
        </tbody>
    </table>

</fmt:bundle>