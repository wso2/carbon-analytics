function removeCarriageReturns(string) {
    return string.replace(/\n/g, "");
}

function hasHTML(inputItem) {
    if (inputItem.search(/<\/?[^>]+(>|$)/g) < 0)
        return false;
    else
        return true;
}
function confirmDialog(message, handleYes, gId) {
    /* This function always assume that your second parameter is handleYes function and third parameter is handleNo function.
     * If you are not going to provide handleYes function and want to give handleNo callback please pass null as the second
     * parameter.
     */
    var strDialog = "<div id='dialog' title='WSO2 Carbon'><div id='messagebox-confirm'><p>" +
            message + "</p></div></div>";

    handleYes = handleYes || function() {
        return true
    };

    jQuery("#dcontainer").html(strDialog);

    jQuery("#dialog").dialog({
        close:function() {
            jQuery(this).dialog('destroy').remove();
            jQuery("#dcontainer").empty();
            return false;
        },
        buttons:{
            "Yes":function() {
                jQuery(this).dialog("destroy").remove();
                jQuery("#dcontainer").empty();
                handleYes(gId);
            },
            "No":function() {
                jQuery(this).dialog("destroy").remove();
                jQuery("#dcontainer").empty();
                return false;
            }
        },
        height:160,
        width:450,
        minHeight:160,
        minWidth:330,
        modal:true
    });
    return false;

}

