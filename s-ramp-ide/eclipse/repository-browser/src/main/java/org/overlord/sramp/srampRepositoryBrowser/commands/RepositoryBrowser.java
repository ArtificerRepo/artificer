package org.overlord.sramp.srampRepositoryBrowser.commands;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import org.overlord.sramp.srampRepositoryBrowser.dialogs.ViewMain;

/**
 * Main class representing Repository Browser View.
 * 
 * @author Jan Bouska
 * 
 * 
 */
public class RepositoryBrowser extends ViewPart {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "org.overlord.sramp.srampRepositoryBrowser.views.RepositoryBrowser";
	private ScrolledComposite ui;
	private ViewMain mainView;

	@Override
	public void createPartControl(Composite parent) {

		ui = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
		ui.setMinHeight(350);
		ui.setMinWidth(970);
		ui.setExpandHorizontal(true);
		ui.setExpandVertical(true);
		mainView = new ViewMain(ui, SWT.NONE);
		ui.setContent(mainView);

	}

	/**
	 * 
	 * @return main class with UI
	 */
	public ViewMain getMainView() {
		return mainView;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		ui.setFocus();
	}

}
