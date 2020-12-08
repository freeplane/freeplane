package org.freeplane.core.ui.menubuilders.generic;

import java.awt.Component;

import javax.swing.Icon;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.TextUtils;

public class EntryAccessor {

	private static final String COMPONENT = "component";
	private static final String TEXT = "text";
	private static final String TEXT_KEY = "textKey";
    public static final String ICON = "icon";
    public static final String DRAW_MENU_ICON_ALWAYS = "draw_menu_icon_always";
    public static final String SHOW_MENU_ICONS_PROPERTY = "show_menu_icons";
	public static final Class<Icon> ICON_INSTANCE = Icon.class;
	public static final String ACCELERATOR = "accelerator";
	private final ResourceAccessor resourceAccessor;
	public static final String MENU_ELEMENT_SEPARATOR = " -> ";

	public EntryAccessor(ResourceAccessor resourceAccessor) {
		this.resourceAccessor = resourceAccessor;
	}

	public EntryAccessor() {
		this(ResourceAccessor.NULL_RESOURCE_ACCESSOR);
    }

	public Icon getIcon(final Entry entry) {
        if (entry.getAttribute(ICON_INSTANCE) != null)
            return entry.getAttribute(ICON_INSTANCE);
        String key = (String) entry.getAttribute(ICON);
        if (key == null) {
			String name = entry.getName();
			key = name + ".icon";
		}
        final Icon icon = resourceAccessor.getIcon(key);
        return icon;

	}

	public String getText(final Entry entry) {
		if (entry.getAttribute(TEXT) != null)
			return (String) entry.getAttribute(TEXT);
		else {
			final String textKey = (String) entry.getAttribute(TEXT_KEY);
			if (textKey != null)
				return resourceAccessor.getRawText(textKey);
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

	public Component getComponent(final Entry entry) {
		return (Component) entry.getAttribute(COMPONENT);
	}

	public Object removeComponent(final Entry entry) {
		return entry.removeAttribute(COMPONENT);
	}

	public void setComponent(final Entry entry, Component component) {
		entry.setAttribute(COMPONENT, component);
	}

	public AFreeplaneAction getAction(final Entry entry) {
		return entry.getAction();
	}

	public void setAction(final Entry entry, AFreeplaneAction action) {
		entry.setAction(action);
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
		entry.setAttribute(ICON_INSTANCE, icon);
	}
	
	public void drawMenuIconAlways(Entry entry) {
	    entry.setAttribute(DRAW_MENU_ICON_ALWAYS, Boolean.TRUE);
	}
	
	public boolean removeMenuIcon(Entry entry) {
	    return  ! resourceAccessor.getBooleanProperty(SHOW_MENU_ICONS_PROPERTY, true) 
	            && ! Boolean.TRUE.equals(entry.getAttribute(DRAW_MENU_ICON_ALWAYS));
	}

	public String getAccelerator(Entry entry) {
		String accelerator = (String) entry.getAttribute(ACCELERATOR);
		return accelerator;
	}

	public Entry addChildAction(Entry target, AFreeplaneAction action) {
		final Entry actionEntry = new Entry();
		actionEntry.addConstraint(target);
		actionEntry.setName(action.getKey());
		setAction(actionEntry, action);
		target.addChild(actionEntry);
		return actionEntry;
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
