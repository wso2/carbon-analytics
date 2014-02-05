<%@ page import="org.wso2.carbon.bam.gadgetgenwizard.ui.GGWUIUtils" %>
<%

    GGWUIUtils.overwriteSessionAttributes(request, session);

%>
<script type="text/javascript" src="../gadgetgenwizard/js/jquery.jqplot.min.js"></script>
<script type="text/javascript" src="../gadgetgenwizard/js/plugins/jqplot.barRenderer.js"></script>
<script type="text/javascript" src="../gadgetgenwizard/js/plugins/jqplot.categoryAxisRenderer.js"></script>


<script type="text/javascript">
    $(document).ready(function () {

        var sqlResultJSON = null;



        //get sql results

        $.post("execute_sql_ajaxprocessor.jsp", null, function(html) {
            var success = !(html.toLowerCase().match(/error executing query/));
            if (success) {
                sqlResultJSON = JSON.parse(html);
            } else {
                CARBON.showErrorDialog(html);
            }
        });

        $("#select-uielement").html(function() {

            var supportedUIElements = {};
            supportedUIElements[jsi18n["bar.graph"]] = "bar";
            supportedUIElements[jsi18n["table"]] = "table";

            var html = "";
            $.each(supportedUIElements, function(key, val) {
                html += "<option value=\"" + val +"\">"+ key + "</option>";
            });
            return html;
        });

        $("#tabletitle").html(jsi18n["table.title"] + "<span style=\"color: red; \">*</span>");
        $("#charttitle").html(jsi18n["chart.title"]);
        $("#ylabel").html(jsi18n["y.axis.label"]);
        $("#ycol").html(jsi18n["y.axis.col"] + "<span style=\"color: red; \">*</span>");
        $("#xlabel").html(jsi18n["x.axis.label"]);
        $("#xcol").html(jsi18n["x.axis.col"] + "<span style=\"color: red; \">*</span>");
        $("#pickuilbl").html(jsi18n["pick.ui.ele"]);
        $("#preview").val(jsi18n["preview"]);
        $("#preview-area").html(jsi18n["preview.area"]);

        $("#preview").click(function () {
            if ($("#select-uielement").val() == "bar") {
                genBarGraphPreview();
            } else if ($("#select-uielement").val() == "table") {
                genTablePreview();
            }
        });

        uiElementChange();

        $("#select-uielement").change(function() {
            uiElementChange();
        });



        function uiElementChange() {
            // hiding all ui element divs
            $(".uielements").hide();

            // clear preview div
            $("#preview-area").html(jsi18n["preview.area"]);

            if ($("#select-uielement").val() == "bar") {
                $("#bar-chart-options").show('fast', function() {
                    var optionHTML = "";
                    $.each(sqlResultJSON.ColumnNames, function(i, val) {
                        optionHTML += "<option value=\"" + val + "\">" + val + "</option>"
                    });
                    $("#bar-chart-options [name$=\"column\"]").html(optionHTML);
                    $("#bar-chart-options .bar").change(genBarGraphPreview);
                    $("#bar-chart-options .bar").bind('input', genBarGraphPreview);
                });

            } else if ($("#select-uielement").val() == "table"){
                $("#table-options").show();
                $("#table-options .table").change(genTablePreview);
                $("#table-options .table").bind('input', genTablePreview);
                genTablePreview();
            }
        }

        function genTablePreview() {
            function getaoColumns(columnNames) {
                var json = [];
                for (var i = 0; i < columnNames.length; i++) {
                    var columnName = columnNames[i];
                    json.push({ sTitle : columnName});
                }
                return json;
            }

            $("#preview-area").html("<div style=\"text-align: center;\"><b>" + $("[name=table-title]").val() + "</b></div><br/>" +
                    "<table id=\"query-results\"></table>");
            $("#query-results").dataTable({
                "aaData" : sqlResultJSON.Rows,
                "aoColumns" : getaoColumns(sqlResultJSON.ColumnNames)
            });
            $("#preview-area").height(Math.max(250, $("#query-results").height()));
            $("#preview-area").width(Math.max(325, $("#query-results").width()));
        }

        function genBarGraphPreview() {

            var allValsComplete = $("#bar-chart-options .bar").map(function(i, e) {
                return $(e).val();
            }).get().join(",");
            var matchVals = allValsComplete.match(/,/g);

            if (sqlResultJSON && matchVals.length >= 3) {

                var xColIndex;
                var yColIndex;

                $.each(sqlResultJSON.ColumnNames, function(i, val) {
                    if ($("[name=\"bar-xcolumn\"]").val() == val) {
                        xColIndex = i;
                    }
                    if ($("[name=\"bar-ycolumn\"]").val() == val) {
                        yColIndex = i;
                    }
                });

                var plotArray = [];
                $.each(sqlResultJSON.Rows, function (i, val) {
                    plotArray.push([val[xColIndex], parseInt(val[yColIndex])]);
                });

                // clear div
                $("#preview-area").html("");

                $.jqplot('preview-area', [plotArray], {
                    title: $("[name=bar-title]").val(),
                    series:[{renderer:$.jqplot.BarRenderer}],
                    axes: {
                        xaxis: {
                            renderer: $.jqplot.CategoryAxisRenderer,
                            label: $("[name=bar-xlabel]").val(),
                            // labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                            tickRenderer: $.jqplot.CanvasAxisTickRenderer,
                            tickOptions: {
                                enableFontSupport: true,
                                angle: -30
                            }

                        },
                        yaxis: {
                            autoscale:true,
                            label: $("[name=bar-ylabel]").val(),
                            // labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                            tickRenderer: $.jqplot.CanvasAxisTickRenderer,
                            tickOptions: {
                                enableFontSupport: true,
                                angle: -30
                            }
                        }
                    }
                });


            }
        }



    })

