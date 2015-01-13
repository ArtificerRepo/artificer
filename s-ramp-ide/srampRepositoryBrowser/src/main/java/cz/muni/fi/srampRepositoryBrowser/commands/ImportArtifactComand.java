package cz.muni.fi.srampRepositoryBrowser.commands;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.handlers.HandlerUtil;

import cz.muni.fi.srampRepositoryBrowser.dialogs.ImportToRepositoryDialog;

/**
 * Default handler for ImportArtifactComand
 * 
 * @author Jan Bouska
 * 
 */
public class ImportArtifactComand extends AbstractHandler {

	public static final Logger log = Logger
			.getLogger(ConnectToServerComand.class.getName());

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		if (getView(event) == null) {
			log.log(Level.WARNING, "Getting Browser view failed");
			return null;
		}

		if (!(getView(event) instanceof RepositoryBrowser)) {
			log.log(Level.WARNING, "Getting Browser view failed");
			return null;
		}

		// getting Repository browser for communication with manager
		RepositoryBrowser browser = (RepositoryBrowser) getView(event);

		if (!browser.getMainView().getManager().isConnected()) {
			MessageDialog.openError(HandlerUtil.getActiveShell(event),
					"Uploadint artifact", "You must be connected before!");
			return null;

		}

		// getting selection resource
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection != null) {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection s = (IStructuredSelection) selection;
				Object elem = s.getFirstElement();
				if (elem instanceof IAdaptable) {

					IResource r = (IResource) ((IAdaptable) elem)
							.getAdapter(IResource.class);

					if (r != null) {
						if (r instanceof IFile) {
							IFile f = (IFile) r;
							ImportToRepositoryDialog dialog = new ImportToRepositoryDialog(
									HandlerUtil.getActiveShell(event),
									SWT.TITLE | SWT.APPLICATION_MODAL, f,
									browser.getMainView());
							dialog.open();
							return null;
						} else {
							log.log(Level.WARNING, "Is not instance of IFile.");
						}

					} else {
						log.log(Level.WARNING,
								"Is not adaptabile to IResource.");
					}
				} else {
					log.log(Level.WARNING,
							"Selection element is not IAdaptable.");
				}
			} else {
				log.log(Level.WARNING, "Selection is not structured selection.");
			}
		} else {
			log.log(Level.WARNING, "Selection is null.");
		}

		return null;
	}

	/**
	 * get repository view
	 * 
	 * @param event
	 * @return repository view
	 */
	private IViewPart getView(ExecutionEvent event) {

		IViewReference ref = HandlerUtil.getActiveWorkbenchWindow(event)
				.getActivePage().findViewReference(RepositoryBrowser.ID);
		if (ref == null)
			return null;

		return ref.getView(true);

	}

}
