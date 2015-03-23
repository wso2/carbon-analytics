var dashboard = window.dashboard || {};

Widget = function (data) {
	this.id = data.id.split(".")[1];
	this.dataview = data.id.split(".")[0];
	this.dimensions = data.dimensions;
};

//BindingSource A subscription manager for Widgets
dashboard.BindingSource = function() {
	this.channels = [];
};

dashboard.BindingSource.prototype.addWidget = function(dataview,widget) {
	var channel = this.getChannelByName(dataview);
	if(channel) {
		channel.subscriptions.push(widget);
		console.log("+++ Subscribed to channel " + channel.name); 
	} else {
		this.channels.push({
			"name" : dataview,
			"subscriptions" : [widget]
		});
		console.log("+++ Creating channel " + dataview + " and subscribing"); 
	}
};

dashboard.BindingSource.prototype.onDataReceived = function(data,update) {
	var channel = this.getChannelByName(data.dataview);
	if(channel) {
		channel.subscriptions.forEach(function (chart,i){
			if(update) {
				console.log("+++ Updating all widgets subscribed for dataview : " + data.dataview);
				chart.updateList(data.data);
			} else {
				console.log("+++ Redrawing all widgets subscribed for dataview :" + data.dataview);
				chart.plot(data.data);
			}
			
		});
	}
};

dashboard.BindingSource.prototype.getChannelByName = function (name) {
	for (var i = 0; i < this.channels.length; i++) {
		if(this.channels[i].name == name) {
			return this.channels[i];
		}
	}
};

dashboard.BindingSource.prototype.removeWidget = function(dataview,widget) {
	// TODO implement me
};