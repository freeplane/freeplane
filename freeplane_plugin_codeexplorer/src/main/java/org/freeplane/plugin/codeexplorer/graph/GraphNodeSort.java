/*
 * Created on 8 Nov 2023
 *
 * author dimitry
 */
package org.freeplane.plugin.codeexplorer.graph;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import org.jgrapht.Graph;
import org.jgrapht.alg.connectivity.ConnectivityInspector;
import org.jgrapht.alg.cycle.CycleDetector;
import org.jgrapht.alg.cycle.JohnsonSimpleCycles;
import org.jgrapht.graph.AsSubgraph;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

public class GraphNodeSort<V> {


    private final Graph<V, CoupledWeightedEdge> graph = new SimpleDirectedWeightedGraph<>(CoupledWeightedEdge.class);

    public void addEdge(V source, V target, double weight) {
        // Ensure vertices are in the graph
        graph.addVertex(source);
        graph.addVertex(target);

        CoupledWeightedEdge forwardEdge = graph.getEdge(source, target);
        CoupledWeightedEdge reverseEdge = graph.getEdge(target, source);

        // Check if the reverse edge already exists
        if (reverseEdge != null) {
            double reverseWeight = graph.getEdgeWeight(reverseEdge);
            if (weight > reverseWeight) {
                // If the new edge has greater weight, remove the reverse edge
                // and add the new edge with the weight difference
                graph.removeEdge(reverseEdge);
                CoupledWeightedEdge newEdge = graph.addEdge(source, target);
                newEdge.addCoupling(weight + reverseWeight);
                graph.setEdgeWeight(newEdge, weight - reverseWeight);
            } else {
                // If the reverse edge has a greater weight, update its weight
                graph.setEdgeWeight(reverseEdge, reverseWeight - weight);
                reverseEdge.addCoupling(weight);
            }
        } else if (forwardEdge != null) {
            // If the forward edge exists, update its weight
            double currentWeight = graph.getEdgeWeight(forwardEdge);
            graph.setEdgeWeight(forwardEdge, currentWeight + weight);
            forwardEdge.addCoupling(weight);
        } else {
            // If neither edge exists, add the new edge
            CoupledWeightedEdge newEdge = graph.addEdge(source, target);
            graph.setEdgeWeight(newEdge, weight);
            newEdge.addCoupling(weight);
        }
    }

    public void addNode(V node) {
        graph.addVertex(node);
    }


    static private class CycleSearchStopException extends RuntimeException{
        private static final long serialVersionUID = 1L;
        static final CycleSearchStopException INSTANCE = new CycleSearchStopException();
    }

    public List<List<V>> sortNodes(Comparator<V> nodeComparator, Comparator<Set<V>> secondComparator) {

        // Compute Connected Components
        ConnectivityInspector<V, CoupledWeightedEdge> connectivityInspector = new ConnectivityInspector<>(graph);
        List<Set<V>> connectedSets = connectivityInspector.connectedSets();

        // Sort the connected components in descending order by size
        Comparator<Set<V>> comparator = (set1, set2) -> Integer.compare(set2.size(), set1.size());
        connectedSets.sort(comparator.thenComparing(secondComparator));

        List<List<V>> finalOrdering = new ArrayList<>();

        // Process each connected set
        for (Set<V> connectedSet : connectedSets) {
            // Create a subgraph for the connected set
            Graph<V, CoupledWeightedEdge> connectedSubgraph = new AsSubgraph<>(graph, connectedSet);

            CycleDetector<V, CoupledWeightedEdge> cycleDetector = new CycleDetector<>(connectedSubgraph);
            if (cycleDetector.detectCycles()) {
                boolean cyclesLeft = true;
                do{
                    // Find and break cycles within the SCG
                    JohnsonSimpleCycles<V, CoupledWeightedEdge> cycleFinder = new JohnsonSimpleCycles<>(connectedSubgraph);
                    List<List<V>> cycles = new ArrayList<>();
                    try {
                        cycleFinder.findSimpleCycles(c -> {
                            cycles.add(c);
                            if(cycles.size() >= 100)
                                throw CycleSearchStopException.INSTANCE;
                        });
                        cyclesLeft = false;
                    } catch (CycleSearchStopException e) {/* stopped iterations using exception */}

                    while (!cycles.isEmpty()) {
                        List<V> firstCycle = cycles.get(0);
                        CoupledWeightedEdge minWeightEdge = findMinWeightEdge(connectedSubgraph, firstCycle);
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
                } while(cyclesLeft);
            }

            // Perform topological sort
            TopologicalOrderIterator<V, CoupledWeightedEdge> topologicalOrderIterator =
                    new TopologicalOrderIterator<>(connectedSubgraph,
                            Comparator.<V>comparingDouble(
                                    v -> calculateConnectionStrength(connectedSubgraph.incomingEdgesOf(v)))
                            .reversed()
                            .thenComparing(Comparator.<V>comparingDouble(
                                    v -> calculateConnectionStrength(connectedSubgraph.outgoingEdgesOf(v))))
                            .thenComparing(nodeComparator));
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

    double calculateConnectionStrength(final Set<CoupledWeightedEdge> edges) {
        final double strength = edges
        .stream()
        .mapToDouble(CoupledWeightedEdge::getCoupling).sum();
        return strength;
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

    private CoupledWeightedEdge findMinWeightEdge(Graph<V, CoupledWeightedEdge> graph, List<V> cycle) {
        double minWeight = Double.MAX_VALUE;
        CoupledWeightedEdge minWeightEdge = null;
        for (int i = 0; i < cycle.size(); i++) {
            V source = cycle.get(i);
            V target = cycle.get((i + 1) % cycle.size());
            CoupledWeightedEdge edge = graph.getEdge(source, target);
            if (edge != null && graph.getEdgeWeight(edge) < minWeight) {
                minWeight = graph.getEdgeWeight(edge);
                minWeightEdge = edge;
            }
        }
        return minWeightEdge;
    }
}
