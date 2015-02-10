package org.overlord.sramp.srampRepositoryBrowser.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Class that groups the property fields with add property button.
 * 
 * @author Jan Bouska
 * 
 */
class Properties extends Composite {

	private Prop prop;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public Properties(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));
		prop = new Prop(this, SWT.NONE);
		prop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		Button addProperty = new Button(this, SWT.NONE);
		addProperty.setText("Add property");
		addProperty.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				prop.addProperty();
			}
		});

	}

	public java.util.Properties getPropertyList() {
		return prop.getData();
	}

}

/**
 * Class represents one property composite.
 * 
 * @author Jan Bouska
 * 
 */
class Property extends Composite {
	private Text nameT;
	private Text valueT;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public Property(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(2, false));

		Label nameLabel = new Label(this, SWT.NONE);

		nameLabel.setText("Name:");

		Label valueLabel = new Label(this, SWT.NONE);
		valueLabel.setText("Value:");

		nameT = new Text(this, SWT.BORDER);
		nameT.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1,
				1));

		valueT = new Text(this, SWT.BORDER);
		valueT.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1,
				1));

	}

	/**
	 * 
	 * @return text from the name field
	 */
	public String getNameT() {
		return nameT.getText();
	}

	/**
	 * 
	 * @return text from the value field
	 */
	public String getValueT() {
		return valueT.getText();
	}
}

/**
 * Class, which wraps all properties.
 * 
 * @author Jan Bouska
 * 
 */
class Prop extends Composite {

	private List<Property> list;

	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public Prop(Composite parent, int style) {
		super(parent, style);
		list = new ArrayList<>();
		setLayout(new GridLayout(1, false));

		Property pr = new Property(this, SWT.NONE);
		list.add(pr);
		pr.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

	}

	/**
	 * add property fields to dialog
	 */
	void addProperty() {
		Property pr = new Property(this, SWT.NONE);
		list.add(pr);
		pr.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		getShell().layout();
		getShell().pack();
	}

	/**
	 * return list of property data
	 */
	public java.util.Properties getData() {

		java.util.Properties data = new java.util.Properties();

		for (Property p : list) {
			if (p.getNameT().isEmpty()) {
				continue;
			}
			data.setProperty(p.getNameT(), p.getValueT());

		}

		return data;

	}

}
