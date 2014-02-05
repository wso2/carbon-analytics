//Class AreaChart : Chart
//This is the custom wrapper class for protovis area/line charts

//Constructor
wso2vis.s.chart.protovis.AreaChart = function(div, chartTitle, chartDesc) {
    wso2vis.s.chart.Chart.call(this, div, chartTitle, chartDesc);

    this.band(12)
        .ySuffix("")
        .xSuffix("")
        .xInterval(10000)
		.dirFromLeft(true);

    /* @private */
    this.dataHistory = [];
    this.vis = null;
    this.x = null;
    this.y = null;
};

// this makes AreaChart.prototype inherit from Chart
wso2vis.extend(wso2vis.s.chart.protovis.AreaChart, wso2vis.s.chart.Chart);

wso2vis.s.chart.protovis.AreaChart.prototype
    .property("band")
    .property("dataField")
    .property("dataValue")
    .property("dataLabel")
    .property("ySuffix")
    .property("xSuffix")
    .property("xInterval")
	.property("dirFromLeft");

//Public function load
//Loads the chart inside the given HTML element
wso2vis.s.chart.protovis.AreaChart.prototype.load = function (w, h, band) {
    if ( w !== undefined ) {
        this.width(w);
    }
    if ( h !== undefined ) {
        this.height(h);
    }
    if ( band !== undefined ) {
        this.band(band);
    }

    var thisObject = this;

    this.x = pv.Scale.linear(0, this.band()).range(0, this.width());
    this.y = pv.Scale.linear(0, 50).range(0, this.height()*0.9);
 
    this.vis = new pv.Panel()
        .canvas(function() { return thisObject.divEl(); })
        .width(function() { return thisObject.width(); })
        .height(function() { return thisObject.height(); })
        .bottom(20)
        .top(0)
        .left(30)
        .right(10);

    var panel = this.vis.add(pv.Panel)
        .data(function() { return thisObject.getData(thisObject); })
        .top(function() { return (thisObject.height() * (1 - thisObject.titleSpacing())); })
        .height(function() { return (thisObject.height() * thisObject.titleSpacing()); });
        //.strokeStyle("#ccc");

    var area = panel.add(pv.Area)
        .data(function(a) { return a; })
        .left(function(d) 
			  {
				  if (thisObject.dirFromLeft())
				  	 return thisObject.x(this.index);
			      return thisObject.x((thisObject.dataHistory[this.parent.index].length <= thisObject.band()) ? (thisObject.band() - thisObject.dataHistory[this.parent.index].length) + this.index + 1: this.index); 
			  })
        .bottom(0)//pv.Layout.stack())
        .height(function(d) { return thisObject.y(d); })
        .title(function() {
            var dataObj = thisObject.traverseToDataField(thisObject.data, thisObject.dataField());
            if( dataObj instanceof Array ) {
                return thisObject.onTooltip(dataObj[this.parent.index]);
            }
            else {
                return thisObject.onTooltip(dataObj);
            }
        })
        .event("click", function() {
            var dataObj = thisObject.traverseToDataField(thisObject.data, thisObject.dataField());
            if( dataObj instanceof Array ) {
                return thisObject.onClick(dataObj[this.parent.index]);
            }
            else {
                return thisObject.onClick(dataObj);
            }
        });

    var areaDot = area.anchor("top").add(pv.Dot).title(function(d) { return d; })
        .visible(function() { return thisObject.marks(); })
        .fillStyle("#fff")
        .size(10);
        //.add(pv.Label);

    /* Legend */
    panel.add(pv.Dot)
        .visible(function() { return thisObject.legend(); })
        .right(100)
        .fillStyle(function() { return area.fillStyle(); })
        .bottom(function() { return (this.parent.index * 15) + 10; })
        .size(20) 
        .lineWidth(1)
        .strokeStyle("#000")
      .anchor("right").add(pv.Label)
        .text(function() { return thisObject.getDataLabel(this.parent.index); });
     
    /* Vertical Grid Lines */
    panel.add(pv.Rule)
        .data(function() { return thisObject.x.ticks(); })
        //.visible(function(d) { return (d > 0); })
        .left(function(d) { return (Math.round(thisObject.x(d)) - 0.5); })
        //.strokeStyle("rgba(128,128,128,.1)")
      //.add(pv.Rule)
        .bottom(-2)
        .height(5)
        .strokeStyle("rgba(128,128,128,1)")
      .anchor("bottom").add(pv.Label)
        .textMargin(10)
        .text(function(d) { var n = new Number(((thisObject.dirFromLeft())? d : thisObject.band() - d) * thisObject.xInterval() / 1000); return n.toFixed() + thisObject.xSuffix(); })
        .font(function() { return thisObject.labelFont(); })
        .textStyle("rgb(0,0,0)");
    
    /* Horizontal Grid Lines */
    panel.add(pv.Rule)
        .data(function() { return thisObject.y.ticks(); })
        //.visible(function() { return !(this.parent.index % 2); })
        .bottom(function(d) { return (Math.round(thisObject.y(d)) - 0.5); })
        .strokeStyle("rgba(128,128,128,.2)")
        //.strokeStyle(function(d) { return (d==12) ? "green" : "rgba(128,128,128,.2)"; })
      .add(pv.Rule)
        .left(-5)
        .width(5)
        .strokeStyle("rgba(128,128,128,1)")
      .anchor("left").add(pv.Label)
        .textMargin(10)
        .text(function(d) {return d.toFixed() + thisObject.ySuffix(); })
        .font(function() { return thisObject.labelFont(); })
        .textStyle("rgb(0,0,0)");

    this.vis.add(pv.Label)
        .left(this.width() / 2)
        .visible(function() { return !(thisObject.title() === ""); })
        .top(16)
        .textAlign("center")
        .text(function() { return thisObject.title(); })
        .font(function() { return thisObject.titleFont(); });
};

