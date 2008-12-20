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
package org.freeplane.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;

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

import org.freeplane.controller.ActionDescriptor;
import org.freeplane.controller.Controller;
import org.freeplane.controller.FreeplaneAction;
import org.freeplane.io.NodeCreatorAdapter;
import org.freeplane.io.ReadManager;
import org.freeplane.io.xml.TreeXmlReader;
import org.freeplane.io.xml.n3.nanoxml.IXMLElement;
import org.freeplane.main.Tools;
import org.freeplane.modes.ModeController;

public class MenuBuilder extends UIBuilder {
	private static class ActionHolder implements INameMnemonicHolder {
		final private Action action;

		public ActionHolder(final Action action) {
			super();
			this.action = action;
		}

		/*
		 * (non-Javadoc)
		 * @see freemind.main.Tools.IAbstractButton#getText()
		 */
		public String getText() {
			return (String) action.getValue(Action.NAME);
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * freemind.main.Tools.IAbstractButton#setDisplayedMnemonicIndex(int)
		 */
		public void setDisplayedMnemonicIndex(final int mnemoSignIndex) {
		}

		/*
		 * (non-Javadoc)
		 * @see freemind.main.Tools.IAbstractButton#setMnemonic(char)
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
		 * @see freemind.main.Tools.IAbstractButton#setText(java.lang.String)
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
		 * @see freemind.main.Tools.IAbstractButton#getText()
		 */
		public String getText() {
			return btn.getText();
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * freemind.main.Tools.IAbstractButton#setDisplayedMnemonicIndex(int)
		 */
		public void setDisplayedMnemonicIndex(final int mnemoSignIndex) {
			btn.setDisplayedMnemonicIndex(mnemoSignIndex);
		}

		/*
		 * (non-Javadoc)
		 * @see freemind.main.Tools.IAbstractButton#setMnemonic(char)
		 */
		public void setMnemonic(final char charAfterMnemoSign) {
			btn.setMnemonic(charAfterMnemoSign);
		}

		/*
		 * (non-Javadoc)
		 * @see freemind.main.Tools.IAbstractButton#setText(java.lang.String)
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
			menuPath.path = "";
			return menuPath;
		}

		String parentPath;
		String path;

		MenuPath(final String path) {
			parentPath = path;
		}

		void setName(final String name) {
			path = parentPath + '/' + name;
		}

		@Override
		public String toString() {
			return path;
		}
	}

	private class MenuStructureReader {
		private final class ActionCreator extends NodeCreatorAdapter {
			public Object createNode(final Object parent, final String tag) {
				return new MenuPath(parent.toString());
			}

			@Override
			public void setAttributes(final String tag, final Object node,
			                          final IXMLElement attributes) {
				final MenuPath menuPath = (MenuPath) node;
				final String field = attributes.getAttribute("field", null);
				String name = attributes.getAttribute("name", null);
				if (name == null) {
					name = field;
				}
				menuPath.setName(name);
				final String keystroke = attributes.getAttribute("key_ref", null);
				try {
					final Action theAction = modeController.getAction(field);
					if (tag.equals("menu_radio_action")) {
						final JRadioButtonMenuItem item = (JRadioButtonMenuItem) addRadioItem(
						    menuPath.parentPath, theAction, keystroke, "true".equals(attributes
						        .getAttribute("selected", "false")));
						if (buttonGroup == null) {
							buttonGroup = new ButtonGroup();
						}
						buttonGroup.add(item);
					}
					else {
						addAction(menuPath.parentPath, menuPath.path, theAction, keystroke,
						    MenuBuilder.AS_CHILD);
					}
				}
				catch (final Exception e1) {
					org.freeplane.main.Tools.logException(e1);
				}
			}
		}

		private final class CategoryCreator extends NodeCreatorAdapter {
			public Object createNode(final Object parent, final String tag) {
				buttonGroup = null;
				return new MenuPath(parent.toString());
			}

			@Override
			public void setAttributes(final String tag, final Object node,
			                          final IXMLElement attributes) {
				final MenuPath menuPath = (MenuPath) node;
				menuPath.setName(attributes.getAttribute("name", null));
				if (!contains(menuPath.path)) {
					if (tag.equals("menu_submenu")) {
						final JMenu menuItem = new JMenu();
						MenuBuilder.setLabelAndMnemonic(menuItem, Controller.getText(attributes
						    .getAttribute("name_ref", null)));
						addMenuItem(menuPath.parentPath, menuItem, menuPath.path,
						    MenuBuilder.AS_CHILD);
					}
					else {
						if (!(menuPath.parentPath.equals(""))) {
							addMenuItemGroup(menuPath.parentPath, menuPath.path,
							    MenuBuilder.AS_CHILD);
						}
					}
				}
			}
		}

		private final class SeparatorCreator extends NodeCreatorAdapter {
			public Object createNode(final Object parent, final String tag) {
				addSeparator(parent.toString(), MenuBuilder.AS_CHILD);
				return parent;
			}
		}

		private final class StructureCreator extends NodeCreatorAdapter {
			public Object createNode(final Object parent, final String tag) {
				return MenuPath.emptyPath();
			}
		}

		private ButtonGroup buttonGroup;
		final private ReadManager readManager;

		MenuStructureReader() {
			readManager = new ReadManager();
			readManager.addNodeCreator("menu_structure", new StructureCreator());
			readManager.addNodeCreator("menu_category", new CategoryCreator());
			readManager.addNodeCreator("menu_submenu", new CategoryCreator());
			readManager.addNodeCreator("menu_action", new ActionCreator());
			readManager.addNodeCreator("menu_radio_action", new ActionCreator());
			readManager.addNodeCreator("menu_separator", new SeparatorCreator());
		}

		public void processMenu(final URL menu) {
			final TreeXmlReader reader = new TreeXmlReader(readManager);
			try {
				reader.load(new InputStreamReader(menu.openStream()));
			}
			catch (final IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private static Insets nullInsets = new Insets(0, 0, 0, 0);

	static public JMenu createMenu(final String name) {
		final JMenu menu = new JMenu();
		final String text = Controller.getText(name);
		MenuBuilder.setLabelAndMnemonic(menu, text);
		return menu;
	}

	static public JMenuItem createMenuItem(final String name) {
		final JMenuItem menu = new JMenuItem();
		final String text = Controller.getText(name);
		MenuBuilder.setLabelAndMnemonic(menu, text);
		return menu;
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
		item.setText(Tools.removeMnemonic(rawLabel));
		final int mnemoSignIndex = rawLabel.indexOf("&");
		if (mnemoSignIndex >= 0 && mnemoSignIndex + 1 < rawLabel.length()) {
			final char charAfterMnemoSign = rawLabel.charAt(mnemoSignIndex + 1);
			if (charAfterMnemoSign != ' ') {
				if (!Tools.isMacOsX()) {
					item.setMnemonic(charAfterMnemoSign);
					item.setDisplayedMnemonicIndex(mnemoSignIndex);
				}
			}
		}
	}

	final private ModeController modeController;
	final MenuStructureReader reader;

	public MenuBuilder(final ModeController modeController) {
		super(null);
		this.modeController = modeController;
		reader = new MenuStructureReader();
	}

	public void addAction(final FreeplaneAction action, final ActionDescriptor actionAnnotation) {
		final String docu = actionAnnotation.tooltip();
		if (!docu.equals("")) {
			action.setTooltip(docu);
		}
		final String actionName = actionAnnotation.name();
		MenuBuilder.setLabelAndMnemonic(action, Controller.getText(actionName));
		final String iconPath = actionAnnotation.iconPath();
		if (!iconPath.equals("")) {
			final ImageIcon icon = new ImageIcon(Controller.getResourceController().getResource(
			    iconPath));
			action.putValue(Action.SMALL_ICON, icon);
		}
		final String[] actionLocations = actionAnnotation.locations();
		String keystroke = actionAnnotation.keyStroke();
		if (keystroke.equals("")) {
			keystroke = null;
		}
		for (int i = 0; i < actionLocations.length; i++) {
			final String key = actionLocations.length == 0 ? actionName : actionName + "[" + i
			        + "]";
			addAction(actionLocations[i], key, action, keystroke, MenuBuilder.AS_CHILD);
		}
	}

	/**
	 * @return returns the new JMenuItem.
	 * @param keystroke
	 *            can be null, if no keystroke should be assigned.
	 */
	public void addAction(final String category, final Action action, final String keystroke,
	                      final int position) {
		addAction(category, null, action, keystroke, position);
	}

	public void addAction(final String category, final String key, final Action action,
	                      final String keystroke, final int position) {
		assert action != null;
		if (getContainer(get(category), Container.class) instanceof JToolBar) {
			addButton(category, action, position);
			return;
		}
		final JMenuItem item;
		if (action.getClass().getAnnotation(SelectableAction.class) != null) {
			item = new JAutoCheckBoxMenuItem((action));
		}
		else if (action.getClass().getAnnotation(VisibleAction.class) != null) {
			item = new JAutoVisibleMenuItem(action);
		}
		else {
			item = new JMenuItem(action);
		}
		if (key != null) {
			addMenuItem(category, item, key, position);
		}
		else {
			addMenuItem(category, item, position);
		}
		if (action instanceof PopupMenuListener) {
			addPopupMenuListener(key, (PopupMenuListener) action);
		}
		if (FreeplaneAction.checkSelectionOnPopup(action)) {
			addPopupMenuListener(key, new PopupMenuListener() {
				public void popupMenuCanceled(final PopupMenuEvent e) {
				}

				public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
				}

				public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
					((FreeplaneAction) action).setSelected();
				}
			});
		}
		if (FreeplaneAction.checkVisibilityOnPopup(action)) {
			addPopupMenuListener(key, new PopupMenuListener() {
				public void popupMenuCanceled(final PopupMenuEvent e) {
				}

				public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
				}

				public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
					((FreeplaneAction) action).setVisible();
				}
			});
		}
		if (FreeplaneAction.checkEnabledOnPopup(action)) {
			addPopupMenuListener(key, new PopupMenuListener() {
				public void popupMenuCanceled(final PopupMenuEvent e) {
				}

				public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
				}

				public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
					((FreeplaneAction) action).setEnabled();
				}
			});
		}
		if (keystroke != null) {
			final String keyProperty = Controller.getResourceController().getAdjustableProperty(
			    keystroke);
			item.setAccelerator(KeyStroke.getKeyStroke(keyProperty));
		}
		return;
	}

	public void addAnnotatedAction(final FreeplaneAction action) {
		addAction(action, action.getClass().getAnnotation(ActionDescriptor.class));
	}

	private void addButton(final String category, final Action action, final int position) {
		final AbstractButton button;
		if (action.getClass().getAnnotation(SelectableAction.class) != null) {
			button = new JAutoToggleButton(action);
		}
		else {
			button = new JButton(action);
		}
		button.setText(null);
		addComponent(category, button, position);
	}

	@Override
	protected void addComponent(final Container container, final Component component,
	                            final int index) {
		if (container instanceof JMenu) {
			final JMenu menu = (JMenu) container;
			menu.getPopupMenu().insert(component, index);
			return;
		}
		if (container instanceof JToolBar && component instanceof AbstractButton) {
			{
				((AbstractButton) component).setMargin(MenuBuilder.nullInsets);
			}
		}
		super.addComponent(container, component, index);
	}

	public void addComponent(final String parent, final Container item, final Action action,
	                         final int position) {
		action.addPropertyChangeListener(new Enabler(item));
		addElement(parent, item, position);
	}

	public void addComponent(final String parent, final Container item, final int position) {
		addElement(parent, item, position);
	}

	/**
	 * Add new first level menu bar.
	 */
	public void addMenuBar(final JMenuBar menubar, final String key) {
		addElement(this, menubar, key, UIBuilder.AS_CHILD);
	}

	public void addMenuItem(final JMenuItem item) {
		addElement(this, item, UIBuilder.AS_CHILD);
	}

	public void addMenuItem(final String relativeKey, final JMenuItem item, final int position) {
		addElement(relativeKey, item, position);
	}

	public void addMenuItem(final String relativeKey, final JMenuItem item, final String key,
	                        final int position) {
		addElement(relativeKey, item, key, position);
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
		if (node.getUserObject() instanceof JPopupMenu) {
			popup = (JPopupMenu) node.getUserObject();
		}
		else {
			final Container container = getContainer(((DefaultMutableTreeNode) node.getParent()),
			    Container.class);
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

	public JMenuItem addRadioItem(final String category, final Action action,
	                              final String keystroke, final boolean isSelected) {
		final JRadioButtonMenuItem item = new JRadioButtonMenuItem(action);
		addMenuItem(category, item, MenuBuilder.AS_CHILD);
		if (keystroke != null) {
			item.setAccelerator(KeyStroke.getKeyStroke(Controller.getResourceController()
			    .getAdjustableProperty(keystroke)));
		}
		item.setSelected(isSelected);
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

	@Override
	protected Component getChildComponent(final Container parentComponent, final int index) {
		if (parentComponent instanceof JMenu) {
			return ((JMenu) parentComponent).getMenuComponent(index);
		}
		return super.getChildComponent(parentComponent, index);
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

	public void processMenuCategory(final URL menu) {
		reader.processMenu(menu);
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
}
