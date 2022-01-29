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
package org.freeplane.features.icon.mindmapmode;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.plaf.basic.BasicIconFactory;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.resources.components.OptionPanelBuilder;
import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.MenuSplitter;
import org.freeplane.core.ui.components.FreeplaneToolBar;
import org.freeplane.core.ui.components.JAutoScrollBarPane;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.ui.components.resizer.CollapseableBoxBuilder;
import org.freeplane.core.ui.components.resizer.JResizer.Direction;
import org.freeplane.core.ui.menubuilders.generic.Entry;
import org.freeplane.core.ui.menubuilders.generic.EntryAccessor;
import org.freeplane.core.ui.menubuilders.generic.EntryVisitor;
import org.freeplane.core.ui.menubuilders.generic.PhaseProcessor.Phase;
import org.freeplane.core.undo.IActor;
import org.freeplane.features.filter.condition.ICondition;
import org.freeplane.features.icon.EmojiIcon;
import org.freeplane.features.icon.IconContainedCondition;
import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.IconExistsCondition;
import org.freeplane.features.icon.IconGroup;
import org.freeplane.features.icon.IconStore;
import org.freeplane.features.icon.MindIcon;
import org.freeplane.features.icon.NamedIcon;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.IExtensionCopier;
import org.freeplane.features.map.INodeChangeListener;
import org.freeplane.features.map.NodeChangeEvent;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.styles.ConditionPredicate;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.LogicalStyleKeys;

/**
 * @author Dimitry Polivaev
 */
public class MIconController extends IconController {
    private static final String RECENTLY_USED_ICONS_PROPERTY = "recently_used_icons";
    private static final String ADD_EMOJIS_TO_MENU = "add_emojis_to_menu";
    private static final String ADD_EMOJIS_TO_ICON_TOOLBAR = "add_emojis_to_icon_toolbar";
    private static final Insets ICON_SUBMENU_INSETS = new Insets(3, 0, 3, 0);
	private static final ConditionPredicate DEPENDS_ON_ICON = new ConditionPredicate() {

		@Override
		public boolean test(ICondition condition) {
			return condition instanceof IconContainedCondition
					|| condition instanceof IconExistsCondition;
		}
	};

	private final class IconMenuBuilder implements EntryVisitor {
		final private ModeController modeController;

		public IconMenuBuilder(ModeController modeController) {
			this.modeController = modeController;
		}

		@Override
		public void visit(Entry target) {
			addIcons(target);
			updateIconToolbar(modeController);
		}

		private void addIcons(final Entry target) {
			for (final IconGroup iconGroup : STORE.getGroups()) {
				addIconGroup(target, iconGroup, false);
			}
		}

		private void addIconGroup(final Entry target, final IconGroup group, boolean isEmoji) {
		    if (group.getIcons().size() < 1)
		        return;
			isEmoji = isEmoji || group.getName().equals(IconStore.EMOJI_GROUP);
			if (isEmoji && ! areEmojisAvailbleFromMenu())
				return;
		    final Entry item = new Entry();
		    item.setName("icons");
		    EntryAccessor entryAccessor = new EntryAccessor();
		    entryAccessor.drawMenuIconAlways(item);
		    entryAccessor.setIcon(item, group.getGroupIcon().getIcon());
		    entryAccessor.setText(item, group.getDescription());
		    if(isEmoji)
		    	entryAccessor.processUiOnPopup(item);
		    target.addChild(item);
		    List<IconGroup> childGroups = group.getGroups();
            for (final IconGroup childGroup : childGroups) {
		        if(childGroup.isLeaf()) {
		            MindIcon icon = childGroup.getGroupIcon();
		            Entry actionItem = entryAccessor.addChildAction(item, iconActions.get(icon.getName()));
		            entryAccessor.drawMenuIconAlways(actionItem);
		        }
		        else
		            addIconGroup(item, childGroup, isEmoji);
		    }
            if(childGroups.isEmpty()) {
                Entry noActions = new Entry();
                noActions.setBuilders("noActions");
                item.addChild(noActions );
            }
		}

		@Override
		public boolean shouldSkipChildren(Entry entry) {
			return false;
		}
	}

	public static enum Keys {
		ICONS
	}

	private static class ExtensionCopier implements IExtensionCopier {
		@Override
		public void copy(final Object key, final NodeModel from, final NodeModel to) {
            if (key.equals(Keys.ICONS)) {
                copyIcons(from, to);
            }
            if (key.equals(LogicalStyleKeys.NODE_STYLE)) {
                copyIconSize(from, to);
            }
		}

		private void copyIconSize(NodeModel from, NodeModel to) {
		    Quantity<LengthUnit> iconSize = from.getSharedData().getIcons().getIconSize();
		    if(iconSize != null)
		    	to.getSharedData().getIcons().setIconSize(iconSize);
            
        }

