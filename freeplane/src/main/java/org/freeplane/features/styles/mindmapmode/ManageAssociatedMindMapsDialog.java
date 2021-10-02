package org.freeplane.features.styles.mindmapmode;

import java.awt.Frame;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.function.Supplier;
import java.util.stream.Stream;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JTextArea;

import org.freeplane.core.ui.components.UITools;
import org.freeplane.core.util.Hyperlink;
import org.freeplane.core.util.LogUtils;
import org.freeplane.core.util.TextUtils;
import org.freeplane.features.map.MapModel;
import org.freeplane.features.styles.MapStyle;
import org.freeplane.features.styles.MapStyleModel;
import org.freeplane.features.url.UrlManager;
import org.freeplane.features.url.mindmapmode.TemplateManager;
import org.freeplane.view.swing.features.filepreview.MindMapPreviewWithOptions;

import com.jgoodies.forms.builder.FormBuilder;

public class ManageAssociatedMindMapsDialog{
    private final MapModel map;
    private final JDialog dialog;
    private URI followedMapLocation;
    private URI associatedMapLocation;
    
    private final JButton[] followedMapButtons;
    private final JButton[] associatedMapButtons;
    
    public ManageAssociatedMindMapsDialog(String title, final MapModel map){
        this.map = map;
        Frame owner = UITools.getCurrentFrame();
        dialog = new JDialog(owner);
        FormBuilder formBuilder = FormBuilder.create().columns("p, 3dlu, fill:3dlu:grow, 3dlu, p")
                .rows("p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p, 3dlu, p");
        formBuilder.add(TextUtils.getText("followed_map")).xy(1,  1);
        JTextArea followedMapField = new JTextArea();
        followedMapField.setColumns(80);
        followedMapField.setLineWrap(true);
        followedMapField.setWrapStyleWord(false);
        formBuilder.add(followedMapField).xy(3, 1);
        JButton changeFollowedMapButton = createChangeMapButton(MapStyleModel.FOLLOWED_TEMPLATE_LOCATION_PROPERTY, "select_followed_map", followedMapField);
        formBuilder.add(changeFollowedMapButton).xy(5, 1);
        JButton openFollowedMapButton = createOpenButton(() -> followedMapLocation);
        JButton copyStylesFromFollowedMapButton = createCopyStylesButton(() -> followedMapLocation);
        JButton replaceStylesFromFollowedMapButton = createReplaceStylesButton(() -> followedMapLocation);
        JButton unfolowButton = createUnfollowButton(followedMapField);
        followedMapButtons = new JButton[] {openFollowedMapButton, copyStylesFromFollowedMapButton, replaceStylesFromFollowedMapButton, unfolowButton};
        formBuilder.addBar(openFollowedMapButton, copyStylesFromFollowedMapButton, 
                replaceStylesFromFollowedMapButton, unfolowButton).xyw(1, 3, 5);
        
        formBuilder.add(TextUtils.getText("associated_template")).xy(1,  5);
        JTextArea associatedMapField = new JTextArea();
        associatedMapField.setColumns(80);
        associatedMapField.setLineWrap(true);
        associatedMapField.setWrapStyleWord(false);
        formBuilder.add(associatedMapField).xy(3, 5);
        JButton changeAssosiatedMapButton = createChangeMapButton(MapStyleModel.ASSOCIATED_TEMPLATE_LOCATION_PROPERTY, "select_associated_template", associatedMapField);
        formBuilder.add(changeAssosiatedMapButton).xy(5, 5);
        JButton openAssociatedMapButton = createOpenButton(() -> associatedMapLocation);
        JButton copyStylesFromAssociatedMapButton = createCopyStylesButton(() -> associatedMapLocation);
        JButton replaceStylesFromAssociatedMapButton = createReplaceStylesButton(() -> associatedMapLocation);
        associatedMapButtons = new JButton[] {openAssociatedMapButton, copyStylesFromAssociatedMapButton, replaceStylesFromAssociatedMapButton};
        formBuilder.addBar(openAssociatedMapButton, copyStylesFromAssociatedMapButton, 
                replaceStylesFromAssociatedMapButton).xyw(1, 7, 5);
        
        JButton closeButton = createCloseMapButton();
        formBuilder.add(closeButton).xy(1, 9, "l,c");

        updateAssociatedMap(MapStyleModel.FOLLOWED_TEMPLATE_LOCATION_PROPERTY, followedMapField, TextUtils.getText("no_map_followed"));      
        updateAssociatedMap(MapStyleModel.ASSOCIATED_TEMPLATE_LOCATION_PROPERTY, associatedMapField, TextUtils.getText("no_template_associated"));      

        dialog.setModal(true);
        dialog.setTitle(title);
        dialog.getContentPane().add(formBuilder.build());
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
    }
    private JButton createUnfollowButton(JTextArea followedMapField) {
        JButton unfolowButton = new JButton(TextUtils.getText("unfollow"));
        unfolowButton.setEnabled(followedMapLocation != null);
        unfolowButton.addActionListener(e -> {
            MapStyle.getController().setProperty(map, MapStyleModel.FOLLOWED_TEMPLATE_LOCATION_PROPERTY, null);
            MapStyle.getController().setProperty(map, MapStyleModel.FOLLOWED_MAP_LAST_TIME, null);
            followedMapField.setText(TextUtils.getText("no_map_followed"));
            enableButtons(followedMapButtons, false);
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
    private JButton createReplaceStylesButton(Supplier<URI>  sourceLocation) {
        JButton replaceStylesFromFollowedMapButton = new JButton(TextUtils.getText("replaceStyles"));
        replaceStylesFromFollowedMapButton.addActionListener(e -> {
            try {
                MapStyle.getController().replaceStyles(sourceLocation.get(), map, false, false);
                dialog.setVisible(false);
            } catch (MalformedURLException ex) {
                LogUtils.warn(ex);
            }
        });
        return replaceStylesFromFollowedMapButton;
    }

    private JButton createCopyStylesButton(Supplier<URI>  sourceLocation) {
        JButton copyStylesFromFollowedMapButton = new JButton(TextUtils.getText("copyStyles"));
        copyStylesFromFollowedMapButton.addActionListener(e -> {
            try {
                MapStyle.getController().copyStyles(sourceLocation.get(), map, false, false);
                dialog.setVisible(false);
            } catch (MalformedURLException ex) {
                LogUtils.warn(ex);
            }
        });
        return copyStylesFromFollowedMapButton;
    }

    private JButton createOpenButton(Supplier<URI> mapLocation) {
        JButton openFollowedMapButton = new JButton(TextUtils.getText("open"));
        openFollowedMapButton.addActionListener(e -> {
            UrlManager.getController().loadHyperlink(new Hyperlink(mapLocation.get()));
            dialog.setVisible(false);
        });
        return openFollowedMapButton;
    }

    private JButton createChangeMapButton(String mapProperty, String fileChooserTitleProperty, JTextArea field) {
        JButton assignMapButton = new JButton(TextUtils.getText("OptionPanel.set_property_text"));
        assignMapButton.addActionListener(e -> {
            MindMapPreviewWithOptions previewWithOptions = MindMapPreviewWithOptions.createFileOpenDialogAndOptions(
                    TextUtils.getText(fileChooserTitleProperty)
            );
            JFileChooser fileChooser = previewWithOptions.getFileChooser();
            final int returnVal = fileChooser.showOpenDialog(UITools.getCurrentFrame());
            if (returnVal != JFileChooser.APPROVE_OPTION) {
                return;
            }
            File file = fileChooser.getSelectedFile();
            if(! file.exists()){
                return;
            }
            MapStyle.getController().setProperty(map, mapProperty,
                    TemplateManager.INSTANCE.normalizeTemplateLocation(file.toURI()).toString());
            updateAssociatedMap(mapProperty, field, TextUtils.getText("no_map_followed"));
        });
        return assignMapButton;
    }

    private void updateAssociatedMap(String propertyName, JTextArea mapField, String noMapMessage) {
        String followedMap = MapStyle.getController().getProperty(map, propertyName);
        mapField.setEditable(false);
        URI uri = null;
        if(followedMap != null) {
            try {
                uri = new URI(followedMap);
                String message = TemplateManager.INSTANCE.describeNormalizedLocation(uri);
                mapField.setText(message);
                uri = TemplateManager.INSTANCE.expandTemplateLocation(uri);
            } catch (URISyntaxException e) {
                LogUtils.severe(e);               
            }
        } 
        else {
            mapField.setText(noMapMessage);
        }
        if(MapStyleModel.FOLLOWED_TEMPLATE_LOCATION_PROPERTY.equals(propertyName)) {
            followedMapLocation = uri;
            enableButtons(followedMapButtons, uri != null);
        }
        else if(MapStyleModel.ASSOCIATED_TEMPLATE_LOCATION_PROPERTY.equals(propertyName)) {
            associatedMapLocation = uri;
            enableButtons(associatedMapButtons, uri != null);
        }
    }
    
    
    private void enableButtons(JButton[] buttons, boolean enabled) {
        Stream.of(buttons).forEach(button -> button.setEnabled(enabled));
    }
    
    public void show() {
        dialog.setVisible(true);
    }
    
}