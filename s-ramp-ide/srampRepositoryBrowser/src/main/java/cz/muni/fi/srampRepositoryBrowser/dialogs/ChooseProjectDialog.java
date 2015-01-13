package cz.muni.fi.srampRepositoryBrowser.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * Class represents choose project dialog.
 * 
 * @author Jan Bouska
 * 
 */
public class ChooseProjectDialog extends Dialog {

	private IProject result;
	private Shell shlProjectDialog;
	private Table table;
	private IProject[] projectsList;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public ChooseProjectDialog(Shell parent, int style) {
		super(parent, style);

	}

	/**
	 * Open the dialog.
	 * 
	 * @return the result
	 */
	public IProject open() {
		createContents();
		shlProjectDialog.open();
		shlProjectDialog.layout();
		Display display = getParent().getDisplay();
		while (!shlProjectDialog.isDisposed()) {
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

		shlProjectDialog = new Shell(getParent(), getStyle());
		shlProjectDialog.setText("Choose the target project!");
		int width = 300;
		int height = 400;

		shlProjectDialog.setSize(width, height);
		shlProjectDialog.setLayout(new GridLayout(2, true));

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		projectsList = root.getProjects();

		table = new Table(shlProjectDialog, SWT.BORDER | SWT.MULTI);
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		table.setLinesVisible(true);

		for (IProject p : projectsList) {
			TableItem item = new TableItem(table, SWT.NONE);
			item.setText(p.getName());

		}

		Button ok = new Button(shlProjectDialog, SWT.NONE);

		GridData gd_OKButton = new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 1, 1);
		gd_OKButton.widthHint = 60;
		ok.setLayoutData(gd_OKButton);
		ok.setText("OK");
		ok.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				if (table.getSelectionIndex() != (-1)) {
					result = projectsList[table.getSelectionIndex()];
					shlProjectDialog.close();
				}
			}
		});

		Button cancelButton = new Button(shlProjectDialog, SWT.NONE);
		GridData gd_cancelButton = new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 1, 1);
		gd_cancelButton.widthHint = 60;
		cancelButton.setLayoutData(gd_cancelButton);
		cancelButton.setText("Cancel");
		cancelButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				shlProjectDialog.close();
			}

		});

		shlProjectDialog.setDefaultButton(ok);

	}
}
