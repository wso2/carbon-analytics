/**
 * Constructs a new DateRange.
 * @class Represents a DateRange.
 * @augments wso2vis.c.Control
 * @constructor
 */
wso2vis.c.DateRange = function() {
    this.currentTimestamp = 0;
    this.firstStart = true; // user to hide the date selection box in the first start
    this.startHr;
    this.endHr = 0;
    this.startEndHrState = "init";
    this.pageMode = 'hour'; //This is a flag set to keep the page mode (hour,day,or month);

    wso2vis.c.Control.call(this);

    this.showHours(true);
    this.showDays(true);
    this.showMonths(true);
};

wso2vis.extend(wso2vis.c.DateRange, wso2vis.c.Control);

/* Define all properties. */
wso2vis.c.DateRange.prototype.property("showHours");
wso2vis.c.DateRange.prototype.property("showDays");
wso2vis.c.DateRange.prototype.property("showMonths");

wso2vis.c.DateRange.prototype.onApplyClicked = function(mode, date1, date2) {
}

wso2vis.c.DateRange.prototype.create = function() {
    /*YAHOO.util.Event.onDOMReady(function() {*/
    //variables to keep the date interval start and end
    var styleTabMonthDisplay = '';
    var styleTabDayDisplay = '';
    var styleTabHourDisplay = '';
    var styleMonthDisplay = '';
    var styleDayDisplay = '';
    var styleHourDisplay = '';

    this.pageMode = 'none';

    if (this.showMonths()) {
        this.pageMode = 'month';
        styleMonthDisplay = '';
        styleDayDisplay = 'style="display:none"';
        styleHourDisplay = 'style="display:none"';
    }
    else {
        styleTabMonthDisplay = 'style="display:none"';
        styleMonthDisplay = 'style="display:none"';
    }

    if (this.showDays()) {
        this.pageMode = 'day';
        styleMonthDisplay = 'style="display:none"';
        styleDayDisplay = '';
        styleHourDisplay = 'style="display:none"';
    }
    else {
        styleTabDayDisplay = 'style="display:none"';
        styleDayDisplay = 'style="display:none"';
    }

    if (this.showHours()) {
        this.pageMode = 'hour';
        styleMonthDisplay = 'style="display:none"';
        styleDayDisplay = 'style="display:none"';
        styleHourDisplay = '';
    }
    else {
        styleTabHourDisplay = 'style="display:none"';
        styleHourDisplay = 'style="display:none"';
    }

    if (this.pageMode == 'none')
        return;

    var canvas = YAHOO.util.Dom.get(this.canvas());
    canvas.innerHTML = '<div style="height:40px;"><a style="cursor:pointer;" onClick="wso2vis.fn.toggleDateSelector(' + this.getID() + ')">' + 
                '<table class="time-header">' + 
                	'<tr>' + 
                    	'<td><span id="dateDisplay' + this.getID() + '"></span><img src="../images/down.png" alt="calendar" align="middle" style="margin-bottom: 4px;margin-left:5px;margin-right:5px" id="imgObj' + this.getID() + '"/></td>' +
                    '</tr>' +
                '</table>' +
            '</a></div>' +
            '<div class="dates-selection-Box yui-skin-sam" style="display:none" id="datesSelectionBox' + this.getID() + '">' +
            	'<div class="dates" style="float:left">' +
                    '<table>' +
                        '<tr>' +
                            '<td>Range</td>' +
                            '<td><input type="text" name="in" id="in' + this.getID() + '" class="in"></td>' +
                            '<td> -</td>' +
                            '<td><input type="text" name="out" id="out' + this.getID() + '" class="out"></td>' +
                        '</tr>' +
                    '</table>' +
                '</div>' +
                '<ul class="dates-types" id="datesTypes' + this.getID() + '">' +
                    '<li><a class="nor-right" id="datesSelectionBox' + this.getID() + 'MonthTab" onClick="wso2vis.fn.setPageMode(\'month\',this,' + this.getID() + ')" ' + styleTabMonthDisplay + '>Month</a></li>' +
                    '<li><a class="nor-rep" id="datesSelectionBox' + this.getID() + 'DayTab" onClick="wso2vis.fn.setPageMode(\'day\',this,' + this.getID() + ')" ' + styleTabDayDisplay + '>Day</a></li>' +
                    '<li><a class="sel-left" id="datesSelectionBox' + this.getID() + 'HourTab" onClick="wso2vis.fn.setPageMode(\'hour\',this,' + this.getID() + ')" ' + styleTabHourDisplay + '>Hour</a></li>' +
                '</ul>' +
                '<div class="dateBox-main" id="dateBox-main' + this.getID() + '"><div id="cal1Container' + this.getID() + '" ' + styleDayDisplay + '></div></div>' +
                '<div class="timeBox-main" id="timeBox-main' + this.getID() + '" ' + styleHourDisplay + '></div>' +
                '<div class="monthBox-main" id="monthBox-main' + this.getID() + '" ' + styleMonthDisplay + '></div>' +
                '<div style="clear:both;padding-top:5px;"><input type="button" value="Apply" onClick="wso2vis.fn.updatePage(' + this.getID() + ', true);wso2vis.fn.toggleDateSelector(' + this.getID() + ')" class="button"/></div>' +
            '</div>' +
            '<div style="clear:both"></div>';

    var d = new Date();
    var dateDisplay = YAHOO.util.Dom.get("dateDisplay" + this.getID());
    var inTxtTop = getStringMonth(d.getMonth()) + ' ' + d.getDate() + ',' + d.getFullYear();
    var outTxtTop = "";
    var inTxt = YAHOO.util.Dom.get("in" + this.getID()),
            outTxt = YAHOO.util.Dom.get("out" + this.getID()),
            inDate, outDate, interval;

    inTxt.value = "";
    outTxt.value = "";

    var d = new Date();
    this.currentTimestamp = d.getTime();

    var cal = new YAHOO.example.calendar.IntervalCalendar("cal1Container" + this.getID(), {pages:3,width:60});
    var copyOfID = this.getID();

    cal.selectEvent.subscribe(function() {
        interval = this.getInterval();
        if (interval.length == 2) {
            inDate = interval[0];
            inTxt.value = (inDate.getMonth() + 1) + "/" + inDate.getDate() + "/" + inDate.getFullYear();
            inTxtTop = getStringMonth(inDate.getMonth()) + ' ' + inDate.getDate() + ',' + inDate.getFullYear();
            wso2vis.fn.getControlFromID(copyOfID).startHr = inDate.getTime();
            if (interval[0].getTime() != interval[1].getTime()) {
                outDate = interval[1];
                outTxt.value = (outDate.getMonth() + 1) + "/" + outDate.getDate() + "/" + outDate.getFullYear();
                outTxtTop = getStringMonth(outDate.getMonth()) + ' ' + outDate.getDate() + ',' + outDate.getFullYear();
                wso2vis.fn.getControlFromID(copyOfID).endHr = outDate.getTime();
            } else {
                outTxt.value = "";
                outTxtTop = "";
            }
        }
        dateDisplay.innerHTML = inTxtTop + " - " + outTxtTop;
    }, cal, true);

    cal.render();
    genTimeHours(this.getID());
    genTimeMonths(this.getID());

    //Set the time ranges
    var tmpString;
    if (this.showMonths()) {
        var twoMonthLate = new Date(getPrevMonth(getPrevMonth(d.getTime()))); //Get the prev month
        tmpString = getStringMonth(twoMonthLate.getMonth()) + ' ' + twoMonthLate.getDate() + ',' + twoMonthLate.getFullYear();
        tmpString += ' -> ' + getStringMonth(d.getMonth()) + ' ' + d.getDate() + ',' + d.getFullYear();
        wso2vis.fn.getControlFromID(this.getID()).startHr = twoMonthLate.getTime();
        wso2vis.fn.getControlFromID(this.getID()).endHr = d.getTime();
    }

    if (this.showDays()) {
        var sevenDayLate = new Date(d.getTime() - 7 * oneDay); //Get the yesterdays midnight
        tmpString = getStringMonth(sevenDayLate.getMonth()) + ' ' + sevenDayLate.getDate() + ',' + sevenDayLate.getFullYear();
        tmpString += ' -> ' + getStringMonth(d.getMonth()) + ' ' + d.getDate() + ',' + d.getFullYear();
        wso2vis.fn.getControlFromID(this.getID()).startHr = sevenDayLate.getTime();
        wso2vis.fn.getControlFromID(this.getID()).endHr = d.getTime();
    }

    if (this.showHours()) {
        wso2vis.fn.setPageMode('hour', document.getElementById('datesSelectionBox' + this.getID() + 'HourTab'), this.getID());
    }
    else if (this.showDays()) {
        wso2vis.fn.setPageMode('day', document.getElementById('datesSelectionBox' + this.getID() + 'DayTab'), this.getID());
    }
    else if (this.showMonths()) {
        wso2vis.fn.setPageMode('month', document.getElementById('datesSelectionBox' + this.getID() + 'MonthTab'), this.getID());
    }

    wso2vis.fn.updatePage(this.getID());
};


