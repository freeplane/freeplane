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
package org.freeplane.service.filter.condition;

import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.freeplane.controller.Freeplane;
import org.freeplane.io.xml.n3.nanoxml.XMLElement;
import org.freeplane.main.Tools;
import org.freeplane.map.tree.NodeModel;
import org.freeplane.service.filter.FilterController;

/**
 * @author Dimitry Polivaev
 */
public class DisjunctConditions implements ICondition {
	static final String NAME = "disjunct_condition";

	static ICondition load(final XMLElement element) {
		final Vector children = element.getChildren();
		final Object[] conditions = new Object[children.size()];
		for (int i = 0; i < conditions.length; i++) {
			final ICondition cond = FilterController.getConditionFactory()
			    .loadCondition((XMLElement) children.get(i));
			conditions[i] = cond;
		}
		return new DisjunctConditions(conditions);
	}

	final private Object[] conditions;

	/**
	 *
	 */
	public DisjunctConditions(final Object[] conditions) {
		this.conditions = conditions;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freemind.controller.filter.condition.Condition#checkNode(freemind.modes
	 * .MindMapNode)
	 */
	public boolean checkNode(final NodeModel node) {
		for (int i = 0; i < conditions.length; i++) {
			final ICondition cond = (ICondition) conditions[i];
			if (cond.checkNode(node)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freemind.controller.filter.condition.Condition#getListCellRendererComponent
	 * ()
	 */
	public JComponent getListCellRendererComponent() {
		final JCondition component = new JCondition();
		component.add(new JLabel("("));
		ICondition cond = (ICondition) conditions[0];
		JComponent rendererComponent = cond.getListCellRendererComponent();
		rendererComponent.setOpaque(false);
		component.add(rendererComponent);
		int i;
		for (i = 1; i < conditions.length; i++) {
			final String or = Tools.removeMnemonic(Freeplane.getController()
			    .getResourceController().getResourceString("filter_or"));
			final String text = ' ' + or + ' ';
			component.add(new JLabel(text));
			cond = (ICondition) conditions[i];
			rendererComponent = cond.getListCellRendererComponent();
			rendererComponent.setOpaque(false);
			component.add(rendererComponent);
		}
		component.add(new JLabel(")"));
		return component;
	}

	public void toXml(final XMLElement element) {
		final XMLElement child = new XMLElement();
		child.setName(DisjunctConditions.NAME);
		for (int i = 0; i < conditions.length; i++) {
			final ICondition cond = (ICondition) conditions[i];
			cond.toXml(child);
		}
		element.addChild(child);
	}
}
