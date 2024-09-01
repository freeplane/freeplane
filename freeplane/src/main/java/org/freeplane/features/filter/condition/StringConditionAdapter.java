package org.freeplane.features.filter.condition;

import org.freeplane.core.util.TextUtils;
import org.freeplane.features.filter.StringMatchingStrategy.Type;
import org.freeplane.n3.nanoxml.XMLElement;

public abstract class StringConditionAdapter extends ASelectableCondition {
    public static final String MATCH_CASE = "MATCH_CASE";
    public static final String MATCH_APPROXIMATELY = "MATCH_APPROXIMATELY";
    public static final String MATCH_WORDWISE = "MATCH_WORDWISE";
    public static final String IGNORE_DIACRITICS = "IGNORE_DIACRITICS";
    private transient String normalizedValue;
    protected final boolean matchCase;
    protected final boolean matchApproximately;
    protected final boolean matchWordwise;
    protected final boolean ignoreDiacritics;

    public StringConditionAdapter(boolean matchCase,
            boolean matchApproximately, boolean matchWordwise, boolean ignoreDiacritics) {
        super();
        this.matchCase = matchCase;
        this.matchApproximately = matchApproximately;
        this.matchWordwise = matchWordwise;
        this.ignoreDiacritics = ignoreDiacritics;
    }

    protected String normalizedValue() {
        if(normalizedValue == null)
            normalizedValue = normalize(conditionValue());
        return normalizedValue;
    }

    protected abstract Object conditionValue();

    protected String normalize(Object value) {
        return StringTransformer.transform(value.toString(), !matchCase, ignoreDiacritics);
    }

    protected String containsDescription() {
        return matchWordwise ? TextUtils.getText(ConditionFactory.FILTER_CONTAINS_WORDWISE)
                :  TextUtils.getText(ConditionFactory.FILTER_CONTAINS);
    }

    protected Type substringMatchType() {
        return matchWordwise ? Type.WORDWISE : Type.SUBSTRING;
    }

    @Override
    protected void fillXML(XMLElement element) {
        super.fillXML(element);
        if(matchCase)
            element.setAttribute(MATCH_CASE, "true");
        if(matchApproximately)
            element.setAttribute(MATCH_APPROXIMATELY, "true");
        if(matchWordwise)
            element.setAttribute(MATCH_WORDWISE, "true");
        if(ignoreDiacritics)
            element.setAttribute(IGNORE_DIACRITICS, "true");
    }

    protected String createDescription(final String attribute, final String simpleCondition, final String value) {
        return ConditionFactory.createDescription(attribute, simpleCondition, value, matchCase, matchApproximately, ignoreDiacritics);
    }


}
