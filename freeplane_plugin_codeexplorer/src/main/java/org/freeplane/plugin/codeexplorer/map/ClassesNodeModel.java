package org.freeplane.plugin.codeexplorer.map;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

    @Override
    HasName getCodeElement() {
        return javaPackage;
    }

	@Override
    protected boolean initializeChildNodes() {
	    List<NodeModel> children = super.getChildrenInternal();
	    if (children.isEmpty()) {
	        final List<JavaClass> classes = javaPackage.getClasses().stream()
	                .filter(CodeNodeModel::isClassSourceKnown).collect(Collectors.toList());
	        if(! classes.isEmpty()) {
	            GraphNodeSort<JavaClass> nodeSort = new GraphNodeSort<JavaClass>();
                for (JavaClass javaClass : classes) {
                    JavaClass edgeStart = findEnclosingNamedClass(javaClass);
                    nodeSort.addNode(edgeStart);
                    DistinctTargetDependencyFilter filter = new DistinctTargetDependencyFilter();
                    Map<JavaClass, Long> dependencies = javaClass.getDirectDependenciesFromSelf().stream()
                            .filter(dep -> goesOutsideEnclosingOriginClass(edgeStart, dep))
                            .map(filter::knownDependency)
                            .collect(Collectors.groupingBy(CodeNodeModel::getTargetNodeClass, Collectors.counting()));
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
	    return false;
	}

    private boolean goesOutsideEnclosingOriginClass(JavaClass edgeStart, Dependency dependency) {
        JavaClass jc = getTargetNodeClass(dependency);
        return ! jc.equals(edgeStart) && jc.getPackage().equals(javaPackage);
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
    Set<CodeNodeModel> findCyclicDependencies() {
        GraphCycleFinder<ClassesNodeModel> cycleFinder = new GraphCycleFinder<ClassesNodeModel>();
        cycleFinder.addNode(this);
        cycleFinder.stopSearchHere();
        cycleFinder.exploreGraph(Collections.singleton(this),
                this::connectedTargetNodesInTheSameScope,
                this::connectedOriginNodesInTheSameScope);
        List<List<ClassesNodeModel>> cycles = cycleFinder.findSimpleCycles();
        Map<JavaPackage, Set<JavaPackage>> origins = new HashMap<>();
        Map<JavaPackage, Set<JavaPackage>> targets = new HashMap<>();
        for(List<ClassesNodeModel> cycle : cycles) {
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
                .map(CodeNodeModel::findEnclosingNamedClass)
                .map(JavaClass::getName)
                .map(getMap()::getNodeForID)
                .map(ClassNodeModel.class::cast)
                .collect(Collectors.toSet());
    }


    private Stream<ClassesNodeModel> connectedOriginNodesInTheSameScope(CodeNodeModel node) {
        Stream<JavaClass> originClasses = node.getIncomingDependenciesWithKnownTargets()
        .map(Dependency::getOriginClass);
        return nodesContainedInScope(originClasses);
    }

    private Stream<ClassesNodeModel> connectedTargetNodesInTheSameScope(CodeNodeModel node) {
        Stream<JavaClass> targetClasses = node.getOutgoingDependenciesWithKnownTargets()
        .map(Dependency::getTargetClass);
        return nodesContainedInScope(targetClasses);
    }
    private Stream<ClassesNodeModel> nodesContainedInScope(Stream<JavaClass> originClasses) {
        Stream<ClassesNodeModel> packageNodes = originClasses
        .map(JavaClass::getPackage)
        .map(JavaPackage::getName)
        .map(id -> id + ".package")
        .map(getMap()::getNodeForID)
        .map(ClassesNodeModel.class::cast);
        return packageNodes;
    }
}
