/*
 * Created on 8 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.map;


import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.cycle.JohnsonSimpleCycles;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

public class GraphNodeSort<V> {


    private final Graph<V, DefaultWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(DefaultWeightedEdge.class);

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


    public List<List<V>> sortNodes() {

        // Compute Connected Components
        ConnectivityInspector<V, DefaultWeightedEdge> connectivityInspector = new ConnectivityInspector<>(graph);
        List<Set<V>> connectedSets = connectivityInspector.connectedSets();

        // Sort the connected components in descending order by size
        connectedSets.sort((set1, set2) -> Integer.compare(set2.size(), set1.size()));

        List<List<V>> finalOrdering = new ArrayList<>();

        // Process each connected set
        for (Set<V> connectedSet : connectedSets) {
            // Create a subgraph for the connected set
            Graph<V, DefaultWeightedEdge> connectedSubgraph = new AsSubgraph<>(graph, connectedSet);

            // Find and break cycles within the SCG
            JohnsonSimpleCycles<V, DefaultWeightedEdge> cycleFinder = new JohnsonSimpleCycles<>(connectedSubgraph);
            List<List<V>> cycles = cycleFinder.findSimpleCycles();

            while (!cycles.isEmpty()) {
                List<V> firstCycle = cycles.get(0);
                DefaultWeightedEdge minWeightEdge = findMinWeightEdge(connectedSubgraph, firstCycle);
                if (minWeightEdge != null) {
                    V source = connectedSubgraph.getEdgeSource(minWeightEdge);
                    V target = connectedSubgraph.getEdgeTarget(minWeightEdge);
                    connectedSubgraph.removeEdge(minWeightEdge);

                    // Remove the first cycle from the list, since the edge removal will break it
                    cycles.remove(0);

                    // Remove all cycles containing the removed edge
                    cycles.removeIf(cycle -> cycleContainsEdge(source, target, cycle));

                } else {
                    // If no edge is found, it should break and avoid an infinite loop
                    break;
                }
            }

            // Perform topological sort
            TopologicalOrderIterator<V, DefaultWeightedEdge> topologicalOrderIterator =
                    new TopologicalOrderIterator<>(connectedSubgraph);
            List<V> subgroupOrdering = new ArrayList<>();
            finalOrdering.add(subgroupOrdering);
            // Add the sorted vertices to the final ordering
            while (topologicalOrderIterator.hasNext()) {
                V node = topologicalOrderIterator.next();
                subgroupOrdering.add(node);
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
        GraphNodeSort<String> nodeSort = new GraphNodeSort<String>();
        nodeSort.addEdge("a", "b", 30);
        nodeSort.addEdge("b", "c", 20);
        nodeSort.addEdge("c", "a", 10);
        List<List<String>> ordering = nodeSort.sortNodes();
        System.out.println(ordering);
    }
}
