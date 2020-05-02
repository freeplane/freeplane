package org.freeplane.features.fpsearch;

public class PreferencesItem {

    public PreferencesItem(final String tab, final String separator, final String key, final String text)
    {
        this.tab = tab;
        this.separator = separator;
        this.key = key;
        this.text = text;
    }

    public String tab;
    public String separator;
    public String key;
    public String text;

    @Override
    public String toString()
    {
        return String.format("PreferencesItem[%s:%s:%s:%s]", tab, separator, key, text);
    }
}