        private void copyIcons(final NodeModel from, final NodeModel to) {
			final List<NamedIcon> sourceIcons = from.getIcons();
			final List<NamedIcon> targetIcons = to.getIcons();
			for (final NamedIcon icon : sourceIcons) {
				if (targetIcons.contains(icon)) {
					continue;
				}
				to.addIcon(icon);
			}
		}

		@Override
		public void remove(final Object key, final NodeModel from) {
			if (key.equals(Keys.ICONS)) {
			    while (from.removeIcon() > 0) {/**/}
			}
            if (key.equals(LogicalStyleKeys.NODE_STYLE)) {
                removeIconSize(from);
            }

		}

		private void removeIconSize(NodeModel from) {
		    from.getSharedData().getIcons().setIconSize(null);
        }

        @Override
		public void remove(final Object key, final NodeModel from, final NodeModel which) {
			if (key.equals(Keys.ICONS)) {
			    removeIcons(from, which);
			}
            if (key.equals(LogicalStyleKeys.NODE_STYLE)
                     &&  which.getSharedData().getIcons().getIconSize() != null) {
                removeIconSize(from);
            }
		}

        private void removeIcons(final NodeModel from, final NodeModel which) {
            final List<NamedIcon> targetIcons = from.getIcons();
			final List<NamedIcon> whichIcons = which.getIcons();
			final Iterator<NamedIcon> targetIconIterator = targetIcons.iterator();
			while (targetIconIterator.hasNext()) {
				NamedIcon icon = targetIconIterator.next();
				if (!whichIcons.contains(icon)) {
					continue;
				}
				targetIconIterator.remove();
			}
        }
	}

	private final Map<String, AFreeplaneAction> iconActions = new LinkedHashMap<>();
	private final IconStore STORE = IconStoreFactory.ICON_STORE;
	private final JToolBar iconToolBar;
	private final Box iconBox;
	private final FastAccessableIcons recentlyUsedIcons;

	/**
	 * @param modeController
	 */
	public MIconController(final ModeController modeController) {
		super(modeController);
		modeController.registerExtensionCopier(new ExtensionCopier());
		iconToolBar = new FreeplaneToolBar("icon_toolbar", SwingConstants.VERTICAL);
		JAutoScrollBarPane iconToolBarScrollPane = new JAutoScrollBarPane(iconToolBar);
		UITools.setScrollbarIncrement(iconToolBarScrollPane);
		UITools.addScrollbarIncrementPropertyListener(iconToolBarScrollPane);
		iconBox = new CollapseableBoxBuilder("leftToolbarVisible").createBox(iconToolBarScrollPane, Direction.LEFT);
		createIconActions(modeController);
		createPreferences();
		modeController.addUiBuilder(Phase.ACTIONS, "icon_actions", new IconMenuBuilder(modeController));
		recentlyUsedIcons = new FastAccessableIcons(modeController);
	}

	@Override
	public void install(final ModeController modeController) {
		super.install(modeController);
		modeController.getMapController().addUINodeChangeListener(new INodeChangeListener() {

			@Override
			public void nodeChanged(NodeChangeEvent event) {
				final NodeModel node = event.getNode();
				if(event.getProperty().equals(NodeModel.NODE_ICON)
						&& LogicalStyleController.getController().conditionalStylesOf(node).dependsOnConditionRecursively(DEPENDS_ON_ICON)){
					modeController.getMapController().delayedNodeRefresh(node, NodeModel.UNKNOWN_PROPERTY, null, null);
				}
			}
		});
	}


    public void addIconByUserAction(final NodeModel node, final IconAction action) {
        addIcon(node, action.getMindIcon());
        recentlyUsedIcons.add(action);
    }

