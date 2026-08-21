package org.freeplane.main.application;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.Compat;
import org.freeplane.core.util.Hyperlink;
import org.freeplane.core.util.LogUtils;
import org.freeplane.features.mode.Controller;

import com.sun.jna.Native;
import com.sun.jna.WString;
import com.sun.jna.platform.win32.Shell32;
import com.sun.jna.platform.win32.WinUser;
import com.sun.jna.win32.StdCallLibrary;

public class Browser {
    static final String WINDOWS_LAUNCHER_SET_DLL_DIRECTORY_PROPERTY = "org.freeplane.windows.launcher.setDllDirectory";
    static final String WINDOWS_SHELL_LAUNCHER_PROCESS_PROPERTY =
        "org.freeplane.main.application.Browser.windowsShellLauncherProcess";

    public void openDocument(final Hyperlink link) {
        if (shouldUseWindowsShellLauncher()) {
            try {
                openDocumentWithWindowsShellLauncher(link);
                return;
            }
            catch (IOException e) {
                LogUtils.warn("Failed to start Windows browser helper", e);
            }
        }
        openDocumentDirect(link);
    }

    boolean shouldUseWindowsShellLauncher() {
        return isWindows()
                && Boolean.getBoolean(WINDOWS_LAUNCHER_SET_DLL_DIRECTORY_PROPERTY)
                && !Boolean.getBoolean(WINDOWS_SHELL_LAUNCHER_PROCESS_PROPERTY);
    }

    boolean isWindows() {
        return Compat.isWindowsOS();
    }

    void openDocumentWithWindowsShellLauncher(final Hyperlink link) throws IOException {
        WindowsShellLauncher.open(link);
    }

    void openDocumentDirect(final Hyperlink link) {
        final URI uri = preprocessUri(link);
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop desktop = Desktop.getDesktop();

                String scheme = uri.getScheme();
                if ("file".equalsIgnoreCase(scheme)) {
                    if (desktop.isSupported(Desktop.Action.OPEN)) {
                        desktop.open(new File(uri));
                        return;
                    }
                }
                else {
                    if ("mailto".equalsIgnoreCase(scheme) && desktop.isSupported(Desktop.Action.MAIL)) {
                        desktop.mail(uri);
                        return;
                    }
                    if ((!Compat.isWindowsOS() || "https".equalsIgnoreCase(scheme)
                            || "http".equalsIgnoreCase(scheme)) && desktop.isSupported(Desktop.Action.BROWSE)) {
                        desktop.browse(uri);
                        return;
                    }
                }
            }
        }
        catch (Exception ignored) {
            LogUtils.warn(ignored);
        }
        openWithPlatformFallback(uri);
    }

    private URI preprocessUri(Hyperlink link) {
        try {
            String uriString = normalizeUncPrefix(link.toString());

            if (!uriString.equals(link.toString())) {
                return new URI(uriString);
            }

            if ("smb".equalsIgnoreCase(link.getScheme()) && Compat.isWindowsOS()) {
                String unc = Compat.smbUri2unc(link.getUri());
                return new File(unc).toURI();
            }

            return normalizeUri(link.getUri());
        }
        catch (Exception e) {
            return link.getUri();
        }
    }

    private String normalizeUncPrefix(String uriString) {
        final String UNC_PREFIX = "file:////";
        if (uriString.startsWith(UNC_PREFIX)) {
            return "file://" + uriString.substring(UNC_PREFIX.length());
        }
        return uriString;
    }

    private URI normalizeUri(URI uri) throws Exception {
        if (uri == null) {
            return null;
        }

        String rawPath = uri.getRawPath();
        if (rawPath == null) {
            return uri;
        }

        if (isProperlyEncoded(rawPath)) {
            return uri;
        }

        return new URI(uri.getScheme(), uri.getAuthority(), uri.getPath(), uri.getQuery(), uri.getFragment());
    }

    private boolean isProperlyEncoded(String path) {
        try {
            String decoded = URLDecoder.decode(path, StandardCharsets.UTF_8.name());
            String[] segments = decoded.split("/", -1);
            StringBuilder reencoded = new StringBuilder();

            for (int i = 0; i < segments.length; i++) {
                if (i > 0) {
                    reencoded.append("/");
                }
                if (!segments[i].isEmpty()) {
                    reencoded.append(URLEncoder.encode(segments[i], StandardCharsets.UTF_8.name())
                                               .replace("+", "%20"));
                }
            }

            return path.equals(reencoded.toString());
        }
        catch (Exception e) {
            return false;
        }
    }

    private void openWithPlatformFallback(URI uri) {
        String uriString = normalizeUncPrefix(uri.toString());

        String scheme = uri.getScheme();
        try {
            if (Compat.isWindowsOS()) {
                Shell32.INSTANCE.ShellExecute(
                        null,
                        "open",
                        uriString,
                        null,
                        null,
                        WinUser.SW_SHOWNORMAL
                );
            }
            else if (Compat.isMacOsX()) {
                if ("file".equalsIgnoreCase(scheme)) {
                    uriString = uri.getPath();
                }
                Controller.exec(new String[]{"open", uriString});
            }
            else {
                Controller.exec(new String[]{"xdg-open", uriString});
            }
        }
        catch (IOException ex) {
            System.err.println("Caught: " + ex);
        }
    }

    public static class WindowsShellLauncher {
        private static final int REQUIRED_ARGUMENT_COUNT = 2;

        static void open(Hyperlink link) throws IOException {
            final String installationBaseDirectory = ResourceController.getResourceController().getInstallationBaseDir();
            if (installationBaseDirectory == null || installationBaseDirectory.isEmpty()) {
                throw new IOException("Missing installation base directory");
            }

            new ProcessBuilder(
                    javaCommand().getPath(),
                    "-D" + WINDOWS_SHELL_LAUNCHER_PROCESS_PROPERTY + "=true",
                    "-cp",
                    helperClassPath(new File(installationBaseDirectory)),
                    WindowsShellLauncher.class.getName(),
                    link.toString(),
                    link.getUri().toString()
            ).start();
        }

        static String helperClassPath(File baseDirectory) {
            return new File(bundleLibDirectory(baseDirectory), "*").getPath();
        }

        static Hyperlink hyperlink(String[] args) {
            try {
                return new Hyperlink(args[0], new URI(args[1]));
            }
            catch (URISyntaxException e) {
                throw new IllegalArgumentException(e);
            }
        }

        private static File bundleLibDirectory(File baseDirectory) {
            return new File(new File(new File(baseDirectory, "core"), "org.freeplane.core"), "lib");
        }

        private static File javaCommand() {
            File binDirectory = new File(System.getProperty("java.home"), "bin");
            File javaw = new File(binDirectory, "javaw.exe");
            return javaw.isFile() ? javaw : new File(binDirectory, "java.exe");
        }

        public static void main(String[] args) {
            if (args.length != REQUIRED_ARGUMENT_COUNT) {
                System.exit(2);
            }

            if (!Kernel32.INSTANCE.SetDllDirectoryW(null)) {
                System.exit(3);
            }

            try {
                new Browser().openDocumentDirect(hyperlink(args));
                System.exit(0);
            }
            catch (RuntimeException e) {
                System.exit(4);
            }
        }

        private interface Kernel32 extends StdCallLibrary {
            Kernel32 INSTANCE = Native.load("kernel32", Kernel32.class);

            boolean SetDllDirectoryW(WString path);
        }
    }
}
