/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Dimitry Polivaev
 *
 *  This file author is Dimitry Polivaev
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.core.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.io.IElementHandler;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.SetBooleanPropertyAction;
import org.freeplane.core.ui.components.JAutoCheckBoxMenuItem;
import org.freeplane.core.ui.components.JAutoRadioButtonMenuItem;
import org.freeplane.core.ui.components.JAutoToggleButton;
import org.freeplane.core.ui.components.JFreeplaneMenuItem;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.FileUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.n3.nanoxml.XMLElement;

public class MenuBuilder extends UIBuilder {
	private static final String EXTRA_SUBMENU = MenuBuilder.class.getName()+".extra_submenu";
	private static final int MAX_MENU_ITEM_COUNT = ResourceController.getResourceController().getIntProperty("max_menu_item_count");
	private static class ActionHolder implements INameMnemonicHolder {
		final private Action action;

		public ActionHolder(final Action action) {
			super();
			this.action = action;
		}

		/*
		 * (non-Javadoc)
		 * @see freeplane.main.Tools.IAbstractButton#getText()
		 */
		public String getText() {
			return (String) action.getValue(Action.NAME);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * freeplane.main.Tools.IAbstractButton#setDisplayedMnemonicIndex(int)
		 */
		public void setDisplayedMnemonicIndex(final int mnemoSignIndex) {
			action.putValue("SwingDisplayedMnemonicIndexKey", mnemoSignIndex);
			
		}

		/*
		 * (non-Javadoc)
		 * @see freeplane.main.Tools.IAbstractButton#setMnemonic(char)
		 */
		public void setMnemonic(final char charAfterMnemoSign) {
			int vk = charAfterMnemoSign;
			if (vk >= 'a' && vk <= 'z') {
				vk -= ('a' - 'A');
			}
			action.putValue(Action.MNEMONIC_KEY, new Integer(vk));
		}

		/*
		 * (non-Javadoc)
		 * @see freeplane.main.Tools.IAbstractButton#setText(java.lang.String)
		 */
		public void setText(final String text) {
			action.putValue(Action.NAME, text);
		}
	}

	public static class ButtonHolder implements INameMnemonicHolder {
		final private AbstractButton btn;

		public ButtonHolder(final AbstractButton btn) {
			super();
			this.btn = btn;
		}

		/*
		 * (non-Javadoc)
		 * @see freeplane.main.Tools.IAbstractButton#getText()
		 */
		public String getText() {
			return btn.getText();
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * freeplane.main.Tools.IAbstractButton#setDisplayedMnemonicIndex(int)
		 */
		public void setDisplayedMnemonicIndex(final int mnemoSignIndex) {
			btn.setDisplayedMnemonicIndex(mnemoSignIndex);
		}

		/*
		 * (non-Javadoc)
		 * @see freeplane.main.Tools.IAbstractButton#setMnemonic(char)
		 */
		public void setMnemonic(final char charAfterMnemoSign) {
			btn.setMnemonic(charAfterMnemoSign);
		}

		/*
		 * (non-Javadoc)
		 * @see freeplane.main.Tools.IAbstractButton#setText(java.lang.String)
		 */
		public void setText(final String text) {
			btn.setText(text);
		}
	}

	static private class DelegatingPopupMenuListener implements PopupMenuListener {
		final private PopupMenuListener listener;
		final private Object source;

		public DelegatingPopupMenuListener(final PopupMenuListener listener, final Object source) {
			super();
			this.listener = listener;
			this.source = source;
		}

		public Object getSource() {
			return source;
		}

		private PopupMenuEvent newEvent() {
			return new PopupMenuEvent(source);
		}

		public void popupMenuCanceled(final PopupMenuEvent e) {
			listener.popupMenuCanceled(newEvent());
		}

		public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
			listener.popupMenuWillBecomeInvisible(newEvent());
		}

		public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
			listener.popupMenuWillBecomeVisible(newEvent());
		}
	}

	static private class Enabler implements PropertyChangeListener {
		final private WeakReference<Component> comp;