function genHourTable(timestamp, id) {
    var timeBoxMain = document.getElementById('timeBox-main' + id);
    var d = new Date(timestamp);
    var externDiv = document.createElement("DIV");
    YAHOO.util.Dom.addClass(externDiv, 'timeBox-sub');
    var insideStr = '<div class="date-title">' + getStringMonth(d.getMonth()) + ' ' + d.getDate() + ',' + d.getFullYear() + '</div>' +
            '<div class="timeBox-Wrapper">' +
            '<ul>';
    for (var i = 0; i <= 23; i++) {
        insideStr += '<li title="' + (timestamp + i * oneHour) + '" onclick="wso2vis.fn.setHourRange(this, ' + id + ')">' + i + '</li>';
    }
    insideStr += '</ul></div>';
    externDiv.innerHTML = insideStr;

    timeBoxMain.appendChild(externDiv);
}

function genTimeHours(id) {
    var clearToday = getClearTimestamp(wso2vis.fn.getControlFromID(id).currentTimestamp);


    //set the buttons
    var timeBoxMain = document.getElementById('timeBox-main' + id);
    var navButtons = '<div class="navButtons"><a class="left" onclick="wso2vis.fn.navHour(\'left\',' + id + ')"><<</a><a class="right" onclick="wso2vis.fn.navHour(\'right\',' + id + ')">>></a></div>';
    var navButtonDiv = document.createElement("DIV");
    navButtonDiv.innerHTML = navButtons;
    timeBoxMain.innerHTML = "";
    timeBoxMain.appendChild(navButtonDiv);

    genHourTable(clearToday - oneDay * 2, id);
    genHourTable(clearToday - oneDay, id);
    genHourTable(clearToday, id);
}

