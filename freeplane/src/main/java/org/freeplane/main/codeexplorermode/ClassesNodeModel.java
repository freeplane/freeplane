package org.freeplane.main.codeexplorermode;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaPackage;


class ClassesNodeModel extends CodeNodeModel {
	final private JavaPackage javaPackage;

	public ClassesNodeModel(final JavaPackage javaPackage, final MapModel map) {
		super(map);
		this.javaPackage = javaPackage;
		setFolded(! javaPackage.getClasses().isEmpty());
		setID(javaPackage.getName() + ".package");
		setText(javaPackage.getRelativeName() + " classes");
	}

	@Override
	public List<NodeModel> getChildren() {
		initializeChildNodes();
		return super.getChildren();
	}

	private void initializeChildNodes() {
	    List<NodeModel> children = super.getChildrenInternal();
	    if (children.isEmpty()) {
	        final Set<JavaClass> classes = javaPackage.getClasses();
	        if(! classes.isEmpty()) {
	            GraphNodeSort<JavaClass> nodeSort = new GraphNodeSort<JavaClass>();
	            for (JavaClass javaClass : classes) {
	                JavaClass edgeStart = findEnclosingNamedClass(javaClass);
                    nodeSort.addNode(edgeStart);
	                Map<JavaClass, Long> dependencies = javaClass.getDirectDependenciesFromSelf().stream()
	                        .map(this::getTargetClass)
	                        .filter(jc -> ! jc.equals(edgeStart) && jc.getPackage().equals(javaPackage))
	                        .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

	                dependencies.entrySet().stream()
	                .forEach(e -> nodeSort.addEdge(edgeStart, e.getKey(), e.getValue()));
	            }

	            List<JavaClass> orderedClasses = nodeSort.sortNodes();
                for (JavaClass childClass : orderedClasses) {
                    final ClassNodeModel node = new ClassNodeModel(childClass, getMap());
                    children.add(node);
                    node.setParent(this);
                }
            }
	    }
	}

	@Override
	public int getChildCount(){
		return super.getChildCount();
	}



    @Override
	protected List<NodeModel> getChildrenInternal() {
    	initializeChildNodes();
    	return super.getChildrenInternal();
	}

	@Override
	public boolean hasChildren() {
    	return ! javaPackage.getClasses().isEmpty();
	}


    @Override
	public String toString() {
		return getText();
	}

    @Override
    Set<Dependency> getOutgoingDependencyCandidates(boolean includesDependenciesForChildPackages) {
        Set<Dependency> dependencies = includesDependenciesForChildPackages
                ? javaPackage.getClassDependenciesFromThisPackage()
                        : Collections.emptySet();
        return dependencies;
    }

    @Override
    Set<Dependency> getIncomingDependencyCandidates(boolean includesDependenciesForChildPackages) {
        return includesDependenciesForChildPackages
                ? javaPackage.getClassDependenciesToThisPackage()
                        : Collections.emptySet();
    }

    private JavaClass getTargetClass(Dependency dependency) {
        return findEnclosingNamedClass(dependency.getTargetClass());
    }
}