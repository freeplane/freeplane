package org.freeplane.main.application;

import org.apache.commons.lang.StringUtils;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.LogUtils;
import org.freeplane.main.application.CommandLineParser.Options;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

public class SingleInstanceManager {
	private File lockFile = new File(Compat.getApplicationUserDirectory(), "single_instance.lock");
	private boolean isSingleInstanceMode;
	private boolean isSingleInstanceForceMode;
	private Integer port;
	private boolean isSlave;
	private boolean isMasterPresent;
    final private FreeplaneStarter starter;

	public SingleInstanceManager(FreeplaneStarter starter, boolean runsHeadless) {
	    this.starter = starter;
	    final ResourceController resourceController = starter.getResourceController();
		isSingleInstanceMode = !runsHeadless && resourceController.getBooleanProperty("single_instance");
		isSingleInstanceForceMode =!runsHeadless && resourceController.getBooleanProperty("single_instance_force");
	}

	public void start(String[] args) {
        final Options options = CommandLineParser.parse(args, false);
        final String[] filesToLoad = options.getFilesToOpenAsArray();
		if (isSingleInstanceMode && !options.hasMenuItemsToExecute()) {
			initLockFile();
			if (filesToLoad.length == 0 && !isSingleInstanceForceMode && checkIsMasterPresent()) {
				isMasterPresent = true;
				startStandAlone();
			}
			else if (!startAsSlave(filesToLoad)) {
				if (!startAsMaster()) {
					startStandAlone();
				}
			}
		}
		else {
			startStandAlone();
		}
	}

	private boolean checkIsMasterPresent() {
		if (port == null)
			return false;
		try {
			Socket clientSocket = new Socket("localhost", port);
			clientSocket.close();
			LogUtils.info("master is present.");
			return true;
		}
		catch (Exception e) {
			// this is only a check - we'll log later
			return false;
		}
	}

	public boolean isSlave() {
		return isSlave;
	}

	public boolean isMasterPresent() {
		return isSlave || isMasterPresent;
	}

	private boolean startAsSlave(String[] filesToLoad) {
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
			Socket clientSocket = new Socket("localhost", port);
			OutputStream out = clientSocket.getOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(out);
			oos.writeObject(filesToLoad);
			oos.close();
			clientSocket.close();
			LogUtils.info("Successfully notified first instance.");
			return true;
		}
		catch (Exception e) {
			LogUtils.warn("Error connecting to existing instance (stale lockfiles may cause this).", e);
			return false;
		}
	}

	private boolean startAsMaster() {
		try {
			// port number 0: use any free socket
			final ServerSocket socket = new ServerSocket(0, 10, InetAddress.getByName(null));
			port = socket.getLocalPort();
			LogUtils.info("Listening for application instances on socket " + port);
			createLockFile();
			Thread filesToOpenListenerThread = new Thread(new Runnable() {
				@Override
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
                                in.close();
                                client.close();
								starter.loadMapsLater(filesToLoadForClient);
							}
							catch (SecurityException e) {
							    // this happens when the master is currently executing a script
							    LogUtils.warn("master is currently not accepting new files. Try again later", e);
							}
							catch (IOException e) {
							    socketClosed = true;
							}
							catch (ClassNotFoundException e) {
								// this should never happen
								throw new RuntimeException("implementation error", e);
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
			@Override
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

	private void startStandAlone() {
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
