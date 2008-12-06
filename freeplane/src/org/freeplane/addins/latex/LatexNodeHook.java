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
package org.freeplane.addins.latex;

import java.util.Iterator;

import org.freeplane.addins.NodeHookDescriptor;
import org.freeplane.addins.PersistentNodeHook;
import org.freeplane.controller.ActionDescriptor;
import org.freeplane.extension.IExtension;
import org.freeplane.io.xml.n3.nanoxml.IXMLElement;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.map.tree.view.NodeView;
import org.freeplane.modes.INodeViewLifeCycleListener;
import org.freeplane.modes.ModeController;

/**
 * @author Dimitry Polivaev
 * @file LatexNodeHook.java
 * @package freemind.modes.mindmapmode
 */
@NodeHookDescriptor(hookName = "plugins/latex/LatexNodeHook.properties", //
onceForMap = false)
@ActionDescriptor(name = "plugins/latex/LatexNodeHook.properties_name", //
locations = "/menu_bar/insert/other", //
tooltip = "plugins/latex/LatexNodeHook.properties_documentation")
public class LatexNodeHook extends PersistentNodeHook implements
        INodeViewLifeCycleListener {
	/**
	 */
	public LatexNodeHook(final ModeController modeController) {
		super(modeController);
		modeController.addNodeViewLifeCycleListener(this);
	}

	@Override
	protected void add(final NodeModel node, final IExtension extension) {
		final LatexExtension latexExtension = (LatexExtension) extension;
		final Iterator iterator = node.getViewers().iterator();
		while (iterator.hasNext()) {
			final NodeView view = (NodeView) iterator.next();
			latexExtension.createViewer(view);
		}
		super.add(node, extension);
	}

	@Override
	protected IExtension createExtension(final NodeModel node,
	                                     final IXMLElement element) {
		final LatexExtension latexExtension = new LatexExtension(node);
		if (element != null) {
			latexExtension.setEquation(element.getAttribute("EQUATION"));
		}
		return latexExtension;
	}

	@Override
	protected Class getExtensionClass() {
		return LatexExtension.class;
	}

	public void onViewCreated(final NodeView nodeView) {
		final LatexExtension latexExtension = (LatexExtension) nodeView
		    .getModel().getExtension(LatexExtension.class);
		if (latexExtension == null) {
			return;
		}
		latexExtension.createViewer(nodeView);
	}

	public void onViewRemoved(final NodeView nodeView) {
		final LatexExtension latexExtension = (LatexExtension) nodeView
		    .getModel().getExtension(LatexExtension.class);
		if (latexExtension == null) {
			return;
		}
		latexExtension.deleteViewer(nodeView);
	}

	@Override
	protected void remove(final NodeModel node, final IExtension extension) {
		final LatexExtension latexExtension = (LatexExtension) extension;
		latexExtension.removeViewers();
		super.remove(node, extension);
	}

	@Override
	protected void saveExtension(final IExtension extension,
	                             final IXMLElement element) {
		final LatexExtension latexExtension = (LatexExtension) extension;
		element.setAttribute("EQUATION", latexExtension.getEquation());
		super.saveExtension(extension, element);
	}
}
