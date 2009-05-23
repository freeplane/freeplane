/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
 *
 *  This file is created by Dimitry Polivaev in 2008.
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
package org.freeplane.features.mindmapmode.icon;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.IIconInformation;
import org.freeplane.core.model.MindIcon;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.ui.IPropertyControl;
import org.freeplane.core.resources.ui.IPropertyControlCreator;
import org.freeplane.core.resources.ui.KeyProperty;
import org.freeplane.core.resources.ui.OptionPanelBuilder;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.core.ui.components.JAutoScrollBarPane;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.common.icon.IconController;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.view.swing.ui.UserInputListenerFactory;

/**
 * @author Dimitry Polivaev
 */
public class MIconController extends IconController {
	static final private Map<String, AFreeplaneAction> iconActions = new LinkedHashMap<String, AFreeplaneAction>();
	final private JToolBar iconToolBar;
	final private JAutoScrollBarPane iconToolBarScrollPane;
	final List<String>  userIconNames;

	/**
	 * @param modeController
	 */
	public MIconController(final ModeController modeController) {
		super(modeController);
		iconToolBar = new FreeplaneToolBar();
		iconToolBarScrollPane = new JAutoScrollBarPane(iconToolBar);
		iconToolBar.setOrientation(SwingConstants.VERTICAL);
		userIconNames = new LinkedList<String>();
		createIconActions();
		createPreferences();
	}

	public void addIcon(final NodeModel node, final MindIcon icon, final int position) {
		final IActor actor = new IActor() {
			public void act() {
				node.addIcon(icon, position);
				getModeController().getMapController().nodeChanged(node, "icon", null, icon);
			}

			public String getDescription() {
				return "addIcon";
			}

			public void undo() {
				node.removeIcon(position);
				getModeController().getMapController().nodeChanged(node, "icon", icon, null);
			}
		};
		getModeController().execute(actor);
	}

	public void addIconsToMenu(final MenuBuilder builder, final String iconMenuString) {
		final String category = iconMenuString + "/icons/icons";
		builder.addAction(category, getModeController().getAction("RemoveIconAction"), MenuBuilder.AS_CHILD);
		builder.addAction(category, getModeController().getAction("RemoveAllIconsAction"), MenuBuilder.AS_CHILD);
		builder.addSeparator(category, MenuBuilder.AS_CHILD);
		final Set<Entry<String, List<String>>> iconGroups = MindIcon.getIconGroups().entrySet();
		for(Entry<String, List<String>> entry:iconGroups){
			JMenuItem item = new JMenu();
			String key = entry.getKey();
			final List<String> iconList = entry.getValue();
			if(iconList.isEmpty()){
				continue;
			}
			final ImageIcon icon = MindIcon.factory(iconList.get(0)).getIcon();
			item.setIcon(icon);
			item.setText(ResourceBundles.getText("IconGroupPopupAction." +key + ".text"));
			key = category + "/" + key;
			builder.addMenuItem(category, item, key, MenuBuilder.AS_CHILD);
			for(String iconName:iconList)
				builder.addAction(key, iconActions.get(iconName), MenuBuilder.AS_CHILD);
		}
	}

	private void createIconActions() {
		final ModeController modeController = getModeController();
		final Controller controller = modeController.getController();
		final RemoveIconAction removeLastIconAction = new RemoveIconAction(controller);
		modeController.addAction(removeLastIconAction);
		modeController.addAction(new RemoveAllIconsAction(controller));
		final List<String> iconNames = MindIcon.getAllIconNames();
		final File iconDir = new File(ResourceController.getResourceController().getFreeplaneUserDirectory(), "icons");
		if (iconDir.exists()) {
			final String[] userIconArray = iconDir.list(new FilenameFilter() {
				public boolean accept(final File dir, final String name) {
					return name.matches(".*\\.png");
				}
			});
			if (userIconArray != null) {
				for (int i = 0; i < userIconArray.length; ++i) {
					String iconName = userIconArray[i];
					iconName = iconName.substring(0, iconName.length() - 4);
					if (iconName.equals("")) {
						continue;
					}
					iconNames.add(iconName);
					userIconNames.add(iconName);
				}
			}
		}
		for (String iconName:iconNames) {
			final MindIcon myIcon = MindIcon.factory(iconName);
			final IconAction myAction = new IconAction(controller, myIcon);
			iconActions.put(iconName, myAction);
		}
	}

