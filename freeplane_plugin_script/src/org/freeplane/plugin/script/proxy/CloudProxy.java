package org.freeplane.plugin.script.proxy;

import java.awt.Color;

import org.freeplane.core.util.ColorUtils;
import org.freeplane.features.cloud.CloudController;
import org.freeplane.features.cloud.CloudModel;
import org.freeplane.features.cloud.mindmapmode.MCloudController;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.script.proxy.Proxy.Cloud;

public class CloudProxy implements Cloud {
    private final NodeModel node;

    public CloudProxy(NodeProxy nodeProxy) {
        this.node = nodeProxy.getDelegate();
    }

    @Override
    public boolean getEnabled() {
        return getCloudModel() != null;
    }

    @Override
    public void setEnabled(boolean enable) {
        getCloudController().setCloud(node, enable);
    }

    @Override
    public String getShape() {
        final CloudModel cloudModel = getCloudModel();
        return cloudModel == null ? null : cloudModel.getShape().name();
    }

    @Override
    public void setShape(String shape) {
        if (!handleArgumentIfNull(shape)) {
            getCloudController().setShape(node, CloudModel.Shape.valueOf(shape));
        }
    }

    @Override
    public Color getColor() {
        final CloudModel cloudModel = getCloudModel();
        return cloudModel == null ? null : cloudModel.getColor();
    }

    @Override
    public void setColor(Color color) {
        if (!handleArgumentIfNull(color)) {
            getCloudController().setColor(node, color);
        }
    }

    @Override
    public String getColorCode() {
        final Color color = getColor();
        return color == null ? null : ColorUtils.colorToString(color);
    }

    @Override
    public void setColorCode(String rgbString) {
        setColor(ColorUtils.stringToColor(rgbString));
    }

    private CloudModel getCloudModel() {
        return CloudModel.getModel(node);
    }

    private MCloudController getCloudController() {
        return (MCloudController) CloudController.getController();
    }

    private boolean handleArgumentIfNull(Object arg) {
        if (arg == null) {
            if (getEnabled())
                setEnabled(false);
            return true;
        }
        return false;
    }
}
