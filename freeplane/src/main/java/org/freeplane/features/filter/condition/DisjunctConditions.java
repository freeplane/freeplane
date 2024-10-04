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

import java.awt.FontMetrics;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import javax.swing.Icon;

import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 */
public class DisjunctConditions extends CombinedConditions implements ICombinedCondition {
	static final String NAME = "disjunct_condition";

    public static DisjunctConditions combine(final ASelectableCondition... conditions) {
        return  new DisjunctConditions(CombinedConditions.combine(DisjunctConditions.class, conditions));
    }

	static ASelectableCondition load(final ConditionFactory conditionFactory, final XMLElement element) {
	    final ASelectableCondition[] conditions = loadConditions(conditionFactory, element);
		return new DisjunctConditions(conditions);
	}

	final private ASelectableCondition[] conditions;

	/**
	 *
	 */
	DisjunctConditions(final ASelectableCondition... conditions) {
		this.conditions = conditions;
	}

    @Override
    protected ASelectableCondition[] getConditions() {
       return conditions;
    }

	/*
	 * (non-Javadoc)
	 * @see
	 * freeplane.controller.filter.condition.Condition#checkNode(freeplane.modes
	 * .MindMapNode)
	 */
	@Override
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
    @Override
    protected List<Icon> createRenderedIcons(FontMetrics fontMetrics) {
        return createRenderedIcons("filter_or", fontMetrics);
    }


    @Override
    public boolean canBePersisted() {
        return Stream.of(conditions).allMatch(ASelectableCondition::canBePersisted);
    }

	@Override
    public void fillXML(final XMLElement child) {
		for (final ASelectableCondition condition : conditions) {
			condition.toXml(child);
		}
	}

	@Override
    protected String createDescription() {
        return createDescription("filter_or");
    }

	@Override
    protected String getName() {
	    return NAME;
    }
	@Override
    public Collection<ASelectableCondition> split() {
	    return Arrays.asList(conditions);
    }

    @Override
    protected ASelectableCondition createConditions(ASelectableCondition[] newConditions) {
        return new DisjunctConditions(newConditions);
    }
}
