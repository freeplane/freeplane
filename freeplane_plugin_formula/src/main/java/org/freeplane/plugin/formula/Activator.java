package org.freeplane.plugin.formula;

import java.net.URL;
import java.util.Hashtable;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.map.MapController;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.text.TextController;
import org.freeplane.features.text.mindmapmode.ConditionalContentTransformer;
import org.freeplane.features.text.mindmapmode.MTextController;
import org.freeplane.main.osgi.IModeControllerExtensionProvider;
import org.freeplane.plugin.formula.dependencies.ActionFactory;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	private static final String FORMULA_DISABLE_PLUGIN = "formula_disable_plugin";
	private static final String FORMULA_DISABLE_CACHING = "formula_disable_caching";
	private static final String MENU_BAR_PARENT_LOCATION = "/menu_bar/extras/first";
	static final String MENU_BAR_LOCATION = MENU_BAR_PARENT_LOCATION + "/formula";

	private static final String TOGGLE_PARSE_FORMULAS = "parse_formulas";

	private final class FormulaPluginRegistration implements IModeControllerExtensionProvider {
		private static final String PREFERENCES_RESOURCE = "preferences.xml";

		@Override
		public void installExtension(ModeController modeController) {
			addPluginDefaults();
			addPreferencesToOptionPanel();
			final boolean disablePluginProperty = ResourceController.getResourceController().getBooleanProperty(
			    FORMULA_DISABLE_PLUGIN);
			final EvaluateAllAction evaluateAllAction = new EvaluateAllAction();
			modeController.addAction(evaluateAllAction);
			ActionFactory.createActions(modeController);
			if (!disablePluginProperty) {

				TextController textController = TextController.getController(modeController);
                textController.addTextTransformer(//
						new ConditionalContentTransformer(new FormulaTextTransformer(1), TOGGLE_PARSE_FORMULAS));
                
                if(textController instanceof MTextController) {
                    ((MTextController)textController).addDetailContentType(FormulaTextTransformer.CONTENT_TYPE_FORMULA);
                }

				// to enable Formulas in text templates:
				// TextController.getController(modeController).addTextTransformer(new FormulaTextTransformer(100));
				final FormulaUpdateChangeListener listener = new FormulaUpdateChangeListener();
				final MapController mapController = modeController.getMapController();
				mapController.addNodeChangeListener(listener);
				mapController.addMapChangeListener(listener);
				mapController.addMapLifeCycleListener(listener);
				final boolean disableCacheProperty = ResourceController.getResourceController().getBooleanProperty(
				    FORMULA_DISABLE_CACHING);
				if (disableCacheProperty) {
					System.err.println("Formula cache disabled."
					        + " This might severely impair performance when using formulas.");
				}
			}
			else {
				System.out.println("Formula plugin is disabled");
				evaluateAllAction.setEnabled(false);
			}
		}

		private void addPreferencesToOptionPanel() {
			final URL preferences = this.getClass().getResource(PREFERENCES_RESOURCE);
			if (preferences == null)
				throw new RuntimeException("cannot open preferences");
			final Controller controller = Controller.getCurrentController();
			MModeController modeController = (MModeController) controller.getModeController();
			modeController.getOptionPanelBuilder().load(preferences);
		}

		private void addPluginDefaults() {
			final URL defaults = this.getClass().getResource(ResourceController.PLUGIN_DEFAULTS_RESOURCE);
			if (defaults == null)
				throw new RuntimeException("cannot open " + ResourceController.PLUGIN_DEFAULTS_RESOURCE);
			Controller.getCurrentController().getResourceController().addDefaults(defaults);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
		props.put("mode", new String[] { MModeController.MODENAME /*TODO: browse mode too?*/});
		context.registerService(IModeControllerExtensionProvider.class.getName(), new FormulaPluginRegistration(),
		    props);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
	}
}
