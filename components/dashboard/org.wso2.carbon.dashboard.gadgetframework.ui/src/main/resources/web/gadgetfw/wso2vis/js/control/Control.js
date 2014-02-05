/**
* Constructs a new Control.
* @class Represents an abstract Class for controls. The Control class is the base class for all graphical controls in WSO2Vis.
* @constructor
* @param {string} canvas the name of the HTML element (ex: div) where the graphical control should be drawn.
*/
wso2vis.c.Control = function(canvas) {
   	this.attr = []; 	
  	this.canvas(canvas);   
    
	this.dp = null;
	wso2vis.environment.controls.push(this);
    
	id = wso2vis.environment.controls.length - 1;
    this.getID = function() {return id;}	
};

/**
* @private Defines and registers a property method for the property with the given name.
* @param {string} name the property name.
*/
wso2vis.c.Control.prototype.property = function(name) {
    wso2vis.c.Control.prototype[name] = function(v) {
      if (arguments.length) {
        this.attr[name] = v;
        return this;
      }
      return this.attr[name];
    };

    return this;
};

/* Define all properties. */
wso2vis.c.Control.prototype.property("canvas");

/**
* Creates the graphical control.
*/
wso2vis.c.Control.prototype.create = function() {
};

/**
* Loads the graphical control inside the given HTML element (ex: div).
*/
wso2vis.c.Control.prototype.load = function() {
    var divEl = document.getElementById(this.canvas());
    divEl.innerHTML = this.create();
};

/**
* Unloads the graphical control from the given HTML element (ex: div).
*/
wso2vis.c.Control.prototype.unload = function() {
    var divEl = document.getElementById(this.canvas());
    divEl.innerHTML = "";
};