/**
 * Creates Month range selector.
 * @param {string} timestamp the time stamp.
 * @param {number} id .
 */
function genMonthTable(timestamp, id) {
    var timeBoxMain = document.getElementById('monthBox-main' + id);
    var d = new Date(timestamp);
    var externDiv = document.createElement("DIV");
    YAHOO.util.Dom.addClass(externDiv, 'monthBox-sub');
    var insideStr = '<div class="date-title">' + d.getFullYear() + '</div>' +
            '<div class="monthBox-Wrapper">' +
            '<ul>';
    var iTime = timestamp;
    for (var i = 0; i < m_names.length; i++) {
        insideStr += '<li title="' + iTime + '" onclick="wso2vis.fn.setMonthRange(this, ' + id + ')">' + m_names[i] + '</li>';
        iTime = getNextMonth(iTime);
    }
    insideStr += '</ul></div>';
    externDiv.innerHTML = insideStr;

    timeBoxMain.appendChild(externDiv);
}

/**
 * @param {number} id .
 */
function genTimeMonths(id) {
    //set the buttons
    var timeBoxMain = document.getElementById('monthBox-main' + id);
    var navButtons = '<div class="navButtons"><a class="left" onclick="wso2vis.fn.navMonth(\'left\',' + id + ')"><<</a><a class="right" onclick="wso2vis.fn.navMonth(\'right\', ' + id + ')">>></a></div>';
    var navButtonDiv = document.createElement("DIV");
    navButtonDiv.innerHTML = navButtons;
    timeBoxMain.innerHTML = "";
    timeBoxMain.appendChild(navButtonDiv);
    var jan1st = new Date((new Date(wso2vis.fn.getControlFromID(id).currentTimestamp)).getFullYear(), 0, 1);
    genMonthTable(getPrevYear(wso2vis.fn.getControlFromID(id).currentTimestamp), id);
    genMonthTable(jan1st.getTime(), id);
}

function getNextYear(timestamp) {
    now = new Date(timestamp);
    var current;
    current = new Date(now.getFullYear() + 1, 0, 1);
    return current.getTime();
}

function getPrevYear(timestamp) {
    now = new Date(timestamp);
    var current;
    current = new Date(now.getFullYear() - 1, 0, 1);
    return current.getTime();
}

//util function
function getStringMonth(num) {
    var m_names = new Array("January", "February", "March",
            "April", "May", "June", "July", "August", "September",
            "October", "November", "December");

    return m_names[num];
}

function getClearTimestamp(timestamp) {
    var d = new Date(timestamp);
    var dateClear = new Date(Date.UTC(d.getFullYear(), d.getMonth(), d.getDate()));
    return (dateClear.getTime() + d.getTimezoneOffset() * 1000 * 60);
}

function formatDate(inDate) {
    var year = inDate.split(" ")[0].split("-")[0];
    var month = inDate.split(" ")[0].split("-")[1];
    var day = inDate.split(" ")[0].split("-")[2];
    var hour = inDate.split(" ")[1].split(":")[0];

    return m_names[month - 1] + " " + day + "-" + hour + ":00";
}


/**
 * YAHOO IntervalCalender.
 */
function IntervalCalendar(container, cfg) {
    this._iState = 0;
    cfg = cfg || {};
    cfg.multi_select = true;
    IntervalCalendar.superclass.constructor.call(this, container, cfg);

    this.beforeSelectEvent.subscribe(this._intervalOnBeforeSelect, this, true);
    this.selectEvent.subscribe(this._intervalOnSelect, this, true);
    this.beforeDeselectEvent.subscribe(this._intervalOnBeforeDeselect, this, true);
    this.deselectEvent.subscribe(this._intervalOnDeselect, this, true);
}

IntervalCalendar._DEFAULT_CONFIG = YAHOO.widget.CalendarGroup._DEFAULT_CONFIG;

