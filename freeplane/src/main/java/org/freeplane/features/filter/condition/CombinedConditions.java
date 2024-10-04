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

import java.awt.Color;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import javax.swing.Icon;

import org.freeplane.core.ui.components.IconListComponent;
import org.freeplane.core.ui.components.IconRow;
import org.freeplane.core.ui.components.ObjectIcon;
import org.freeplane.core.ui.components.TextIcon;
import org.freeplane.core.util.TextUtils;
import org.freeplane.n3.nanoxml.XMLElement;

/**
 * @author Dimitry Polivaev
 */
abstract class CombinedConditions extends ASelectableCondition implements ICombinedCondition{

    protected static ASelectableCondition[] loadConditions(final ConditionFactory conditionFactory,
            final XMLElement element) {
        final Vector<XMLElement> children = element.getChildren();
        final ASelectableCondition[] conditions = new ASelectableCondition[children.size()];
        for (int i = 0; i < conditions.length; i++) {
            final ASelectableCondition condition = conditionFactory.loadCondition(children.get(i));
            if(condition == null){
                return null;
            }
            conditions[i] = condition;
        }
        return conditions;
    }

    static ASelectableCondition[] combine(Class<? extends CombinedConditions> targetClass, final ASelectableCondition... conditions) {
        int conjunctConditionsCounter = 0;
        int conjunctConditionsLength = 0;
        for(ASelectableCondition condition : conditions) {
            if(condition.getClass().equals(targetClass)) {
                conjunctConditionsCounter++;
                conjunctConditionsLength+=((CombinedConditions)condition).getConditions().length;
            }
        }
        ASelectableCondition[] combinedConditions;
        if(conjunctConditionsCounter == 0)
            combinedConditions = conditions;
        else {
            combinedConditions = new ASelectableCondition[conditions.length + conjunctConditionsLength - conjunctConditionsCounter];
            conjunctConditionsCounter = 0;
            for(ASelectableCondition condition : conditions) {
                if(condition.getClass().equals(targetClass)) {
                    ASelectableCondition[] containedConditions = ((CombinedConditions)condition).getConditions();
                    System.arraycopy(containedConditions, 0, combinedConditions, conjunctConditionsCounter, containedConditions.length);
                    conjunctConditionsCounter += containedConditions.length;
                }
                else {
                    combinedConditions[conjunctConditionsCounter] = condition;
                    conjunctConditionsCounter++;
                }
            }
        }
        return combinedConditions;
    }

    protected abstract ASelectableCondition[] getConditions();


    @Override
    public IconListComponent createGraphicComponent(FontMetrics  fontMetrics) {
        IconListComponent graphicComponent = super.createGraphicComponent(fontMetrics);
        graphicComponent.removeIcon(getUserName() != null ? 1 : 0);
        graphicComponent.removeIcon(graphicComponent.getIconCount() - 1);
        return graphicComponent;
    }

    @Override
    protected IconListComponent createIconListComponent(List<Icon> icons) {
        return new IconListComponent(icons);
    }

    @Override
    protected List<Icon> createRenderedIcons(String operatorTextPropertyName, FontMetrics fontMetrics) {
        List<Icon> iconList = new ArrayList<Icon>();
        iconList.add(new ObjectIcon<>(this, new TextIcon("(", fontMetrics)));
        ASelectableCondition[] conditions = getConditions();
        ASelectableCondition cond = conditions[0];
        iconList.add(conditionIcons(cond, fontMetrics));
        Color operatorBackgroundColor = operatorBackgroundColor();
        final String operator = TextUtils.getText(operatorTextPropertyName);
        TextIcon gapIcon = textIcon(" ", fontMetrics,
                icon -> {/**/});
        TextIcon operatorSymbolIcon = textIcon(operator, fontMetrics,
                icon -> icon.setIconBackgroundColor(operatorBackgroundColor));
        ObjectIcon<ASelectableCondition> operatorIcon = new ObjectIcon<ASelectableCondition>(this,
                new IconRow(Arrays.asList(gapIcon, operatorSymbolIcon, gapIcon)));
        for (int i = 1; i < conditions.length; i++) {
            iconList.add(operatorIcon);
            cond = conditions[i];
            iconList.add(conditionIcons(cond, fontMetrics));
        }
        iconList.add(new ObjectIcon<>(this, new TextIcon(")", fontMetrics)));
        return iconList;
    }

    private ObjectIcon<ASelectableCondition> conditionIcons(ASelectableCondition conditions,
            FontMetrics fontMetrics) {
        return new ObjectIcon<>(conditions, new IconRow(conditions.createSmallRendererIcons(fontMetrics)));
    }

    protected String createDescription(String operatorTextPropertyName) {
        StringBuilder description = new StringBuilder();
        description.append("(");
        ASelectableCondition[] conditions = getConditions();
        ASelectableCondition cond = conditions[0];
        description.append(cond.createSmallDescription());
        for (int i = 1; i < conditions.length; i++) {
            final String operator = TextUtils.getText(operatorTextPropertyName);
            description.append(' ' + operator + ' ');
            cond = conditions[i];
            description.append(cond.createSmallDescription());
        }
        description.append(")");
        return description.toString();
    }

    @Override
    public ASelectableCondition removeCondition(ASelectableCondition removedCondition) {
        if (removedCondition == this)
            return null;
        ASelectableCondition[] conditions = getConditions();
        for (int i = 0; i < conditions.length; i++) {
            ASelectableCondition condition = conditions[i];
            ASelectableCondition newCondition = condition.removeCondition(removedCondition);
            if(newCondition != condition) {
                return replaceCondition(i, newCondition);
            }
        }
        return this;
    }

    private ASelectableCondition replaceCondition(int replacedIndex, ASelectableCondition newCondition) {
        ASelectableCondition[] oldConditions = getConditions();
        int newLength = oldConditions.length - (newCondition == null ? 1 : 0);
        if(newLength == 1)
            return oldConditions[1 - replacedIndex];
        ASelectableCondition[] newConditions = new ASelectableCondition[newLength];
        for(int i = 0, j = 0; i < newLength;) {
            if(i != replacedIndex) {
                newConditions[j++] = oldConditions[i++];
            }
            else if(newCondition != null) {
                newConditions[j++] = newCondition;
                i++;
            }
        }
        return createConditions(newConditions);
    }

    protected abstract ASelectableCondition createConditions(ASelectableCondition[] newConditions);


}
