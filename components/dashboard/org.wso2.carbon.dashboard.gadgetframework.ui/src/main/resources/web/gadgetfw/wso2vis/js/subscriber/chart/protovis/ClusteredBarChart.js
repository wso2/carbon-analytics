//Class c.protovis.ClusteredBarChart : Chart
//This is the custom wrapper class for protovis bar charts

//Constructor
wso2vis.s.chart.protovis.ClusteredBarChart = function(canvas, chartTitle, chartDesc) {
    wso2vis.s.chart.Chart.call(this, canvas, chartTitle, chartDesc);

    this.ySuffix("")
        .xSuffix("");

    /* @private */
    this.vis = null;
    this.y = null;
    this.x = null;
    this.dataFieldCount = 1;
    this.subDataFieldCount = 1;
    this.maxDataValue = 50;
}

// this makes c.protovis.ClusteredBarChart.prototype inherit from Chart
wso2vis.extend(wso2vis.s.chart.protovis.ClusteredBarChart, wso2vis.s.chart.Chart);

wso2vis.s.chart.protovis.ClusteredBarChart.prototype
    .property("dataField")
    .property("dataLabel")
    .property("subDataField")
    .property("subDataValue")
    .property("subDataLabel")
    .property("ySuffix")
    .property("xSuffix");

