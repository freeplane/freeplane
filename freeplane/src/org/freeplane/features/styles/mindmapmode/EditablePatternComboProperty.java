package org.freeplane.features.styles.mindmapmode;

import java.util.List;

import org.freeplane.core.resources.components.EditableComboProperty;
import org.freeplane.features.format.PatternFormat;

public class EditablePatternComboProperty extends EditableComboProperty<PatternFormat> {
    private PatternFormat defaultPattern;

    public EditablePatternComboProperty(final String name, PatternFormat defaultPattern, List<PatternFormat> values) {
        super(name, values);
        this.defaultPattern = defaultPattern;
    }

    @Override
    public PatternFormat toValueObject(Object value) {
        if (value instanceof PatternFormat)
            return (PatternFormat) value;
        final PatternFormat patternFormat = PatternFormat.guessPatternFormat(value.toString());
        return (patternFormat == null) ? defaultPattern : patternFormat;
    }

    public String getSelectedPattern() {
        final PatternFormat selected = getSelected();
        return selected == null ? null : selected.getPattern();
    }
}
