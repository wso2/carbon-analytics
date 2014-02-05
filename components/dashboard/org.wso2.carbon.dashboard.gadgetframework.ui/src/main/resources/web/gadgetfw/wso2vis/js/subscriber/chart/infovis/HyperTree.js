//Class c.infovis.HyperTree : Chart
//This is the custom wrapper class for protovis bar charts

//Constructor
wso2vis.s.chart.infovis.HyperTree = function(divElementLog, canvas, chartTitle, chartDesc) {
    wso2vis.s.chart.Chart.call(this, canvas, chartTitle, chartDesc);

    /* @private */
    this.divElementLog = divElementLog;
    this.canvas = canvas;
    this.ht = null;
    this.y = null;
    this.x = null;
}

// this makes c.infovis.HyperTree.prototype inherits
// from Chart.prototype
wso2vis.extend(wso2vis.s.chart.infovis.HyperTree, wso2vis.s.chart.Chart);

wso2vis.s.chart.infovis.HyperTree.prototype
    .property("dataField")
    .property("dataValue")
    .property("dataLabel")
    .property("ySuffix")
    .property("xSuffix");


function addEvent(obj, type, fn) {
    if (obj.addEventListener) obj.addEventListener(type, fn, false);
    else obj.attachEvent('on' + type, fn);
};

//Public function loadChart
//Loads the chart inside the given HTML element

wso2vis.s.chart.infovis.HyperTree.prototype.load = function (w, h) {
	 
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
	
var canvas = new Canvas('mycanvas', {
	        'injectInto': that.canvas,
	        'width': that.width(),
	        'height': that.height(),
	        'backgroundColor': '#1a1a1a'
	    });

    //end
    var style = document.getElementById('mycanvas').style;
    style.marginLeft = style.marginTop = "25px";
    //init Hypertree
    this.ht = new Hypertree(canvas, {
        //Change node and edge styles such as
        //color, width and dimensions.
        Node: {
            dim: 9,
            color: "#f00"
        },
        
        Edge: {
            lineWidth: 2,
            color: "#024"
        },
        
        onBeforeCompute: function(node){
            //Log.write("centering");
        },
        //Attach event handlers and add text to the
        //labels. This method is only triggered on label
        //creation
        onCreateLabel: function(domElement, node){
            domElement.innerHTML = node.name;
            addEvent(domElement, 'click', function () {
                that.ht.onClick(node.id);
            });
        },
        //Change node styles when labels are placed
        //or moved.
        onPlaceLabel: function(domElement, node){
            var style = domElement.style;
            style.display = '';
            style.cursor = 'pointer';
            if (node._depth <= 1) {
                style.fontSize = "0.8em";
                style.color = "#000";

            } else if(node._depth == 2){
                style.fontSize = "0.7em";
                style.color = "#011";

            } else {
                style.display = 'none';
            }

            var left = parseInt(style.left);
            var w = domElement.offsetWidth;
            style.left = (left - w / 2) + 'px';
        },
        
        onAfterCompute: function(){
           // Log.write("done");
	    }
	});
	    
};


wso2vis.s.chart.infovis.HyperTree.prototype.populateData = function (thisObject) {
	// Space Tree can only be drawn with a JSON Tree i.e. with JSON nodes
    var _dataField = thisObject.traverseToDataField(thisObject.data, thisObject.dataField());
if ((_dataField instanceof Array) && (_dataField.length < 1)) {
	return false;
}
var ht = thisObject.ht
var lbs = ht.fx.labels;
for (label in lbs) {
 	   if (lbs[label]) {
 	    lbs[label].parentNode.removeChild(lbs[label]);
 	   }
}
ht.fx.labels = {};
thisObject.ht.loadJSON(_dataField[0]);
return true;

  };



wso2vis.s.chart.infovis.HyperTree.prototype.update = function () {
	var ht = this.ht;
if (this.populateData(this)) {
   ht.refresh();
   ht.controller.onAfterCompute();  
	
    if(this.tooltip() === true) {
        tooltip.init();
    }
}
};

