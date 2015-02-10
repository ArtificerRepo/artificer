package org.overlord.sramp.srampRepositoryBrowser.dialogs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
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

import org.overlord.sramp.srampRepositoryBrowser.manager.BrowserManager;
import org.overlord.sramp.srampRepositoryBrowser.manager.ServiceFailureException;

/**
 * rule for sheduling jobs
 * 
 * @author Jan Bouska
 * 
 */
class Mutex implements ISchedulingRule {

	@Override
	public boolean isConflicting(ISchedulingRule rule) {
		return rule == this;
	}

	@Override
	public boolean contains(ISchedulingRule rule) {
		return rule == this;
	}
}

/**
 * Connect to server dialog class
 * 
 * @author Jan Bouska
 * 
 */
public class ConnectToServerDialog extends Dialog {

	private static final Mutex rule = new Mutex();

	private Object result;
	private Shell shlConnectToServer;
	private Text serverT;
	private Text usernameT;
	private Label passwordLabel;
	private Text passwordT;
	private Button cancelButton;
	private ViewMain ui;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public ConnectToServerDialog(Shell parent, int style, ViewMain ui) {
		super(parent, style);
		this.ui = ui;

	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public Object open() {
		createContents();
		shlConnectToServer.open();
		shlConnectToServer.layout();
		Display display = getParent().getDisplay();
		while (!shlConnectToServer.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shlConnectToServer = new Shell(getParent(), getStyle());
		shlConnectToServer.setText("Connect to server");
		int width = 450;
		int height = 176;
		shlConnectToServer.setSize(width, height);
		shlConnectToServer.setLayout(new GridLayout(2, false));

		Label usernameLabel = new Label(shlConnectToServer, SWT.NONE);
		usernameLabel.setText("Username:");

		passwordLabel = new Label(shlConnectToServer, SWT.NONE);
		passwordLabel.setText("Password:");

		usernameT = new Text(shlConnectToServer, SWT.BORDER);
		usernameT.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));

		passwordT = new Text(shlConnectToServer, SWT.BORDER | SWT.PASSWORD);
		passwordT.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				1, 1));

		Label serverLabel = new Label(shlConnectToServer, SWT.NONE);
		serverLabel.setText("Server:");
		new Label(shlConnectToServer, SWT.NONE);

		serverT = new Text(shlConnectToServer, SWT.BORDER);
		serverT.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false,
				2, 1));
		serverT.setText("http://localhost:8080/s-ramp-server");

		Button OKButton = new Button(shlConnectToServer, SWT.NONE);
		GridData gd_OKButton = new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 1, 1);
		gd_OKButton.widthHint = 60;
		OKButton.setLayoutData(gd_OKButton);
		OKButton.setText("OK");
		OKButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {

				final String user = usernameT.getText();
				final String password = passwordT.getText();
				final String server = serverT.getText();

				// connecting to server job
				Job connecting = new Job("connecting") {

					@Override
					protected IStatus run(IProgressMonitor monitor) {

						try {
							getManager().setConnection(server, user, password);
							// setting default query
							ui.setFilter(getManager().listAllArtifacts());

						} catch (ServiceFailureException e) {
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									MessageDialog
											.openError(shlConnectToServer,
													"Connection failed!",
													"You have wrong URL, username or password.");

								}
							});

						}

						return Status.OK_STATUS;
					}

				};

				// loading data and updating table
				RefreshJob loading = new RefreshJob("loading data", ui);

				// setting sheduling rule for jobs
				connecting.setRule(rule);
				loading.setRule(rule);
				// sheduling jobs
				connecting.schedule();
				loading.schedule();

				shlConnectToServer.close();

			}

		});

		cancelButton = new Button(shlConnectToServer, SWT.NONE);
		GridData gd_cancelButton = new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 1, 1);
		gd_cancelButton.widthHint = 60;
		cancelButton.setLayoutData(gd_cancelButton);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				shlConnectToServer.close();
			}

		});

		shlConnectToServer.setDefaultButton(OKButton);

	}

	/**
	 * return browser manager
	 * 
	 * @return browser manager
	 */
	private BrowserManager getManager() {
		return ui.getManager();
	}

}
