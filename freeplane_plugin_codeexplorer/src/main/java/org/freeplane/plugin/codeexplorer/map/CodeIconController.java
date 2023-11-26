/*
 * Created on 20 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.map;

import java.util.Collection;

import org.freeplane.features.icon.IconController;
import org.freeplane.features.icon.NamedIcon;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.LogicalStyleController.StyleOption;

public class CodeIconController extends IconController{
    static {
        IconStoreFactory.INSTANCE.createStateIcon(EmptyNodeModel.UI_ICON_NAME, "code/generated.svg");
        IconStoreFactory.INSTANCE.createStateIcon(PackageNodeModel.UI_ICON_NAME, "code/folder.svg");
        IconStoreFactory.INSTANCE.createStateIcon(ClassesNodeModel.UI_CHILD_PACKAGE_ICON_NAME, "code/childPackage.svg");
        IconStoreFactory.INSTANCE.createStateIcon(ClassesNodeModel.UI_SAME_PACKAGE_ICON_NAME, "code/samePackage.svg");
        IconStoreFactory.INSTANCE.createStateIcon(ClassNodeModel.INTERFACE_ICON_NAME, "code/interface.svg");
        IconStoreFactory.INSTANCE.createStateIcon(ClassNodeModel.ABSTRACT_CLASS_ICON_NAME, "code/classAbstract.svg");
        IconStoreFactory.INSTANCE.createStateIcon(ClassNodeModel.CLASS_ICON_NAME, "code/class.svg");
        IconStoreFactory.INSTANCE.createStateIcon(ClassNodeModel.ENUM_ICON_NAME, "code/enum.svg");
        IconStoreFactory.INSTANCE.createStateIcon(ClassNodeModel.ANNOTATION_ICON_NAME, "code/annotation.svg");
    }

    public CodeIconController(ModeController modeController) {
        super(modeController);
    }

    @Override
    public Collection<NamedIcon> getIcons(NodeModel node, StyleOption option) {
        return node.getIcons();
    }

}
