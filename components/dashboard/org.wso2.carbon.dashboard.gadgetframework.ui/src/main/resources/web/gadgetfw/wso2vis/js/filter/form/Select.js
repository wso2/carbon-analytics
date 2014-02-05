/**
 * @class Select
 * @extends Form
 */
wso2vis.f.form.Select = function() { //canvas, selectID, onChangeFuncStr, dataField, key, value, defaultText) {
    wso2vis.f.BasicFilter.call(this);
    this.defaultText("default");
    this.filterArray([]);
    /* @private */
	this.dirty = true;
};

wso2vis.extend(wso2vis.f.form.Select, wso2vis.f.BasicFilter);

wso2vis.f.form.Select.prototype
    .property("canvas")
    .property("dataField")
    .property("dataLabel")
    .property("dataValue")
    .property("defaultText");

wso2vis.f.form.Select.prototype.invalidate = function() {
    this.dirty = true;
}

/*wso2vis.f.form.Select.prototype.filterData = function(data) {
    if (this.dirty) {
        this.dirty = false;
        
        
        
    }
    this.superclass.filterData(data);
};*/

wso2vis.f.form.Select.prototype.create = function() {
    var newElementHTML = '<select id="wso2visSelect_'+this.getID()+'" onchange="wso2vis.fn.selectFormChanged('+this.getID()+');">';    
    if ((this.filterArray() !== undefined) && (this.filterArray() !== null) && (this.filterArray().length > 0)) {
        newElementHTML += '<option value="' + this.defaultText() + '">' + this.defaultText() + '</option>';
        newElementHTML += '<option value="' + this.filterArray()[0] + '" selected>' + this.filterArray()[0] + '</option>';        
    }
    else {
        newElementHTML += '<option value="' + this.defaultText() + '" selected>' + this.defaultText() + '</option>';
    }    
    if (this.remainingArray !== null && this.remainingArray.length > 0) {
        for (var x = 0; x < this.remainingArray.length; x++) {
            newElementHTML += '<option value="' + this.remainingArray[x] + '">' + this.remainingArray[x] + '</option>'
        }
    }    
    newElementHTML += '</select>';
    return newElementHTML;
};

wso2vis.f.form.Select.prototype.load = function() {    var canvas = document.getElementById(this.canvas());    canvas.innerHTML = this.create();};
wso2vis.f.form.Select.prototype.unload = function() {    var canvas = document.getElementById(this.canvas());    canvas.innerHTML = "";};

wso2vis.f.form.Select.prototype.onChange = function(text) {    
};

wso2vis.fn.selectFormChanged = function(id) {
    var filter = wso2vis.fn.getFilterFromID(id);
    var elem = document.getElementById("wso2visSelect_"+id);      
    filter.filterArray([]);
    if (elem[elem.selectedIndex].text != filter.defaultText())
        filter.filterArray().push(elem[elem.selectedIndex].text);   
    filter.onChange(elem[elem.selectedIndex].text);  
};
