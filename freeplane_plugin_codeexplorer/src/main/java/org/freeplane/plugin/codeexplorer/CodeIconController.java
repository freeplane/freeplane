/*
 * Created on 20 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer;

import java.util.Collection;

import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.NamedIcon;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;

class CodeIconController extends IconController{

    CodeIconController(ModeController modeController) {
        super(modeController);
    }

    @Override
    public Collection<NamedIcon> getIcons(NodeModel node, StyleOption option) {
        return node.getIcons();
    }

}
