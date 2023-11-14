package org.freeplane.main.codeexplorermode;

import java.util.List;
import java.util.Set;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;


class ClassNodeModel extends CodeNodeModel {
	final private JavaClass javaClass;

	public ClassNodeModel(final JavaClass javaClass, final MapModel map) {
		super(map);
		this.javaClass = javaClass;
		setFolded(false);
		setID(javaClass.getName());
		String simpleName = javaClass.getSimpleName();
        setText(simpleName);
	}

	@Override
	public List<NodeModel> getChildren() {
		initializeChildNodes();
		return super.getChildren();
	}

	private void initializeChildNodes() {/**/}

	@Override
	public int getChildCount(){
		return 0;
	}



    @Override
	protected List<NodeModel> getChildrenInternal() {
    	initializeChildNodes();
    	return super.getChildrenInternal();
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
    Set<Dependency> getOutgoingDependencyCandidates(boolean includesDependenciesForChildPackages) {
        return javaClass.getDirectDependenciesFromSelf();
    }

    @Override
    Set<Dependency> getIncomingDependencyCandidates(boolean includesDependenciesForChildPackages) {
        return javaClass.getDirectDependenciesToSelf();
    }
}
