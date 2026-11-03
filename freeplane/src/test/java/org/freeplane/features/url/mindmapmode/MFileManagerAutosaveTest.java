package org.freeplane.features.url.mindmapmode;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Timer;

import org.apache.commons.io.FileUtils;
import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.ui.ViewController;
import org.freeplane.features.url.UrlManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.MockedStatic;

public class MFileManagerAutosaveTest {
    private static final String PREVIOUS_CONTENTS = "previous map contents";
    private static final String CURRENT_CONTENTS = "current map contents";

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Controller previousController;
    private Controller controller;
    private MModeController modeController;
    private ViewController viewController;
    private Field backupDirectoryField;
    private File previousBackupDirectory;
    private File backupDirectory;
    private ResourceController resources;
    private MFileManager fileManager;
    private TestMapModel map;
    private File mapFile;

    @Before
    public void setUp() throws Exception {
        previousController = Controller.getCurrentController();
        controller = mock(Controller.class);
        modeController = mock(MModeController.class);
        MMapController mapController = mock(MMapController.class);
        viewController = mock(ViewController.class);
        ResourceBundles bundles = mock(ResourceBundles.class);
        when(bundles.getResourceString("automatically_save_message")).thenReturn("Saved {0}");
        resources = mock(ResourceController.class);
        when(resources.getResources()).thenReturn(bundles);
        when(controller.getResourceController()).thenReturn(resources);
        when(controller.getModeController()).thenReturn(modeController);
        when(controller.getMapViewManager()).thenReturn(mock(IMapViewManager.class));
        when(controller.getViewController()).thenReturn(viewController);
        when(modeController.getController()).thenReturn(controller);
        when(modeController.getMapController()).thenReturn(mapController);
        doAnswer(invocation -> {
            Runnable action = invocation.getArgument(0);
            action.run();
            return null;
        }).when(viewController).invokeLater(any(Runnable.class));
        when(resources.getProperty("default_save_dir")).thenReturn(temporaryFolder.getRoot().getAbsolutePath());
        when(resources.getBooleanProperty("automatically_overwrite_mindmap_files")).thenReturn(true);
        doCallRealMethod().when(mapController).mapSaved(any(MapModel.class), anyBoolean());
        Controller.setCurrentController(controller);

        fileManager = spy(new MFileManager(mapController));
        when(modeController.getExtension(UrlManager.class)).thenReturn(fileManager);
        // Preserve autosave file rotation without involving XML serialization.
        doAnswer(invocation -> {
            File file = invocation.getArgument(1);
            Files.write(file.toPath(), CURRENT_CONTENTS.getBytes(UTF_8));
            return null;
        }).when(fileManager).writeToFile(any(MapModel.class), any(File.class));

        backupDirectoryField = MFileManager.class.getDeclaredField("singleBackupDirectory");
        backupDirectoryField.setAccessible(true);
        previousBackupDirectory = (File) backupDirectoryField.get(null);
        backupDirectory = temporaryFolder.newFolder("backups");
        backupDirectoryField.set(null, backupDirectory);

        mapFile = temporaryFolder.newFile("map.mm");
        Files.write(mapFile.toPath(), PREVIOUS_CONTENTS.getBytes(UTF_8));
        map = new TestMapModel();
        map.setURL(mapFile.toURI().toURL());
        map.enableAutosave();
    }

    @After
    public void tearDown() throws Exception {
        try {
            if (map != null) {
                map.stopTimers();
            }
            if (backupDirectoryField != null) {
                backupDirectoryField.set(null, previousBackupDirectory);
            }
        }
        finally {
            Controller.setCurrentController(previousController);
        }
    }

    @Test
    public void autosavesOneChangeAfterAutosavingOneChange() throws Exception {
        makeChanges(1);
        fireAutosaveTimer();
        assertThat(map.isSaved()).isTrue();
        makeChanges(1);

        fireAutosaveTimer();

        assertThat(map.isSaved()).isTrue();
        verify(fileManager, times(2)).writeToFile(map, mapFile);
    }

