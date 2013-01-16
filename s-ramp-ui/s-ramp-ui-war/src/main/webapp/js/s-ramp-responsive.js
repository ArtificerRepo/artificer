var prevMobile = false;

$(document).ready(function() {
	$(window).resize(function(e) { onResize(); });
	onResize();
	if (!isMobile()) {
		$('#accordion-filters-core').collapse();
	}
});

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
	return $(window).width() <= 767;
}
