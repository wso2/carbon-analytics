//Class AreaChart2 : Chart
//This is the custom wrapper class for protovis area/line charts

//Constructor
wso2vis.s.chart.protovis.AreaChart2 = function(div, chartTitle, chartDesc) {
    wso2vis.s.chart.Chart.call(this, div, chartTitle, chartDesc);

    this.band(12)
        .xSuffix("")
        .ySuffix("");

    /* @private */
    this.vis = null;
    this.x = null;
    this.y = null;
    this.customXticks = null;
};

// this makes AreaChart2.prototype inherit from Chart
wso2vis.extend(wso2vis.s.chart.protovis.AreaChart2, wso2vis.s.chart.Chart);

wso2vis.s.chart.protovis.AreaChart2.prototype
    .property("dataField")
    .property("subDataField")
    .property("xDataValue")
    .property("yDataValue")
    .property("dataLabel")
    .property("xSuffix")
    .property("ySuffix")
    .property("xLabel")
    .property("band");

//Public function load
//Loads the chart inside the given HTML element
wso2vis.s.chart.protovis.AreaChart2.prototype.load = function (w, h) {
    if ( w !== undefined ) {
        this.width(w);
    }
    if ( h !== undefined ) {
        this.height(h);
    }

    var thisObject = this;

    this.x = pv.Scale.linear(0, 4).range(0, this.width());
    this.y = pv.Scale.linear(0, 50).range(0, this.height()*0.9);
    this.customXticks = [];
 
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
        .height(function() { return (thisObject.height() * thisObject.titleSpacing()); })
        .strokeStyle("#ccc");

    var area = panel.add(pv.Area)
        .data(function(a) { return a; })
        .left(function(d) { return thisObject.x(d.x); })
        .bottom(0)//pv.Layout.stack())
        .height(function(d) { return thisObject.y(d.y); })
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

    var areaDot = area.anchor("top").add(pv.Dot).title(function(d) { return d.y; })
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
     
    panel.add(pv.Rule)
        .data(function() { return thisObject.customXticks; /*thisObject.x.ticks();*/ })
        .visible(function(d) { return (d > 0); })
        .left(function(d) { return (Math.round(thisObject.x(d)) - 0.5); })
        .strokeStyle("rgba(128,128,128,.1)")
      .anchor("bottom").add(pv.Label)
        .text(function(d) { return d.toFixed() + thisObject.xSuffix(); })
        .font(function() { return thisObject.labelFont(); })
        .textStyle("rgba(128,128,128,0.5)");
     
    panel.add(pv.Rule)
        .data(function() { return thisObject.y.ticks(); })
        .visible(function() { return !(this.parent.index % 2); })
        .bottom(function(d) { return (Math.round(thisObject.y(d)) - 0.5); })
        .strokeStyle("rgba(128,128,128,.2)")
      .anchor("left").add(pv.Label)
        .text(function(d) {return d.toFixed() + thisObject.ySuffix(); })
        .font(function() { return thisObject.labelFont(); })
        .textStyle("rgba(128,128,128,0.5)");

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
wso2vis.s.chart.protovis.AreaChart2.prototype.titleSpacing = function () {
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
wso2vis.s.chart.protovis.AreaChart2.prototype.populateData = function (thisObject) {
    var _dataField = thisObject.traverseToDataField(thisObject.data, thisObject.dataField());
    var tempDataArray = [];

    var dataGrpCount = 1;
    if( _dataField instanceof Array ) {
        dataGrpCount = _dataField.length;
    }

    thisObject.formattedData = pv.range(dataGrpCount).map(genDataMap);
    var xMax = 0;
    var xMin = Infinity;

    for (var x=0; x<thisObject.formattedData.length; x++) {
        var dataSet = thisObject.formattedData[x];
        for (var y=0; y<dataSet.length; y++) {
            xMax = (xMax < dataSet[y].x) ? dataSet[y].x : xMax;
            xMin = (xMin > dataSet[y].x) ? dataSet[y].x : xMin;
            //TODO clean up this. we need only one data set
            thisObject.customXticks[y] = dataSet[y].x;
        }
    }

    var maxheight = tempDataArray.max();
    if (maxheight < 5) maxheight = 5; // fixing value repeating issue.
    if (thisObject.xDataValue() === undefined) {
        thisObject.x.domain(0, thisObject.band()).range(0, this.width());
    }
    else {
        //thisObject.x.domain(thisObject.formattedData[0], function(d) { return d.x; }).range(0, this.width());
        thisObject.x.domain(xMin, xMax).range(0, this.width());
    }
    thisObject.y.domain(0, maxheight).range(0, (thisObject.height() * thisObject.titleSpacing()) - 35);
    thisObject.y.nice();
    
    function genDataMap(x) {
        var innerArray = [];
        var rootObja;
        if( _dataField instanceof Array ) {
            rootObja = _dataField[x];
        }
        else {
            rootObja = _dataField;
        }

        var _subDataField = thisObject.traverseToDataField(rootObja, thisObject.subDataField());

        var subDataGrpCount = 1;
        if( _subDataField instanceof Array ) {
            subDataGrpCount = _subDataField.length;
        }

        for(var y=0; y<subDataGrpCount; y++) {
            var rootObjb;
            if( _subDataField instanceof Array ) {
                rootObjb = _subDataField[y];
            }
            else {
                rootObjb = _subDataField;
            }

            var valObjY = parseInt(thisObject.traverseToDataField(rootObjb, thisObject.yDataValue()));
            tempDataArray.push(valObjY);

            if (thisObject.xDataValue() === undefined) {
                innerArray.push(valObjY);
            }
            else {
                var valObjX = parseInt(thisObject.traverseToDataField(rootObjb, thisObject.xDataValue()));
                innerArray.push({ x: valObjX, y: valObjY });
            }
        }
        return innerArray;
    }
};

wso2vis.s.chart.protovis.AreaChart2.prototype.getData = function (thisObject) {
    return thisObject.formattedData;
};

wso2vis.s.chart.protovis.AreaChart2.prototype.update = function () {
    this.populateData(this);
    this.vis.render();
    if(this.tooltip() === true) {
        tooltip.init();
    }
};

wso2vis.s.chart.protovis.AreaChart2.prototype.getDataLabel = function (i) {
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

