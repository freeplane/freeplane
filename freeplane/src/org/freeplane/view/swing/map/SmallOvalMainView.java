package org.freeplane.view.swing.map;

import org.freeplane.features.nodestyle.NodeStyleModel.Shape;

@SuppressWarnings("serial")
public class SmallOvalMainView extends OvalMainView {

	public SmallOvalMainView() {
		super();
	}
	
	@Override
    public
    Shape getShape() {
		return Shape.small_oval;
	}
	
}