/**
* @private
*/
wso2vis.s.chart.protovis.AreaChart.prototype.titleSpacing = function () {
    if(this.title() === "") {
        return 1;
    }
    else {
        return 0.9;
    }
};

/**
* @private 
*/
wso2vis.s.chart.protovis.AreaChart.prototype.populateData = function (thisObject) {
    var _dataField = thisObject.traverseToDataField(thisObject.data, thisObject.dataField());

    var dataGrpCount = 1;
    if( _dataField instanceof Array ) {
        dataGrpCount = _dataField.length;
    }

    thisObject.formattedData = pv.range(dataGrpCount).map(genDataMap);

    thisObject.x.domain(0, thisObject.band()).range(0,thisObject.width());
    var maxheight = calcMaxHeight();
    if (maxheight < 5) maxheight = 5; // fixing value repeating issue.
    thisObject.y.domain(0, maxheight).range(0, (thisObject.height() * thisObject.titleSpacing()) - 35);
    thisObject.y.nice();
    
    var maxLabelLength = (maxheight == 0) ? 1 : Math.floor(Math.log(maxheight)/Math.log(10)) + 1; //TODO: maxheight will never become 0. But we check it just to be in the safe side. useless?
    this.vis.left((maxLabelLength*9.5)+10);
    
    function genDataMap(x) {
        var rootObj;
        if( _dataField instanceof Array ) {
            rootObj = _dataField[x];
        }
        else {
            rootObj = _dataField;
        }
        var valObj = parseInt(thisObject.traverseToDataField(rootObj, thisObject.dataValue()));

        if (thisObject.dataHistory[x] === undefined){
            thisObject.dataHistory[x] = new Array();
        }
		if (thisObject.dirFromLeft()) {
			thisObject.dataHistory[x].unshift(valObj);

			if(thisObject.dataHistory[x].length > thisObject.band()+1){
				thisObject.dataHistory[x].pop();
			}
		}
		else {
			thisObject.dataHistory[x].push(valObj);

			if(thisObject.dataHistory[x].length > thisObject.band()+1){
				thisObject.dataHistory[x].shift();
			}
		}
        return thisObject.dataHistory[x];
    }

    function calcMaxHeight() {
        var totHeights = [];
        for (var k=0; k<thisObject.dataHistory.length; k++) {
            totHeights.push(thisObject.dataHistory[k].max());
        }
        return totHeights.max();
    }
};

wso2vis.s.chart.protovis.AreaChart.prototype.getData = function (thisObject) {
    return thisObject.formattedData;
};

wso2vis.s.chart.protovis.AreaChart.prototype.update = function () {
    this.populateData(this);
    this.vis.render();
    if(this.tooltip() === true) {
        tooltip.init();
    }
};

wso2vis.s.chart.protovis.AreaChart.prototype.getDataLabel = function (i) {
    if (this.data !== null){
        var rootObj = this.traverseToDataField(this.data, this.dataField());
        if( rootObj instanceof Array ) {
            return  this.traverseToDataField(rootObj[i], this.dataLabel());
        }
        else {
            return  this.traverseToDataField(rootObj, this.dataLabel());
        }
    }
    return i;
};

wso2vis.s.chart.protovis.AreaChart.prototype.clear = function () {
    this.dataHistory.length = 0;
};

