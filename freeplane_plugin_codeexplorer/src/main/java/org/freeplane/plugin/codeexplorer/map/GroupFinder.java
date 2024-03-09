/*
 * Created on 3 Dec 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.map;

import java.util.stream.Stream;

import com.tngtech.archunit.core.domain.JavaClass;

interface GroupFinder {
    GroupFinder EMPTY = new GroupFinder() {

        @Override
        public int groupIndexOf(JavaClass javaClass) {
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
        public int groupIndexOf(String groupId) {
            return -1;
        }

        @Override
        public boolean belongsToAnyGroup(JavaClass javaClass) {
             return false;
        }
    };
    boolean belongsToAnyGroup(JavaClass javaClass);
    int groupIndexOf(JavaClass javaClass);
    int groupIndexOf(String groupId);
    String getIdByIndex(int index);
    Stream<JavaClass> allClasses();
}
