/**
 * 
 */
package org.freeplane.plugin.script.proxy;

import org.freeplane.core.model.NodeModel;
import org.freeplane.features.common.nodestyle.NodeStyleController;
import org.freeplane.features.common.nodestyle.NodeStyleModel;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.features.mindmapmode.nodestyle.MNodeStyleController;

class FontProxy extends AbstractProxy implements Proxy.Font {
	FontProxy(final NodeModel delegate, final MModeController modeController) {
		super(delegate, modeController);
	}

	public String getName() {
		return getStyleController().getFontFamilyName(getNode());
	}

	public int getSize() {
		return getStyleController().getFontSize(getNode());
	}

	private MNodeStyleController getStyleController() {
		return (MNodeStyleController) NodeStyleController
				.getController(getModeController());
	}

	public boolean isBold() {
		return getStyleController().isBold(getNode());
	}

	public boolean isBoldSet() {
		return NodeStyleModel.isBold(getNode()) != null;
	}

	public boolean isItalic() {
		return getStyleController().isItalic(getNode());
	}

	public boolean isItalicSet() {
		return NodeStyleModel.isItalic(getNode()) != null;
	}

	public boolean isNameSet() {
		return NodeStyleModel.getFontFamilyName(getNode()) != null;
	}

	public boolean isSizeSet() {
		return NodeStyleModel.getFontSize(getNode()) != null;
	}

	public void resetBold() {
		getStyleController().setBold(getNode(), null);

	}

	public void resetItalic() {
		getStyleController().setItalic(getNode(), null);

	}

	public void resetName() {
		getStyleController().setFontFamily(getNode(), null);

	}

	public void resetSize() {
		getStyleController().setFontSize(getNode(), null);

	}

	public void setBold(final boolean bold) {
		getStyleController().setBold(getNode(), bold);

	}

	public void setItalic(final boolean italic) {
		getStyleController().setItalic(getNode(), italic);

	}

	public void setName(final String name) {
		getStyleController().setFontFamily(getNode(), name);
	}

	public void setSize(final int size) {
		getStyleController().setFontSize(getNode(), size);

	}
}