		public Enabler(final Component comp) {
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

	interface INameMnemonicHolder {
		/**
		 */
		String getText();

		/**
		 */
		void setDisplayedMnemonicIndex(int mnemoSignIndex);

		/**
		 */
		void setMnemonic(char charAfterMnemoSign);

		/**
		 */
		void setText(String replaceAll);
	}

	private static class MenuPath {
		static MenuPath emptyPath() {
			final MenuPath menuPath = new MenuPath("");
			menuPath.key = "";
			return menuPath;
		}

		String parentKey;
		String key;

		MenuPath(final String key) {
			parentKey = key;
		}

		void setKey(final String name) {
			key = name;
		}

		void setLastKeySection(final String name) {
			key = parentKey + '/' + name;
		}

		@Override
		public String toString() {
			return key;
		}
	}

	private class MenuStructureReader {
		private final class ActionCreator implements IElementHandler {
			public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
				if (attributes == null) {
					return null;
				}
				final MenuPath menuPath = new MenuPath(parent.toString());
				final String action = attributes.getAttribute("action", null);
				final String plugin = attributes.getAttribute("plugin", null);
				if(plugin != null && ! plugins.contains(plugin))
				    return null;
				try {
				    AFreeplaneAction theAction = modeController.getAction(action);
					if (theAction == null) {
						if(action.startsWith("SetBooleanPropertyAction.")){
							String propertyName = action.substring("SetBooleanPropertyAction.".length());
							theAction = new SetBooleanPropertyAction(propertyName);
							modeController.addAction(theAction);
						}
						else{
						    LogUtils.severe("action " + action + " not found");
						    return null;
						}
					}
					String menuKey = attributes.getAttribute("menu_key", null);
					if(menuKey == null)
					    menuKey = getMenuKey(menuPath.parentKey, theAction.getKey());
					menuPath.setKey(menuKey);
					String accelerator = attributes.getAttribute("accelerator", null);
					if (accelerator != null) {
					    if (Compat.isMacOsX()) {
					        accelerator = accelerator.replaceFirst("CONTROL", "META").replaceFirst("control", "meta");
					    }
					    setDefaultAccelerator(menuPath.key, accelerator);
					}
					if (tag.equals("menu_radio_action")) {
						final JRadioButtonMenuItem item = (JRadioButtonMenuItem) 
						addRadioItem(menuPath.parentKey, menuPath.key, theAction, "true".equals(attributes.getAttribute("selected", "false")));
						if (buttonGroup == null) {
							buttonGroup = new ButtonGroup();
						}
						buttonGroup.add(item);
					}
					else {
					    addAction(menuPath.parentKey, menuPath.key, theAction, MenuBuilder.AS_CHILD);
					}
				}
				catch (final Exception e) {
					LogUtils.severe(e);
				}
				return menuPath;
			}
		}

		private final class CategoryCreator implements IElementHandler {
			public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
				if (attributes == null) {
					return null;
				}
				final String plugin = attributes.getAttribute("plugin", null);
				if(plugin != null && ! plugins.contains(plugin))
					return null;
				buttonGroup = null;
				final MenuPath menuPath = new MenuPath(parent.toString());
				final String menuKey = attributes.getAttribute("menu_key", null);
				if(menuKey == null)
					menuPath.setLastKeySection(attributes.getAttribute("name", null));
				else
					menuPath.setKey(menuKey);
				if (!contains(menuPath.key)) {
					if (tag.equals("menu_submenu")) {
						final JMenu menuItem = new JMenu();
						String nameRef = attributes.getAttribute("name_ref", null);
						if(nameRef == null)
						    nameRef = attributes.getAttribute("name", null);
						final String iconResource = ResourceController.getResourceController().getProperty(nameRef + ".icon", null);
						MenuBuilder.setLabelAndMnemonic(menuItem, TextUtils.getRawText(nameRef));
						if(iconResource != null){
							final URL url = ResourceController.getResourceController().getResource(iconResource);
							menuItem.setIcon(new ImageIcon(url));
						}
						addMenuItem(menuPath.parentKey, menuItem, menuPath.key, MenuBuilder.AS_CHILD);
					}
					else {
						if (!(menuPath.parentKey.equals(""))) {
							addMenuItemGroup(menuPath.parentKey, menuPath.key, MenuBuilder.AS_CHILD);
						}
					}
				}
				return menuPath;
			}
		}

		private final class SeparatorCreator implements IElementHandler {
			public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
				addSeparator(parent.toString(), MenuBuilder.AS_CHILD);
				return parent;
			}
		}