	private void createPreferences() {
		final Vector actions = new Vector();
		actions.addAll(iconActions.values());
		final MModeController modeController = (MModeController) getModeController();
		final OptionPanelBuilder optionPanelBuilder = modeController.getOptionPanelBuilder();
		actions.add(modeController.getAction("RemoveIconAction"));
		actions.add(modeController.getAction("RemoveAllIconsAction"));
		final Iterator iterator = actions.iterator();
		while (iterator.hasNext()) {
			final IIconInformation info = (IIconInformation) iterator.next();
			optionPanelBuilder.addCreator("Keystrokes/icons", new IPropertyControlCreator() {
				public IPropertyControl createControl() {
					final KeyProperty keyProperty = new KeyProperty(info.getShortcutKey());
					keyProperty.setLabelText(info.getDescription());
					keyProperty.setImageIcon(info.getIcon());
					keyProperty.disableModifiers();
					return keyProperty;
				}
			}, IndexedTree.AS_CHILD);
		}
	}

	public Collection<AFreeplaneAction> getIconActions() {
		return Collections.unmodifiableCollection(iconActions.values());
	}

	/**
	 * @return
	 */
	public Component getIconToolBarScrollPane() {
		return iconToolBarScrollPane;
	}

	public Collection getMindIcons() {
		final Vector iconInformationVector = new Vector();
		final Collection<AFreeplaneAction> iconActions = getIconActions();
		for (final Iterator<AFreeplaneAction> i = iconActions.iterator(); i.hasNext();) {
			final Action action = i.next();
			final MindIcon info = ((IconAction) action).getMindIcon();
			iconInformationVector.add(info);
		}
		return iconInformationVector;
	}

	public void removeAllIcons(final NodeModel node) {
		((RemoveAllIconsAction) getModeController().getAction("RemoveAllIconsAction")).removeAllIcons(node);
	}

	public int removeIcon(final NodeModel node, final int position) {
		return ((RemoveIconAction) getModeController().getAction("RemoveIconAction")).removeIcon(node, position);
	}

	public void updateIconToolbar() {
		iconToolBar.removeAll();
		iconToolBar.add(getModeController().getAction("RemoveIconAction")).setAlignmentX(JComponent.CENTER_ALIGNMENT);
		iconToolBar.add(getModeController().getAction("RemoveAllIconsAction")).setAlignmentX(JComponent.CENTER_ALIGNMENT);
		iconToolBar.addSeparator();

		if(ResourceController.getResourceController().getBooleanProperty("structured_icon_toolbar")){
			insertSubmenus(iconToolBar);
			return;
		}
		for (String name:MindIcon.getAllIconNames()) {
			final AFreeplaneAction iconAction = iconActions.get(name);
			if(iconAction != null){
				iconToolBar.add(iconAction).setAlignmentX(JComponent.CENTER_ALIGNMENT);
			}
		}
		for (String name:userIconNames) {
			final AFreeplaneAction iconAction = iconActions.get(name);
			if(iconAction != null){
				iconToolBar.add(iconAction).setAlignmentX(JComponent.CENTER_ALIGNMENT);
			}
		}
	}


	private void insertSubmenus(final JToolBar iconToolBar) {
	    JMenuBar iconMenuBar = new JMenuBar(){
            private static final long serialVersionUID = 1L;

			@Override
            public Dimension getMaximumSize() {
	            final Dimension preferredSize = getPreferredSize();
				return new Dimension(Short.MAX_VALUE, preferredSize.height);
            }
			
		};
		iconMenuBar.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		iconMenuBar.setLayout(new GridLayout(0, 1));
		final Controller controller = getModeController().getController();
		for (Entry<String, List<String>> entry:MindIcon.getIconGroups().entrySet()) {
			final List<String> groupIconList = entry.getValue();
			if(groupIconList.isEmpty()){
				continue;
			}
			final String iconName = groupIconList.get(0);
			iconMenuBar.add(getSubmenu(controller, entry.getKey(), groupIconList, MindIcon.factory(iconName)));
		}
		if(! userIconNames.isEmpty()){
			iconMenuBar.add(getSubmenu(controller, "user", userIconNames, MindIcon.factory(userIconNames.get(0))));
		}
		iconToolBar.add(iconMenuBar);
    }

	private  JMenu getSubmenu(Controller controller, String group, final List<String> iconList, MindIcon menuIcon) {
		JMenu menu = new JMenu("\u25ba");
		menu.setFont(menu.getFont().deriveFont(8F));
		menu.setMargin(new Insets(0, 0, 0, 0));
		menu.setIcon(menuIcon.getIcon());
		for(String icon:iconList){
			final AFreeplaneAction myAction = iconActions.get(icon);
			menu.add(myAction);
		}
		menu.setToolTipText(ResourceBundles.getText("IconGroupPopupAction." + group + ".text"));
		return menu;
    }
	
	public void updateMenus(final MenuBuilder builder) {
		addIconsToMenu(builder, FreeplaneMenuBar.INSERT_MENU);
		addIconsToMenu(builder, UserInputListenerFactory.NODE_POPUP);
	}
}
