<%@ page import="java.util.Map" %>
<%@ page import="org.wso2.carbon.bam.gadgetgenwizard.ui.GGWUIUtils" %>


<%
    GGWUIUtils.overwriteSessionAttributes(request, session);

    Object sqlParam = session.getAttribute("sql");
    String sql = (sqlParam == null) ? "" : ((String[])sqlParam) [0];

%>
<script type="text/javascript" src="../gadgetgenwizard/js/jquery.dataTables.min.js"></script>
<script type="text/javascript">
    $(document).ready(function() {
        $("#query-results-holder").hide();

        $("#execute-sql").click(function() {
            $.post("execute_sql_ajaxprocessor.jsp", $("form").serialize(), function(html) {
                var success = !(html.toLowerCase().match(/error executing query/));
                function getaoColumns(columnNames) {
                    var json = [];
                    for (var i = 0; i < columnNames.length; i++) {
                        var columnName = columnNames[i];
                        json.push({ sTitle : columnName});
                    }
                    return json;
                }
                if (success) {
                    var respJson = JSON.parse(html);

                    $("#query-results-holder").html("<table id=\"query-results\"></table>");
                    $("#query-results").dataTable({
                        "aaData" : respJson.Rows,
                        "aoColumns" : getaoColumns(respJson.ColumnNames)
                    });
                    $("#query-results-holder").show();
                } else {
                    CARBON.showErrorDialog(jsi18n["sql.invalid"] + html);
                }
            })
        });

        $("#sqllbl").html(jsi18n["sql.label"] + "<span style=\"color: red; \">*</span>");
        $("#execute-sql").val(jsi18n["preview.sql"]);
    });
</script>
<table class="normal">
    <tbody>
    <tr>
        <td id="sqllbl">
        </td>
        <td><textarea cols="50" rows="5" class="validate" type="text" name="sql" style="width:400px"><%=sql%></textarea></td>
        <td><input type="button" id="execute-sql" value=""></td>
    </tr>
    <tr>
        <td colspan="3">
            <div id="query-results-holder" style="padding-top:25px"></div>
        </td>
    </tr>
    <input type="hidden" name="page" id="page" value="2">
    </tbody>
</table>