//Public function load
//Loads the chart inside the given HTML element
wso2vis.s.chart.protovis.ClusteredBarChart.prototype.load = function (w, h) {
    if ( w !== undefined ) {
        this.width(w);
    }
    if ( h !== undefined ) {
        this.height(h);
    }

    var thisObject = this;

    this.x = pv.Scale.linear(0, this.maxDataValue).range(0, this.width());
    this.y = pv.Scale.ordinal(pv.range(this.dataFieldCount)).splitBanded(0, this.height(), 4/5);
 
    this.vis = new pv.Panel()
        .canvas(function() { return thisObject.divEl(); })
        .width(function() { return thisObject.width(); })
        .height(function() { return thisObject.height(); });

    var chart = this.vis.add(pv.Panel)
        .width(function() { return (thisObject.width() - thisObject.paddingLeft() - thisObject.paddingRight()); })
        .height(function() { return (thisObject.height() - thisObject.paddingTop() - thisObject.paddingBottom()); })
        .top(thisObject.paddingTop())
        .bottom(thisObject.paddingBottom())
        .left(thisObject.paddingLeft())
        .right(thisObject.paddingRight());
     
    var bar = chart.add(pv.Panel)
        .data(function() { return thisObject.getData(thisObject); })
        .top(function() { return thisObject.y(this.index); })
      .add(pv.Bar)
        .data(function(a) { return a; })
        .top(function() { return (this.index * thisObject.y.range().band / thisObject.subDataFieldCount); })
        .height(function() { return (thisObject.y.range().band / thisObject.subDataFieldCount); })
        .left(0)
        .width(thisObject.x)
        .fillStyle(pv.Colors.category20().by(pv.index))
        .title(function() {
            var dataObj = thisObject.traverseToDataField(thisObject.data, thisObject.dataField());
            if( dataObj instanceof Array ) {
                return thisObject.onTooltip(dataObj[this.parent.index], this.index);
            }
            else {
                return thisObject.onTooltip(dataObj, this.index);
            }
        })
        .event("click", function() {
            var dataObj = thisObject.traverseToDataField(thisObject.data, thisObject.dataField());
            if( dataObj instanceof Array ) {
                return thisObject.onClick(dataObj[this.parent.index], this.index);
            }
            else {
                return thisObject.onClick(dataObj, this.index);
            }
        });
    
    /* marks */
    bar.anchor("right").add(pv.Label)
        .visible(function() { return thisObject.marks(); })
        .textStyle("white")
        .text(function(d) { return d; });
     
    chart.add(pv.Label)
        .data(function() { return pv.range(thisObject.dataFieldCount); })
        .left(0)
        .top(function() { return (thisObject.y(this.index) + thisObject.y.range().band / 2); })
        .textMargin(5)
        .textAlign("right")
        .text(function() { return thisObject.getDataLabel(this.index); })
        .font(function() { return thisObject.labelFont(); })
        .textStyle("rgb(0,0,0)");
     
    chart.add(pv.Rule)
        .data(function() { return thisObject.x.ticks(); })
        .left(function(d) { return (Math.round(thisObject.x(d)) - 0.5); })
        .strokeStyle(function(d) { return (d ? "rgba(128,128,128,.3)" : "rgba(128,128,128,.8)"); })
      .add(pv.Rule)
        .bottom(0)
        .height(5)
        .strokeStyle("rgba(128,128,128,1)")
      .anchor("bottom").add(pv.Label)
        .text(function(d) { return d.toFixed(); })
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
wso2vis.s.chart.protovis.ClusteredBarChart.prototype.titleSpacing = function () {
    if(this.title() === "") {
        return 1;
    }
    else {
        return 0.9;
    }
};

wso2vis.s.chart.protovis.ClusteredBarChart.prototype.populateData = function (thisObject) {

    var tmpMaxValHolder = [];
    var _dataField = thisObject.traverseToDataField(thisObject.data, thisObject.dataField());

    var dataGrpCount = 1;
    if( _dataField instanceof Array ) {
        dataGrpCount = _dataField.length;
    }

    thisObject.subDataFieldCount = 1;
    thisObject.formattedData = pv.range(dataGrpCount).map( genDataMap );
    thisObject.dataFieldCount = dataGrpCount;
    thisObject.maxDataValue = tmpMaxValHolder.max() + 5; //to keep bars inside the ruler
    if (thisObject.maxDataValue < 5) thisObject.maxDataValue = 5; // fixing value repeating issue.

    thisObject.x.domain(0, thisObject.maxDataValue).range(0, (thisObject.width() - thisObject.paddingLeft() - thisObject.paddingRight()));
    thisObject.y.domain(pv.range(thisObject.dataFieldCount)).splitBanded(0, (thisObject.height() - thisObject.paddingTop() - thisObject.paddingBottom()), 4/5);

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
            thisObject.subDataFieldCount = (thisObject.subDataFieldCount < _subDataField.length) ? _subDataField.length : thisObject.subDataFieldCount;
        }

        for(var y=0; y<subDataGrpCount; y++) {
            var rootObjb;
            if( _subDataField instanceof Array ) {
                rootObjb = _subDataField[y];
            }
            else {
                rootObjb = _subDataField;
            }
            var temp = parseInt(thisObject.traverseToDataField(rootObjb, thisObject.subDataValue()));
            innerArray.push(temp);
        }
        tmpMaxValHolder.push(innerArray.max());
        return innerArray;
    }
};

wso2vis.s.chart.protovis.ClusteredBarChart.prototype.getData = function (thisObject) {

    return thisObject.formattedData;
};

wso2vis.s.chart.protovis.ClusteredBarChart.prototype.update = function () {

    this.populateData(this);
    this.vis.render();
    if(this.tooltip() === true) {
        tooltip.init();
    }
};

wso2vis.s.chart.protovis.ClusteredBarChart.prototype.getDataLabel = function (i) {
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

wso2vis.s.chart.protovis.ClusteredBarChart.prototype.getSubDataLable = function (i, j) {
    if (this.data !== null){
        var rootDataObj;
        var _dataField = this.traverseToDataField(this.data, this.dataField());

        if( _dataField instanceof Array ) {
            rootDataObj = _dataField[i];
        }
        else {
            rootDataObj = _dataField;
        }

        var rootSubDataObj = this.traverseToDataField(rootDataObj, this.subDataField());

        if( rootSubDataObj instanceof Array ) {
            return this.traverseToDataField(rootSubDataObj[j], this.subDataLabel());
        }
        else {
            return this.traverseToDataField(rootSubDataObj, this.subDataLabel());
        }
    }
    return j;
};

