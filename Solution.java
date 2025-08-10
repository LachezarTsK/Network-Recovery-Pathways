
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

public class Solution {

    private record Point(int node, int edgeCost) {}
    private record Step(int node, int minEdgeCostFromStart, long sumCostFromStart) {}

    private static final int NO_PATH_FOUND = -1;
    private static final int START_NODE = 0;
    private static final int POSITIVE_INFINITY = Integer.MAX_VALUE;
    private static final int MAX_COST = (int) Math.pow(10, 9);

    private int numberOfNodes;
    private List<Point>[] directedGraph;

    public int findMaxPathScore(int[][] edges, boolean[] online, long maxSumCost) {
        if (edges.length == 0) {
            return NO_PATH_FOUND;
        }
        numberOfNodes = online.length;
        directedGraph = createDirectedGraph(edges);
        return searchForMaxPathScore(online, maxSumCost);
    }

    private int searchForMaxPathScore(boolean[] online, long maxSumCost) {
        int lowerEdgeCost = 0;
        int upperEdgeCost = MAX_COST;
        int maxPathScore = NO_PATH_FOUND;

        while (lowerEdgeCost <= upperEdgeCost) {
            int minTargetEdgeCost = lowerEdgeCost + (upperEdgeCost - lowerEdgeCost) / 2;
            int edgeCost = findPathWithScoreNotLessThanMinTargetEdgeCost(minTargetEdgeCost, maxSumCost, online);

            if (edgeCost != NO_PATH_FOUND) {
                maxPathScore = Math.max(maxPathScore, edgeCost);
                lowerEdgeCost = minTargetEdgeCost + 1;
            } else {
                upperEdgeCost = minTargetEdgeCost - 1;
            }
        }
        return maxPathScore;
    }

    private int findPathWithScoreNotLessThanMinTargetEdgeCost(int minTargetEdgeCost, long maxSumCost, boolean[] online) {
        PriorityQueue<Step> minHeapForEdgeCost = new PriorityQueue<>((x, y) -> x.minEdgeCostFromStart - y.minEdgeCostFromStart);
        minHeapForEdgeCost.add(new Step(START_NODE, POSITIVE_INFINITY, 0));

        int[] minEdgeCost = new int[numberOfNodes];
        Arrays.fill(minEdgeCost, POSITIVE_INFINITY);
        minEdgeCost[START_NODE] = 0;

        while (!minHeapForEdgeCost.isEmpty()) {

            Step current = minHeapForEdgeCost.poll();
            if (current.node == numberOfNodes - 1) {
                return current.minEdgeCostFromStart;
            }
            if (directedGraph[current.node].isEmpty()) {
                continue;
            }

            for (Point next : directedGraph[current.node]) {
                if (!online[next.node]
                        || next.edgeCost < minTargetEdgeCost
                        || minEdgeCost[next.node] < next.edgeCost) {
                    continue;
                }
                if (current.sumCostFromStart + next.edgeCost > maxSumCost) {
                    break;
                }
                minEdgeCost[next.node] = next.edgeCost;
                minHeapForEdgeCost.add(new Step(next.node,
                        Math.min(next.edgeCost, current.minEdgeCostFromStart),
                        (current.sumCostFromStart + next.edgeCost)));
            }
        }
        return NO_PATH_FOUND;
    }

    private List<Point>[] createDirectedGraph(int[][] edges) {
        List<Point>[] graph = new ArrayList[numberOfNodes];
        for (int node = 0; node < numberOfNodes; ++node) {
            graph[node] = new ArrayList<>();
        }

        for (int[] edge : edges) {
            int from = edge[0];
            int to = edge[1];
            int cost = edge[2];
            graph[from].add(new Point(to, cost));
        }

        /*
        The sorting is needed so that the search is stopped when the cost of the current edge 
        makes the total cost from start greater than the given limit(maxSumCost). 
         */
        for (int node = 0; node < numberOfNodes; ++node) {
            if (!graph[node].isEmpty()) {
                Collections.sort(graph[node], (x, y) -> x.edgeCost - y.edgeCost);
            }
        }
        return graph;
    }
}
