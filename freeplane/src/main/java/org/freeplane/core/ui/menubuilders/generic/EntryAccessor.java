package org.freeplane.core.ui.menubuilders.generic;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.freeplane.core.ui.AFreeplaneAction;

public class EntryAccessor {

	public static final String COMPONENT = "component";
	public static final String ACTION = "action";
	public static final String TEXT = "text";
	public static final String ICON = "icon";
	public static final String ACCELERATOR = "accelerator";
	public final ResourceAccessor resourceAccessor;

	public EntryAccessor(ResourceAccessor resourceAccessor) {
		this.resourceAccessor = resourceAccessor;
	}

	public EntryAccessor() {
		this(ResourceAccessor.NULL_RESOURCE_ACCESSOR);
    }

	public Icon getIcon(final Entry entry) {
		if (entry.getAttribute(ICON) != null)
			return (Icon) entry.getAttribute(ICON);
		else {
			String name = entry.getName();
			final String key = name + ".icon";
			final String iconResource = resourceAccessor.getProperty(key);
			final Icon icon;
			if (iconResource != null) {
				final URL url = resourceAccessor.getResource(iconResource);
				icon = new ImageIcon(url);
			}
			else
				icon = null;
			return icon;
		}

	}

	public String getText(final Entry entry) {
		if (entry.getAttribute(TEXT) != null)
			return (String) entry.getAttribute(TEXT);
		else {
			final AFreeplaneAction action = getAction(entry);
			if (action != null)
				return action.getRawText();
			String name = entry.getName();
			if (name.isEmpty())
				return "";
			else {
				final String rawText = resourceAccessor.getRawText(name);
				if (rawText != null)
					return rawText;
				else
					return "";
			}
		}
	}

	public Object getComponent(final Entry entry) {
		return entry.getAttribute(COMPONENT);
	}

	public Object removeComponent(final Entry entry) {
		return entry.removeAttribute(COMPONENT);
	}

	public void setComponent(final Entry entry, Object component) {
		entry.setAttribute(COMPONENT, component);
	}

	public AFreeplaneAction getAction(final Entry entry) {
		return (AFreeplaneAction) entry.getAttribute(ACTION);
	}

	public void setAction(final Entry entry, AFreeplaneAction action) {
		entry.setAttribute(ACTION, action);
	}

	public Object getAncestorComponent(final Entry entry) {
		final Entry parent = entry.getParent();
		if (parent == null)
			return null;
		else {
			final Object parentComponent = getComponent(parent);
			if (parentComponent != null)
				return parentComponent;
            else
				return getAncestorComponent(parent);
		}
	}

	public void setText(Entry entry, String text) {
		entry.setAttribute(TEXT, text);
	}

	public void setIcon(Entry entry, Icon icon) {
		entry.setAttribute(ICON, icon);
	}

	public String getAccelerator(Entry entry) {
		String accelerator = (String) entry.getAttribute(ACCELERATOR);
		return accelerator;
	}

	public void addChildAction(Entry target, AFreeplaneAction action) {
		final Entry actionEntry = new Entry();
		new EntryAccessor().setAction(actionEntry, action);
		target.addChild(actionEntry);
	}
}
