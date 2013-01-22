var prevMobile = false;
var fileInputSupported = true;

/**
 * Do some work when the page first loads.
 */
$(document).ready(function() {
	$(window).resize(function(e) { onResize(); });
	fileInputSupported = detectFileInputSupport();
	if (isFileInputSupported()) {
		$('body').addClass('fileupload');
	} else {
		$('body').addClass('no-fileupload');
	}
	onResize();
	if (!isMobile()) {
		$('.collapse-on-mobile-load').collapse();
	}
});

/**
 * Returns true if uploading files is supported by the current device.
 * @returns {Boolean}
 */
function isFileInputSupported() {
	return fileInputSupported;
}

/**
 * Called when the browser is resized in such a way that it transitions
 * from a mobile layout to a desktop layout.
 */
function onSwitchToDesktop() {
	$('.collapse-on-mobile').collapse('show');
}

/**
 * Called when the browser is resized in such a way that it transitions
 * from a desktop layout to a mobile layout.
 */
function onSwitchToMobile() {
	$('.collapse-on-mobile').collapse('hide');
}

/**
 * Callback registered with the browser's resize event.
 */
function onResize() {
	var mobile = isMobile();
	if (prevMobile && !mobile) {
		onSwitchToDesktop();
	} else if (!prevMobile && mobile) {
		onSwitchToMobile();
	}
	prevMobile = mobile;
}

/**
 * Returns true if the current window is a compatible "mobile" size.
 * @returns {Boolean}
 */
function isMobile() {
	return $(window).width() < 768;
}

/**
 * Detect whether this device supports file uploading.  For reference:
 * @see http://viljamis.com/blog/2012/file-upload-support-on-mobile/
 */
function detectFileInputSupport() {
	// Handle devices which falsely report support
	if (navigator.userAgent.match(/(Android (1.0|1.1|1.5|1.6|2.0|2.1))|(Windows Phone (OS 7|8.0)|(XBLWP)|(ZuneWP)|(w(eb)?OSBrowser)|(webOS)|Pre\/1.2|Kindle\/(1.0|2.0|2.5|3.0))/)) {
		return false;
	}
	// Create test element
	var el = document.createElement("input");
	el.type = "file";
	return !el.disabled;
}