    @Test
    public void autosavesTwoChangesAfterAutosavingTwoChanges() throws Exception {
        makeChanges(2);
        fireAutosaveTimer();
        assertThat(map.isSaved()).isTrue();
        makeChanges(2);

        fireAutosaveTimer();

        assertThat(map.isSaved()).isTrue();
        verify(fileManager, times(2)).writeToFile(map, mapFile);
    }

    @Test
    public void autosavesTwoChangesAfterAutosavingOneChange() throws Exception {
        makeChanges(1);
        fireAutosaveTimer();
        makeChanges(2);

        fireAutosaveTimer();

        assertThat(map.isSaved()).isTrue();
        verify(fileManager, times(2)).writeToFile(map, mapFile);
    }

    @Test
    public void autosavesChangeAfterIdleTimerCallback() throws Exception {
        makeChanges(1);
        fireAutosaveTimer();
        fireAutosaveTimer();
        makeChanges(1);

        fireAutosaveTimer();

        assertThat(map.isSaved()).isTrue();
        verify(fileManager, times(2)).writeToFile(map, mapFile);
    }

    @Test
    public void autosavesOneChangeAfterManualSave() throws Exception {
        makeChanges(1);
        assertThat(fileManager.save(map)).isTrue();
        makeChanges(1);

        fireAutosaveTimer();

        assertThat(map.isSaved()).isTrue();
        verify(fileManager, times(2)).writeToFile(map, mapFile);
    }

    @Test
    public void backupLeavesMapDirtyAndDoesNotRepeatWithoutChanges() throws Exception {
        when(resources.getBooleanProperty("automatically_overwrite_mindmap_files")).thenReturn(false);
        makeChanges(1);

        fireAutosaveTimer();
        fireAutosaveTimer();
        fireAutosaveTimer();

        assertThat(map.isSaved()).isFalse();
        assertThat(map.getNumberOfChangesSinceLastSave()).isEqualTo(1);
        verify(fileManager).writeToFile(eq(map), any(File.class));
    }

    @Test
    public void backupIncludesFurtherChanges() throws Exception {
        when(resources.getBooleanProperty("automatically_overwrite_mindmap_files")).thenReturn(false);
        makeChanges(1);
        fireAutosaveTimer();
        makeChanges(1);

        fireAutosaveTimer();

        assertThat(map.isSaved()).isFalse();
        assertThat(map.getNumberOfChangesSinceLastSave()).isEqualTo(2);
        verify(fileManager, times(2)).writeToFile(eq(map), any(File.class));
    }

    @Test
    public void failedSaveLeavesMapDirtyAndRecreatesAutosaveTimer() throws Exception {
        makeChanges(1);
        Timer previousTimer = map.getTimerForAutomaticSaving();
        doThrow(new IllegalStateException("write failed")).when(fileManager).writeToFile(map, mapFile);

        boolean saved = fileManager.save(map);

        assertThat(saved).isFalse();
        assertThat(map.isSaved()).isFalse();
        assertThat(map.getTimerForAutomaticSaving()).isNotSameAs(previousTimer);
    }

    @Test
    public void overwriteBacksUpPreviousContentsBeforeSavingCurrentMap() throws Exception {
        makeChanges(1);

        fireAutosaveTimer();

        assertThat(map.isSaved()).isTrue();
        assertThat(backupFile(1)).hasContent(PREVIOUS_CONTENTS);
        assertThat(mapFile).hasContent(CURRENT_CONTENTS);
        assertOnlyCurrentTimerIsRunning();
    }

    @Test
    public void moveCopiesAndDeletesSourceWhenRenameFails() throws Exception {
        File source = spy(mapFile);
        File destination = backupFile(1);
        doReturn(false).when(source).renameTo(destination);

        FileUtils.moveFile(source, destination);

        verify(source).renameTo(destination);
        assertThat(mapFile).doesNotExist();
        assertThat(destination).hasContent(PREVIOUS_CONTENTS);
    }

