/*
 * Created on 26 Aug 2023
 *
 * author dimitry
 */
package org.freeplane.features.link;

import java.util.EnumMap;

import org.freeplane.core.ui.components.RenderedContent;
import org.freeplane.core.util.TextUtils;

public class ConnectorShapeRenderedContent {
    static private final EnumMap<ConnectorShape, RenderedContent<ConnectorShape>> renderers = new EnumMap<>(ConnectorShape.class);
    static public RenderedContent<ConnectorShape> of(ConnectorShape content) {
        return renderers.computeIfAbsent(content, key -> new RenderedContent<ConnectorShape>(key, TextUtils.getText("ChangeConnectorShapeAction." + key.name() + ".text"), null));
    }
}