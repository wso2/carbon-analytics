var defaultDataProvider = null;
var timer = null;
var r = null;
var uwElements = []; //ui elements
var hwElements = []; //hidden elements
var propGrid;
var editor;

window.onload = function() {
	global_initialize();
}

function global_initialize() 
{
	$("#tabs").tabs({
		select: function(event, ui) {
			if (ui.index == 1)
				editor.setCode(generateCode());
			return true;
		}
	});
	wso2vis.initialize();
	defaultDataProvider = new wso2vis.ide.hwElement(new wso2vis.p.ProviderGET("../examples/generator2.php"),
													0,
													"dataProvider0");
																		
	timer = new wso2vis.u.Timer(5000);
	timer.tick = function() { defaultDataProvider.pullData(); };
	r = Raphael("canvaz", 760, 560);

	$(function() {
		$(".draggable").draggable({ revert: true, opacity: 0.7, helper: 'clone' });
		$("#droppable").droppable({			
			//hoverClass: 'ui-state-hover-droppable',
			drop: dropped
		});
		
		propGrid = new wso2vis.ide.PropertyGrid("property-grid");
	});
	
	defaultDataProvider.initialize();
	timer.startTimer();
	hwElements.push(defaultDataProvider);
	updateCanvasElementsDropdown();
	
	var that = this;
	function deselectAll() {
		for (var i = 0; i < uwElements.length; i++) {
			uwElements[i].deselect();
		}
	}
	
	$(r.canvas.parentNode).click(deselectAll);
	
	editor = CodeMirror.fromTextArea('codez', {
		height: "690px",
		width: "1150px",
		parserfile: ["parsexml.js", "parsecss.js", "tokenizejavascript.js", "parsejavascript.js", "parsehtmlmixed.js", "parsegadgetxml.js"],
		stylesheet: ["editor/css/xmlcolors.css", "editor/css/jscolors.css", "editor/css/csscolors.css"],
		path: "editor/js/",
		readOnly:true
		//lineNumbers: true
		//autoMatchParens: true
	  });
	
}

function dropped(event, ui) {
	switch(ui.draggable[0].id) 
	{
		case "piechart":
			var chart = new wso2vis.s.chart.raphael.PieChart()
						.raphaelPaper(r) //optional
						.dataField(["services", "service"])
						.dataValue(["stats", "requestCount"])
						.dataLabel(["@name"])
						.tooltip(true)      //default: true
						.showPercent(true)  //default: true
						.showValue(true)
						.width(300);
			var uwElement = new wso2vis.ide.uwElement(r, chart, 300, 300, defaultDataProvider, true, "pieChart" + uwElements.length, pieChartPropObj, 0);
			uwElements.push(uwElement);
			defaultDataProvider.addDataReceiver(uwElement);
			uwElement.redraw(ui.offset.left, ui.offset.top);
			defaultDataProvider.pullDataSync();
			
			//Property Grid
			propGrid.load(uwElement);
			selectUwElementByIndex(uwElements.length - 1);
			
			break;
		case "columnchart":
			alert("barchart");
			break;
		case "dataprovider1":
			var dataProvider = new wso2vis.ide.hwElement(new wso2vis.p.ProviderGET("../examples/generator2.php"),
									0,
									"dataProvider" + hwElements.length);
			dataProvider.initialize();
			hwElements.push(dataProvider);
			break;
	}
	updateCanvasElementsDropdown();
}

function updateCanvasElementsDropdown() {
	$("#canvasElementsDropdown").html("");
	var i;
	for (i = 0; i < uwElements.length; i++)
		$("<option value='"+ uwElements[i].name +"'>" + uwElements[i].name + "</option>").appendTo("#canvasElementsDropdown");
	for (i = 0; i < hwElements.length; i++)
		$("<option value='"+ hwElements[i].name +"'>" + hwElements[i].name + "</option>").appendTo("#canvasElementsDropdown");
}

