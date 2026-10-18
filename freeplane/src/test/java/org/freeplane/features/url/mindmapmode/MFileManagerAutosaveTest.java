package org.freeplane.features.url.mindmapmode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Files;

import javax.swing.Timer;

import org.freeplane.core.resources.ResourceBundles;
import org.freeplane.core.resources.ResourceController;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.map.mindmapmode.MMapController;
import org.freeplane.features.map.mindmapmode.MMapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.mindmapmode.MModeController;
import org.freeplane.features.ui.IMapViewManager;
import org.freeplane.features.url.UrlManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class MFileManagerAutosaveTest {
    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    private Controller previousController;
    private ResourceController resources;
    private MFileManager fileManager;
    private TestMapModel map;
    private File mapFile;

    @Before
    public void setUp() throws Exception {
        previousController = Controller.getCurrentController();
        Controller controller = mock(Controller.class);
        MModeController modeController = mock(MModeController.class);
        MMapController mapController = mock(MMapController.class);
        resources = mock(ResourceController.class);
        when(resources.getResources()).thenReturn(mock(ResourceBundles.class));
        when(controller.getResourceController()).thenReturn(resources);
        when(controller.getModeController()).thenReturn(modeController);
        when(controller.getMapViewManager()).thenReturn(mock(IMapViewManager.class));
        when(modeController.getMapController()).thenReturn(mapController);
        when(resources.getProperty("default_save_dir")).thenReturn(temporaryFolder.getRoot().getAbsolutePath());
        when(resources.getBooleanProperty("automatically_overwrite_mindmap_files")).thenReturn(true);
        doCallRealMethod().when(mapController).mapSaved(any(MapModel.class), anyBoolean());
        Controller.setCurrentController(controller);

        fileManager = spy(new MFileManager(mapController));
        when(modeController.getExtension(UrlManager.class)).thenReturn(fileManager);
        // Preserve autosave file rotation without involving XML serialization.
        doAnswer(invocation -> {
            File file = invocation.getArgument(1);
            Files.write(file.toPath(), new byte[0]);
            return null;
        }).when(fileManager).writeToFile(any(MapModel.class), any(File.class));

        mapFile = temporaryFolder.newFile("map.mm");
        map = new TestMapModel();
        map.setURL(mapFile.toURI().toURL());
        map.scheduleTimerForAutomaticSaving();
    }

    @After
    public void tearDown() {
        Controller.setCurrentController(previousController);
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
        private Timer timer;

        private TestMapModel() {
            super(null);
        }

        @Override
        public void scheduleTimerForAutomaticSaving() {
            // Callbacks are fired explicitly so these tests also work headlessly.
            timer = new Timer(60000, new DoAutomaticSave(this, 1, false));
        }

        @Override
        public Timer getTimerForAutomaticSaving() {
            return timer;
        }
    }
}
