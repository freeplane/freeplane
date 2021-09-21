/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2020 Felix Natter
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
package org.freeplane.features.commandsearch;

import static org.freeplane.features.commandsearch.SearchItem.ITEM_PATH_SEPARATOR;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;

import org.freeplane.core.resources.components.IPropertyControlCreator;
import org.freeplane.core.resources.components.OptionPanelBuilder;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;

public class PreferencesIndexer
{
    private final List<String> path;

    private final List<PreferencesItem> prefs;

    public PreferencesIndexer()
    {
    	prefs = new LinkedList<>();
    	path = new ArrayList<>(2);
        load();
    }

    public List<PreferencesItem> getPrefs()
    {
        return prefs;
    }

    private void load() {
    	final Controller controller = Controller.getCurrentController();
		MModeController modeController = (MModeController) controller.getModeController(MModeController.MODENAME);
		OptionPanelBuilder optionPanelBuilder = modeController.getOptionPanelBuilder();
		final DefaultMutableTreeNode node = optionPanelBuilder.getRoot();
		load(node, 0);
    }

	public void load(final TreeNode parent, int level) {
		@SuppressWarnings("unchecked")
		Enumeration<DefaultMutableTreeNode> children = (Enumeration<DefaultMutableTreeNode>) parent.children();
		while(children.hasMoreElements()) {
			final DefaultMutableTreeNode child = children.nextElement();
			final IPropertyControlCreator userObject = (IPropertyControlCreator)child.getUserObject();
			if(userObject != null) {
				final String propertyName = userObject.getPropertyName();
				final String translatedText = HtmlUtils.htmlToPlain(userObject.getTranslatedText());
				if(! propertyName.isEmpty()){
				    String tooltipText = HtmlUtils.htmlToPlain(userObject.getTranslatedTooltipText());
				    String currentTabTranslated = path.get(0);
				    if(path.size() > 1) {
				    	String currentSeparatorTranslated = path.get(1);
//				    	System.out.println(currentTabTranslated + ITEM_PATH_SEPARATOR + currentSeparatorTranslated + ITEM_PATH_SEPARATOR + translatedText);
				    	if(parent.getChildCount() < 20) {
				    		String prefPath = currentSeparatorTranslated + ITEM_PATH_SEPARATOR + translatedText;
				    		prefs.add(new PreferencesItem(currentTabTranslated, propertyName, prefPath, tooltipText));
				    	}
				    	else {
				    		prefs.add(new PreferencesItem(currentTabTranslated, propertyName, translatedText, tooltipText));
				    	}
				    }
				    else {
				    	prefs.add(new PreferencesItem(currentTabTranslated, propertyName, translatedText, tooltipText));
				    }
				}
				else {
					path.add(translatedText);
				}
				if(level < 2) {
					load(child, level + 1);
				}
				if(propertyName.isEmpty()){
					path.remove(path.size() - 1);
				}
			}
			else {
				load(child, level);
			}
		}
	}
}
