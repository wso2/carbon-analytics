/**
* FilterForm 
*/
wso2vis.f.form.FilterForm = function() {
	wso2vis.f.BasicFilter.call(this);
};

wso2vis.extend(wso2vis.f.form.FilterForm, wso2vis.f.BasicFilter);

wso2vis.f.form.FilterForm.prototype.property("canvas");

wso2vis.f.form.FilterForm.prototype.create = function() {
    var i = 0;
    var content;
    content = '<form>' +
    '<table width="100%" border="0">' +
    '<tr>' +
    '  <td width="13%" rowspan="4"><select name="FilterFormList1_'+this.getID()+'" id="FilterFormList1_'+this.getID()+'" size="10" style="width: 110px">';
    for (i = 0; i < this.remainingArray.length; i++) {
        if (i == 0)
            content += '<option value="'+i+'" selected>' + this.remainingArray[i] +'</option>';
        else 
            content += '<option value="'+i+'">' + this.remainingArray[i] +'</option>';
    }
    content += '  </select></td>' +
    '  <td width="6%">&nbsp;</td>' +
    '  <td width="14%" rowspan="4"><select name="FilterFormList2_'+this.getID()+'" id="FilterFormList2_'+this.getID()+'" size="10" style="width: 110px">';
    if (this.filterArray() !== undefined) {
        for (i = 0; i < this.filterArray().length; i++) {
            if (i == 0)
                content += '<option value="'+i+'" selected>' + this.filterArray()[i] +'</option>';
            else 
                content += '<option value="'+i+'">' + this.filterArray()[i] +'</option>';
        }
    }
    content += '  </select></td>' +
    '  <td width="7%">&nbsp;</td>' +
    '  <td width="60%" rowspan="4">&nbsp;</td>' +
    '</tr>' +
    '<tr>' +
    '  <td><div align="center">' +
    '    <input type="button" name="buttonLeft" id="buttonLeft" value="Add" style="width: 50px" onclick="FilterFormButtonLeft('+this.getID()+');"/>' +
    '  </div></td>' +
    '  <td><div align="center">' +
    '      <input type="button" name="buttonUp" id="buttonUp" value="Up" style="width: 50px" onclick="FilterFormButtonUp('+this.getID()+');"/>' +
    '  </div></td>' +
    '</tr>' +
    '<tr>' +
    '  <td><div align="center">' +
    '    <input type="button" name="buttonRight" id="buttonRight" value="Remove" style="width: 50px" onclick="FilterFormButtonRight('+this.getID()+');"/>' +
    '  </div></td>' +
    '  <td><div align="center">' +
    '      <input type="button" name="buttonDown" id="buttonDown" value="Down" style="width: 50px" onclick="FilterFormButtonDown('+this.getID()+');"/>' +
    '  </div></td>' +
    '</tr>' +
    '<tr>' +
    '  <td>&nbsp;</td>' +
    '  <td>&nbsp;</td>' +
    '</tr>' +
    '<tr>' +
    '  <td colspan="5">' +
    '    <input type="button" name="buttonApply" id="buttonApply" value="Apply" style="width: 50px"  onclick="FilterFormButtonApply('+this.getID()+')"/>' +
    '    <input type="button" name="buttonCancel" id="buttonCancel" value="Cancel" style="width: 50px"  onclick="FilterFormButtonCancel('+this.getID()+')"/></td>' +
    '</tr>' +
    '</table>' +
    '</form>';
    
    return content;
}

wso2vis.f.form.FilterForm.prototype.load = function() {    var canvas = document.getElementById(this.canvas());    canvas.innerHTML = this.create();};
wso2vis.f.form.FilterForm.prototype.unload = function() {    var canvas = document.getElementById(this.canvas());    canvas.innerHTML = "";};

wso2vis.f.form.FilterForm.prototype.onApply = function(data) {    
};

wso2vis.f.form.FilterForm.prototype.create.onCancel = function() {
};

FilterFormButtonUp = function(id) {
	FilterFormItemMoveWithin(true, "FilterFormList2_"+id);
};

FilterFormButtonDown = function(id) {
	FilterFormItemMoveWithin(false, "FilterFormList2_"+id);
};

FilterFormButtonLeft = function(id) {
	FilterFormItemMoveInbetween("FilterFormList1_"+id, "FilterFormList2_"+id);
};

FilterFormButtonRight = function(id) {
	FilterFormItemMoveInbetween("FilterFormList2_"+id, "FilterFormList1_"+id);
};

FilterFormButtonApply = function(id) {
    var basicDataFilter = wso2vis.fn.getFilterFromID(id);	
    var list2Element = document.getElementById("FilterFormList2_" + id);
    var i = 0;
    basicDataFilter.filterArray([]);
    for (i = 0; i < list2Element.length; i++) {
        basicDataFilter.filterArray().push(list2Element.options[i].text);
    }    
    basicDataFilter.onApply(basicDataFilter.filterArray());
};

FilterFormButtonCancel = function(id) {
    var FilterForm = wso2vis.fn.getFilterFromID(id);
    FilterForm.onCancel();
};

FilterFormItemMoveInbetween = function(listName, listName2) {
    var src = document.getElementById(listName);
    var dst = document.getElementById(listName2);
    var idx = src.selectedIndex;
    if (idx==-1) 
        alert("You must first select the item to move.");
    else {
        var oldVal = src[idx].value;
        var oldText = src[idx].text;
        src.remove(idx);		

        var nxidx;
        if (idx>=src.length-1) 
	        nxidx=src.length-1;		
        else 
	        nxidx=idx;			
        if (src.length > 0) { 
	        src.selectedIndex = nxidx;		
        }

        var opNew = document.createElement('option');
        opNew.text = oldText;
        opNew.value = oldVal;		
        try {
	        dst.add(opNew, null); // standards compliant; doesn't work in IE
        }
        catch(ex) {
	        dst.add(opNew); // IE only
        }		
        dst.selectedIndex = dst.length - 1;	
    }
};

FilterFormItemMoveWithin = function(bDir,sName) {
    var el = document.getElementById(sName);
    var idx = el.selectedIndex
    if (idx==-1) 
        alert("You must first select the item to reorder.")
    else {
        var nxidx = idx+( bDir? -1 : 1)
        if (nxidx<0) nxidx=el.length-1
        if (nxidx>=el.length) nxidx=0
        var oldVal = el[idx].value
        var oldText = el[idx].text
        el[idx].value = el[nxidx].value
        el[idx].text = el[nxidx].text
        el[nxidx].value = oldVal
        el[nxidx].text = oldText
        el.selectedIndex = nxidx
    }
};

