wso2vis.ide.uwElement = function (r, uElement, width, height, provider, consProp, name, propObj, type) {
	this.x = 0;
	this.y = 0;
	this.width = width;
	this.height = height;
	this.r = r;
	this.elem = uElement;
	this.hotspots = [];
	this.selected = true;
	this.hslen = 5;
	this.hslen2 = 10;
	this.propertyObject = propObj;
	
	this.consProp = consProp;
	this.provider = provider;
	this.name = name;
	this.type = type; // 0 - pieChart, ... 
}

wso2vis.ide.uwElement.prototype.updateHotspots = function (ind) {
	if (ind != 0)
		this.hotspots[0].attr({x:this.x - this.hslen, y:this.y - this.hslen}); // top-left
	if (ind != 1)
		this.hotspots[1].attr({x:this.x - this.hslen + this.width/2, y:this.y - this.hslen}); // top-center
	if (ind != 2)
		this.hotspots[2].attr({x:this.x - this.hslen + this.width, y:this.y - this.hslen}); // top-right
	if (ind != 3)
		this.hotspots[3].attr({x:this.x - this.hslen + this.width, y:this.y - this.hslen + this.height/2}); // middle-right
	if (ind != 4)
		this.hotspots[4].attr({x:this.x - this.hslen + this.width, y:this.y - this.hslen + this.height}); // bottom-right
	if (ind != 5)
		this.hotspots[5].attr({x:this.x - this.hslen + this.width/2, y:this.y - this.hslen + this.height}); //bottom-center
	if (ind != 6)
		this.hotspots[6].attr({x:this.x - this.hslen, y:this.y - this.hslen + this.height}); //bottom-left
	if (ind != 7)
		this.hotspots[7].attr({x:this.x - this.hslen, y:this.y - this.hslen + this.height/2}); // middle-left
	if (ind != 8)
		this.hotspots[8].attr({x:this.x - this.hslen + this.width/2, y:this.y - this.hslen + this.height/2}); // middle - center
		
	this.trlayer.attr({x: this.x, y: this.y, width: this.width, height: this.height});
}

wso2vis.ide.uwElement.prototype.updateDimentions = function (that){
	that.elem.width(that.width);
	that.elem.height(that.height);
	that.elem.left(that.x);
	that.elem.top(that.y);
	propGrid.update(that);
}

wso2vis.ide.uwElement.prototype.updateUwElementDimentions = function (){
	this.width = parseInt(this.elem.width());
	this.height = parseInt(this.elem.height());
	this.x = parseInt(this.elem.left());
	this.y = parseInt(this.elem.top());
	this.updateHotspots();
	this.provider.pullDataSync();
}

