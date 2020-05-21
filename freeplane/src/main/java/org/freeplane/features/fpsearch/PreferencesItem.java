package org.freeplane.features.fpsearch;

public class PreferencesItem {

    public PreferencesItem(final String tab, final String separator, final String key, final String text, final String tooltip)
    {
        this.tab = tab;
        this.separator = separator;
        this.key = key;
        this.text = text;
        this.tooltip = tooltip;
    }

    public String tab;
    public String separator;
    public String key;
    public String text;
    public String tooltip;

    @Override
    public String toString()
    {
        return String.format("PreferencesItem[%s:%s:%s:%s]", tab, separator, key, text);
    }
}