    @Test
    public void retriesFailedMoveWithoutAnotherEdit() throws Exception {
        File destination = backupFile(1);
        Timer previousTimer = map.getTimerForAutomaticSaving();
        makeChanges(1);
        try (MockedStatic<FileUtils> files = mockFileMoves()) {
            files.when(() -> FileUtils.moveFile(mapFile, destination))
                .thenThrow(new IOException("move failed"))
                .thenCallRealMethod();

            fireAutosaveTimer();

            assertThat(mapFile).hasContent(PREVIOUS_CONTENTS);
            assertThat(destination).doesNotExist();
            assertThat(map.isSaved()).isFalse();
            assertThat(map.getNumberOfChangesSinceLastSave()).isEqualTo(1);
            assertThat(map.getTimerForAutomaticSaving()).isSameAs(previousTimer);
            assertOnlyCurrentTimerIsRunning();
            verify(fileManager, never()).writeToFile(eq(map), any(File.class));

            fireAutosaveTimer();

            assertThat(map.isSaved()).isTrue();
            assertThat(destination).hasContent(PREVIOUS_CONTENTS);
            assertThat(mapFile).hasContent(CURRENT_CONTENTS);
            assertOnlyCurrentTimerIsRunning();
            files.verify(() -> FileUtils.moveFile(mapFile, destination), times(2));
        }
    }

    @Test
    public void retriesFailedBackupDirectoryCreationWithoutAnotherEdit() throws Exception {
        File blockingFile = temporaryFolder.newFile("blocking-parent");
        backupDirectory = new File(blockingFile, "backups");
        backupDirectoryField.set(null, backupDirectory);
        makeChanges(1);

        fireAutosaveTimer();

        assertThat(mapFile).hasContent(PREVIOUS_CONTENTS);
        assertThat(map.isSaved()).isFalse();
        assertOnlyCurrentTimerIsRunning();
        verify(fileManager, never()).writeToFile(eq(map), any(File.class));
        Files.delete(blockingFile.toPath());

        fireAutosaveTimer();

        assertThat(map.isSaved()).isTrue();
        assertThat(backupFile(1)).hasContent(PREVIOUS_CONTENTS);
        assertOnlyCurrentTimerIsRunning();
    }

    @Test
    public void retriesFailedBackupRotationWithoutAnotherEdit() throws Exception {
        map.numberOfFiles = 2;
        map.scheduleTimerForAutomaticSaving();
        File firstBackup = backupFile(1);
        Files.createDirectory(firstBackup.toPath());
        File blockingFile = new File(firstBackup, "blocking-child");
        Files.write(blockingFile.toPath(), new byte[0]);
        Files.write(backupFile(2).toPath(), "older backup".getBytes(UTF_8));
        makeChanges(1);

        fireAutosaveTimer();

        assertThat(mapFile).hasContent(PREVIOUS_CONTENTS);
        assertThat(map.isSaved()).isFalse();
        assertOnlyCurrentTimerIsRunning();
        verify(fileManager, never()).writeToFile(eq(map), any(File.class));
        Files.delete(blockingFile.toPath());
        Files.delete(firstBackup.toPath());

        fireAutosaveTimer();

        assertThat(map.isSaved()).isTrue();
        assertThat(backupFile(1)).hasContent("older backup");
        assertThat(backupFile(2)).hasContent(PREVIOUS_CONTENTS);
        assertOnlyCurrentTimerIsRunning();
    }

    @Test
    public void blockedBackupSlotKeepsOriginalAndRetriesWithoutAnotherEdit() throws Exception {
        File destination = backupFile(1);
        Files.createDirectory(destination.toPath());
        File blockingFile = new File(destination, "blocking-child");
        Files.write(blockingFile.toPath(), new byte[0]);
        makeChanges(1);

        fireAutosaveTimer();

        assertThat(mapFile).hasContent(PREVIOUS_CONTENTS);
        assertThat(blockingFile).exists();
        assertThat(map.isSaved()).isFalse();
        assertOnlyCurrentTimerIsRunning();
        verify(fileManager, never()).writeToFile(eq(map), any(File.class));
        Files.delete(blockingFile.toPath());
        Files.delete(destination.toPath());

        fireAutosaveTimer();

        assertThat(map.isSaved()).isTrue();
        assertThat(destination).hasContent(PREVIOUS_CONTENTS);
        assertOnlyCurrentTimerIsRunning();
    }

