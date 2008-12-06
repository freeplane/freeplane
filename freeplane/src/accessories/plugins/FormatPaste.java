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

import javax.swing.JOptionPane;

import org.freeplane.controller.Freeplane;
import org.freeplane.map.pattern.mindmapnode.StylePatternFactory;
import org.freeplane.map.tree.NodeModel;

import deprecated.freemind.modes.mindmapmode.hooks.MindMapNodeHookAdapter;
import freemind.controller.actions.generated.instance.Pattern;

/**
 * @author foltin
 */
public class FormatPaste extends MindMapNodeHookAdapter {
	private static Pattern pattern = null;

	public FormatPaste() {
		super();
	}

	/**
	 */
	private void copyFormat(final NodeModel node) {
		FormatPaste.pattern = StylePatternFactory.createPatternFromNode(node);
	}

	@Override
	public void invoke(final NodeModel node) {
		super.invoke(node);
		final String actionType = getResourceString("actionType");
		if (actionType.equals("copy_format")) {
			copyFormat(node);
		}
		else {
			pasteFormat(node);
		}
	}

	/**
	 */
	private void pasteFormat(final NodeModel node) {
		if (FormatPaste.pattern == null) {
			JOptionPane.showMessageDialog(Freeplane.getController()
			    .getViewController().getContentPane(),
			    getResourceString("no_format_copy_before_format_paste"), "" /*
			    				    				 * =Title
			    				    				 */, JOptionPane.ERROR_MESSAGE);
			return;
		}
		getMindMapController().getPatternController().applyPattern(node,
		    FormatPaste.pattern);
	}
}
