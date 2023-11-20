package org.freeplane.main.codeexplorermode;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.NodeModel;

import com.tngtech.archunit.core.domain.Dependency;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaModifier;


class ClassNodeModel extends CodeNodeModel {
    private final JavaClass javaClass;
    private Set<JavaClass> innerClasses;
    static final String ANNOTATION_ICON_NAME = "code_annotation";
    static final String INTERFACE_ICON_NAME = "code_interface";
    static final String ABSTRACT_CLASS_ICON_NAME = "code_abstractClass";
    static final String CLASS_ICON_NAME = "code_class";
    static final String ENUM_ICON_NAME = "code_enum";

	ClassNodeModel(final JavaClass javaClass, final MapModel map) {
		super(map);
        this.javaClass = javaClass;
        this.innerClasses = null;
		setFolded(false);
		setID(javaClass.getName());
		String nodeText = nodeText(javaClass);
        setText(nodeText);
	}

    private static String nodeText(final JavaClass javaClass) {
        String simpleName = javaClass.getSimpleName();
        return javaClass.getEnclosingClass()
                .map(ec -> nodeText(ec) + "." + simpleName)
                .orElse(simpleName);
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
        return getDependencies(JavaClass::getDirectDependenciesFromSelf);
    }

    @Override
    Stream<Dependency> getIncomingDependencies() {
        return getDependencies(JavaClass::getDirectDependenciesToSelf);
    }

    private Stream<Dependency> getDependencies(Function<? super JavaClass, ? extends Set<Dependency>> mapper) {
        return innerClasses == null ? mapper.apply(javaClass).stream()
                : Stream.concat(Stream.of(javaClass), innerClasses.stream())
                .map(mapper)
                .flatMap(Set::stream)
                .filter(this::connectsDifferentNodes);
    }

    private boolean connectsDifferentNodes(Dependency dep) {
        return findEnclosingNamedClass(dep.getOriginClass()) != findEnclosingNamedClass(dep.getTargetClass());
    }

    void registerInnerClass(JavaClass innerClass) {
        if(innerClass == javaClass)
            return;
        if(innerClasses == null)
            innerClasses = new HashSet<>();
        innerClasses.add(innerClass);
    }


    @Override
    String getUIIconName() {
        if(javaClass.isInterface())
            return INTERFACE_ICON_NAME;
        if(javaClass.isEnum())
            return ENUM_ICON_NAME;
        if(javaClass.getModifiers().contains(JavaModifier.ABSTRACT))
            return ABSTRACT_CLASS_ICON_NAME;
        if(javaClass.isAnnotation())
            return ANNOTATION_ICON_NAME;
        return CLASS_ICON_NAME;
    }
}
