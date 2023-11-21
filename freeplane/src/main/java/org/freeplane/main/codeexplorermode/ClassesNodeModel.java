package org.freeplane.main.codeexplorermode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.domain.properties.HasName;


class ClassesNodeModel extends CodeNodeModel {
	final private JavaPackage javaPackage;
    static final String UI_CHILD_PACKAGE_ICON_NAME = "code_classes";
    static final String UI_SAME_PACKAGE_ICON_NAME = "code_same_package_classes";
    private final boolean samePackage;

	public ClassesNodeModel(final JavaPackage javaPackage, final MapModel map, boolean samePackage, int subgroupIndex) {
		super(map, subgroupIndex);
		this.javaPackage = javaPackage;
        this.samePackage = samePackage;
		setFolded(! javaPackage.getClasses().isEmpty());
		setID(javaPackage.getName() + ".package");
        long classCount = javaPackage.getClasses().stream()
                .filter(jc -> isNamed(jc))
                .count();
		String text = samePackage ? "package" : javaPackage.getRelativeName();
        setText(text + formatClassCount(classCount));
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
                            .map(CodeNodeModel::getTargetNodeClass)
                            .filter(jc -> ! jc.equals(edgeStart) && jc.getPackage().equals(javaPackage))
                            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

                    dependencies.entrySet().stream()
                    .forEach(e -> nodeSort.addEdge(edgeStart, e.getKey(), e.getValue()));
                }
	            Map<JavaClass, ClassNodeModel> nodes = new HashMap<>();
                List<List<JavaClass>> orderedClasses = nodeSort.sortNodes();
                for(int subgroupIndex = 0; subgroupIndex < orderedClasses.size(); subgroupIndex++) {
                    for (JavaClass childClass : orderedClasses.get(subgroupIndex)) {
                        final ClassNodeModel node = new ClassNodeModel(childClass, getMap(), subgroupIndex);
                        nodes.put(childClass, node);
                        children.add(node);
                        node.setParent(this);
                    }
                }
	            for (JavaClass javaClass : classes) {
	                JavaClass enclosingClass = findEnclosingNamedClass(javaClass);
	                ClassNodeModel node = nodes.get(enclosingClass);
	                node.registerInnerClass(javaClass);
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
    Set<JavaClass> getClassesInPackageTree() {
        return javaPackage.getClassesInPackageTree();
    }

    @Override
    Stream<Dependency> getOutgoingDependencies() {
        return javaPackage.getClassDependenciesFromThisPackage().stream();
    }

    @Override
    Stream<Dependency> getIncomingDependencies() {
        return javaPackage.getClassDependenciesToThisPackage().stream();
    }


    @Override
    String getUIIconName() {
        return samePackage
                ? UI_SAME_PACKAGE_ICON_NAME
                        : UI_CHILD_PACKAGE_ICON_NAME;
    }

    @Override
    HasName getElementInScope(JavaClass dependencyClass) {
        return dependencyClass.getPackage();
    }
}
