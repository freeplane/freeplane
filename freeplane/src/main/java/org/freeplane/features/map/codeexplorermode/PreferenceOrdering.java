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
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.cycle.JohnsonSimpleCycles;
import org.jgrapht.graph.AsSubgraph;
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

        // Compute Connected Components
        ConnectivityInspector<V, DefaultWeightedEdge> connectivityInspector = new ConnectivityInspector<>(graph);
        List<Set<V>> connectedSets = connectivityInspector.connectedSets();

        // Sort the connected components in descending order by size
        connectedSets.sort((set1, set2) -> Integer.compare(set2.size(), set1.size()));

        List<V> finalOrdering = new ArrayList<>();

        // Process each SCG
        for (Set<V> scgSet : connectedSets) {
            // Create a subgraph for the SCG
            Graph<V, DefaultWeightedEdge> subgraph = new AsSubgraph<>(graph, scgSet);

            // Find and break cycles within the SCG
            JohnsonSimpleCycles<V, DefaultWeightedEdge> cycleFinder = new JohnsonSimpleCycles<>(subgraph);
            List<List<V>> cycles = cycleFinder.findSimpleCycles();

            while (!cycles.isEmpty()) {
                List<V> firstCycle = cycles.get(0);
                DefaultWeightedEdge minWeightEdge = findMinWeightEdge(subgraph, firstCycle);
                if (minWeightEdge != null) {
                    V source = subgraph.getEdgeSource(minWeightEdge);
                    V target = subgraph.getEdgeTarget(minWeightEdge);
                    subgraph.removeEdge(minWeightEdge);

                    // Remove the first cycle from the list, since the edge removal will break it
                    cycles.remove(0);

                    // Remove all cycles containing the removed edge
                    cycles.removeIf(cycle -> cycleContainsEdge(source, target, cycle));

                    if (cycles.isEmpty())
                        cycles = cycleFinder.findSimpleCycles();
                } else {
                    // If no edge is found, it should break and avoid an infinite loop
                    break;
                }
            }

            // Determine the ordering within the SCG based on the remaining weights
            Map<V, Double> scgOrderingWeights = new HashMap<>();
            for (V vertex : scgSet) {
                double netWeight = subgraph.outgoingEdgesOf(vertex).stream()
                        .mapToDouble(subgraph::getEdgeWeight).sum()
                        - subgraph.incomingEdgesOf(vertex).stream()
                        .mapToDouble(subgraph::getEdgeWeight).sum();
                scgOrderingWeights.put(vertex, netWeight);
            }

            // Sort the vertices within the SCG based on net weights
            List<Map.Entry<V, Double>> scgSortedEntries = new ArrayList<>(scgOrderingWeights.entrySet());
            scgSortedEntries.sort(Map.Entry.<V, Double>comparingByValue().reversed());

            // Add the sorted vertices to the final ordering
            for (Map.Entry<V, Double> entry : scgSortedEntries) {
                finalOrdering.add(entry.getKey());
            }
        }

        return finalOrdering;
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
