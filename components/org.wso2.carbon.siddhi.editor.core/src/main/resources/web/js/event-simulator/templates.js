

var singleTemplate =
    '<form role="form" id="singleEventForm_{{dynamicId}}" class="singleEventForm"  data-id="{{dynamicId}}"' +
    ' data-toggle="loading" data-loading-style="overlay" data-loading-text="Sending event"> ' +
    '   <div class="form-group"> ' +
    '       <label for="single_executionPlanName_{{dynamicId}}">' +
    '           Execution Plan Name ' +
    '       </label> ' +
    '       <select id="single_executionPlanName_{{dynamicId}}" name="single_executionPlanName_{{dynamicId}}"' +
    '       class="form-control"> ' +
    '       </select> ' +
    '       <div id ="single_executionPlanName_{{dynamicId}}_mode"> ' +
    '       </div>' +
    '   </div> ' +
    '   <div class="form-group"> ' +
    '       <label for="single_streamName_{{dynamicId}}">' +
    '           Stream Name ' +
    '       </label> ' +
    '       <select id="single_streamName_{{dynamicId}}" name="single_streamName_{{dynamicId}}" ' +
    '       class="form-control"> ' +
    '       </select> ' +
    '       </div> ' +
    '   <div class="form-group"> ' +
    '       <label for="single_timestamp_{{dynamicId}}">' +
    '           Timestamp<span class="optional"> - optional</span></label> ' +
    '       <input type="text" class="form-control" id="single_timestamp_{{dynamicId}}"' +
    '       name="single_timestamp_{{dynamicId}}"> ' +
    '   </div> ' +
    '   <div class="form-group" id="single_attributes_{{dynamicId}}"> ' +
    '   </div> ' +
    '   <div class="form-group row" id="single_runDebugButtons_{{dynamicId}}"> ' +
    '   </div> ' +
    '   <div class="form-group"> ' +
    '       <button type="submit" class="btn btn-default" id="single_sendEvent_{{dynamicId}}" ' +
    '       name="single_sendEvent_{{dynamicId}}">' +
    '           Send ' +
    '       </button> ' +
    '   </div> ' +
    '</form>';

var indexTemplate =
    '<div class="sidebarTemp"> ' +
    '   <!-- Nav tabs --> ' +
    '   <ul class="nav nav-tabs" role="tablist"> ' +
    '       <li role="presentation" class="active"> ' +
    '           <a href="#singleEventConfigs" aria-controls="singleEventConfigs" role="tab" data-toggle="tab">' +
    '           Single ' +
    '           </a> ' +
    '       </li> ' +
    '   </ul> ' +
    '<!-- Tab panes --> ' +
    '   <div class="tab-content"> ' +
    '       <div role="tabpanel" class="tab-pane fade in active" id="singleEventConfigs"> ' +
    '           <div class="content"> ' +
    '               <h3>Single Simulation</h3> ' +
    '               <ul id="singleEventConfigTab" class="nav nav-tabs single_configs" role="tablist"> ' +
    '                   <li role="presentation" class="active" id="single_ListItem_1" data-id="1"> ' +
    '                       <a href="#singleEventContent_parent_1" id="singleEventConfig_1"' +
    '                       aria-controls="singleEventConfigs" role="tab" data-toggle="tab">' +
    '                       S1 ' +
    '                       </a> ' +
    '                   </li> ' +
    '                   <li id="addSingleEventForm" role="presentation"> ' +
    '                       <a>+</a> ' +
    '                   </li> ' +
    '               </ul> ' +
    '               <div id="singleEventConfigTabContent" class="tab-content"> ' +
    '                   <div role="tabpanel" class="tab-pane active" id="singleEventContent_parent_1"> ' +
    '                       <div class="content" id="singleEventContent_1"> ' +
    '                           <form role="form" id="singleEventForm_1" class="singleEventForm"  ' +
    '                           data-id="1" data-toggle="loading" data-loading-style="overlay"' +
    '                           data-loading-text="Sending event">' +
    '                               <div class="form-group"> ' +
    '                                   <label for="single_executionPlanName_1">' +
    '                                       Execution Plan Name ' +
    '                                   </label> ' +
    '                                   <select id="single_executionPlanName_1" name="single_executionPlanName_1"' +
    '                                    class="form-control"> ' +
    '                                   </select> ' +
    '                                   <div id ="single_executionPlanName_1_mode"> ' +
    '                                   </div>' +
    '                               </div> ' +
    '                               <div class="form-group"> ' +
    '                                   <label for="single_streamName_1">' +
    '                                   Stream Name ' +
    '                                   </label> ' +
    '                                   <select id="single_streamName_1" name="single_streamName_1" ' +
    '                                   class="form-control"> ' +
    '                                   </select> ' +
    '                               </div>' +
    '                               <div class="form-group"> ' +
    '                                   <label for="single_timestamp_1">' +
    '                                   Timestamp<span class="optional"> - optional</span> ' +
    '                                   </label> ' +
    '                                   <input type="text" class="form-control" id="single_timestamp_1" ' +
    '                                   name="single_timestamp_1"> ' +
    '                               </div> ' +
    '                               <div class="form-group" id="single_attributes_1">' +
    '                               </div> ' +
    '                               <div class="form-group row" id="single_runDebugButtons_1"> ' +
    '                               </div> ' +
    '                               <div class="form-group"> ' +
    '                                   <button type="submit" class="btn btn-default" id="single_sendEvent_1"' +
    '                                    name="single_sendEvent_1">' +
    '                                       Send ' +
    '                                   </button> ' +
    '                               </div> ' +
    '                           </form> ' +
    '                       </div> ' +
    '                   </div> ' +
    '               </div> ' +
    '           </div> ' +
    '       </div> ' +
    '   </div> ' +
    '</div>';

String.prototype.replaceAll = function (search, replacement) {
    var target = this;
    return target.replace(new RegExp(search, 'g'), replacement);
};
