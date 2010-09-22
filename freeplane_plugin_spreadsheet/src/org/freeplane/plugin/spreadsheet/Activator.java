package org.freeplane.plugin.spreadsheet;

import java.net.URL;
import java.util.Hashtable;

import javax.swing.JMenu;

import org.freeplane.core.controller.Controller;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.common.map.ModeController;
import org.freeplane.features.common.text.TextController;
import org.freeplane.features.mindmapmode.MModeController;
import org.freeplane.main.osgi.IModeControllerExtensionProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	private static final String MENU_BAR_PARENT_LOCATION = "/menu_bar/extras/first";
	static final String MENU_BAR_LOCATION = MENU_BAR_PARENT_LOCATION + "/spreadsheet";

	private final class SpreadsheetRegistration implements IModeControllerExtensionProvider {

		// 		private MModeController modeController;
		// implements IModeControllerExtensionProvider.installExtension()
		public void installExtension(ModeController modeController) {
			addMenuItems(modeController);
			TextController.getController(modeController).addTextTransformer(new SpreadsheetTextTransformer());
			final FormulaUpdateChangeListener listener = new FormulaUpdateChangeListener();
			Controller.getCurrentModeController().getMapController().addNodeChangeListener(listener);
			Controller.getCurrentModeController().getMapController().addMapChangeListener(listener);
			Controller.getCurrentController().getMapViewManager().addMapViewChangeListener(listener);
		}

		private void addMenuItems(ModeController modeController) {
			final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
			addSubMenu(menuBuilder, MENU_BAR_PARENT_LOCATION, MENU_BAR_LOCATION,
			    SpreadSheetUtils.getSpreadSheetKey("menuname"));
			menuBuilder.addAnnotatedAction(new EvaluateAllAction());
			addPropertiesToOptionPanel();
		}

		private void addSubMenu(final MenuBuilder menuBuilder, final String parentLocation,
		                        final String location, final String menuKey) {
			final JMenu menuItem = new JMenu();
			MenuBuilder.setLabelAndMnemonic(menuItem, TextUtils.getText(menuKey));
			menuBuilder.addMenuItem(parentLocation, menuItem, location, MenuBuilder.AS_CHILD);
		}

		private void addPropertiesToOptionPanel() {
			final URL preferences = this.getClass().getResource("preferences.xml");
			if (preferences == null)
				throw new RuntimeException("cannot open preferences");
			final Controller controller = Controller.getCurrentController();
			MModeController modeController = (MModeController) controller.getModeController();
			modeController.getOptionPanelBuilder().load(preferences);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
		props.put("mode", new String[] { MModeController.MODENAME /*TODO: browse mode too?*/});
		context.registerService(IModeControllerExtensionProvider.class.getName(), new SpreadsheetRegistration(), props);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext context) throws Exception {
	}
}
