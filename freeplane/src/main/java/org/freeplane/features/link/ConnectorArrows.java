package org.freeplane.features.link;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.RenderedContent;
import org.freeplane.core.ui.components.RenderedContentSupplier;
import org.freeplane.core.util.TextUtils;

public enum ConnectorArrows implements RenderedContentSupplier<ConnectorArrows>{
	 NONE(ArrowType.NONE, ArrowType.NONE, "none", "arrow-mode-none.png"), 
	 FORWARD(ArrowType.NONE, ArrowType.DEFAULT, "forward", "arrow-mode-forward.png"), 
	 BACKWARD(ArrowType.DEFAULT, ArrowType.NONE, "backward", "arrow-mode-backward.png"), 
	 BOTH(ArrowType.DEFAULT, ArrowType.DEFAULT, "both", "arrow-mode-both.png");
	
	public static ConnectorArrows DEFAULT = ConnectorArrows.FORWARD;
	
	public final ArrowType start;
	public final ArrowType end;
	public final String text;
	public final Icon icon;
	
	private RenderedContent<ConnectorArrows> renderedContent;


	
	private ConnectorArrows(ArrowType start, ArrowType end, String description, String iconName) {
		this.start = start;
		this.end = end;
		icon = new ImageIcon(ResourceController.getResourceController().getResource("/images/" + iconName));
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
