package org.freeplane.features.format;

import java.util.Locale;

import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;

public final class FormatUtils {
    private static final String PROPERTY_FORMAT_LOCALE = "format_locale";

    static Locale getFormatLocaleFromResources() {
        final String formatLoc = ResourceController.getResourceController().getProperty(PROPERTY_FORMAT_LOCALE);
        return formatLoc.equals(ResourceBundles.LANGUAGE_AUTOMATIC) ? Locale.getDefault() : new Locale(formatLoc);
    }
}
