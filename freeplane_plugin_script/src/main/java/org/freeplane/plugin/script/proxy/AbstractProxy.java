package org.freeplane.plugin.script.proxy;

import org.freeplane.features.encrypt.EncryptionController;
import org.freeplane.features.encrypt.mindmapmode.MEncryptionController;
import org.freeplane.features.explorer.MapExplorerController;
import org.freeplane.features.explorer.mindmapmode.MMapExplorerController;
import org.freeplane.features.layout.LayoutController;
import org.freeplane.features.map.FreeNode;
import org.freeplane.features.map.clipboard.MapClipboardController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.nodelocation.LocationController;
import org.freeplane.features.note.NoteController;
import org.freeplane.features.text.TextController;
import org.freeplane.plugin.script.ScriptContext;

public abstract class AbstractProxy<T> {
	private static final String DEFAULT_CLASS_NAME_ENDING = "Proxy";
	private final T delegate;
	private final ScriptContext scriptContext;

	AbstractProxy(final T delegate, final ScriptContext scriptContext) {
		this.delegate = delegate;
		this.scriptContext = scriptContext;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(final Object obj) {
		if (!getClass().equals(obj.getClass())) {
			return false;
		}
		return delegate.equals(((AbstractProxy) obj).getDelegate());
	}

	public T getDelegate() {
		return delegate;
	}

	public ScriptContext getScriptContext() {
		return scriptContext;
	}

	public MModeController getModeController() {
		return MModeController.getMModeController();
	}


	public MEncryptionController getEncryptionController() {
		return (MEncryptionController) getModeController().getExtension(EncryptionController.class);
	}

	public LocationController getLocationController() {
		return LocationController.getController(getModeController());
	}

	public MMapExplorerController getExplorer() {
		return (MMapExplorerController) getModeController().getExtension(MapExplorerController.class);
	}

	public LayoutController getLayoutController() {
		return LayoutController.getController(getModeController());
	}

	public NoteController getNoteController() {
		return getModeController().getExtension(NoteController.class);
	}

	public FreeNode getFreeNodeHook() {
		return getModeController().getExtension(FreeNode.class);
	}

	public TextController getTextController() {
		return TextController.getController(getModeController());
	}

	public MapClipboardController getClipboardController() {
		return getModeController().getExtension(MapClipboardController.class);
	}

	@Override
	public int hashCode() {
		return delegate.hashCode() * 31 + getClass().hashCode();
	}

	@Override
	public String toString() {
		final String simpleName = getClass().getSimpleName();
		String className = simpleName.endsWith(DEFAULT_CLASS_NAME_ENDING) ? simpleName.substring(0, simpleName.length() - DEFAULT_CLASS_NAME_ENDING.length()) : simpleName;
		return '(' + className + ") " + delegate.toString();
	}
}
