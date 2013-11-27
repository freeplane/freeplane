import java.util.Hashtable;

import org.freeplane.core.ui.IMenuContributor;
import org.freeplane.core.ui.MenuBuilder;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.main.osgi.IModeControllerExtensionProvider;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
    public void start(final BundleContext context) throws Exception {
		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
		props.put("mode", new String[] { MModeController.MODENAME /*TODO: other modes too?*/});
		context.registerService(IModeControllerExtensionProvider.class.getName(),
		    new IModeControllerExtensionProvider() {
			    @Override
                public void installExtension(ModeController modeController) {
				    final MenuBuilder menuBuilder = modeController.getUserInputListenerFactory().getMenuBuilder();
				    final $$$$Action action = new $$$$Action();
					modeController.addAction(action);
				    modeController.addMenuContributor(new IMenuContributor() {
						@Override
						public void updateMenus(ModeController modeController, MenuBuilder builder) {
						    menuBuilder.addAction("/menu_bar/file", action, MenuBuilder.AS_CHILD);
						}
					});
			    }
		    /*TODO: further initializations*/}, props);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
    public void stop(final BundleContext context) throws Exception {
	}
}
