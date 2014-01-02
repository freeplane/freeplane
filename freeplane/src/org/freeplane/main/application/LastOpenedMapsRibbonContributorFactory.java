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

import java.util.List;
import java.util.Properties;

import javax.swing.Action;
import javax.swing.JPanel;

import org.freeplane.core.ui.AFreeplaneAction;
import org.freeplane.core.ui.ribbon.ARibbonContributor;
import org.freeplane.core.ui.ribbon.IRibbonContributorFactory;
import org.freeplane.core.ui.ribbon.RibbonBuildContext;
import org.pushingpixels.flamingo.api.common.JCommandButton;
import org.pushingpixels.flamingo.api.common.JCommandButton.CommandButtonKind;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary;
import org.pushingpixels.flamingo.api.ribbon.RibbonApplicationMenuEntryPrimary.PrimaryRolloverCallback;

/**
 * @author Dimitry Polivaev
 * 02.01.2014
 */
public class LastOpenedMapsRibbonContributorFactory implements IRibbonContributorFactory {
	private PrimaryRolloverCallback rolloverCallback;
	private final LastOpenedList lastOpenedList;

	public LastOpenedMapsRibbonContributorFactory(LastOpenedList lastOpenedList) {
		this.lastOpenedList = lastOpenedList;
    }

	public ARibbonContributor getContributor(final Properties attributes) {
		return new ARibbonContributor() {
			int counter = 0;
			@Override
			public String getKey() {
				return "lastOpenedMaps";
			}

			@Override
			public void contribute(RibbonBuildContext context, ARibbonContributor parent) {
				RibbonApplicationMenuEntryPrimary primeEntry = new RibbonApplicationMenuEntryPrimary(null, "LastOpendText", null, CommandButtonKind.POPUP_ONLY);
				primeEntry.setRolloverCallback(getCallback());
				parent.addChild(primeEntry, new ChildProperties(parseOrderSettings(attributes.getProperty("orderPriority", ""))));
			}

			@Override
			public void addChild(Object child, ChildProperties properties) {
				// TODO Auto-generated method stub
			}
		};
	}

	private PrimaryRolloverCallback getCallback() {
		if(rolloverCallback == null) {
			rolloverCallback = new PrimaryRolloverCallback() {
				public void menuEntryActivated(JPanel targetPanel) {
					List<AFreeplaneAction> openActions = lastOpenedList.createOpenLastMapActionList();
					for (AFreeplaneAction action : openActions) {
						JCommandButton openMapButton = new JCommandButton((String) action.getValue(Action.NAME));
						openMapButton.addActionListener(action);
						targetPanel.add(openMapButton);
			        }
					targetPanel.revalidate();
					targetPanel.repaint();
				}
			};
		}
        return rolloverCallback;
    }
}
