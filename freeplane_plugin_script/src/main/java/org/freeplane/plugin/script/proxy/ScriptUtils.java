package org.freeplane.plugin.script.proxy;

import org.freeplane.features.mode.Controller;
import org.freeplane.plugin.script.FormulaThreadLocalStacks;

import java.util.function.Supplier;

/** Various utilities for use in scripting, especially in utility scripts. */
public class ScriptUtils {
    /** a substitute for the binding variable 'c' in
     * <a href="http://freeplane.sourceforge.net/wiki/index.php?title=Your_own_utility_script_library">utility scripts</a>.
     * @since 1.3.2_03 */
    public static Proxy.Controller c() {
        return new ControllerProxy(null);
    }

    /** a substitute for the binding variable 'node' in
     * <a href="http://freeplane.sourceforge.net/wiki/index.php?title=Your_own_utility_script_library">utility scripts</a>.
     * @since 1.3.2_03 */
    public static Proxy.Node node() {
        return new NodeProxy(Controller.getCurrentController().getSelection().getSelected(), null);
    }

	/**
	 * Executes given closure.
	 *
	 * If there are any cyclic dependencies formulas are skipped and no warnings or exceptions are thrown.
	 *
	 * Statically imported by default
	 *
	 * @since 1.7.4
	 */
	public static <T> T ignoreCycles(Supplier<T> closure) {
		return FormulaThreadLocalStacks.INSTANCE.ignoreCycles(closure);
	}
}
