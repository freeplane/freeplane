package org.freeplane.plugin.script;

public class ScriptEngineNotLoadedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    ScriptEngineNotLoadedException(String message) {
        super(message);
    }

}
