package org.freeplane.features.styles.mindmapmode;

import java.awt.Frame;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JTextField;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Hyperlink;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.mode.Controller;
import org.freeplane.features.mode.ModeController;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.MFileManager;
import org.freeplane.features.url.mindmapmode.TemplateManager;
import org.freeplane.view.swing.features.filepreview.MindMapPreviewWithOptions;

import com.jgoodies.forms.builder.FormBuilder;

class ManageAssociatedMindMapsDialog{
    private final MapModel map;
    private final JDialog dialog;
    private URI followedMapLocation;
    private URI associatedMapLocation;
    ManageAssociatedMindMapsDialog(String title, final MapModel map){
        this.map = map;
        Frame owner = UITools.getCurrentFrame();
        dialog = new JDialog(owner);
        FormBuilder formBuilder = FormBuilder.create().columns("p, 3dlu, p")
                .rows("p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p");
        JTextField followedMapField = new JTextField(80);
        followedMapLocation = updateAssociatedMap(MapStyleModel.FOLLOWED_TEMPLATE_LOCATION_PROPERTY, followedMapField, TextUtils.getText("no_map_followed"));      
        formBuilder.add(followedMapField).xy(1, 1);
        JButton changeFollowedMapButton = createChangeMapButton(MapStyleModel.FOLLOWED_TEMPLATE_LOCATION_PROPERTY, followedMapField);
        formBuilder.add(changeFollowedMapButton).xy(3, 1);
        JButton openFollowedMapButton = createOpenButton(followedMapLocation);
        JButton copyStylesFromFollowedMapButton = createCopyStylesButton(followedMapLocation);
        JButton replaceStylesFromFollowedMapButton = createReplaceStylesButton(followedMapLocation);
        JButton unfolowButton = createUnfollowButton(followedMapField);
        formBuilder.addBar(openFollowedMapButton, copyStylesFromFollowedMapButton, replaceStylesFromFollowedMapButton, unfolowButton).xyw(1, 3, 3);
        
        JTextField associatedMapField = new JTextField(80);
        associatedMapLocation = updateAssociatedMap(MapStyleModel.ASSOCIATED_TEMPLATE_LOCATION_PROPERTY, associatedMapField, "???");      
        formBuilder.add(associatedMapField).xy(1, 5);
        JButton changeAssosiatedMapButton = createChangeMapButton(MapStyleModel.ASSOCIATED_TEMPLATE_LOCATION_PROPERTY, associatedMapField);
        formBuilder.add(changeAssosiatedMapButton).xy(3, 5);
        JButton openAssociatedMapButton = createOpenButton(associatedMapLocation);
        JButton copyStylesFromAssociatedMapButton = createCopyStylesButton(associatedMapLocation);
        JButton replaceStylesFromAssociatedMapButton = createReplaceStylesButton(associatedMapLocation);
        formBuilder.addBar(openAssociatedMapButton, copyStylesFromAssociatedMapButton, replaceStylesFromAssociatedMapButton).xyw(1, 7, 3);
        
        JButton closeButton = createCloseMapButton();
        formBuilder.add(closeButton).xy(1, 9, "left");
        dialog.setModal(true);
        dialog.setTitle(title);
        dialog.getContentPane().add(formBuilder.build());
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
    }
    private JButton createUnfollowButton(JTextField followedMapField) {
        JButton unfolowButton = new JButton(TextUtils.getText("unfollow"));
        unfolowButton.addActionListener(e -> {
            MapStyle.getController().setProperty(map, MapStyleModel.FOLLOWED_TEMPLATE_LOCATION_PROPERTY, null);
            MapStyle.getController().setProperty(map, MapStyleModel.FOLLOWED_MAP_LAST_TIME, null);
            followedMapField.setText("");
        });
        return unfolowButton;
    }
    private JButton createCloseMapButton() {
        JButton closeButton = new JButton(TextUtils.getText("close"));
        closeButton.addActionListener(e -> {
            dialog.setVisible(false);
        });
        return closeButton;
    }
    private JButton createReplaceStylesButton(URI sourceLocation) {
        JButton replaceStylesFromFollowedMapButton = new JButton(TextUtils.getText("replaceStyles"));
        replaceStylesFromFollowedMapButton.addActionListener(e -> {
            try {
                MapStyle.getController().replaceStyles(sourceLocation, map, false, false);
                dialog.setVisible(false);
            } catch (MalformedURLException ex) {
                LogUtils.warn(ex);
            }
        });
        return replaceStylesFromFollowedMapButton;
    }

