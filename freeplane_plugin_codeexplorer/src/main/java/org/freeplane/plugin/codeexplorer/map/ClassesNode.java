package org.freeplane.plugin.codeexplorer.map;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.codeexplorer.graph.GraphCycleFinder;
import org.freeplane.plugin.codeexplorer.graph.GraphNodeSort;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.domain.properties.HasName;


class ClassesNode extends CodeNode {
    static {
        IconStoreFactory.INSTANCE.createStateIcon(ClassesNode.UI_CHILD_PACKAGE_ICON_NAME, "code/childPackage.svg");
        IconStoreFactory.INSTANCE.createStateIcon(ClassesNode.UI_SAME_PACKAGE_ICON_NAME, "code/samePackage.svg");
    }	final private JavaPackage javaPackage;
    static final String UI_CHILD_PACKAGE_ICON_NAME = "code_classes";
    static final String UI_SAME_PACKAGE_ICON_NAME = "code_same_package_classes";
    private final boolean samePackage;

	public ClassesNode(final JavaPackage javaPackage, final CodeMap map, String name, boolean samePackage, int subprojectIndex) {
		super(map, subprojectIndex);
		this.javaPackage = javaPackage;
        this.samePackage = samePackage;
		setFolded(! javaPackage.getClasses().isEmpty());
		setIdWithIndex(javaPackage.getName() + ".package");
        long classCount = getClasses()
                .filter(jc -> isNamed(jc))
                .count();
        setText(name + formatClassCount(classCount));
	}

    private Stream<JavaClass> getClasses() {
        return javaPackage.getClasses().stream()
                .filter(this::belongsToSameSubproject);
    }

	@Override
	public List<NodeModel> getChildren() {
		initializeChildNodes();
		return super.getChildren();
	}

    @Override
    HasName getCodeElement() {
        return javaPackage;
    }

	@Override
    protected boolean initializeChildNodes() {
	    List<NodeModel> children = super.getChildrenInternal();
	    if (children.isEmpty()) {
	        final List<JavaClass> classes = getClasses()
	                .collect(Collectors.toList());
	        if(! classes.isEmpty()) {
	            GraphNodeSort<JavaClass> nodeSort = new GraphNodeSort<JavaClass>();
                for (JavaClass javaClass : classes) {
                    JavaClass edgeStart = findEnclosingNamedClass(javaClass);
                    nodeSort.addNode(edgeStart);
                    DistinctTargetDependencyFilter filter = new DistinctTargetDependencyFilter();
                    Map<JavaClass, Long> dependencies = javaClass.getDirectDependenciesFromSelf().stream()
                            .filter(dep -> goesOutsideEnclosingOriginClass(edgeStart, dep))
                            .map(filter::knownDependency)
                            .filter(CodeNode::classesBelongToTheSamePackage)
                            .filter(dep -> belongsToSameSubproject(dep.getTargetClass()))
                            .collect(Collectors.groupingBy(CodeNode::getTargetNodeClass, Collectors.counting()));
                    dependencies.entrySet().stream()
                    .forEach(e -> nodeSort.addEdge(edgeStart, e.getKey(), e.getValue()));
                }
	            Map<JavaClass, ClassNode> nodes = new HashMap<>();
                List<List<JavaClass>> orderedClasses = nodeSort.sortNodes();
                for(int subgroupIndex = 0; subgroupIndex < orderedClasses.size(); subgroupIndex++) {
                    for (JavaClass childClass : orderedClasses.get(subgroupIndex)) {
                        final ClassNode node = new ClassNode(childClass, getMap(), subprojectIndex);
                        nodes.put(childClass, node);
                        children.add(node);
                        node.setParent(this);
                    }
                }
	            for (JavaClass javaClass : classes) {
	                JavaClass enclosingClass = findEnclosingNamedClass(javaClass);
	                ClassNode node = nodes.get(enclosingClass);
	                node.registerInnerClass(javaClass);
	            }

            }
	    }
	    return false;
	}