    @Test
    public void successfulOverwriteReportsSavedMapAndDoesNotRestartOldTimer() throws Exception {
        when(resources.getBooleanProperty("outputs_status_message_after_automatic_save", true)).thenReturn(true);
        Timer previousTimer = map.getTimerForAutomaticSaving();
        makeChanges(1);

        fireAutosaveTimer();

        verify(viewController).out("Saved " + mapFile);
        assertThat(previousTimer.isRunning()).isFalse();
        assertThat(map.getTimerForAutomaticSaving()).isNotSameAs(previousTimer);
        assertOnlyCurrentTimerIsRunning();
    }

    @Test
    public void successfulBackupReportsBackupFileAndDoesNotRestartOldTimer() throws Exception {
        when(resources.getBooleanProperty("automatically_overwrite_mindmap_files")).thenReturn(false);
        when(resources.getBooleanProperty("outputs_status_message_after_automatic_save", true)).thenReturn(true);
        Timer previousTimer = map.getTimerForAutomaticSaving();
        makeChanges(1);

        fireAutosaveTimer();

        verify(viewController).out("Saved " + backupFile(1));
        assertThat(map.isSaved()).isFalse();
        assertThat(previousTimer.isRunning()).isFalse();
        assertThat(map.getTimerForAutomaticSaving()).isNotSameAs(previousTimer);
        assertOnlyCurrentTimerIsRunning();
    }

    @Test
    public void failedOverwriteDoesNotReportSuccessOrRestartOldTimer() throws Exception {
        when(resources.getBooleanProperty("outputs_status_message_after_automatic_save", true)).thenReturn(true);
        Timer previousTimer = map.getTimerForAutomaticSaving();
        doThrow(new IllegalStateException("write failed")).when(fileManager).writeToFile(map, mapFile);
        makeChanges(1);

        fireAutosaveTimer();

        verify(viewController, never()).out(any());
        assertThat(map.isSaved()).isFalse();
        assertThat(backupFile(1)).hasContent(PREVIOUS_CONTENTS);
        assertThat(previousTimer.isRunning()).isFalse();
        assertThat(map.getTimerForAutomaticSaving()).isNotSameAs(previousTimer);
        assertOnlyCurrentTimerIsRunning();
    }

    @Test
    public void failedBackupDoesNotReportSuccessOrRestartOldTimer() throws Exception {
        when(resources.getBooleanProperty("automatically_overwrite_mindmap_files")).thenReturn(false);
        when(resources.getBooleanProperty("outputs_status_message_after_automatic_save", true)).thenReturn(true);
        Timer previousTimer = map.getTimerForAutomaticSaving();
        doThrow(new IllegalStateException("write failed"))
            .when(fileManager).writeToFile(eq(map), any(File.class));
        makeChanges(1);

        fireAutosaveTimer();

        verify(viewController, never()).out(any());
        assertThat(map.isSaved()).isFalse();
        assertThat(previousTimer.isRunning()).isFalse();
        assertThat(map.getTimerForAutomaticSaving()).isNotSameAs(previousTimer);
        assertOnlyCurrentTimerIsRunning();
    }

    @Test
    public void backupFailureDoesNotRestartSupersededTimer() throws Exception {
        Timer previousTimer = map.getTimerForAutomaticSaving();
        File destination = backupFile(1);
        makeChanges(1);
        try (MockedStatic<FileUtils> files = mockFileMoves()) {
            files.when(() -> FileUtils.moveFile(mapFile, destination)).thenAnswer(invocation -> {
                map.scheduleTimerForAutomaticSaving();
                throw new IOException("move failed after timer replacement");
            });

            fireAutosaveTimer();

            assertThat(mapFile).hasContent(PREVIOUS_CONTENTS);
            assertThat(map.isSaved()).isFalse();
            assertThat(previousTimer.isRunning()).isFalse();
            assertThat(map.getTimerForAutomaticSaving()).isNotSameAs(previousTimer);
            assertOnlyCurrentTimerIsRunning();
        }
    }

