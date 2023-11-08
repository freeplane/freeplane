/*
 * Created on 8 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.features.map.codeexplorermode;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jgrapht.Graph;
import org.jgrapht.alg.cycle.JohnsonSimpleCycles;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

public class PreferenceOrdering<V> {


    private final Graph<V, DefaultWeightedEdge> graph = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);

    public void addEdge(V source, V target, double weight) {
        // Ensure vertices are in the graph
        graph.addVertex(source);
        graph.addVertex(target);

        DefaultWeightedEdge forwardEdge = graph.getEdge(source, target);
        DefaultWeightedEdge reverseEdge = graph.getEdge(target, source);

        // Check if the reverse edge already exists
        if (reverseEdge != null) {
            double reverseWeight = graph.getEdgeWeight(reverseEdge);
            if (weight > reverseWeight) {
                // If the new edge has greater weight, remove the reverse edge
                // and add the new edge with the weight difference
                graph.removeEdge(reverseEdge);
                DefaultWeightedEdge newEdge = graph.addEdge(source, target);
                graph.setEdgeWeight(newEdge, weight - reverseWeight);
            } else if (weight < reverseWeight) {
                // If the reverse edge has a greater weight, update its weight
                graph.setEdgeWeight(reverseEdge, reverseWeight - weight);
            } else {
                // If both weights are equal, remove both edges
                graph.removeEdge(reverseEdge);
            }
        } else if (forwardEdge != null) {
            // If the forward edge exists, update its weight
            double currentWeight = graph.getEdgeWeight(forwardEdge);
            graph.setEdgeWeight(forwardEdge, currentWeight + weight);
        } else {
            // If neither edge exists, add the new edge
            DefaultWeightedEdge newEdge = graph.addEdge(source, target);
            graph.setEdgeWeight(newEdge, weight);
        }
    }

    public void addNode(V node) {
        graph.addVertex(node);
    }


    public List<V> findStrongestOrdering() {
        // Find and break cycles
        JohnsonSimpleCycles<V, DefaultWeightedEdge> cycleFinder = new JohnsonSimpleCycles<>(graph);
        List<List<V>> cycles = cycleFinder.findSimpleCycles();

        while (!cycles.isEmpty()) {
            List<V> firstCycle = cycles.get(0);
            System.out.println("Cycle detected: " + firstCycle);

            DefaultWeightedEdge minWeightEdge = findMinWeightEdge(graph, firstCycle);
            if (minWeightEdge != null) {
                V source = graph.getEdgeSource(minWeightEdge);
                V target = graph.getEdgeTarget(minWeightEdge);
                graph.removeEdge(minWeightEdge);
                System.out.println("Removing edge: " + source + " -> " + target);

                // Remove the first cycle from the list, since the edge removal will break it
                cycles.remove(0);

                // Remove all cycles containing the removed edge
                cycles.removeIf(cycle -> cycleContainsEdge(source, target, cycle));

                if (cycles.isEmpty())
                    cycles = cycleFinder.findSimpleCycles();
            } else {
                // Handle the error condition, potentially with a break or continue statement
                System.err.println("Error: Could not find a minimum weight edge in the cycle.");
                // Decide on how to handle this situation - for example, you could just break out of the loop
                break;
            }
        }

        System.out.println("Graph after cycles have been reduced:");
        for (DefaultWeightedEdge edge : graph.edgeSet()) {
            System.out.println(graph.getEdgeSource(edge) + " -> " + graph.getEdgeTarget(edge) + " : " + graph.getEdgeWeight(edge));
        }

        // Determine the ordering based on the remaining weights
        Map<V, Double> orderingWeights = new HashMap<>();
        for (V vertex : graph.vertexSet()) {
            double netWeight = graph.outgoingEdgesOf(vertex).stream()
                    .mapToDouble(graph::getEdgeWeight).sum()
                    - graph.incomingEdgesOf(vertex).stream()
                    .mapToDouble(graph::getEdgeWeight).sum();
            orderingWeights.put(vertex, netWeight);
        }

        // Sort the vertices based on the net weights
        List<Map.Entry<V, Double>> sortedEntries = new ArrayList<>(orderingWeights.entrySet());
        sortedEntries.sort(Map.Entry.<V, Double>comparingByValue().reversed());

        System.out.println("Final ordering and weights:");
        for (Map.Entry<V, Double> entry : sortedEntries) {
            System.out.println(entry.getKey() + " : " + entry.getValue());
        }

        // Extract the sorted node names
        List<V> sortedNodes = new ArrayList<>();
        for (Map.Entry<V, Double> entry : sortedEntries) {
            sortedNodes.add(entry.getKey());
        }

        return sortedNodes;
    }

    private boolean cycleContainsEdge(V source, V target, List<V> cycle) {
        // Check if the cycle contains the source
        int sourceIndex = cycle.indexOf(source);
        if(sourceIndex == -1) {
            // The source is not in the cycle, so the edge cannot be either
            return false;
        }
        // Check if the target is the next node in the cycle or the first if source is the last
        int targetIndex = (sourceIndex + 1) % cycle.size();
        return target.equals(cycle.get(targetIndex));
    }

    private DefaultWeightedEdge findMinWeightEdge(Graph<V, DefaultWeightedEdge> graph, List<V> cycle) {
        double minWeight = Double.MAX_VALUE;
        DefaultWeightedEdge minWeightEdge = null;
        for (int i = 0; i < cycle.size(); i++) {
            V source = cycle.get(i);
            V target = cycle.get((i + 1) % cycle.size());
            DefaultWeightedEdge edge = graph.getEdge(source, target);
            if (edge != null && graph.getEdgeWeight(edge) < minWeight) {
                minWeight = graph.getEdgeWeight(edge);
                minWeightEdge = edge;
            }
        }
        return minWeightEdge;
    }


    public static void main(String[] args) {
        PreferenceOrdering<String> preferenceOrdering = new PreferenceOrdering<String>();
        preferenceOrdering.addEdge("a", "b", 30);
        preferenceOrdering.addEdge("b", "c", 20);
        preferenceOrdering.addEdge("c", "a", 10);
        List<String> ordering = preferenceOrdering.findStrongestOrdering();
        System.out.println(ordering);
    }
}
