package org.freeplane.features.link;

import org.freeplane.core.ui.components.RenderedContent;
import org.freeplane.core.ui.components.RenderedContentSupplier;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.link.ConnectorShape;

public enum ConnectorShape implements RenderedContentSupplier<ConnectorShape>{
	LINE, LINEAR_PATH, CUBIC_CURVE, EDGE_LIKE;

    private final RenderedContent<ConnectorShape> renderedContent;
    
    private ConnectorShape() {
        String text = TextUtils.getText("ChangeConnectorShapeAction." + name() + ".text");
        this.renderedContent = new RenderedContent<ConnectorShape>(this, text, null);
    }

    @Override
    public RenderedContent<ConnectorShape> createRenderedContent() {
        return renderedContent;
    }
}