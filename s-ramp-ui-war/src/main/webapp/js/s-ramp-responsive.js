var prevMobile = false;

$(document).ready(function() {
	$(window).resize(function(e) { onResize(); });
	onResize();
});

/**
 * Called when the browser is resized in such a way that it transitions
 * from a mobile layout to a desktop layout.
 */
function onSwitchToDesktop() {
	$('#sramp-filters').removeAttr('style');
}

/**
 * Called when the browser is resized in such a way that it transitions
 * from a desktop layout to a mobile layout.
 */
function onSwitchToMobile() {
	$('#sramp-filters').collapse();
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
	return $(window).width() <= 767;
}
