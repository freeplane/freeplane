/*
 * Created on 4 Apr 2024
 *
 * author dimitry
 */
package org.freeplane.core.ui.components;

import java.awt.Color;

public class HSLColorConverter {
    public static Color generateColorFromLong(long value) {
        // Extract and normalize the first 3 bytes for hue (0-360)
        float hue = ((value >> 8) & 0xFFFFFF) / (float)0xFFFFFF;

        // Extract the last byte and map it to lightness between 0.2 and 0.8
        float lightness = 0.3f + ((value & 0xFF) / (float)0xFF) * 0.4f;

        // Saturation remains maximal
        float saturation = 1.0f;

        float adjustedHue = adjustHueToYellow(hue);
        return hslToRgb(adjustedHue, saturation, lightness);
    }

    public static float adjustHueToYellow(float x) {
        if (x < 4/36f) { // Adjust range towards yellow
            return x * (1 + 1/36f); // Stretch the red to yellow range
        } else if (x < 8/36f) { // Yellow to green range
            return 6/36f + (x - 6/36f) * 1/2f; // Compress yellow to green
        } else {
            return 1f + (x - 1f) * (29/28f); // Normalize the rest of the spectrum
        }
    }
    public static Color hslToRgb(float h, float s, float l){
        float r, g, b;
        if(s == 0){
            r = g = b = l; // achromatic
        }else{
            float q = l < 0.5 ? l * (1 + s) : l + s - l * s;
            float p = 2 * l - q;
            r = hueToRgb(p, q, h + 1/3f);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1/3f);
        }
        return new Color((int)(r * 255), (int)(g * 255), (int)(b * 255));
    }

    private static float hueToRgb(float p, float q, float t){
        if(t < 0) t += 1;
        if(t > 1) t -= 1;
        if(t < 1/6f) return p + (q - p) * 6 * t;
        if(t < 1/2f) return q;
        if(t < 2/3f) return p + (q - p) * (2/3f - t) * 6;
        return p;
    }

    public static float lightness(Color color) {
        int r = color.getRed(), g = color.getGreen(), b = color.getBlue();
        int max = Math.max(r, Math.max(g, b));
        int min = Math.min(r, Math.min(g, b));

        // Calculate lightness as the average of max and min, keeping the result in [0, 512]
        int lightness = (max + min);
        return lightness / 512f;
    }
}
