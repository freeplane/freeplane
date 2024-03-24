/*
 * Created on 1 Dec 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.map;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.codeexplorer.graph.GraphNodeSort;
import org.freeplane.plugin.codeexplorer.task.CodeExplorerConfiguration;
import org.freeplane.plugin.codeexplorer.task.GroupIdentifier;
import org.freeplane.plugin.codeexplorer.task.GroupMatcher;
import org.freeplane.plugin.codeexplorer.task.UserDefinedCodeExplorerConfiguration;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.domain.properties.HasName;

class ProjectRootNode extends CodeNode implements GroupFinder{
    static final String UI_ICON_NAME = "code_project";
    static {
        IconStoreFactory.INSTANCE.createStateIcon(ProjectRootNode.UI_ICON_NAME, "code/homeFolder.svg");
    }
    private static final Entry<Integer, String> UNKNOWN = new AbstractMap.SimpleEntry<>(-1, ":unknown:");
    private final JavaPackage rootPackage;
    private final Map<String, Map.Entry<Integer, String>> groupsById;
    private final String[] idBySubrojectIndex;
    private final Set<String> badLocations;
    private final JavaClasses classes;
    private final long classCount;
    private final GroupMatcher groupMatcher;
    static ProjectRootNode asMapRoot(String projectName, CodeMap map, JavaClasses classes, GroupMatcher groupMatcher) {
        ProjectRootNode projectRootNode = new ProjectRootNode(projectName, map, classes, groupMatcher);
        map.setRoot(projectRootNode);
        if(projectRootNode.getChildCount() > 20)
            projectRootNode.getChildren()
                .forEach(node -> ((CodeNode)node).memoizeCodeDependencies());
        return projectRootNode;
    }
    private ProjectRootNode(String projectName, CodeMap map, JavaClasses classes, GroupMatcher groupMatcher) {
        super(map, 0);
        this.classes = classes;
        this.groupMatcher = groupMatcher;
        this.rootPackage = classes.getDefaultPackage();
        setID("projectRoot");

        groupsById = new LinkedHashMap<>();
        classes.stream()
        .map(groupMatcher::groupIdentifier)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(this::addLocation);
        badLocations = new HashSet<>();
        map.setGroupFinder(this);
        initializeChildNodes();
        classCount = super.getChildrenInternal().stream()
                .map(PackageNode.class::cast)
                .mapToLong(PackageNode::getClassCount)
                .sum();
        setText(projectName + formatClassCount(classCount));
        final CodeExplorerConfiguration configuration = map.getConfiguration();
        if(configuration instanceof UserDefinedCodeExplorerConfiguration) {
            ((UserDefinedCodeExplorerConfiguration)configuration).getUserContent().keySet()
            .forEach(this::addDeletedLocation);
        }
        idBySubrojectIndex = new String[groupsById.size()];
        groupsById.entrySet().forEach(e -> idBySubrojectIndex[e.getValue().getKey()] = e.getKey());
    }
    private void addDeletedLocation(String location) {
        final Entry<Integer, String> locationEntry = addLocation(new GroupIdentifier(location, location));
        final int childIndex = locationEntry.getKey();
        if(childIndex == getChildCount())
            insert(new DeletedContentNode(getMap(), "", childIndex, locationEntry.getValue()));
    }

    private Entry<Integer, String> addLocation(GroupIdentifier identifier) {
        return groupsById.computeIfAbsent(identifier.getId(),
                key -> new AbstractMap.SimpleEntry<>(groupsById.size(), identifier.getName()));
    }

    private void initializeChildNodes() {
        List<NodeModel> children = super.getChildrenInternal();
        List<PackageNode> nodes = groupsById.values().stream()
                .parallel()
                .map(e ->
                    new PackageNode(rootPackage, getMap(), e.getValue(), e.getKey().intValue(), true))
                .collect(Collectors.toList());
        GraphNodeSort<Integer> childNodes = new GraphNodeSort<>();
        Integer[] subrojectIndices = IntStream.range(0, groupsById.size())
                .mapToObj(Integer::valueOf)
                .toArray(Integer[]::new);

        nodes
        .stream()
        .filter(node ->node.getClassCount() > 0)
        .forEach(node -> {
            childNodes.addNode(subrojectIndices[node.groupIndex]);
            DistinctTargetDependencyFilter filter = new DistinctTargetDependencyFilter();
            Map<Integer, Long> referencedGroups = node.getOutgoingDependenciesWithKnownTargets()
                    .map(filter::knownDependency)
                    .map(Dependency::getTargetClass)
                    .collect(Collectors.groupingBy(t -> subrojectIndices[groupIndexOf(t)], Collectors.counting()));
            referencedGroups.entrySet()
            .forEach(e -> childNodes.addEdge(subrojectIndices[node.groupIndex], e.getKey(), e.getValue()));
        });
        Comparator<Set<Integer>> comparingByReversedClassCount = Comparator.comparing(
                indices -> -indices.stream()
                    .map(nodes::get)
                    .mapToLong(PackageNode::getClassCount)
                    .sum()
                );
        List<List<Integer>> orderedPackages = childNodes.sortNodes(
                Comparator.comparing(i -> nodes.get(i).getText()),
                comparingByReversedClassCount
                .thenComparing(SubgroupComparator.comparingByName(i -> nodes.get(i).getText())));
        for(int subgroupIndex = 0; subgroupIndex < orderedPackages.size(); subgroupIndex++) {
            for (Integer groupIndex : orderedPackages.get(subgroupIndex)) {
                final CodeNode node = nodes.get(groupIndex);
                children.add(node);
                node.setParent(this);
            }
        }
        for(NodeModel child: children)
            ((CodeNode) child).setInitialFoldingState();

    }

    @Override
    HasName getCodeElement() {
        return () -> "root";
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
    public boolean belongsToAnyGroup(JavaClass javaClass) {
        return groupMatcher.belongsToGroup(javaClass);
    }

    @Override
    public int groupIndexOf(JavaClass javaClass) {
        Optional<String> classSourceLocation = groupMatcher.groupIdentifier(javaClass).map(GroupIdentifier::getId);
        Optional <Entry<Integer, String>> groupEntry = classSourceLocation
                .map( s -> groupsById.getOrDefault(s, UNKNOWN));

        if(groupEntry.filter(UNKNOWN::equals).isPresent() && badLocations.add(classSourceLocation.get())) {
            LogUtils.info("Unknown class source location " + javaClass.getSource().get().getUri());
         }
        return groupEntry.orElse(UNKNOWN).getKey().intValue();
    }

    @Override
    public int groupIndexOf(String location) {
        return groupsById.getOrDefault(location, UNKNOWN).getKey().intValue();
    }

    @Override
    public Stream<JavaClass> allClasses() {
        return classes.stream();
    }

    JavaClasses getImportedClasses() {
        return classes;
    }

    @Override
    public String getIdByIndex(int index) {
        if(index >= 0 && index < idBySubrojectIndex.length)
            return idBySubrojectIndex[index];
        else
            throw new IllegalArgumentException("Bad index " + index);
    }
}
