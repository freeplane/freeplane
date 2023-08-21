package org.freeplane.plugin.script.proxy;

import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.nodestyle.NodeBorderModel;
import org.freeplane.features.nodestyle.NodeStyleController;
import org.freeplane.features.nodestyle.mindmapmode.MNodeStyleController;
import org.freeplane.features.styles.LogicalStyleController;
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
        return NodeBorderModel.getModel(getDelegate())==null?false:(NodeBorderModel.getModel(getDelegate()).getBorderColor() != null);
    }
}