YAHOO.lang.extend(IntervalCalendar, YAHOO.widget.CalendarGroup, {
    _dateString : function(d) {
        var a = [];
        a[this.cfg.getProperty(IntervalCalendar._DEFAULT_CONFIG.MDY_MONTH_POSITION.key) - 1] = (d.getMonth() + 1);
        a[this.cfg.getProperty(IntervalCalendar._DEFAULT_CONFIG.MDY_DAY_POSITION.key) - 1] = d.getDate();
        a[this.cfg.getProperty(IntervalCalendar._DEFAULT_CONFIG.MDY_YEAR_POSITION.key) - 1] = d.getFullYear();
        var s = this.cfg.getProperty(IntervalCalendar._DEFAULT_CONFIG.DATE_FIELD_DELIMITER.key);
        return a.join(s);
    },

    _dateIntervalString : function(l, u) {
        var s = this.cfg.getProperty(IntervalCalendar._DEFAULT_CONFIG.DATE_RANGE_DELIMITER.key);
        return (this._dateString(l)
                + s + this._dateString(u));
    },

    getInterval : function() {
        // Get selected dates
        var dates = this.getSelectedDates();
        if (dates.length > 0) {
            // Return lower and upper date in array
            var l = dates[0];
            var u = dates[dates.length - 1];
            return [l, u];
        }
        else {
            // No dates selected, return empty array
            return [];
        }
    },

    setInterval : function(d1, d2) {
        // Determine lower and upper dates
        var b = (d1 <= d2);
        var l = b ? d1 : d2;
        var u = b ? d2 : d1;
        // Update configuration
        this.cfg.setProperty('selected', this._dateIntervalString(l, u), false);
        this._iState = 2;
    },

    resetInterval : function() {
        // Update configuration
        this.cfg.setProperty('selected', [], false);
        this._iState = 0;
    },

    _intervalOnBeforeSelect : function(t, a, o) {
        // Update interval state
        this._iState = (this._iState + 1) % 3;
        if (this._iState == 0) {
            // If starting over with upcoming selection, first deselect all
            this.deselectAll();
            this._iState++;
        }
    },

    _intervalOnSelect : function(t, a, o) {
        // Get selected dates
        var dates = this.getSelectedDates();
        if (dates.length > 1) {
            /* If more than one date is selected, ensure that the entire interval
             between and including them is selected */
            var l = dates[0];
            var u = dates[dates.length - 1];
            this.cfg.setProperty('selected', this._dateIntervalString(l, u), false);
        }
        // Render changes
        this.render();
    },

    _intervalOnBeforeDeselect : function(t, a, o) {
        if (this._iState != 0) {
            /* If part of an interval is already selected, then swallow up
             this event because it is superfluous (see _intervalOnDeselect) */
            return false;
        }
    },

    _intervalOnDeselect : function(t, a, o) {
        if (this._iState != 0) {
            // If part of an interval is already selected, then first deselect all
            this._iState = 0;
            this.deselectAll();

            // Get individual date deselected and page containing it
            var d = a[0][0];
            var date = YAHOO.widget.DateMath.getDate(d[0], d[1] - 1, d[2]);
            var page = this.getCalendarPage(date);
            if (page) {
                // Now (re)select the individual date
                page.beforeSelectEvent.fire();
                this.cfg.setProperty('selected', this._dateString(date), false);
                page.selectEvent.fire([d]);
            }
            // Swallow up since we called deselectAll above
            return false;
        }
    }
});

YAHOO.namespace("example.calendar");
YAHOO.example.calendar.IntervalCalendar = IntervalCalendar;

/* <end> YAHOO IntervalCalender */


function gotoInitMode(id) {
    var allDivs1 = document.getElementById("timeBox-main" + id).getElementsByTagName("*");
    var allDivs2 = document.getElementById("monthBox-main" + id).getElementsByTagName("*");

    for (i = 0; i < allDivs1.length; i++) {
        YAHOO.util.Dom.removeClass(allDivs1[i], 'selected');
    }
    for (i = 0; i < allDivs2.length; i++) {
        YAHOO.util.Dom.removeClass(allDivs2[i], 'selected');
    }
    wso2vis.fn.getControlFromID(id).startEndHrState = "init";
}

function getNextMonth(timestamp) {
    now = new Date(timestamp);
    var current;
    if (now.getMonth() == 11) {
        current = new Date(now.getFullYear() + 1, 0, 1);
    } else {
        current = new Date(now.getFullYear(), now.getMonth() + 1, 1);
    }
    return current.getTime();
}

function getPrevMonth(timestamp) {
    now = new Date(timestamp);
    var current;
    if (now.getMonth() == 0) {
        current = new Date(now.getFullYear() - 1, 11, 1);
    } else {
        current = new Date(now.getFullYear(), now.getMonth() - 1, 1);
    }
    return current.getTime();
}

var oneDay = 1000 * 60 * 60 * 24;
var oneHour = 1000 * 60 * 60;
var m_names = new Array("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");

