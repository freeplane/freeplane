/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2011 Volker Boerchers
 *
 *  This file author is Volker Boerchers
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
package org.freeplane.features.format;

import org.freeplane.core.util.FactoryMethod;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.SerializationMethod;

/**
 * IFormattedObject that stores the format along a formula. This object does no formatting for itself but lets this
 * delegate to the FormulaTextTransformer.
 * @author vboerchers
 */
@FactoryMethod("deserialize")
@SerializationMethod("serialize")
public class FormattedFormula implements IFormattedObject {
    private static final String BAR = "@B@A@R@";
    private final String formula;
    private final String pattern;

    public FormattedFormula(final String formula, final String pattern) {
        this.formula = formula;
        this.pattern = pattern;
    }

    public String getPattern() {
        return pattern;
    }

    public String getObject() {
        return formula;
    }

    public static String serialize(final FormattedFormula formattedObject) {
        return encodeBar(formattedObject.formula) + "|" + formattedObject.pattern;
    }

    public static Object deserialize(final String text) {
        try {
            final int index = text.indexOf('|');
            final String formula = decodeBar(text.substring(0, index));
            return new FormattedFormula(formula, text.substring(index + 1));
        }
        catch (Exception e) {
            LogUtils.warn("cannot deserialize " + text, e);
            return text;
        }
    }

    private static String encodeBar(String formulaText) {
        return formulaText.replace("|", BAR);
    }
    
    private static String decodeBar(String formulaText) {
        return formulaText.replace(BAR, "|");
    }

    @Override
    public String toString() {
        return formula;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((formula == null) ? 0 : formula.hashCode());
        result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FormattedFormula other = (FormattedFormula) obj;
        if (formula == null) {
            if (other.formula != null)
                return false;
        }
        else if (!formula.equals(other.formula))
            return false;
        if (pattern == null) {
            if (other.pattern != null)
                return false;
        }
        else if (!pattern.equals(other.pattern))
            return false;
        return true;
    }
}
