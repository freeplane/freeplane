package org.freeplane.core.ui.menubuilders.generic;

import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;

public class EntryAccessor {

	public static final String COMPONENT = "component";
	public static final Class<AFreeplaneAction> ACTION = AFreeplaneAction.class;
	public static final String TEXT = "text";
	public static final String TEXT_KEY = "textKey";
	public static final Class<Icon> ICON = Icon.class;
	public static final String ACCELERATOR = "accelerator";
	public final ResourceAccessor resourceAccessor;
	public static final String MENU_ELEMENT_SEPARATOR = " -> ";

	public EntryAccessor(ResourceAccessor resourceAccessor) {
		this.resourceAccessor = resourceAccessor;
	}

	public EntryAccessor() {
		this(ResourceAccessor.NULL_RESOURCE_ACCESSOR);
    }

	public Icon getIcon(final Entry entry) {
		if (entry.getAttribute(ICON) != null)
			return entry.getAttribute(ICON);
		else {
			String name = entry.getName();
			final String key = name + ".icon";
			final String iconResource = resourceAccessor.getProperty(key);
			final Icon icon;
			if (iconResource != null) {
				final URL url = resourceAccessor.getResource(iconResource);
				if(url != null)
					icon = new ImageIcon(url);
				else {
					LogUtils.severe("Can not load icon '" + iconResource + "'");
					icon = null;
				}
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
			final String textKey = (String) entry.getAttribute(TEXT_KEY);
			if (textKey != null)
				return (String) resourceAccessor.getRawText(textKey);
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
	}

	public String getTextKey(final Entry entry) {
		if (entry.getAttribute(TEXT) != null)
			return null;
		final String textKey = (String) entry.getAttribute(TEXT_KEY);
		if (textKey != null)
			return textKey;
		final AFreeplaneAction action = getAction(entry);
		if (action != null) {
			final String actionTextKey = action.getTextKey();
			if(TextUtils.getRawText(actionTextKey, null) != null)
				return actionTextKey;
			else
				return null;
		}
		String name = entry.getName();
		if (name.isEmpty())
			return null;
		else
			return name;
	}
	
	public String getTooltipKey(final Entry entry) {
		final AFreeplaneAction action = getAction(entry);
		if (action != null) {
			final String actionTooltipKey = action.getTooltipKey();
			return actionTooltipKey;
		}
		return null;
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
		return entry.getAttribute(ACTION);
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
		actionEntry.setName(action.getKey());
		setAction(actionEntry, action);
		target.addChild(actionEntry);
	}
	
	public String getLocationDescription(Entry entry) {
		final StringBuilder stringBuilder = new StringBuilder();
		buildLocationDescription(entry, stringBuilder);
		return stringBuilder.toString();
	}

	private void buildLocationDescription(Entry entry, StringBuilder stringBuilder) {
		final Entry parent = entry.getParent();
		if(parent != null)
			buildLocationDescription(parent, stringBuilder);
		final String entryText = TextUtils.removeMnemonic (getText(entry));
		if(! entryText.isEmpty()){
			if(stringBuilder.length() > 0)
				stringBuilder.append(MENU_ELEMENT_SEPARATOR);
			stringBuilder.append(entryText);
		}
	}

}
