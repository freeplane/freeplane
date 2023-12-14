/*
 * Created on 28 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.dependencies;

/**
 * Enums for specifying the direction of a dependency.
 */
public enum DependencyDirection {
    UP("->^"), DOWN("->v"), ANY("->");

    public static DependencyDirection parseDirection(String notation) {
        for(DependencyDirection direction : DependencyDirection.values())
            if(notation.equals(direction.notation))
                return direction;
        throw new IllegalArgumentException(
                "No enum constant for notation " + notation);

    }

    public final String notation;

    DependencyDirection(String notation) {
        this.notation = notation;
    }

    boolean matches(boolean goesUp) {
        return this == ANY || (this == UP) == goesUp;
    }
}