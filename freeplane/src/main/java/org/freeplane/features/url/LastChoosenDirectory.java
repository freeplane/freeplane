package org.freeplane.features.url;

import java.io.File;

import org.freeplane.core.extension.IExtension;
import org.freeplane.features.map.MapModel;

class LastChoosenDirectory implements IExtension{
    private File lastDirectory;
    static File get(MapModel map) {
        if (map == null) {
            return null;
        } else {
            LastChoosenDirectory extension = map.getExtension(LastChoosenDirectory.class);
            if(extension != null) 
                return extension.lastDirectory;
            File mapFile = map.getFile();
            if (mapFile == null) {
                return null;
            } else {
                File mapDirectory = mapFile.getParentFile();
                if (mapDirectory == null) {
                    return null;
                } else {
                    return mapDirectory;
                }
            }
        }
    }
    
    static void set(MapModel map, File directory) {
        if(directory == null)
            return;
        LastChoosenDirectory extension = map.getExtension(LastChoosenDirectory.class);
        if(extension == null) {
            extension = new LastChoosenDirectory();
            map.addExtension(extension);
        }
        extension.lastDirectory = directory;
    }
}
