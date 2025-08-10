
import kotlin.math.min
import kotlin.math.max
import kotlin.math.pow

class Solution {

    private data class Point(val node: Int, val edgeCost: Int)
    private data class Step(val node: Int, val minEdgeCostFromStart: Int, val sumCostFromStart: Long)

    private companion object {
        const val NO_PATH_FOUND = -1
        const val START_NODE = 0
        const val POSITIVE_INFINITY = Int.MAX_VALUE
        var MAX_COST = (10.0).pow(9.0).toInt()
    }

    private var numberOfNodes: Int = 0
    private lateinit var directedGraph: Array<MutableList<Point>>

    fun findMaxPathScore(edges: Array<IntArray>, online: BooleanArray, maxSumCost: Long): Int {
        if (edges.isEmpty()) {
            return NO_PATH_FOUND
        }
        numberOfNodes = online.size
        directedGraph = createDirectedGraph(edges)
        return searchForMaxPathScore(online, maxSumCost)
    }

    private fun searchForMaxPathScore(online: BooleanArray, maxSumCost: Long): Int {
        var lowerEdgeCost = 0
        var upperEdgeCost = MAX_COST
        var maxPathScore = NO_PATH_FOUND

        while (lowerEdgeCost <= upperEdgeCost) {
            val minTargetEdgeCost = lowerEdgeCost + (upperEdgeCost - lowerEdgeCost) / 2
            val edgeCost = findPathWithScoreNotLessThanMinTargetEdgeCost(minTargetEdgeCost, maxSumCost, online)

            if (edgeCost != NO_PATH_FOUND) {
                maxPathScore = max(maxPathScore, edgeCost)
                lowerEdgeCost = minTargetEdgeCost + 1
            } else {
                upperEdgeCost = minTargetEdgeCost - 1
            }
        }
        return maxPathScore
    }

    private fun findPathWithScoreNotLessThanMinTargetEdgeCost(minTargetEdgeCost: Int, maxSumCost: Long, online: BooleanArray): Int {
        val minHeapForEdgeCost = PriorityQueue<Step> { x, y -> x.minEdgeCostFromStart - y.minEdgeCostFromStart }
        minHeapForEdgeCost.add(Step(START_NODE, POSITIVE_INFINITY, 0))

        val minEdgeCost = IntArray(numberOfNodes) { POSITIVE_INFINITY }
        minEdgeCost[START_NODE] = 0

        while (!minHeapForEdgeCost.isEmpty()) {

            val current = minHeapForEdgeCost.poll()
            if (current.node == numberOfNodes - 1) {
                return current.minEdgeCostFromStart
            }
            if (directedGraph[current.node].isEmpty()) {
                continue
            }

            for (next in directedGraph[current.node]) {
                if (!online[next.node]
                    || next.edgeCost < minTargetEdgeCost
                    || minEdgeCost[next.node] < next.edgeCost
                ) {
                    continue
                }
                if (current.sumCostFromStart + next.edgeCost > maxSumCost) {
                    break
                }
                minEdgeCost[next.node] = next.edgeCost
                minHeapForEdgeCost.add(Step(
                                       next.node,
                                       min(next.edgeCost, current.minEdgeCostFromStart),
                                       (current.sumCostFromStart + next.edgeCost)))
            }
        }
        return NO_PATH_FOUND
    }

    private fun createDirectedGraph(edges: Array<IntArray>): Array<MutableList<Point>> {
        val graph = Array(numberOfNodes) { mutableListOf<Point>() }
        for ((from, to, cost) in edges) {
            graph[from].add(Point(to, cost))
        }

        /*
        The sorting is needed so that the search is stopped when the cost of the current edge
        makes the total cost from start greater than the given limit(maxSumCost).
         */
        for (node in 0..<numberOfNodes) {
            if (graph[node].isNotEmpty()) {
                graph[node].sortWith { x, y -> x.edgeCost - y.edgeCost }
            }
        }
        return graph
    }
}
