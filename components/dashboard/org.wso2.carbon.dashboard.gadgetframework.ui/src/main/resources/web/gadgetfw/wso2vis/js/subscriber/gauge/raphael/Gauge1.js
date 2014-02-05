//Class c.raphael.Gauge1 : Gauge1
//This is the custom wrapper class for protovis bar charts

//Constructor
wso2vis.s.gauge.raphael.Gauge1 = function(canvas, chartTitle, chartDesc) {
    wso2vis.s.gauge.Gauge.call(this, canvas, chartTitle, chartDesc);

    /* @private */
    this.y = null;
    this.x = null;

	this.r = null; // raphael page
	this.s = null; // needle set
	this.cx = 0;
	this.cy = 0;
	
	this.minValue(0);
	this.maxValue(1000);
	this.largeTick(100);
	this.smallTick(10);
	this.minAngle(30);
	this.maxAngle(330);
	this.radius(60);
	
	this.needleLength(55);
	this.smallTickLength(10);
	this.largeTickLength(15);
    //this.legendText("Data 1");
}

// this makes c.protovis.BarChart.prototype inherits
// from Chart.prototype
wso2vis.extend(wso2vis.s.gauge.raphael.Gauge1, wso2vis.s.gauge.Gauge);

wso2vis.s.gauge.raphael.Gauge1.prototype
    .property("dataField")
    .property("dataValue")
    .property("dataLabel")
    .property("ySuffix")
    .property("xSuffix")
    .property("titleTop")
    .property("titleLeft")
    .property("titleRight")
    .property("titleBottom")
    .property("xTitle")
    .property("yTitle")
    .property("legendText")
    .property("segmentBorderColor")
	
	.property("minAngle")
	.property("maxAngle")
	.property("radius")
	
	.property("minValue")
	.property("maxValue")
	.property("largeTick")
	.property("smallTick")
	.property("largeTickLength")
	.property("smallTickLength")
	.property("needleLength")
	
	.property("needleColor")
	.property("needleBaseColor")
	.property("labelColor")
	.property("largeTickColor")
	.property("smallTickColor");
	
	

//Public function load
//Loads the chart inside the given HTML element
wso2vis.s.gauge.raphael.Gauge1.prototype.load = function (w, h) {
    if ( w !== undefined ) {
        this.width(w);
    }
    if ( h !== undefined ) {
        this.height(h);
    } 
	
	this.cx = this.width() / 2;
	this.cy = this.height() / 2;
	
	this.r = Raphael(this.divEl(), this.width(), this.height());
	
	this.drawDial(this.r, this.largeTick(), this.radius(), this.largeTickLength(), this.cx, this.cy, true);
	this.drawDial(this.r, this.smallTick(), this.radius(), this.smallTickLength(), this.cx, this.cy, false);
	
	//drawBorder(this.r, this.radius(), this.cx, this.cy);
	
	this.drawInitialNeedle(this.r, this.radius(), this.cx, this.cy);
	this.showTitle();
};


/**
* @private
*/
wso2vis.s.gauge.raphael.Gauge1.prototype.showTitle = function() {
	this.r.text(this.cx, this.cy + this.radius() + 20, this.title()).attr({"stroke-width": 1, stroke: "#ccc"});
}

/**
* @private
*/
wso2vis.s.gauge.raphael.Gauge1.prototype.drawDial = function(r, tick, radius, length, cx, cy, isLargeTick) {
	var maxValAlt = Math.floor(this.maxValue() / tick) * tick;
	var minValAlt = Math.ceil(this.minValue() / tick) * tick;
	
	var n = Math.floor((maxValAlt - minValAlt) / tick);
	
	var tickAngle = tick * (this.maxAngle() - this.minAngle()) / (this.maxValue() - this.minValue());
	var startAngle = 0;
	if (this.minValue() >= 0) 
	{
		startAngle = ((this.minValue() % tick) == 0)? 0 : (tick - this.minValue() % tick) * (this.maxAngle() - this.minAngle()) / (this.maxValue() - this.minValue());
	}
	else 
	{
		startAngle = (-this.minValue() % tick) * (this.maxAngle() - this.minAngle()) / (this.maxValue() - this.minValue());
	}
	for (var i = 0; i <= n; i++) {
		var ang = (this.minAngle() + startAngle + i * tickAngle);
		r.path("M" + cx + " " + (cy + radius) + "L" + cx+ " " + (cy + radius - length)).attr({rotation: ang + " " + cx + " " + cy, "stroke-width": isLargeTick? 2 : 1, stroke: "#fff"});		
		if (isLargeTick) 
		{
			/*if (minValAlt + i * tick == 0)
				r.text(cx, cy + radius + 10, "0").attr({rotation:ang + " " + cx + " " + cy, "stroke-width": 1, stroke: "#fff"});						
			else 
				r.text(cx, cy + radius + 10, minValAlt + i * tick).attr({rotation:ang + " " + cx + " " + cy, "stroke-width": 1, stroke: "#fff"});						
			*/
			if (ang >= 90 && ang <= 270) {
				if (minValAlt + i * tick == 0)
					r.text(cx, cy - radius - 10, "0").attr({rotation:(ang-180) + " " + cx + " " + cy, "stroke-width": 1, stroke: "#fff"});						
				else 
					r.text(cx, cy - radius - 10, minValAlt + i * tick).attr({rotation:(ang-180) + " " + cx + " " + cy, "stroke-width": 1, stroke: "#fff"});						
			}
			else {
				if (minValAlt + i * tick == 0)
					r.text(cx, cy + radius + 10, "0").attr({rotation:ang + " " + cx + " " + cy, "stroke-width": 1, stroke: "#fff"});						
				else 
					r.text(cx, cy + radius + 10, minValAlt + i * tick).attr({rotation:ang + " " + cx + " " + cy, "stroke-width": 1, stroke: "#fff"});						
			}
		
		}
		
	}
}

