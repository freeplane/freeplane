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

import com.google.gson.Gson;
import com.tngtech.archunit.freeplane.extension.ArchTestResult;

public class ArchUnitServer {
    private volatile ServerSocket serverSocket;
    private final LinkedList<ArchTestResult> submittedTestResults;
    private final ExecutorService clientExecutor;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Consumer<ArchTestResult> callback = result -> {/**/};

    public ArchUnitServer() {
        this.submittedTestResults = new LinkedList<>();
        this.clientExecutor = Executors.newCachedThreadPool();

        // Shutdown hook for cleaning up resources on application termination
        Runtime.getRuntime().addShutdownHook(new Thread(this::cleanup));
    }

    public void start(int port) {
        if (running.compareAndSet(false, true)) {
            try {
                serverSocket = new ServerSocket(port);
                System.out.println("Server started on port: " + port);

                new Thread(() -> {
                    while (running.get()) {
                        try {
                            Socket clientSocket = serverSocket.accept();
                            clientExecutor.submit(new ClientHandler(clientSocket));
                        } catch (IOException e) {
                            if (!running.get()) {
                                System.out.println("Server is stopping...");
                            } else {
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
        } else {
            System.out.println("Server is already running.");
        }
    }

    public void stop() {
        if (running.compareAndSet(true, false)) {
            try {
                if (serverSocket != null && !serverSocket.isClosed()) {
                    serverSocket.close();
                }
                System.out.println("Server stopped.");
            } catch (IOException e) {
                System.out.println("Error closing the server: " + e.getMessage());
            }
        } else {
            System.out.println("Server is not running.");
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
                ArchTestResult dto = new Gson().fromJson(reader, ArchTestResult.class);
                EventQueue.invokeLater(() -> addTestResult(dto));
            } catch (IOException e) {
                System.out.println("Client handler exception: " + e.getMessage());
            } finally {
                try {
                    clientSocket.close();
                } catch (IOException e) {
                    System.out.println("Error closing client socket: " + e.getMessage());
                }
            }
        }

        private void addTestResult(ArchTestResult dto) {
            submittedTestResults.add(dto);
            callback.accept(dto);
        }
    }

    public List<ArchTestResult> getSubmittedTestResults() {
        return submittedTestResults;
    }

    public Consumer<ArchTestResult> getCallback() {
        return callback;
    }

    public void setCallback(Consumer<ArchTestResult> callback) {
        this.callback = callback;
    }
}