/*
 * Created on 3 Dec 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.map;

import java.util.stream.Stream;

import com.tngtech.archunit.core.domain.JavaClass;

interface SubprojectFinder {
    SubprojectFinder EMPTY = new SubprojectFinder() {

        @Override
        public int subprojectIndexOf(JavaClass javaClass) {
            return -1;
        }

        @Override
        public Stream<JavaClass> allClasses() {
            return Stream.empty();
        }

        @Override
        public String getIdByIndex(int index) {
           throw new IllegalArgumentException("No locations");
        }

        @Override
        public int subprojectIndexOf(String subprojectId) {
            return -1;
        }

        @Override
        public boolean belongsToAnySubproject(JavaClass javaClass) {
             return false;
        }
    };
    boolean belongsToAnySubproject(JavaClass javaClass);
    int subprojectIndexOf(JavaClass javaClass);
    int subprojectIndexOf(String subprojectId);
    String getIdByIndex(int index);
    Stream<JavaClass> allClasses();
}
