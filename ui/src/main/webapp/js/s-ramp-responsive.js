
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
	$('.open-on-desktop').slideDown();
	$('.close-on-desktop').slideUp();
};
/**
 * Called when the browser is resized in such a way that it transitions
 * from a desktop layout to a mobile layout.
 */
SRAMPi.prototype.onSwitchToMobile = function() {
	$('.close-on-mobile').slideUp();
	$('.open-on-mobile').slideDown();
};
/**
 * Called whenever the page is loaded or reconstructed.
 */
SRAMPi.prototype.onPageLoad = function() {
	if (this.isMobile()) {
		// Loading on a mobile device
		$('.open-on-mobile').slideDown();
		$('.close-on-mobile').slideUp();
		$('.open-on-mobile-load').slideDown();
		$('.close-on-mobile-load').slideUp();
	} else {
		// Loading on a desktop device
		$('.open-on-desktop').slideDown();
		$('.close-on-desktop').slideUp();
		$('.open-on-desktop-load').slideDown();
		$('.close-on-desktop-load').slideUp();
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
window.SRAMP = new SRAMPi();

/**
 * Do some work when the page first loads.
 */
$(document).ready(function() {
	$(window).resize(function(e) { window.SRAMP.onResize(); });
	if (window.SRAMP.isFileInputSupported()) {
		$('body').addClass('fileupload');
	} else {
		$('body').addClass('no-fileupload');
	}
	window.SRAMP.onPageLoad();
});

/**
 * jQuery based slide API
 */
$(document).on('click.slide.data-api', '[data-toggle=slide]', function (e) {
    var $this = $(this), href
      , target = $this.attr('data-target')
        || e.preventDefault()
        || (href = $this.attr('href')) && href.replace(/.*(?=#[^\s]+$)/, '') //??strip for ie7??
      , option = $(target).data('collapse') ? 'toggle' : $this.data();
    option;
    $(target).slideToggle('fast');
  });