wso2vis.fn.setPageMode = function(mode, clickedObj, id) {
    var d = new Date();
    wso2vis.fn.getControlFromID(id).pageMode = mode;
    var dateDisplay = YAHOO.util.Dom.get("dateDisplay" + id);
    var allObjs = document.getElementById("datesTypes" + id).getElementsByTagName("*");
    for (var i = 0; i < allObjs.length; i++) {
        if (YAHOO.util.Dom.hasClass(allObjs[i], "sel-left")) {
            YAHOO.util.Dom.removeClass(allObjs[i], "sel-left");
            YAHOO.util.Dom.addClass(allObjs[i], "nor-left");
        }
        if (YAHOO.util.Dom.hasClass(allObjs[i], "sel-right")) {
            YAHOO.util.Dom.removeClass(allObjs[i], "sel-right");
            YAHOO.util.Dom.addClass(allObjs[i], "nor-right");
        }
        if (YAHOO.util.Dom.hasClass(allObjs[i], "sel-rep")) {
            YAHOO.util.Dom.removeClass(allObjs[i], "sel-rep");
            YAHOO.util.Dom.addClass(allObjs[i], "nor-rep");
        }
    }
    var timeBoxMain = document.getElementById('timeBox-main' + id);
    var cal1Container = document.getElementById('cal1Container' + id);
    var monthBoxMain = document.getElementById('monthBox-main' + id);
    gotoInitMode(id);
    if (wso2vis.fn.getControlFromID(id).pageMode == 'hour') {
        timeBoxMain.style.display = '';
        cal1Container.style.display = 'none';
        monthBoxMain.style.display = 'none';
        YAHOO.util.Dom.removeClass(clickedObj, "nor-left");
        YAHOO.util.Dom.addClass(clickedObj, "sel-left");
        if (wso2vis.fn.getControlFromID(id).startEndHrState == 'init') {
            var hourLate = new Date(d.getTime() - oneHour * 8);
            var tmpString = getStringMonth(hourLate.getMonth()) + ' ' + hourLate.getDate() + ',' + hourLate.getFullYear() + ' - <span class="hourStrong">' + hourLate.getHours() + ':00</span>';
            tmpString += ' -> ' + getStringMonth(d.getMonth()) + ' ' + d.getDate() + ',' + d.getFullYear() + ' - <span class="hourStrong">' + d.getHours() + ':00</span>';
            dateDisplay.innerHTML = tmpString;
        }
        wso2vis.fn.updatePage(id);
    }
    if (wso2vis.fn.getControlFromID(id).pageMode == 'day') {
        d = new Date(d.getFullYear(), d.getMonth(), d.getDate(), 0, 0, 0);
        timeBoxMain.style.display = 'none';
        monthBoxMain.style.display = 'none';
        cal1Container.style.display = '';
        YAHOO.util.Dom.removeClass(clickedObj, "nor-rep");
        YAHOO.util.Dom.addClass(clickedObj, "sel-rep");
        if (wso2vis.fn.getControlFromID(id).startEndHrState == 'init') {
            var sevenDayLate = new Date(d.getTime() - 7 * oneDay); //Get the yesterdays midnight
            var tmpString = getStringMonth(sevenDayLate.getMonth()) + ' ' + sevenDayLate.getDate() + ',' + sevenDayLate.getFullYear();
            tmpString += ' -> ' + getStringMonth(d.getMonth()) + ' ' + d.getDate() + ',' + d.getFullYear();
            dateDisplay.innerHTML = tmpString;
            wso2vis.fn.getControlFromID(id).startHr = sevenDayLate.getTime();
            wso2vis.fn.getControlFromID(id).endHr = d.getTime();
        }
        wso2vis.fn.updatePage(id);
    }
    if (wso2vis.fn.getControlFromID(id).pageMode == 'month') {
        d = new Date(d.getFullYear(), d.getMonth(), 1, 0, 0, 0);
        timeBoxMain.style.display = 'none';
        monthBoxMain.style.display = '';
        cal1Container.style.display = 'none';
        YAHOO.util.Dom.removeClass(clickedObj, "nor-right");
        YAHOO.util.Dom.addClass(clickedObj, "sel-right");
        if (wso2vis.fn.getControlFromID(id).startEndHrState == 'init') {
            var twoMonthLate = new Date(getPrevMonth(getPrevMonth(d.getTime()))); //Get the prev month
            var tmpString = getStringMonth(twoMonthLate.getMonth()) + ' ' + twoMonthLate.getDate() + ',' + twoMonthLate.getFullYear();
            tmpString += ' -> ' + getStringMonth(d.getMonth()) + ' ' + d.getDate() + ',' + d.getFullYear();
            dateDisplay.innerHTML = tmpString;
            wso2vis.fn.getControlFromID(id).startHr = twoMonthLate.getTime();
            wso2vis.fn.getControlFromID(id).endHr = d.getTime();
        }
        wso2vis.fn.updatePage(id);
    }
};

