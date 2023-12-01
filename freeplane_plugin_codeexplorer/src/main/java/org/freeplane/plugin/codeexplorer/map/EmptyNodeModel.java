package org.freeplane.plugin.codeexplorer.map;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.properties.HasName;


class EmptyNodeModel extends CodeNode {
    static {
        IconStoreFactory.INSTANCE.createStateIcon(EmptyNodeModel.UI_ICON_NAME, "code/generated.svg");
    }
	static final String UI_ICON_NAME = "code_empty";

    EmptyNodeModel(final MapModel map, String text) {
		super(map);
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
	public boolean hasChildren() {
    	return false;
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
    String getUIIconName() {
        return UI_ICON_NAME;
    }
    @Override
    protected boolean initializeChildNodes() {
        return false;
    }
    @Override
    HasName getCodeElement() {
        return () -> "";
    }
}
