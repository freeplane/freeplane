/*
 * Created on 8 Feb 2024
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.task;

import com.tngtech.archunit.core.domain.JavaClasses;

public interface CodeExplorerConfiguration {

    int countLocations();

    JavaClasses importClasses();

    String getProjectName();

    LocationMatcher createLocationMatcher();

    DependencyJudge getDependencyJudge();

    AnnotationMatcher getAnnotationMatcher();

    boolean canBeSaved();

}
