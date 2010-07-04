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
package org.freeplane.plugin.latex;

import java.awt.Component;
import java.awt.Container;
import java.util.Iterator;
import java.util.Set;

import org.freeplane.core.addins.NodeHookDescriptor;
import org.freeplane.core.addins.PersistentNodeHook;
import org.freeplane.core.extension.IExtension;
import org.freeplane.core.modecontroller.INodeViewLifeCycleListener;
import org.freeplane.core.modecontroller.ModeController;
import org.freeplane.core.model.MapModel;
import org.freeplane.core.model.NodeModel;
import org.freeplane.core.ui.ActionLocationDescriptor;
import org.freeplane.core.undo.IActor;
import org.freeplane.n3.nanoxml.XMLElement;
import org.freeplane.view.swing.map.NodeView;

/**
 * @author Dimitry Polivaev
 * @file LatexNodeHook.java
 * @package freeplane.modes.mindmapmode
 */
@NodeHookDescriptor(hookName = "plugins/latex/LatexNodeHook.properties", //
onceForMap = false)
@ActionLocationDescriptor(locations = "/menu_bar/insert/other")
class LatexNodeHook extends PersistentNodeHook implements INodeViewLifeCycleListener {
	/**
	 */
	public LatexNodeHook(final ModeController modeController) {
		super(modeController);
		modeController.addINodeViewLifeCycleListener(this);
	}

	@Override
	protected void add(final NodeModel node, final IExtension extension) {
		final LatexExtension latexExtension = (LatexExtension) extension;
		final Iterator iterator = node.getViewers().iterator();
		while (iterator.hasNext()) {
			final NodeView view = (NodeView) iterator.next();
			createViewer(latexExtension, view);
		}
		super.add(node, extension);
	}

	@Override
	protected IExtension createExtension(final NodeModel node, final XMLElement element) {
		final LatexExtension latexExtension = new LatexExtension();
		if (element != null) {
			latexExtension.setEquation(element.getAttribute("EQUATION"));
			getModeController().getMapController().nodeChanged(node);
		}
		return latexExtension;
	}

	void createViewer(final LatexExtension model, final NodeView view) {
		final JLatexViewer comp = new JLatexViewer(this, model);
		final Set<JLatexViewer> viewers = model.getViewers();
		viewers.add(comp);
		view.getContentPane().add(comp);
	}

	void deleteViewer(final LatexExtension model, final NodeView nodeView) {
		final Set<JLatexViewer> viewers = model.getViewers();
		if (viewers.isEmpty()) {
			return;
		}
		final Container contentPane = nodeView.getContentPane();
		final int componentCount = contentPane.getComponentCount();
		for (int i = 0; i < componentCount; i++) {
			final Component component = contentPane.getComponent(i);
			if (viewers.contains(component)) {
				viewers.remove(component);
				contentPane.remove(i);
				return;
			}
		}
	}

	@Override
	protected Class getExtensionClass() {
		return LatexExtension.class;
	}

	public void onViewCreated(final Container container) {
		final NodeView nodeView = (NodeView) container;
		final LatexExtension latexExtension = (LatexExtension) nodeView.getModel().getExtension(LatexExtension.class);
		if (latexExtension == null) {
			return;
		}
		createViewer(latexExtension, nodeView);
	}

	public void onViewRemoved(final Container container) {
		final NodeView nodeView = (NodeView) container;
		final LatexExtension latexExtension = (LatexExtension) nodeView.getModel().getExtension(LatexExtension.class);
		if (latexExtension == null) {
			return;
		}
		deleteViewer(latexExtension, nodeView);
	}

	@Override
	protected void remove(final NodeModel node, final IExtension extension) {
		final LatexExtension latexExtension = (LatexExtension) extension;
		latexExtension.removeViewers();
		super.remove(node, extension);
	}

	@Override
	protected void saveExtension(final IExtension extension, final XMLElement element) {
		final LatexExtension latexExtension = (LatexExtension) extension;
		element.setAttribute("EQUATION", latexExtension.getEquation());
		super.saveExtension(extension, element);
	}

	void setEquationUndoable(final LatexExtension model, final String newEquation) {
		final String equation = model.getEquation();
		if (equation.equals(newEquation)) {
			return;
		}
		final IActor actor = new IActor() {
			private final String oldEquation = equation;

			public void act() {
				model.setEquation(newEquation);
				final MapModel map = getModeController().getController().getMap();
				getModeController().getMapController().setSaved(map, false);
			}

			public String getDescription() {
				return "setLatexEquationUndoable";
			}

			public void undo() {
				model.setEquation(oldEquation);
			}
		};
		getModeController().execute(actor, getModeController().getController().getMap());
	}
}