/**
* @private
*/
wso2vis.s.gauge.raphael.Gauge1.prototype.drawBorder = function(r, radius, cx, cy) {
	var alpha = (360 - (this.maxAngle() - this.minAngle()));
	var alphar = alpha * Math.PI / 180;
	var tx, ty;
	tx = cx + radius * Math.sin(alphar);
	ty = cy + radius * Math.cos(alphar);
	r.path("M320 " + (cy + radius) + " A " + radius + " " + radius + " 0 1 1 "+ tx + " " + ty).attr({rotation: this.minAngle(), "stroke-width": 1, stroke: "#fff"});
} 

/**
* @private
*/
wso2vis.s.gauge.raphael.Gauge1.prototype.drawInitialNeedle = function(r, radius, cx, cy) {
	this.s = r.set();                
	this.s.push(r.path("M" + cx + " " + (cy - 15) + " L" + cx + " " + (cy + this.needleLength())).attr({fill: "none", "stroke-width": 4, stroke: "#f00"}));
	this.s.push(r.circle(cx, cy, 5).attr({fill: "none", "stroke-width": 10, stroke: "#aaa"}));
	this.s.animate({rotation:this.minAngle() + " " + cx + " " + cy}, 0, "<>");
}

/**
* @private
*/
wso2vis.s.gauge.raphael.Gauge1.prototype.updateNeedle = function(val) {
	var angle = (val - this.minValue()) * (this.maxAngle() - this.minAngle()) / (this.maxValue() - this.minValue()) + this.minAngle();
	this.s.animate({rotation:angle + " " + this.cx + " " + this.cy}, 800, "<>");
}

/**
* @private
*/
wso2vis.s.gauge.raphael.Gauge1.prototype.titleSpacing = function () {
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
wso2vis.s.gauge.raphael.Gauge1.prototype.populateData = function (thisObject) {
    var _dataField = thisObject.traverseToDataField(thisObject.data, thisObject.dataField());

    var dataGrpCount = 1;
    if( _dataField instanceof Array ) {
        dataGrpCount = _dataField.length;
    }
	
	var rootObj;
	if (_dataField instanceof Array ) {
		rootObj = _dataField[0];
	}
	else {
		rootObj = _dataField;
	}
	
	this.updateNeedle(parseInt(thisObject.traverseToDataField(rootObj, this.dataValue())));

    /*thisObject.formattedData = pv.range(dataGrpCount).map( genDataMap );

    
    var maxVal = thisObject.formattedData.max();
    if (maxVal < 5) maxVal = 5; // fixing value repeating issue.

    thisObject.x.domain(0, maxVal).range(0, (thisObject.width() - thisObject.paddingLeft() - thisObject.paddingRight()) );
    thisObject.y.domain(pv.range(dataGrpCount)).splitBanded(0, (thisObject.height() - thisObject.paddingTop() - thisObject.paddingBottom()), 4/5);

    function genDataMap(x) {
        var rootObj;
        if( _dataField instanceof Array ) {
            rootObj = _dataField[x];
        }
        else {
            rootObj = _dataField;
        }
        return parseInt(thisObject.traverseToDataField(rootObj, thisObject.dataValue()));
    }*/
};

//wso2vis.s.gauge.raphael.Gauge1.prototype.getData = function (thisObject) {
    //return thisObject.formattedData;
//};

wso2vis.s.gauge.raphael.Gauge1.prototype.update = function () {
    this.populateData(this);
    //this.vis.render();
    if(this.tooltip() === true) {
        tooltip.init();
    }
};

wso2vis.s.gauge.raphael.Gauge1.prototype.getDataLabel = function (i) {
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
