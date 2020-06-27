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

/**
 * @author Dimitry Polivaev
 */
abstract class CombinedConditions extends ASelectableCondition implements ICombinedCondition{

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
    
}
