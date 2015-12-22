package org.freeplane.core.ui.components;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;

import javax.swing.AbstractButton;

import org.freeplane.core.ui.SelectableAction;

public class PropertyChangeListenerWithSelectionSupport implements PropertyChangeListener{
	final private WeakReference<AbstractButton> target;
	final private PropertyChangeListener defaultPropertyChangeListener;
	
	public PropertyChangeListenerWithSelectionSupport(AbstractButton target, PropertyChangeListener defaultPropertyChanegListener) {
		super();
		this.defaultPropertyChangeListener = defaultPropertyChanegListener;
		this.target = new WeakReference<AbstractButton>(target);
	}

	public void propertyChange(final PropertyChangeEvent e) {
		if (e.getPropertyName().equals(SelectableAction.SELECTION_PROPERTY)) {
			AbstractButton button = target.get();
			if(button != null) {
				final Boolean isSelected = (Boolean) e.getNewValue();
				button.setSelected(isSelected.booleanValue());
			}
		}
		else {
			defaultPropertyChangeListener.propertyChange(e);
		}
	}
}