    @Test
    public void backupFailureDoesNotRestartClosedMapsTimer() throws Exception {
        Timer previousTimer = map.getTimerForAutomaticSaving();
        File destination = backupFile(1);
        makeChanges(1);
        try (MockedStatic<FileUtils> files = mockFileMoves()) {
            files.when(() -> FileUtils.moveFile(mapFile, destination)).thenAnswer(invocation -> {
                map.releaseResources();
                throw new IOException("move failed after map closure");
            });

            fireAutosaveTimer();

            assertThat(mapFile).hasContent(PREVIOUS_CONTENTS);
            assertThat(previousTimer.isRunning()).isFalse();
            assertThat(map.getTimerForAutomaticSaving()).isSameAs(previousTimer);
            assertThat(map.timers).noneMatch(Timer::isRunning);
        }
    }

    @Test
    public void inactiveMindmapModeDoesNotStopAutosaveTimer() throws Exception {
        makeChanges(1);
        when(controller.getModeController()).thenReturn(mock(ModeController.class));

        fireAutosaveTimer();

        assertThat(mapFile).hasContent(PREVIOUS_CONTENTS);
        assertThat(map.isSaved()).isFalse();
        assertOnlyCurrentTimerIsRunning();
        verify(fileManager, never()).writeToFile(eq(map), any(File.class));
    }

    @Test
    public void inactiveFileManagerDoesNotStopAutosaveTimer() throws Exception {
        makeChanges(1);
        when(modeController.getExtension(UrlManager.class)).thenReturn(mock(UrlManager.class));

        fireAutosaveTimer();

        assertThat(mapFile).hasContent(PREVIOUS_CONTENTS);
        assertThat(map.isSaved()).isFalse();
        assertOnlyCurrentTimerIsRunning();
        verify(fileManager, never()).writeToFile(eq(map), any(File.class));
    }

    private MockedStatic<FileUtils> mockFileMoves() {
        // Do not execute a real move while Mockito records the stub.
        return mockStatic(FileUtils.class, invocation -> {
            if (invocation.getMethod().getName().equals("moveFile")
                    && invocation.getMethod().getParameterCount() == 2) {
                return null;
            }
            return invocation.callRealMethod();
        });
    }

    private File backupFile(int number) {
        return MFileManager.backupFile(backupDirectory, mapFile, number, MFileManager.AlternativeFileMode.AUTOSAVE);
    }

    private void assertOnlyCurrentTimerIsRunning() {
        assertThat(map.getTimerForAutomaticSaving().isRunning()).isTrue();
        assertThat(map.timers).filteredOn(Timer::isRunning).containsExactly(map.getTimerForAutomaticSaving());
    }

    private void makeChanges(int count) {
        for (int i = 0; i < count; i++) {
            map.setSaved(false);
        }
    }

    private void fireAutosaveTimer() {
        Timer timer = map.getTimerForAutomaticSaving();
        timer.getActionListeners()[0].actionPerformed(
            new ActionEvent(timer, ActionEvent.ACTION_PERFORMED, "autosave"));
    }

    private static class TestMapModel extends MMapModel {
        private final List<Timer> timers = new ArrayList<>();
        private int numberOfFiles = 1;
        private Timer timer;

        private TestMapModel() {
            super(null);
        }

        @Override
        public void scheduleTimerForAutomaticSaving() {
            // Bypass the graphical-environment check, but keep real timer lifecycle behavior.
            if (timer != null) {
                timer.stop();
            }
            timer = new Timer(Integer.MAX_VALUE, new DoAutomaticSave(this, numberOfFiles, false));
            timers.add(timer);
            timer.start();
        }

        @Override
        public Timer getTimerForAutomaticSaving() {
            return timer;
        }

        private void stopTimers() {
            timers.forEach(Timer::stop);
        }
    }
}
