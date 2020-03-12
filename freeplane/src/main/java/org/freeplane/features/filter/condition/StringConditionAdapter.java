package org.freeplane.features.filter.condition;

import org.freeplane.n3.nanoxml.XMLElement;

public abstract class StringConditionAdapter extends ASelectableCondition {
    public static final String MATCH_CASE = "MATCH_CASE";
    public static final String MATCH_APPROXIMATELY = "MATCH_APPROXIMATELY";
    public static final String IGNORE_DIACRITICS = "IGNORE_DIACRITICS";
    private transient String normalizedValue;
    protected final boolean matchCase;
    protected final boolean matchApproximately;
    protected final boolean ignoreDiacritics;

    public StringConditionAdapter(boolean matchCase,
            boolean matchApproximately, boolean ignoreDiacritics) {
        super();
        this.matchCase = matchCase;
        this.matchApproximately = matchApproximately;
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

    @Override
    protected void fillXML(XMLElement element) {
        super.fillXML(element);
        if(matchCase)
            element.setAttribute(MATCH_CASE, "true");
        if(matchApproximately)
            element.setAttribute(MATCH_APPROXIMATELY, "true");
        if(ignoreDiacritics)
            element.setAttribute(IGNORE_DIACRITICS, "true");
    }
}
