/** 
 * PropertyGrid 
 */
wso2vis.ide.PropertyGrid = function(canvas) {
    this.attr = [];
    id = 1; //TODO
    this.getID = function() {
        return id;
    };
    
    this.divEl(canvas);
    this.clean();
    this.uwElement = null;
};

wso2vis.ide.PropertyGrid.prototype.property = function(name) {
    /*
    * Define the setter-getter globally
    */
    wso2vis.ide.PropertyGrid.prototype[name] = function(v) {
      if (arguments.length) {
        this.attr[name] = v;
        return this;
      }
      return this.attr[name];
    };

    return this;
};

wso2vis.ide.PropertyGrid.prototype
    .property("title")
    .property("divEl")
    .property("width")
    .property("height");
    
wso2vis.ide.PropertyGrid.prototype.load = function (uwElement) {
    this.clean();
    this.uwElement = uwElement;
    var propObj = uwElement.propertyObject;
    var elem = uwElement.elem;

    var propCount = propObj.properties.property.length;
    
    for( var i=0; i<propCount; i++ ) {
        var propName = propObj.properties.property[i]["name"];
        var propType = propObj.properties.property[i]["type"];
        var propFunction = propObj.properties.property[i]["function"];
        
        if( propType == "int" ) {
            var valfunction = elem[propFunction];
            var val = (valfunction != undefined) ? elem[propFunction]() : 0;
            this.field_int(propName, val, propFunction);
        }
        else if( propType == "string" ) {
            var valfunction = elem[propFunction];
            var val = (valfunction != undefined) ? elem[propFunction]() : 0;
            this.field_string(propName, val, propFunction);
        }
        else if( propType == "boolean" ) {
            this.field_boolean(propName, val, propFunction);
        }
        else if( propType == "array" ) {
            var valfunction = elem[propFunction];
            var val = (valfunction !== undefined) ? elem[propFunction]() : 0;
            this.field_array(propName, val.join(","), propFunction);
        }
    }
};

wso2vis.ide.PropertyGrid.prototype.update = function (uwElement) {
    this.clean();
    this.uwElement = uwElement;
    var propObj = uwElement.propertyObject;
    var elem = uwElement.elem;
    
    var propCount = propObj.properties.property.length;
    
    for( var i=0; i<propCount; i++ ) {
        var propName = propObj.properties.property[i]["name"];
        var propType = propObj.properties.property[i]["type"];
        var propFunction = propObj.properties.property[i]["function"];
        
        if( propType == "int" ) {
            var valfunction = elem[propFunction];
            var val = (valfunction !== undefined) ? elem[propFunction]() : 0;
            this.field_int(propName, val, propFunction);
        }
        else if( propType == "boolean" ) {
            this.field_boolean(propName, val, propFunction);
        }
        else if( propType == "string" ) {
            var valfunction = elem[propFunction];
            var val = (valfunction !== undefined) ? elem[propFunction]() : 0;
            this.field_string(propName, val, propFunction);
        }
        else if( propType == "array" ) {
            var valfunction = elem[propFunction];
            var val = (valfunction !== undefined) ? elem[propFunction]() : 0;
            this.field_array(propName, val.join(","), propFunction);
        }
    }
};

wso2vis.ide.PropertyGrid.prototype.updateProperty = function (propname, value) {
    var valfunction = this.uwElement.elem[propname];
    if (valfunction !== undefined) {
        this.uwElement.elem[propname](value);
        this.uwElement.updateUwElementDimentions();
    }
};

wso2vis.ide.PropertyGrid.prototype.clean = function () {
    document.getElementById(this.divEl()).innerHTML = "<table id='property_table'><tbody></tbody></table>";
};

wso2vis.ide.PropertyGrid.prototype.field_int = function (lbl, val, fname) {
    var html = "<input type='text' value='"+val+"' onChange='propGrid.updateProperty(\""+fname+"\", parseInt(this.value))'/>";
    this.addTableRow('property_table', lbl, html);
};

wso2vis.ide.PropertyGrid.prototype.field_string = function (lbl, val, fname) {
    var html = "<input type='text' value='"+val+"' onChange='propGrid.updateProperty(\""+fname+"\", this.value)'/>";
    this.addTableRow('property_table', lbl, html);
};

wso2vis.ide.PropertyGrid.prototype.field_array = function (lbl, val, fname) {
    var html = "<input type='text' value='"+val+"' onChange='propGrid.updateProperty(\""+fname+"\", this.value.split(\",\"))'/>";
    this.addTableRow('property_table', lbl, html);
};

wso2vis.ide.PropertyGrid.prototype.field_enum = function (lbl, enums) {
    var html = "<select>";
    for( var i=0; i<enums; i++ ) {
        html += "<option>"+enums[i]+"</option>";
    }
    html += "</select>";
    this.addTableRow('property_table', lbl, html);
};

wso2vis.ide.PropertyGrid.prototype.field_boolean = function (lbl, val, fname) {
    var html = "<select onChange='propGrid.updateProperty(\""+fname+"\", (this.options[this.selectedIndex].value == \"1\")?true:false)'>";
    html += "<option value='1'>True</option>";
    html += "<option value='0'>False</option>";
    this.addTableRow('property_table', lbl, html);
};

wso2vis.ide.PropertyGrid.prototype.addTableRow = function (tablename, label, content) {
    var td1 = "<td>"+label+"</td>";
    var td2 = "<td>"+content+"</td>";
    $("#"+tablename+" > tbody").append("<tr>"+td1+td2+"</tr>");
};


