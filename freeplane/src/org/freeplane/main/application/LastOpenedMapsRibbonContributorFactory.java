/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2014 Dimitry
 *
 *  This file author is Dimitry
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
package org.freeplane.main.application;

import java.awt.BorderLayout;
import java.util.List;
import java.util.Properties;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ribbon.ARibbonContributor;
import org.freeplane.core.ui.ribbon.IRibbonContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonBuildContext;
import org.freeplane.core.util.TextUtils;
import org.pushingpixels.flamingo.api.common.AbstractCommandButton;
import org.pushingpixels.flamingo.api.common.CommandButtonDisplayState;
import org.pushingpixels.flamingo.api.common.CommandButtonLayoutManager;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonPopupOrientationKind;
import org.pushingpixels.flamingo.api.common.JCommandButtonPanel;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback;
import org.pushingpixels.flamingo.internal.ui.ribbon.appmenu.CommandButtonLayoutManagerMenuTileLevel2;

/**
 * @author Dimitry Polivaev
 * 02.01.2014
 */
public class LastOpenedMapsRibbonContributorFactory implements IRibbonContributorFactory {
	public static class RibbonMenuLastOpenedMapsPanel extends JCommandButtonPanel {

		private static final long serialVersionUID = 1L;
		protected final static CommandButtonDisplayState MENU_TILE_LEVEL_2 = new CommandButtonDisplayState(
				"Ribbon application menu tile level 2", 32) {
			@Override
			public CommandButtonLayoutManager createLayoutManager(
					AbstractCommandButton commandButton) {
				return new CommandButtonLayoutManagerMenuTileLevel2();
			}
		};
		
		public RibbonMenuLastOpenedMapsPanel() {
			super(MENU_TILE_LEVEL_2);
			this.setMaxButtonColumns(1);
		}
	}

	private PrimaryRolloverCallback rolloverCallback;
	private final LastOpenedList lastOpenedList;
	private String menuName = TextUtils.getText("most_recent_files");

	public LastOpenedMapsRibbonContributorFactory(LastOpenedList lastOpenedList) {
		this.lastOpenedList = lastOpenedList;
    }

	public ARibbonContributor getContributor(final Properties attributes) {
		return new ARibbonContributor() {
			
			public String getKey() {
				return "lastOpenedMaps";
			}

			@Override
			public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
				RibbonApplicationMenuEntryPrimary primeEntry = new RibbonApplicationMenuEntryPrimary(null, menuName, null, CommandButtonKind.POPUP_ONLY);
				primeEntry.setRolloverCallback(getCallback());
				parent.addChild(primeEntry, new ChildProperties(parseOrderSettings(attributes.getProperty("orderPriority", ""))));
			}

			@Override
			public void addChild(Object child, ChildProperties properties) {
			}
		};
	}
	
	private PrimaryRolloverCallback getCallback() {
		if(rolloverCallback == null) {
			rolloverCallback = new PrimaryRolloverCallback() {
				public void menuEntryActivated(JPanel targetPanel) {
					targetPanel.removeAll();
					targetPanel.setLayout(new BorderLayout());
					JCommandButtonPanel secondary = new RibbonMenuLastOpenedMapsPanel();
					
					String groupDesc = menuName;
					secondary.addButtonGroup(groupDesc);
					List<AFreeplaneAction> openActions = lastOpenedList.createOpenLastMapActionList();
					for (AFreeplaneAction action : openActions) {
						String text = (String) action.getValue(Action.NAME);
						JCommandButton openMapButton = new JCommandButton(text);
						openMapButton.addActionListener(action);						
						openMapButton.setCommandButtonKind(CommandButtonKind.ACTION_ONLY);
						openMapButton.setHorizontalAlignment(SwingUtilities.LEADING);
						openMapButton.setPopupOrientationKind(CommandButtonPopupOrientationKind.SIDEWARD);
						openMapButton.setEnabled(true);
						openMapButton.setActionKeyTip(text);
						
						secondary.addButtonToLastGroup(openMapButton);
					}
					targetPanel.add(secondary, BorderLayout.CENTER);
				}
			};
		}
        return rolloverCallback;
    }
}
