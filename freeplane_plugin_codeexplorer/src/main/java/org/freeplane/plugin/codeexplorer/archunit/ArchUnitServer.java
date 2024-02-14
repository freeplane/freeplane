package org.freeplane.plugin.codeexplorer.archunit;

import java.awt.EventQueue;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

import org.freeplane.core.resources.IFreeplanePropertyListener;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;

import com.google.gson.Gson;
import com.tngtech.archunit.freeplane.extension.ArchitectureViolations;
public class ArchUnitServer implements IFreeplanePropertyListener {
    public static final String ARCHUNIT_SERVER_ENABLED_PROPERTY = "code.archunit.server.enabled";
    public static final String ARCHUNIT_SERVER_PORT_PROPERTY = "code.archunit.server.port";
    private volatile ServerSocket serverSocket;
    private final LinkedList<ArchitectureViolations> submittedTestResults;
    private final ExecutorService clientExecutor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Consumer<ArchitectureViolations> callback = result -> {/**/};

    public ArchUnitServer() {
        this.submittedTestResults = new LinkedList<>();
        this.clientExecutor = Executors.newCachedThreadPool();
        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanup));
    }

    private void start() {
        final int port = ResourceController.getResourceController().getIntProperty(ARCHUNIT_SERVER_PORT_PROPERTY, 6297);
        start(port);
    }

    private void start(int port) {
        if (running.compareAndSet(false, true)) {
            try {
                serverSocket = new ServerSocket(port);
                LogUtils.info("ArchUnit Server started on port " + port);

                new Thread(() -> {
                    while (running.get()) {
                        try {
                            Socket clientSocket = serverSocket.accept();
                            clientExecutor.submit(new ClientHandler(clientSocket));
                        } catch (IOException e) {
                            if (running.get()) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
            } catch (BindException e) {
                running.set(false);
            } catch (IOException e) {
                e.printStackTrace();
                running.set(false);
            }
        }
    }

    public void stop() {
        if (running.compareAndSet(true, false)) {
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
                LogUtils.info("ArchUnit Server stopped.");
            } catch (IOException e) {
                LogUtils.severe("Error closing the ArchUnit server: " + e.getMessage());
            }
        }
    }

    // Cleanup method for shutdown hook and stop method reuse
    private void cleanup() {
        stop(); // Ensure server socket is closed
        clientExecutor.shutdown(); // Disable new tasks from being submitted
        try {
            if (!clientExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                clientExecutor.shutdownNow(); // Cancel currently executing tasks
            }
        } catch (InterruptedException e) {
            clientExecutor.shutdownNow();
        }
    }

    private class ClientHandler implements Runnable {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        @Override
        public void run() {
            try (InputStream inputStream = clientSocket.getInputStream();
                 GZIPInputStream gzipInputStream = new GZIPInputStream(inputStream);
                 Reader reader = new InputStreamReader(gzipInputStream, StandardCharsets.UTF_8)) {
                ArchitectureViolations dto = new Gson().fromJson(reader, ArchitectureViolations.class);
                EventQueue.invokeLater(() -> addTestResult(dto));
            } catch (IOException e) {
                LogUtils.severe("ArchUnit Client handler exception: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    LogUtils.severe("Error closing ArchUnit client socket: " + e.getMessage());
                }
            }
        }

        private void addTestResult(ArchitectureViolations dto) {
            submittedTestResults.add(dto);
            callback.accept(dto);
        }
    }

    public List<ArchitectureViolations> getSubmittedTestResults() {
        return submittedTestResults;
    }

    public Consumer<ArchitectureViolations> getCallback() {
        return callback;
    }

    public void setCallback(Consumer<ArchitectureViolations> callback) {
        this.callback = callback;
    }

    @Override
    public void propertyChanged(String propertyName, String newValue, String oldValue) {
        if(propertyName.equals(ARCHUNIT_SERVER_ENABLED_PROPERTY)) {
            if(Boolean.parseBoolean(newValue))
                start();
            else
                stop();
        }
    }
}