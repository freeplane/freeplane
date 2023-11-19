package org.freeplane.main.codeexplorermode;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

import com.tngtech.archunit.core.domain.Dependency;


class EmptyNodeModel extends CodeNodeModel {

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
}