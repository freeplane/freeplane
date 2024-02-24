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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.freeplane.core.util.LogUtils;
import org.freeplane.features.icon.factory.IconStoreFactory;
import org.freeplane.features.map.NodeModel;
import org.freeplane.plugin.codeexplorer.graph.GraphNodeSort;
import org.freeplane.plugin.codeexplorer.task.CodeExplorerConfiguration;
import org.freeplane.plugin.codeexplorer.task.LocationMatcher;
import org.freeplane.plugin.codeexplorer.task.UserDefinedCodeExplorerConfiguration;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaPackage;
import com.tngtech.archunit.core.domain.properties.HasName;

class ProjectRootNode extends CodeNode implements SubprojectFinder{
    static final String UI_ICON_NAME = "code_project";
    static {
        IconStoreFactory.INSTANCE.createStateIcon(ProjectRootNode.UI_ICON_NAME, "code/homeFolder.svg");
    }
    private static final Entry<Integer, String> UNKNOWN = new AbstractMap.SimpleEntry<>(-1, ":unknown:");
    private final JavaPackage rootPackage;
    private final Map<String, Map.Entry<Integer, String>> subprojectsByLocation;
    private final String[] locationsBySubrojectIndex;
    private final Set<String> badLocations;
    private JavaClasses classes;
    private final LocationMatcher locationMatcher;
    static ProjectRootNode asMapRoot(String projectName, CodeMap map, JavaClasses classes, LocationMatcher locationMatcher) {
        ProjectRootNode projectRootNode = new ProjectRootNode(projectName, map, classes, locationMatcher);
        map.setRoot(projectRootNode);
        if(projectRootNode.getChildCount() > 20)
            projectRootNode.getChildren()
                .forEach(node -> ((CodeNode)node).memoizeCodeDependencies());
        return projectRootNode;
    }
    private ProjectRootNode(String projectName, CodeMap map, JavaClasses classes, LocationMatcher locationMatcher) {
        super(map, 0);
        this.classes = classes;
        this.locationMatcher = locationMatcher;
        this.rootPackage = classes.getDefaultPackage();
        setID("projectRoot");
        setText(projectName);

        subprojectsByLocation = new LinkedHashMap<>();
        classes.stream()
        .map(locationMatcher::coreLocationPath)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .forEach(this::addLocation);
        badLocations = new HashSet<>();
        map.setSubprojectFinder(this);
        initializeChildNodes();
        final CodeExplorerConfiguration configuration = map.getConfiguration();
        if(configuration instanceof UserDefinedCodeExplorerConfiguration) {
            ((UserDefinedCodeExplorerConfiguration)configuration).getUserContent().keySet()
            .forEach(this::addDeletedLocation);
        }
        locationsBySubrojectIndex = new String[subprojectsByLocation.size()];
        subprojectsByLocation.entrySet().forEach(e -> locationsBySubrojectIndex[e.getValue().getKey()] = e.getKey());
    }
    private void addDeletedLocation(String location) {
        final Entry<Integer, String> locationEntry = addLocation(location);
        final int childIndex = locationEntry.getKey();
        if(childIndex == getChildCount())
            insert(new DeletedContentNode(getMap(), "", childIndex, locationEntry.getValue()));
    }

    private Entry<Integer, String> addLocation(String location) {
        return subprojectsByLocation.computeIfAbsent(location,
                key -> new AbstractMap.SimpleEntry<>(subprojectsByLocation.size(), toSubprojectName(location)));
    }

    private static String toSubprojectName(String location) {
        Pattern projectName = Pattern.compile("/([^/]+?)!?/(?:(?:bin|build|target)/.*)*$");
        Matcher matcher = projectName.matcher(location);
        if(matcher.find())
            return matcher.group(1);
        else
            return location;
    }

    private void initializeChildNodes() {
        List<NodeModel> children = super.getChildrenInternal();
        List<PackageNode> nodes = subprojectsByLocation.values().stream()
                .parallel()
                .map(e ->
                    new PackageNode(rootPackage, getMap(), e.getValue(), e.getKey().intValue(), true))
                .collect(Collectors.toList());
        GraphNodeSort<Integer> childNodes = new GraphNodeSort<>();
        nodes.forEach(node -> {
            childNodes.addNode(node.subprojectIndex);
            DistinctTargetDependencyFilter filter = new DistinctTargetDependencyFilter();
            Map<Integer, Long> referencedSubprojects = node.getOutgoingDependenciesWithKnownTargets()
                    .map(filter::knownDependency)
                    .map(Dependency::getTargetClass)
                    .collect(Collectors.groupingBy(this::subprojectIndexOf, Collectors.counting()));
            referencedSubprojects.entrySet()
            .forEach(e -> childNodes.addEdge(node.subprojectIndex, e.getKey(), e.getValue()));
        });
        Comparator<Set<Integer>> comparingByReversedClassCount = Comparator.comparing(
                indices -> -indices.stream()
                    .map(nodes::get)
                    .mapToLong(PackageNode::getClassCount)
                    .sum()
                );
        List<List<Integer>> orderedPackages = childNodes.sortNodes(comparingByReversedClassCount
                .thenComparing(SubgroupComparator.comparingByName(i -> nodes.get(i).getText())));
        for(int subgroupIndex = 0; subgroupIndex < orderedPackages.size(); subgroupIndex++) {
            for (Integer subprojectIndex : orderedPackages.get(subgroupIndex)) {
                final CodeNode node = nodes.get(subprojectIndex);
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
    public int subprojectIndexOf(JavaClass javaClass) {
        Optional<String> classSourceLocation = locationMatcher.coreLocationPath(javaClass);
        Optional <Entry<Integer, String>> subprojectEntry = classSourceLocation
                .map( s -> subprojectsByLocation.getOrDefault(s, UNKNOWN));

        if(subprojectEntry.filter(UNKNOWN::equals).isPresent() && badLocations.add(classSourceLocation.get())) {
            LogUtils.info("Unknown class source location " + javaClass.getSource().get().getUri());
         }
        return subprojectEntry.orElse(UNKNOWN).getKey().intValue();
    }

    @Override
    public int subprojectIndexOf(String location) {
        return subprojectsByLocation.getOrDefault(location, UNKNOWN).getKey().intValue();
    }

    @Override
    public Stream<JavaClass> allClasses() {
        return classes.stream();
    }

    @Override
    public String locationByIndex(int index) {
        if(index >= 0 && index < locationsBySubrojectIndex.length)
            return locationsBySubrojectIndex[index];
        else
            throw new IllegalArgumentException("Bad index " + index);
    }
}
