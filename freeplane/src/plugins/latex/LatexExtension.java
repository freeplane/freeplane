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
package plugins.latex;

import java.awt.Component;
import java.awt.Container;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.freeplane.core.extension.IExtension;
import org.freeplane.core.map.NodeModel;
import org.freeplane.core.undo.IUndoableActor;
import org.freeplane.view.map.NodeView;

/**
 * @author Dimitry Polivaev
 * 06.12.2008
 */
class LatexExtension implements IExtension {
	private String equation;
	private final NodeModel node;
	final private Set<JZoomedHotEqn> viewers;

	public LatexExtension(final NodeModel node) {
		equation = "\\mbox{I}^\\fgcolor{ff0000}{\\heartsuit}\\mbox{HotEqn}";
		viewers = new LinkedHashSet();
		this.node = node;
	}

	void createViewer(final NodeView view) {
		final JZoomedHotEqn comp = new JZoomedHotEqn(this);
		viewers.add(comp);
		view.getContentPane().add(comp);
	}

	void deleteViewer(final NodeView nodeView) {
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

	public String getEquation() {
		return equation;
	}

	private NodeModel getNode() {
		return node;
	}

	Set<JZoomedHotEqn> getViewers() {
		return viewers;
	}

	void removeViewers() {
		final Iterator iterator = getViewers().iterator();
		while (iterator.hasNext()) {
			final JZoomedHotEqn comp = (JZoomedHotEqn) iterator.next();
			comp.getParent().remove(comp);
		}
		viewers.clear();
	}

	public void setEquation(final String equation) {
		this.equation = equation;
		final Iterator iterator = viewers.iterator();
		while (iterator.hasNext()) {
			final JZoomedHotEqn comp = (JZoomedHotEqn) iterator.next();
			comp.setModel(this);
		}
		getNode().getModeController().getMapController().nodeChanged(getNode());
	}

	public void setEquationUndoable(final String newEquation) {
		if (equation.equals(newEquation)) {
			return;
		}
		final IUndoableActor actor = new IUndoableActor() {
			private final String oldEquation = equation;

			public void act() {
				setEquation(newEquation);
			}

			public String getDescription() {
				return "setLatexEquationUndoable";
			}

			public void undo() {
				setEquation(oldEquation);
			}
		};
		getNode().getModeController().execute(actor);
	}
}