    private boolean goesOutsideEnclosingOriginClass(JavaClass edgeStart, Dependency dependency) {
        JavaClass jc = getTargetNodeClass(dependency);
        return ! jc.equals(edgeStart);
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
    Stream<Dependency> getOutgoingDependencies() {
        return getClasses()
                .flatMap(c -> c.getDirectDependenciesFromSelf().stream())
                .filter(dep -> ! dep.getTargetClass().getPackage().equals(dep.getOriginClass().getPackage())
                        || subprojectIndexOf(dep.getTargetClass()) != subprojectIndex);
    }

    @Override
    Stream<Dependency> getIncomingDependencies() {
        return getClasses()
                .flatMap(c -> c.getDirectDependenciesToSelf().stream())
                .filter(dep -> ! dep.getTargetClass().getPackage().equals(dep.getOriginClass().getPackage())
                        || subprojectIndexOf(dep.getOriginClass()) != subprojectIndex);
    }


    @Override
    String getUIIconName() {
        return samePackage
                ? UI_SAME_PACKAGE_ICON_NAME
                        : UI_CHILD_PACKAGE_ICON_NAME;
    }


    @Override
    Set<CodeNode> findCyclicDependencies() {
        GraphCycleFinder<ClassesNode> cycleFinder = new GraphCycleFinder<ClassesNode>();
        cycleFinder.addNode(this);
        cycleFinder.stopSearchHere();
        cycleFinder.exploreGraph(Collections.singleton(this),
                this::connectedTargetNodesInTheSameScope,
                this::connectedOriginNodesInTheSameScope);
        List<List<ClassesNode>> cycles = cycleFinder.findSimpleCycles();
        Map<JavaPackage, Set<JavaPackage>> origins = new HashMap<>();
        Map<JavaPackage, Set<JavaPackage>> targets = new HashMap<>();
        for(List<ClassesNode> cycle : cycles) {
            for(int n = 0; n < cycle.size(); n++) {
                JavaPackage origin = cycle.get(n).javaPackage;
                JavaPackage target = cycle.get((n+1) % cycle.size()).javaPackage;
                origins.computeIfAbsent(target, x -> new HashSet<>()).add(origin);
                targets.computeIfAbsent(origin, x -> new HashSet<>()).add(target);
            }
        }
        return cycles.stream().flatMap(List::stream)
                .distinct()
                .flatMap(packageNode ->
                    Stream.concat(
                            packageNode.getOutgoingDependenciesWithKnownTargets()
                            .map(Dependency::getTargetClass)
                            .filter(targetClass -> targets.get(packageNode.javaPackage).contains(targetClass.getPackage())),
                            packageNode.getIncomingDependenciesWithKnownTargets()
                            .map(Dependency::getOriginClass)
                            .filter(originClass -> origins.get(packageNode.javaPackage).contains(originClass.getPackage()))
                            )
                )
                .map(CodeNode::findEnclosingNamedClass)
                .map(JavaClass::getName)
                .map(this::idWithSubprojectIndex)
                .map(getMap()::getNodeForID)
                .map(ClassNode.class::cast)
                .collect(Collectors.toSet());
    }


    private Stream<ClassesNode> connectedOriginNodesInTheSameScope(CodeNode node) {
        Stream<JavaClass> originClasses = node.getIncomingDependenciesWithKnownTargets()
        .map(Dependency::getOriginClass);
        return nodesContainedInScope(originClasses);
    }

    private Stream<ClassesNode> connectedTargetNodesInTheSameScope(CodeNode node) {
        Stream<JavaClass> targetClasses = node.getOutgoingDependenciesWithKnownTargets()
        .map(Dependency::getTargetClass);
        return nodesContainedInScope(targetClasses);
    }
    private Stream<ClassesNode> nodesContainedInScope(Stream<JavaClass> originClasses) {
        Stream<ClassesNode> packageNodes = originClasses
        .map(JavaClass::getPackage)
        .map(JavaPackage::getName)
        .map(id -> id + ".package")
        .map(this::idWithSubprojectIndex)
        .map(getMap()::getNodeForID)
        .filter(node -> node != null)
        .map(ClassesNode.class::cast);
        return packageNodes;
    }
}
