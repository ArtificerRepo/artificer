function expandAllTreeNodes(dialogId) {
	$('#' + dialogId).find('ul.collapse').collapse({ toggle : false });
	$('#' + dialogId).find('ul.collapse').collapse('show');
	$('#' + dialogId).find('button.tree').each(function() {
		$(this).html('&minus;');
	});
}
function collapseAllTreeNodes(dialogId) {
	$('#' + dialogId).find('ul.collapse').collapse({ toggle : false });
	$('#' + dialogId).find('ul.collapse').collapse('hide');
	$('#' + dialogId).find('button.tree').each(function() {
		$(this).html('+');
	});
}
function toggleTreeIcon(iconButton) {
	if (iconButton.text() == '+') {
		iconButton.html('&minus;');
	} else {
		iconButton.text('+');
	}
}
