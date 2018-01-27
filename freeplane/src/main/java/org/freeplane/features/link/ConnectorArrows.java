package org.freeplane.features.link;

import java.net.URL;

import javax.swing.Icon;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.RenderedContent;
import org.freeplane.core.ui.components.RenderedContentSupplier;
import org.freeplane.core.ui.svgicons.FreeplaneIconFactory;
import org.freeplane.core.util.TextUtils;

public enum ConnectorArrows implements RenderedContentSupplier<ConnectorArrows>{
	 NONE(ArrowType.NONE, ArrowType.NONE, "none", "arrow-mode-none.svg"), 
	 FORWARD(ArrowType.NONE, ArrowType.DEFAULT, "forward", "arrow-mode-forward.svg"), 
	 BACKWARD(ArrowType.DEFAULT, ArrowType.NONE, "backward", "arrow-mode-backward.svg"), 
	 BOTH(ArrowType.DEFAULT, ArrowType.DEFAULT, "both", "arrow-mode-both.svg");
	
	public static ConnectorArrows DEFAULT = ConnectorArrows.FORWARD;
	
	public final ArrowType start;
	public final ArrowType end;
	public final String text;
	public final Icon icon;
	
	private RenderedContent<ConnectorArrows> renderedContent;


	
	private ConnectorArrows(ArrowType start, ArrowType end, String description, String iconName) {
		this.start = start;
		this.end = end;
		final URL url = ResourceController.getResourceController().getResource("/images/" + iconName);
		icon = url != null ? FreeplaneIconFactory.createSVGIcon(url) : null;
		text = TextUtils.getText("ChangeConnectorArrowsAction." + description + ".text");
	}

	
	@Override
	public RenderedContent<ConnectorArrows> createRenderedContent() {
		if(renderedContent == null) {
			renderedContent = new RenderedContent<ConnectorArrows>(this, text, icon);
		}
		return renderedContent;
	}
}
