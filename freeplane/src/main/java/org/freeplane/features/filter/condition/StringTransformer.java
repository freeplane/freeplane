package org.freeplane.features.filter.condition;

import java.text.Normalizer;
import java.util.regex.Pattern;

public class StringTransformer {
    private String value;

    private static final Pattern ACCENTS = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");

    public StringTransformer(String value) {
        super();
        this.value = value;
    }

    public static String transform (String value, boolean toLowerCase, boolean removeAccents) {
        if(! toLowerCase && ! removeAccents)
            return value;
        StringTransformer t = new StringTransformer(value);
        if(toLowerCase)
            t.toLowerCase();
        if(removeAccents)
            t.removeAccents();
        return t.value();
    }

    public StringTransformer toLowerCase() {
        value = value.toLowerCase();
        return this;
    }

    public StringTransformer removeAccents() {
        value = ACCENTS.matcher(Normalizer.normalize(value, Normalizer.Form.NFD))
        .replaceAll("");
        return this;
    }

    public String value() {
        return value;
    }
}
