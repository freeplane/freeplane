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
package org.freeplane.core.filter.condition;

import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.freeplane.core.model.NodeModel;
import org.freeplane.core.resources.FpStringUtils;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 */
public class ConjunctConditions implements ISelectableCondition {
	static final String NAME = "conjunct_condition";

	@SuppressWarnings("unchecked")
	static ISelectableCondition load(final ConditionFactory conditionFactory, final XMLElement element) {
		final Vector<XMLElement> children = element.getChildren();
		final ISelectableCondition[] conditions = new ISelectableCondition[children.size()];
		for (int i = 0; i < conditions.length; i++) {
			conditions[i] = conditionFactory.loadCondition(children.get(i));
		}
		return new ConjunctConditions(conditions);
	}

	final private ISelectableCondition[] conditions;

	/**
	 *
	 */
	public ConjunctConditions(final ISelectableCondition[] conditions) {
		this.conditions = conditions;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.filter.condition.Condition#checkNode(freeplane.modes
	 * .MindMapNode)
	 */
	public boolean checkNode(final NodeModel node) {
		for(ISelectableCondition condition : conditions) {
			if (!condition.checkNode(node)) {
				return false;
			}
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.filter.condition.Condition#getListCellRendererComponent
	 * ()
	 */
	public JComponent getListCellRendererComponent() {
		final JCondition component = new JCondition();
		component.add(new JLabel("("));
		ISelectableCondition cond = conditions[0];
		JComponent rendererComponent = cond.getListCellRendererComponent();
		rendererComponent.setOpaque(false);
		component.add(rendererComponent);
		for (int i = 1; i < conditions.length; i++) {
			final String and = FpStringUtils.removeMnemonic(ResourceBundles.getText("filter_and"));
			final String text = ' ' + and + ' ';
			component.add(new JLabel(text));
			cond = conditions[i];
			rendererComponent = cond.getListCellRendererComponent();
			rendererComponent.setOpaque(false);
			component.add(rendererComponent);
		}
		component.add(new JLabel(")"));
		return component;
	}

	public void toXml(final XMLElement element) {
		final XMLElement child = new XMLElement();
		child.setName(ConjunctConditions.NAME);
		for(ISelectableCondition condition : conditions) {
			condition.toXml(child);
		}
		element.addChild(child);
	}
}
