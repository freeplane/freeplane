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
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 */
abstract public class DecoratedCondition extends ASelectableCondition implements ICombinedCondition{

	protected static ASelectableCondition loadOriginalCondition(final ConditionFactory conditionFactory, final XMLElement element) {
		final Vector<XMLElement> children = element.getChildren();
		final ASelectableCondition cond = conditionFactory.loadCondition(children.get(0));
		return cond;
	}

	final protected ASelectableCondition originalCondition;
    final private String decoratorKey;

	/**
	 *
	 */
	public DecoratedCondition(final ASelectableCondition originalCondition, String name, String decoratorKey) {
		super();
        this.name = name;
        this.decoratorKey = decoratorKey;
		assert originalCondition != null;
		this.originalCondition = originalCondition;
	}
	

    private final String name;

    @Override
    protected String createDescription() {
        return name;
    }

    @Override
    protected String getName() {
        return name;
    }


	abstract public boolean checkNode(final NodeModel node);

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.filter.condition.Condition#getListCellRendererComponent
	 * ()
	 */
	protected JComponent createRendererComponent() {
		final JCondition component = new JCondition();
        final String decoratorText = TextUtils.getText(decoratorKey);
		final String text = decoratorText + ' ';
		component.add(ConditionFactory.createConditionLabel(text));
		final JComponent renderer = originalCondition.createShortRendererComponent();
		component.add(renderer);
		return component;
	}

	@Override
    public boolean canBePersisted() {
        return originalCondition.canBePersisted();
    }

    public void fillXML(final XMLElement child) {
		originalCondition.toXml(child);
	}
	
	public Collection<ASelectableCondition> split() {
	    return Arrays.asList(new ASelectableCondition[]{originalCondition});
    }

    public boolean checksParent() {
        return originalCondition.checksParent();
    }

    public boolean checksAncestors() {
        return originalCondition.checksAncestors();
    }

    public boolean checksChildren() {
        return originalCondition.checksChildren();
    }

    public boolean checksDescendants() {
        return originalCondition.checksDescendants();
    }
	
	

}
