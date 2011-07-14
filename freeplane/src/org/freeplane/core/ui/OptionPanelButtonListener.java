package org.freeplane.core.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.freeplane.core.resources.components.IPropertyControl;

public class OptionPanelButtonListener implements ActionListener {
	private static List<ActionListener> list = new ArrayList<ActionListener>();
	private static Vector<IPropertyControl> propertyControls = new Vector<IPropertyControl>();
	
	
	public static void addButtonListener(ActionListener listener) {
		list.add(listener);
	}

	public void actionPerformed(ActionEvent e) {
		for(ActionListener listener : list) {
			listener.actionPerformed(e);
		}

	}
	
	public static void setPropertyControls(final Vector<IPropertyControl> props) {
		propertyControls = props;
	}
	
	public static Vector<IPropertyControl> getPropertyControls() {
		return propertyControls;
	}

}
