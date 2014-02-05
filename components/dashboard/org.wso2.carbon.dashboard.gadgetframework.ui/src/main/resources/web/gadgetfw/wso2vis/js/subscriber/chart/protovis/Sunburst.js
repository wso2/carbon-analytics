//@class wso2vis.s.chart.protovis.Sunburst : wso2vis.s.chart.Chart

//Constructor
wso2vis.s.chart.protovis.Sunburst = function(canvas, chartTitle, chartDesc) {
    wso2vis.s.chart.Chart.call(this, canvas, chartTitle, chartDesc);

    this.labelLength(12)
        .thickness(30);

    /* @private */
    this.vis = null;
    this.sunburst = null;
    this.wedge = null;

    this.flare = {
      analytics: {
        cluster: {
          AgglomerativeCluster: 3938,
          CommunityStructure: 3812,
          HierarchicalCluster: 6714,
          MergeEdge: 743
        },
        graph: {
          BetweennessCentrality: 3534,
          LinkDistance: 5731,
          MaxFlowMinCut: 7840,
          ShortestPaths: 5914,
          SpanningTree: 3416
        },
        optimization: {
          AspectRatioBanker: 7074
        }
      }
    };
}

// this makes c.protovis.Sunburst.prototype inherits from wso2vis.s.chart.Chart
wso2vis.extend(wso2vis.s.chart.protovis.Sunburst, wso2vis.s.chart.Chart);

wso2vis.s.chart.protovis.Sunburst.prototype
    .property("dataField")
    .property("dataValue")
    .property("dataLabel")
    .property("labelLength")
    .property("thickness");

//Public function load
//Loads the chart inside the given HTML element
wso2vis.s.chart.protovis.Sunburst.prototype.load = function (w) {
    if ( w !== undefined ) {
        this.width(w);
    }
    /*if ( h !== undefined ) { //not using height for the Wedge
        this.height(h);
    }*/
    var r = this.width() / 2.5;

    var thisObject = this;

    //this.sunburst = pv.Layout.sunburst(this.data).size(Number);
    
    this.vis = new pv.Panel()
        //.def("i", -1)
        .canvas(function() { return thisObject.divEl(); })
        .width(function() { return thisObject.width(); })
        .height(function() { return thisObject.height(); });
    
    this.wedge = this.vis.add(pv.Wedge)
        .extend(pv.Layout.sunburst(this.formattedData).size(function(n) { return parseInt(n); }))
        .fillStyle(pv.Colors.category10()
              .by(function(n) { return n.children ? n.keys : n.keys.slice(0, -1); }))
        .strokeStyle("#222")
          .lineWidth(1)
          //.visible(function(n) { return n.depth < 3; })
          .title(function(n) { return /*thisObject.traverseToDataField(thisObject.data, n.keys)*/ n.keys.join(".") + ": " + n.size; });
        //.anchor("center").add(pv.Label)
          //.visible(function(n) { return n.angle * n.depth > .05; })
          //.text(function(n) { return n.keys[n.keys.length - 1]; });

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
wso2vis.s.chart.protovis.Sunburst.prototype.titleSpacing = function () {
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
wso2vis.s.chart.protovis.Sunburst.prototype.populateData = function (thisObject) {

    rec(this.data);
    //console.log(this.data);
    this.formattedData = this.data;

    function rec(d) {
        for( i in d ) {
            if( typeof(d[i]) == "string" && isNaN(d[i]) ) {
                d[i] = 0;
            }
            rec(d[i]);
        }
    }
};

wso2vis.s.chart.protovis.Sunburst.prototype.getData = function (thisObject) {

    return thisObject.formattedData;
};

wso2vis.s.chart.protovis.Sunburst.prototype.update = function () {

    this.populateData(this);
    this.wedge.extend(pv.Layout.sunburst(this.formattedData).size(function(n) { return parseInt(n); })); 
    //this.sunburst.size(Number);//console.log(JSON.stringify(this.data));
    this.vis.render();
    if(this.tooltip() === true) {
        tooltip.init();
    }
};

wso2vis.s.chart.protovis.Sunburst.prototype.getDataLable = function (i) {

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