wso2vis.ide.uwElement.prototype.redraw = function (x, y) {
	this.x = x;
	this.y = y;
	this.elem.width(this.width);
	this.elem.height(this.height);
	this.elem.left(this.x);
	this.elem.top(this.y);
	
	this.hotspots[0] = this.r.rect(this.x - this.hslen, this.y - this.hslen, this.hslen2, this.hslen2, 2); // top-left
	this.hotspots[1] = this.r.rect(this.x - this.hslen + this.width/2, this.y - this.hslen, this.hslen2, this.hslen2, 2); // top-center
	this.hotspots[2] = this.r.rect(this.x - this.hslen + this.width, this.y - this.hslen, this.hslen2, this.hslen2, 2); // top-right
	this.hotspots[3] = this.r.rect(this.x - this.hslen + this.width, this.y - this.hslen + this.height/2, this.hslen2, this.hslen2, 2); // middle-right
	this.hotspots[4] = this.r.rect(this.x - this.hslen + this.width, this.y - this.hslen + this.height, this.hslen2, this.hslen2, 2); // bottom-right
	this.hotspots[5] = this.r.rect(this.x - this.hslen + this.width/2, this.y - this.hslen + this.height, this.hslen2, this.hslen2, 2); //bottom-center
	this.hotspots[6] = this.r.rect(this.x - this.hslen, this.y - this.hslen + this.height, this.hslen2, this.hslen2, 2); //bottom-left
	this.hotspots[7] = this.r.rect(this.x - this.hslen, this.y - this.hslen + this.height/2, this.hslen2, this.hslen2, 2); // middle-left
	this.hotspots[8] = this.r.rect(this.x - this.hslen + this.width/2, this.y - this.hslen + this.height/2, this.hslen2, this.hslen2, 2); // middle - center
	
	for (var i = 0; i < 9; i++)
	{
		this.hotspots[i].attr({fill:"#AAA", opacity:0.5});
	}
	
	if (this.consProp)
	{
		this.hotspots[1].hide();
		this.hotspots[3].hide();
		this.hotspots[5].hide();
		this.hotspots[7].hide();
	}
	
	this.trlayer = this.r.rect(this.x, this.y, this.width, this.height);
	this.trlayer.attr({"fill-opacity":0, fill:"#fff", stroke:"#AAA", "stroke-dasharray":"--", "stroke-opacity":0.5});
	var that = this;
	
	var trlayerClick = function(event) {
		that.select();
		event.stopPropagation();
	}
	this.trlayer.click(trlayerClick);
	
	var trlayer = this.trlayer;
	var widwid = this.width;
	var heihei = this.height;
	var toptop = this.y;
	var leflef = this.x;
	
	// top-center
	var start1 = function () {
		this.ox = this.attr("x");
		this.oy = this.attr("y");
		this.startY = that.y;
		this.startHeight = that.height;
		this.attr({opacity: 1});
	},
	move1 = function (dx, dy) {
		this.attr({x: this.ox, y: this.oy + dy});		
		trlayer.attr({y:this.startY + dy, height: this.startHeight - dy});
		that.y = this.startY + dy;
		that.height = this.startHeight - dy;
		that.updateHotspots(1);
	},
	up1 = function (d) {
		this.attr({opacity: .5});
		that.updateDimentions(that);
		that.provider.pullDataSync();
	};
	this.hotspots[1].drag(move1, start1, up1);

	// middle-right
	var start3 = function () {
		this.ox = this.attr("x");
		this.oy = this.attr("y");
		this.startWidth = that.width;
		this.attr({opacity: 1});
	},
	move3 = function (dx, dy) {
		this.attr({x: this.ox + dx, y: this.oy});		
		trlayer.attr({width:this.startWidth + dx});
		that.width = this.startWidth + dx;
		that.updateHotspots(3);
	},
	up3 = function () {
		this.attr({opacity: .5});
	};
	this.hotspots[3].drag(move3, start3, up1);

	// bottom-center
	var start5 = function () {
		this.ox = this.attr("x");
		this.oy = this.attr("y");
		this.startHeight = that.height;
		this.attr({opacity: 1});
	},
	move5 = function (dx, dy) {
		this.attr({x: this.ox, y: this.oy + dy});		
		trlayer.attr({height:this.startHeight + dy});
		that.height = this.startHeight + dy;
		that.updateHotspots(5);
	},
	up5 = function () {
		this.attr({opacity: .5});
	};
	this.hotspots[5].drag(move5, start5, up1);

	// middle-left
	var start7 = function () {
		this.ox = this.attr("x");
		this.oy = this.attr("y");
		this.startWidth = that.width;
		this.startX = that.x;
		this.attr({opacity: 1});
	},
	move7 = function (dx, dy) {
		this.attr({x: this.ox + dx, y: this.oy});		
		trlayer.attr({width:this.startWidth - dx, x:this.startX + dx});
		that.width = this.startWidth - dx;
		that.x = this.startX + dx;
		that.updateHotspots(7);
	},
	up7 = function () {
		this.attr({opacity: .5});
	};
	this.hotspots[7].drag(move7, start7, up1);
	
	// bottom-center
	var start8 = function () {
		this.ox = this.attr("x");
		this.oy = this.attr("y");
		this.startX = that.x;
		this.startY = that.y;
		this.attr({opacity: 1});
	},
	move8 = function (dx, dy) {
		this.attr({x: this.ox + dx, y: this.oy + dy});		
		trlayer.attr({x:this.startX + dx, y:this.startY + dy});
		that.x = this.startX + dx;
		that.y = this.startY + dy;
		that.updateHotspots(8);
	},
	up8 = function () {
		this.attr({opacity: .5});
	};
	this.hotspots[8].drag(move8, start8, up1);

	// top-left
	var start0 = function () {
		this.ox = this.attr("x");
		this.oy = this.attr("y");
		this.startX = that.x;
		this.startY = that.y;
		this.startHeight = that.height;
		this.startWidth = that.width;
		this.alpha = Math.atan2(this.startHeight, this.startWidth);
		this.cosalpha = Math.cos(this.alpha);
		this.sinalpha = Math.sin(this.alpha);
		this.attr({opacity: 1});
	},
	move0 = function (dx, dy) {
		var gamma = Math.atan2(dy, dx);
		var dist = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
		var ax = dist * Math.cos(this.alpha - gamma) * this.cosalpha;
		var ay = dist * Math.cos(this.alpha - gamma) * this.sinalpha;
		this.attr({x: this.ox + ax, y: this.oy + ay});
		trlayer.attr({x:this.startX + ax, y:this.startY + ay, width: this.startWidth - ax, height: this.startHeight - ay});
		that.y = this.startY + ay;
		that.x = this.startX + ax;
		that.width = this.startWidth - ax;
		that.height = this.startHeight - ay;
		that.updateHotspots(0);
	},
	up0 = function () {
		this.attr({opacity: .5});
	};
	this.hotspots[0].drag(move0, start0, up1);
	
	// top-right
	var start2 = function () {
		this.ox = this.attr("x");
		this.oy = this.attr("y");
		this.startX = that.x;
		this.startY = that.y;
		this.startHeight = that.height;
		this.startWidth = that.width;
		this.alpha = Math.atan2(this.startHeight, this.startWidth);
		this.cosalpha = Math.cos(this.alpha);
		this.sinalpha = Math.sin(this.alpha);
		this.attr({opacity: 1});
	},
	move2 = function (dx, dy) {
		var gamma = Math.atan2(dy, -dx);
		var dist = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
		var ax = dist * Math.cos(this.alpha - gamma) * this.cosalpha;
		var ay = dist * Math.cos(this.alpha - gamma) * this.sinalpha;
		this.attr({x: this.ox - ax, y: this.oy + ay});

		trlayer.attr({y:this.startY + ay, width: this.startWidth - ax, height: this.startHeight - ay});
		that.y = this.startY + ay;
		that.width = this.startWidth - ax;
		that.height = this.startHeight - ay;
		that.updateHotspots(2);
	},
	up2 = function () {
		this.attr({opacity: .5});
	};
	this.hotspots[2].drag(move2, start2, up1);


	// bottom-left
	var start6 = function () {
		this.ox = this.attr("x");
		this.oy = this.attr("y");
		this.startX = that.x;
		//this.startY = that.y;
		this.startHeight = that.height;
		this.startWidth = that.width;
		this.alpha = Math.atan2(this.startHeight, this.startWidth);
		this.cosalpha = Math.cos(this.alpha);
		this.sinalpha = Math.sin(this.alpha);
		this.attr({opacity: 1});
	},
	move6 = function (dx, dy) {
		var gamma = Math.atan2(-dy, dx);
		var dist = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
		var ax = dist * Math.cos(this.alpha - gamma) * this.cosalpha;
		var ay = dist * Math.cos(this.alpha - gamma) * this.sinalpha;
		this.attr({x: this.ox + ax, y: this.oy - ay});
		trlayer.attr({x:this.startX + ax, width: this.startWidth - ax, height: this.startHeight - ay});
		that.x = this.startX + ax;
		that.width = this.startWidth - ax;
		that.height = this.startHeight - ay;
		that.updateHotspots(6);
	},
	up6 = function () {
		this.attr({opacity: .5});
	};
	this.hotspots[6].drag(move6, start6, up1);

	// bottom-right
	var start4 = function () {
		this.ox = this.attr("x");
		this.oy = this.attr("y");
		this.startHeight = that.height;
		this.startWidth = that.width;
		this.alpha = Math.atan2(this.startHeight, this.startWidth);
		this.cosalpha = Math.cos(this.alpha);
		this.sinalpha = Math.sin(this.alpha);
		this.attr({opacity: 1});
	},
	move4 = function (dx, dy) {
		var gamma = Math.atan2(-dy, -dx);
		var dist = Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
		var ax = dist * Math.cos(this.alpha - gamma) * this.cosalpha;
		var ay = dist * Math.cos(this.alpha - gamma) * this.sinalpha;
		this.attr({x: this.ox - ax, y: this.oy - ay});
		trlayer.attr({width: this.startWidth - ax, height: this.startHeight - ay});
		that.width = this.startWidth - ax;
		that.height = this.startHeight - ay;
		that.updateHotspots(4);
	},
	up4 = function () {
		this.attr({opacity: .5});
	};
	this.hotspots[4].drag(move4, start4, up1);

	this.elem.load(this.width);
};

