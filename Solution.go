
package main

import (
    "container/heap"
    "math"
    "slices"
)

const NO_PATH_FOUND = -1
const START_NODE = 0
const POSITIVE_INFINITY = math.MaxInt

var MAX_COST = int(math.Pow(10.0, 9.0))
var numberOfNodes int
var directedGraph [][]Point

type Point struct {
    node     int
    edgeCost int
}

func NewPoint(node int, edgeCost int) Point {
    point := Point{
        node:     node,
        edgeCost: edgeCost,
    }
    return point
}

type Step struct {
    node                 int
    minEdgeCostFromStart int
    sumCostFromStart     int64
}

func NewStep(node int, minEdgeCostFromStart int, sumCostFromStart int64) Step {
    step := Step{
        node:                 node,
        minEdgeCostFromStart: minEdgeCostFromStart,
        sumCostFromStart:     sumCostFromStart,
    }
    return step
}

func findMaxPathScore(edges [][]int, online []bool, maxSumCost int64) int {
    if len(edges) == 0 {
        return NO_PATH_FOUND
    }
    numberOfNodes = len(online)
    directedGraph = createDirectedGraph(edges)
    return searchForMaxPathScore(online, maxSumCost)
}

func searchForMaxPathScore(online []bool, maxSumCost int64) int {
    lowerEdgeCost := 0
    upperEdgeCost := MAX_COST
    maxPathScore := NO_PATH_FOUND

    for lowerEdgeCost <= upperEdgeCost {
        minTargetEdgeCost := lowerEdgeCost + (upperEdgeCost-lowerEdgeCost)/2
        edgeCost := findPathWithScoreNotLessThanMinTargetEdgeCost(minTargetEdgeCost, maxSumCost, online)

        if edgeCost != NO_PATH_FOUND {
            maxPathScore = max(maxPathScore, edgeCost)
            lowerEdgeCost = minTargetEdgeCost + 1
        } else {
            upperEdgeCost = minTargetEdgeCost - 1
        }
    }
    return maxPathScore
}

func findPathWithScoreNotLessThanMinTargetEdgeCost(minTargetEdgeCost int, maxSumCost int64, online []bool) int {
    minHeapForEdgeCost := PriorityQueue{}
    heap.Push(&minHeapForEdgeCost, NewStep(START_NODE, POSITIVE_INFINITY, 0))

    minEdgeCost := make([]int, numberOfNodes)
    for i := range minEdgeCost {
        minEdgeCost[i] = POSITIVE_INFINITY
    }
    minEdgeCost[START_NODE] = 0

    for minHeapForEdgeCost.Len() > 0 {

        current := heap.Pop(&minHeapForEdgeCost).(Step)
        if current.node == numberOfNodes - 1 {
            return current.minEdgeCostFromStart
        }
        if len(directedGraph[current.node]) == 0 {
            continue
        }

        for _, next := range directedGraph[current.node] {
            if !online[next.node] || next.edgeCost < minTargetEdgeCost || minEdgeCost[next.node] < next.edgeCost {
                continue
            }
            if current.sumCostFromStart + int64(next.edgeCost) > maxSumCost {
                break
            }
            minEdgeCost[next.node] = next.edgeCost
            heap.Push(&minHeapForEdgeCost,
                NewStep(next.node, 
                        min(next.edgeCost, current.minEdgeCostFromStart), 
                        (current.sumCostFromStart + int64(next.edgeCost))))
        }
    }
    return NO_PATH_FOUND
}

func createDirectedGraph(edges [][]int) [][]Point {
    graph := make([][]Point, numberOfNodes)
    for i := range graph {
        graph[i] = make([]Point, 0)
    }

    for i := range edges {
        from := edges[i][0]
        to := edges[i][1]
        cost := edges[i][2]
        graph[from] = append(graph[from], NewPoint(to, cost))
    }

    /*
       The sorting is needed so that the search is stopped when the cost of the current edge
       makes the total cost from start greater than the given limit(maxSumCost).
    */
    for node := range numberOfNodes {
        if len(graph[node]) > 0 {
            slices.SortFunc(graph[node], func(x Point, y Point) int { return x.edgeCost - y.edgeCost })
        }
    }
    return graph
}

type PriorityQueue []Step

func (pq PriorityQueue) Len() int {
    return len(pq)
}

func (pq PriorityQueue) Less(first int, second int) bool {
    return pq[first].minEdgeCostFromStart < pq[second].minEdgeCostFromStart
}

func (pq PriorityQueue) Swap(first int, second int) {
    pq[first], pq[second] = pq[second], pq[first]
}

func (pq *PriorityQueue) Push(object any) {
    *pq = append(*pq, object.(Step))
}

func (pq *PriorityQueue) Pop() any {
    step := (*pq)[pq.Len() - 1]
    *pq = (*pq)[0 : pq.Len() - 1]
    return step
}
