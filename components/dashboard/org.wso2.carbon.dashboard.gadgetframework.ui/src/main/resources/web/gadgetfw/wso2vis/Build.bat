@ECHO Building wso2vis library..
@ECHO ON
SETLOCAL
SET JS_SUBSCRIBER_FILES=js\subscriber\Subscriber.js^
+js\subscriber\chart\Chart.js^
+js\subscriber\chart\protovis\WedgeChart.js^
+js\subscriber\chart\protovis\BarChart.js^
+js\subscriber\chart\protovis\ClusteredBarChart.js^
+js\subscriber\chart\protovis\ColumnChart.js^
+js\subscriber\chart\protovis\ClusteredColumnChart.js^
+js\subscriber\chart\protovis\AreaChart.js^
+js\subscriber\chart\protovis\AreaChart2.js^
+js\subscriber\chart\protovis\LineChart.js^
+js\subscriber\chart\protovis\LineChart2.js^
+js\subscriber\chart\protovis\Sunburst.js^
+js\subscriber\chart\protovis\PieChart.js^
+js\subscriber\chart\raphael\FunnelChart.js^
+js\subscriber\chart\infovis\HyperTree.js^
+js\subscriber\chart\infovis\SpaceTree.js^
+js\subscriber\chart\composite\CompositeChart1.js^
+js\subscriber\form\Dump.js^
+js\subscriber\form\TreeView.js^
+js\subscriber\gauge\Gauge.js^
+js\subscriber\gauge\raphael\Gauge1.js^
+js\subscriber\chart\raphael\PieChart.js

SET JS_PROVIDER_FILES=js\provider\Provider.js^
+js\provider\ProviderGET.js^
+js\provider\ProviderGETJSON.js

SET JS_FILTER_FILES=js\filter\Filter.js^
+js\filter\BasicFilter.js^
+js\filter\form\FilterForm.js^
+js\filter\form\Select.js

SET JS_UTIL_FILES=js\wso2vis.js^
+js\util\Timer.js^
+js\util\Tooltip.js^
+js\util\Utils.js

SET JS_3RD_PARTY_DEBUG_FILES=3rdparty\json2.js^
+3rdparty\qtip.js^
+3rdparty\protovis-r3.1.js^
+3rdparty\jquery-1.4.1.min.js^
+3rdparty\raphael-min.js^
+3rdparty\yui\build\yahoo-dom-event\yahoo-dom-event.js^
+3rdparty\yui\build\animation\animation-min.js^
+3rdparty\yui\build\calendar\calendar-min.js^
+3rdparty\yui\build\treeview\treeview-min.js^
+3rdparty\jit-yc.js

SET JS_3RD_PARTY_MIN_FILES=3rdparty\json2.js^
+3rdparty\qtip.js^
+3rdparty\protovis-r3.1.js^
+3rdparty\jquery-1.4.1.min.js^
+3rdparty\raphael-min.js^
+3rdparty\yui\build\yahoo-dom-event\yahoo-dom-event.js^
+3rdparty\yui\build\animation\animation-min.js^
+3rdparty\yui\build\calendar\calendar-min.js^
+3rdparty\jit-yc.js

SET JS_ADAPTER_FILES=js\adapter\Adapter.js

SET JS_CONTROL_FILES=js\control\Control.js^
+js\control\Tooltip.js^
+js\control\DateRange.js

SET JS_FILES=%JS_UTIL_FILES%^
+%JS_PROVIDER_FILES%^
+%JS_FILTER_FILES%^
+%JS_SUBSCRIBER_FILES%^
+%JS_ADAPTER_FILES%^
+%JS_CONTROL_FILES%

ECHO Delete existing files..
DEL /F /Q wso2vis*.js 

ECHO Building wso2visd.js, the debug version of the library..
@COPY /b %JS_FILES% wso2vis-pre.js

ECHO Building wso2vis.js, the release version of the library using yuicompressor..
JAVA -jar lib/yuicompressor-2.4.2.jar wso2vis-pre.js -o wso2vis-pre-min.js --charset UTF-8 

@COPY /b %JS_3RD_PARTY_DEBUG_FILES%+wso2vis-pre.js wso2vis.js
@COPY /b %JS_3RD_PARTY_MIN_FILES%+wso2vis-pre-min.js wso2vis-min.js

DEL /F /Q wso2vis-pre*.js 

REM COPY /b wso2*.js ../*.*

ENDLOCAL
ECHO Build Complete.
@ECHO ON
