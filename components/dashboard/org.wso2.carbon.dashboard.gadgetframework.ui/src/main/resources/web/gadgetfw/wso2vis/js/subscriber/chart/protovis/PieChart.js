//@class wso2vis.s.chart.protovis.PieChart : wso2vis.s.chart.WedgeChart

//Constructor
wso2vis.s.chart.protovis.PieChart = function(canvas, chartTitle, chartDesc) {
    wso2vis.s.chart.protovis.WedgeChart.call(this, canvas, chartTitle, chartDesc);
}

// this makes c.protovis.PieChart.prototype inherits from wso2vis.s.chart.WedgeChart
wso2vis.extend(wso2vis.s.chart.protovis.PieChart, wso2vis.s.chart.protovis.WedgeChart);

//Public function load
//Loads the chart inside the given HTML element
wso2vis.s.chart.protovis.PieChart.prototype.load = function (w) {
    if ( w !== undefined ) {
        this.width(w);
    }
    /*if ( h !== undefined ) { //not using height for the Wedge
        this.height(h);
    }*/
    //var r = this.width() / 2.5;

    var thisObject = this;
    
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
    
    var wedge = chart.add(pv.Wedge)
        .data(function() { return pv.normalize(thisObject.getData(thisObject)); })
        .left((thisObject.width() - thisObject.paddingLeft() - thisObject.paddingRight()) / 2)
        .bottom((thisObject.height() - thisObject.paddingTop() - thisObject.paddingBottom()) / 2)
        .innerRadius(0)
        .outerRadius(function() { return (thisObject.width() - thisObject.paddingLeft() - thisObject.paddingRight()) / 2.5; })
        .angle(function(d) { return (d * 2 * Math.PI); })
        .title(function() { 
            var dataObj = thisObject.traverseToDataField(thisObject.data, thisObject.dataField());
            if( dataObj instanceof Array ) {
                return thisObject.onTooltip(dataObj[this.index]);
            }
            else {
                return thisObject.onTooltip(dataObj);
            }
        })
        .event("click", function() { 
            var dataObj = thisObject.traverseToDataField(thisObject.data, thisObject.dataField());
            if( dataObj instanceof Array ) {
                return thisObject.onClick(dataObj[this.index]);
            }
            else {
                return thisObject.onClick(dataObj);
            }
        });

    wedge.anchor("center").add(pv.Label)
         .visible(function(d) { return (thisObject.marks() && (d > 0.10)); })
         .textAngle(0)
         .text(function(d) { return (d*100).toFixed() + "%"; })
         .textStyle("#fff");

	//wedge.anchor("end").add(pv.Label)
		//.visible(function(d) { return (d > 0.05); })
		//.textMargin(0) //function(){ return thisObject.thickness() + 5; }
		//.text(function(d) { var lbl=thisObject.getDataLabel(this.index); return (lbl.length > thisObject.labelLength() ? lbl.substring(0,thisObject.labelLength())+"..." : lbl); })
		//.font(function() { return thisObject.labelFont(); });
//		.textStyle(function() { return wedge.fillStyle(); });

    /* Legend */
/*    var legend = chart.add(pv.Panel)
        .top(function() { return thisObject.legendTop(); })
        .left(function() { return thisObject.legendLeft(); })
        .right(function() { return thisObject.legendRight(); })
        .bottom(function() { return thisObject.legendBottom(); })*/
        

    chart.add(pv.Dot)
        .data(function() { return pv.normalize(thisObject.getData(thisObject)); })
        .visible(function() { return thisObject.legend(); })
        .fillStyle(function() { return wedge.fillStyle(); })
        .right(function() { return (thisObject.width() - thisObject.legendX()); })
        .bottom(function() { return (this.index * 15) + (thisObject.height() - thisObject.legendY()); })
        .size(20) 
        .lineWidth(1)
        .strokeStyle("#000")
      .anchor("right").add(pv.Label)
        .text(function() { return thisObject.getDataLabel(this.index); });

     this.vis.add(pv.Label)
        .left(this.width() / 2)
        .visible(function() { return !(thisObject.title() === ""); })
        .top(16)
        .textAlign("center")
        .text(function() { return thisObject.title(); })
        .font(function() { return thisObject.titleFont(); });
};

