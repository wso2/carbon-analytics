//Class c.infovis.SpaceTree : Chart
//This is the custom wrapper class for protovis bar charts

//Constructor
wso2vis.s.chart.infovis.SpaceTree = function(divElementLog, canvas, chartTitle, chartDesc) {
	wso2vis.s.chart.Chart.call(this, canvas, chartTitle, chartDesc);

	/* @private */
	this.divElementLog = divElementLog;
	this.canvas = canvas;
	this.st = null;
	this.y = null;
	this.x = null;
	this.tip = new wso2vis.c.Tooltip();
	this.testLabel = null;
	this.edgeLabelArray = null;
};

//this makes c.infovis.SpaceTree.prototype inherits
//from Chart.prototype
wso2vis.extend(wso2vis.s.chart.infovis.SpaceTree, wso2vis.s.chart.Chart);

wso2vis.s.chart.infovis.SpaceTree.prototype
.property("dataField")
.property("dataValue")
.property("dataLabel")
.property("ySuffix")
.property("xSuffix");


ST.Plot.EdgeTypes.implement({
	'custom-line': function(adj, canvas) {
	//plot arrow edge
	this.edgeTypes.arrow.call(this, adj, canvas);
	//get nodes cartesian coordinates
	var pos = adj.nodeFrom.pos.getc(true);
	var posChild = adj.nodeTo.pos.getc(true);
	//check for edge label in data
	var data = adj.nodeTo.data;
	if(data.labelid && data.labeltext) {
		var domlabel = document.getElementById(data.labelid);
		//if the label doesn't exist create it and append it
		//to the label container
		if(!domlabel) {
			var domlabel= document.createElement('div');
			domlabel.id = data.labelid;
			domlabel.innerHTML = data.labeltext;
			//add some custom style
			var style = domlabel.style;
			style.position = 'absolute';
			style.color = '#00f';
			style.fontSize = '9px';
			//append the label to the labelcontainer
			this.getLabelContainer().appendChild(domlabel);
			
		}

		//now adjust the label placement
		var radius = this.viz.canvas.getSize();
		domlabel.style.left = parseInt((pos.x + posChild.x + radius.width - domlabel.offsetWidth) /2) + 'px';
		domlabel.style.top = parseInt((pos.y + posChild.y + radius.height) /2) + 'px';
	}
}
});


