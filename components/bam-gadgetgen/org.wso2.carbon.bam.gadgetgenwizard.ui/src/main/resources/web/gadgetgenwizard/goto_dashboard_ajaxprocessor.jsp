<%@ page import="org.wso2.carbon.bam.gadgetgenwizard.ui.GGWUIUtils" %>
<script type="text/javascript">
    $(document).ready(function () {

        $("#gadgetgetatlbl").html(jsi18n["gadget.gen.at"]);
        $('#dashboardlink').html(jsi18n["go.to.dashboard"]);

        $('#dashboardlink').click(function() {
            $(this).target = "_blank";
            window.open($(this).prop('href'));
            return false;
        });

        $("#gadget-loc").html($.trim($("#serverUrl").html()) + $.trim($("#gadget-loc").html()));
    })

</script>
<table class="normal">
    <tbody>
    <tr>
        <td id="gadgetgetatlbl">
        </td>
        <td>
            <textarea id="gadget-loc" readonly="yes" cols="50" rows="5"
                      name="gadget-path"
                      style="width:400px"><%=request.getParameter("gadgetXMLPath")%></textarea>
            <input type="hidden" id="serverUrl" value="<%=GGWUIUtils.getServerUrl(request)%>">
        </td>
        <td>
            <a target="_blank" id="dashboardlink" href="../dashboard/index.jsp?region=region1&item=portal_menu"></a>
        </td>
    </tr>
    <input type="hidden" name="page" id="page" value="5">
    </tbody>
</table>
