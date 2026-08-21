package org.freeplane.main.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.net.URI;

import org.freeplane.core.util.Hyperlink;
import org.junit.Test;

public class BrowserTest {

    @Test
    public void usesWindowsShellLauncherOnlyForJpackageWindowsProcesses() {
        RecordingBrowser browser = new RecordingBrowser();
        browser.windows = true;

        withWindowsShellLauncherProperties("true", null, new Runnable() {
            @Override
            public void run() {
                assertThat(browser.shouldUseWindowsShellLauncher()).isTrue();
            }
        });
    }

    @Test
    public void doesNotUseWindowsShellLauncherInsideWindowsShellLauncherProcess() {
        RecordingBrowser browser = new RecordingBrowser();
        browser.windows = true;

        withWindowsShellLauncherProperties("true", "true", new Runnable() {
            @Override
            public void run() {
                assertThat(browser.shouldUseWindowsShellLauncher()).isFalse();
            }
        });
    }

    @Test
    public void routesOpenRequestsThroughWindowsShellLauncherWhenRequired() {
        Hyperlink link = hyperlink();
        RecordingBrowser browser = new RecordingBrowser();
        browser.useWindowsShellLauncher = true;

        browser.openDocument(link);

        assertThat(browser.helperLink).isSameAs(link);
        assertThat(browser.directLink).isNull();
    }

    @Test
    public void fallsBackToDirectOpenWhenWindowsShellLauncherCannotStart() {
        Hyperlink link = hyperlink();
        RecordingBrowser browser = new RecordingBrowser();
        browser.useWindowsShellLauncher = true;
        browser.windowsShellLauncherFailure = new IOException("boom");

        browser.openDocument(link);

        assertThat(browser.helperLink).isSameAs(link);
        assertThat(browser.directLink).isSameAs(link);
    }

    @Test
    public void opensDirectlyWhenWindowsShellLauncherIsNotRequired() {
        Hyperlink link = hyperlink();
        RecordingBrowser browser = new RecordingBrowser();

        browser.openDocument(link);

        assertThat(browser.helperLink).isNull();
        assertThat(browser.directLink).isSameAs(link);
    }

    @Test
    public void windowsShellLauncherBuildsClasspathFromBundleLibDirectory() {
        File baseDirectory = new File("installation");

        String classPath = Browser.WindowsShellLauncher.helperClassPath(baseDirectory);

        assertThat(classPath).isEqualTo(new File(
                new File(new File(new File(baseDirectory, "core"), "org.freeplane.core"), "lib"),
                "*").getPath());
    }

    @Test
    public void windowsShellLauncherRecreatesOriginalHyperlink() {
        String originalText = "file:////server/share/file%20name.mm";
        String uriText = "file://server/share/file%20name.mm";

        Hyperlink hyperlink = Browser.WindowsShellLauncher.hyperlink(new String[]{originalText, uriText});

        assertThat(hyperlink.toString()).isEqualTo(originalText);
        assertThat(hyperlink.getUri()).isEqualTo(URI.create(uriText));
    }

    private static Hyperlink hyperlink() {
        return new Hyperlink("file:///tmp/test.mm", URI.create("file:///tmp/test.mm"));
    }

    private static void withWindowsShellLauncherProperties(String launcherValue, String helperValue, Runnable test) {
        String previousLauncherValue = System.getProperty(Browser.WINDOWS_LAUNCHER_SET_DLL_DIRECTORY_PROPERTY);
        String previousHelperValue = System.getProperty(Browser.WINDOWS_SHELL_LAUNCHER_PROCESS_PROPERTY);
        try {
            setOrClearProperty(Browser.WINDOWS_LAUNCHER_SET_DLL_DIRECTORY_PROPERTY, launcherValue);
            setOrClearProperty(Browser.WINDOWS_SHELL_LAUNCHER_PROCESS_PROPERTY, helperValue);
            test.run();
        }
        finally {
            setOrClearProperty(Browser.WINDOWS_LAUNCHER_SET_DLL_DIRECTORY_PROPERTY, previousLauncherValue);
            setOrClearProperty(Browser.WINDOWS_SHELL_LAUNCHER_PROCESS_PROPERTY, previousHelperValue);
        }
    }

    private static void setOrClearProperty(String propertyName, String value) {
        if (value == null) {
            System.clearProperty(propertyName);
        }
        else {
            System.setProperty(propertyName, value);
        }
    }

    private static class RecordingBrowser extends Browser {
        boolean windows;
        boolean useWindowsShellLauncher;
        IOException windowsShellLauncherFailure;
        Hyperlink helperLink;
        Hyperlink directLink;

        @Override
        boolean isWindows() {
            return windows;
        }

        @Override
        boolean shouldUseWindowsShellLauncher() {
            return useWindowsShellLauncher || super.shouldUseWindowsShellLauncher();
        }

        @Override
        void openDocumentWithWindowsShellLauncher(Hyperlink link) throws IOException {
            helperLink = link;
            if (windowsShellLauncherFailure != null) {
                throw windowsShellLauncherFailure;
            }
        }

        @Override
        void openDocumentDirect(Hyperlink link) {
            directLink = link;
        }
    }
}
