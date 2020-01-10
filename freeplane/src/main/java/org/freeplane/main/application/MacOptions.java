package org.freeplane.main.application;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.freeplane.core.util.Compat;

public class MacOptions {
    static public final Collection<String> macFilesToOpen =  Compat.isMacOsX() ?  
            new ConcurrentLinkedQueue<>() : 
                Collections.emptyList();
}
