package org.docear.plugin.pdfutilities.ui;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JMenu;

public class JDocearInvisibleMenu extends JMenu implements PropertyChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private boolean setVisibility;
	private boolean setEnabled;

	public JDocearInvisibleMenu(String text, boolean setVisibility, boolean setEnabled) {
		super(text);
		this.setSetVisibility(setVisibility);
		this.setSetEnabled(setEnabled);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		boolean enabled = false;
		for(Component component : this.getMenuComponents()){
			if(component.isEnabled()){
				enabled = true;
				break;
			}
		}
		if(isSetVisibility()){
			this.setVisible(enabled);
		}
		if(isSetEnabled()){
			this.setEnabled(enabled);
		}	
	}

	public boolean isSetVisibility() {
		return setVisibility;
	}

	public void setSetVisibility(boolean setVisibility) {
		this.setVisibility = setVisibility;
	}

	public boolean isSetEnabled() {
		return setEnabled;
	}

	public void setSetEnabled(boolean setEnabled) {
		this.setEnabled = setEnabled;
	}

}