    public void addIcon(final NodeModel node, final NamedIcon icon) {
		final IActor actor = new IActor() {
			@Override
			public void act() {
				node.addIcon(icon);
				Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeModel.NODE_ICON, null, icon);
			}

			@Override
			public String getDescription() {
				return "addIcon";
			}

			@Override
			public void undo() {
				node.removeIcon();
				Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeModel.NODE_ICON, icon, null);
			}
		};
		Controller.getCurrentModeController().execute(actor, node.getMap());
	}

	public void addIcon(final NodeModel node, final MindIcon icon, final int position) {
		final IActor actor = new IActor() {
			@Override
			public void act() {
				node.addIcon(icon, position);
				Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeModel.NODE_ICON, null, icon);
			}

			@Override
			public String getDescription() {
				return "addIcon";
			}

			@Override
			public void undo() {
				node.removeIcon(position);
				Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeModel.NODE_ICON, icon, null);
			}
		};
		Controller.getCurrentModeController().execute(actor, node.getMap());
	}

	public void changeIconSize(final NodeModel node, final Quantity<LengthUnit> iconSize)
	{
		final IActor actor = new IActor() {

			private Quantity<LengthUnit> oldIconSize;

			@Override
			public void act() {
				oldIconSize = node.getSharedData().getIcons().getIconSize();
				node.getSharedData().getIcons().setIconSize(iconSize);
				Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeModel.NODE_ICON_SIZE, null, iconSize);
			}

			@Override
			public String getDescription() {
				return "changeIconSize";
			}

			@Override
			public void undo() {
				node.getSharedData().getIcons().setIconSize(oldIconSize);
				Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeModel.NODE_ICON_SIZE, oldIconSize, null);
			}
		};
		Controller.getCurrentModeController().execute(actor, node.getMap());
	}

	private void createIconActions(final ModeController modeController) {
		modeController.addAction(new RemoveIconAction(0));
		modeController.addAction(new RemoveIconAction(-1));
		modeController.addAction(new RemoveAllIconsAction());
		for (final MindIcon icon : STORE.getMindIcons()) {
			final IconAction myAction = new IconAction(icon);
			modeController.addActionIfNotAlreadySet(myAction);
			iconActions.put(icon.getName(), myAction);
		}
	}

	private void createPreferences() {
		final MModeController modeController = (MModeController) Controller.getCurrentModeController();
		final OptionPanelBuilder optionPanelBuilder = modeController.getOptionPanelBuilder();
		final List<AFreeplaneAction> actions = new ArrayList<AFreeplaneAction>();
		actions.addAll(areEmojisAvailbleFromMenu() ? getIconActions() : getIconActions(icon -> ! (icon instanceof EmojiIcon)));
		actions.add(modeController.getAction("RemoveIcon_0_Action"));
		actions.add(modeController.getAction("RemoveIconAction"));
		actions.add(modeController.getAction("RemoveAllIconsAction"));
	}

	public Collection<AFreeplaneAction> getIconActions() {
		return Collections.unmodifiableCollection(iconActions.values());
	}

	public Collection<AFreeplaneAction> getIconActions(Predicate<MindIcon> filter) {
		return iconActions.values().stream()
				.filter(action -> filter.test(((IconAction) action).getMindIcon()))
				.collect(Collectors.toList());
	}

    public Map<String, AFreeplaneAction> getAllIconActions() {
        return Collections.unmodifiableMap(iconActions);
    }

	/**
	 * @return
	 */
	public JComponent getIconToolBarScrollPane() {
		return iconBox;
	}

	final static private Icon SUBMENU_ICON = BasicIconFactory.getMenuArrowIcon();
	private JMenu getSubmenu( final IconGroup group) {
		final JMenu menu = createToolbarSubmenu(group);
		fillSubmenuOnSelect(menu, group);
		return menu;
	}

    private void fillSubmenuOnSelect(final JMenu menu, final IconGroup group) {
    	menu.addMenuListener(new MenuListener() {
			@Override
			public void menuSelected(MenuEvent e) {
				menu.removeMenuListener(this);
		        fillSubmenu(menu, group);
			}

			private void fillSubmenu(final JMenu menu, final IconGroup group) {
				for (final IconGroup childGroup : group.getGroups()) {
				    if(childGroup.isLeaf()) {
				        MindIcon groupIcon = childGroup.getGroupIcon();
				        addActionToIconSubmenu(menu, groupIcon);
				    }
				    else {
				        final JMenu submenu = new JMenu(childGroup.getDescription());
				        submenu.setIcon(childGroup.getGroupIcon().getIcon());
				        fillSubmenuOnSelect(submenu, childGroup);
				        addGroupToIconSubmenu(menu, submenu);
				    }
				}
			}

			@Override
			public void menuDeselected(MenuEvent e) {
			}

			@Override
			public void menuCanceled(MenuEvent e) {
			}
		});
    }

    private JMenu createToolbarSubmenu(final IconGroup group) {
        final JMenu menu = new JMenu() {
			private static final long serialVersionUID = 1L;

			@Override
			protected Point getPopupMenuOrigin() {
				return new Point(getWidth(), 0);
			}

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				final int x = getWidth() - SUBMENU_ICON.getIconWidth();
				final int y = (getHeight() - SUBMENU_ICON.getIconHeight()) / 2;
				SUBMENU_ICON.paintIcon(this, g, x, y);
			}

			@Override
			public Dimension getPreferredSize() {
				final Dimension result = super.getPreferredSize();
				result.width += SUBMENU_ICON.getIconWidth();
				return result;
			}


		};
		menu.setMargin(ICON_SUBMENU_INSETS);
		menu.setIcon(group.getGroupIcon().getIcon());
		menu.setToolTipText(group.getDescription());
        return menu;
    }

	private void addGroupToIconSubmenu(JMenu menu, JMenu submenu) {
	    new MenuSplitter().addMenuComponent(menu, submenu,  menu.getItemCount());
    }

    private void addActionToIconSubmenu(final JMenu menu, final MindIcon icon) {
		final AFreeplaneAction myAction = iconActions.get(icon.getName());
			new MenuSplitter().addMenuComponent(menu, new JMenuItem(myAction),  menu.getItemCount());
	}

	private void insertToolbarSubmenus(final JToolBar iconToolBar, boolean isStructured) {
		final JMenuBar iconMenuBar = new JMenuBar() {
			private static final long serialVersionUID = 1L;

			@Override
			public Dimension getMaximumSize() {
				final Dimension preferredSize = getPreferredSize();
				return new Dimension(Short.MAX_VALUE, preferredSize.height);
			}
		};
		iconMenuBar.setAlignmentX(JComponent.CENTER_ALIGNMENT);
		iconMenuBar.setLayout(new GridLayout(0, 1));
		for (final IconGroup iconGroup : STORE.getGroups()) {
		    if(isStructured && (! iconGroup.getName().equals(IconStore.EMOJI_GROUP) || areEmojisAvailbleOnIconToolbar())
		            || iconGroup.getName().equals(IconStore.EMOJI_GROUP) && areEmojisAvailbleOnIconToolbar())
			iconMenuBar.add(getSubmenu(iconGroup));
		}
		iconToolBar.add(iconMenuBar);
	}

	boolean areEmojisAvailbleFromMenu() {
	    return ResourceController.getResourceController().getBooleanProperty(ADD_EMOJIS_TO_MENU);
    }

	boolean areEmojisAvailbleOnIconToolbar() {
	    return ResourceController.getResourceController().getBooleanProperty(ADD_EMOJIS_TO_ICON_TOOLBAR);
    }

    public void removeAllIcons(final NodeModel node) {
		final int size = node.getIcons().size();
		final MIconController iconController = (MIconController) IconController.getController();
		for (int i = 0; i < size; i++) {
			iconController.removeIcon(node, 0);
		}
	}

	public int removeIcon(final NodeModel node) {
		return removeIcon(node, -1);
	}

	public int removeIcon(final NodeModel node, final int position) {
		final int size = node.getIcons().size();
		final int index = position >= 0 ? position : size + position;
		if (size == 0 || size <= index) {
			return size;
		}
		final IActor actor = new IActor() {
			private final NamedIcon icon = node.getIcon(index);

			@Override
			public void act() {
				node.removeIcon(index);
				Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeModel.NODE_ICON, icon, null);
			}

			@Override
			public String getDescription() {
				return "removeIcon";
			}

			@Override
			public void undo() {
				node.addIcon(icon, index);
				Controller.getCurrentModeController().getMapController().nodeChanged(node, NodeModel.NODE_ICON, null, icon);
			}
		};
		Controller.getCurrentModeController().execute(actor, node.getMap());
		return node.getIcons().size();
	}

	private void updateIconToolbar(ModeController modeController) {
		iconToolBar.removeAll();
		iconToolBar.add(modeController.getAction("RemoveIcon_0_Action"))
		    .setAlignmentX(JComponent.CENTER_ALIGNMENT);
		iconToolBar.add(modeController.getAction("RemoveIconAction")).setAlignmentX(JComponent.CENTER_ALIGNMENT);
		iconToolBar.add(modeController.getAction("RemoveAllIconsAction")).setAlignmentX(
		    JComponent.CENTER_ALIGNMENT);
        iconToolBar.addSeparator();
        recentlyUsedIcons.load(ResourceController.getResourceController().getProperty(RECENTLY_USED_ICONS_PROPERTY, ""));
        recentlyUsedIcons.addPanelTo(iconToolBar);
        boolean isStructured = ResourceController.getResourceController().getBooleanProperty("structured_icon_toolbar");
		if (! isStructured && areEmojisAvailbleOnIconToolbar())
		    iconToolBar.addSeparator();
		insertToolbarSubmenus(iconToolBar, isStructured);
		if (! isStructured) {
		    iconToolBar.addSeparator();
		    for (final MindIcon mindIcon : STORE.getMindIcons()) {
		        if(!(mindIcon instanceof EmojiIcon)) {
		            final AFreeplaneAction iconAction = iconActions.get(mindIcon.getName());
		            iconToolBar.add(iconAction).setAlignmentX(JComponent.CENTER_ALIGNMENT);
		        }
		    }
		}
	}

    public void saveRecentlyUsedActions() {
        String initializer = recentlyUsedIcons.getInitializer();
        ResourceController.getResourceController().setProperty(RECENTLY_USED_ICONS_PROPERTY, initializer);
    }

}
