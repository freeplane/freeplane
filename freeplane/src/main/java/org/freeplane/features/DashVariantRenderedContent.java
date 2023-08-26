/*
 * Created on 26 Aug 2023
 *
 * author dimitry
 */
package org.freeplane.features;

import java.util.EnumMap;

import org.freeplane.core.ui.components.RenderedContent;

public class DashVariantRenderedContent{

    static private final EnumMap<DashVariant, RenderedContent<DashVariant>> renderers = new EnumMap<>(DashVariant.class);
    static public RenderedContent<DashVariant> of(DashVariant content) {
        return renderers.computeIfAbsent(content, key -> new RenderedContent<DashVariant>(key, null, key.icon));
    }


}