wso2vis.ide.uwElement.prototype.pushData = function (d) {
	this.elem.pushData(d);
	if (this.selected) 
	{
		this.trlayer.toFront();
		if (!this.consProp)
		{
			this.hotspots[1].show();
			this.hotspots[1].toFront();
			this.hotspots[3].show();
			this.hotspots[3].toFront();
			this.hotspots[5].show();
			this.hotspots[5].toFront();
			this.hotspots[7].show();
			this.hotspots[7].toFront();
		}
		
		this.hotspots[0].show();
		this.hotspots[0].toFront();
		this.hotspots[2].show();
		this.hotspots[2].toFront();
		this.hotspots[4].show();
		this.hotspots[4].toFront();
		this.hotspots[6].show();
		this.hotspots[6].toFront();
		this.hotspots[8].show();
		this.hotspots[8].toFront();
		
		this.trlayer.attr({'stroke-opacity':0.5});
	}
	else 
	{
		for (var i = 0; i < 9; i++) 
			this.hotspots[i].hide();
		this.trlayer.toFront();
		this.trlayer.attr({'stroke-opacity':0});
		//this.trlayer.hide();
	}
};

wso2vis.ide.uwElement.prototype.select = function () {
    this.selected = true;
    this.provider.pullDataSync();
};

wso2vis.ide.uwElement.prototype.deselect = function () {
    this.selected = false;
    this.provider.pullDataSync();
};

