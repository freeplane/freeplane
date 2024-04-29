package org.freeplane.plugin.codeexplorer.map;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.NodeModel;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.properties.HasName;


class EmptyNodeModel extends CodeNode {
    static {
        IconStoreFactory.INSTANCE.createStateIcon(EmptyNodeModel.UI_ICON_NAME, "code/generated.svg");
    }
	static final String UI_ICON_NAME = "code_empty";

    EmptyNodeModel(final CodeMap map, String text) {
		super(map, 0);
        setText(text);
	}
	@Override
	public int getChildCount(){
		return 0;
	}

    @Override
	protected List<NodeModel> getChildrenInternal() {
    	return Collections.emptyList();
	}

    @Override
	public String toString() {
		return getText();
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
    protected Stream<JavaClass> getClasses() {
        return Stream.empty();
    }

    @Override
    String getUIIconName() {
        return UI_ICON_NAME;
    }

    @Override
    HasName getCodeElement() {
        return () -> "";
    }
}
