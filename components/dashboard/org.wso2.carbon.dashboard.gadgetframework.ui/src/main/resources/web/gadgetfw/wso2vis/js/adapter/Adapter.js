/**
* Constructs a new data adapter.
* @class Represents an abstract Class for data adapters. The Adapter class is the base class for all custom adapters in WSO2Vis.
* @constructor
*/
wso2vis.a.Adapter = function() {
    this.dp = null;
	this.drList = [];
	wso2vis.environment.adapters.push(this);
    id = wso2vis.environment.adapters.length - 1;    
    this.getID = function() {
        return id;
    }
};

/**
* Adds a data provider.
* @param {wso2vis.p.DataProvider} dp a data provider.
*/
wso2vis.a.Adapter.prototype.dataProvider = function(dp) {
	this.dp = dp;
	this.dp.addDataReceiver(this);
	return;
};

/**
* Adds a data receiver.
* @param {wso2vis.s.Subscriber} dr a data receiver.
*/
wso2vis.a.Adapter.prototype.addDataReceiver = function(dr) {
	this.drList.push(dr);
};


/**
* Pushes data to all the available data receivers.
* @private
* @param {json} data a json data object.
*/
wso2vis.a.Adapter.prototype.pushData = function(data) {
	var filteredData = this.convertData(data);
	for (i = 0; i < this.drList.length; i++) {
		(this.drList[i]).pushData(filteredData); 
	}
};

/**
* Pulls data from the data provider.
*/
wso2vis.a.Adapter.prototype.pullData = function() {
	this.dp.pullData();
};

/**
* Converts data inside the adapter. This method should be override with a custom converter function.
* @param {json} data a json data object.
* @returns {json} converted json data object.
*/
wso2vis.a.Adapter.prototype.convertData = function(data) {
	return data;
};

