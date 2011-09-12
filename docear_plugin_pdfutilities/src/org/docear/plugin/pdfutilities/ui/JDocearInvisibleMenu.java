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

	public JDocearInvisibleMenu(String text) {
		super(text);
	}

	public void propertyChange(PropertyChangeEvent evt) {
		boolean visible = false;
		for(Component component : this.getMenuComponents()){
			if(component.isEnabled()){
				visible = true;
				break;
			}
		}
		this.setVisible(visible);
	}

}
