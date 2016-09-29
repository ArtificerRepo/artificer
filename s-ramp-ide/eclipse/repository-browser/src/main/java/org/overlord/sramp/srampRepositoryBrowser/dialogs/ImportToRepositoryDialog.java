package org.overlord.sramp.srampRepositoryBrowser.dialogs;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import org.overlord.sramp.srampRepositoryBrowser.manager.ServiceFailureException;

/**
 * import to repository dialog
 * 
 * @author Jan Bouska
 * 
 */
public class ImportToRepositoryDialog extends Dialog {

	private IFile file;
	private ViewMain browser;
	private Shell shell;
	private Text name;
	private Text type;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public ImportToRepositoryDialog(Shell parent, int style, IFile file,
			ViewMain browser) {
		super(parent, style);
		setText("Import to S-RAMP repository");
		this.file = file;
		this.browser = browser;

	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Object open() {
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return null;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), getStyle());

		shell.setText(getText());
		shell.setLayout(new GridLayout(2, false));

		Label nameLabel = new Label(shell, SWT.NONE);
		nameLabel.setText("Name:");
		new Label(shell, SWT.NONE);

		name = new Text(shell, SWT.BORDER);
		name.setText(file.getName());
		name.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label artifactTypeLabel = new Label(shell, SWT.NONE);
		artifactTypeLabel.setText("Artifact Type:");
		new Label(shell, SWT.NONE);

		type = new Text(shell, SWT.BORDER);
		type.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		new Label(shell, SWT.NONE);

		Label propertiesLabel = new Label(shell, SWT.NONE);
		propertiesLabel.setText("Properties");

		final Properties property = new Properties(shell, SWT.BORDER);

		GridData prGr = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		prGr.widthHint = 400;
		property.setLayoutData(prGr);

		Button OKButton = new Button(shell, SWT.NONE);
		GridData gd_OKButton = new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 1, 1);
		gd_OKButton.widthHint = 60;
		OKButton.setLayoutData(gd_OKButton);
		OKButton.setText("OK");
		OKButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {

				final String nameFin = name.getText();
				final String typeFin = type.getText();
				final java.util.Properties propList = property
						.getPropertyList();

				Job uploading = new Job("uploading artifact") {

					/**
					 * job should run when manager is connected
					 */
					public boolean shouldRun() {
						return super.shouldSchedule()
								&& browser.getManager().isConnected();
					}

					@Override
					protected IStatus run(IProgressMonitor monitor) {

						try {

							browser.getManager().uploadArtifact(file, nameFin,
									typeFin, propList);

							Job refresh = new RefreshJob("refreshing data",
									browser);
							refresh.schedule();

						} catch (ServiceFailureException e) {
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									MessageDialog.openError(browser.getShell(),
											"Uploading failed!",
											"Error while uploading the artifact into the repository.");

								}
							});
						}

						return Status.OK_STATUS;
					}

				};

				uploading.schedule();
				shell.close();
			}

		});

		Button cancelButton = new Button(shell, SWT.NONE);
		GridData gd_cancelButton = new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 1, 1);
		gd_cancelButton.widthHint = 60;
		cancelButton.setLayoutData(gd_cancelButton);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				shell.close();
			}

		});

		shell.setDefaultButton(OKButton);

		shell.pack();

	}

}
