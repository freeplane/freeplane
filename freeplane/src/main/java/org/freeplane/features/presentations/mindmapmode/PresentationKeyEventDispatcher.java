package org.freeplane.features.presentations.mindmapmode;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;

class PresentationKeyEventDispatcher implements KeyEventDispatcher, IFreeplanePropertyListener {
	private final KeyEventDispatcher delegate;
	private final String propertyName;
	public PresentationKeyEventDispatcher(String propertyName, KeyEventDispatcher delegate, boolean processKeys) {
		super();
		this.propertyName = propertyName;
		this.delegate = delegate;
		isPresentationRunning = false;
		this.processKeys = processKeys;
	}
	private boolean isPresentationRunning;
	private boolean processKeys;
	@Override
	public boolean dispatchKeyEvent(KeyEvent e) {
		if(! (isPresentationRunning && processKeys))
			return false;
		return delegate.dispatchKeyEvent(e);
	}
	public void activate() {
		isPresentationRunning = true;
	}
	public void deactivate() {
		isPresentationRunning = false;
	}
	@Override
	public void propertyChanged(String propertyName, String newValue, String oldValue) {
		if(this.propertyName.equals(propertyName)){
			processKeys = Boolean.parseBoolean(newValue);
		}
	}
	
	static PresentationKeyEventDispatcher of(KeyEventDispatcher upDownKeyEventDispatcher, String propertyName) {
		ResourceController resourceController = ResourceController.getResourceController();
		final boolean processKeys = ResourceController.getResourceController().getBooleanProperty(propertyName);
		final PresentationKeyEventDispatcher presentationKeyEventDispatcher = new PresentationKeyEventDispatcher(propertyName, upDownKeyEventDispatcher, processKeys);
		resourceController.addPropertyChangeListener(presentationKeyEventDispatcher);
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(presentationKeyEventDispatcher);
		resourceController.addPropertyChangeListener(presentationKeyEventDispatcher);
		return presentationKeyEventDispatcher;
	}
}