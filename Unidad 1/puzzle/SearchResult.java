package puzzle;

public class SearchResult {
    private final boolean found;
    private final Node goalNode;
    private final int nodesExpanded;
    private final int nodesGenerated;
    private final long timeNanos;

    public SearchResult(boolean found, Node goalNode, int nodesExpanded, int nodesGenerated, long timeNanos) {
        this.found = found;
        this.goalNode = goalNode;
        this.nodesExpanded = nodesExpanded;
        this.nodesGenerated = nodesGenerated;
        this.timeNanos = timeNanos;
    }

    public boolean isFound() { return found; }
    public Node getGoalNode() { return goalNode; }
    public int getNodesExpanded() { return nodesExpanded; }
    public int getNodesGenerated() { return nodesGenerated; }
    public long getTimeNanos() { return timeNanos; }
    public double getTimeSeconds() { return timeNanos / 1_000_000_000.0; }
}
