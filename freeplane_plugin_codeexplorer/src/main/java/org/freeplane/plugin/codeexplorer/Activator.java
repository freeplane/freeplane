package org.freeplane.plugin.codeexplorer;

import java.net.URL;
import java.util.Hashtable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.Compat;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.main.application.CommandLineOptions;
import org.freeplane.main.mindmapmode.stylemode.ExtensionInstaller;
import org.freeplane.main.mindmapmode.stylemode.ExtensionInstaller.Context;
import org.freeplane.main.osgi.IControllerExtensionProvider;
import org.freeplane.plugin.codeexplorer.archunit.ArchUnitServer;
import org.freeplane.plugin.codeexplorer.configurator.CodeProjectController;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
    private CodeModeController modeController;
    private ExecutorService classImportService;
    private static final String PREFERENCES_RESOURCE = "preferences.xml";

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(final BundleContext context) throws Exception {
		if (Compat.isJavaVersionLessThan("11."))
			System.out.println("Java 11 is required for code explorer mode. Disabled.");
		else
			registerMindMapModeExtension(context);
	}

	private void registerMindMapModeExtension(final BundleContext context) {
		final Hashtable<String, String[]> props = new Hashtable<String, String[]>();
		context.registerService(IControllerExtensionProvider.class.getName(),
		    new IControllerExtensionProvider() {

                @Override
				public void installExtension(Controller controller, CommandLineOptions options, ExtensionInstaller.Context context) {
			        if(context == Context.MAIN && modeController == null) {
			            classImportService = Executors.newSingleThreadExecutor(this::newThread);
			            final ArchUnitServer archUnitServer = new ArchUnitServer();
			            ResourceController.getResourceController().addPropertyChangeListenerAndPropagate(archUnitServer);
                        modeController = CodeModeControllerFactory.createModeController(classImportService, archUnitServer);
                        addPreferencesToOptionPanel();
                    }
			    }

                private Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "Load explored packages");
                    thread.setDaemon(true);
                    thread.setPriority(Math.min(Thread.MAX_PRIORITY, Thread.currentThread().getPriority() + 1));
                    return thread;
                }

                private void addPreferencesToOptionPanel() {
                    final URL preferences = this.getClass().getResource(PREFERENCES_RESOURCE);
                    if (preferences == null)
                        throw new RuntimeException("cannot open preferences");
                    MModeController modeController = MModeController.getMModeController();
                    modeController.getOptionPanelBuilder().load(preferences);
                }
		    }, props);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(final BundleContext context) throws Exception {
	    if(modeController != null) {
	        modeController.getExtension(CodeProjectController.class).saveConfiguration();
	        classImportService.shutdownNow();
	    }
	}
}
