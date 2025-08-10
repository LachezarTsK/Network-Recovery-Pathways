
#include <span>
#include <queue>
#include <limits>
#include <ranges>
#include <vector>
#include <algorithm>
using namespace std;

class Solution {

    struct Point {
        int node{};
        int edgeCost{};
        Point(int node, int edgeCost) :node{ node }, edgeCost{ edgeCost }{}
    };

    struct Step {
        int node{};
        int minEdgeCostFromStart{};
        long long sumCostFromStart;

        Step() = default;
        Step(int node, int minEdgeCostFromStart, long sumCostFromStart) :
            node{ node }, minEdgeCostFromStart{ minEdgeCostFromStart }, sumCostFromStart{ sumCostFromStart }{}
    };

    struct ComparatorStep {
        bool operator()(const Step& x, const Step& y) {
            return x.minEdgeCostFromStart > y.minEdgeCostFromStart;
        }
    };

    inline static const int NO_PATH_FOUND = -1;
    inline static const int START_NODE = 0;
    inline static const int POSITIVE_INFINITY = numeric_limits<int>::max();
    inline static  int MAX_COST = pow(10, 9);

    int numberOfNodes;
    vector<vector<Point>> directedGraph;

public:
    int findMaxPathScore(vector<vector<int>>& edges, vector<bool>& online, long long maxSumCost) {
        if (edges.size() == 0) {
            return NO_PATH_FOUND;
        }
        numberOfNodes = online.size();
        directedGraph = createDirectedGraph(edges);
        return searchForMaxPathScore(online, maxSumCost);
    }


private:
    int searchForMaxPathScore(const vector<bool>& online, long long maxSumCost) const {
        int lowerEdgeCost = 0;
        int upperEdgeCost = MAX_COST;
        int maxPathScore = NO_PATH_FOUND;

        while (lowerEdgeCost <= upperEdgeCost) {
            int minTargetEdgeCost = lowerEdgeCost + (upperEdgeCost - lowerEdgeCost) / 2;
            int edgeCost = findPathWithScoreNotLessThanMinTargetEdgeCost(minTargetEdgeCost, maxSumCost, online);

            if (edgeCost != NO_PATH_FOUND) {
                maxPathScore = max(maxPathScore, edgeCost);
                lowerEdgeCost = minTargetEdgeCost + 1;
            }
            else {
                upperEdgeCost = minTargetEdgeCost - 1;
            }
        }
        return maxPathScore;
    }

    int findPathWithScoreNotLessThanMinTargetEdgeCost(int minTargetEdgeCost, long long maxSumCost, const vector<bool>& online) const {
        priority_queue<Step, vector<Step>, ComparatorStep> minHeapForEdgeCost;
        minHeapForEdgeCost.emplace(START_NODE, POSITIVE_INFINITY, 0);

        vector<int> minEdgeCost(numberOfNodes, POSITIVE_INFINITY);
        minEdgeCost[START_NODE] = 0;

        while (!minHeapForEdgeCost.empty()) {

                Step current = minHeapForEdgeCost.top();
                minHeapForEdgeCost.pop();

                if (current.node == numberOfNodes - 1) {
                        return current.minEdgeCostFromStart;
                }
                if (directedGraph[current.node].empty()) {
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
                minHeapForEdgeCost.emplace(next.node,
                                           min(next.edgeCost, current.minEdgeCostFromStart),
                                           (current.sumCostFromStart + next.edgeCost));
            }
        }
        return NO_PATH_FOUND;
    }

    vector<vector<Point>> createDirectedGraph(span<const vector<int>> edges) {
        vector<vector<Point>> graph(numberOfNodes);

        for (const auto& edge : edges) {
            int from = edge[0];
            int to = edge[1];
            int cost = edge[2];
            graph[from].emplace_back(to, cost);
        }

        /*
        The sorting is needed so that the search is stopped when the cost of the current edge
        makes the total cost from start greater than the given limit(maxSumCost).
         */
        for (int node = 0; node < numberOfNodes; ++node) {
            if (!graph[node].empty()) {
                ranges::sort(graph[node], [](const Point& x, const Point& y) {return x.edgeCost < y.edgeCost; });
            }
        }
        return graph;
    }
};
