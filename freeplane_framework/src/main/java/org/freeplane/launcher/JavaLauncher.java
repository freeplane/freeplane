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
    private static int getMajorJavaVersion() {
        String versionString = System.getProperty("java.version");
        int start = 0;
        int end = versionString.indexOf('.');
        if(versionString.startsWith("1.")) {
        	start = end + 1;
			end = versionString.indexOf('.', start);
		}
        return Integer.parseInt(versionString.substring(start, end));
    }
    public static void main(String[] args) {
    	String os = System.getProperty("os.name");
        if(os.startsWith("Mac OS")) {
        	System.setProperty("apple.awt.UIElement", "true");
        }
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();

        List<String> jvmArguments = runtimeMxBean.getInputArguments();
        String classpath = System.getProperty("java.class.path");
        String javaBin = System.getProperty("java.home") + "/bin/java";
        

        // Construct command to execute the other Java program
        List<String> command = new ArrayList<>();
        command.add(javaBin);
        command.add("-cp");
        command.add(classpath);
        jvmArguments.stream()
        .forEach(command::add);
        final int javaVersion = getMajorJavaVersion();
        if(javaVersion > 8) {
            command.add("--add-exports=java.desktop/sun.awt.shell=ALL-UNNAMED");
            command.add("--add-exports=java.desktop/com.sun.java.swing.plaf.windows=ALL-UNNAMED");
        }
        if(javaVersion > 17) {
        	command.add("-Djava.security.manager=allow");
        }
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
            if(os.startsWith("Mac OS")) {
            	MacAppConfigurer.configureMacApp();
            	process.waitFor();
            }

        } catch (IOException  | InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class MacAppConfigurer {
    private static Desktop fmMacApplication;
	static void configureMacApp() {
		if(fmMacApplication == null) {
			fmMacApplication = Desktop.getDesktop();
			fmMacApplication.setOpenFileHandler(new OpenFilesHandler() {

				@Override
				public void openFiles(OpenFilesEvent e) {
					String[] files = e.getFiles().stream().map(File::getAbsolutePath).toArray(String[]::new);
					JavaLauncher.main(files);
				}
			});
			fmMacApplication.setOpenURIHandler(new  OpenURIHandler() {

				@Override
				public void openURI(OpenURIEvent e) {
					String uri = e.getURI().toString();
					JavaLauncher.main(new String[] {uri});
				}
			});
		}
	}

}
