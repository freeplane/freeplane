/*
 * Created on 8 Mar 2024
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.task;

public class SubprojectIdentifier {
    private final String id;
    private final String name;
    public SubprojectIdentifier(String id, String name) {
        super();
        this.id = id;
        this.name = name;
    }
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
}
