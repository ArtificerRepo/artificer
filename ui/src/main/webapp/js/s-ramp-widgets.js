function expandAllTreeNodes(context) {
	$(context).find('ul.collapse').collapse({ toggle : false });
	$(context).find('ul.collapse').collapse('show');
	$(context).find('button.tree').each(function() {
		$(this).html('&minus;');
	});
}
function collapseAllTreeNodes(context) {
	$(context).find('ul.collapse').collapse({ toggle : false });
	$(context).find('ul.collapse').collapse('hide');
	$(context).find('button.tree').each(function() {
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
