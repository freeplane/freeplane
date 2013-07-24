/*
 *  Freeplane - mind map editor
 *  Copyright (C) 2012 Dimitry
 *
 *  This file author is Dimitry
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.freeplane.plugin.script;

import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintStream;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.apache.commons.io.FilenameUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.FileUtils;
import org.freeplane.features.map.NodeModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.main.application.FreeplaneSecurityManager;
import org.freeplane.plugin.script.proxy.ProxyFactory;

/**
 * Implements scripting via JSR233 implementation for all other languages except Groovy.
 */
public class GenericScript implements IScript {
    private static final boolean CACHE_COMPILED_SCRIPTS = false;
    final private String script;
    private ScriptingPermissions specificPermissions;
    private CompiledScript compiledScript;
    private Throwable errorsInScript;
    private IFreeplaneScriptErrorHandler errorHandler;
    private PrintStream outStream;
    private ScriptContext scriptContext;
    private final static Object scriptEngineManagerMutex = new Object();
    private static ScriptEngineManager scriptEngineManager;
    private final ScriptEngine engine;

    public GenericScript(String script, ScriptEngine scriptEngine, ScriptingPermissions permissions) {
        this.script = script;
        this.specificPermissions = permissions;
        engine = scriptEngine;
        compiledScript = null;
        errorsInScript = null;
        errorHandler = IGNORING_SCRIPT_ERROR_HANDLER;
        outStream = System.out;
        scriptContext = null;
    }

    public GenericScript(String script, String scriptEngineName, ScriptingPermissions permissions) {
        this(script, findScriptEngine(scriptEngineName), permissions);
    }
    
    public GenericScript(File scriptFile, ScriptingPermissions permissions) {
        this(slurpFile(scriptFile), findScriptEngine(scriptFile), permissions);
    }

    private static String slurpFile(File scriptFile) {
        try {
            return FileUtils.slurpFile(scriptFile);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public IFreeplaneScriptErrorHandler getErrorHandler() {
        return errorHandler;
    }

    @Override
    public IScript setErrorHandler(IFreeplaneScriptErrorHandler pErrorHandler) {
        this.errorHandler = pErrorHandler;
        return this;
    }

    @Override
    public PrintStream getOutStream() {
        return outStream;
    }

    @Override
    public IScript setOutStream(PrintStream outStream) {
        this.outStream = outStream;
        return this;
    }

    @Override
    public ScriptContext getScriptContext() {
        return scriptContext;
    }

    @Override
    public IScript setScriptContext(ScriptContext scriptContext) {
        this.scriptContext = scriptContext;
        return this;
    }

    @Override
    public Object getScript() {
        return script;
    }

    @Override
    public ScriptingPermissions getSpecificPermissions() {
        return specificPermissions;
    }

    static public File getUserScriptDir() {
        final String userDir = ResourceController.getResourceController().getFreeplaneUserDirectory();
        return new File(userDir, ScriptingConfiguration.USER_SCRIPTS_DIR);
    }

    @Override
    public Object execute(final NodeModel node) {
        try {
            if (errorsInScript != null)
                throw new ExecuteScriptException(errorsInScript.getMessage(), errorsInScript);
            final ScriptSecurity scriptSecurity = new ScriptSecurity(script, specificPermissions, outStream);
            final ScriptingPermissions originalScriptingPermissions = new ScriptingPermissions(ResourceController
                .getResourceController().getProperties());
            final FreeplaneSecurityManager securityManager = (FreeplaneSecurityManager) System.getSecurityManager();
            final boolean needsSecurityManager = securityManager.needsFinalSecurityManager();
            final ScriptingSecurityManager scriptingSecurityManager = scriptSecurity.getScriptingSecurityManager();
            try {
                final SimpleScriptContext context = createScriptContext(node);
                if (engine instanceof Compilable) {
                    compileAndCache((Compilable) engine);
                    if (needsSecurityManager)
                        securityManager.setFinalSecurityManager(scriptingSecurityManager);
                    return compiledScript.eval(context);
                }
                else {
                    if (needsSecurityManager)
                        securityManager.setFinalSecurityManager(scriptingSecurityManager);
                    return engine.eval(script, context);
                }
            }
            finally {
                if (compiledScript != null) {
                    if (needsSecurityManager)
                        securityManager.removeFinalSecurityManager(scriptingSecurityManager);
                }
                /* restore preferences (and assure that the values are unchanged!). */
                originalScriptingPermissions.restorePermissions();
            }
        }
        catch (final ScriptException e) {
            handleScriptRuntimeException(e);
            throw new RuntimeException(e);
        }
        catch (final Throwable e) {
            if (Controller.getCurrentController().getSelection() != null)
                Controller.getCurrentModeController().getMapController().select(node);
            throw new ExecuteScriptException(e.getMessage(), e);
        }
    }

    private SimpleScriptContext createScriptContext(final NodeModel node) {
        final SimpleScriptContext context = new SimpleScriptContext();
        // FIXME: two writer for one stream?
        context.setWriter(new OutputStreamWriter(outStream));
        context.setErrorWriter(new OutputStreamWriter(outStream));
        context.setBindings(createBinding(node), javax.script.ScriptContext.ENGINE_SCOPE);
        return context;
    }

    private Bindings createBinding(final NodeModel node) {
        final Bindings binding = engine.createBindings();
        binding.put("c", ProxyFactory.createController(scriptContext));
        binding.put("node", ProxyFactory.createNode(node, scriptContext));
        return binding;
    }

    private static ScriptEngineManager getScriptEngineManager() {
        synchronized (scriptEngineManagerMutex) {
            if (scriptEngineManager == null)
                scriptEngineManager = new ScriptEngineManager();
            return scriptEngineManager;
        }
    }

    @SuppressWarnings("unused")
    private void compileAndCache(Compilable engine) throws Throwable {
        if (compiledScript != null && CACHE_COMPILED_SCRIPTS)
            return;
        else if (errorsInScript != null)
            throw errorsInScript;
        else
            try {
                compiledScript = engine.compile(script);
            }
            catch (Throwable e) {
                errorsInScript = e;
                throw e;
            }
    }

    private static ScriptEngine findScriptEngine(String scriptEngineName) {
        final ScriptEngineManager manager = getScriptEngineManager();
        return manager.getEngineByName(scriptEngineName);
    }

    private static ScriptEngine findScriptEngine(File scriptFile) {
        final ScriptEngineManager manager = getScriptEngineManager();
        return manager.getEngineByExtension(FilenameUtils.getExtension(scriptFile.getName()));
    }

    private void handleScriptRuntimeException(final ScriptException e) {
        final String resultString = e.getMessage();
        outStream.print("message: " + resultString);
        int lineNumber = e.getLineNumber();
        outStream.print("Line number: " + lineNumber);
        errorHandler.gotoLine(lineNumber);
        throw new ExecuteScriptException(e.getMessage() + " at line " + lineNumber, e);
    }
}
