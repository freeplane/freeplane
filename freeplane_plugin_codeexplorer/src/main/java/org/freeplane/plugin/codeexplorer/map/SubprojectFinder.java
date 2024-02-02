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
        public String locationByIndex(int index) {
           throw new IllegalArgumentException("No locations");
        }

        @Override
        public int subprojectIndexOf(String location) {
            return -1;
        }
    };

    int subprojectIndexOf(JavaClass javaClass);
    int subprojectIndexOf(String location);
    String locationByIndex(int index);
    Stream<JavaClass> allClasses();
}
