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
import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.IIconInformation;
import org.freeplane.core.model.MindIcon;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.ui.IPropertyControl;
import org.freeplane.core.resources.ui.IPropertyControlCreator;
import org.freeplane.core.resources.ui.KeyProperty;
import org.freeplane.core.resources.ui.OptionPanelBuilder;
import org.freeplane.core.ui.IndexedTree;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.ui.components.FreeplaneMenuBar;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.core.ui.components.JAutoScrollBarPane;
import org.freeplane.core.undo.IUndoableActor;
import org.freeplane.features.common.icon.IconController;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.view.swing.ui.UserInputListenerFactory;

/**
 * @author Dimitry Polivaev
 */
public class MIconController extends IconController {
	static final private List<Action> iconActions = new Vector<Action>();
	final private JToolBar iconToolBar;
	final private JAutoScrollBarPane iconToolBarScrollPane;

	/**
	 * @param modeController
	 */
	public MIconController(final ModeController modeController) {
		super(modeController);
		iconToolBar = new FreeplaneToolBar();
		iconToolBarScrollPane = new JAutoScrollBarPane(iconToolBar);
		iconToolBar.setOrientation(SwingConstants.VERTICAL);
		iconToolBar.setRollover(true);
		createIconActions();
		createPreferences();
	}

	public void addIcon(final NodeModel node, final MindIcon icon, final int position) {
		final IUndoableActor actor = new IUndoableActor() {
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
		builder.addMenuItem(iconMenuString + "/icons", new JMenuItem(getModeController().getAction(
		    "removeLastIconAction")), MenuBuilder.AS_CHILD);
		builder.addMenuItem(iconMenuString + "/icons", new JMenuItem(getModeController().getAction(
		    "removeAllIconsAction")), MenuBuilder.AS_CHILD);
		builder.addSeparator(iconMenuString + "/icons", MenuBuilder.AS_CHILD);
		for (int i = 0; i < iconActions.size(); ++i) {
			builder.addMenuItem(iconMenuString + "/icons", new JMenuItem((Action) iconActions.get(i)),
			    MenuBuilder.AS_CHILD);
		}
	}

	private void createIconActions() {
		final ModeController modeController = getModeController();
		final Controller controller = modeController.getController();
		final RemoveIconAction removeLastIconAction = new RemoveIconAction(controller);
		modeController.putAction("removeLastIconAction", removeLastIconAction);
		modeController.putAction("removeAllIconsAction", new RemoveAllIconsAction(controller));
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
				}
			}
		}
		for (int i = 0; i < iconNames.size(); ++i) {
			final String iconName = ((String) iconNames.get(i));
			final MindIcon myIcon = MindIcon.factory(iconName);
			final IconAction myAction = new IconAction(modeController, myIcon);
			iconActions.add(myAction);
		}
	}

	private void createPreferences() {
		final Vector actions = new Vector();
		actions.addAll(iconActions);
		final MModeController modeController = (MModeController) getModeController();
		final OptionPanelBuilder optionPanelBuilder = modeController.getOptionPanelBuilder();
		actions.add(modeController.getAction("removeLastIconAction"));
		actions.add(modeController.getAction("removeAllIconsAction"));
		final Iterator iterator = actions.iterator();
		while (iterator.hasNext()) {
			final IIconInformation info = (IIconInformation) iterator.next();
			optionPanelBuilder.addCreator("Keystrokes/icons", new IPropertyControlCreator() {
				public IPropertyControl createControl() {
					final KeyProperty keyProperty = new KeyProperty(info.getKeystrokeResourceName());
					keyProperty.setLabelText(info.getDescription());
					keyProperty.setImageIcon(info.getIcon());
					keyProperty.disableModifiers();
					return keyProperty;
				}
			}, IndexedTree.AS_CHILD);
		}
	}

	public Collection<Action> getIconActions() {
		return Collections.unmodifiableCollection(iconActions);
	}

	/**
	 * @return
	 */
	public Component getIconToolBarScrollPane() {
		return iconToolBarScrollPane;
	}

	public Collection getMindIcons() {
		final Vector iconInformationVector = new Vector();
		final Collection<Action> iconActions = getIconActions();
		for (final Iterator<Action> i = iconActions.iterator(); i.hasNext();) {
			final Action action = i.next();
			final MindIcon info = ((IconAction) action).getMindIcon();
			iconInformationVector.add(info);
		}
		return iconInformationVector;
	}

	public void removeAllIcons(final NodeModel node) {
		((RemoveAllIconsAction) getModeController().getAction("removeAllIconsAction")).removeAllIcons(node);
	}

	public int removeIcon(final NodeModel node, final int position) {
		return ((RemoveIconAction) getModeController().getAction("removeLastIconAction")).removeIcon(node, position);
	}

	public void updateIconToolbar() {
		updateIconToolbar(iconToolBar);
	}

	private void updateIconToolbar(final JToolBar iconToolBar) {
		iconToolBar.removeAll();
		iconToolBar.add(getModeController().getAction("removeLastIconAction"));
		iconToolBar.add(getModeController().getAction("removeAllIconsAction"));
		iconToolBar.addSeparator();
		for (int i = 0; i < iconActions.size(); ++i) {
			iconToolBar.add((Action) iconActions.get(i));
		}
	}

	public void updateMenus(final MenuBuilder builder) {
		addIconsToMenu(builder, FreeplaneMenuBar.INSERT_MENU);
		addIconsToMenu(builder, UserInputListenerFactory.NODE_POPUP);
	}
}
