/*
 * Created on 30 Jan 2024
 *
 * author dimitry
 */
package org.freeplane.plugin.script;

import java.util.function.Supplier;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.HtmlUtils;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.MenuUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.plugin.script.proxy.ScriptUtils;

public class GroovyStaticImports {
    public final static LogUtils logger = new LogUtils();
    public final static UITools ui = new UITools();
    public final static HtmlUtils htmlUtils = HtmlUtils.getInstance();
    public final static TextUtils textUtils = new TextUtils();
    public final static MenuUtils menuUtils = new MenuUtils();
    public final static ScriptUtils scriptUtils = new ScriptUtils();
    public final static FreeplaneScriptBaseClass.ConfigProperties config = new FreeplaneScriptBaseClass.ConfigProperties();
    public static <T> T ignoreCycles(final Supplier<T> closure) {
        return ScriptUtils.ignoreCycles(closure);
    }
}
