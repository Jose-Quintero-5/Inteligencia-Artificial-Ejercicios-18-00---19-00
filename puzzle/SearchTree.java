package puzzle;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public class SearchTree {
    private final String initialState;
    private final String goalState;
    private final Node root;

    public SearchTree(String initialState, String goalState) {
        this.initialState = initialState;
        this.goalState = goalState;
        this.root = new Node(initialState, null);
    }

    public SearchResult breadthFirstSearch() {
        long start = System.nanoTime();
        Queue<Node> queue = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        queue.add(root);
        visited.add(root.getState());
        int expanded = 0;
        int generated = 1;

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            expanded++;
            if (current.getState().equals(goalState)) {
                return result(true, current, expanded, generated, start);
            }
            for (Node child : NodeUtils.generateChildren(current)) {
                generated++;
                if (visited.add(child.getState())) {
                    queue.add(child);
                }
            }
        }
        return result(false, null, expanded, generated, start);
    }

    public SearchResult uniformCostSearch() {
        long start = System.nanoTime();
        PriorityQueue<Node> queue = new PriorityQueue<>(Comparator.comparingInt(Node::getCost));
        Map<String, Integer> bestCost = new HashMap<>();
        queue.add(root);
        bestCost.put(root.getState(), 0);
        int expanded = 0;
        int generated = 1;

        while (!queue.isEmpty()) {
            Node current = queue.poll();
            if (current.getCost() != bestCost.getOrDefault(current.getState(), Integer.MAX_VALUE)) {
                continue;
            }
            expanded++;
            if (current.getState().equals(goalState)) {
                return result(true, current, expanded, generated, start);
            }
            for (Node child : NodeUtils.generateChildren(current)) {
                generated++;
                int oldCost = bestCost.getOrDefault(child.getState(), Integer.MAX_VALUE);
                if (child.getCost() < oldCost) {
                    bestCost.put(child.getState(), child.getCost());
                    queue.add(child);
                }
            }
        }
        return result(false, null, expanded, generated, start);
    }

    public SearchResult depthFirstSearch() {
        long start = System.nanoTime();
        Deque<Node> stack = new ArrayDeque<>();
        Set<String> visited = new HashSet<>();
        stack.push(root);
        int expanded = 0;
        int generated = 1;

        while (!stack.isEmpty()) {
            Node current = stack.pop();
            if (!visited.add(current.getState())) {
                continue;
            }
            expanded++;
            if (current.getState().equals(goalState)) {
                return result(true, current, expanded, generated, start);
            }
            List<Node> children = NodeUtils.generateChildren(current);
            Collections.reverse(children);
            for (Node child : children) {
                generated++;
                if (!visited.contains(child.getState())) {
                    stack.push(child);
                }
            }
        }
        return result(false, null, expanded, generated, start);
    }

    public SearchResult depthLimitedSearch(int limit) {
        long start = System.nanoTime();
        SearchCounters counters = new SearchCounters();
        Node found = depthLimited(root, limit, new HashSet<>(), counters);
        return result(found != null, found, counters.expanded, counters.generated, start);
    }

    private Node depthLimited(Node current, int limit, Set<String> path, SearchCounters counters) {
        counters.expanded++;
        counters.generated++;
        if (current.getState().equals(goalState)) {
            return current;
        }
        if (current.getDepth() >= limit) {
            return null;
        }

        path.add(current.getState());
        for (Node child : NodeUtils.generateChildren(current)) {
            if (!path.contains(child.getState())) {
                Node found = depthLimited(child, limit, path, counters);
                if (found != null) {
                    return found;
                }
            }
        }
        path.remove(current.getState());
        return null;
    }

    public SearchResult iterativeDeepeningSearch() {
        long start = System.nanoTime();
        SearchCounters total = new SearchCounters();
        int limit = 0;
        while (true) {
            SearchCounters current = new SearchCounters();
            Node found = depthLimited(root, limit, new HashSet<>(), current);
            total.expanded += current.expanded;
            total.generated += current.generated;
            if (found != null) {
                return result(true, found, total.expanded, total.generated, start);
            }
            limit++;
        }
    }

    public SearchResult bidirectionalSearch() {
        long start = System.nanoTime();
        if (initialState.equals(goalState)) {
            return result(true, root, 1, 1, start);
        }

        Map<String, Node> forward = new HashMap<>();
        Map<String, Node> backward = new HashMap<>();
        Queue<Node> qForward = new ArrayDeque<>();
        Queue<Node> qBackward = new ArrayDeque<>();

        Node startNode = root;
        Node goalNode = new Node(goalState, null);
        forward.put(initialState, startNode);
        backward.put(goalState, goalNode);
        qForward.add(startNode);
        qBackward.add(goalNode);
        int expanded = 0;
        int generated = 2;

        while (!qForward.isEmpty() && !qBackward.isEmpty()) {
            int[] forwardStats = new int[2];
            Node meet = expandOneLevel(qForward, forward, backward, forwardStats);
            expanded += forwardStats[0];
            generated += forwardStats[1];
            if (meet != null) {
                Node fullGoal = buildBidirectionalPath(meet, backward.get(meet.getState()));
                return result(true, fullGoal, expanded, generated, start);
            }
            int[] backwardStats = new int[2];
            Node meetBack = expandOneLevel(qBackward, backward, forward, backwardStats);
            expanded += backwardStats[0];
            generated += backwardStats[1];
            if (meetBack != null) {
                Node fullGoal = buildBidirectionalPath(forward.get(meetBack.getState()), meetBack);
                return result(true, fullGoal, expanded, generated, start);
            }
        }
        return result(false, null, expanded, generated, start);
    }

    private Node expandOneLevel(Queue<Node> queue, Map<String, Node> own, Map<String, Node> other, int[] stats) {
        int levelSize = queue.size();
        for (int i = 0; i < levelSize; i++) {
            Node current = queue.poll();
            stats[0]++;
            for (Node child : NodeUtils.generateChildren(current)) {
                stats[1]++;
                if (!own.containsKey(child.getState())) {
                    own.put(child.getState(), child);
                    queue.add(child);
                    if (other.containsKey(child.getState())) {
                        return child;
                    }
                }
            }
        }
        return null;
    }

    private Node buildBidirectionalPath(Node forwardMeet, Node backwardMeet) {
        List<String> states = new ArrayList<>();
        Node current = forwardMeet;
        while (current != null) {
            states.add(current.getState());
            current = current.getParent();
        }
        Collections.reverse(states);

        current = backwardMeet.getParent();
        while (current != null) {
            states.add(current.getState());
            current = current.getParent();
        }

        Node parent = null;
        Node result = null;
        for (String state : states) {
            result = new Node(state, parent);
            parent = result;
        }
        return result;
    }

    private SearchResult result(boolean found, Node node, int expanded, int generated, long start) {
        return new SearchResult(found, node, expanded, generated, System.nanoTime() - start);
    }

    private static class SearchCounters {
        int expanded = 0;
        int generated = 0;
    }
}
