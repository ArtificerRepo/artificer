
/**
 * The global SRAMP namespace.
 */
function SRAMPi() {
	this.prevMobile = false;
	this.fileInputSupported = function() {
		// Handle devices which falsely report support
		if (navigator.userAgent.match(/(Android (1.0|1.1|1.5|1.6|2.0|2.1))|(Windows Phone (OS 7|8.0)|(XBLWP)|(ZuneWP)|(w(eb)?OSBrowser)|(webOS)|Pre\/1.2|Kindle\/(1.0|2.0|2.5|3.0))/)) {
			return false;
		}
		// Create test element
		var el = document.createElement("input");
		el.type = "file";
		return !el.disabled;
	}();
}
SRAMPi.prototype.isMobile = function() {
	return $(window).width() < 768;
};
SRAMPi.prototype.isFileInputSupported = function() {
	return this.fileInputSupported;
};
/**
 * Called when the browser is resized in such a way that it transitions
 * from a mobile layout to a desktop layout.
 */
SRAMPi.prototype.onSwitchToDesktop = function() {
	$('.collapse-on-mobile').collapse('show');
};
/**
 * Called when the browser is resized in such a way that it transitions
 * from a desktop layout to a mobile layout.
 */
SRAMPi.prototype.onSwitchToMobile = function() {
	$('.collapse-on-mobile').collapse('hide');
};
/**
 * Called whenever the page is loaded or reconstructed.
 */
SRAMPi.prototype.onPageLoad = function() {
	this.onResize();
	if (!this.isMobile()) {
		$('.collapse-on-mobile-load').collapse();
	}
};
/**
 * Called whenever the page is resized.
 */
SRAMPi.prototype.onResize = function() {
	var mobile = this.isMobile();
	if (this.prevMobile && !mobile) {
		this.onSwitchToDesktop();
	} else if (!this.prevMobile && mobile) {
		this.onSwitchToMobile();
	}
	this.prevMobile = mobile;
};

// The global SRAMP instance.
var SRAMP = new SRAMPi();

/**
 * Do some work when the page first loads.
 */
$(document).ready(function() {
	$(window).resize(function(e) { SRAMP.onResize(); });
	if (SRAMP.isFileInputSupported()) {
		$('body').addClass('fileupload');
	} else {
		$('body').addClass('no-fileupload');
	}
	SRAMP.onPageLoad();
});
