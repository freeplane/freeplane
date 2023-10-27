package org.freeplane.plugin.script.proxy;

import org.freeplane.api.Dash;
import org.freeplane.api.LengthUnit;
import org.freeplane.api.Quantity;
import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.nodestyle.NodeBorderModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;
import org.freeplane.features.styles.LogicalStyleController;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;
import org.freeplane.plugin.script.ScriptContext;

import java.awt.Color;

public class BorderProxy extends AbstractProxy<NodeModel> implements Proxy.Border {
    BorderProxy(NodeModel delegate, ScriptContext scriptContext) {
        super(delegate, scriptContext);
    }

    private MNodeStyleController getStyleController() {
        return (MNodeStyleController) NodeStyleController.getController();
    }

    @Override
    public void setColor(Color color) {
        getStyleController().setBorderColor(getDelegate(), color);
    }

    @Override
    public void setColorCode(String rgbString) {
        setColor(ColorUtils.stringToColor(rgbString));
    }

    @Override
    public Color getColor() {
        return getStyleController().getBorderColor(getDelegate(), LogicalStyleController.StyleOption.FOR_UNSELECTED_NODE);
    }

    @Override
    public String getColorCode() {
        return ColorUtils.colorToString(getColor());
    }

    @Override
    public boolean isColorSet() {
        NodeBorderModel border = NodeBorderModel.getModel(getDelegate());
        return border!=null && border.getBorderColor() != null;
    }

    @Override
    public Quantity<LengthUnit> getWidth() {
        return getStyleController().getBorderWidth(getDelegate(), LogicalStyleController.StyleOption.FOR_UNSELECTED_NODE);
    }


	@Override
	public boolean isWidthSet() {
		NodeBorderModel border = NodeBorderModel.getModel(getDelegate());
		return border!=null && border.getBorderDash() != null;
	}

    @Override
    public void setWidth(Quantity<LengthUnit> borderWidth) {
        getStyleController().setBorderWidth(getDelegate(), borderWidth);
    }

	@Override
	public Dash getDash() {
		return getStyleController().getBorderDash(getDelegate(), StyleOption.FOR_UNSELECTED_NODE);
	}

	@Override
	public boolean isDashSet() {
		NodeBorderModel border = NodeBorderModel.getModel(getDelegate());
		return border!=null && border.getBorderDash() != null;
	}

	@Override
	public void setDash(Dash dash) {
		getStyleController().setBorderDash(getDelegate(), dash);
	}
}
