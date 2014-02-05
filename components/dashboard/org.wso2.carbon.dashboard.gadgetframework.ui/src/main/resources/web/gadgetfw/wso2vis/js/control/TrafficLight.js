/**
* Constructs a new TrafficLight.
* @class Represents a traffic light widget which creates a graphical traffic light in WSO2Vis.
* @param {canvas} canvas the name of the HTML element (ex: div) where the graphical traffic light should be drawn.
* @constructor
*/
wso2vis.c.TrafficLight = function(canvas) {
    this.attr = [];
    
    this.divEl(canvas);

    /* @private */
    this.lights = [];
    this.back = [];
    
    this.stat = [];
    this.stat["top"] = false;
    this.stat["middle"] = false;
    this.stat["bottom"] = false;
        
    this.color = [];
    this.color["top_on"] = "r#f00-#f00-#89070D";
    this.color["top_off"] = "r#900-#560101-#350305";
    this.color["middle_on"] = "r#FFD52E-#FFEB14-#9E4500";
    this.color["middle_off"] = "r#BD5F00-#6B2F00-#473A00";
    this.color["bottom_on"] = "r#0f0-#0e0-#07890D";
    this.color["bottom_off"] = "r#090-#015601-#033505";
        
    this.radius(35)
        .xspace(15)
        .yspace(20)
        .gap(10)
        .paper(null);
        
    this.dia = this.radius()*2;
    this.backh = (this.dia*3)+(this.gap()*2)+(this.yspace()*2);
    this.backw = this.dia+(this.xspace()*2);
    
    this.startX = this.radius() + this.xspace();
    this.startY = this.radius() + this.yspace();
};

/**
* @private Defines and registers a property method for the property with the given name.
* @param {string} name the property name.
*/
wso2vis.c.TrafficLight.prototype.property = function(name) {
    /*
    * Define the setter-getter globally
    */
    wso2vis.c.TrafficLight.prototype[name] = function(v) {
      if (arguments.length) {
        this.attr[name] = v;
        return this;
      }
      return this.attr[name];
    };

    return this;
};

/* Define all properties. */
wso2vis.c.TrafficLight.prototype
    .property("title")
    .property("divEl")
    .property("radius")
    .property("gap")
    .property("paper")
    .property("xspace")
    .property("yspace");

/**
* Loads the graphical control inside the given HTML element (ex: div).
* @param {string} name the property name.
*/
wso2vis.c.TrafficLight.prototype.load = function (p) {

    if( (p != null) && (p != undefined) && (p != "") ) {
        this.paper(p);
    }
    else {
        this.paper(Raphael(this.divEl()));
    }
    
    /* Init */
    this.paper().clear();
    this.back[0] = this.paper().rect(0, 0, this.backw, this.backh, 8).attr({"stroke-width": 1, stroke: "#000", fill: "#ccc"});
    this.back[1] = this.paper().rect(5, 5, this.backw-10, this.backh-10, 8).attr({stroke: "none", fill: "#000"});
    
    //Lights
    this.lights["top"] = this.paper().circle(this.startX, this.startY, this.radius()).attr({fill: this.color["top_off"], stroke: "#333"});
    this.lights["middle"] = this.paper().circle(this.startX, (this.startY + this.dia + this.gap()), this.radius()).attr({fill: this.color["middle_off"], stroke: "#333"});
    this.lights["bottom"] = this.paper().circle(this.startX, this.startY + (this.dia*2) + (this.gap()*2), this.radius()).attr({fill: this.color["bottom_off"], stroke: "#333"});

};

wso2vis.c.TrafficLight.prototype.on = function (light) {
    this.lights[light].animate({fill: this.color[light+"_on"]}, 100);
    this.stat[light] = true;
};

wso2vis.c.TrafficLight.prototype.off = function (light) {
    this.lights[light].animate({fill: this.color[light+"_off"]}, 100);
    this.stat[light] = false;
};

wso2vis.c.TrafficLight.prototype.toggle = function (light) {
    if( this.stat[light] ) {
        this.off(light);
    }
    else {
        this.on(light);
    }
};

wso2vis.c.TrafficLight.prototype.horizontal = function () {
    /**
    * Horizontal Layout
    **/
    this.back[0].attr({width: this.backh, height: this.backw});
    this.back[1].attr({width: this.backh-10, height: this.backw-10});
    
    this.lights["top"].attr({cx: this.startY, cy: this.startX});
    this.lights["middle"].attr({cx: this.startY + this.dia + this.gap(), cy: this.startX});
    this.lights["bottom"].attr({cx: this.startY + (this.dia*2) + (this.gap()*2), cy: this.startX});
};

wso2vis.c.TrafficLight.prototype.vertical = function () {
    /**
    * Vertical Layout
    **/
    this.back[0].attr({width: this.backw, height: this.backh});
    this.back[1].attr({width: this.backw-10, height: this.backh-10});
    
    this.lights["top"].attr({cx: this.startX, cy: this.startY});
    this.lights["middle"].attr({cx: this.startX, cy: this.startY + this.dia + this.gap()});
    this.lights["bottom"].attr({cx: this.startX, cy: this.startY + (this.dia*2) + (this.gap()*2)});
};

