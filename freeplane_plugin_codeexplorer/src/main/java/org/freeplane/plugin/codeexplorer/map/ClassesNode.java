package org.freeplane.plugin.codeexplorer.map;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
        initializeChildNodes();
	}

    private Stream<JavaClass> getClasses() {
        return javaPackage.getClasses().stream()
                .filter(this::belongsToSameSubproject);
    }

    @Override
    HasName getCodeElement() {
        return javaPackage;
    }

    private void initializeChildNodes() {
        List<NodeModel> children = super.getChildrenInternal();
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
            List<List<JavaClass>> orderedClasses = nodeSort.sortNodes(SubgroupComparator.comparingByName(HasName::getName));
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

    private boolean goesOutsideEnclosingOriginClass(JavaClass edgeStart, Dependency dependency) {
        return hasValidTopLevelClasses(dependency)
                && ! getTargetNodeClass(dependency).equals(edgeStart);
    }

    @Override
	public String toString() {
		return getText();
	}

    @Override
    Stream<Dependency> getOutgoingDependencies() {
        return getClasses()
                .flatMap(c -> c.getDirectDependenciesFromSelf().stream())
                .filter(CodeNode::hasValidTopLevelClasses)
                .filter(dep -> ! dep.getTargetClass().getPackage().equals(dep.getOriginClass().getPackage())
                        || subprojectIndexOf(dep.getTargetClass()) != subprojectIndex);
    }

    @Override
    Stream<Dependency> getIncomingDependencies() {
        return getClasses()
                .flatMap(c -> c.getDirectDependenciesToSelf().stream())
                .filter(CodeNode::hasValidTopLevelClasses)
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
        Set<Entry<ClassesNode, ClassesNode>> cycles = cycleFinder.findSimpleCycles();
        Map<JavaPackage, Set<JavaPackage>> origins = new HashMap<>();
        Map<JavaPackage, Set<JavaPackage>> targets = new HashMap<>();
        for(Entry<ClassesNode, ClassesNode>edge : cycles) {
                JavaPackage origin = edge.getKey().javaPackage;
                JavaPackage target = edge.getValue().javaPackage;
                origins.computeIfAbsent(target, x -> new HashSet<>()).add(origin);
                targets.computeIfAbsent(origin, x -> new HashSet<>()).add(target);
        }
        return cycles.stream()
                .map(Map.Entry::getKey)
                .flatMap(packageNode ->
                    Stream.concat(
                            packageNode.getOutgoingDependenciesWithKnownTargets()
                            .map(Dependency::getTargetClass)
                            .filter(targetClass -> targets.get(packageNode.javaPackage).contains(targetClass.getPackage())),
                            packageNode.getIncomingDependenciesWithKnownOrigins()
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
        Stream<JavaClass> originClasses = node.getIncomingDependenciesWithKnownOrigins()
        .map(Dependency::getOriginClass);
        return nodesContainedInSubproject(originClasses);
    }

    private Stream<ClassesNode> connectedTargetNodesInTheSameScope(CodeNode node) {
        Stream<JavaClass> targetClasses = node.getOutgoingDependenciesWithKnownTargets()
        .map(Dependency::getTargetClass);
        return nodesContainedInSubproject(targetClasses);
    }
    private Stream<ClassesNode> nodesContainedInSubproject(Stream<JavaClass> classes) {
        return classes
        .filter(this::belongsToSameSubproject)
        .map(CodeNode::findEnclosingNamedClass)
        .map(JavaClass::getName)
        .map(this::idWithSubprojectIndex)
        .map(getMap()::getNodeForID)
        .map(NodeModel::getParentNode)
        .map(ClassesNode.class::cast);

    }
}
