package cz.muni.fi.srampRepositoryBrowser.dialogs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.overlord.sramp.client.SrampClientQuery;

/**
 * Dialog represents calendar.
 * 
 * @author Jan Bouska
 * 
 */
class DateTimeDialog extends Dialog {

	private Point location;
	private DateTime dt;
	private Calendar result;
	private Shell shlConnectToServer;

	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public DateTimeDialog(Shell parent, int style, Point location) {
		super(parent, style);
		this.location = location;

	}

	/**
	 * Open the dialog.
	 * 
	 * @return the selected date
	 */
	public Calendar open() {
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
		shlConnectToServer.setLocation(location);
		shlConnectToServer.setLayout(new GridLayout(1, false));
		dt = new DateTime(shlConnectToServer, SWT.CALENDAR | SWT.BORDER);

		Button b = new Button(shlConnectToServer, SWT.NONE);
		b.setText("ok");
		b.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				result = GregorianCalendar.getInstance();
				result.set(Calendar.YEAR, dt.getYear());
				result.set(Calendar.MONTH, dt.getMonth());
				result.set(Calendar.DAY_OF_MONTH, dt.getDay());
				result.set(Calendar.MILLISECOND, 0);
				result.set(Calendar.SECOND, 0);
				result.set(Calendar.MINUTE, 0);
				result.set(Calendar.HOUR_OF_DAY, 0);

				shlConnectToServer.dispose();
			}
		});

		b.pack();
		shlConnectToServer.pack();

	}

}

/**
 * Class representing the browser filter.
 * 
 * @author Jan Bouska
 * 
 */
public class Filter extends Composite {
	private Text typeT;
	private Text createdByT;
	private Text lastModifiedByT;
	private ViewMain parent;
	private Calendar createdFrom = null;
	private Calendar createdTo = null;
	private Calendar lastModifFrom = null;
	private Calendar lastModifTo = null;

	private Text createdFromT;
	private Text createdToT;
	private Text lastModifiedFrom;
	private Text lastModifiedToT;

