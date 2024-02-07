package org.freeplane.plugin.codeexplorer.archunit;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.google.gson.Gson;
import com.tngtech.archunit.freeplane.extension.EvaluatedRuleDto;

public class ArchUnitExtensionServer {
    private volatile ServerSocket serverSocket;
    private final ConcurrentLinkedQueue<EvaluatedRuleDto> submittedConfigurations;
    private final ExecutorService clientExecutor;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public ArchUnitExtensionServer() {
        this.submittedConfigurations = new ConcurrentLinkedQueue<>();
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
            try (InputStream in = clientSocket.getInputStream();
                 Reader reader = new InputStreamReader(in, StandardCharsets.UTF_8)) {
                EvaluatedRuleDto dto = new Gson().fromJson(reader, EvaluatedRuleDto.class);
                submittedConfigurations.add(dto);
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
    }
}