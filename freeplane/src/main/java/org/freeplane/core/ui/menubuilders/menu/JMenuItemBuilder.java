package org.freeplane.core.ui.menubuilders.menu;

import java.awt.Component;
import java.awt.Container;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;

import org.dpolivaev.mnemonicsetter.MnemonicSetter;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ActionEnabler;
import org.freeplane.core.ui.LabelAndMnemonicSetter;
import org.freeplane.core.ui.MenuSplitter;
import org.freeplane.core.ui.MenuSplitterConfiguration;
import org.freeplane.core.ui.menubuilders.action.AcceleratebleActionProvider;
import org.freeplane.core.ui.menubuilders.action.IAcceleratorMap;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryPopupListener;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.ResourceAccessor;
import org.freeplane.core.ui.svgicons.FreeplaneIconFactory;
import org.freeplane.core.ui.textchanger.TranslatedElement;
import org.freeplane.core.util.LogUtils;

public class JMenuItemBuilder implements EntryVisitor{

	final private EntryPopupListener popupListener;
	final ResourceAccessor resourceAccessor;
	final private MenuSplitter menuSplitter;
	final private EntryAccessor entryAccessor;
	final private ComponentProvider menuActionComponentProvider;

	public JMenuItemBuilder(EntryPopupListener popupListener, IAcceleratorMap accelerators,
	                        AcceleratebleActionProvider acceleratebleActionProvider, ResourceAccessor resourceAccessor) {
		this(popupListener,
		    new MenuActionComponentProvider(accelerators, acceleratebleActionProvider, resourceAccessor),
		    resourceAccessor);
	}

	public JMenuItemBuilder(EntryPopupListener popupListener, ComponentProvider menuActionComponentProvider,
	                        ResourceAccessor resourceAccessor) {
		this.popupListener = popupListener;
		this.resourceAccessor = resourceAccessor;
		this.entryAccessor = new EntryAccessor(resourceAccessor);
		menuSplitter = new MenuSplitter(resourceAccessor.getIntProperty(
		    MenuSplitterConfiguration.MAX_MENU_ITEM_COUNT_KEY, 10));
		this.menuActionComponentProvider = menuActionComponentProvider;
	}

	@Override
	public void visit(Entry entry) {
		if ((entry.hasChildren() || entryAccessor.getAction(entry) == null) && !entryAccessor.getText(entry).isEmpty())
			addSubmenu(entry);
		else
			addActionItem(entry);
	}

	private void addActionItem(Entry entry) {
		final Component actionComponent = createActionComponent(entry);
		if(actionComponent != null){
			setTranslationKey(entry, actionComponent);
			addComponent(entry, actionComponent);
		}
	}

	private Component createActionComponent(Entry entry) {
		
		// FIXME actually not possible
		final Object alreadyExistingComponent = entryAccessor.getComponent(entry);
		if(alreadyExistingComponent != null) {
			LogUtils.severe("BUG : component already exists at " + entry.getPath());
			return null;
		}
		
	    final Component component = menuActionComponentProvider.createComponent(entry);
		final AFreeplaneAction action = entryAccessor.getAction(entry);
		if (action != null) {
			final ActionEnabler actionEnabler = new ActionEnabler(component);
			action.addPropertyChangeListener(actionEnabler);
			entry.setAttribute(actionEnabler.getClass(), actionEnabler);
		}
		return component;
    }

	private void addComponent(Entry entry, final Component component) {
		entryAccessor.setComponent(entry, component);
		final Container container = (Container) entryAccessor.getAncestorComponent(entry);
		if (container != null)
			menuSplitter.addComponent(container, component);
    }

	private void addSubmenu(final Entry entry) {
		final Component actionComponent = createActionComponent(entry);
		JMenu menu = new JMenu();
		final String rawText = entryAccessor.getText(entry);
		LabelAndMnemonicSetter.setLabelAndMnemonic(menu, rawText);
		final Icon icon = entryAccessor.getIcon(entry);
		if (icon != null) {
			// SR - Don't use menu icons in macOS
			//menu.setIcon(FreeplaneIconFactory.toImageIcon(icon));
		}
		addComponent(entry, menu);
		if(actionComponent != null){
			menuSplitter.addMenuComponent(menu, actionComponent);
		}
		final JPopupMenu popupMenu = menu.getPopupMenu();
		popupMenu.addPopupMenuListener(new PopupMenuListenerForEntry(entry, popupListener));
		popupMenu.addPopupMenuListener(MnemonicSetter.INSTANCE);
		setTranslationKey(entry, menu);

	}

	private void setTranslationKey(final Entry entry, Component actionComponent) {
		if(actionComponent instanceof JComponent) {
			final String textKey = entryAccessor.getTextKey(entry);
			if (textKey != null)
				TranslatedElement.TEXT.setKey((JComponent) actionComponent, textKey);
			String tooltipKey = entryAccessor.getTooltipKey(entry);
			if (textKey != null)
				TranslatedElement.TOOLTIP.setKey((JComponent) actionComponent, tooltipKey);
		}
	}

	@Override
	public boolean shouldSkipChildren(Entry entry) {
		return false;
	}

}
