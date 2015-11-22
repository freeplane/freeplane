package org.freeplane.view.swing.map;

import org.freeplane.features.nodestyle.NodeStyleModel.Shape;

@SuppressWarnings("serial")
public class BigOvalMainView extends OvalMainView {
	public BigOvalMainView() {
		super();
	}
	
	@Override
    public
    Shape getShape() {
		return Shape.big_oval;
	}
}