function canvasElementsDropdownOnChange(ele) {
    var elename = ele.options[ele.selectedIndex].value;
    var uwIndex = findIndexByName_uwElement(elename);
    var hwIndex = findIndexByName_hwElement(elename);
    
    if( uwIndex >= 0 ){
        propGrid.load(uwElements[uwIndex]);
        selectUwElementByIndex(uwIndex);
    }
    else if( hwIndex >= 0 ){
    
    }
}

function generateCode() {
	var i;
	var code = "";
	code += "<html>\n";
	code += "	<head>\n";
	code += " 		<title>wso2vis IDE Demo</title>\n";
	code += "		<script type=\"text/javascript\" src=\"wso2vis.js\"></script>\n";
	code += "		<script language=\"javascript\" type=\"text/javascript\">\n";
	code += "			var r;\n";					
	for (i = 0; i < hwElements.length; i++) {
		if (hwElements[i].type == 0)
			code += "			var "+ hwElements[i].name +" = new wso2vis.p.ProviderGET(\"http://localhost/wso2vis/examples/generator2.php\"); \n";     
	}
	code += "			var timer = new wso2vis.u.Timer(5000);\n";
	for (i = 0; i < uwElements.length; i++) {
		switch (uwElements[i].type)
		{
			case 0:
				code += "			var "+ uwElements[i].name +" = new wso2vis.s.chart.raphael.PieChart(); \n";     
		}
	}
	
	code += "			\n";
	//code += "			dataProvider.addDataReceiver(pieChart);\n";
	code += "			timer.tick = globalTick;\n";
	code += "			window.onload = initialize;\n";
	code += "			\n";
	code += "			function initialize() {\n";
	code += "				wso2vis.initialize();\n";
	code += "				r = Raphael(\"canvaz\", 760, 560);\n";

	code += "				\n";

	for (i = 0; i < uwElements.length; i++) {
		switch (uwElements[i].type)
		{
			case 0:
				code += "				" + uwElements[i].name + ".load(250)\n";
				code += "					.raphaelPaper(r)\n";
				code += "					.dataField([\"services\", \"service\"])\n";
				code += "					.dataValue([\"stats\", \"requestCount\"])\n";
				code += "					.dataLabel([\"@name\"])\n";
				code += "					.tooltip(true)\n";      
				code += "					.showPercent(true)\n";  
				code += "					.showValue(true);\n";  
				code += "				dataProvider0.addDataReceiver(" + uwElements[i].name + ");\n";
		}
	}
	code += "					\n";
	for (i = 0; i < hwElements.length; i++) {
		if (hwElements[i].type == 0)
			code += "				"+ hwElements[i].name +".initialize();\n";
	}	
	code += "				timer.startTimer();\n";
	code += "			}\n";
	code += "			\n";
	code += "			function globalTick() {\n";
	for (i = 0; i < hwElements.length; i++) {
		if (hwElements[i].type == 0)
			code += "				"+ hwElements[i].name +".pullData();\n";
	}	
	code += "			}\n";
	code += "			\n";
	code += "		</script>\n";
	code += "	</head>\n";
	code += "	<body>\n";
	code += "		<h2> wso2vis IDE Demo </h2>\n";
	code += "		<div id=\"canvaz\"></div>\n";
	code += "		\n";
	code += "	</body>\n";
	code += "</html>\n";
	
	return code;
}

function findIndexByName_uwElement(elementName) {
    for (i = 0; i < uwElements.length; i++) {
		if(uwElements[i].name == elementName) {
		    return i;
		}
    }
    return -1;
}

function findIndexByName_hwElement(elementName) {
	for (i = 0; i < hwElements.length; i++) {
		if(hwElements[i].name == elementName) {
		    return i;
		}
    }
    return -1;
}

function selectUwElementByIndex(index) {
    for (var ix = 0; ix < uwElements.length; ix++) {
        if( ix == index ){
            uwElements[ix].select();
        }
		else {
		    uwElements[ix].deselect();
		}
    }
}

