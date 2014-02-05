/**
 * Timer 
 */
wso2vis.u.Timer = function(timerInterval) {
	this.timerInterval = timerInterval; // sets the interval

	var timerID = 0;
	var timerRunning = false;
	var thisObject = null;
	
	this.updateInterval = function(interval) {
		if ((interval > 0) && (interval != this.timerInterval)) {
			this.timerInterval = interval;
			this.stopTimer();
			this.startTimer();
		}
	};

	this.startTimer = function(immediate) {
        this.stopTimer();
		if (timerInterval > 0) {
			this.timerRunning = true;
	
			thisObject = this;
			if (thisObject.timerRunning)
			{
                if(immediate) thisObject.tick();
				thisObject.timerID = setInterval(
					function()
					{
						thisObject.tick();
					}, 
					thisObject.timerInterval);
			}
		}
	};
	
	this.stopTimer = function() {
		if (this.timerRunning)
			clearInterval(thisObject.timerID);
		this.timerRunning = false;        
	};
	
	this.tick = function() {	
	};
};

