JMapViewer

(c) 2008 Jan Peter Stotz and others

This work bases partly on the JOSM plugin "Slippy Map Chooser" by Tim Haussmann

License: GPL

FAQ:

1. What is JMapViewer?

JMapViewer is a Java Swing component for integrating OSM maps in to your Java 
application. JMapViewer allows you to set markers on the map or zoom to a specific 
location on the map.

2. How does JMapViewer work?

JMapViewer loads bitmap tiles from the OpenStreetmap tile server (Mapnik renderer).
Therefore any application using JMapViewer requires a working Internet connection.    

3. How do I use JMapViewer in my application?

You can just create an instance of the class org.openstreetmap.gui.jmapviewer.JMapViewer
using the default constructor and add it to your panel/frame/windows.  
For more details please see the Demo class in the same package.  