package org.freeplane.features.icon.factory;

import java.net.URL;

import javax.swing.Icon;

import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.features.icon.UIIcon;

class HeadlessIconFactory implements IconFactory {
	private static final Icon EMPTY_ICON = new EmptyIcon(16, 16);
	
	static final HeadlessIconFactory FACTORY = new HeadlessIconFactory();
	private HeadlessIconFactory() {};

	@Override
	public boolean canScaleIcon(Icon icon) {
		return false;
	}

	@Override
	public Icon getScaledIcon(Icon icon, Quantity<LengthUnit> quantity) {
		return EMPTY_ICON;
	}

	@Override
	public Icon getIcon(URL imageURL) {
		return EMPTY_ICON;
	}

	@Override
	public Icon getIcon(UIIcon icon) {
		return EMPTY_ICON;
	}

	@Override
	public Icon getIcon(UIIcon uiIcon, Quantity<LengthUnit> iconHeight) {
		return EMPTY_ICON;
	}

	@Override
	public Icon getIcon(URL url, Quantity<LengthUnit> defaultUiIconHeight) {
		return EMPTY_ICON;
	}
	
}