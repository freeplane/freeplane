package org.freeplane.core.ui;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import javax.swing.Action;

public class ActionEnabler implements PropertyChangeListener {
	final private WeakReference<Component> comp;

	public ActionEnabler(final Component comp) {
		this.comp = new WeakReference<Component>(comp);
	}

	public void propertyChange(final PropertyChangeEvent evt) {
		final Component component = comp.get();
		if (component == null) {
			final Action action = (Action) evt.getSource();
			action.removePropertyChangeListener(this);
		}
		else if (evt.getPropertyName().equals("enabled")) {
			final Action action = (Action) evt.getSource();
			component.setEnabled(action.isEnabled());
		}
	}
}