</script>
<table class="normal">
    <tbody>
    <tr>
        <td>
            <table>
                <tbody>
                <tr>
                    <td id="pickuilbl">
                    </td>
                    <td><select name="uielement" id="select-uielement" style="width:200px">
                    </select>
                    </td>
                    <td><input id="preview" type="button"></td>

                </tr>
                <tr>
                    <td colspan="3">
                        <div class="uielements" id="bar-chart-options">
                            <table>
                                <tbody>
                                <tr>
                                    <td id="charttitle">
                                    </td>
                                    <td><input type="text" class="bar" name="bar-title" value="" style="width:150px"></td>
                                </tr>
                                <tr>
                                    <td id="ylabel"></td>
                                    <td><input class="bar" type="text" name="bar-ylabel" value="" style="width:150px"></td>
                                </tr>
                                <tr>
                                    <td id="ycol"></td>
                                    <td><select class="bar validate" name="bar-ycolumn" style="width:150px"></select></td>
                                </tr>
                                <tr>
                                    <td id="xlabel"></td>
                                    <td><input type="text" class="bar" name="bar-xlabel" value="" style="width:150px"></td>
                                </tr>
                                <tr>
                                    <td id="xcol"></td>
                                    <td><select class="bar validate" name="bar-xcolumn" style="width:150px"></select></td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                        <div class="uielements" id="table-options">
                            <table>
                                <tbody>
                                <tr>
                                    <td id=tabletitle>
                                    </td>
                                    <td><input type="text" class="table" name="table-title" value="" style="width:150px"></td>
                                </tr>
                                </tbody>
                            </table>
                        </div>
                    </td>
                </tr>
                </tbody>
            </table>
        </td>
        <td align="center" style="border:1px solid #000 !important">
            <div id="preview-area" style="text-align:center;width:325px;height:250px"></div>
        </td>
        <input type="hidden" name="page" id="page" value="3"/>
    </tr>
    </tbody>
</table>