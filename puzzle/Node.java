package puzzle;
//TODO Private attributes
public class Node {
    private String state;
    private Node parent;
    private int depth;

    public Node(String state, Node parent) {
        this.state = state;
        this.parent = parent;
    }
    public String getState() {
        return state;
    }
    void setState(String state) {
        this.state = state;
    }
    public Node getParent() {
        return parent;
    }
    void setParent(Node parent) {
        this.parent = parent;
    }
    public int getDepth() {
        return depth;
    }
    void setDepth(int depth) {
        this.depth = depth;
    }
}
