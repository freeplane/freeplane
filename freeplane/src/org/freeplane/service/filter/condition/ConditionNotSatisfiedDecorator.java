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
public class ConditionNotSatisfiedDecorator implements ICondition {
	static final String NAME = "negate_condition";

	static ICondition load(final XMLElement element) {
		final Vector children = element.getChildren();
		final ICondition cond = FilterController.getConditionFactory()
		    .loadCondition((XMLElement) children.get(0));
		return new ConditionNotSatisfiedDecorator(cond);
	}

	final private ICondition originalCondition;

	/**
	 *
	 */
	public ConditionNotSatisfiedDecorator(final ICondition originalCondition) {
		super();
		this.originalCondition = originalCondition;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freemind.controller.filter.condition.Condition#checkNode(freemind.modes
	 * .MindMapNode)
	 */
	public boolean checkNode(final NodeModel node) {
		return !originalCondition.checkNode(node);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freemind.controller.filter.condition.Condition#getListCellRendererComponent
	 * ()
	 */
	public JComponent getListCellRendererComponent() {
		final JCondition component = new JCondition();
		final String not = Tools
		    .removeMnemonic(Freeplane.getText("filter_not"));
		final String text = not + ' ';
		component.add(new JLabel(text));
		final JComponent renderer = originalCondition
		    .getListCellRendererComponent();
		renderer.setOpaque(false);
		component.add(renderer);
		return component;
	}

	public void toXml(final XMLElement element) {
		final XMLElement child = new XMLElement();
		child.setName(ConditionNotSatisfiedDecorator.NAME);
		originalCondition.toXml(child);
		element.addChild(child);
	}
}