	private Button btnClearAllFilters;
	private Button btnFilter;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public Filter(ViewMain parent, int style) {
		super(parent, SWT.NONE);
		this.parent = parent;
		setLayout(new GridLayout(3, false));

		Label filter = new Label(this, SWT.NONE);

		filter.setEnabled(true);
		filter.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false,
				1, 1));

		filter.setText("filter");
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);

		Label type = new Label(this, SWT.NONE);
		type.setEnabled(true);
		type.setText("Type");
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);

		typeT = new Text(this, SWT.BORDER);
		typeT.setEnabled(true);
		GridData gd_typeT = new GridData(SWT.FILL, SWT.CENTER, false, false, 3,
				1);
		gd_typeT.widthHint = 230;
		typeT.setLayoutData(gd_typeT);

		Label dateCreated = new Label(this, SWT.NONE);
		dateCreated.setEnabled(true);
		dateCreated.setText("Date Created");
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);

		createdFromT = new Text(this, SWT.READ_ONLY | SWT.NONE);
		createdFromT.setText("Any");
		GridData gd_createdFromT = new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 1, 1);
		gd_createdFromT.widthHint = 107;
		createdFromT.setLayoutData(gd_createdFromT);
		// open calendar and set the date
		createdFromT.addMouseListener(new MouseListener() {

			@Override
			public void mouseDown(MouseEvent arg0) {

				DateTimeDialog dt = new DateTimeDialog(getShell(), SWT.NONE,
						createdFromT.toDisplay(0, 0));
				createdFrom = dt.open();
				DateFormat dateFormat = new SimpleDateFormat("MM/dd/YYYY");
				createdFromT.setText(dateFormat.format(createdFrom.getTime()));
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {

			}

			@Override
			public void mouseUp(MouseEvent e) {

			}

		});

		Label dCto = new Label(this, SWT.NONE);
		dCto.setEnabled(true);
		GridData gd_dCto = new GridData(SWT.CENTER, SWT.CENTER, false, false,
				1, 1);
		gd_dCto.widthHint = 16;
		dCto.setLayoutData(gd_dCto);
		dCto.setText("to");

		createdToT = new Text(this, SWT.READ_ONLY | SWT.NONE);
		createdToT.setText("Any");
		GridData gd_createdToT = new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 1, 1);
		gd_createdToT.widthHint = 107;
		createdToT.setLayoutData(gd_createdToT);
		createdToT.addMouseListener(new MouseListener() {

			@Override
			public void mouseDown(MouseEvent arg0) {

				DateTimeDialog dt = new DateTimeDialog(getShell(), SWT.NONE,
						createdToT.toDisplay(0, 0));
				createdTo = dt.open();
				DateFormat dateFormat = new SimpleDateFormat("MM/dd/YYYY");
				createdToT.setText(dateFormat.format(createdTo.getTime()));
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}

		});

		Label dateLastModified = new Label(this, SWT.NONE);
		dateLastModified.setEnabled(true);
		dateLastModified.setText("Date Last Modified");
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);

		lastModifiedFrom = new Text(this, SWT.READ_ONLY | SWT.NONE);
		lastModifiedFrom.setText("Any");
		GridData gd_LastModifFrom = new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 1, 1);
		gd_LastModifFrom.widthHint = 107;
		lastModifiedFrom.setLayoutData(gd_LastModifFrom);
		lastModifiedFrom.addMouseListener(new MouseListener() {

			@Override
			public void mouseDown(MouseEvent arg0) {

				DateTimeDialog dt = new DateTimeDialog(getShell(), SWT.NONE,
						lastModifiedFrom.toDisplay(0, 0));
				lastModifFrom = dt.open();
				DateFormat dateFormat = new SimpleDateFormat("MM/dd/YYYY");
				lastModifiedFrom.setText(dateFormat.format(lastModifFrom
						.getTime()));
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}

		});

		Label dMto = new Label(this, SWT.NONE);
		dMto.setEnabled(true);
		GridData gd_dMto = new GridData(SWT.CENTER, SWT.CENTER, false, false,
				1, 1);
		gd_dMto.widthHint = 16;
		dMto.setLayoutData(gd_dMto);
		dMto.setText("to");

		lastModifiedToT = new Text(this, SWT.READ_ONLY | SWT.NONE);
		lastModifiedToT.setText("Any");
		GridData gd_LastModifTo = new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 1, 1);
		gd_LastModifTo.widthHint = 107;
		lastModifiedToT.setLayoutData(gd_LastModifTo);
		lastModifiedToT.addMouseListener(new MouseListener() {

			@Override
			public void mouseDown(MouseEvent arg0) {

				DateTimeDialog dt = new DateTimeDialog(getShell(), SWT.NONE,
						lastModifiedToT.toDisplay(0, 0));
				lastModifTo = dt.open();
				DateFormat dateFormat = new SimpleDateFormat("MM/dd/YYYY");
				lastModifiedToT.setText(dateFormat.format(lastModifTo.getTime()));
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}

			@Override
			public void mouseUp(MouseEvent e) {
			}

		});

		Label createdBy = new Label(this, SWT.NONE);
		createdBy.setEnabled(true);
		createdBy.setText("Created By");
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);

		createdByT = new Text(this, SWT.BORDER);
		createdByT.setEnabled(true);
		GridData gd_createdByT = new GridData(SWT.FILL, SWT.CENTER, false,
				false, 3, 1);
		gd_createdByT.widthHint = 230;
		createdByT.setLayoutData(gd_createdByT);

		Label lastModifiedBy = new Label(this, SWT.NONE);
		lastModifiedBy.setEnabled(true);
		lastModifiedBy.setText("Last Modified By");
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);

		lastModifiedByT = new Text(this, SWT.BORDER);
		lastModifiedByT.setEnabled(true);
		GridData gd_lastModifiedByT = new GridData(SWT.FILL, SWT.CENTER, false,
				false, 3, 1);
		gd_lastModifiedByT.widthHint = 230;
		lastModifiedByT.setLayoutData(gd_lastModifiedByT);

		btnFilter = new Button(this, SWT.NONE);
		btnFilter.setEnabled(true);
		btnFilter.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false,
				false, 1, 1));
		btnFilter.setText("Filter");
		btnFilter.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Filter.this.parent.setFilter(setFilter());
				Job refresh = new RefreshJob("refreshing", Filter.this.parent);
				refresh.schedule();
			}
		});
		new Label(this, SWT.NONE);

		btnClearAllFilters = new Button(this, SWT.NONE);
		btnClearAllFilters.setEnabled(true);
		btnClearAllFilters.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER,
				false, false, 1, 1));
		btnClearAllFilters.setText("Clear all filters");
		btnClearAllFilters.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				Filter.this.parent.setFilter(Filter.this.parent.getManager()
						.listAllArtifacts());
				typeT.setText("");
				createdByT.setText("");
				lastModifiedByT.setText("");
				createdFrom = null;
				createdTo = null;
				lastModifFrom = null;
				lastModifTo = null;

				createdFromT.setText("Any");
				createdToT.setText("Any");
				lastModifiedFrom.setText("Any");
				lastModifiedToT.setText("Any");

				Job refresh = new RefreshJob("refreshing", Filter.this.parent);
				refresh.schedule();
			}
		});

	}

	/**
	 * Find artifact model from artifact type.
	 * 
	 * @param type
	 * @return artifact model
	 */
	private String findModel(String type) {
		switch (type) {
		// core
		case "Document":
		case "XmlDocument":
			return "core";

			// xsd
		case "XsdDocument":
		case "AttributeDeclaration":
		case "ElementDeclaration":
		case "SimpleTypeDeclaration":
		case "ComplexTypeDeclaration":
			return "xsd";

			// wsdl
		case "WsdlDocument":
		case "WsdlService":
		case "Port":
		case "WsdlExtension":
		case "Part":
		case "Message":
		case "Fault":
		case "PortType":
		case "Operation":
		case "OperationInput":
		case "OperationOutput":
		case "Binding":
		case "BindingOperation":
		case "BindingOperationInput":
		case "BindingOperationOutput":
		case "BindingOperationFault":
			return "wsdl";

			// policy
		case "PolicyDocument":
		case "PolicyExpression":
		case "PolicyAttachment":
			return "policy";

			// soa
		case "HumanActor":
		case "Choreography":
		case "ChoreographyProcess":
		case "Collaboration":
		case "CollaborationProcess":
		case "Composition":
		case "Effect":
		case "Element":
		case "Event":
		case "InformationType":
		case "Orchestration":
		case "OrchestrationProcess":
		case "Policy":
		case "PolicySubject":
		case "Process":
		case "Service":
		case "ServiceContract":
		case "ServiceComposition":
		case "ServiceInterface":
		case "System":
		case "Task":
			return "soa";

			// serviceImplementation
		case "Organization":
		case "ServiceEndpoint":
		case "ServiceInstance":
		case "ServiceOperation":
			return "serviceImplementation";

		default:
			return "ext";
		}
	}

	/**
	 * Method set query by typeT field (type of artifact), createdByT,
	 * lastModifiedByT, createdFrom, createdTo, lastModifFrom and lastModifTo
	 * field.
	 */
	private SrampClientQuery setFilter() {
		String query = "/s-ramp";
		if (typeT.getText().length() > 0) {
			query += "/" + findModel(typeT.getText()) + "/" + typeT.getText();

			System.err.println(query);
		}

		SortedMap<String, String> texts = new TreeMap<>();
		if (!createdByT.getText().isEmpty())
			texts.put("createdBy", createdByT.getText());
		if (!lastModifiedByT.getText().isEmpty())
			texts.put("lastModifiedBy", lastModifiedByT.getText());

		SortedMap<String, Calendar> dates = new TreeMap<>();
		if (createdFrom != null)
			dates.put("createdTimestamp >", createdFrom);
		if (createdTo != null)
			dates.put("createdTimestamp <", createdTo);
		if (lastModifFrom != null)
			dates.put("lastModifiedTimestamp >", lastModifFrom);
		if (lastModifTo != null)
			dates.put("lastModifiedTimestamp <", lastModifTo);

		query += "[@derived = 'false' ";

		if ((!texts.isEmpty()) || (!dates.isEmpty())) {
			query += "and";
			int textsSize = texts.size() - 1;
			for (String s : texts.keySet()) {
				query += "@" + s + "= ?";
				if (textsSize > 0) {
					query += " and ";
					textsSize--;
				}
			}

			if ((!texts.isEmpty()) && (!dates.isEmpty())) {
				query += " and ";
			}
			int datesSize = dates.size() - 1;
			for (String s : dates.keySet()) {
				query += "@" + s + " ?";
				if (datesSize > 0) {
					query += " and ";
					datesSize--;
				}
			}

		}

		query += "]";
		SrampClientQuery srampQuery = parent.getManager().buildQuery(query);

		for (String s : texts.keySet()) {
			srampQuery.parameter(texts.get(s));
		}

		for (String s : dates.keySet()) {
			srampQuery.parameter(dates.get(s));
		}

		return srampQuery;

	}

}