wso2vis.fn.updatePage = function(id, butt) {
    if (butt !== undefined)
        (wso2vis.fn.getControlFromID(id)).onApplyClicked(wso2vis.fn.getControlFromID(id).pageMode, wso2vis.fn.getControlFromID(id).startHr, wso2vis.fn.getControlFromID(id).endHr);

    if (!wso2vis.fn.getControlFromID(id).firstStart) {
        var now = new Date();
        if (wso2vis.fn.getControlFromID(id).startEndHrState == "init") {
            if (wso2vis.fn.getControlFromID(id).pageMode == "hour") {
                now = getClearTimestamp(now.getTime());
                wso2vis.fn.getControlFromID(id).startHr = now - 8 * oneHour;
                wso2vis.fn.getControlFromID(id).endHr = now;
            } else if (wso2vis.fn.getControlFromID(id).pageMode == "day") {
                now = new Date(now.getFullYear(), now.getMonth(), now.getDate());
                wso2vis.fn.getControlFromID(id).startHr = now.getTime() - oneDay * 7;
                wso2vis.fn.getControlFromID(id).endHr = now.getTime();
            } else if (wso2vis.fn.getControlFromID(id).pageMode == "month") {
                now = new Date(now.getFullYear(), now.getMonth(), 1);
                wso2vis.fn.getControlFromID(id).startHr = getPrevMonth(getPrevMonth(now.getTime()));
                wso2vis.fn.getControlFromID(id).endHr = now.getTime();
            }
        } else if (wso2vis.fn.getControlFromID(id).startEndHrState == "startSet") {
            wso2vis.fn.getControlFromID(id).endHr = wso2vis.fn.getControlFromID(id).startHr;
            if (wso2vis.fn.getControlFromID(id).pageMode == "hour") {
                wso2vis.fn.getControlFromID(id).startHr = wso2vis.fn.getControlFromID(id).startHr - 8 * oneHour;
            } else if (wso2vis.fn.getControlFromID(id).pageMode == "day") {
                wso2vis.fn.getControlFromID(id).startHr = wso2vis.fn.getControlFromID(id).startHr - oneDay * 7;
            } else if (wso2vis.fn.getControlFromID(id).pageMode == "month") {
                wso2vis.fn.getControlFromID(id).startHr = getPrevMonth(getPrevMonth(wso2vis.fn.getControlFromID(id).startHr));
            }
        } else if (wso2vis.fn.getControlFromID(id).startEndHrState == "endSet") {
        }
    }
    wso2vis.fn.getControlFromID(id).firstStart = false;
    //var startHrToSend = wso2vis.fn.getControlFromID(id).startHr-oneHour*1/2;
    //var endHrToSend = wso2vis.fn.getControlFromID(id).endHr-oneHour*1/2;
};

