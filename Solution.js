
// const {PriorityQueue} = require('@datastructures-js/priority-queue');
/*
 PriorityQueue is internally included in the solution file on leetcode.
 When running the code on leetcode it should stay commented out. 
 It is mentioned here just for information about the external library 
 that is applied for this data structure.
 */

/**
 * @param {number[][]} edges
 * @param {boolean[]} online
 * @param {number} maxSumCost
 * @return {number}
 */
var findMaxPathScore = function (edges, online, maxSumCost) {
    if (edges.length === 0) {
        return Util.NO_PATH_FOUND;
    }
    const util = new Util(online.length, edges);
    return searchForMaxPathScore(online, maxSumCost, util);
};

/**
 * @param {boolean[]} online
 * @param {number} maxSumCost
 * @param {Util} util 
 * @return {number}
 */
function searchForMaxPathScore(online, maxSumCost, util) {
    let lowerEdgeCost = 0;
    let upperEdgeCost = Util.MAX_COST;
    let maxPathScore = Util.NO_PATH_FOUND;

    while (lowerEdgeCost <= upperEdgeCost) {
        const minTargetEdgeCost = lowerEdgeCost + Math.floor((upperEdgeCost - lowerEdgeCost) / 2);
        const edgeCost = findPathWithScoreNotLessThanMinTargetEdgeCost(minTargetEdgeCost, maxSumCost, online, util);

        if (edgeCost !== Util.NO_PATH_FOUND) {
            maxPathScore = Math.max(maxPathScore, edgeCost);
            lowerEdgeCost = minTargetEdgeCost + 1;
        } else {
            upperEdgeCost = minTargetEdgeCost - 1;
        }
    }
    return maxPathScore;
}

/**
 * @param {number} minTargetEdgeCost 
 * @param {number} maxSumCost
 * @param {boolean[]} online
 * @param {Util} util 
 * @return {number}
 */
function findPathWithScoreNotLessThanMinTargetEdgeCost(minTargetEdgeCost, maxSumCost, online, util) {
    const minHeapForEdgeCost = new PriorityQueue((x, y) => x.minEdgeCostFromStart - y.minEdgeCostFromStart);
    minHeapForEdgeCost.enqueue(new Step(Util.START_NODE, Number.POSITIVE_INFINITY, 0));

    const minEdgeCost = new Array(util.numberOfNodes).fill(Number.POSITIVE_INFINITY);
    minEdgeCost[Util.START_NODE] = 0;

    while (!minHeapForEdgeCost.isEmpty()) {

        const current = minHeapForEdgeCost.dequeue();
        if (current.node === util.numberOfNodes - 1) {
            return current.minEdgeCostFromStart;
        }
        if (util.directedGraph[current.node].length === 0) {
            continue;
        }

        for (let next of util.directedGraph[current.node]) {
            if (!online[next.node]
                    || next.edgeCost < minTargetEdgeCost
                    || minEdgeCost[next.node] < next.edgeCost) {
                continue;
            }
            if (current.sumCostFromStart + next.edgeCost > maxSumCost) {
                break;
            }
            minEdgeCost[next.node] = next.edgeCost;
            minHeapForEdgeCost.enqueue(new Step(next.node,
                    Math.min(next.edgeCost, current.minEdgeCostFromStart),
                    (current.sumCostFromStart + next.edgeCost)));
        }
    }
    return Util.NO_PATH_FOUND;
}

/**
 * @param {number} node 
 * @param {number} edgeCost
 */
function Point(node, edgeCost) {
    this.node = node;
    this.edgeCost = edgeCost;
}

/**
 * @param {number} node 
 * @param {number} minEdgeCostFromStart
 * @param {number} sumCostFromStart
 */
function Step(node, minEdgeCostFromStart, sumCostFromStart) {
    this.node = node;
    this.minEdgeCostFromStart = minEdgeCostFromStart;
    this.sumCostFromStart = sumCostFromStart;
}

class Util {
    static NO_PATH_FOUND = -1;
    static START_NODE = 0;
    static MAX_COST = Math.pow(10, 9);

    /**
     * @param {number} numberOfNodes
     * @param {number[][]} edges
     */
    constructor(numberOfNodes, edges) {
        this.numberOfNodes = numberOfNodes;
        this.directedGraph = this.createDirectedGraph(edges);
    }

    /**
     * @param {number[][]} edges
     * @return {Point[][]} 
     */
    createDirectedGraph(edges) {
        const graph = Array.from(new Array(this.numberOfNodes), () => new Array());
        for (let[from, to, cost] of edges) {
            graph[from].push(new Point(to, cost));
        }

        /*
         The sorting is needed so that the search is stopped when the cost of the current edge 
         makes the total cost from start greater than the given limit(maxSumCost). 
         */
        for (let node = 0; node < this.numberOfNodes; ++node) {
            if (graph[node].length > 0) {
                graph[node].sort((x, y) => x.edgeCost - y.edgeCost);
            }
        }
        return graph;
    }
}
