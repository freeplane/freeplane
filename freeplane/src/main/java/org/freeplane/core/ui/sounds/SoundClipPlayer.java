/*
 * Created on 23 Jun 2023
 *
 * author dimitry
 */
package org.freeplane.core.ui.sounds;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

import org.freeplane.core.resources.ResourceController;
import org.freeplane.core.util.LogUtils;

public class SoundClipPlayer {
    private static Map<String, Clip> sounds = new ConcurrentHashMap<>();
    static int counter = 0;
    public static void playSound(String sound) {
        if(GraphicsEnvironment.isHeadless())
            return;
        ResourceController resourceController = ResourceController.getResourceController();
        if(! resourceController.getBooleanProperty("playsSound." + sound))
            return;
        Clip clip = loadSoundClip(sound);
        if(clip != null && ! clip.isRunning()) {
            clip.setFramePosition(0);
            clip.start();
        }
    }

    public static Clip loadSoundClip(String sound) {
        Clip clip = sounds.get(sound);
        if(clip == null && ! sounds.containsKey(sound)) {
            try {
                ResourceController resourceController = ResourceController.getResourceController();
                URL resource = resourceController.getResource("/sounds/" + sound + ".wav");
                if(resource == null) {
                    sounds.put(sound, null);
                    return null;
                }
                clip = AudioSystem.getClip();
                AudioInputStream ais = AudioSystem.getAudioInputStream(resource);
                clip.open(ais);
                sounds.put(sound, clip);
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                LogUtils.severe(e);
                sounds.put(sound, null);
            }
        }
        return clip;
    }


}
