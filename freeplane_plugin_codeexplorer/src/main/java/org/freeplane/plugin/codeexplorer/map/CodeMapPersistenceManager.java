/*
 * Created on 1 Feb 2024
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.map;

import java.util.Collection;

import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeStream;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.url.UrlManager;
import org.freeplane.plugin.codeexplorer.task.CodeExplorerConfiguration;
import org.freeplane.plugin.codeexplorer.task.CodeNodeUserContent;

public class CodeMapPersistenceManager extends UrlManager {
    public static CodeMapPersistenceManager getCodeMapPersistenceManager(ModeController modeController) {
        return (CodeMapPersistenceManager) modeController.getExtension(UrlManager.class);
    }

    public CodeMapPersistenceManager() {
        super();
        Controller.getCurrentModeController().addAction(new SaveAction());
    }

    @Override
    public boolean save(MapModel map) {
        if(! (map instanceof CodeMap))
            return false;
        map.setSaved(true);
        CodeMap codemap = (CodeMap)map;
        CodeExplorerConfiguration configuration = codemap.getConfiguration();
        configuration.removeUserContent();
        NodeStream.of(map.getRootNode())
        .map(CodeNode.class::cast)
        .filter(CodeNodeUserContent.Factory.INSTANCE::hasCustomContent)
        .forEach(node -> configuration.addUserContent(codemap.locationByIndex(node.subprojectIndex), CodeNodeUserContent.Factory.INSTANCE.contentOf(node)));
        configuration.getAttributeConfiguration()
            .setAttributeViewType(map.getExtension(AttributeRegistry.class).getAttributeViewType());
        return true;
    }

    public void restoreUserContent(CodeMap map) {
        CodeExplorerConfiguration configuration = map.getConfiguration();
        configuration
        .getUserContent()
        .entrySet().stream()
        .forEach(content -> addToMap(map, content.getKey(), content.getValue()));
        map.getExtension(AttributeRegistry.class).setAttributeViewType(
                configuration.getAttributeConfiguration().getAttributeViewType());

        map.setSaved(true);
    }

    private void addToMap(CodeMap map, String location, Collection<CodeNodeUserContent> contentCollection) {
        final int subprojectIndex = map.subprojectIndexOf(location);
        contentCollection.forEach(content -> addToMap(map, subprojectIndex, content));
     }

    private void addToMap(CodeMap map, int subprojectIndex, CodeNodeUserContent content) {
        CodeNode node = deletedContentNode(map, subprojectIndex, content.getNodeIdWithoutProjectIndex());
        content.getDetails()
        .map(CodeNodeUserContent.Factory.INSTANCE::fromCodeNodeDetails)
        .ifPresent(node::addExtension);

        addAttributes(node, content);
    }

    private CodeNode deletedContentNode(CodeMap map, int subprojectIndex,
            final String nodeIdWithoutProjectIndex) {
        String nodeId = CodeNode.idWithSubprojectIndex(nodeIdWithoutProjectIndex, subprojectIndex);
        CodeNode node = (CodeNode) map.getNodeForID(nodeId);
        if(node != null)
            return node;

        final NodeModel parentNode;
        final String deletedNodeText;
        final int packageSeparatorPosition =nodeIdWithoutProjectIndex.lastIndexOf('.',
                nodeIdWithoutProjectIndex.length() - 1 - (nodeIdWithoutProjectIndex.endsWith(ClassesNode.NODE_ID_SUFFIX)? ClassesNode.NODE_ID_SUFFIX.length() : 0));
        if(packageSeparatorPosition == -1) {
            parentNode = map.getRootNode().getChildAt(subprojectIndex);
            deletedNodeText = nodeIdWithoutProjectIndex;
        }
        else {
            final String parentIdWithoutProjectIndex = nodeIdWithoutProjectIndex.substring(0, packageSeparatorPosition);
            parentNode = deletedContentNode(map, subprojectIndex, parentIdWithoutProjectIndex);
            if(nodeIdWithoutProjectIndex.endsWith(ClassesNode.NODE_ID_SUFFIX)) {
                deletedNodeText = nodeIdWithoutProjectIndex.substring(packageSeparatorPosition + 1, nodeIdWithoutProjectIndex.length() - ClassesNode.NODE_ID_SUFFIX.length()) + " package";
            } else
                deletedNodeText = nodeIdWithoutProjectIndex.substring(packageSeparatorPosition + 1, nodeIdWithoutProjectIndex.length());
        }
        final DeletedContentNode deletedContentNode = new DeletedContentNode(map, nodeIdWithoutProjectIndex, subprojectIndex, deletedNodeText);
        parentNode.insert(deletedContentNode);
        return deletedContentNode;
    }

    private void addAttributes(CodeNode node, CodeNodeUserContent content) {
        content.getAttributes().stream()
        .map(CodeNodeUserContent.Factory.INSTANCE::fromCodeNodeAttribute)
        .forEach(attribute -> {
            NodeAttributeTableModel attributeModel = node
                    .getExtension(NodeAttributeTableModel.class);
            if (attributeModel == null) {
                attributeModel = new NodeAttributeTableModel();
                node.addExtension(attributeModel);
            }
            attributeModel.silentlyAddRowNoUndo(node, attribute);
        });
    }
}
