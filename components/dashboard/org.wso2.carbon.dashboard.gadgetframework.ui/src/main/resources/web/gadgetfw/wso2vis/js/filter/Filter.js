/**
 * Filter
 */
wso2vis.f.Filter = function() {
    this.attr = [];
	this.dp = null;
	this.drList = [];
	wso2vis.environment.filters.push(this);
    id = wso2vis.environment.filters.length - 1;
    this.getID = function() {return id;}
};

wso2vis.f.Filter.prototype.property = function(name) {
    /*
    * Define the setter-getter globally
    */
    wso2vis.f.Filter.prototype[name] = function(v) {
      if (arguments.length) {
        this.attr[name] = v;
        return this;
      }
      return this.attr[name];
    };

    return this;
};

wso2vis.f.Filter.prototype.dataProvider = function(dp) {
	this.dp = dp;
	this.dp.addDataReceiver(this);
	return;
};

wso2vis.f.Filter.prototype.addDataReceiver = function(dr) {
	this.drList.push(dr);
};

wso2vis.f.Filter.prototype.pushData = function(data) {
	var filteredData = this.filterData(data);
	for (i = 0; i < this.drList.length; i++) {
		(this.drList[i]).pushData(filteredData); 
	}
};

wso2vis.f.Filter.prototype.pullData = function() {
	this.dp.pullData();
};

wso2vis.f.Filter.prototype.filterData = function(data) {
	return data;
};

wso2vis.f.Filter.prototype.traverseToDataField = function (object, dataFieldArray) {
	var a = object;					
	for (var i = 0; i < dataFieldArray.length; i++) {
		a = a[dataFieldArray[i]];
	}
	return a;
};