wso2vis.fn.setHourRange = function(theli, id) {
    var inTxt = YAHOO.util.Dom.get("in" + id),outTxt = YAHOO.util.Dom.get("out" + id),dateDisplay = YAHOO.util.Dom.get("dateDisplay" + id);
    var timestamp = theli.title;
    timestamp = parseInt(timestamp);
    var allDivs = document.getElementById("timeBox-main" + id).getElementsByTagName("*");

    if (wso2vis.fn.getControlFromID(id).startEndHrState == "init") {
        wso2vis.fn.getControlFromID(id).startHr = timestamp;
        for (var i = 0; i < allDivs.length; i++) {
            YAHOO.util.Dom.removeClass(allDivs[i], 'selected');
        }
        YAHOO.util.Dom.addClass(theli, 'selected');
        wso2vis.fn.getControlFromID(id).startEndHrState = "startSet";
        //set the headers and the text boxes
        var d = new Date(timestamp);
        inTxt.value = (d.getMonth() + 1) + "/" + d.getDate() + "/" + d.getFullYear() + " - " + d.getHours() + ":00";
        outTxt.value = '';
        var tmpString = getStringMonth(d.getMonth()) + ' ' + d.getDate() + ',' + d.getFullYear() + ' - <span class="hourStrong">' + d.getHours() + ':00</span>';
        dateDisplay.innerHTML = tmpString;

    } else if (wso2vis.fn.getControlFromID(id).startEndHrState == "endSet") {
        wso2vis.fn.getControlFromID(id).startHr = timestamp;
        for (var i = 0; i < allDivs.length; i++) {
            YAHOO.util.Dom.removeClass(allDivs[i], 'selected');
        }
        YAHOO.util.Dom.addClass(theli, 'selected');
        wso2vis.fn.getControlFromID(id).startEndHrState = "startSet";

        //set the headers and the text boxes
        var d = new Date(timestamp);
        inTxt.value = (d.getMonth() + 1) + "/" + d.getDate() + "/" + d.getFullYear() + " - " + d.getHours() + ":00";
        outTxt.value = '';
        var tmpString = getStringMonth(d.getMonth()) + ' ' + d.getDate() + ',' + d.getFullYear() + ' - <span class="hourStrong">' + d.getHours() + ':00</span>';
        dateDisplay.innerHTML = tmpString;

    } else if (wso2vis.fn.getControlFromID(id).startEndHrState == "startSet") {
        wso2vis.fn.getControlFromID(id).endHr = timestamp;
        if (wso2vis.fn.getControlFromID(id).startHr > wso2vis.fn.getControlFromID(id).endHr) {//Swap if the end time is smaller than start time
            var tmp = wso2vis.fn.getControlFromID(id).endHr;
            wso2vis.fn.getControlFromID(id).endHr = wso2vis.fn.getControlFromID(id).startHr;
            wso2vis.fn.getControlFromID(id).startHr = tmp;
        }
        for (var i = 0; i < allDivs.length; i++) {
            if (allDivs[i].title <= wso2vis.fn.getControlFromID(id).endHr && allDivs[i].title >= wso2vis.fn.getControlFromID(id).startHr) {
                YAHOO.util.Dom.addClass(allDivs[i], 'selected');
            }
            else {
                YAHOO.util.Dom.removeClass(allDivs[i], 'selected');
            }
        }
        wso2vis.fn.getControlFromID(id).startEndHrState = "endSet";

        //set the headers and the text boxes
        var dStart = new Date(wso2vis.fn.getControlFromID(id).startHr);
        var dEnd = new Date(wso2vis.fn.getControlFromID(id).endHr);
        inTxt.value = (dStart.getMonth() + 1) + "/" + dStart.getDate() + "/" + dStart.getFullYear() + " - " + dStart.getHours() + ":00";
        outTxt.value = (dEnd.getMonth() + 1) + "/" + dEnd.getDate() + "/" + dEnd.getFullYear() + " - " + dEnd.getHours() + ":00";
        var tmpString = getStringMonth(dStart.getMonth()) + ' ' + dStart.getDate() + ',' + dStart.getFullYear() + ' - <span class="hourStrong">' + dStart.getHours() + ':00</span>' + ' -> ' + getStringMonth(dEnd.getMonth()) + ' ' + dEnd.getDate() + ',' + dEnd.getFullYear() + ' - <span class="hourStrong">' + dEnd.getHours() + ':00</span>';
        dateDisplay.innerHTML = tmpString;
    }
};

wso2vis.fn.navHour = function(direction, id) {
    if (direction == "left") {
        wso2vis.fn.getControlFromID(id).currentTimestamp -= oneDay;
    } else if (direction == "right") {
        wso2vis.fn.getControlFromID(id).currentTimestamp += oneDay;
    }
    genTimeHours(id);
    var allDivs = document.getElementById("timeBox-main" + id).getElementsByTagName("*");
    if (wso2vis.fn.getControlFromID(id).startEndHrState == "startSet") {
        for (var i = 0; i < allDivs.length; i++) {
            if (allDivs[i].title == wso2vis.fn.getControlFromID(id).startHr) {
                YAHOO.util.Dom.addClass(allDivs[i], 'selected');
            }
        }
    } else if (wso2vis.fn.getControlFromID(id).startEndHrState == "endSet") {
        for (var i = 0; i < allDivs.length; i++) {
            if (allDivs[i].title <= wso2vis.fn.getControlFromID(id).endHr && allDivs[i].title >= wso2vis.fn.getControlFromID(id).startHr) {
                YAHOO.util.Dom.addClass(allDivs[i], 'selected');
            }
            else {
                YAHOO.util.Dom.removeClass(allDivs[i], 'selected');
            }
        }
    }
};

wso2vis.fn.navMonth = function(direction, id) {
    if (direction == "left") {
        wso2vis.fn.getControlFromID(id).currentTimestamp = getPrevYear(wso2vis.fn.getControlFromID(id).currentTimestamp);
    } else if (direction == "right") {
        wso2vis.fn.getControlFromID(id).currentTimestamp = getNextYear(wso2vis.fn.getControlFromID(id).currentTimestamp);
    }
    genTimeMonths(id);
    var allDivs = document.getElementById("monthBox-main" + id).getElementsByTagName("*");
    if (wso2vis.fn.getControlFromID(id).startEndHrState == "startSet") {
        for (var i = 0; i < allDivs.length; i++) {
            if (allDivs[i].title == wso2vis.fn.getControlFromID(id).startHr) {
                YAHOO.util.Dom.addClass(allDivs[i], 'selected');
            }
        }
    } else if (wso2vis.fn.getControlFromID(id).startEndHrState == "endSet") {
        for (var i = 0; i < allDivs.length; i++) {
            if (allDivs[i].title <= wso2vis.fn.getControlFromID(id).endHr && allDivs[i].title >= wso2vis.fn.getControlFromID(id).startHr) {
                YAHOO.util.Dom.addClass(allDivs[i], 'selected');
            }
            else {
                YAHOO.util.Dom.removeClass(allDivs[i], 'selected');
            }
        }
    }
};

