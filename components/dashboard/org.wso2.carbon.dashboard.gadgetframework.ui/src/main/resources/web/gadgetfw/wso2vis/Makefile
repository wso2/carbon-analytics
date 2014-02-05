# Protovis Makefile
#
# Note: This Makefile depends on the YUI Compressor for JavaScript
# minification. The JAR file should be installed in the lib directory.
# The YUI Compressor is available at http://yuilibrary.com/downloads/

JS_CHART_FILES = \
    js/subscriber/chart/Chart.js \
    js/subscriber/chart/protovis/WedgeChart.js \
    js/subscriber/chart/protovis/PieChart.js \
    js/subscriber/chart/protovis/BarChart.js \
    js/subscriber/chart/protovis/ClusteredBarChart.js \
    js/subscriber/chart/protovis/ColumnChart.js \
    js/subscriber/chart/protovis/ClusteredColumnChart.js \
    js/subscriber/chart/protovis/AreaChart.js \
    js/subscriber/chart/protovis/AreaChart2.js \
    js/subscriber/chart/protovis/LineChart.js \
    js/subscriber/chart/protovis/LineChart2.js \
    js/subscriber/chart/protovis/Sunburst.js \
    js/subscriber/chart/raphael/FunnelChart.js \
    js/subscriber/chart/infovis/SpaceTree.js \
    js/subscriber/chart/infovis/HyperTree.js \
	js/subscriber/chart/composite/CompositeChart1.js \
    js/subscriber/form/TreeView.js \
	js/subscriber/gauge/Gauge.js \
	js/subscriber/gauge/raphael/Gauge1.js \
    js/subscriber/chart/raphael/DependencyTree.js \
    js/subscriber/chart/raphael/PieChart.js \
    js/control/TrafficLight.js

JS_DATA_FILES = \
    js/provider/Provider.js \
    js/provider/ProviderGET.js \
    js/provider/ProviderGETJSON.js \
    js/provider/ProviderGETMakeRequest.js \
    js/subscriber/Subscriber.js	

JS_FILTER_FILES = \
    js/filter/Filter.js \
    js/filter/BasicFilter.js \
    js/filter/CallbackFilter.js

JS_FILTERFORM_FILES = \
    js/filter/form/Select.js \
    js/filter/form/FilterForm.js

JS_UTIL_FILES = \
    js/wso2vis.js \
    js/util/Utils.js \
    js/util/Timer.js \
    js/control/Tooltip.js

JS_3RD_PARTY_DEBUG_FILES = \
    3rdparty/json2.js \
    3rdparty/qtip.js \
    3rdparty/protovis-r3.2.js \
    3rdparty/jquery-1.4.1.min.js \
    3rdparty/raphael-min.js \
    3rdparty/jit-yc.js \
    3rdparty/yui/build/yahoo-dom-event/yahoo-dom-event.js \
    3rdparty/yui/build/animation/animation-min.js \
    3rdparty/yui/build/calendar/calendar-min.js \
    3rdparty/yui/build/treeview/treeview-min.js
   
JS_3RD_PARTY_MIN_FILES = \
    3rdparty/json2.js \
    3rdparty/qtip.js \
    3rdparty/protovis-r3.2.js \
    3rdparty/jquery-1.4.1.min.js \
    3rdparty/raphael-min.js \
    3rdparty/jit-yc.js \
    3rdparty/yui/build/yahoo-dom-event/yahoo-dom-event.js \
    3rdparty/yui/build/animation/animation-min.js \
    3rdparty/yui/build/calendar/calendar-min.js \
    3rdparty/yui/build/treeview/treeview-min.js
    
JS_CONTROL_FILES = js/control/Control.js \
	js/control/Tooltip.js \
	js/control/DateRange.js

JS_ADAPTER_FILES = \
    js/adapter/Adapter.js

JS_FILES = \
    $(JS_UTIL_FILES) \
	$(JS_DATA_FILES) \
	$(JS_FILTER_FILES) \
    $(JS_FILTERFORM_FILES) \
	$(JS_CHART_FILES) \
	$(JS_ADAPTER_FILES) \
	$(JS_CONTROL_FILES)

all: wso2vis.js wso2vis-min.js

wso2vis.js: $(JS_FILES) Makefile
	#grep '	' -Hn $(JS_FILES) && echo "ERROR: tab" && exit 1 || true
	#grep '
#' -Hn $(JS_FILES) && echo "ERROR: dos newline" && exit 1 || true
	#grep ' $$' -Hn $(JS_FILES) && echo "ERROR: trailing space" && exit 1 || true
	cat $(JS_3RD_PARTY_DEBUG_FILES) > $@
	cat $(JS_FILES) >> $@
	

wso2vis-min.js: wso2vis.js
	rm -f $@
	cat $(JS_3RD_PARTY_MIN_FILES) > $@
	cat $(JS_UTIL_FILES) | java -jar lib/yuicompressor-2.4.2.jar --charset UTF-8 --type js >> $@
	cat $(JS_DATA_FILES) | java -jar lib/yuicompressor-2.4.2.jar --charset UTF-8 --type js >> $@
	cat $(JS_FILTER_FILES) | java -jar lib/yuicompressor-2.4.2.jar --charset UTF-8 --type js >> $@
	cat $(JS_FILTERFORM_FILES) | java -jar lib/yuicompressor-2.4.2.jar --charset UTF-8 --type js >> $@
	cat $(JS_CHART_FILES) | java -jar lib/yuicompressor-2.4.2.jar --charset UTF-8 --type js >> $@
	cat $(JS_ADAPTER_FILES) | java -jar lib/yuicompressor-2.4.2.jar --charset UTF-8 --type js >> $@
	cat $(JS_CONTROL_FILES) | java -jar lib/yuicompressor-2.4.2.jar --charset UTF-8 --type js >> $@	

clean:
	rm -rf wso2vis.js wso2vis-min.js

