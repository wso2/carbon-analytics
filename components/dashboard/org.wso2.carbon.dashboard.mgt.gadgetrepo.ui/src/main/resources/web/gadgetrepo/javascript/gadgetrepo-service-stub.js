var GadgetRepoService = GadgetRepoService || {};

GadgetRepoService.addGadget = function addGadget(userId, tabId, url,
		dashboardName, gadgetPath, grp) {

	var response = jQuery.ajax( {
		type : "POST",
		url : "GadgetBrowser-ajaxprocessor.jsp",
		data : "func=addGadget&userId=" + userId + "&tabId=" + tabId + "&url="
				+ url + "&dashboardName=" + dashboardName + "&nocache="
				+ new Date().getTime() + "&gadgetPath=" + gadgetPath + "&group=" + grp,
		async : false
	}).responseText;

	response = removeCarriageReturns(response);
	return response == "true" || response == "1";
}

GadgetRepoService.rateGadget = function rateGadget (path, rateVal,tabId,gadgetGroup) {

	var response = jQuery.ajax( {
		type : "POST",
		url : "GadgetRepo-ajaxprocessor.jsp",
		data : "func=rateGadget&gadgetPath=" + path + "&rating=" + rateVal+"&tab="+tabId+"&grp="+gadgetGroup,
		async : false
	}).responseText;

	response = removeCarriageReturns(response);
	return response == "true" || response == "1";
}

GadgetRepoService.deleteGadget = function deleteGadget(gadgetPath) {

	var response = jQuery.ajax( {
		type : "POST",
		url : "GadgetRepo-ajaxprocessor.jsp",
		data : "func=deleteGadget&gadgetPath=" + gadgetPath,
		async : false
	}).responseText;

	response = removeCarriageReturns(response);
    $('.pagination').load('paginetor-ajaxprocessor.jsp');
    paginate(0);
	return response == "true" || response == "1";

}

GadgetRepoService.addComment = function addComment(gadgetPath, comment, user,tabId,gadgetGroup) {
	var response = jQuery.ajax( {
		type : "POST",
		url	: "GadgetRepo-ajaxprocessor.jsp",
		data : "func=addComment&gadgetPath=" + gadgetPath + "&commentText=" + comment + "&user=" + user+"&tab="+tabId+"&grp="+gadgetGroup,
		async : false			
	}).responseText;
	
	response = removeCarriageReturns(response);
	return response == "true" || response == "1";
     }
	


GadgetRepoService.deleteComment = function addComment(commentId) {
	var response = jQuery.ajax( {
		type : "POST",
		url	: "GadgetRepo-ajaxprocessor.jsp",
		data : "func=deleteComment&commentId=" + commentId,
		async : false			
	}).responseText;
	
	response = removeCarriageReturns(response);
	return response == "true" || response == "1";
	

}

GadgetRepoService.makeDefault = function makeDefault(path, isMakeDef) {
	 if (checkSession()) {
    var response = jQuery.ajax( {
		type : "POST",
		url	: "GadgetRepo-ajaxprocessor.jsp",
		data : "func=makeDef&gadgetPath=" + path + "&isMakeDefault=" + isMakeDef,
		async : false			
	}).responseText;
	
	response = removeCarriageReturns(response);
	return response == "true" || response == "1";
     }
}

GadgetRepoService.unsignedUserGadget = function unsignedUserGadget(path, isMakeUnUser) {
	 if (checkSession()) {
    var response = jQuery.ajax( {
		type : "POST",
		url	: "GadgetRepo-ajaxprocessor.jsp",
		data : "func=unsigned&gadgetPath=" + path + "&isUnsigned=" + isMakeUnUser,
		async : false			
	}).responseText;
	
	response = removeCarriageReturns(response);
	return response == "true" || response == "1";
}
}

GadgetRepoService.deleteGadgetImage = function deleteGadgetImage(path) {
	var response = jQuery.ajax( {
		type : "POST",
		url	: "GadgetRepo-ajaxprocessor.jsp",
		data : "func=delimage&gadgetPath=" + path,
		async : false
	}).responseText;

	response = removeCarriageReturns(response);
	return response == "true" || response == "1";
}