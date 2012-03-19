package org.freeplane.core.resources.components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPasswordField;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class PasswordProperty extends PropertyBean implements IPropertyControl {
	final JPasswordField mTextField;
	
	public PasswordProperty(String name, String label, String description) {
		super(name, label, description);
		mTextField = new JPasswordField();
		initialize();
	}

	public PasswordProperty(String name) {
		super(name);
		mTextField = new JPasswordField();
		
		initialize();
	}
	
	public void layout(DefaultFormBuilder builder) {
		layout(builder, mTextField);

	}

	public void setEnabled(boolean pEnabled) {
		mTextField.setEnabled(pEnabled);
	}

	public String getValue() {
		return new String(mTextField.getPassword());
	}

	public void setValue(String value) {
		mTextField.setText(value);
		mTextField.selectAll();

	}
	
	protected Component[] getComponents() {
		return new Component[]{mTextField};
	}
	
	private void initialize() {		
		mTextField.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent pE) {
				firePropertyChangeEvent();
			}
		});
	}

}