    private JButton createCopyStylesButton(URI sourceLocation) {
        JButton copyStylesFromFollowedMapButton = new JButton(TextUtils.getText("copyStyles"));
        copyStylesFromFollowedMapButton.addActionListener(e -> {
            try {
                MapStyle.getController().copyStyles(sourceLocation, map, false, false);
                dialog.setVisible(false);
            } catch (MalformedURLException ex) {
                LogUtils.warn(ex);
            }
        });
        return copyStylesFromFollowedMapButton;
    }

    private JButton createOpenButton(URI mapLocation) {
        JButton openFollowedMapButton = new JButton(TextUtils.getText("open"));
        openFollowedMapButton.addActionListener(e -> {
            UrlManager.getController().loadHyperlink(new Hyperlink(mapLocation));
            dialog.setVisible(false);
        });
        return openFollowedMapButton;
    }

    private JButton createChangeMapButton(String property, JTextField field) {
        JButton changeFollowedMapButton = new JButton(TextUtils.getText("OptionPanel.set_property_text"));
        changeFollowedMapButton.addActionListener(e -> {
            MindMapPreviewWithOptions previewWithOptions = createFileOpenDialogAndOptions();
            JFileChooser fileChooser = previewWithOptions.getFileChooser();
            final int returnVal = fileChooser.showOpenDialog(Controller.getCurrentController().getMapViewManager().getMapViewComponent());
            if (returnVal != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File file = fileChooser.getSelectedFile();
            if(! file.exists()){
                return;
            }
            MapStyle.getController().setProperty(map, property,
                    TemplateManager.INSTANCE.normalizeTemplateLocation(file.toURI()).toString());
            followedMapLocation = updateAssociatedMap(MapStyleModel.FOLLOWED_TEMPLATE_LOCATION_PROPERTY, field, TextUtils.getText("no_map_followed"));
        });
        return changeFollowedMapButton;
    }

    private MindMapPreviewWithOptions createFileOpenDialogAndOptions() {
        final ModeController modeController = Controller.getCurrentModeController();
        final MFileManager fileManager = MFileManager.getController(modeController);
        MindMapPreviewWithOptions previewWithOptions = new MindMapPreviewWithOptions(fileManager.getMindMapFileChooser());
        previewWithOptions.hideOptions();
        previewWithOptions.getFileChooser().setAccessory(previewWithOptions);
        previewWithOptions.getFileChooser().setMultiSelectionEnabled(false);
        return previewWithOptions;
    }

    private URI updateAssociatedMap(String propertyName, JTextField followedMapField, String noMapMessage) {
        String followedMap = MapStyle.getController().getProperty(map, propertyName);
        followedMapField.setEditable(false);
        if(followedMap != null) {
            URI uri;
            try {
                uri = new URI(followedMap);
                String message;
                if (TemplateManager.TEMPLATE_SCHEME.equals(uri.getScheme())) {
                    message = TextUtils.format("followed_template", uri.getPath().substring(1));
                } else {
                    String followedMapPath = "file".equalsIgnoreCase(uri.getScheme()) 
                            ? Paths.get(uri).toFile().getAbsolutePath() : followedMap;
                    message = TextUtils.format("followed_map", followedMapPath);
                }
                followedMapField.setText(message);
                return TemplateManager.INSTANCE.expandTemplateLocation(uri);
            } catch (URISyntaxException e) {
                LogUtils.severe(e);               
            }
        } 
        followedMapField.setText(noMapMessage);
        return null;
    }
    public void show() {
        dialog.setVisible(true);
    }
    
}