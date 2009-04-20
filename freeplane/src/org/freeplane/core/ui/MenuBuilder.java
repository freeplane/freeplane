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
import java.awt.Event;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.tree.DefaultMutableTreeNode;

import org.freeplane.core.Compat;
import org.freeplane.core.io.IElementHandler;
import org.freeplane.core.io.ReadManager;
import org.freeplane.core.io.xml.TreeXmlReader;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.resources.FreeplaneResourceBundle;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.GrabKeyDialog;
import org.freeplane.core.ui.components.JAutoCheckBoxMenuItem;
import org.freeplane.core.ui.components.JAutoRadioButtonMenuItem;
import org.freeplane.core.ui.components.JAutoToggleButton;
import org.freeplane.core.ui.components.JAutoVisibleMenuItem;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.LogTool;
import org.freeplane.n3.nanoxml.XMLElement;

public class MenuBuilder extends UIBuilder {
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
		private final class ActionCreator implements IElementHandler {
			public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
				if (attributes == null) {
					return null;
				}
				final MenuPath menuPath = new MenuPath(parent.toString());
				final String action = attributes.getAttribute("action", null);
				menuPath.setName(action);
				final String accelerator = attributes.getAttribute("accelerator", null);
				if(accelerator != null){
					String shortcutKey = getShortcutKey(menuPath.path);
					ResourceController.getResourceController().setDefaultProperty(shortcutKey, accelerator);
				}
				try {
					final AFreeplaneAction theAction = modeController.getAction(action);
					assert  theAction != null;
					if (tag.equals("menu_radio_action")) {
						final JRadioButtonMenuItem item = (JRadioButtonMenuItem) addRadioItem(menuPath.parentPath,
						    theAction, "true".equals(attributes.getAttribute("selected", "false")));
						if (buttonGroup == null) {
							buttonGroup = new ButtonGroup();
						}
						buttonGroup.add(item);
					}
					else {
						addAction(menuPath.parentPath, theAction, MenuBuilder.AS_CHILD);
					}
				}
				catch (final Exception e1) {
					LogTool.logException(e1);
				}
				return menuPath;
			}
		}

		private final class CategoryCreator implements IElementHandler {
			public Object createElement(final Object parent, final String tag, final XMLElement attributes) {
				if (attributes == null) {
					return null;
				}
				buttonGroup = null;
				final MenuPath menuPath = new MenuPath(parent.toString());
				menuPath.setName(attributes.getAttribute("name", null));
				if (!contains(menuPath.path)) {
					if (tag.equals("menu_submenu")) {
						final JMenu menuItem = new JMenu();
						MenuBuilder.setLabelAndMnemonic(menuItem, FreeplaneResourceBundle.getText(attributes
						    .getAttribute("name_ref", null)));
						addMenuItem(menuPath.parentPath, menuItem, menuPath.path, MenuBuilder.AS_CHILD);
					}
					else {
						if (!(menuPath.parentPath.equals(""))) {
							addMenuItemGroup(menuPath.parentPath, menuPath.path, MenuBuilder.AS_CHILD);
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
		final String text = FreeplaneResourceBundle.getText(name);
		MenuBuilder.setLabelAndMnemonic(menu, text);
		return menu;
	}

	static public JMenuItem createMenuItem(final String name) {
		final JMenuItem menu = new JMenuItem();
		final String text = FreeplaneResourceBundle.getText(name);
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
		item.setText(FpStringUtils.removeMnemonic(rawLabel));
		final int mnemoSignIndex = rawLabel.indexOf("&");
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

	final private ModeController modeController;
	final MenuStructureReader reader;

	public MenuBuilder(final ModeController modeController) {
		super(null);
		this.modeController = modeController;
		reader = new MenuStructureReader();
	}

	public void addAction(final AFreeplaneAction action, final ActionLocationDescriptor actionAnnotation) {
		final String[] actionLocations = actionAnnotation.locations();
		for (int i = 0; i < actionLocations.length; i++) {
			final String key = actionLocations.length == 0 ? action.getKey() : action.getKey() + "[" + i + "]";
			String itemKey = actionLocations[i] + '/' + key;
			if(i == 0){
				String accelerator = actionAnnotation.accelerator();
				if(! accelerator.equals("")){
					String shortcutKey = getShortcutKey(itemKey);
					ResourceController.getResourceController().setDefaultProperty(shortcutKey, accelerator);
				}
			}
			addAction(actionLocations[i], itemKey, action, MenuBuilder.AS_CHILD);
		}
	}

	Action decorateAction(String category, Action action){
		if (null == getMenubar(get(category))){
			return action;
		}
		return new AccelerateableAction(action);
		
	}
	class AccelerateableAction implements Action{
		final private Action originalAction;

		public void actionPerformed(ActionEvent e) {
			if(! (e.getModifiers() == ActionEvent.CTRL_MASK + InputEvent.BUTTON1_MASK && e.getSource() instanceof JMenuItem)){
		        originalAction.actionPerformed(e);
		        return;
			}
			JMenuItem item = (JMenuItem) e.getSource();
			newAccelerator(item);
			
        }

		private void newAccelerator(final JMenuItem editedItem) {
	        Frame frame = JOptionPane.getFrameForComponent(editedItem);
			final Object key = getKeyByUserObject(editedItem);
			String shortcutKey = getShortcutKey(key.toString());
			final GrabKeyDialog grabKeyDialog = new GrabKeyDialog(frame, 
				ResourceController.getResourceController().getProperty(shortcutKey));
			grabKeyDialog.setValidator(new IKeystrokeValidator(){
				public boolean isValid(KeyStroke keystroke) {
					if(keystroke == null){
						return true;
					}
					Object menubarKey = getMenubar(get(key));
					if(menubarKey == null){
						return true;
					}
					DefaultMutableTreeNode menubarNode = get(menubarKey);
					if((keystroke.getModifiers() & (Event.ALT_MASK | Event.CTRL_MASK)) == Event.ALT_MASK){
						JMenuBar menuBar = (JMenuBar) menubarNode.getUserObject();
						int menuCount = menuBar.getMenuCount();
						for(int i = 0; i < menuCount; i++){
							JMenu menu = menuBar.getMenu(i);
							char c = (char) menu.getMnemonic();
							if(Character.toLowerCase(keystroke.getKeyCode()) == Character.toLowerCase(c)){
								JOptionPane.showMessageDialog (grabKeyDialog, menu.getText(), FreeplaneResourceBundle.getText("used_in_menu"), JOptionPane.WARNING_MESSAGE);
								return false;
							}
						}
					}
					return isValid(menubarNode, keystroke);
                }

				private boolean isValid(DefaultMutableTreeNode menubarNode, KeyStroke keystroke) {
	                Enumeration menuElements = menubarNode.children();
					while (menuElements.hasMoreElements()){
						Node menuItemNode = (Node) menuElements.nextElement();
						Object userObject = menuItemNode.getUserObject();
						if(userObject instanceof JMenuItem){
							JMenuItem menuItem = (JMenuItem) userObject;
							if(keystroke.equals(menuItem.getAccelerator())){
								if(editedItem.equals(menuItem)){
									return true;
								}
								int replace = JOptionPane.showConfirmDialog(grabKeyDialog, menuItem.getText(), FreeplaneResourceBundle.getText("remove_shortcut_question"), JOptionPane.YES_NO_OPTION);
								if(replace == JOptionPane.YES_OPTION){
									menuItem.setAccelerator(null);
									String shortcutKey = getShortcutKey(menuItemNode.getKey().toString());
									ResourceController.getResourceController().setProperty(shortcutKey, "");
									return true;
								}
								return false;
							}
						}
						if (! isValid(menuItemNode, keystroke)) {
							return false;
						}
					}
					return true;
                }});
			grabKeyDialog.setVisible(true);
			if (grabKeyDialog.isOK()) {
				String shortcut = grabKeyDialog.getShortcut();
				KeyStroke accelerator = UITools.getKeyStroke(shortcut);
				editedItem.setAccelerator(accelerator);
				ResourceController.getResourceController().setProperty(shortcutKey, shortcut);
			}
        }

		public void addPropertyChangeListener(PropertyChangeListener listener) {
	        originalAction.addPropertyChangeListener(listener);
        }

		public Object getValue(String key) {
	        return originalAction.getValue(key);
        }

		public boolean isEnabled() {
	        return originalAction.isEnabled();
        }

		public void putValue(String key, Object value) {
	        originalAction.putValue(key, value);
        }

		public void removePropertyChangeListener(PropertyChangeListener listener) {
	        originalAction.removePropertyChangeListener(listener);
        }

		public void setEnabled(boolean b) {
	        originalAction.setEnabled(b);
        }

		public AccelerateableAction(Action originalAction) {
	        super();
	        this.originalAction = originalAction;
        }
	};
	/**
	 * @return returns the new JMenuItem.
	 */
	public void addAction(final String category, final AFreeplaneAction action, final int position) {
		addAction(category, category + '/' + action.getKey(), action, position);
	}

	public void addAction(final String category, final String key, final AFreeplaneAction action,
	                      final int position) {
		assert action != null;
		assert key != null;
		if (getContainer(get(category), Container.class) instanceof JToolBar) {
			addButton(category, action, position);
			return;
		}
		final JMenuItem item;
		if (action.getClass().getAnnotation(SelectableAction.class) != null) {
			item = new JAutoCheckBoxMenuItem(decorateAction(category, action));
		}
		else if (action.getClass().getAnnotation(VisibleAction.class) != null) {
			item = new JAutoVisibleMenuItem(decorateAction(category, action));
		}
		else {
			item = new JMenuItem(decorateAction(category, action));
		}
		addMenuItem(category, item, key, position);
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
					action.setSelected();
				}
			});
		}
		if (AFreeplaneAction.checkVisibilityOnPopup(action)) {
			addPopupMenuListener(key, new PopupMenuListener() {
				public void popupMenuCanceled(final PopupMenuEvent e) {
				}

				public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
				}

				public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
					action.setVisible();
				}
			});
		}
		if (AFreeplaneAction.checkEnabledOnPopup(action)) {
			addPopupMenuListener(key, new PopupMenuListener() {
				public void popupMenuCanceled(final PopupMenuEvent e) {
				}

				public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
				}

				public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
					action.setEnabled();
				}
			});
		}
		return;
	}

	public void addAnnotatedAction(final AFreeplaneAction action) {
		addAction(action, action.getClass().getAnnotation(ActionLocationDescriptor.class));
	}

	private void addButton(final String category, final Action action, final int position) {
		final AbstractButton button;
		assert action != null;
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
	protected void addComponent(final Container container, final Component component, final int index) {
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

	public void addComponent(final String parent, final Container item, final Action action, final int position) {
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

	public void addMenuItem(final String relativeKey, final JMenuItem item, final String key, final int position) {
		String shortcutKey = getShortcutKey(key);
		String keyStrokeString = ResourceController.getResourceController().getAdjustableProperty(shortcutKey);
		DefaultMutableTreeNode element = addElement(relativeKey, item, key, position);
		if(null == getMenubar(element)){
			return;
		}
		if(keyStrokeString != null && !keyStrokeString.equals("")){
			KeyStroke keyStroke = UITools.getKeyStroke(keyStrokeString);
			item.setAccelerator(keyStroke);
		}
	}

	private Object getMenubar(DefaultMutableTreeNode element) {
		do {
	        Object userObject = element.getUserObject();
			if (userObject instanceof JMenuBar) {
		        return ((Node)element).getKey();
	        }
	        element = (DefaultMutableTreeNode) element.getParent();
        } while (element != null);
        return null;
    }

	private String getShortcutKey(final String key) {
	    return "acceleratorFor" + modeController.getModeName() + "/" + key;
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
		String key = action.getKey();
		assert key != null;
		final JRadioButtonMenuItem item;
		if (action.getClass().getAnnotation(SelectableAction.class) != null) {
			item = new JAutoRadioButtonMenuItem(decorateAction(category, action));
		}
		else {
			item = new JRadioButtonMenuItem(decorateAction(category, action));
		}
		addMenuItem(category, item, category + '/' + key, MenuBuilder.AS_CHILD);
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
