/*
 * Created on 16 Dec 2023
 *
 * author dimitry
 */
package org.freeplane.launcher;

import java.awt.Desktop;
import java.awt.desktop.OpenFilesEvent;
import java.awt.desktop.OpenFilesHandler;
import java.awt.desktop.OpenURIEvent;
import java.awt.desktop.OpenURIHandler;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class JavaLauncher {
    private static Desktop fmMacApplication;

    public static void main(String[] args) {
        System.setProperty("apple.awt.UIElement", "true");
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();

        List<String> jvmArguments = runtimeMxBean.getInputArguments();
        String classpath = System.getProperty("java.class.path");
        String javaBin = System.getProperty("java.home") + "/bin/java";
        String os = System.getProperty("os.name");

        // Construct command to execute the other Java program
        List<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-cp");
        command.add(classpath);
        jvmArguments.stream()
        .filter(arg -> ! arg.startsWith(" -Xmx"))
        .forEach(command::add);

        command.add("-Xmx3g");

        try {

            if(os.startsWith("Mac OS")) {
                String path = new File(Launcher.getFreeplaneInstallationDirectory(), "../Resources/Freeplane.icns").getAbsolutePath();
                command.add("-Xdock:icon=" + path);
                command.add("-Xdock:name=Freeplane");
            }

            command.add(Launcher.class.getName());
            Stream.of(args).forEach(command::add);

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            if(fmMacApplication == null) {
                fmMacApplication = Desktop.getDesktop();
                fmMacApplication.setOpenFileHandler(new OpenFilesHandler() {

                    @Override
                    public void openFiles(OpenFilesEvent e) {
                        String[] files = e.getFiles().stream().map(File::getAbsolutePath).toArray(String[]::new);
                        main(files);
                    }
                });
                fmMacApplication.setOpenURIHandler(new  OpenURIHandler() {

                    @Override
                    public void openURI(OpenURIEvent e) {
                        String uri = e.getURI().toString();
                        main(new String[] {uri});
                    }
                });
            }
            process.waitFor();

        } catch (IOException  | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
