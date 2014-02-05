/**
* Constructs a new CallbackFilter.
* @class Represents a filter which uses a callback function to filter data.
* @augments wso2vis.f.Filter
* @param {callback} callback callback function to filter data.
* @constructor
*/
wso2vis.f.CallbackFilter = function(callback) {
	wso2vis.f.Filter.call(this);
    this.callback(callback);
};

wso2vis.extend(wso2vis.f.CallbackFilter, wso2vis.f.Filter);

/* Define all properties. */
wso2vis.f.CallbackFilter.prototype
    .property("callback");

/**
* @private Filters data from the data object.
* @param {data} data the data object.
*/
wso2vis.f.CallbackFilter.prototype.filterData = function(data) {	
    return this.callback().call(this, data);
};

