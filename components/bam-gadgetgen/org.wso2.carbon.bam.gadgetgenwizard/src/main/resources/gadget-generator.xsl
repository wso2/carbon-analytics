<xsl:stylesheet version="2.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:fn="http://www.w3.org/2005/02/xpath-functions"
                xmlns:m0="http://services.samples"
                xmlns:gg="http://wso2.com/bam/gadgetgen"
                exclude-result-prefixes="gg m0 fn">
    <xsl:output method="xml" omit-xml-declaration="yes" indent="yes"/>


    <xsl:template match="/gg:gadgetgen">

<Module>
  <ModulePrefs height="300" scaling="false">
      <xsl:attribute name="title"><xsl:value-of select="gg:gadget-title" /></xsl:attribute>
    <Require feature="dynamic-height"/>
  </ModulePrefs>
  <Content type="html">
      <xsl:text>&#10;</xsl:text>
      <!-- CDATA -->
  <xsl:text disable-output-escaping="yes">&lt;![CDATA[</xsl:text>
    <xsl:text>&#10;</xsl:text>

      <xsl:choose>
          <xsl:when test="gg:BarChart">
              <xsl:call-template name="BarChartResources"/>
          </xsl:when>
          <xsl:when test="gg:Table">
              <xsl:call-template name="TableResources"/>
          </xsl:when>
      </xsl:choose>

      <!--[if lt IE 9]><script language="javascript" type="text/javascript" src="../src/excanvas.min.js"></script><![endif]-->
    <xsl:text>&#10;</xsl:text>
    <xsl:text disable-output-escaping="yes">&lt;!--[if lt IE 9]&gt;&lt;script language="javascript" type="text/javascript" src="js/excanvas.min.js"&gt;&lt;/script&gt;&lt;![endif]--&gt;</xsl:text>
    <xsl:text>&#10;</xsl:text>

    <script type="text/javascript" lang="javascript">
    $(document).ready(function () {

            var widthToHeightRatio = 325/250;
            var width = gadgets.window.getViewportDimensions()["width"];
            var height = (width/widthToHeightRatio);

            $("#ui-element").width(width);
            $("#ui-element").height(height);

            var plot = null;

            update();
            setInterval(update, <xsl:value-of select="gg:refresh-rate" /> * 1000 );

            function update() {
                var respJson = null;
                $.ajax({
                    url: "<xsl:value-of select="gg:jaggeryAppUrl" /><xsl:value-of select="gg:gadget-filename" />.jag",

                    dataType: 'json',
                    //GET method is used
                    type: "POST",

                    async: false,

                    //pass the data
                    data: "",

                    //Do not cache the page
                    cache: false,

                    //success
                    success: function (html) {

                        respJson = html;
                    }
                });

        <!--<xsl:if test="gg:BarChart">-->
        <xsl:choose>
            <xsl:when test="gg:BarChart">
                <xsl:call-template name="BarChart"/>
            </xsl:when>
            <xsl:when test="gg:Table">
                <xsl:call-template name="Table"/>
            </xsl:when>
        </xsl:choose>
        <!--</xsl:if>-->
                gadgets.window.adjustHeight();
            };
        });
    </script>

<div style="width: 325px; height: 250px;" id="ui-element"/>
<div id="text1"/>
         <xsl:text>&#10;</xsl:text>
  <xsl:text disable-output-escaping="yes">]]&gt;</xsl:text>
      <xsl:text>&#10;</xsl:text>
  </Content>
</Module>
    </xsl:template>

    <xsl:template name="BarChart">
        $("#ui-element").html('');
        plot = $.jqplot('ui-element', [respJson], {
                    title: '<xsl:value-of select="gg:bar-title" />',
                    series:[{renderer:$.jqplot.BarRenderer}],
                    axes: {
                        xaxis: {
                            renderer: $.jqplot.CategoryAxisRenderer,
                            label: '<xsl:value-of select="gg:bar-xlabel" />',
                            // labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                            tickRenderer: $.jqplot.CanvasAxisTickRenderer,
                            tickOptions: {
                                enableFontSupport: true,
                                angle: -30
                            }

                        },
                        yaxis: {
                            autoscale:true,
                            label: '<xsl:value-of select="gg:bar-ylabel" />',
                            // labelRenderer: $.jqplot.CanvasAxisLabelRenderer,
                            tickRenderer: $.jqplot.CanvasAxisTickRenderer,
                            tickOptions: {
                                enableFontSupport: true,
                                angle: -30
                            }
                        }
                    }
                });
                plot.replot();
    </xsl:template>
    <xsl:template name="Table">
        function getaoColumns(columnNames) {
            var json = [];
            for (var i = 0; i <xsl:text disable-output-escaping="yes">&#60;</xsl:text> columnNames.length; i++) {
                var columnName = columnNames[i];
                json.push({ sTitle : columnName});
            }
            return json;
        }

        $("#ui-element").html("<xsl:text disable-output-escaping="yes">&lt;div style=\"text-align: center;\"&gt;&lt;b&gt;</xsl:text>" + "<xsl:value-of select="gg:table-title" />" +
        "<xsl:text disable-output-escaping="yes">&lt;/b&gt;&lt;/div&gt;&lt;br/&gt;" +
                "&lt;table id=\"query-results\" style=\"width:100%\"&gt;&lt;/table&gt;</xsl:text>");
        $("#query-results").dataTable({
            "aaData" : respJson.Rows,
            "aoColumns" : getaoColumns(respJson.ColumnNames)
        });
    </xsl:template>

    <xsl:template name="TableResources">
        <xsl:text disable-output-escaping="yes">
        &lt;link href="css/jquery.dataTables.css" type="text/css" rel="stylesheet"/&gt;
        &lt;script src="js/jquery.min.js" type="text/javascript">&lt;/script&gt;
        &lt;script src="js/jquery.dataTables.min.js" type="text/javascript">&lt;/script&gt;
        </xsl:text>
    </xsl:template>


    <xsl:template name="BarChartResources">
        <xsl:text disable-output-escaping="yes">
    &lt;link href="css/jquery.jqplot.min.css" type="text/css" rel="stylesheet"/&gt;
    &lt;script src="js/jquery.min.js" type="text/javascript">&lt;/script&gt;
    &lt;script src="js/jquery.jqplot.min.js" type="text/javascript">&lt;/script&gt;
    &lt;script type="text/javascript" src="js/plugins/jqplot.categoryAxisRenderer.js"&gt;&lt;/script&gt;
    &lt;script type="text/javascript" src="js/plugins/jqplot.barRenderer.js"&gt;&lt;/script&gt;
    </xsl:text>
    </xsl:template>

</xsl:stylesheet>