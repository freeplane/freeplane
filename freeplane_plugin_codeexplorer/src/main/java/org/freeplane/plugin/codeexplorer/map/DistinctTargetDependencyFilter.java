/*
 * Created on 26 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.map;

import java.util.LinkedHashMap;
import java.util.Map;

import com.tngtech.archunit.core.domain.Dependency;

class DistinctTargetDependencyFilter {
    Map<String, Dependency> seen = new LinkedHashMap<>();

    Dependency knownDependency(Dependency element) {
        String comparedDescription = getDescriptionForComparison(element);
        Dependency existing = seen.putIfAbsent(comparedDescription, element);
        return existing != null ? existing : element;
    }

    private String getDescriptionForComparison(Dependency dependency) {
        return dependency.getOriginClass().getName() + " " + removeOrigin(dependency.getDescription())
                .replaceFirst(":\\d+\\)$", ")");
    }

    private String removeOrigin(String inputString) {
        int openDelimiters = 0;
        boolean started = false;

        for (int i = 0; i < inputString.length(); i++) {
            char charAt = inputString.charAt(i);

            if (charAt == '<') {
                if (!started) {
                    started = true;
                }
                openDelimiters++;
            } else if (charAt == '>' && started) {
                openDelimiters--;
                if (openDelimiters == 0) {
                    return inputString.substring(i + 1);
                }
            }
        }
        return inputString; // Return input if no matching delimiter is found
    }

}