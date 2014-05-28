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
import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

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
import org.pushingpixels.flamingo.api.common.JScrollablePanel;
import org.pushingpixels.flamingo.api.common.JScrollablePanel.ScrollType;
import org.pushingpixels.flamingo.api.common.RichTooltip;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback;
import org.pushingpixels.flamingo.internal.ui.ribbon.appmenu.CommandButtonLayoutManagerMenuTileLevel2;
import org.pushingpixels.flamingo.internal.ui.ribbon.appmenu.JRibbonApplicationMenuPopupPanelSecondary;

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
			public CommandButtonLayoutManager createLayoutManager(AbstractCommandButton commandButton) {
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

	public LastOpenedMapsRibbonContributorFactory(LastOpenedList lastOpenedList) {
		this.lastOpenedList = lastOpenedList;
	}

	public ARibbonContributor getContributor(final Properties attributes) {
		return new ARibbonContributor() {
			@Override
			public String getKey() {
				return "lastOpenedMaps";
			}

			final private String menuName = TextUtils.getText(attributes.getProperty("name_ref"));

			@Override
			public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
				RibbonApplicationMenuEntryPrimary primeEntry = new RibbonApplicationMenuEntryPrimary(null, menuName,
				    null, CommandButtonKind.POPUP_ONLY);
				primeEntry.setRolloverCallback(getCallback(primeEntry));
				parent.addChild(primeEntry,
				    new ChildProperties(parseOrderSettings(attributes.getProperty("orderPriority", ""))));
			}

			@Override
			public void addChild(Object child, ChildProperties properties) {
			}

			private PrimaryRolloverCallback getCallback(final RibbonApplicationMenuEntryPrimary primeEntry) {
				if (rolloverCallback == null) {
					rolloverCallback = new PrimaryRolloverCallback() {
						public void menuEntryActivated(JPanel targetPanel) {
							targetPanel.removeAll();
							targetPanel.setLayout(new BorderLayout());
							JCommandButtonPanel secondary = new JRibbonApplicationMenuPopupPanelSecondary(primeEntry);
							secondary.setToShowGroupLabels(false);
							String groupDesc = menuName;
							secondary.addButtonGroup(groupDesc);
							List<AFreeplaneAction> openActions = lastOpenedList.createOpenLastMapActionList();
							for (AFreeplaneAction action : openActions) {
								String restoreable = (String) action.getValue(Action.DEFAULT);
								StringTokenizer tokens = new StringTokenizer(restoreable, ";");
								File file = lastOpenedList.createFileFromRestorable(tokens);
								JCommandButton menuButton = new JCommandButton(file.getName());
								menuButton.addActionListener(action);
								menuButton.setCommandButtonKind(CommandButtonKind.ACTION_ONLY);
								menuButton.setHorizontalAlignment(SwingUtilities.LEADING);
								menuButton.setPopupOrientationKind(CommandButtonPopupOrientationKind.SIDEWARD);
								menuButton.setEnabled(true);
								menuButton.setActionRichTooltip(new RichTooltip((String) action
								    .getValue(Action.SHORT_DESCRIPTION), file.toString()));
								secondary.addButtonToLastGroup(menuButton);
							}
							JScrollablePanel<JCommandButtonPanel> scrollPanel = new JScrollablePanel<JCommandButtonPanel>(
							    secondary, ScrollType.VERTICALLY);
							targetPanel.add(scrollPanel, BorderLayout.CENTER);
						}
					};
				}
				return rolloverCallback;
			}
		};
	}
}
