/**
* Constructs a new Tooltip.
* @class Represents a tool-tip which creates a graphical tool-tip in WSO2Vis.
* @constructor
*/
wso2vis.c.Tooltip = function () {
    this.el = document.createElement('div');
    this.el.setAttribute('id', 'ttipRRR'); // a random name to avoid conflicts. 
    this.el.style.display = 'none';
    this.el.style.width = 'auto';
    this.el.style.height = 'auto';
    this.el.style.margin = '0';
    this.el.style.padding = '5px';
    this.el.style.backgroundColor = '#ffffff';
    this.el.style.borderStyle = 'solid';
    this.el.style.borderWidth = '1px';
    this.el.style.borderColor = '#444444';
    this.el.style.opacity = 0.85;
    
    this.el.style.fontFamily = 'Fontin-Sans, Arial';
    this.el.style.fontSize = '12px';
    
    this.el.innerHTML = "<b>wso2vis</b> tooltip demo <br/> works damn fine!";    
    document.body.appendChild(this.el);    
};

wso2vis.c.Tooltip.prototype.style = function() {
    return this.el.style;
};

/**
* Shows the tool-tip in the given x y coordinates.
* @param {float} x x coordinate of the tool-tip.
* @param {float} y y coordinate of the tool-tip.
* @param {string} content content of the tool-tip.
*/
wso2vis.c.Tooltip.prototype.show = function(x, y, content) {    
	var w = this.el.style.width;
	var h = this.el.style.height;
	var deltaX = 15;
    var deltaY = 15;
	
	if ((w + x) >= (this.getWindowWidth() - deltaX)) { 
		x = x - w;
		x = x - deltaX;
	} 
	else {
		x = x + deltaX;
	}
	
	if ((h + y) >= (this.getWindowHeight() - deltaY)) { 
		y = y - h;
		y = y - deltaY;
	} 
	else {
		y = y + deltaY;
	} 
	
	this.el.style.position = 'absolute';
	this.el.style.top = y + 'px';
	this.el.style.left = x + 'px';	
	if (content != undefined) 
	    this.el.innerHTML = content;
	this.el.style.display = 'block';
	this.el.style.zindex = 1000;
};

/**
* Hides the tool-tip.
*/
wso2vis.c.Tooltip.prototype.hide = function() {
    this.el.style.display = 'none';
};

/**
* Returns current window height
* @private
*/
wso2vis.c.Tooltip.prototype.getWindowHeight = function(){
    var innerHeight;
    if (navigator.appVersion.indexOf('MSIE')>0) {
	    innerHeight = document.body.clientHeight;
    } 
    else {
	    innerHeight = window.innerHeight;
    }
    return innerHeight;	
};

/**
* Returns current window width
* @private
*/
wso2vis.c.Tooltip.prototype.getWindowWidth = function(){
    var innerWidth;
    if (navigator.appVersion.indexOf('MSIE')>0) {
	    innerWidth = document.body.clientWidth;
    } 
    else {
	    innerWidth = window.innerWidth;
    }
    return innerWidth;	
};

