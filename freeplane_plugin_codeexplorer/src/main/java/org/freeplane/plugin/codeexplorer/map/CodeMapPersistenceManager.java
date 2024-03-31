/*
 * Created on 1 Feb 2024
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.map;

import java.util.Collection;

import org.freeplane.features.attribute.AttributeRegistry;
import org.freeplane.features.attribute.AttributeTableLayoutModel;
import org.freeplane.features.attribute.NodeAttributeTableModel;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.map.NodeStream;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.url.UrlManager;
import org.freeplane.plugin.codeexplorer.task.UserDefinedCodeExplorerConfiguration;
import org.freeplane.plugin.codeexplorer.task.CodeNodeUserContent;
import org.freeplane.plugin.codeexplorer.task.CodeExplorerConfiguration;

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
        CodeExplorerConfiguration codeExplorerConfiguration = codemap.getConfiguration();
        if(! (codeExplorerConfiguration instanceof UserDefinedCodeExplorerConfiguration) )
            return true;
        final UserDefinedCodeExplorerConfiguration userDefinedCodeExplorerConfiguration = (UserDefinedCodeExplorerConfiguration) codeExplorerConfiguration;
        userDefinedCodeExplorerConfiguration.removeUserContent();
        NodeStream.of(map.getRootNode())
        .map(CodeNode.class::cast)
        .filter(CodeNodeUserContent.Factory.INSTANCE::hasCustomContent)
        .forEach(node -> userDefinedCodeExplorerConfiguration.addUserContent(
                codemap.groupIdByIndex(node.groupIndex),
                CodeNodeUserContent.Factory.INSTANCE.contentOf(node)));
        userDefinedCodeExplorerConfiguration.getAttributeConfiguration()
            .setAttributeViewType(map.getExtension(AttributeRegistry.class).getAttributeViewType());
        return true;
    }

    public void restoreUserContent(CodeMap map) {
        CodeExplorerConfiguration codeExplorerConfiguration = map.getConfiguration();
        if(codeExplorerConfiguration instanceof UserDefinedCodeExplorerConfiguration ) {
            final UserDefinedCodeExplorerConfiguration configuration = (UserDefinedCodeExplorerConfiguration) codeExplorerConfiguration;
            configuration
            .getUserContent()
            .entrySet().stream()
            .forEach(content -> addToMap(map, content.getKey(), content.getValue()));
            final String attributeViewType = configuration.getAttributeConfiguration().getAttributeViewType();
            AttributeRegistry.getRegistry(map).setAttributeViewType(attributeViewType);

        } else {
            AttributeRegistry.getRegistry(map).setAttributeViewType(AttributeTableLayoutModel.HIDE_ALL);
        }
        map.setSaved(true);
    }

    private void addToMap(CodeMap map, String groupId, Collection<CodeNodeUserContent> contentCollection) {
        final int groupIndex = map.groupIndexOf(groupId);
        contentCollection.forEach(content -> addToMap(map, groupIndex, content));
     }

    private void addToMap(CodeMap map, int groupIndex, CodeNodeUserContent content) {
        CodeNode node = deletedContentNode(map, groupIndex, content.getNodeIdWithoutGroupIndex());
        content.getDetails()
        .map(CodeNodeUserContent.Factory.INSTANCE::fromCodeNodeDetails)
        .ifPresent(node::addExtension);

        addAttributes(node, content);
    }

    private CodeNode deletedContentNode(CodeMap map, int groupIndex,
            final String nodeIdWithoutGroupIndex) {
        String nodeId = CodeNode.idWithGroupIndex(nodeIdWithoutGroupIndex, groupIndex);
        CodeNode node = (CodeNode) map.getNodeForID(nodeId);
        if(node != null)
            return node;

        final NodeModel parentNode;
        final String deletedNodeText;
        final int packageSeparatorPosition =nodeIdWithoutGroupIndex.lastIndexOf('.',
                nodeIdWithoutGroupIndex.length() - 1 - (nodeIdWithoutGroupIndex.endsWith(ClassesNode.NODE_ID_SUFFIX)? ClassesNode.NODE_ID_SUFFIX.length() : 0));
        if(packageSeparatorPosition == -1) {
            parentNode = map.getRootNode().getChildAt(groupIndex);
            deletedNodeText = nodeIdWithoutGroupIndex;
        }
        else {
            final String parentIdWithoutGroupIndex = nodeIdWithoutGroupIndex.substring(0, packageSeparatorPosition);
            parentNode = deletedContentNode(map, groupIndex, parentIdWithoutGroupIndex);
            if(nodeIdWithoutGroupIndex.endsWith(ClassesNode.NODE_ID_SUFFIX)) {
                deletedNodeText = nodeIdWithoutGroupIndex.substring(packageSeparatorPosition + 1, nodeIdWithoutGroupIndex.length() - ClassesNode.NODE_ID_SUFFIX.length()) + " package";
            } else
                deletedNodeText = nodeIdWithoutGroupIndex.substring(packageSeparatorPosition + 1, nodeIdWithoutGroupIndex.length());
        }
        final DeletedContentNode deletedContentNode = new DeletedContentNode(map, nodeIdWithoutGroupIndex, groupIndex, deletedNodeText);
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
