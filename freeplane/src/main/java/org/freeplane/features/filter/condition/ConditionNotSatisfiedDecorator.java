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
public class ConditionNotSatisfiedDecorator extends ASelectableCondition implements ICombinedCondition{
	static final String NAME = "negate_condition";

	static ASelectableCondition load(final ConditionFactory conditionFactory, final XMLElement element) {
		final Vector<XMLElement> children = element.getChildren();
		final ASelectableCondition cond = conditionFactory.loadCondition(children.get(0));
		if(cond == null){
			return null;
		}
		return new ConditionNotSatisfiedDecorator(cond);
	}

	final private ASelectableCondition originalCondition;

	/**
	 *
	 */
	public ConditionNotSatisfiedDecorator(final ASelectableCondition originalCondition) {
		super();
		assert originalCondition != null;
		this.originalCondition = originalCondition;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.filter.condition.Condition#checkNode(freeplane.modes
	 * .MindMapNode)
	 */
	public boolean checkNode(final NodeModel node) {
		return !originalCondition.checkNode(node);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.filter.condition.Condition#getListCellRendererComponent
	 * ()
	 */
	protected JComponent createRendererComponent() {
		final JCondition component = new JCondition();
		final String not = TextUtils.getText("filter_not");
		final String text = not + ' ';
		component.add(ConditionFactory.createConditionLabel(text));
		originalCondition.getUserName();
		final JComponent renderer = originalCondition.createShortRendererComponent();
		component.add(renderer);
		return component;
	}

	public void fillXML(final XMLElement child) {
		originalCondition.toXml(child);
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
	    return Arrays.asList(new ASelectableCondition[]{originalCondition});
    }

}
