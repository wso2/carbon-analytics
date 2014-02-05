<%@ page import="org.wso2.carbon.bam.gadgetgenwizard.ui.GGWUIUtils" %>
<%
    GGWUIUtils.overwriteSessionAttributes(request, session);


%>
<script type="text/javascript">
    $(document).ready(function() {

        $("#gadgettitlellbl").html(jsi18n["gadget.title"] + "<span style=\"color: red; \">*</span>");
        $("#gadgetfilenamelbl").html(jsi18n["gadget.filename"] + "<span style=\"color: red; \">*</span>");
        $("#refreshratelbl").html(jsi18n["refresh.rate"] + "<span style=\"color: red; \">*</span>");
    });
</script>

<table class="normal">
    <tbody>
    <tr>
        <td id="gadgettitlellbl">
        </td>
        <td><input type="text" class="validate" name="gadget-title" value="" style="width:300px"/></td>
    </tr>
    <tr>
        <td id="gadgetfilenamelbl">
        </td>
        <td><input type="text" class="validate" name="gadget-filename" value="" style="width:300px"/></td>
    </tr>
    <tr>
        <td id="refreshratelbl">
        </td>
        <td><input type="text" class="validate" name="refresh-rate" value="60" style="width:300px"/></td>
    </tr>
    <input type="hidden" name="page" id="page" value="4">
    </tbody>
</table>