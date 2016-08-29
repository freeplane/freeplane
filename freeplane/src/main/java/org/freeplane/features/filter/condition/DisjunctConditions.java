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
package org.freeplane.features.filter.condition;

import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JLabel;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 */
public class DisjunctConditions extends ASelectableCondition implements ICombinedCondition {
	static final String NAME = "disjunct_condition";

	static ASelectableCondition load(final ConditionFactory conditionFactory, final XMLElement element) {
		final Vector<XMLElement> children = element.getChildren();
		final ASelectableCondition[] conditions = new ASelectableCondition[children.size()];
		for (int i = 0; i < conditions.length; i++) {
			final ASelectableCondition condition = conditionFactory.loadCondition(children.get(i));
			if(condition == null){
				return null;
			}
			conditions[i] = condition;
		}
		return new DisjunctConditions(conditions);
	}

	final private ASelectableCondition[] conditions;

	/**
	 *
	 */
	public DisjunctConditions(final ASelectableCondition[] conditions) {
		this.conditions = conditions;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.filter.condition.Condition#checkNode(freeplane.modes
	 * .MindMapNode)
	 */
	public boolean checkNode(final NodeModel node) {
		for (final ASelectableCondition condition : conditions) {
			if (condition.checkNode(node)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.filter.condition.Condition#getListCellRendererComponent
	 * ()
	 */
	protected JComponent createRendererComponent() {
		final JCondition component = new JCondition();
		component.add(ConditionFactory.createConditionLabel("("));
		ASelectableCondition cond = conditions[0];
		JComponent rendererComponent = cond.createShortRendererComponent();
		component.add(rendererComponent);
		for (int i = 1; i < conditions.length; i++) {
			final String or = TextUtils.getText("filter_or");
			final String text = ' ' + or + ' ';
			component.add(new JLabel(text));
			cond = conditions[i];
			rendererComponent = cond.createRendererComponent();
			component.add(rendererComponent);
		}
		component.add(ConditionFactory.createConditionLabel(")"));
		return component;
	}

	public void fillXML(final XMLElement child) {
		for (final ASelectableCondition condition : conditions) {
			condition.toXml(child);
		}
	}

	@Override
    protected String createDescription() {
	    return NAME;
    }

	@Override
    protected String getName() {
	    return NAME;
    }
	public Collection<ASelectableCondition> split() {
	    return Arrays.asList(conditions);
    }
}
