package puzzle;
import java.util.List;

public class App {

    public static void main(String[] args) {
        String initialState = "7621 3458"; // random initial state
        String goalState = "12345678"; // goal state
        SearchTree searchTree = new SearchTree(initialState, goalState);
        SearchTree.breadthFirstSearch();
        System.out.println("End");

        List<Node> children = new NodeUtils().generateChildren(new Node(initialState, null));
    }
    
}
