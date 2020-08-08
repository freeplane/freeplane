package org.freeplane.features.mode;

import org.freeplane.core.ui.AFreeplaneAction;

public interface FreeplaneActions {
	public void addAction(final AFreeplaneAction action);
	public AFreeplaneAction getAction(final String key);
	AFreeplaneAction removeActionIfSet(final String key);
	AFreeplaneAction removeAction(final String key);
	AFreeplaneAction addActionIfNotAlreadySet(AFreeplaneAction action);
}
