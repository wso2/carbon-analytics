/*
 statistics.js contains scripts pertaining to handle @server_short_name@ statistics data
 */

// CEP
var graphRequest;
var graphResponse;
//var graphMainRequest;
//var graphMainResponse;
//var graphTopicRequest= {};
//var graphTopicResponse= {};


function initStats(cepXScale) {
    if (cepXScale != null) {
        initGraphs(cepXScale);
    }
}

function isNumeric(sText){
    var validChars = "0123456789.";
    var isNumber = true;
    var character;
    for (var i = 0; i < sText.length && isNumber == true; i++) {
        character = sText.charAt(i);
        if (validChars.indexOf(character) == -1) {
            isNumber = false;
        }
    }
    return isNumber;
}

function initGraphs(cepXScale) {
    if (cepXScale < 1 || !isNumeric(cepXScale)) {
        return;
    }
    graphRequest = new eventStatsGraph(cepXScale);
    graphResponse = new eventStatsGraph(cepXScale);
}




