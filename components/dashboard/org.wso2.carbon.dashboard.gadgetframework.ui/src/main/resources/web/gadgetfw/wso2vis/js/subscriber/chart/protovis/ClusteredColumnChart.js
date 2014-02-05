//Class c.protovis.ClusteredColumnChart : Chart
//This is the custom wrapper class for protovis bar charts

//Constructor
wso2vis.s.chart.protovis.ClusteredColumnChart = function(canvas, chartTitle, chartDesc) {
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

// this makes c.protovis.ClusteredColumnChart.prototype inherit from Chart
wso2vis.extend(wso2vis.s.chart.protovis.ClusteredColumnChart, wso2vis.s.chart.Chart);

wso2vis.s.chart.protovis.ClusteredColumnChart.prototype
    .property("dataField")
    .property("dataLabel")
    .property("subDataField")
    .property("subDataValue")
    .property("subDataLabel")
    .property("ySuffix")
    .property("xSuffix")
    .property("dataLabelLength")
    .property("dataLabelAngle");

//Public function load
//Loads the chart inside the given HTML element
wso2vis.s.chart.protovis.ClusteredColumnChart.prototype.load = function (w, h) {
    if ( w !== undefined ) {
        this.width(w);
    }
    if ( h !== undefined ) {
        this.height(h);
    }

    var thisObject = this;

    this.y = pv.Scale.linear(0, this.maxDataValue).range(0, this.height());
    this.x = pv.Scale.ordinal(pv.range(this.dataFieldCount)).splitBanded(0, this.width(), 4/5);

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
        .left(function() { return thisObject.x(this.index); })
      .add(pv.Bar)
        .data(function(a) { return a; })
        .left(function() { return (this.index * thisObject.x.range().band / thisObject.subDataFieldCount); })
        .width(function() { return (thisObject.x.range().band / thisObject.subDataFieldCount); })
        .bottom(0)
        .height(thisObject.y)
        .fillStyle(pv.Colors.category20().by(pv.index))
        .title(function() {
            var dataObj = thisObject.traverseToDataField(thisObject.data, thisObject.dataField());
            if( dataObj instanceof Array ) {
                return thisObject.onTooltip(dataObj[this.parent.index], this.index);
            }
            else {
                return thisObject.onTooltip(dataObj);
            }
        })
        .event("click", function() {
            var dataObj = thisObject.traverseToDataField(thisObject.data, thisObject.dataField());
            if( dataObj instanceof Array ) {
                return thisObject.onClick(dataObj[this.parent.index], this.index);
            }
            else {
                return thisObject.onClick(dataObj);
            }
        });

    bar.anchor("top").add(pv.Label)
        .visible(function() { return thisObject.marks(); })
        .textStyle("white")
        .text(function(d) { return d; });

    chart.add(pv.Label)
        .events("all")
        .data(function() { return pv.range(thisObject.dataFieldCount); })
        .bottom(0)
        .left(function() { return thisObject.x(this.index); }) //TODO fix the alignment issue (+ thisObject.x.range().band / 2)
        .textMargin(5)
        .textBaseline("top")
        .text(function() {
            if(!thisObject.dataLabelLength()) return thisObject.getDataLabel(this.index);
            var length = thisObject.getDataLabel(this.index).substr(0, thisObject.dataLabelLength() instanceof Function ?
                            thisObject.dataLabelLength().call() : thisObject.dataLabelLength());
            return length ? length : thisObject.getDataLabel(this.index);
        })
        .title(function() {
            if(!thisObject.dataLabelLength()) return null;
            var length = thisObject.getDataLabel(this.index).substr(0, thisObject.dataLabelLength() instanceof Function ?
                            thisObject.dataLabelLength().call() : thisObject.dataLabelLength());
            return (length && length < thisObject.getDataLabel(this.index).length) ? thisObject.getDataLabel(this.index) : null;
        })
        .textAngle(thisObject.dataLabelAngle() ? thisObject.dataLabelAngle() : 0)
        .font(function() { return thisObject.labelFont(); });

    chart.add(pv.Rule)
        .data(function() { return thisObject.y.ticks(); })
        .bottom(function(d) { return (Math.round(thisObject.y(d)) - 0.5); })
        .strokeStyle(function(d) { return (d ? "rgba(128,128,128,.3)" : "#000"); })
      .add(pv.Rule)
        .left(0)
        .width(5)
        .strokeStyle("rgba(128,128,128,1)")
      .anchor("left").add(pv.Label)
        .text(function(d) { return d.toFixed(); })
        .font(function() { return thisObject.labelFont(); });

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
wso2vis.s.chart.protovis.ClusteredColumnChart.prototype.titleSpacing = function () {
    if(this.title() === "") {
        return 1;
    }
    else {
        return 0.9;
    }
};

wso2vis.s.chart.protovis.ClusteredColumnChart.prototype.populateData = function (thisObject) {

    var tmpMaxValHolder = [];
    var _dataField = thisObject.traverseToDataField(thisObject.data, thisObject.dataField());

    var dataGrpCount = 1;
    if( _dataField instanceof Array ) {
        dataGrpCount = _dataField.length;
    }

    thisObject.subDataFieldCount = 1;
    thisObject.formattedData = pv.range(dataGrpCount).map( genDataMap );
    thisObject.dataFieldCount = dataGrpCount;
    thisObject.maxDataValue = tmpMaxValHolder.max();
    if (thisObject.maxDataValue < 8) thisObject.maxDataValue = 8; // fixing value repeating issue.

    thisObject.y.domain(0, thisObject.maxDataValue).range(0, (thisObject.height() - thisObject.paddingTop() - thisObject.paddingBottom()));
    thisObject.x.domain(pv.range(thisObject.dataFieldCount)).splitBanded(0, (thisObject.width() - thisObject.paddingLeft() - thisObject.paddingRight()), 4/5);

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

wso2vis.s.chart.protovis.ClusteredColumnChart.prototype.getData = function (thisObject) {

    return thisObject.formattedData;
};

wso2vis.s.chart.protovis.ClusteredColumnChart.prototype.update = function () {

    this.populateData(this);
    this.vis.render();
    if(this.tooltip() === true) {
        tooltip.init();
    }
};

wso2vis.s.chart.protovis.ClusteredColumnChart.prototype.getDataLabel = function (i) {
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

wso2vis.s.chart.protovis.ClusteredColumnChart.prototype.getSubDataLable = function (i, j) {
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