wso2vis.fn.setMonthRange = function(theli, id) {
    var inTxt = YAHOO.util.Dom.get("in" + id),outTxt = YAHOO.util.Dom.get("out" + id),dateDisplay = YAHOO.util.Dom.get("dateDisplay" + id);
    var timestamp = theli.title;
    timestamp = parseInt(timestamp);
    var allDivs = document.getElementById("monthBox-main" + id).getElementsByTagName("*");

    if (wso2vis.fn.getControlFromID(id).startEndHrState == "init") {
        wso2vis.fn.getControlFromID(id).startHr = timestamp;
        for (var i = 0; i < allDivs.length; i++) {
            YAHOO.util.Dom.removeClass(allDivs[i], 'selected');
        }
        YAHOO.util.Dom.addClass(theli, 'selected');
        wso2vis.fn.getControlFromID(id).startEndHrState = "startSet";
        //set the headers and the text boxes
        var d = new Date(timestamp);
        inTxt.value = (d.getMonth() + 1) + "/" + d.getDate() + "/" + d.getFullYear();
        outTxt.value = '';
        var tmpString = getStringMonth(d.getMonth()) + ' ' + d.getDate() + ',' + d.getFullYear();
        dateDisplay.innerHTML = tmpString;

    } else if (wso2vis.fn.getControlFromID(id).startEndHrState == "endSet") {
        wso2vis.fn.getControlFromID(id).startHr = timestamp;
        for (var i = 0; i < allDivs.length; i++) {
            YAHOO.util.Dom.removeClass(allDivs[i], 'selected');
        }
        YAHOO.util.Dom.addClass(theli, 'selected');
        wso2vis.fn.getControlFromID(id).startEndHrState = "startSet";

        //set the headers and the text boxes
        var d = new Date(timestamp);
        inTxt.value = (d.getMonth() + 1) + "/" + d.getDate() + "/" + d.getFullYear();
        outTxt.value = '';
        var tmpString = getStringMonth(d.getMonth()) + ' ' + d.getDate() + ',' + d.getFullYear();
        dateDisplay.innerHTML = tmpString;

    } else if (wso2vis.fn.getControlFromID(id).startEndHrState == "startSet") {
        wso2vis.fn.getControlFromID(id).endHr = timestamp;
        if (wso2vis.fn.getControlFromID(id).startHr > wso2vis.fn.getControlFromID(id).endHr) {//Swap if the end time is smaller than start time
            var tmp = wso2vis.fn.getControlFromID(id).endHr;
            wso2vis.fn.getControlFromID(id).endHr = wso2vis.fn.getControlFromID(id).startHr;
            wso2vis.fn.getControlFromID(id).startHr = tmp;
        }
        for (var i = 0; i < allDivs.length; i++) {
            if (allDivs[i].title <= wso2vis.fn.getControlFromID(id).endHr && allDivs[i].title >= wso2vis.fn.getControlFromID(id).startHr) {
                YAHOO.util.Dom.addClass(allDivs[i], 'selected');
            }
            else {
                YAHOO.util.Dom.removeClass(allDivs[i], 'selected');
            }
        }
        wso2vis.fn.getControlFromID(id).startEndHrState = "endSet";

        //set the headers and the text boxes
        var dStart = new Date(wso2vis.fn.getControlFromID(id).startHr);
        var dEnd = new Date(wso2vis.fn.getControlFromID(id).endHr);
        inTxt.value = (dStart.getMonth() + 1) + "/" + dStart.getDate() + "/" + dStart.getFullYear();
        outTxt.value = (dEnd.getMonth() + 1) + "/" + dEnd.getDate() + "/" + dEnd.getFullYear();
        var tmpString = getStringMonth(dStart.getMonth()) + ' ' + dStart.getDate() + ',' + dStart.getFullYear() + ' -> ' + getStringMonth(dEnd.getMonth()) + ' ' + dEnd.getDate() + ',' + dEnd.getFullYear();
        dateDisplay.innerHTML = tmpString;
    }
};

wso2vis.fn.toggleDateSelector = function(id) {
    var anim = "";
    var attributes;
    var datesSelectionBox = document.getElementById('datesSelectionBox' + id);
    var imgObj = document.getElementById('imgObj' + id);
    if (datesSelectionBox.style.display == "none") {
        attributes = {
            opacity: { to: 1 },
            height: { to: 230 }
        };
        anim = new YAHOO.util.Anim('datesSelectionBox' + id, attributes);
        datesSelectionBox.style.display = "";
        imgObj.src = "../images/up.png";
    } else {
        attributes = {
            opacity: { to: 0 },
            height: { to: 0 }
        };
        anim = new YAHOO.util.Anim('datesSelectionBox' + id, attributes);

        anim.onComplete.subscribe(function() {
            datesSelectionBox.style.display = "none";
        }, datesSelectionBox);
        imgObj.src = "../images/down.png";
    }

    anim.duration = 0.3;
    anim.animate();
};