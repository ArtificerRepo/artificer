package org.overlord.sramp.srampRepositoryBrowser.commands;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.ui.handlers.HandlerUtil;

import org.overlord.sramp.srampRepositoryBrowser.dialogs.ConnectToServerDialog;

/**
 * 
 * @author Jan Bouska Default handler for ConnectToServerComand
 * 
 */
public class ConnectToServerComand extends AbstractHandler {

	/**
	 * logger for ConnectToServerCommand class
	 */
	public static final Logger log = Logger
			.getLogger(ConnectToServerComand.class.getName());

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		if (HandlerUtil.getActivePart(event) instanceof RepositoryBrowser) {
			RepositoryBrowser b = (RepositoryBrowser) HandlerUtil
					.getActivePart(event);
			ConnectToServerDialog d = new ConnectToServerDialog(
					HandlerUtil.getActiveShell(event), SWT.TITLE
							| SWT.APPLICATION_MODAL, b.getMainView());
			d.open();
		} else {
			// if class is not called from Repository browser view
			log.log(Level.SEVERE,
					"Connect to server comand was not called from RepositoryBrowser class.");
		}
		return null;
	}

}
