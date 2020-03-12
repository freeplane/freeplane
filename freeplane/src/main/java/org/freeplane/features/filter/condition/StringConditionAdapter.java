package org.freeplane.features.filter.condition;

import org.freeplane.core.io.xml.TreeXmlWriter;
import org.freeplane.features.map.NodeModel;
import org.freeplane.n3.nanoxml.XMLElement;

public abstract class StringConditionAdapter extends ASelectableCondition {
    public static final String MATCH_CASE = "MATCH_CASE";
    public static final String MATCH_APPROXIMATELY = "MATCH_APPROXIMATELY";
    private transient String normalizedValue;
    protected final boolean matchCase;
    protected final boolean matchApproximately;

    public StringConditionAdapter(boolean matchCase,
            boolean matchApproximately) {
        super();
        this.matchCase = matchCase;
        this.matchApproximately = matchApproximately;
    }

    protected String normalizedValue() {
        if(normalizedValue == null)
            normalizedValue = normalize(conditionValue());
        return normalizedValue;
    }

    protected abstract Object conditionValue();

    protected String normalize(Object value) {
        return StringTransformer.transform(value.toString(), !matchCase, false);
    }

    @Override
    public boolean checkNode(NodeModel node) {
        // TODO Auto-generated method stub
        throw new RuntimeException("Method not implemented");
    }

    @Override
    protected void fillXML(XMLElement element) {
        super.fillXML(element);
        if(matchCase)
            element.setAttribute(MATCH_CASE, "true");
        if(matchApproximately)
            element.setAttribute(MATCH_APPROXIMATELY, "true");
    }
}
