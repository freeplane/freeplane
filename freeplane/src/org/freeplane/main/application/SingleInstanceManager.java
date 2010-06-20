package org.freeplane.main.application;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.controller.Controller;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.n3.nanoxml.XMLParseException;

public class SingleInstanceManager {
	private File lockFile = new File(FreeplaneStarter.getFreeplaneUserDirectory(), "single_instance.lock");
	private boolean isSingleInstanceMode;
	private boolean isSingleInstanceForceMode;
	private Integer port;
	private boolean isSlave;
	private Controller controller;

	public SingleInstanceManager() {
		// FIXME: temporary!
		ResourceController.setResourceController(new ApplicationResourceController());
		isSingleInstanceMode = ResourceController.getResourceController().getBooleanProperty("single_instance");
		isSingleInstanceForceMode = ResourceController.getResourceController().getBooleanProperty(
		    "single_instance_force");
	}

	public void start(String[] filesToLoad) {
		if (isSingleInstanceMode) {
			if (!startAsSlave(filesToLoad)) {
				if (!startAsMaster(filesToLoad)) {
					startStandAlone(filesToLoad);
				}
			}
		}
		else {
			startStandAlone(filesToLoad);
		}
	}

	public boolean isSlave() {
		return isSlave;
	}

	public void setController(Controller controller) {
		this.controller = controller;
    }

	private boolean startAsSlave(String[] filesToLoad) {
		initLockFile();
		if (filesToLoad.length == 0 && !isSingleInstanceForceMode)
			return false;
		if (port != null) {
			isSlave = openFilesInMaster(filesToLoad);
			return isSlave;
		}
		return false;
	}

	private boolean openFilesInMaster(String[] filesToLoad) {
		if (port == null)
			throw new IllegalArgumentException("port may not be null");
		try {
			Socket clientSocket = new Socket(InetAddress.getLocalHost(), port);
			OutputStream out = clientSocket.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(out);
			oos.writeObject(filesToLoad);
			oos.close();
			clientSocket.close();
			LogUtils.info("Successfully notified first instance.");
			return true;
		}
		catch (UnknownHostException e) {
			LogUtils.severe(e.getMessage(), e);
			return false;
		}
		catch (IOException e) {
			LogUtils.severe("Error connecting to local port for single instance notification", e);
			return false;
		}
	}

	private boolean startAsMaster(String[] filesToLoad) {
		try {
			// port number 0: use any free socket
			final ServerSocket socket = new ServerSocket(0, 10, InetAddress.getLocalHost());
			port = socket.getLocalPort();
			LogUtils.info("Listening for application instances on socket " + port);
			createLockFile();
			Thread filesToOpenListenerThread = new Thread(new Runnable() {
				public void run() {
					boolean socketClosed = false;
					while (!socketClosed) {
						if (socket.isClosed()) {
							socketClosed = true;
						}
						else {
							try {
								Socket client = socket.accept();
								ObjectInputStream in = new ObjectInputStream(client.getInputStream());
								String[] filesToLoadForClient = (String[]) in.readObject();
								LogUtils.info("opening '" + StringUtils.join(filesToLoadForClient, "', '")
								        + "' for client");
								for (String file : filesToLoadForClient) {
									controller.getModeController().getMapController().newMap(Compat.fileToUrl(new File(file)));
                                }
								UITools.getFrame().toFront();
								in.close();
								client.close();
							}
							catch (IOException e) {
								socketClosed = true;
							}
							catch (ClassNotFoundException e) {
								// this should never happen
								throw new RuntimeException("implementation error", e);
							}
                            catch (XMLParseException e) {
                            	LogUtils.severe("cannot open input file: " + e.getMessage(), e);
                            }
                            catch (URISyntaxException e) {
                            	LogUtils.severe("invalid file: " + e.getMessage(), e);
                            }
						}
					}
				}
			});
			filesToOpenListenerThread.start();
			return true;
			// listen
		}
		catch (UnknownHostException e) {
			LogUtils.severe(e.getMessage(), e);
			return false;
		}
		catch (IOException e) {
			LogUtils.severe(e.getMessage(), e);
			return false;
		}
	}

	private void createLockFile() throws IOException {
		final RandomAccessFile randomAccessLockFile = new RandomAccessFile(lockFile, "rwd");
		randomAccessLockFile.writeBytes(port.toString());
		randomAccessLockFile.close();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					lockFile.delete();
				}
				catch (Exception e) {
					error("Unable to remove lock file: " + lockFile, e);
				}
			}
		});
	}

	private void startStandAlone(String[] filesToLoad) {
		// do nothing - whatever is needed will happen later
	}

	/**
	 * opens the lock file and tries to get a lock for it.
	 * If it is locked already then try to read the port number from it.
	 */
	private boolean initLockFile() {
		try {
			if (lockFile.exists()) {
				// slave: read the port from the file
				final RandomAccessFile randomAccessLockFile = new RandomAccessFile(lockFile, "r");
				String portAsString = randomAccessLockFile.readLine().trim();
				randomAccessLockFile.close();
				port = Integer.parseInt(portAsString);
			}
		}
		catch (Exception e) {
			error("Unable to create and/or lock file: " + lockFile, e);
		}
		return false;
	}

	private void error(String message, Exception e) {
		LogUtils.severe(message, e);
	}
}
