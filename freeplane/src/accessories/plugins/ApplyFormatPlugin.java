/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
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
package accessories.plugins;

import java.util.Iterator;
import java.util.List;

import org.freeplane.controller.Controller;
import org.freeplane.map.pattern.mindmapnode.StylePatternFactory;
import org.freeplane.map.tree.NodeModel;

import accessories.plugins.dialogs.ChooseFormatPopupDialog;
import deprecated.freemind.modes.mindmapmode.hooks.MindMapNodeHookAdapter;
import freemind.controller.actions.generated.instance.Pattern;

/**
 * @author adapted to the plugin mechanism by ganzer
 */
public class ApplyFormatPlugin extends MindMapNodeHookAdapter {
	/**
	 */
	public ApplyFormatPlugin() {
		super();
	}

	@Override
	public void invoke(final NodeModel rootNode) {
		final NodeModel focussed = getController().getSelectedNode();
		final List selected = getController().getSelectedNodes();
		final Pattern nodePattern = StylePatternFactory
		    .createPatternFromSelected(focussed, selected);
		final ChooseFormatPopupDialog formatDialog = new ChooseFormatPopupDialog(
		    Controller.getController().getViewController().getJFrame(),
		    getMindMapController(),
		    "accessories/plugins/ApplyFormatPlugin.dialog.title", nodePattern);
		formatDialog.setModal(true);
		formatDialog.setVisible(true);
		if (formatDialog.getResult() == ChooseFormatPopupDialog.OK) {
			final Pattern pattern = formatDialog.getPattern();
			for (final Iterator iter = selected.iterator(); iter.hasNext();) {
				final NodeModel node = (NodeModel) iter.next();
				getMindMapController().getPatternController().applyPattern(
				    node, pattern);
			}
		}
	}
}
