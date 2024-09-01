/*
 * Created on 26 Aug 2023
 *
 * author dimitry
 */
package org.freeplane.features;

import java.util.EnumMap;

import org.freeplane.api.Dash;
import org.freeplane.core.ui.components.RenderedContent;

public class DashRenderedContent{

    static private final EnumMap<Dash, RenderedContent<Dash>> renderers = new EnumMap<>(Dash.class);
    static public RenderedContent<Dash> of(Dash content) {
        return renderers.computeIfAbsent(content, key -> new RenderedContent<Dash>(key, null, DashIconFactory.iconFor(key)));
    }


}