		private final class StructureCreator implements IElementHandler {
			public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
				return MenuPath.emptyPath();
			}
		}

		private ButtonGroup buttonGroup;
		final private ReadManager readManager;

		MenuStructureReader() {
			readManager = new ReadManager();
			readManager.addElementHandler("menu_structure", new StructureCreator());
			readManager.addElementHandler("menu_category", new CategoryCreator());
			readManager.addElementHandler("menu_submenu", new CategoryCreator());
			readManager.addElementHandler("menu_action", new ActionCreator());
			readManager.addElementHandler("menu_radio_action", new ActionCreator());
			readManager.addElementHandler("menu_separator", new SeparatorCreator());
		}

		public void processMenu(final URL menu) {
			InputStreamReader streamReader = null;
			try {
				streamReader = new InputStreamReader(new BufferedInputStream(menu.openStream()));
				final TreeXmlReader reader = new TreeXmlReader(readManager);
				reader.load(streamReader);
			}
			catch (final Exception e) {
				throw new RuntimeException(e);
			}
	        finally {
	        	FileUtils.silentlyClose(streamReader);
	        }
		}
	}

	private static Insets nullInsets = new Insets(0, 0, 0, 0);
	private static final String SHORTCUT_PROPERTY_PREFIX = "acceleratorFor";

	static public JMenu createMenu(final String name) {
		final JMenu menu = new JMenu();
		final String text = TextUtils.getRawText(name);
		MenuBuilder.setLabelAndMnemonic(menu, text);
		return menu;
	}

	static public JMenuItem createMenuItem(final String name) {
		final JMenuItem menu = new JFreeplaneMenuItem();
		final String text = TextUtils.getRawText(name);
		MenuBuilder.setLabelAndMnemonic(menu, text);
		return menu;
	}

	public static void loadAcceleratorPresets(final InputStream in) {
		final Properties prop = new Properties();
		try {
			prop.load(in);
			for (final Entry<Object, Object> property : prop.entrySet()) {
				final String shortcutKey = (String) property.getKey();
				final String keystrokeString = (String) property.getValue();
				if (!shortcutKey.startsWith(SHORTCUT_PROPERTY_PREFIX)) {
					LogUtils.warn("wrong property key " + shortcutKey);
					continue;
				}
				final int pos = shortcutKey.indexOf("/", SHORTCUT_PROPERTY_PREFIX.length());
				if (pos <= 0) {
					LogUtils.warn("wrong property key " + shortcutKey);
					continue;
				}
				final String modeName = shortcutKey.substring(SHORTCUT_PROPERTY_PREFIX.length(), pos);
				final String itemKey = shortcutKey.substring(pos + 1);
				Controller controller = Controller.getCurrentController();
				final ModeController modeController = controller.getModeController(modeName);
				if (modeController == null) {
					LogUtils.warn("unknown mode name in " + shortcutKey);
					continue;
				}
				final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
				final Node node = (Node) menuBuilder.get(itemKey);
				if (node == null) {
					LogUtils.warn("wrong key in " + shortcutKey);
					continue;
				}
				final Object obj = node.getUserObject();
				if (!(obj instanceof JMenuItem)) {
					LogUtils.warn("wrong key in " + shortcutKey);
					continue;
				}
				final KeyStroke keyStroke;
				if (!keystrokeString.equals("")) {
					keyStroke = UITools.getKeyStroke(keystrokeString);
					final Node oldNode = menuBuilder.getMenuItemForKeystroke(keyStroke);
					if (oldNode != null) {
						menuBuilder.setAccelerator(oldNode, null);
						final Object key = oldNode.getKey();
						final String oldShortcutKey = menuBuilder.getShortcutKey(key.toString());
						ResourceController.getResourceController().setProperty(oldShortcutKey, "");
					}
				}
				else {
					keyStroke = null;
				}
				menuBuilder.setAccelerator(node, keyStroke);
				ResourceController.getResourceController().setProperty(shortcutKey, keystrokeString);
			}
		}
		catch (final IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Ampersand indicates that the character after it is a mnemo, unless the
	 * character is a space. In "Find & Replace", ampersand does not label
	 * mnemo, while in "&About", mnemo is "Alt + A".
	 */
	public static void setLabelAndMnemonic(final AbstractButton btn, final String inLabel) {
		MenuBuilder.setLabelAndMnemonic(new ButtonHolder(btn), inLabel);
	}

	/**
	 * Ampersand indicates that the character after it is a mnemo, unless the
	 * character is a space. In "Find & Replace", ampersand does not label
	 * mnemo, while in "&About", mnemo is "Alt + A".
	 */
	public static void setLabelAndMnemonic(final Action action, final String inLabel) {
		MenuBuilder.setLabelAndMnemonic(new ActionHolder(action), inLabel);
	}

	private static void setLabelAndMnemonic(final INameMnemonicHolder item, final String inLabel) {
		String rawLabel = inLabel;
		if (rawLabel == null) {
			rawLabel = item.getText();
		}
		if (rawLabel == null) {
			return;
		}
		item.setText(TextUtils.removeMnemonic(rawLabel));
		final int mnemoSignIndex = rawLabel.indexOf('&');
		if (mnemoSignIndex >= 0 && mnemoSignIndex + 1 < rawLabel.length()) {
			final char charAfterMnemoSign = rawLabel.charAt(mnemoSignIndex + 1);
			if (charAfterMnemoSign != ' ') {
				if (!Compat.isMacOsX()) {
					item.setMnemonic(charAfterMnemoSign);
					item.setDisplayedMnemonicIndex(mnemoSignIndex);
				}
			}
		}
	}

	private IAcceleratorChangeListener acceleratorChangeListener;
	private final Map<KeyStroke, Node> accelerators = new HashMap<KeyStroke, Node>();
 	final private ModeController modeController;
	final MenuStructureReader reader;
	private Set<String> plugins;

	public MenuBuilder(ModeController modeController) {
		super(null);
		this.modeController = modeController;
		reader = new MenuStructureReader();
	}

	private void setDefaultAccelerator(final String itemKey, final String accelerator) {
		final String shortcutKey = getShortcutKey(itemKey);
		if (null == ResourceController.getResourceController().getProperty(shortcutKey, null)) {
			ResourceController.getResourceController().setDefaultProperty(shortcutKey, accelerator);
		}
	}

	/**
	 * @return returns the new JMenuItem.
	 */
	public void addAction(final String category, final AFreeplaneAction action, final int position) {
		final String menuKey = getMenuKey(category, action.getKey());
		addAction(category, menuKey, action, position);
	}

    public String getMenuKey(final String category, String actionKey) {
		actionKey = "$" + actionKey + '$';
		for (int i = 0; i < 1000; i++) {
			final String key = actionKey + i;
			if (null == get(key)) {
				return key;
			}
		}
		return category + '/' + actionKey;
	}

	public void addAction(final String category, final String key, final AFreeplaneAction action, final int position) {
		assert action != null;
		assert key != null;
		if (getContainer(get(category), Container.class) instanceof JToolBar) {
			addButton(category, action, key, position);
			return;
		}
		final JMenuItem item;
		if (action.getClass().getAnnotation(SelectableAction.class) != null) {
			item = new JAutoCheckBoxMenuItem(decorateAction(category, action));
		}
		else {
			item = new JFreeplaneMenuItem(decorateAction(category, action));
		}
		addMenuItem(category, item, key, position);
		addListeners(key, action);
		return;
	}

	private void addListeners(final String key, final AFreeplaneAction action) {
		if (action instanceof PopupMenuListener) {
			addPopupMenuListener(key, (PopupMenuListener) action);
		}
		if (AFreeplaneAction.checkSelectionOnPopup(action)) {
			addPopupMenuListener(key, new PopupMenuListener() {
				public void popupMenuCanceled(final PopupMenuEvent e) {
				}

				public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
				}

				public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
					if(action.isEnabled())
						action.setSelected();
				}
			});
		}
	}

	private void addButton(final String category, final Action action, final String key, final int position) {
		final AbstractButton button;
		assert action != null;
		if (action.getClass().getAnnotation(SelectableAction.class) != null) {
			button = new JAutoToggleButton(action);
		}
		else {
			button = new JButton(action);
		}
		addElement(category, button, key, position);
	}

	@Override
	protected void addComponent(final Container container, final Component component, final int index) {
		if (container instanceof JMenu) {
			final JMenu menu = (JMenu) container;
			final JPopupMenu popupMenu = menu.getPopupMenu();
			final int itemCount = popupMenu.getComponentCount();
			if(itemCount < MAX_MENU_ITEM_COUNT || index < itemCount)
				popupMenu.insert(component, index);
			else{
				final JMenu submenu;
				final Component lastMenuItem = popupMenu.getComponent(itemCount - 1);
				if(itemCount == MAX_MENU_ITEM_COUNT || ! isExtraSubMenu(lastMenuItem)){
					if (component instanceof JPopupMenu.Separator)
						return;
					submenu = new JMenu("");
					submenu.putClientProperty(EXTRA_SUBMENU, Boolean.TRUE);
					popupMenu.add(submenu);
				}
				else{
					submenu = (JMenu) lastMenuItem;
				}
				addComponent(submenu, component, submenu.getPopupMenu().getComponentCount());
			}
			return;
		}
		if (container instanceof JToolBar && component instanceof AbstractButton) {
			{
				((AbstractButton) component).setMargin(MenuBuilder.nullInsets);
			}
		}
		super.addComponent(container, component, index);
	}

	private boolean isExtraSubMenu(final Component c) {
	    return (c instanceof JMenu) &&  (Boolean.TRUE.equals(((JMenu)c).getClientProperty(EXTRA_SUBMENU)));
    }

	public void addComponent(final String parent, final Container item, final Action action, final int position) {
		action.addPropertyChangeListener(new Enabler(item));
		addElement(parent, item, position);
	}

	/**
	 * Add new first level menu bar.
	 */
	public void addMenuBar(final JMenuBar menubar, final String key) {
		addElement(this, menubar, key, UIBuilder.AS_CHILD);
	}

	public void addMenuItem(final String relativeKey, final JMenuItem item, final String key, final int position) {
		final String shortcutKey = getShortcutKey(key);
		final String keyStrokeString = ResourceController.getResourceController().getProperty(shortcutKey);
		final Node element = (Node) addElement(relativeKey, item, key, position);
		if (null == getMenuBar(element)) {
			return;
		}
		if (keyStrokeString != null && !keyStrokeString.equals("")) {
			final KeyStroke keyStroke = UITools.getKeyStroke(keyStrokeString);
			setAccelerator(element, keyStroke);
		}
	}

	public void addMenuItemGroup(final String key, final int position) {
		addElement(this, key, key, position);
	}

	public void addMenuItemGroup(final String relativeKey, final String key, final int position) {
		addElement(relativeKey, key, key, position);
	}

	public void addPopupMenu(final JPopupMenu menu, final String key) {
		addElement(this, menu, key, UIBuilder.AS_CHILD);
	}

	public void addPopupMenuListener(final Object key, final PopupMenuListener listener) {
		final DefaultMutableTreeNode node = get(key);
		assert (node != null);
		final JPopupMenu popup;
		if (node.getUserObject() instanceof JMenu) {
			popup = ((JMenu) node.getUserObject()).getPopupMenu();
		}
		else if (node.getUserObject() instanceof JPopupMenu) {
			popup = (JPopupMenu) node.getUserObject();
		}
		else {
			final Container container = getContainer(((DefaultMutableTreeNode) node.getParent()), Container.class);
			if (container instanceof JPopupMenu) {
				popup = (JPopupMenu) container;
			}
			else if (container instanceof JMenu) {
				popup = ((JMenu) container).getPopupMenu();
			}
			else {
				throw new RuntimeException("no popup menu found!");
			}
		}
		final Object userObject = node.getUserObject();
		popup.addPopupMenuListener(new DelegatingPopupMenuListener(listener, userObject));
	}

	public JMenuItem addRadioItem(final String category, final AFreeplaneAction action, final boolean isSelected) {
		assert action != null;
		final String actionKey = "$" + action.getKey() + '$';
		for(int i = 0; i < 1000; i++){
			String key = actionKey + i;
			if (null == get(key)){
				return addRadioItem(category, key, action, isSelected);
			}
		}
		return addRadioItem(category, category + '/' + actionKey, action, isSelected);
	}

	public JMenuItem addRadioItem(final String category, final String key,
			final AFreeplaneAction action, final boolean isSelected) {
		assert key != null;
		final JRadioButtonMenuItem item;
		if (action.getClass().getAnnotation(SelectableAction.class) != null) {
			item = new JAutoRadioButtonMenuItem(decorateAction(category, action));
		}
		else {
			item = new JRadioButtonMenuItem(decorateAction(category, action));
		}
		addMenuItem(category, item, key, MenuBuilder.AS_CHILD);
		item.setSelected(isSelected);
		addListeners(key, action);
		return item;
	}

	public void addSeparator() {
		addElement(this, new JPopupMenu.Separator(), UIBuilder.AS_CHILD);
	}

	public void addSeparator(final String parentKey, final int position) {
		final Container parent = getContainer(get(parentKey), Container.class);
		if (parent instanceof JMenu || parent instanceof JPopupMenu) {
			addElement(parentKey, new JPopupMenu.Separator(), position);
			return;
		}
		if (parent instanceof JToolBar) {
			final JToolBar t = (JToolBar) parent;
			final JToolBar.Separator s = new JToolBar.Separator();
			addElement(parentKey, s, position);
			if (t.getOrientation() == SwingConstants.VERTICAL) {
				s.setOrientation(SwingConstants.HORIZONTAL);
			}
			else {
				s.setOrientation(SwingConstants.VERTICAL);
			}
			return;
		}
	}

	public void addToolbar(final JToolBar toolbar, final String key) {
		addElement(this, toolbar, key, UIBuilder.AS_CHILD);
	}

	public void afterMapChange(final MapModel newMap) {
		final Iterator<Object> iterator = newObjectIterator();
		while (iterator.hasNext()) {
			final Object next = iterator.next();
			if (next instanceof AbstractButton) {
				final AbstractButton btn = (AbstractButton) next;
				final Action action = btn.getAction();
				if (action instanceof IFreeplaneAction) {
					((IFreeplaneAction) action).afterMapChange(newMap);
				}
			}
		}
	}

	IFreeplaneAction decorateAction(final String category, final AFreeplaneAction action) {
		if (null == getMenuBar(get(category)) || Controller.getCurrentController().getViewController().isApplet()) {
			return action;
		}
		return decorateAction(action);
	}

	public IFreeplaneAction decorateAction(final AFreeplaneAction action) {
		return new AccelerateableAction(this, action);
	}

	public IAcceleratorChangeListener getAcceleratorChangeListener() {
		return acceleratorChangeListener;
	}

	@Override
	protected Component getChildComponent(final Container parentComponent, final int index) {
		if (parentComponent instanceof JMenu) {
			return ((JMenu) parentComponent).getMenuComponent(index);
		}
		return super.getChildComponent(parentComponent, index);
	}
	
	@Override
	protected Container getNextParentComponent(Container parentComponent) {
		if(parentComponent.getComponentCount() > 0 && parentComponent instanceof JMenu)
		{
			final Component lastComponent = parentComponent.getComponent(parentComponent.getComponentCount()-1);
			if(isExtraSubMenu(lastComponent))
				return (Container) lastComponent;
		}
		return null;
    }

	public Node getMenuBar(DefaultMutableTreeNode element) {
	    while (element != null) {
			final Object userObject = element.getUserObject();
			if (userObject instanceof JMenuBar) {
				return (Node) element;
			}
			element = (DefaultMutableTreeNode) element.getParent();
		} 
		return null;
    }

	private Node getMenuItemForKeystroke(final KeyStroke keyStroke) {
		return accelerators.get(keyStroke);
	}

	@Override
	protected DefaultMutableTreeNode getNode(final Object parentKey) {
		final DefaultMutableTreeNode parentNode = super.getNode(parentKey);
		return parentNode;
	}

	@Override
	protected int getParentComponentCount(final Container parentComponent) {
		if (parentComponent instanceof JMenu) {
			return ((JMenu) parentComponent).getMenuComponentCount();
		}
		return super.getParentComponentCount(parentComponent);
	}

	String getShortcutKey(final String key) {
		return SHORTCUT_PROPERTY_PREFIX + modeController.getModeName() + "/" + key;
	}

	public void processMenuCategory(final URL menu, Set<String> plugins) {
		final Set<String> oldPlugins = this.plugins;
		this.plugins = plugins;
		try{
			reader.processMenu(menu);
		}
		finally{
			this.plugins = oldPlugins;
		}
	}

	private KeyStroke removeAccelerator(final Node node) throws AssertionError {
		final KeyStroke oldAccelerator = ((JMenuItem) node.getUserObject()).getAccelerator();
		if (oldAccelerator != null) {
			final Node oldNode = accelerators.remove(oldAccelerator);
			if (!node.equals(oldNode)) {
				throw new AssertionError("unexpected action " + "for accelerator " + oldAccelerator);
			}
		}
		return oldAccelerator;
	}

	@SuppressWarnings("unchecked")
	private void removeAccelerators(final DefaultMutableTreeNode node) {
		final Object userObject = node.getUserObject();
		if (userObject instanceof JMenuItem && !(userObject instanceof JMenu)) {
			setAccelerator((Node) node, null);
		}
		for (final Enumeration<Object> children = node.children(); children.hasMoreElements();) {
			removeAccelerators((DefaultMutableTreeNode) children.nextElement());
		}
	}

	@Override
	protected void removeChildComponents(final Container parentComponent, final DefaultMutableTreeNode node) {
		removeAccelerators(node);
		if (parentComponent instanceof JMenu) {
			final JMenu menu = (JMenu) parentComponent;
			final JPopupMenu popupMenu = menu.getPopupMenu();
			super.removeChildComponents(popupMenu, node);
			for(int i = popupMenu.getComponentCount()-1; i >= 0; i--){
				final Component component = popupMenu.getComponent(i);
				if(isExtraSubMenu(component)){
					final Container container = (Container) component;
					super.removeChildComponents(container, node);
					if(container.getComponentCount() == 0)
						popupMenu.remove(container);
				}
			}
		}
		else{
			super.removeChildComponents(parentComponent, node);
			
		}
	}

	public void removePopupMenuListener(final Object key, final PopupMenuListener listener) {
		final DefaultMutableTreeNode node = get(key);
		final Container container = getContainer(node, Container.class);
		final JPopupMenu popup;
		if (container instanceof JPopupMenu) {
			popup = (JPopupMenu) container;
		}
		else if (container instanceof JMenu) {
			popup = ((JMenu) container).getPopupMenu();
		}
		else {
			throw new RuntimeException("no popup menu found!");
		}
		final Object userObject = node.getUserObject();
		final PopupMenuListener[] popupMenuListeners = popup.getPopupMenuListeners();
		for (int i = 0; i < popupMenuListeners.length; i++) {
			final PopupMenuListener popupMenuListener = popupMenuListeners[i];
			if (!(popupMenuListener instanceof DelegatingPopupMenuListener)
			        || !(((DelegatingPopupMenuListener) popupMenuListener).getSource() == userObject)) {
				continue;
			}
			popup.removePopupMenuListener(popupMenuListener);
			break;
		}
	}

	void setAccelerator(final Node node, final KeyStroke keyStroke) {
		final Node oldAction = accelerators.put(keyStroke, node);
		final JMenuItem item = (JMenuItem) node.getUserObject();
		if (keyStroke != null && oldAction != null) {
			UITools.errorMessage(TextUtils.format("action_keystroke_in_use_error", keyStroke, item
			    .getActionCommand(), ((JMenuItem) oldAction.getUserObject()).getActionCommand()));
			accelerators.put(keyStroke, oldAction);
			final String shortcutKey = getShortcutKey(node.getKey().toString());
			ResourceController.getResourceController().setProperty(shortcutKey, "");
			return;
		}
		if (item instanceof JMenu) {
			UITools.errorMessage(TextUtils.format("submenu_keystroke_in_use_error", keyStroke, item.getText()));
			accelerators.put(keyStroke, oldAction);
			final String shortcutKey = getShortcutKey(node.getKey().toString());
			ResourceController.getResourceController().setProperty(shortcutKey, "");
			return;
		}
		final KeyStroke removedAccelerator = removeAccelerator(node);
		item.setAccelerator(keyStroke);
		if (acceleratorChangeListener != null && (removedAccelerator != null || keyStroke != null)) {
			acceleratorChangeListener.acceleratorChanged(item, removedAccelerator, keyStroke);
		}
	}

	public void setAcceleratorChangeListener(final IAcceleratorChangeListener acceleratorChangeListener) {
		this.acceleratorChangeListener = acceleratorChangeListener;
	}

	public Map<KeyStroke, Node> getAcceleratorMap() {
		return Collections.unmodifiableMap(accelerators);
	}
}