//Public function loadChart
//Loads the chart inside the given HTML element
wso2vis.s.chart.infovis.SpaceTree.prototype.load = function (w, h) {

	if (w !== undefined) {
		this.width(w);
	}	
	if (h !== undefined) {
		this.height(h);
	}

	that = this;
	var Log = {
			elem: false,
			write: function(text){
		if (!this.elem) 
			this.elem = that.divElementLog;
		this.elem.innerHTML = text;
		this.elem.style.left = (500 - this.elem.offsetWidth / 2) + 'px';
	}
	};
	//init canvas
	//Create a new canvas instance.
	var canvas = new Canvas('mycanvas', {
		'injectInto': that.canvas,
		'width': that.width(),
		'height': that.height(),
		'backgroundColor': '#1a1a1a'
	});
	//end

	//init st
	//Create a new ST instance
	this.st = new ST(canvas, {
		//set duration for the animation
		orientation: "left",
		duration: 400,
		//set animation transition type
		transition: Trans.Quart.easeInOut,
		//set distance between node and its children
		levelDistance: 60,
		//set node and edge styles
		//set overridable=true for styling individual
		//nodes or edges
		Node: {
		width:20,
		type: 'none',
		color: '#aaa',
		overridable: true
	},

	Edge: {
		type: 'arrow',
		overridable: true
		

	},

	onBeforeCompute: function(node){

		//     Log.write("loading " + node.name);
	},

	onAfterCompute: function(){
		//Log.write("done");
	},

	//This method is called on DOM label creation.
	//Use this method to add event handlers and styles to
	//your node.
	onCreateLabel: function(label, node){
		label.id = node.id;
		label.innerHTML = node.name;
		label.onclick = function(){
			that.st.onClick(node.id);

			//	Log.write("Done");
		};
		label.onmouseover = function(e){
			that.tip.show(e.pageX, e.pageY, node.name);	
			//	Log.write("mouse is over the" + node.name + "label, triggering mouse event : " + e.toString());
		};
		label.onmouseout = function () {
			that.tip.hide();
		};
		//set label styles
		var style = label.style;
		style.width = 10 + 'px';
		style.height = 17 + 'px';            
		style.cursor = 'pointer';
		style.color = '#333';
		style.fontSize = '0.8em';
		style.textAlign= 'center';
		style.paddingTop = '4px';
		style.paddingLeft = '3px';
	},

	//This method is called right before plotting
	//a node. It's useful for changing an individual node
	//style properties before plotting it.
	//The data properties prefixed with a dollar
	//sign will override the global node style properties.
	onBeforePlotNode: function(node){
		//add some color to the nodes in the path between the
		//root node and the selected node.

		if (node.selected) {
			node.data.$color = "#ff7";
		}
		else {
			delete node.data.$color;
			var GUtil = Graph.Util;
			//if the node belongs to the last plotted level
			if(!GUtil.anySubnode(node, "exist")) {
				//count children number
				var count = 0;
				GUtil.eachSubnode(node, function(n) { count++; });
				//assign a node color based on
				//how many children it has
				node.data.$color = ['#aaa', '#baa', '#caa', '#daa', '#eaa', '#faa'][count];                    
			}
		}
	},

	//This method is called right before plotting
	//an edge. It's useful for changing an individual edge
	//style properties before plotting it.
	//Edge data proprties prefixed with a dollar sign will
	//override the Edge global style properties.
	onBeforePlotLine: function(adj){
		if (adj.nodeFrom.selected && adj.nodeTo.selected) {
			adj.data.$color = "#eed";
			adj.data.$lineWidth = 3;
		}
		else {
			delete adj.data.$color;
			delete adj.data.$lineWidth;
		}
	}
	});

};


wso2vis.s.chart.infovis.SpaceTree.prototype.populateData = function (thisObject) {
	// Space Tree can only be drawn with a JSON Tree i.e. with JSON nodes
	var _dataField = thisObject.traverseToDataField(thisObject.data, thisObject.dataField());
	if ((_dataField instanceof Array) && (_dataField.length < 1)) {
		return false;
	}
	var st = thisObject.st;
	var lbs = st.fx.labels;

	for (label in lbs) {
		if (lbs[label]) {
			lbs[label].parentNode.removeChild(lbs[label]);
		}
	}
//	for (edge in edges) {
//	if (edges(edge)) {
//	edges[edge]
//	}
	st.fx.labels = {};
//	thisObject.adjustWidth(_dataField[0]);
	thisObject.st.loadJSON(_dataField[0]);
	return true;

};

wso2vis.s.chart.infovis.SpaceTree.prototype.trim = function (txt) { 
	if (txt.length > 20) {
		var str = txt.substr(0,18);
		str += "...";	
	} else {
		var str = txt;
	}
	return str;
}


wso2vis.s.chart.infovis.SpaceTree.prototype.getNodeDiv = function () { 
	if (this.testLabel == null) {

		var testLabel = this.testLabel = document.createElement('div'); 
		testLabel.id = "mytestlabel"; 
		testLabel.style.visibility = "hidden"; 
		testLabel.style.position = "absolute";
		testLabel.style.height = 20 + 'px'; 
		document.body.appendChild(testLabel);
		return this.testLabel;
	}
}



wso2vis.s.chart.infovis.SpaceTree.prototype.adjustWidth = function(tree) {
	var elem = this.getNodeDiv();
	TreeUtil.each(tree, function(node) { 
		elem.innerHTML = that.trim(node.name); 
		node.data.$width = elem.offsetWidth; 
	}); 
} 

wso2vis.s.chart.infovis.SpaceTree.prototype.update = function () {
	var st = this.st;
	if (this.populateData(this)) {
		//compute node positions and layout
		st.compute();
		//optional: make a translation of the tree
		st.geom.translate(new Complex(-200, 0), "startPos");
		//emulate a click on the root node.
		st.onClick(st.root);

		if(this.tooltip() === true) {
			tooltip.init();
		}
	}
};
