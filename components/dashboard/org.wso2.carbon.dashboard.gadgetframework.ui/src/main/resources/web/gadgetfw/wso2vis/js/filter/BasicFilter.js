/**
* Constructs a new BasicFilter.
* @class Represents a basic filter.
* @augments wso2vis.f.Filter
* @param {dataField} dataField the data field string.
* @param {dataLabel} dataLabel the data label string.
* @param {filterArray} filterArray the filter array.
* @constructor
*/
wso2vis.f.BasicFilter = function(dataField, dataLabel, filterArray) {
	wso2vis.f.Filter.call(this);

    this.dataField(dataField)
        .dataLabel(dataLabel)
	    .filterArray(filterArray);

    /* @private */
    this.remainingArray = [];
};

wso2vis.extend(wso2vis.f.BasicFilter, wso2vis.f.Filter);

/* Define all properties. */
wso2vis.f.BasicFilter.prototype
    .property("dataField")
    .property("dataLabel")
    .property("filterArray");

/**
* @private Filters data from the data object.
* @param {data} data the data object.
*/
wso2vis.f.BasicFilter.prototype.filterData = function(data) {	
    function getLbl(obj, dataLabel, x, that) {
        var r;
        if (obj instanceof Array) {
            r = obj[x];
        }
        else {
            r = obj;
        }
        return that.traverseToDataField(r, dataLabel);
    }
	    
	function filter(object, dataLabel, filterArray, that) {	    
	    var dcount = 1;
	    if (object instanceof Array)
	        dcount = object.length;
	    
	    if ((filterArray === undefined) || (filterArray == null)) {
	        var arr = [];
	         
	        for (var i = dcount - 1; i >= 0; i--) {
				arr.push(getLbl(object, dataLabel, i, that));
			}
			return {rem:[], fil:arr, isObj:false};					    
	    }
	    else {
	        remainingArray = [];
			var isObj = false;
		    for (var i = dcount - 1; i >= 0; i--) {
			    var found = false;
			    var label = getLbl(object, dataLabel, i, that);
			    for (var j = 0; j < filterArray.length; j++) {
				    if (label == filterArray[j]) {
					    found  = true;
					    break;
				    }							
			    }
			    if (!found) {				
				    remainingArray.push(label);
				    if (object instanceof Array)
				        object.splice(i, 1); // not found! remove from the object				
				    else {
				        isObj = true;
				    }
			    }			
		    }
		    return {rem:remainingArray, fil:filterArray, isObj:isObj};
	    }
	}
	
	function sortthem(object, dataLabel, filterArray, that) {
	    if ((filterArray === undefined) || (filterArray == null)) {
	        return;
	    }
	    var dcount = 1;
	    if (object instanceof Array)
	        dcount = object.length;
	        
		var index = 0;
		for (var i = 0; i < filterArray.length; i++) {
			for (var j = 0; j < dcount; j++) {
			    var label = getLbl(object, dataLabel, j, that);
				if (label == filterArray[i]) {
					if (index != j) {
						var temp = object[index];
						object[index] = object[j];
						object[j] = temp;
					}
					index++;
					break;
				}														
			}
		}
	}

    
	var cloned = JSON.parse(JSON.stringify(data)); //eval (data.toSource());				
	var filtered = wso2vis.fn.traverseToDataField(cloned, this.dataField());
	var result = filter(filtered, this.dataLabel(), this.filterArray(), this);
	this.remainingArray = result.rem;
	this.filterArray(result.fil);
	if (result.isObj) {
		wso2vis.fn.traverseNKillLeaf(cloned, this.dataField());
	}
	else {
		sortthem(filtered, this.dataLabel(), this.filterArray(), this);
	}

	return cloned;
};
