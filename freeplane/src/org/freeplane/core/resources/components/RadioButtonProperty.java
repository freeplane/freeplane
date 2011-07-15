package org.freeplane.core.resources.components;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JLabel;
import javax.swing.JRadioButton;

import org.freeplane.features.mode.Controller;

import com.jgoodies.forms.builder.DefaultFormBuilder;

public class RadioButtonProperty extends PropertyBean implements IPropertyControl{
	
	JRadioButton mRadioButton = new JRadioButton();
	JLabel mLabel;

	public RadioButtonProperty(String name, String enabled) {
		super(name);
		mRadioButton.setName(name);
		mRadioButton.addItemListener(new ItemListener() {
			public void itemStateChanged(final ItemEvent pE) {
				firePropertyChangeEvent();
			}
		});
		
		mRadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(final ActionEvent arg0) {
				Controller.getCurrentController().getOptionPanelController().actionPerformed(arg0);
			}
		});
		
		if(enabled == null || !enabled.equalsIgnoreCase("false")){
			this.setEnabled(true);			
		}
		else{
			this.setEnabled(false);
		}
	}

	public void layout(DefaultFormBuilder builder) {
		this.mLabel = layout(builder, mRadioButton);
		mLabel.setEnabled(this.mRadioButton.isEnabled());
	}

	public void setEnabled(boolean pEnabled) {
		mRadioButton.setEnabled(pEnabled);
		if(mLabel != null){
			mLabel.setEnabled(pEnabled);
		}
	}

	@Override
	public String getValue() {
		return mRadioButton.isSelected() ? Boolean.TRUE.toString() : Boolean.FALSE.toString();
	}

	@Override
	public void setValue(String value) {
		final boolean booleanValue = Boolean.parseBoolean(value);
		setValue(booleanValue);		
	}
	
	public void setValue(final boolean booleanValue) {
		mRadioButton.setSelected(booleanValue);
	}

	public boolean getBooleanValue() {
		return mRadioButton.isSelected();
	}

	@Override
    protected Component[] getComponents() {
	    return new Component[]{mRadioButton};
    }

}
