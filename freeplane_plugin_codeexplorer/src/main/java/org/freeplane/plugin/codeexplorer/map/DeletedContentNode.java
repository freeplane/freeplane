/*
 * Created on 1 Feb 2024
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.map;

import java.util.stream.Stream;

import org.freeplane.features.icon.factory.IconStoreFactory;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.properties.HasName;

public class DeletedContentNode extends CodeNode{
    static final String UI_DELETED_CONTENT_NODE_ICON_NAME = "deleted_content";
    static {
        IconStoreFactory.INSTANCE.createStateIcon(DeletedContentNode.UI_DELETED_CONTENT_NODE_ICON_NAME, "code/delete.svg");
    }

    public DeletedContentNode(CodeMap map, String idWithoutIndex, int subprojectIndex, String text) {
        super(map, subprojectIndex);
        setIdWithIndex(idWithoutIndex);
        setText(text);
    }

    @Override
    HasName getCodeElement() {
        String id = getID();
        int indexStart = id.lastIndexOf('[');
        String name = indexStart > 0 ? id.substring(0, indexStart) : "";
        return () -> name;
    }

    @Override
    Stream<Dependency> getOutgoingDependencies() {
        return Stream.empty();
    }

    @Override
    Stream<Dependency> getIncomingDependencies() {
        return Stream.empty();
    }

    @Override
    String getUIIconName() {
        return UI_DELETED_CONTENT_NODE_ICON_NAME;
    }

}
