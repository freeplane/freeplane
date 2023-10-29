/*
 * Created on 26 Aug 2023
 *
 * author dimitry
 */
package org.freeplane.features.link;

import java.util.EnumMap;

import org.freeplane.core.ui.components.RenderedContent;

public class ConnectorArrowsRenderedContent {
    static private final EnumMap<ConnectorArrows, RenderedContent<ConnectorArrows>> renderers = new EnumMap<>(ConnectorArrows.class);
    static public RenderedContent<ConnectorArrows>  of(ConnectorArrows content) {
        return renderers.computeIfAbsent(content, key -> new RenderedContent<ConnectorArrows>(key, key.text, key.icon));
    }
}