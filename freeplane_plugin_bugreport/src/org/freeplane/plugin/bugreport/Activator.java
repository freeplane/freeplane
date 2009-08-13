package org.freeplane.plugin.bugreport;

import java.util.logging.Logger;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {
	private XmlRpcHandler handler;
	private Logger parentLogger;

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(final BundleContext context) throws Exception {
		parentLogger = Logger.getAnonymousLogger().getParent();
		handler = new XmlRpcHandler();
		parentLogger.addHandler(handler);
//		addBugReporter();
	}

//	private void addBugReporter() {
//	    KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
//			public boolean dispatchKeyEvent(final KeyEvent e) {
//				if (e.getID() == KeyEvent.KEY_PRESSED && e.isAltDown() && e.isAltDown() && e.isShiftDown()
//				        && e.getKeyCode() == KeyEvent.VK_B) {
//					final RuntimeException runtimeException = new RuntimeException(ResourceBundles
//					    .getText("user_initiated_bug_report"));
//					LogTool.severe(runtimeException);
//				}
//				return false;
//			}
//		});
//		new Thread(new Runnable(){
//
//			public void run() {
//				final BufferedReader in
//		          = new BufferedReader(new InputStreamReader(System.in));
//				PrintStream out = null;
//				try {
//	                for(String line = in.readLine();!line.equals("stop"); line = in.readLine()){
//	                	if(line.equals("stack")){
//		                	if(out == null){
//		                		out = new PrintStream(ResourceController.getResourceController().getFreeplaneUserDirectory() + File.separatorChar + "stack.txt");
//		                	}
//		                	out.println(new Date().toString());
//	                		StringBuilder writer = new StringBuilder();
//	                		final Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
//	                		for(Entry<Thread, StackTraceElement[]> stackTraceEntry : allStackTraces.entrySet()){
//	                			writer.append(stackTraceEntry.getKey().getName());
//	                			writer.append('\n');
//	                			final StackTraceElement[] stackTraceElements = stackTraceEntry.getValue();
//	                			for(StackTraceElement stackTraceElement:stackTraceElements){
//		                			writer.append("\tat ");
//		                			writer.append(stackTraceElement.toString());
//		                			writer.append('\n');
//	                			}
//	                		}
//		                	out.println(writer.toString());
//		                	out.flush();
//	                	}
//	                }
//	                System.exit(-1);                }
//                catch (IOException e) {
//                }
//                finally{
//                	if(out != null ) out.close();
//                }
//            }}, "ConsoleReader").start();
//		startAWTTerminationListener();
//    }
//
//	private void startAWTTerminationListener(){
//	    EventQueue.invokeLater(new Runnable(){
//
//			public void run() {
//				final Thread mainThread = Thread.currentThread();
//				new Thread(new Runnable(){
//					public void run() {
//						try {
//	                        mainThread.join();
//	                        String fileName = ResourceController.getResourceController().getFreeplaneUserDirectory() + File.separatorChar + "terminated.txt";
//	                        final PrintStream out = new PrintStream(new FileOutputStream(fileName, true));
//	                        out.println("Event Queue terminated on " + new Date().toString());
//	                        out.close();
//	                        startAWTTerminationListener();
//                        }
//                        catch (Exception e) {
//                        }
//		            }}, "AWTTerminationListener").start();
//            }});
//    }

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(final BundleContext context) throws Exception {
		parentLogger.removeHandler(handler);
	}
}
